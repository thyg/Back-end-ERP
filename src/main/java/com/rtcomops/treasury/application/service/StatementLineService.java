package com.rtcomops.treasury.application.service;

import com.rtcomops.treasury.application.dto.request.CreateStatementLineRequest;
import com.rtcomops.treasury.application.dto.response.StatementLineResponse;
import com.rtcomops.treasury.application.mapper.StatementLineMapper;
import com.rtcomops.treasury.domain.exception.ResourceNotFoundException;
import com.rtcomops.treasury.domain.model.BankStatement;
import com.rtcomops.treasury.domain.model.StatementLine;
import com.rtcomops.treasury.application.port.in.StatementLineUseCase;
import com.rtcomops.treasury.domain.port.out.BankStatementRepositoryPort;
import com.rtcomops.treasury.domain.port.out.StatementLineRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Domain service for StatementLine operations.
 *
 * <p>Implements the StatementLineUseCase port and provides business logic
 * for managing statement lines, including CRUD operations.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Service
@Transactional
public class StatementLineService implements StatementLineUseCase {

    private static final Logger LOG = LoggerFactory.getLogger(StatementLineService.class);
    private static final String RESOURCE_NAME = "StatementLine";

    private final StatementLineRepositoryPort lineRepositoryPort;
    private final BankStatementRepositoryPort statementRepositoryPort;
    private final StatementLineMapper lineMapper;

    public StatementLineService(
            StatementLineRepositoryPort lineRepositoryPort,
            BankStatementRepositoryPort statementRepositoryPort,
            StatementLineMapper lineMapper) {
        this.lineRepositoryPort = lineRepositoryPort;
        this.statementRepositoryPort = statementRepositoryPort;
        this.lineMapper = lineMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<StatementLineResponse> findByStatementId(UUID statementId) {
        LOG.debug("Finding lines for statement id={}", statementId);
        return lineRepositoryPort.findByBankStatementIdOrderByLineNumber(statementId)
            .map(lineMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<StatementLineResponse> findById(UUID id) {
        LOG.debug("Finding statement line by id={}", id);
        return lineRepositoryPort.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .map(lineMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<StatementLineResponse> findUnmatchedByStatementId(UUID statementId) {
        LOG.debug("Finding unmatched lines for statement id={}", statementId);
        return lineRepositoryPort.findUnmatchedByStatementId(statementId)
            .map(lineMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<StatementLineResponse> findMatchedByStatementId(UUID statementId) {
        LOG.debug("Finding matched lines for statement id={}", statementId);
        return lineRepositoryPort.findMatchedByStatementId(statementId)
            .map(lineMapper::toResponse);
    }

    @Override
    public Mono<StatementLineResponse> create(CreateStatementLineRequest request) {
        LOG.info("Creating statement line for statement={}", request.getBankStatementId());

        return statementRepositoryPort.findById(request.getBankStatementId())
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("BankStatement", request.getBankStatementId())))
            .flatMap(statement ->
                lineRepositoryPort.findMaxLineNumberByStatementId(request.getBankStatementId())
                    .defaultIfEmpty(0)
                    .flatMap(maxLineNumber -> {
                        int nextLineNumber = maxLineNumber + 1;
                        StatementLine domain = lineMapper.toDomain(request, nextLineNumber);
                        return lineRepositoryPort.save(domain)
                            .doOnSuccess(saved -> LOG.info("Statement line created: id={}, lineNumber={}",
                                saved.getId(), saved.getLineNumber()))
                            .flatMap(saved -> updateStatementTotals(statement, saved)
                                .thenReturn(lineMapper.toResponse(saved)));
                    }));
    }

    @Override
    public Flux<StatementLineResponse> createBatch(UUID statementId, List<CreateStatementLineRequest> requests) {
        LOG.info("Creating {} statement lines for statement={}", requests.size(), statementId);

        return statementRepositoryPort.findById(statementId)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("BankStatement", statementId)))
            .flatMapMany(statement ->
                lineRepositoryPort.findMaxLineNumberByStatementId(statementId)
                    .defaultIfEmpty(0)
                    .flatMapMany(maxLineNumber -> {
                        int[] currentLineNumber = {maxLineNumber};
                        return Flux.fromIterable(requests)
                            .map(request -> {
                                request.setBankStatementId(statementId);
                                return lineMapper.toDomain(request, ++currentLineNumber[0]);
                            })
                            .collectList()
                            .flatMapMany(entities -> lineRepositoryPort.saveAll(entities))
                            .map(lineMapper::toResponse);
                    }));
    }

    @Override
    public Mono<Void> delete(UUID id) {
        LOG.info("Deleting statement line id={}", id);

        return lineRepositoryPort.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(line -> lineRepositoryPort.delete(line)
                .doOnSuccess(v -> LOG.info("Statement line deleted: id={}", id)));
    }

    @Override
    public Mono<StatementLineResponse> ignore(UUID id) {
        LOG.info("Marking statement line as ignored: id={}", id);

        return lineRepositoryPort.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(line -> {
                line.markAsIgnored();
                return lineRepositoryPort.save(line)
                    .map(lineMapper::toResponse);
            });
    }

    @Override
    public Mono<StatementLineResponse> resetToUnmatched(UUID id) {
        LOG.info("Resetting statement line to unmatched: id={}", id);

        return lineRepositoryPort.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(line -> {
                line.markAsUnmatched();
                return lineRepositoryPort.save(line)
                    .map(lineMapper::toResponse);
            });
    }

    @Override
    public Mono<StatementLineResponse> markAsMatched(UUID id) {
        LOG.info("Marking statement line as matched: id={}", id);

        return lineRepositoryPort.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(line -> {
                line.markAsMatched();
                return lineRepositoryPort.save(line)
                    .map(lineMapper::toResponse);
            });
    }

    /**
     * Updates the parent statement's totals after adding a line.
     *
     * @param statement the bank statement
     * @param line the new line
     * @return Mono of void
     */
    private Mono<Void> updateStatementTotals(BankStatement statement, StatementLine line) {
        if ("CREDIT".equals(line.getDirection())) {
            statement.setTotalCredits(statement.getTotalCredits().add(line.getAmount()));
        } else {
            statement.setTotalDebits(statement.getTotalDebits().add(line.getAmount()));
        }
        statement.setLineCount(statement.getLineCount() + 1);
        statement.setUpdatedAt(LocalDateTime.now());

        return statementRepositoryPort.save(statement).then();
    }
}
