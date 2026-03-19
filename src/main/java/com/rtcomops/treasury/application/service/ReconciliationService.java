package com.rtcomops.treasury.application.service;

import com.rtcomops.treasury.application.dto.request.AutoReconcileRequest;
import com.rtcomops.treasury.application.dto.request.ReconcileManualRequest;
import com.rtcomops.treasury.application.dto.response.ReconciliationMatchResponse;
import com.rtcomops.treasury.application.dto.response.ReconciliationSummaryResponse;
import com.rtcomops.treasury.application.mapper.ReconciliationMatchMapper;
import com.rtcomops.treasury.application.mapper.StatementLineMapper;
import com.rtcomops.treasury.domain.exception.BusinessException;
import com.rtcomops.treasury.domain.exception.ResourceNotFoundException;
import com.rtcomops.treasury.domain.model.BankStatement;
import com.rtcomops.treasury.domain.model.BankTransaction;
import com.rtcomops.treasury.domain.model.Check;
import com.rtcomops.treasury.domain.model.CheckDeposit;
import com.rtcomops.treasury.domain.model.ReconciliationMatch;
import com.rtcomops.treasury.domain.model.StatementLine;
import com.rtcomops.treasury.domain.model.TransactionType;
import com.rtcomops.treasury.domain.model.enums.AuditAction;
import com.rtcomops.treasury.domain.model.enums.AuditModule;
import com.rtcomops.treasury.application.port.in.AuditLogUseCase;
import com.rtcomops.treasury.application.port.in.ReconciliationUseCase;
import com.rtcomops.treasury.domain.port.out.BankAccountRepositoryPort;
import com.rtcomops.treasury.domain.port.out.BankStatementRepositoryPort;
import com.rtcomops.treasury.domain.port.out.BankTransactionRepositoryPort;
import com.rtcomops.treasury.domain.port.out.CheckDepositRepositoryPort;
import com.rtcomops.treasury.domain.port.out.CheckRepositoryPort;
import com.rtcomops.treasury.domain.port.out.ReconciliationMatchRepositoryPort;
import com.rtcomops.treasury.domain.port.out.StatementLineRepositoryPort;
import com.rtcomops.treasury.domain.port.out.TransactionSequenceRepositoryPort;
import com.rtcomops.treasury.domain.port.out.TransactionTypeRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Domain service for reconciliation operations.
 */
@Service
@Transactional
public class ReconciliationService implements ReconciliationUseCase {

    private static final Logger LOG = LoggerFactory.getLogger(ReconciliationService.class);
    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");
    private static final String DEPOSIT_TX_TYPE_CODE = "REM";

    private final ReconciliationMatchRepositoryPort matchRepositoryPort;
    private final StatementLineRepositoryPort lineRepositoryPort;
    private final BankStatementRepositoryPort statementRepositoryPort;
    private final BankTransactionRepositoryPort transactionRepositoryPort;
    private final CheckRepositoryPort checkRepositoryPort;
    private final CheckDepositRepositoryPort depositRepositoryPort;
    private final BankAccountRepositoryPort accountRepositoryPort;
    private final TransactionSequenceRepositoryPort sequenceRepositoryPort;
    private final TransactionTypeRepositoryPort transactionTypeRepositoryPort;
    private final ReconciliationMatchMapper matchMapper;
    private final StatementLineMapper lineMapper;
    private final AuditLogUseCase auditLogUseCase;

    public ReconciliationService(
            ReconciliationMatchRepositoryPort matchRepositoryPort,
            StatementLineRepositoryPort lineRepositoryPort,
            BankStatementRepositoryPort statementRepositoryPort,
            BankTransactionRepositoryPort transactionRepositoryPort,
            CheckRepositoryPort checkRepositoryPort,
            CheckDepositRepositoryPort depositRepositoryPort,
            BankAccountRepositoryPort accountRepositoryPort,
            TransactionSequenceRepositoryPort sequenceRepositoryPort,
            TransactionTypeRepositoryPort transactionTypeRepositoryPort,
            ReconciliationMatchMapper matchMapper,
            StatementLineMapper lineMapper,
            AuditLogUseCase auditLogUseCase) {
        this.matchRepositoryPort = matchRepositoryPort;
        this.lineRepositoryPort = lineRepositoryPort;
        this.statementRepositoryPort = statementRepositoryPort;
        this.transactionRepositoryPort = transactionRepositoryPort;
        this.checkRepositoryPort = checkRepositoryPort;
        this.depositRepositoryPort = depositRepositoryPort;
        this.accountRepositoryPort = accountRepositoryPort;
        this.sequenceRepositoryPort = sequenceRepositoryPort;
        this.transactionTypeRepositoryPort = transactionTypeRepositoryPort;
        this.matchMapper = matchMapper;
        this.lineMapper = lineMapper;
        this.auditLogUseCase = auditLogUseCase;
    }

