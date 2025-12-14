package com.rtcomops.treasury.service;

import com.rtcomops.treasury.dto.request.CreateStatementLineRequest;
import com.rtcomops.treasury.dto.response.StatementLineResponse;
import com.rtcomops.treasury.entity.BankStatement;
import com.rtcomops.treasury.entity.StatementLine;
import com.rtcomops.treasury.exception.ResourceNotFoundException;
import com.rtcomops.treasury.mapper.StatementLineMapper;
import com.rtcomops.treasury.repository.BankStatementRepository;
import com.rtcomops.treasury.repository.StatementLineRepository;
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
 * Service layer for StatementLine operations.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Service
@Transactional
public class StatementLineService {

    private static final Logger LOG = LoggerFactory.getLogger(StatementLineService.class);
    private static final String RESOURCE_NAME = "StatementLine";

    private final StatementLineRepository lineRepository;
    private final BankStatementRepository statementRepository;
    private final StatementLineMapper lineMapper;

    public StatementLineService(
            StatementLineRepository lineRepository,
            BankStatementRepository statementRepository,
            StatementLineMapper lineMapper) {
        this.lineRepository = lineRepository;
        this.statementRepository = statementRepository;
        this.lineMapper = lineMapper;
    }

    /**
     * Retrieves all lines for a bank statement.
     *
     * @param statementId the statement ID
     * @return flux of statement line responses
     */
    @Transactional(readOnly = true)
    public Flux<StatementLineResponse> findByStatementId(UUID statementId) {
        LOG.debug("Finding lines for statement id={}", statementId);
        return lineRepository.findByBankStatementIdOrderByLineNumber(statementId)
            .map(lineMapper::toResponse);
    }

    /**
     * Retrieves a statement line by its ID.
     *
     * @param id the line ID
     * @return mono of statement line response
     */
    @Transactional(readOnly = true)
    public Mono<StatementLineResponse> findById(UUID id) {
        LOG.debug("Finding statement line by id={}", id);
        return lineRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .map(lineMapper::toResponse);
    }

    /**
     * Retrieves unmatched lines for a statement.
     *
     * @param statementId the statement ID
     * @return flux of unmatched statement line responses
     */
    @Transactional(readOnly = true)
    public Flux<StatementLineResponse> findUnmatchedByStatementId(UUID statementId) {
        LOG.debug("Finding unmatched lines for statement id={}", statementId);
        return lineRepository.findUnmatchedByStatementId(statementId)
            .map(lineMapper::toResponse);
    }

    /**
     * Retrieves matched lines for a statement.
     *
     * @param statementId the statement ID
     * @return flux of matched statement line responses
     */
    @Transactional(readOnly = true)
    public Flux<StatementLineResponse> findMatchedByStatementId(UUID statementId) {
        LOG.debug("Finding matched lines for statement id={}", statementId);
        return lineRepository.findMatchedByStatementId(statementId)
            .map(lineMapper::toResponse);
    }

    /**
     * Creates a new statement line.
     *
     * @param request the create request
     * @return mono of created statement line response
     */
    public Mono<StatementLineResponse> create(CreateStatementLineRequest request) {
        LOG.info("Creating statement line for statement={}", request.getBankStatementId());
        
        return statementRepository.findById(request.getBankStatementId())
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("BankStatement", request.getBankStatementId())))
            .flatMap(statement -> 
                lineRepository.findMaxLineNumberByStatementId(request.getBankStatementId())
                    .defaultIfEmpty(0)
                    .flatMap(maxLineNumber -> {
                        int nextLineNumber = maxLineNumber + 1;
                        StatementLine entity = lineMapper.toEntity(request, nextLineNumber);
                        return lineRepository.save(entity)
                            .doOnSuccess(saved -> LOG.info("Statement line created: id={}, lineNumber={}", 
                                saved.getId(), saved.getLineNumber()))
                            .flatMap(saved -> updateStatementTotals(statement, saved)
                                .thenReturn(lineMapper.toResponse(saved)));
                    }));
    }

    /**
     * Creates multiple statement lines in batch.
     *
     * @param statementId the statement ID
     * @param requests list of create requests
     * @return flux of created statement line responses
     */
    public Flux<StatementLineResponse> createBatch(UUID statementId, List<CreateStatementLineRequest> requests) {
        LOG.info("Creating {} statement lines for statement={}", requests.size(), statementId);
        
        return statementRepository.findById(statementId)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("BankStatement", statementId)))
            .flatMapMany(statement -> 
                lineRepository.findMaxLineNumberByStatementId(statementId)
                    .defaultIfEmpty(0)
                    .flatMapMany(maxLineNumber -> {
                        int[] currentLineNumber = {maxLineNumber};
                        return Flux.fromIterable(requests)
                            .map(request -> {
                                request.setBankStatementId(statementId);
                                return lineMapper.toEntity(request, ++currentLineNumber[0]);
                            })
                            .collectList()
                            .flatMapMany(entities -> lineRepository.saveAll(entities))
                            .map(lineMapper::toResponse);
                    }));
    }

    /**
     * Deletes a statement line.
     *
     * @param id the line ID
     * @return mono of void
     */
    public Mono<Void> delete(UUID id) {
        LOG.info("Deleting statement line id={}", id);
        
        return lineRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(line -> lineRepository.delete(line)
                .doOnSuccess(v -> LOG.info("Statement line deleted: id={}", id)));
    }

    /**
     * Marks a line as ignored.
     *
     * @param id the line ID
     * @return mono of updated statement line response
     */
    public Mono<StatementLineResponse> ignore(UUID id) {
        LOG.info("Marking statement line as ignored: id={}", id);
        
        return lineRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(line -> {
                StatementLine updated = lineMapper.markAsIgnored(line);
                return lineRepository.save(updated)
                    .map(lineMapper::toResponse);
            });
    }

    /**
     * Resets a line to unmatched status.
     *
     * @param id the line ID
     * @return mono of updated statement line response
     */
    public Mono<StatementLineResponse> resetToUnmatched(UUID id) {
        LOG.info("Resetting statement line to unmatched: id={}", id);
        
        return lineRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(line -> {
                StatementLine updated = lineMapper.markAsUnmatched(line);
                return lineRepository.save(updated)
                    .map(lineMapper::toResponse);
            });
    }

    /**
     * Updates the parent statement's totals after adding a line.
     *
     * @param statement the bank statement
     * @param line the new line
     * @return mono of void
     */
    private Mono<Void> updateStatementTotals(BankStatement statement, StatementLine line) {
        if ("CREDIT".equals(line.getDirection())) {
            statement.setTotalCredits(statement.getTotalCredits().add(line.getAmount()));
        } else {
            statement.setTotalDebits(statement.getTotalDebits().add(line.getAmount()));
        }
        statement.setLineCount(statement.getLineCount() + 1);
        statement.setNew(false);
        
        return statementRepository.save(statement).then();
    }
}
