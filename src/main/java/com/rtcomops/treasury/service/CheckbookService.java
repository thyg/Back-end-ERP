package com.rtcomops.treasury.service;

import com.rtcomops.treasury.dto.request.CreateCheckbookRequest;
import com.rtcomops.treasury.dto.response.CheckbookResponse;
import com.rtcomops.treasury.dto.response.CheckbookStatsResponse;
import com.rtcomops.treasury.entity.Checkbook;
import com.rtcomops.treasury.enums.AuditAction;
import com.rtcomops.treasury.enums.AuditModule;
import com.rtcomops.treasury.exception.BusinessException;
import com.rtcomops.treasury.exception.ResourceNotFoundException;
import com.rtcomops.treasury.mapper.CheckbookMapper;
import com.rtcomops.treasury.repository.BankAccountRepository;
import com.rtcomops.treasury.repository.CheckRepository;
import com.rtcomops.treasury.repository.CheckbookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service layer for Checkbook operations.
 *
 * <p>Provides business logic for managing checkbooks, including CRUD operations
 * and check number allocation.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-24
 */
@Service
@Transactional
public class CheckbookService {

    private static final Logger LOG = LoggerFactory.getLogger(CheckbookService.class);
    private static final String RESOURCE_NAME = "Checkbook";

    private final CheckbookRepository checkbookRepository;
    private final BankAccountRepository accountRepository;
    private final CheckbookMapper checkbookMapper;
    private final AuditLogService auditLogService;
    private final CheckRepository checkRepository;

    public CheckbookService(
            CheckbookRepository checkbookRepository,
            BankAccountRepository accountRepository,
            CheckbookMapper checkbookMapper,
            AuditLogService auditLogService,
            CheckRepository checkRepository) {
        this.checkbookRepository = checkbookRepository;
        this.accountRepository = accountRepository;
        this.checkbookMapper = checkbookMapper;
        this.auditLogService = auditLogService;
        this.checkRepository = checkRepository;
    }

    /**
     * Retrieves all checkbooks.
     *
     * @return Flux of CheckbookResponse DTOs
     */
    @Transactional(readOnly = true)
    public Flux<CheckbookResponse> findAll() {
        LOG.debug("Finding all checkbooks");
        return checkbookRepository.findAllOrderByCreatedAtDesc()
            .flatMap(this::enrichWithAccountName);
    }

    /**
     * Retrieves a checkbook by its ID.
     *
     * @param id the checkbook ID
     * @return Mono of CheckbookResponse
     * @throws ResourceNotFoundException if checkbook not found
     */
    @Transactional(readOnly = true)
    public Mono<CheckbookResponse> findById(UUID id) {
        LOG.debug("Finding checkbook by id={}", id);
        return checkbookRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(this::enrichWithAccountName);
    }

    /**
     * Retrieves all checkbooks for a bank account.
     *
     * @param accountId the bank account ID
     * @return Flux of CheckbookResponse DTOs
     */
    @Transactional(readOnly = true)
    public Flux<CheckbookResponse> findByBankAccountId(UUID accountId) {
        LOG.debug("Finding checkbooks by accountId={}", accountId);
        return checkbookRepository.findByBankAccountId(accountId)
            .flatMap(this::enrichWithAccountName);
    }

    /**
     * Retrieves checkbooks by status.
     *
     * @param status the status to filter by
     * @return Flux of CheckbookResponse DTOs
     */
    @Transactional(readOnly = true)
    public Flux<CheckbookResponse> findByStatus(String status) {
        LOG.debug("Finding checkbooks by status={}", status);
        return checkbookRepository.findByStatus(status.toUpperCase())
            .flatMap(this::enrichWithAccountName);
    }

