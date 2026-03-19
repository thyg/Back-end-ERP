package com.rtcomops.treasury.application.service;

import com.rtcomops.treasury.application.dto.request.CreateCheckbookRequest;
import com.rtcomops.treasury.application.dto.response.CheckbookResponse;
import com.rtcomops.treasury.application.dto.response.CheckbookStatsResponse;
import com.rtcomops.treasury.application.mapper.CheckbookMapper;
import com.rtcomops.treasury.domain.exception.BusinessException;
import com.rtcomops.treasury.domain.exception.ResourceNotFoundException;
import com.rtcomops.treasury.domain.model.Checkbook;
import com.rtcomops.treasury.application.port.in.CheckbookUseCase;
import com.rtcomops.treasury.domain.port.out.BankAccountRepositoryPort;
import com.rtcomops.treasury.domain.port.out.CheckbookRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain service for Checkbook operations.
 *
 * <p>Implements the CheckbookUseCase port and provides business logic
 * for managing checkbooks, including CRUD operations and check number allocation.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-24
 */
@Service
@Transactional
public class CheckbookService implements CheckbookUseCase {

    private static final Logger LOG = LoggerFactory.getLogger(CheckbookService.class);
    private static final String RESOURCE_NAME = "Checkbook";

    private final CheckbookRepositoryPort checkbookRepositoryPort;
    private final BankAccountRepositoryPort bankAccountRepositoryPort;
    private final CheckbookMapper checkbookMapper;

    public CheckbookService(
            CheckbookRepositoryPort checkbookRepositoryPort,
            BankAccountRepositoryPort bankAccountRepositoryPort,
            CheckbookMapper checkbookMapper) {
        this.checkbookRepositoryPort = checkbookRepositoryPort;
        this.bankAccountRepositoryPort = bankAccountRepositoryPort;
        this.checkbookMapper = checkbookMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<CheckbookResponse> findAll() {
        LOG.debug("Finding all checkbooks");
        return checkbookRepositoryPort.findAllOrderByCreatedAtDesc()
            .flatMap(this::enrichWithAccountName);
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<CheckbookResponse> findById(UUID id) {
        LOG.debug("Finding checkbook by id={}", id);
        return checkbookRepositoryPort.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(this::enrichWithAccountName);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<CheckbookResponse> findByBankAccountId(UUID accountId) {
        LOG.debug("Finding checkbooks by accountId={}", accountId);
        return checkbookRepositoryPort.findByBankAccountId(accountId)
            .flatMap(this::enrichWithAccountName);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<CheckbookResponse> findByStatus(String status) {
        LOG.debug("Finding checkbooks by status={}", status);
        return checkbookRepositoryPort.findByStatus(status.toUpperCase())
            .flatMap(this::enrichWithAccountName);
    }

    @Override
    public Mono<CheckbookResponse> create(CreateCheckbookRequest request) {
        LOG.info("Creating checkbook for account={}", request.getBankAccountId());

        // Validate numberOfPages
        if (request.getNumberOfPages() == null || request.getNumberOfPages() < 1) {
            return Mono.error(new BusinessException(
                "Le nombre de feuilles doit etre au minimum 1"));
        }

        return bankAccountRepositoryPort.findById(request.getBankAccountId())
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("BankAccount", request.getBankAccountId())))
            .flatMap(account ->
                // Check for overlapping ranges
                checkbookRepositoryPort.existsOverlappingRange(
                    request.getBankAccountId(),
                    request.getStartNumber(),
                    request.calculateEndNumber()
                ).flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new BusinessException(
                            "Une plage de numeros chevauchante existe deja pour ce compte"));
                    }

                    Checkbook entity = checkbookMapper.toEntity(request);
                    return checkbookRepositoryPort.save(entity)
                        .doOnSuccess(saved -> LOG.info("Checkbook created: id={}, range={}-{}",
                            saved.getId(), saved.getStartNumber(), saved.getEndNumber()))
                        .flatMap(this::enrichWithAccountName);
                })
            );
    }

    @Override
    public Mono<CheckbookResponse> cancel(UUID id) {
        LOG.info("Cancelling checkbook id={}", id);

        return checkbookRepositoryPort.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(existing -> {
                if ("CANCELLED".equals(existing.getStatus())) {
                    return Mono.error(new BusinessException("Le chequier est deja annule"));
                }
                if ("FINISHED".equals(existing.getStatus())) {
                    return Mono.error(new BusinessException("Impossible d'annuler un chequier termine"));
                }

                existing.setStatus("CANCELLED");
                existing.setUpdatedAt(LocalDateTime.now());

                return checkbookRepositoryPort.save(existing)
                    .doOnSuccess(saved -> LOG.info("Checkbook cancelled: id={}", saved.getId()))
                    .flatMap(this::enrichWithAccountName);
            });
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<String> peekNextCheckNumber(UUID id) {
        LOG.debug("Peeking next check number for checkbook id={}", id);

        return checkbookRepositoryPort.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(checkbook -> {
                if (!checkbook.hasAvailableChecks()) {
                    return Mono.error(new BusinessException("Aucun cheque disponible dans ce chequier"));
                }
                return Mono.just(checkbook.getNextCheckNumber());
            });
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<CheckbookResponse> findActiveByBankAccountId(UUID accountId) {
        LOG.debug("Finding active checkbook for accountId={}", accountId);
        return checkbookRepositoryPort.findActiveByBankAccountId(accountId)
            .flatMap(this::enrichWithAccountName);
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<CheckbookResponse> findSystemCheckbook() {
        LOG.debug("Finding system checkbook");
        return checkbookRepositoryPort.findByIsSystemTrue()
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                "Chequier systeme non trouve. Veuillez contacter l'administrateur.")))
            .flatMap(this::enrichWithAccountName);
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<CheckbookStatsResponse> getStats(UUID checkbookId) {
        LOG.debug("Getting stats for checkbook id={}", checkbookId);
        return checkbookRepositoryPort.findById(checkbookId)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, checkbookId)))
            .map(checkbook -> {
                // Build basic stats from the checkbook itself
                return CheckbookStatsResponse.builder()
                    .remainingChecks(checkbook.getAvailableChecksCount())
                    .build();
            });
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

        return bankAccountRepositoryPort.findById(checkbook.getBankAccountId())
            .map(account -> checkbookMapper.toResponseWithDetails(checkbook, account.getName()))
            .defaultIfEmpty(checkbookMapper.toResponseWithDetails(checkbook, "Compte inconnu"));
    }
}
