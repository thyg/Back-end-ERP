package com.rtcomops.treasury.service;

import com.rtcomops.treasury.dto.request.CreateBankStatementRequest;
import com.rtcomops.treasury.dto.request.UpdateBankStatementRequest;
import com.rtcomops.treasury.dto.response.BankStatementResponse;
import com.rtcomops.treasury.entity.BankStatement;
import com.rtcomops.treasury.exception.DuplicateResourceException;
import com.rtcomops.treasury.exception.ResourceNotFoundException;
import com.rtcomops.treasury.mapper.BankStatementMapper;
import com.rtcomops.treasury.repository.BankAccountRepository;
import com.rtcomops.treasury.repository.BankStatementRepository;
import com.rtcomops.treasury.repository.StatementLineRepository;
import com.rtcomops.treasury.service.AuditLogService;
import com.rtcomops.treasury.enums.AuditAction;
import com.rtcomops.treasury.enums.AuditModule;
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
 * Service layer for BankStatement operations.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Service
@Transactional
public class BankStatementService {

    private static final Logger LOG = LoggerFactory.getLogger(BankStatementService.class);
    private static final String RESOURCE_NAME = "BankStatement";

    private final BankStatementRepository statementRepository;
    private final BankAccountRepository accountRepository;
    private final StatementLineRepository lineRepository;
    private final BankStatementMapper statementMapper;
    private final AuditLogService auditLogService;

    public BankStatementService(
            BankStatementRepository statementRepository,
            BankAccountRepository accountRepository,
            StatementLineRepository lineRepository,
            BankStatementMapper statementMapper,
            AuditLogService auditLogService) {
        this.statementRepository = statementRepository;
        this.accountRepository = accountRepository;
        this.lineRepository = lineRepository;
        this.statementMapper = statementMapper;
        this.auditLogService = auditLogService;
    }

    /**
     * Retrieves all bank statements ordered by date descending.
     *
     * @return flux of bank statement responses
     */
    @Transactional(readOnly = true)
    public Flux<BankStatementResponse> findAll() {
        LOG.debug("Finding all bank statements");
        return statementRepository.findAllOrderByDateDesc()
            .flatMap(this::enrichWithAccountName);
    }

    /**
     * Retrieves a bank statement by its ID.
     *
     * @param id the statement ID
     * @return mono of bank statement response
     */
    @Transactional(readOnly = true)
    public Mono<BankStatementResponse> findById(UUID id) {
        LOG.debug("Finding bank statement by id={}", id);
        return statementRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(this::enrichWithAccountName);
    }

    /**
     * Retrieves bank statements by bank account ID.
     *
     * @param accountId the bank account ID
     * @return flux of bank statement responses
     */
    @Transactional(readOnly = true)
    public Flux<BankStatementResponse> findByAccountId(UUID accountId) {
        LOG.debug("Finding bank statements by accountId={}", accountId);
        return statementRepository.findByBankAccountId(accountId)
            .flatMap(this::enrichWithAccountName);
    }

    /**
     * Retrieves bank statements by status.
     *
     * @param status the statement status
     * @return flux of bank statement responses
     */
    @Transactional(readOnly = true)
    public Flux<BankStatementResponse> findByStatus(String status) {
        LOG.debug("Finding bank statements by status={}", status);
        return statementRepository.findByStatus(status.toUpperCase())
            .flatMap(this::enrichWithAccountName);
    }

    /**
     * Retrieves bank statements by account ID and date range.
     *
     * @param accountId the bank account ID
     * @param startDate the start date
     * @param endDate the end date
     * @return flux of bank statement responses
     */
    @Transactional(readOnly = true)
    public Flux<BankStatementResponse> findByAccountIdAndDateRange(
            UUID accountId, LocalDate startDate, LocalDate endDate) {
        LOG.debug("Finding bank statements by accountId={} from {} to {}", accountId, startDate, endDate);
        return statementRepository.findByAccountIdAndDateRange(accountId, startDate, endDate)
            .flatMap(this::enrichWithAccountName);
    }

    /**
     * Creates a new bank statement.
     *
     * @param request the create request
     * @return mono of created bank statement response
     */
    public Mono<BankStatementResponse> create(CreateBankStatementRequest request) {
        LOG.info("Creating bank statement for account={}, period={} to {}", 
            request.getBankAccountId(), request.getPeriodStart(), request.getPeriodEnd());
        
        return accountRepository.findById(request.getBankAccountId())
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("BankAccount", request.getBankAccountId())))
            .flatMap(account -> statementRepository.existsByAccountIdAndPeriod(
                    request.getBankAccountId(), request.getPeriodStart(), request.getPeriodEnd())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new DuplicateResourceException(
                            RESOURCE_NAME, "period", 
                            request.getPeriodStart() + " - " + request.getPeriodEnd()));
                    }
                    BankStatement entity = statementMapper.toEntity(request);
                    return statementRepository.save(entity)
                        .flatMap(saved -> auditLogService.log(
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

    /**
     * Updates an existing bank statement.
     *
     * @param id the statement ID
     * @param request the update request
     * @return mono of updated bank statement response
     */
    public Mono<BankStatementResponse> update(UUID id, UpdateBankStatementRequest request) {
        LOG.info("Updating bank statement id={}", id);
        
        return statementRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(existing -> {
                BankStatement original = existing.toBuilder().build();
                BankStatement updated = statementMapper.updateEntity(existing, request);
                return statementRepository.save(updated)
                    .flatMap(saved -> auditLogService.log(
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

    /**
     * Deletes a bank statement and all its lines.
     *
     * @param id the statement ID
     * @return mono of void
     */
    public Mono<Void> delete(UUID id) {
        LOG.info("Deleting bank statement id={}", id);
        
        return statementRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(statement -> auditLogService.log(
                    AuditModule.BANK_STATEMENT,
                    AuditAction.DELETE,
                    statement.getId(),
                    statement.getReference(),
                    statement,
                    null,
                    "Suppression du relevé " + statement.getReference()
            ).then(statementRepository.delete(statement).then(Mono.fromRunnable(() -> LOG.info("Bank statement deleted: id={}", id)))));
    }

    /**
     * Updates the statement totals and line count.
     *
     * @param id the statement ID
     * @return mono of updated bank statement response
     */
    public Mono<BankStatementResponse> updateTotals(UUID id) {
        LOG.debug("Updating totals for statement id={}", id);
        
        return statementRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(statement -> {
                BankStatement original = statement.toBuilder().build();
                return lineRepository.findByBankStatementIdOrderByLineNumber(id)
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
                        statement.setNew(false);
                        
                        return statementRepository.save(statement)
                            .flatMap(saved -> auditLogService.log(
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

    /**
     * Closes a bank statement after reconciliation is complete.
     *
     * @param id the statement ID
     * @return mono of closed bank statement response
     */
    public Mono<BankStatementResponse> close(UUID id) {
        LOG.info("Closing bank statement id={}", id);
        
        return statementRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(statement -> {
                BankStatement original = statement.toBuilder().build();
                statement.setStatus("CLOSED");
                statement.setNew(false);
                return statementRepository.save(statement)
                    .flatMap(saved -> auditLogService.log(
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
     * @return mono of bank statement response with account name
     */
    private Mono<BankStatementResponse> enrichWithAccountName(BankStatement statement) {
        return accountRepository.findById(statement.getBankAccountId())
            .map(account -> statementMapper.toResponseWithAccountName(statement, account.getName()))
            .defaultIfEmpty(statementMapper.toResponse(statement));
    }
}
