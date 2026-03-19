package com.rtcomops.treasury.application.service;

import com.rtcomops.treasury.application.dto.request.CreateAccountTypeRequest;
import com.rtcomops.treasury.application.dto.request.UpdateAccountTypeRequest;
import com.rtcomops.treasury.application.dto.response.AccountSubTypeResponse;
import com.rtcomops.treasury.application.dto.response.AccountTypeResponse;
import com.rtcomops.treasury.domain.exception.DuplicateResourceException;
import com.rtcomops.treasury.domain.exception.ResourceNotFoundException;
import com.rtcomops.treasury.domain.model.AccountSubType;
import com.rtcomops.treasury.domain.model.AccountType;
import com.rtcomops.treasury.application.port.in.AccountTypeUseCase;
import com.rtcomops.treasury.domain.port.out.AccountSubTypeRepositoryPort;
import com.rtcomops.treasury.domain.port.out.AccountTypeRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain service for AccountType operations.
 *
 * <p>Implements the AccountTypeUseCase port and provides business logic
 * for managing account types, including CRUD operations with validation
 * and error handling.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
@Service
@Transactional
public class AccountTypeService implements AccountTypeUseCase {

    private static final Logger LOG = LoggerFactory.getLogger(AccountTypeService.class);
    private static final String RESOURCE_NAME = "AccountType";

    private final AccountTypeRepositoryPort accountTypeRepositoryPort;
    private final AccountSubTypeRepositoryPort accountSubTypeRepositoryPort;

