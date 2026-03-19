package com.rtcomops.treasury.application.service;

import com.rtcomops.treasury.application.dto.request.CreateBankStatementRequest;
import com.rtcomops.treasury.application.dto.request.UpdateBankStatementRequest;
import com.rtcomops.treasury.application.dto.response.BankStatementResponse;
import com.rtcomops.treasury.application.mapper.BankStatementMapper;
import com.rtcomops.treasury.domain.exception.DuplicateResourceException;
import com.rtcomops.treasury.domain.exception.ResourceNotFoundException;
import com.rtcomops.treasury.domain.model.BankStatement;
import com.rtcomops.treasury.domain.model.enums.AuditAction;
import com.rtcomops.treasury.domain.model.enums.AuditModule;
import com.rtcomops.treasury.application.port.in.AuditLogUseCase;
import com.rtcomops.treasury.application.port.in.BankStatementUseCase;
import com.rtcomops.treasury.domain.port.out.BankAccountRepositoryPort;
import com.rtcomops.treasury.domain.port.out.BankStatementRepositoryPort;
import com.rtcomops.treasury.domain.port.out.StatementLineRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Domain service for BankStatement operations.
 *
 * <p>Implements the BankStatementUseCase port and provides business logic
 * for managing bank statements, including CRUD operations with validation.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Service
@Transactional
public class BankStatementService implements BankStatementUseCase {

    private static final Logger LOG = LoggerFactory.getLogger(BankStatementService.class);
    private static final String RESOURCE_NAME = "BankStatement";

    private final BankStatementRepositoryPort statementRepositoryPort;
    private final BankAccountRepositoryPort accountRepositoryPort;
    private final StatementLineRepositoryPort lineRepositoryPort;
    private final BankStatementMapper statementMapper;
    private final AuditLogUseCase auditLogUseCase;