    /**
     * Creates a new checkbook.
     *
     * @param request the create request DTO
     * @return Mono of created CheckbookResponse
     * @throws ResourceNotFoundException if bank account not found
     * @throws BusinessException if validation fails
     */
    public Mono<CheckbookResponse> create(CreateCheckbookRequest request) {
        LOG.info("Creating checkbook for account={}", request.getBankAccountId());

        // Validate numberOfPages
        if (request.getNumberOfPages() == null || request.getNumberOfPages() < 1) {
            return Mono.error(new BusinessException(
                "Le nombre de feuilles doit être au minimum 1"));
        }

        return accountRepository.findById(request.getBankAccountId())
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("BankAccount", request.getBankAccountId())))
            .flatMap(account ->
                // Check for overlapping ranges
                checkbookRepository.existsOverlappingRange(
                    request.getBankAccountId(),
                    request.getStartNumber(),
                    request.calculateEndNumber()
                ).flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new BusinessException(
                            "Une plage de numéros chevauchante existe déjà pour ce compte"));
                    }

                    // Use IBAN from account if not provided
                    if (request.getIban() == null || request.getIban().isBlank()) {
                        String iban = account.getIban() != null ? account.getIban() : account.getAccountNumber();
                        request.setIban(iban);
                    }

                    Checkbook entity = checkbookMapper.toEntity(request);
                    return checkbookRepository.save(entity)
                        .doOnSuccess(saved -> LOG.info("Checkbook created: id={}, range={}-{}",
                            saved.getId(), saved.getStartNumber(), saved.getEndNumber()))
                        .flatMap(saved -> auditLogService.log(
                                AuditModule.CHECKBOOK,
                                AuditAction.CREATE,
                                saved.getId(),
                                "Chéquier " + saved.getPrefix(),
                                null,
                                saved,
                                "Création du chéquier " + saved.getPrefix() +
                                    " (plage " + saved.getStartNumber() + "-" + saved.getEndNumber() + ")"
                            ).thenReturn(saved))
                        .flatMap(saved -> enrichWithAccountName(saved));
                })
            );
    }

    /**
     * Cancels a checkbook.
     *
     * @param id the checkbook ID
     * @return Mono of updated CheckbookResponse
     * @throws ResourceNotFoundException if checkbook not found
     * @throws BusinessException if checkbook is already cancelled or finished
     */
    public Mono<CheckbookResponse> cancel(UUID id) {
        LOG.info("Cancelling checkbook id={}", id);

        return checkbookRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(existing -> {
                if ("CANCELLED".equals(existing.getStatus())) {
                    return Mono.error(new BusinessException("Le chéquier est déjà annulé"));
                }
                if ("FINISHED".equals(existing.getStatus())) {
                    return Mono.error(new BusinessException("Impossible d'annuler un chéquier terminé"));
                }

                Checkbook oldState = checkbookMapper.copy(existing);
                existing.setStatus("CANCELLED");
                existing.setUpdatedAt(LocalDateTime.now());
                existing.setNew(false);

                return checkbookRepository.save(existing)
                    .doOnSuccess(saved -> LOG.info("Checkbook cancelled: id={}", saved.getId()))
                    .flatMap(saved -> auditLogService.log(
                            AuditModule.CHECKBOOK,
                            AuditAction.CANCEL,
                            saved.getId(),
                            "Chéquier " + saved.getPrefix(),
                            oldState,
                            saved,
                            "Annulation du chéquier " + saved.getPrefix()
                        ).thenReturn(saved))
                    .flatMap(saved -> enrichWithAccountName(saved));
            });
    }

   // AJOUTEZ cette nouvelle méthode :
/**
 * Gets the next available check number without incrementing the counter.
 * This is for display purposes only.
 *
 * @param id the checkbook ID
 * @return Mono of the next check number string
 */
@Transactional(readOnly = true) // C'est une opération de lecture seule
public Mono<String> peekNextCheckNumber(UUID id) {
    LOG.debug("Peeking next check number for checkbook id={}", id);

    return checkbookRepository.findById(id)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
        .flatMap(checkbook -> {
            if (!checkbook.hasAvailableChecks()) {
                return Mono.error(new BusinessException("Aucun chèque disponible dans ce chéquier"));
            }
            return Mono.just(checkbook.getNextCheckNumber());
        });
}

    /**
     * Gets the active checkbook for a bank account.
     *
     * @param accountId the bank account ID
     * @return Mono of CheckbookResponse, or empty if none active
     */
    @Transactional(readOnly = true)
    public Mono<CheckbookResponse> findActiveByBankAccountId(UUID accountId) {
        LOG.debug("Finding active checkbook for accountId={}", accountId);
        return checkbookRepository.findActiveByBankAccountId(accountId)
            .flatMap(this::enrichWithAccountName);
    }

    /**
     * Gets statistics for a specific checkbook.
     *
     * @param checkbookId the checkbook ID
     * @return Mono of CheckbookStatsResponse
     * @throws ResourceNotFoundException if checkbook not found
     */
    @Transactional(readOnly = true)
    public Mono<CheckbookStatsResponse> getStats(UUID checkbookId) {
        LOG.debug("Getting stats for checkbook id={}", checkbookId);
        return checkbookRepository.findById(checkbookId)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, checkbookId)))
            .flatMap(checkbook -> checkRepository.getStatsByCheckbookId(checkbookId)
                .map(stats -> {
                    stats.setRemainingChecks(checkbook.getAvailableChecksCount());
                    return stats;
                }));
    }

    /**
     * Enriches a checkbook with the bank account name.
     *
     * @param checkbook the checkbook entity
     * @return Mono of CheckbookResponse with account name
     */
    private Mono<CheckbookResponse> enrichWithAccountName(Checkbook checkbook) {
        // Si le bankAccountId est null (chequier fictif/systeme), retourner directement sans enrichissement
        if (checkbook.getBankAccountId() == null) {
            return Mono.just(checkbookMapper.toResponseWithDetails(checkbook, null));
        }

        return accountRepository.findById(checkbook.getBankAccountId())
            .map(account -> checkbookMapper.toResponseWithDetails(checkbook, account.getName()))
            .defaultIfEmpty(checkbookMapper.toResponseWithDetails(checkbook, "Compte inconnu"));
    }
}