    /**
     * Constructs the AccountTypeService with required dependencies.
     *
     * @param accountTypeRepositoryPort the account type repository port
     * @param accountSubTypeRepositoryPort the account sub-type repository port
     */
    public AccountTypeService(
            AccountTypeRepositoryPort accountTypeRepositoryPort,
            AccountSubTypeRepositoryPort accountSubTypeRepositoryPort) {
        this.accountTypeRepositoryPort = accountTypeRepositoryPort;
        this.accountSubTypeRepositoryPort = accountSubTypeRepositoryPort;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Flux<AccountTypeResponse> findAll(boolean activeOnly) {
        LOG.debug("Finding all account types, activeOnly={}", activeOnly);

        Flux<AccountType> types = activeOnly
                ? accountTypeRepositoryPort.findAllActiveOrderByOrdre()
                : accountTypeRepositoryPort.findAllOrderByOrdre();

        return types.map(this::toResponse);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Mono<AccountTypeResponse> findById(UUID id) {
        LOG.debug("Finding account type by id={}", id);

        return accountTypeRepositoryPort.findById(id)
                .map(this::toResponse)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Mono<AccountTypeResponse> findByIdWithSubTypes(UUID id) {
        LOG.debug("Finding account type by id={} with sub-types", id);

        return accountTypeRepositoryPort.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
                .flatMap(accountType -> {
                    AccountTypeResponse response = toResponse(accountType);

                    return accountSubTypeRepositoryPort.findByAccountTypeId(id)
                            .map(subType -> toSubTypeResponse(subType, accountType))
                            .collectList()
                            .map(subTypes -> {
                                response.setSubTypes(subTypes);
                                return response;
                            });
                });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Mono<AccountTypeResponse> findByCode(String code) {
        LOG.debug("Finding account type by code={}", code);

        return accountTypeRepositoryPort.findByCode(code.toUpperCase())
                .map(this::toResponse)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, "code", code)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Flux<AccountTypeResponse> findCheckEmitters() {
        LOG.debug("Finding account types that can emit checks");
        return accountTypeRepositoryPort.findByPeutEmettreChequesTrue()
                .map(this::toResponse);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Flux<AccountTypeResponse> findCheckReceivers() {
        LOG.debug("Finding account types that can receive checks");
        return accountTypeRepositoryPort.findByPeutRecevoirChequesTrue()
                .map(this::toResponse);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Flux<AccountTypeResponse> findCashEnabled() {
        LOG.debug("Finding account types that allow cash transactions");
        return accountTypeRepositoryPort.findByPeutTransactionsEspecesTrue()
                .map(this::toResponse);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Flux<AccountTypeResponse> findOverdraftEnabled() {
        LOG.debug("Finding account types with overdraft enabled");
        return accountTypeRepositoryPort.findByDecouvertAutoriseTrue()
                .map(this::toResponse);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<AccountTypeResponse> create(CreateAccountTypeRequest request) {
        String code = request.getCode().toUpperCase();
        LOG.info("Creating account type with code={}", code);

        return accountTypeRepositoryPort.existsByCode(code)
                .flatMap(exists -> {
                    if (exists) {
                        LOG.warn("Account type creation failed: code {} already exists", code);
                        return Mono.error(new DuplicateResourceException(RESOURCE_NAME, "code", code));
                    }

                    AccountType entity = toEntity(request);
                    return accountTypeRepositoryPort.save(entity)
                            .doOnSuccess(saved -> LOG.info("Account type created successfully: id={}, code={}",
                                    saved.getId(), saved.getCode()))
                            .map(this::toResponse);
                });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<AccountTypeResponse> update(UUID id, UpdateAccountTypeRequest request) {
        LOG.info("Updating account type with id={}", id);

        return accountTypeRepositoryPort.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
                .flatMap(existing -> {
                    // Check for duplicate code if code is being changed
                    if (request.getCode() != null && !request.getCode().equalsIgnoreCase(existing.getCode())) {
                        return accountTypeRepositoryPort.existsByCodeAndIdNot(request.getCode().toUpperCase(), id)
                                .flatMap(exists -> {
                                    if (exists) {
                                        LOG.warn("Account type update failed: code {} already exists", request.getCode());
                                        return Mono.error(new DuplicateResourceException(RESOURCE_NAME, "code", request.getCode()));
                                    }
                                    return saveUpdatedAccountType(existing, request);
                                });
                    }
                    return saveUpdatedAccountType(existing, request);
                });
    }

    /**
     * Saves the updated account type entity.
     */
    private Mono<AccountTypeResponse> saveUpdatedAccountType(
            AccountType existing,
            UpdateAccountTypeRequest request) {
        AccountType updated = updateEntity(existing, request);
        return accountTypeRepositoryPort.save(updated)
                .doOnSuccess(saved -> LOG.info("Account type updated successfully: id={}", saved.getId()))
                .map(this::toResponse);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Void> delete(UUID id) {
        LOG.info("Deleting account type with id={}", id);

        return accountTypeRepositoryPort.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
                .flatMap(type -> accountTypeRepositoryPort.delete(type)
                        .doOnSuccess(v -> LOG.info("Account type deleted successfully: id={}, code={}",
                                type.getId(), type.getCode())));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Flux<AccountSubTypeResponse> findSubTypes(UUID accountTypeId, boolean activeOnly) {
        LOG.debug("Finding sub-types for account type id={}, activeOnly={}", accountTypeId, activeOnly);

        return accountTypeRepositoryPort.findById(accountTypeId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, accountTypeId)))
                .flatMapMany(parent -> {
                    Flux<AccountSubType> subTypes = activeOnly
                            ? accountSubTypeRepositoryPort.findByAccountTypeIdAndIsActiveTrue(accountTypeId)
                            : accountSubTypeRepositoryPort.findByAccountTypeId(accountTypeId);

                    return subTypes.map(subType -> toSubTypeResponse(subType, parent));
                });
    }

    // ========== Mapping Methods ==========

    /**
     * Converts a CreateAccountTypeRequest to an AccountType domain model.
     */
    private AccountType toEntity(CreateAccountTypeRequest request) {
        LocalDateTime now = LocalDateTime.now();

        return AccountType.builder()
                .id(UUID.randomUUID())
                .code(request.getCode().toUpperCase())
                .libelle(request.getLibelle())
                .description(request.getDescription())
                .peutEmettreChecques(request.getPeutEmettreChecques() != null ? request.getPeutEmettreChecques() : false)
                .peutRecevoirChecques(request.getPeutRecevoirChecques() != null ? request.getPeutRecevoirChecques() : false)
                .peutTransactionsEspeces(request.getPeutTransactionsEspeces() != null ? request.getPeutTransactionsEspeces() : false)
                .decouvertAutorise(request.getDecouvertAutorise() != null ? request.getDecouvertAutorise() : false)
                .decouvertParDefaut(request.getDecouvertParDefaut() != null ? request.getDecouvertParDefaut() : BigDecimal.ZERO)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .ordreAffichage(request.getOrdreAffichage() != null ? request.getOrdreAffichage() : 0)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * Updates an existing AccountType domain model with values from UpdateAccountTypeRequest.
     */
    private AccountType updateEntity(AccountType existing, UpdateAccountTypeRequest request) {
        if (request.getCode() != null) {
            existing.setCode(request.getCode().toUpperCase());
        }
        if (request.getLibelle() != null) {
            existing.setLibelle(request.getLibelle());
        }
        if (request.getDescription() != null) {
            existing.setDescription(request.getDescription());
        }
        if (request.getPeutEmettreChecques() != null) {
            existing.setPeutEmettreChecques(request.getPeutEmettreChecques());
        }
        if (request.getPeutRecevoirChecques() != null) {
            existing.setPeutRecevoirChecques(request.getPeutRecevoirChecques());
        }
        if (request.getPeutTransactionsEspeces() != null) {
            existing.setPeutTransactionsEspeces(request.getPeutTransactionsEspeces());
        }
        if (request.getDecouvertAutorise() != null) {
            existing.setDecouvertAutorise(request.getDecouvertAutorise());
        }
        if (request.getDecouvertParDefaut() != null) {
            existing.setDecouvertParDefaut(request.getDecouvertParDefaut());
        }
        if (request.getIsActive() != null) {
            existing.setIsActive(request.getIsActive());
        }
        if (request.getOrdreAffichage() != null) {
            existing.setOrdreAffichage(request.getOrdreAffichage());
        }
        existing.setUpdatedAt(LocalDateTime.now());

        return existing;
    }

    /**
     * Converts an AccountType domain model to an AccountTypeResponse DTO.
     */
    private AccountTypeResponse toResponse(AccountType entity) {
        return AccountTypeResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .libelle(entity.getLibelle())
                .description(entity.getDescription())
                .peutEmettreChecques(entity.getPeutEmettreChecques())
                .peutRecevoirChecques(entity.getPeutRecevoirChecques())
                .peutTransactionsEspeces(entity.getPeutTransactionsEspeces())
                .decouvertAutorise(entity.getDecouvertAutorise())
                .decouvertParDefaut(entity.getDecouvertParDefaut())
                .isActive(entity.getIsActive())
                .ordreAffichage(entity.getOrdreAffichage())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Converts an AccountSubType domain model to an AccountSubTypeResponse DTO.
     * Computes effective values from parent AccountType.
     */
    private AccountSubTypeResponse toSubTypeResponse(AccountSubType entity, AccountType parent) {
        return AccountSubTypeResponse.builder()
                .id(entity.getId())
                .accountTypeId(entity.getAccountTypeId())
                .code(entity.getCode())
                .libelle(entity.getLibelle())
                .description(entity.getDescription())
                // Override values
                .peutEmettreChequesOverride(entity.getPeutEmettreChequesOverride())
                .peutRecevoirChequesOverride(entity.getPeutRecevoirChequesOverride())
                .peutTransactionsEspecesOverride(entity.getPeutTransactionsEspecesOverride())
                .decouvertAutoriseOverride(entity.getDecouvertAutoriseOverride())
                .decouvertParDefautOverride(entity.getDecouvertParDefautOverride())
                // Effective values (computed)
                .peutEmettreChecques(entity.getEffectivePeutEmettreChecques(parent.getPeutEmettreChecques()))
                .peutRecevoirChecques(entity.getEffectivePeutRecevoirChecques(parent.getPeutRecevoirChecques()))
                .peutTransactionsEspeces(entity.getEffectivePeutTransactionsEspeces(parent.getPeutTransactionsEspeces()))
                .decouvertAutorise(entity.getEffectiveDecouvertAutorise(parent.getDecouvertAutorise()))
                .decouvertParDefaut(entity.getEffectiveDecouvertParDefaut(parent.getDecouvertParDefaut()))
                // Other fields
                .isActive(entity.getIsActive())
                .ordreAffichage(entity.getOrdreAffichage())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