    @Override
    public Mono<ReconciliationMatchResponse> reconcileManual(ReconcileManualRequest request) {
        LOG.info("Performing manual reconciliation for line={}", request.getStatementLineId());

        return lineRepositoryPort.findById(request.getStatementLineId())
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("StatementLine", request.getStatementLineId())))
            .flatMap(line -> {
                Mono<BigDecimal> amountMono;
                String entityType;
                UUID internalId;

                if (request.getBankTransactionId() != null) {
                    entityType = "Transaction";
                    internalId = request.getBankTransactionId();
                    amountMono = transactionRepositoryPort.findById(request.getBankTransactionId())
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException("BankTransaction", request.getBankTransactionId())))
                        .flatMap(tx -> markTransactionAsReconciled(tx).thenReturn(tx.getAmount()));
                } else if (request.getCheckId() != null) {
                    entityType = "Check";
                    internalId = request.getCheckId();
                    amountMono = checkRepositoryPort.findById(request.getCheckId())
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Check", request.getCheckId())))
                        .map(Check::getAmount);
                } else {
                    return Mono.error(new IllegalArgumentException("Either bankTransactionId or checkId must be provided"));
                }

                return amountMono.flatMap(amount -> {
                    BigDecimal matchedAmount = request.getMatchedAmount() != null
                        ? request.getMatchedAmount()
                        : amount;

                    ReconciliationMatch match = matchMapper.toEntityFromManual(request, matchedAmount);

                    return matchRepositoryPort.save(match)
                        .flatMap(savedMatch -> auditLogUseCase.log(
                                AuditModule.RECONCILIATION,
                                AuditAction.CREATE,
                                savedMatch.getId(),
                                "Rapprochement Manuel",
                                null,
                                savedMatch,
                                String.format("Ligne %.2f rapprochée avec %s %s", line.getAmount(), entityType, internalId)
                        ).thenReturn(savedMatch))
                        .flatMap(savedMatch -> {
                            StatementLine updatedLine = lineMapper.markAsMatched(line);
                            return lineRepositoryPort.save(updatedLine)
                                .flatMap(sl -> updateStatementReconciledCount(sl.getBankStatementId()))
                                .thenReturn(matchMapper.toResponse(savedMatch));
                        });
                });
            });
    }

    @Override
    public Mono<Void> unmatch(UUID matchId) {
        LOG.info("Removing reconciliation match id={}", matchId);

        return matchRepositoryPort.findById(matchId)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("ReconciliationMatch", matchId)))
            .flatMap(match ->
                auditLogUseCase.log(
                    AuditModule.RECONCILIATION,
                    AuditAction.DELETE,
                    match.getId(),
                    "Annulation Rapprochement",
                    match,
                    null,
                    "Annulation du rapprochement pour la ligne de relevé " + match.getStatementLineId()
                ).then(Mono.defer(() -> {
                    Mono<Void> unmarkTransaction = Mono.empty();
                    if (match.getBankTransactionId() != null) {
                        unmarkTransaction = transactionRepositoryPort.findById(match.getBankTransactionId())
                            .flatMap(this::unmarkTransactionAsReconciled)
                            .then();
                    }

                    return unmarkTransaction
                        .then(lineRepositoryPort.findById(match.getStatementLineId()))
                        .flatMap(line -> {
                            StatementLine updated = lineMapper.markAsUnmatched(line);
                            return lineRepositoryPort.save(updated);
                        })
                        .flatMap(line -> updateStatementReconciledCount(line.getBankStatementId()))
                        .then(matchRepositoryPort.deleteById(match.getId()));
                }))
            );
    }

    @Override
    public Flux<ReconciliationMatchResponse> reconcileAuto(AutoReconcileRequest request) {
        LOG.info("Performing automatic reconciliation for statement={}", request.getBankStatementId());

        return statementRepositoryPort.findById(request.getBankStatementId())
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("BankStatement", request.getBankStatementId())))
            .flatMapMany(statement ->
                lineRepositoryPort.findUnmatchedByStatementId(statement.getId())
                    .flatMap(line -> findMatchingTransaction(line, statement.getBankAccountId(), request)
                        .flatMap(match -> {
                            if (match.getConfidenceScore().compareTo(request.getMinimumConfidenceScore()) >= 0) {
                                return matchRepositoryPort.save(match)
                                    .flatMap(savedMatch -> auditLogUseCase.log(
                                        AuditModule.RECONCILIATION,
                                        AuditAction.CREATE,
                                        savedMatch.getId(),
                                        "Rapprochement Auto",
                                        null,
                                        savedMatch,
                                        String.format("Ligne %.2f auto-rapprochée (Confiance: %.2f%%)",
                                            line.getAmount(), savedMatch.getConfidenceScore())
                                    ).thenReturn(savedMatch))
                                    .flatMap(savedMatch -> {
                                        StatementLine updatedLine = lineMapper.markAsMatched(line);
                                        return lineRepositoryPort.save(updatedLine)
                                            .thenReturn(matchMapper.toResponse(savedMatch));
                                    });
                            }
                            return Mono.empty();
                        })
                    )
            )
            .doOnComplete(() ->
                statementRepositoryPort.findById(request.getBankStatementId())
                    .flatMap(st -> {
                        st.setStatus("IN_PROGRESS");
                        return statementRepositoryPort.save(st);
                    })
                    .subscribe()
            );
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<ReconciliationSummaryResponse> getSummary(UUID statementId) {
        LOG.debug("Getting reconciliation summary for statement={}", statementId);

        return statementRepositoryPort.findById(statementId)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("BankStatement", statementId)))
            .flatMap(statement ->
                accountRepositoryPort.findById(statement.getBankAccountId())
                    .flatMap(account ->
                        lineRepositoryPort.findByBankStatementIdOrderByLineNumber(statementId)
                            .collectList()
                            .map(lines -> buildSummary(statement, account.getName(), lines))
                    )
            );
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<ReconciliationMatchResponse> findMatchesByLineId(UUID lineId) {
        LOG.debug("Finding matches for line={}", lineId);
        return matchRepositoryPort.findByStatementLineId(lineId)
            .flatMap(this::enrichMatchWithDetails);
    }

    private Mono<ReconciliationMatch> findMatchingTransaction(
            StatementLine line, UUID accountId, AutoReconcileRequest request) {

        return transactionRepositoryPort.findUnreconciledByAccountId(accountId)
            .filter(tx -> isMatch(line, tx, request))
            .next()
            .flatMap(tx -> {
                BigDecimal confidence = calculateConfidence(line, tx, request);

                return markTransactionAsReconciled(tx)
                    .thenReturn(matchMapper.toEntityFromAuto(
                        line.getId(),
                        tx.getId(),
                        null,
                        line.getAmount(),
                        confidence,
                        confidence.compareTo(BigDecimal.valueOf(100)) == 0 ? "AUTO_EXACT" : "AUTO_FUZZY"
                    ));
            });
    }

    private boolean isMatch(StatementLine line, BankTransaction tx, AutoReconcileRequest request) {
        boolean amountMatch = true;
        boolean directionMatch = line.getDirection().equals(tx.getDirection());

        if (request.getMatchByAmount()) {
            BigDecimal diff = line.getAmount().subtract(tx.getAmount()).abs();
            amountMatch = diff.compareTo(request.getAmountTolerance()) <= 0;
        }

        boolean dateMatch = true;
        if (request.getMatchByDate()) {
            long daysDiff = Math.abs(
                line.getTransactionDate().toEpochDay() - tx.getTransactionDate().toEpochDay()
            );
            dateMatch = daysDiff <= request.getDateToleranceDays();
        }

        return amountMatch && directionMatch && dateMatch;
    }

    private BigDecimal calculateConfidence(StatementLine line, BankTransaction tx, AutoReconcileRequest request) {
        BigDecimal score = BigDecimal.ZERO;
        int factors = 0;

        if (request.getMatchByAmount()) {
            if (line.getAmount().compareTo(tx.getAmount()) == 0) {
                score = score.add(BigDecimal.valueOf(40));
            } else {
                BigDecimal diff = line.getAmount().subtract(tx.getAmount()).abs();
                BigDecimal ratio = BigDecimal.ONE.subtract(
                    diff.divide(line.getAmount(), 4, RoundingMode.HALF_UP)
                );
                score = score.add(ratio.multiply(BigDecimal.valueOf(40)));
            }
            factors++;
        }

        if (request.getMatchByDate()) {
            long daysDiff = Math.abs(
                line.getTransactionDate().toEpochDay() - tx.getTransactionDate().toEpochDay()
            );
            if (daysDiff == 0) {
                score = score.add(BigDecimal.valueOf(30));
            } else {
                BigDecimal ratio = BigDecimal.ONE.subtract(
                    BigDecimal.valueOf(daysDiff).divide(BigDecimal.valueOf(request.getDateToleranceDays()), 4, RoundingMode.HALF_UP)
                );
                score = score.add(ratio.max(BigDecimal.ZERO).multiply(BigDecimal.valueOf(30)));
            }
            factors++;
        }

        if (request.getMatchByReference() && line.getReference() != null && tx.getReference() != null) {
            if (line.getReference().equalsIgnoreCase(tx.getReference())) {
                score = score.add(BigDecimal.valueOf(30));
            }
            factors++;
        }

        if (factors > 0 && factors < 3) {
            score = score.multiply(BigDecimal.valueOf(100)).divide(
                BigDecimal.valueOf(factors * (100 / 3)), 2, RoundingMode.HALF_UP
            );
        }

        return score.min(BigDecimal.valueOf(100));
    }

    private Mono<BankTransaction> markTransactionAsReconciled(BankTransaction tx) {
        tx.setIsReconciled(true);
        tx.setReconciledAt(LocalDateTime.now());
        return transactionRepositoryPort.save(tx);
    }

    private Mono<BankTransaction> unmarkTransactionAsReconciled(BankTransaction tx) {
        tx.setIsReconciled(false);
        tx.setReconciledAt(null);
        return transactionRepositoryPort.save(tx);
    }

    private Mono<Void> updateStatementReconciledCount(UUID statementId) {
        return lineRepositoryPort.countMatchedByStatementId(statementId)
            .flatMap(count -> statementRepositoryPort.findById(statementId)
                .flatMap(statement -> {
                    statement.setReconciledCount(count.intValue());
                    return statementRepositoryPort.save(statement);
                }))
            .then();
    }

    private Mono<ReconciliationMatchResponse> enrichMatchWithDetails(ReconciliationMatch match) {
        Mono<String> txRefMono = match.getBankTransactionId() != null
            ? transactionRepositoryPort.findById(match.getBankTransactionId())
                .map(BankTransaction::getReference)
                .defaultIfEmpty("")
            : Mono.just("");

        Mono<String> checkNumMono = match.getCheckId() != null
            ? checkRepositoryPort.findById(match.getCheckId())
                .map(Check::getCheckNumber)
                .defaultIfEmpty("")
            : Mono.just("");

        return Mono.zip(txRefMono, checkNumMono)
            .map(tuple -> matchMapper.toResponseWithDetails(match, tuple.getT1(), tuple.getT2()));
    }

    private ReconciliationSummaryResponse buildSummary(
            BankStatement statement, String accountName, List<StatementLine> lines) {

        int matchedLines = 0;
        int unmatchedLines = 0;
        int ignoredLines = 0;
        BigDecimal matchedCredits = BigDecimal.ZERO;
        BigDecimal matchedDebits = BigDecimal.ZERO;
        BigDecimal unmatchedCredits = BigDecimal.ZERO;
        BigDecimal unmatchedDebits = BigDecimal.ZERO;

        for (StatementLine line : lines) {
            boolean isCredit = "CREDIT".equals(line.getDirection());

            switch (line.getReconciliationStatus()) {
                case "MATCHED":
                    matchedLines++;
                    if (isCredit) matchedCredits = matchedCredits.add(line.getAmount());
                    else matchedDebits = matchedDebits.add(line.getAmount());
                    break;
                case "IGNORED":
                    ignoredLines++;
                    break;
                default:
                    unmatchedLines++;
                    if (isCredit) unmatchedCredits = unmatchedCredits.add(line.getAmount());
                    else unmatchedDebits = unmatchedDebits.add(line.getAmount());
            }
        }

        int totalLines = lines.size();
        BigDecimal progress = totalLines > 0
            ? BigDecimal.valueOf(matchedLines + ignoredLines)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalLines), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        BigDecimal calculatedBalance = statement.getOpeningBalance()
            .add(statement.getTotalCredits())
            .subtract(statement.getTotalDebits());

        BigDecimal balanceDifference = statement.getClosingBalance().subtract(calculatedBalance);

        return ReconciliationSummaryResponse.builder()
            .bankStatementId(statement.getId())
            .bankAccountName(accountName)
            .totalLines(totalLines)
            .matchedLines(matchedLines)
            .unmatchedLines(unmatchedLines)
            .ignoredLines(ignoredLines)
            .progressPercentage(progress)
            .totalCredits(statement.getTotalCredits())
            .totalDebits(statement.getTotalDebits())
            .matchedCredits(matchedCredits)
            .matchedDebits(matchedDebits)
            .unmatchedCredits(unmatchedCredits)
            .unmatchedDebits(unmatchedDebits)
            .openingBalance(statement.getOpeningBalance())
            .closingBalance(statement.getClosingBalance())
            .calculatedBalance(calculatedBalance)
            .balanceDifference(balanceDifference)
            .status(statement.getStatus())
            .build();
    }

    // =========================================================================
    // CHECK DEPOSIT RECONCILIATION
    // =========================================================================

    @Override
    public Mono<Void> reconcileCheckDeposit(UUID depositId, UUID statementLineId) {
        LOG.info("Reconciling check deposit id={} with statement line id={}", depositId, statementLineId);

        return depositRepositoryPort.findById(depositId)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("CheckDeposit", depositId)))
            .flatMap(deposit -> {
                if ("RECONCILED".equals(deposit.getStatus())) {
                    return Mono.error(new BusinessException(
                        "Cette remise de chèques a déjà été rapprochée"));
                }

                return lineRepositoryPort.findById(statementLineId)
                    .switchIfEmpty(Mono.error(new ResourceNotFoundException("StatementLine", statementLineId)))
                    .flatMap(line -> reconcileDepositWithLine(deposit, line));
            });
    }

    private Mono<Void> reconcileDepositWithLine(CheckDeposit deposit, StatementLine line) {
        LocalDate reconciliationDate = line.getTransactionDate();

        return createTransactionForDeposit(deposit, reconciliationDate)
            .flatMap(transaction -> {
                deposit.setStatus("RECONCILED");
                deposit.setBankTransactionId(transaction.getId());

                return depositRepositoryPort.save(deposit)
                    .flatMap(savedDeposit ->
                        cashChecksInDeposit(savedDeposit.getId(), reconciliationDate)
                            .then(updateAccountBalanceForDeposit(savedDeposit.getBankAccountId(), savedDeposit.getTotalAmount()))
                            .then(createMatchForDeposit(transaction, line))
                            .then(markLineAsMatched(line))
                            .then(auditLogUseCase.log(
                                AuditModule.CHECK_DEPOSIT,
                                AuditAction.RECONCILE,
                                savedDeposit.getId(),
                                savedDeposit.getReference(),
                                null,
                                savedDeposit,
                                String.format("Rapprochement de la remise %s - Transaction %s créée - %d chèques encaissés",
                                    savedDeposit.getReference(), transaction.getReference(), savedDeposit.getCheckCount())
                            ))
                    );
            })
            .doOnSuccess(v -> LOG.info("Check deposit reconciliation completed: depositId={}", deposit.getId()));
    }

    private Mono<BankTransaction> createTransactionForDeposit(CheckDeposit deposit, LocalDate transactionDate) {
        return findOrCreateDepositTransactionType()
            .flatMap(transactionType ->
                generateDepositTransactionReference()
                    .flatMap(reference -> {
                        BankTransaction transaction = BankTransaction.builder()
                            .id(UUID.randomUUID())
                            .bankAccountId(deposit.getBankAccountId())
                            .transactionTypeId(transactionType.getId())
                            .reference(reference)
                            .transactionDate(transactionDate)
                            .valueDate(transactionDate)
                            .amount(deposit.getTotalAmount())
                            .direction("CREDIT")
                            .description("Remise de chèques " + deposit.getReference() +
                                " (" + deposit.getCheckCount() + " chèques)")
                            .status("VALIDATED")
                            .systemDate(LocalDateTime.now())
                            .isReconciled(true)
                            .reconciledAt(LocalDateTime.now())
                            .build();

                        return transactionRepositoryPort.save(transaction)
                            .doOnSuccess(saved -> LOG.info(
                                "Transaction created for deposit: txId={}, depositId={}, reference={}",
                                saved.getId(), deposit.getId(), saved.getReference()))
                            .flatMap(saved -> auditLogUseCase.log(
                                AuditModule.BANK_TRANSACTION,
                                AuditAction.CREATE,
                                saved.getId(),
                                saved.getReference(),
                                null,
                                saved,
                                "Création automatique de transaction pour remise de chèques " + deposit.getReference()
                            ).thenReturn(saved));
                    })
            );
    }

    private Mono<TransactionType> findOrCreateDepositTransactionType() {
        return transactionTypeRepositoryPort.findByCode(DEPOSIT_TX_TYPE_CODE)
            .switchIfEmpty(
                transactionTypeRepositoryPort.findByCategory("CHECK")
                    .next()
                    .switchIfEmpty(
                        Mono.defer(() -> {
                            LOG.warn("No REM transaction type found, creating default");
                            TransactionType defaultType = TransactionType.builder()
                                .id(UUID.randomUUID())
                                .code(DEPOSIT_TX_TYPE_CODE)
                                .label("Remise de chèques")
                                .category("CHECK")
                                .description("Transaction pour remise de chèques en lot")
                                .isActive(true)
                                .build();
                            return transactionTypeRepositoryPort.save(defaultType);
                        })
                    )
            );
    }

    private Mono<String> generateDepositTransactionReference() {
        String yearMonth = LocalDate.now().format(YEAR_MONTH_FORMATTER);
        String typeCode = "REM-TX";

        return sequenceRepositoryPort.incrementSequence(typeCode, yearMonth)
            .then(sequenceRepositoryPort.findLastSequence(typeCode, yearMonth))
            .map(sequence -> {
                String formattedSequence = String.format("%04d", sequence);
                return typeCode + "-" + yearMonth + "-" + formattedSequence;
            })
            .switchIfEmpty(Mono.error(new BusinessException(
                "Impossible de générer une référence pour la transaction de remise.")));
    }

    private Mono<Void> cashChecksInDeposit(UUID depositId, LocalDate cashedDate) {
        return checkRepositoryPort.findByCheckDepositId(depositId)
            .flatMap(check -> {
                Check original = Check.builder()
                    .id(check.getId())
                    .checkNumber(check.getCheckNumber())
                    .status(check.getStatus())
                    .build();
                check.setStatus("CASHED");
                check.setCashedDate(cashedDate);

                return checkRepositoryPort.save(check)
                    .flatMap(saved -> auditLogUseCase.log(
                        AuditModule.CHECK,
                        AuditAction.CASH,
                        saved.getId(),
                        saved.getCheckNumber(),
                        original,
                        saved,
                        "Encaissement automatique via rapprochement de la remise"
                    ).thenReturn(saved));
            })
            .then();
    }

    private Mono<Void> updateAccountBalanceForDeposit(UUID accountId, BigDecimal amount) {
        return accountRepositoryPort.findById(accountId)
            .flatMap(account -> {
                BigDecimal currentBalance = account.getCurrentBalance() != null
                    ? account.getCurrentBalance()
                    : BigDecimal.ZERO;

                BigDecimal newBalance = currentBalance.add(amount);
                account.setCurrentBalance(newBalance);

                LOG.info("Updating account {} balance for deposit: {} + {} = {}",
                    accountId, currentBalance, amount, newBalance);
                return accountRepositoryPort.save(account).then();
            });
    }

    private Mono<Void> createMatchForDeposit(BankTransaction transaction, StatementLine line) {
        ReconciliationMatch match = matchMapper.toEntityFromAuto(
            line.getId(),
            transaction.getId(),
            null,
            transaction.getAmount(),
            BigDecimal.valueOf(100),
            "MANUAL_DEPOSIT"
        );

        return matchRepositoryPort.save(match)
            .flatMap(savedMatch -> auditLogUseCase.log(
                AuditModule.RECONCILIATION,
                AuditAction.MANUAL_MATCH,
                savedMatch.getId(),
                "Rapprochement Remise",
                null,
                savedMatch,
                String.format("Ligne %.2f rapprochée avec remise de chèques (Transaction %s)",
                    line.getAmount(), transaction.getReference())
            ).thenReturn(savedMatch))
            .then();
    }

    private Mono<Void> markLineAsMatched(StatementLine line) {
        StatementLine updatedLine = lineMapper.markAsMatched(line);
        return lineRepositoryPort.save(updatedLine)
            .flatMap(sl -> updateStatementReconciledCount(sl.getBankStatementId()))
            .then();
    }
}