    public BankStatementService(
            BankStatementRepositoryPort statementRepositoryPort,
            BankAccountRepositoryPort accountRepositoryPort,
            StatementLineRepositoryPort lineRepositoryPort,
            BankStatementMapper statementMapper,
            AuditLogUseCase auditLogUseCase) {
        this.statementRepositoryPort = statementRepositoryPort;
        this.accountRepositoryPort = accountRepositoryPort;
        this.lineRepositoryPort = lineRepositoryPort;
        this.statementMapper = statementMapper;
        this.auditLogUseCase = auditLogUseCase;
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<BankStatementResponse> findAll() {
        LOG.debug("Finding all bank statements");
        return statementRepositoryPort.findAllOrderByDateDesc()
            .flatMap(this::enrichWithAccountName);
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<BankStatementResponse> findById(UUID id) {
        LOG.debug("Finding bank statement by id={}", id);
        return statementRepositoryPort.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(this::enrichWithAccountName);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<BankStatementResponse> findByAccountId(UUID accountId) {
        LOG.debug("Finding bank statements by accountId={}", accountId);
        return statementRepositoryPort.findByBankAccountId(accountId)
            .flatMap(this::enrichWithAccountName);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<BankStatementResponse> findByStatus(String status) {
        LOG.debug("Finding bank statements by status={}", status);
        return statementRepositoryPort.findByStatus(status.toUpperCase())
            .flatMap(this::enrichWithAccountName);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<BankStatementResponse> findByAccountIdAndDateRange(
            UUID accountId, LocalDate startDate, LocalDate endDate) {
        LOG.debug("Finding bank statements by accountId={} from {} to {}", accountId, startDate, endDate);
        return statementRepositoryPort.findByAccountIdAndDateRange(accountId, startDate, endDate)
            .flatMap(this::enrichWithAccountName);
    }

    @Override
    public Mono<BankStatementResponse> create(CreateBankStatementRequest request) {
        LOG.info("Creating bank statement for account={}, period={} to {}",
            request.getBankAccountId(), request.getPeriodStart(), request.getPeriodEnd());

        return accountRepositoryPort.findById(request.getBankAccountId())
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("BankAccount", request.getBankAccountId())))
            .flatMap(account -> statementRepositoryPort.existsByAccountIdAndPeriod(
                    request.getBankAccountId(), request.getPeriodStart(), request.getPeriodEnd())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new DuplicateResourceException(
                            RESOURCE_NAME, "period",
                            request.getPeriodStart() + " - " + request.getPeriodEnd()));
                    }
                    BankStatement domain = statementMapper.toDomain(request);
                    return statementRepositoryPort.save(domain)
                        .flatMap(saved -> auditLogUseCase.log(
                                AuditModule.BANK_STATEMENT,
                                AuditAction.CREATE,
                                saved.getId(),
                                saved.getReference(),
                                null,
                                saved,
                                "Création du relevé " + saved.getReference()
                        ).thenReturn(saved))
                        .doOnSuccess(saved -> LOG.info("Bank statement created: id={}", saved.getId()))
                        .map(saved -> statementMapper.toResponseWithAccountName(saved, account.getName()));
                }));
    }

    @Override
    public Mono<BankStatementResponse> update(UUID id, UpdateBankStatementRequest request) {
        LOG.info("Updating bank statement id={}", id);

        return statementRepositoryPort.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(existing -> {
                BankStatement original = existing.toBuilder().build();
                BankStatement updated = statementMapper.updateDomain(existing, request);
                return statementRepositoryPort.save(updated)
                    .flatMap(saved -> auditLogUseCase.log(
                            AuditModule.BANK_STATEMENT,
                            AuditAction.UPDATE,
                            saved.getId(),
                            saved.getReference(),
                            original,
                            saved,
                            "Mise à jour du relevé " + saved.getReference()
                    ).thenReturn(saved))
                    .flatMap(this::enrichWithAccountName);
            });
    }

    @Override
    public Mono<Void> delete(UUID id) {
        LOG.info("Deleting bank statement id={}", id);

        return statementRepositoryPort.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(statement -> auditLogUseCase.log(
                    AuditModule.BANK_STATEMENT,
                    AuditAction.DELETE,
                    statement.getId(),
                    statement.getReference(),
                    statement,
                    null,
                    "Suppression du relevé " + statement.getReference()
            ).then(lineRepositoryPort.deleteByStatementId(id))
             .then(statementRepositoryPort.delete(statement))
             .doOnSuccess(v -> LOG.info("Bank statement deleted: id={}", id)));
    }

    @Override
    public Mono<BankStatementResponse> updateTotals(UUID id) {
        LOG.debug("Updating totals for statement id={}", id);

        return statementRepositoryPort.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(statement -> {
                BankStatement original = statement.toBuilder().build();
                return lineRepositoryPort.findByBankStatementIdOrderByLineNumber(id)
                    .collectList()
                    .flatMap(lines -> {
                        BigDecimal totalCredits = BigDecimal.ZERO;
                        BigDecimal totalDebits = BigDecimal.ZERO;
                        int reconciledCount = 0;

                        for (var line : lines) {
                            if ("CREDIT".equals(line.getDirection())) {
                                totalCredits = totalCredits.add(line.getAmount());
                            } else {
                                totalDebits = totalDebits.add(line.getAmount());
                            }
                            if ("MATCHED".equals(line.getReconciliationStatus())) {
                                reconciledCount++;
                            }
                        }

                        statement.setTotalCredits(totalCredits);
                        statement.setTotalDebits(totalDebits);
                        statement.setLineCount(lines.size());
                        statement.setReconciledCount(reconciledCount);

                        return statementRepositoryPort.save(statement)
                            .flatMap(saved -> auditLogUseCase.log(
                                AuditModule.BANK_STATEMENT,
                                AuditAction.UPDATE,
                                saved.getId(),
                                saved.getReference(),
                                original,
                                saved,
                                "Mise à jour des totaux du relevé " + saved.getReference()
                            ).thenReturn(saved));
                    });
            })
            .flatMap(this::enrichWithAccountName);
    }

    @Override
    public Mono<BankStatementResponse> close(UUID id) {
        LOG.info("Closing bank statement id={}", id);

        return statementRepositoryPort.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(statement -> {
                BankStatement original = statement.toBuilder().build();
                statement.setStatus("CLOSED");
                return statementRepositoryPort.save(statement)
                    .flatMap(saved -> auditLogUseCase.log(
                            AuditModule.BANK_STATEMENT,
                            AuditAction.CLOSE,
                            saved.getId(),
                            saved.getReference(),
                            original,
                            saved,
                            "Clôture du relevé " + saved.getReference()
                    ).thenReturn(saved))
                    .flatMap(this::enrichWithAccountName);
            });
    }

    /**
     * Enriches a bank statement with its account name.
     *
     * @param statement the bank statement
     * @return Mono of BankStatementResponse with account name
     */
    private Mono<BankStatementResponse> enrichWithAccountName(BankStatement statement) {
        return accountRepositoryPort.findById(statement.getBankAccountId())
            .map(account -> statementMapper.toResponseWithAccountName(statement, account.getName()))
            .defaultIfEmpty(statementMapper.toResponse(statement));
    }
}
