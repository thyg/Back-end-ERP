package com.rtcomops.treasury.application.service;

import com.rtcomops.treasury.domain.exception.DuplicateResourceException;
import com.rtcomops.treasury.domain.exception.ResourceNotFoundException;
import com.rtcomops.treasury.domain.model.Bank;
import com.rtcomops.treasury.domain.model.enums.AuditAction;
import com.rtcomops.treasury.domain.model.enums.AuditModule;
import com.rtcomops.treasury.application.port.in.AuditLogUseCase;
import com.rtcomops.treasury.application.port.in.BankUseCase;
import com.rtcomops.treasury.domain.port.out.BankRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain service for Bank operations.
 *
 * <p>Provides business logic for managing banks, including CRUD operations
 * with validation and error handling.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-10
 */
@Service
@Transactional
public class BankService implements BankUseCase {

    private static final Logger LOG = LoggerFactory.getLogger(BankService.class);
    private static final String RESOURCE_NAME = "Bank";

    private final BankRepositoryPort bankRepositoryPort;
    private final AuditLogUseCase auditLogUseCase;

    /**
     * Constructs the BankService with required dependencies.
     *
     * @param bankRepositoryPort the bank repository port
     * @param auditLogUseCase the audit log use case
     */
    public BankService(BankRepositoryPort bankRepositoryPort, AuditLogUseCase auditLogUseCase) {
        this.bankRepositoryPort = bankRepositoryPort;
        this.auditLogUseCase = auditLogUseCase;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Flux<Bank> findAll(boolean activeOnly) {
        LOG.debug("Finding all banks, activeOnly={}", activeOnly);

        return activeOnly
            ? bankRepositoryPort.findAllActiveOrderByName()
            : bankRepositoryPort.findAllOrderByName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Mono<Bank> findById(UUID id) {
        LOG.debug("Finding bank by id={}", id);

        return bankRepositoryPort.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Mono<Bank> findByCode(String code) {
        LOG.debug("Finding bank by code={}", code);

        return bankRepositoryPort.findByCode(code.toUpperCase())
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, "code", code)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Bank> create(Bank bank) {
        String code = bank.getCode().toUpperCase();
        LOG.info("Creating bank with code={}", code);

        return bankRepositoryPort.existsByCode(code)
            .flatMap(exists -> {
                if (exists) {
                    LOG.warn("Bank creation failed: code {} already exists", code);
                    return Mono.error(new DuplicateResourceException(RESOURCE_NAME, "code", code));
                }

                // Ensure the bank has proper initialization
                if (bank.getId() == null) {
                    bank.setId(UUID.randomUUID());
                }
                bank.setCode(code);
                if (bank.getCreatedAt() == null) {
                    bank.setCreatedAt(LocalDateTime.now());
                }
                bank.setUpdatedAt(LocalDateTime.now());

                return bankRepositoryPort.save(bank)
                    .doOnSuccess(saved -> LOG.info("Bank created successfully: id={}, code={}",
                        saved.getId(), saved.getCode()))
                    .flatMap(saved -> auditLogUseCase.log(
                        AuditModule.BANK,
                        AuditAction.CREATE,
                        saved.getId(),
                        "Banque " + saved.getCode(),
                        null,
                        saved,
                        "Création de la banque " + saved.getName()
                    ).thenReturn(saved));
            });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Bank> update(UUID id, Bank bank) {
        LOG.info("Updating bank with id={}", id);

        return bankRepositoryPort.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(existing -> {
                // Check for duplicate code if code is being changed
                if (bank.getCode() != null && !bank.getCode().equalsIgnoreCase(existing.getCode())) {
                    return bankRepositoryPort.existsByCodeAndIdNot(bank.getCode().toUpperCase(), id)
                        .flatMap(exists -> {
                            if (exists) {
                                LOG.warn("Bank update failed: code {} already exists", bank.getCode());
                                return Mono.error(new DuplicateResourceException(
                                    RESOURCE_NAME, "code", bank.getCode()));
                            }
                            return saveUpdatedBank(existing, bank);
                        });
                }
                return saveUpdatedBank(existing, bank);
            });
    }

    /**
     * Saves the updated bank entity.
     *
     * @param existing the existing bank entity
     * @param updates the bank with updates
     * @return Mono of updated Bank
     */
    private Mono<Bank> saveUpdatedBank(Bank existing, Bank updates) {
        // Copy the original state for audit
        Bank oldState = copyBank(existing);

        // Apply updates
        if (updates.getCode() != null) {
            existing.setCode(updates.getCode().toUpperCase());
        }
        if (updates.getName() != null) {
            existing.setName(updates.getName());
        }
        if (updates.getAddress() != null) {
            existing.setAddress(updates.getAddress());
        }
        if (updates.getSwiftCode() != null) {
            existing.setSwiftCode(updates.getSwiftCode());
        }
        if (updates.getBankCode() != null) {
            existing.setBankCode(updates.getBankCode());
        }
        if (updates.getCountry() != null) {
            existing.setCountry(updates.getCountry());
        }
        if (updates.getBankCategoryId() != null) {
            existing.setBankCategoryId(updates.getBankCategoryId());
        }
        if (updates.getIsActive() != null) {
            existing.setIsActive(updates.getIsActive());
        }
        existing.setUpdatedAt(LocalDateTime.now());

        return bankRepositoryPort.save(existing)
            .doOnSuccess(saved -> LOG.info("Bank updated successfully: id={}", saved.getId()))
            .flatMap(saved -> auditLogUseCase.log(
                AuditModule.BANK,
                AuditAction.UPDATE,
                saved.getId(),
                "Banque " + saved.getCode(),
                oldState,
                saved,
                "Mise à jour de la banque " + saved.getName()
            ).thenReturn(saved));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Void> delete(UUID id) {
        LOG.info("Deleting bank with id={}", id);

        return bankRepositoryPort.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(bank -> auditLogUseCase.log(
                    AuditModule.BANK,
                    AuditAction.DELETE,
                    bank.getId(),
                    "Banque " + bank.getCode(),
                    bank,
                    null,
                    "Suppression de la banque " + bank.getName()
                ).then(bankRepositoryPort.deleteById(bank.getId())
                .doOnSuccess(v -> LOG.info("Bank deleted successfully: id={}, code={}",
                    bank.getId(), bank.getCode()))));
    }

    /**
     * Creates a copy of a Bank for audit purposes.
     */
    private Bank copyBank(Bank original) {
        return Bank.builder()
            .id(original.getId())
            .code(original.getCode())
            .name(original.getName())
            .swiftCode(original.getSwiftCode())
            .bankCode(original.getBankCode())
            .country(original.getCountry())
            .address(original.getAddress())
            .isActive(original.getIsActive())
            .bankCategoryId(original.getBankCategoryId())
            .createdAt(original.getCreatedAt())
            .updatedAt(original.getUpdatedAt())
            .build();
    }
}
