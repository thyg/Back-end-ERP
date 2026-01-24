package com.rtcomops.treasury.service;

import com.rtcomops.treasury.dto.request.CreateAccountTypeRequest;
import com.rtcomops.treasury.dto.request.UpdateAccountTypeRequest;
import com.rtcomops.treasury.dto.response.AccountSubTypeResponse;
import com.rtcomops.treasury.dto.response.AccountTypeResponse;
import com.rtcomops.treasury.entity.AccountType;
import com.rtcomops.treasury.exception.DuplicateResourceException;
import com.rtcomops.treasury.exception.ResourceNotFoundException;
import com.rtcomops.treasury.mapper.AccountTypeMapper;
import com.rtcomops.treasury.repository.AccountSubTypeRepository;
import com.rtcomops.treasury.repository.AccountTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Service layer for AccountType operations.
 *
 * <p>Provides business logic for managing account types, including CRUD operations
 * with validation and error handling.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
@Service
@Transactional
public class AccountTypeService {

    private static final Logger LOG = LoggerFactory.getLogger(AccountTypeService.class);
    private static final String RESOURCE_NAME = "AccountType";

    private final AccountTypeRepository accountTypeRepository;
    private final AccountSubTypeRepository accountSubTypeRepository;
    private final AccountTypeMapper accountTypeMapper;

    /**
     * Constructs the AccountTypeService with required dependencies.
     *
     * @param accountTypeRepository the account type repository
     * @param accountSubTypeRepository the account sub-type repository
     * @param accountTypeMapper the account type mapper
     */
    public AccountTypeService(
            AccountTypeRepository accountTypeRepository,
            AccountSubTypeRepository accountSubTypeRepository,
            AccountTypeMapper accountTypeMapper) {
        this.accountTypeRepository = accountTypeRepository;
        this.accountSubTypeRepository = accountSubTypeRepository;
        this.accountTypeMapper = accountTypeMapper;
    }

    /**
     * Retrieves all account types ordered by display order.
     *
     * @param activeOnly if true, returns only active account types
     * @return Flux of AccountTypeResponse DTOs
     */
    @Transactional(readOnly = true)
    public Flux<AccountTypeResponse> findAll(boolean activeOnly) {
        LOG.debug("Finding all account types, activeOnly={}", activeOnly);

        Flux<AccountType> types = activeOnly
            ? accountTypeRepository.findAllActiveOrderByOrdre()
            : accountTypeRepository.findAllOrderByOrdre();

        return types.map(accountTypeMapper::toResponse);
    }

    /**
     * Retrieves an account type by its ID.
     *
     * @param id the account type ID
     * @return Mono of AccountTypeResponse
     * @throws ResourceNotFoundException if account type not found
     */
    @Transactional(readOnly = true)
    public Mono<AccountTypeResponse> findById(UUID id) {
        LOG.debug("Finding account type by id={}", id);

        return accountTypeRepository.findById(id)
            .map(accountTypeMapper::toResponse)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)));
    }

    /**
     * Retrieves an account type by its ID with sub-types included.
     *
     * @param id the account type ID
     * @return Mono of AccountTypeResponse with subTypes populated
     * @throws ResourceNotFoundException if account type not found
     */
    @Transactional(readOnly = true)
    public Mono<AccountTypeResponse> findByIdWithSubTypes(UUID id) {
        LOG.debug("Finding account type by id={} with sub-types", id);

        return accountTypeRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(accountType -> {
                AccountTypeResponse response = accountTypeMapper.toResponse(accountType);

                return accountSubTypeRepository.findByAccountTypeId(id)
                    .map(subType -> accountTypeMapper.toSubTypeResponse(subType, accountType))
                    .collectList()
                    .map(subTypes -> {
                        response.setSubTypes(subTypes);
                        return response;
                    });
            });
    }

    /**
     * Retrieves an account type by its code.
     *
     * @param code the account type code
     * @return Mono of AccountTypeResponse
     * @throws ResourceNotFoundException if account type not found
     */
    @Transactional(readOnly = true)
    public Mono<AccountTypeResponse> findByCode(String code) {
        LOG.debug("Finding account type by code={}", code);

        return accountTypeRepository.findByCode(code.toUpperCase())
            .map(accountTypeMapper::toResponse)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, "code", code)));
    }

    /**
     * Retrieves account types that allow check emission.
     *
     * @return Flux of AccountTypeResponse DTOs
     */
    @Transactional(readOnly = true)
    public Flux<AccountTypeResponse> findCheckEmitters() {
        LOG.debug("Finding account types that can emit checks");
        return accountTypeRepository.findByPeutEmettreChequesTrue()
            .map(accountTypeMapper::toResponse);
    }

    /**
     * Retrieves account types that allow check reception.
     *
     * @return Flux of AccountTypeResponse DTOs
     */
    @Transactional(readOnly = true)
    public Flux<AccountTypeResponse> findCheckReceivers() {
        LOG.debug("Finding account types that can receive checks");
        return accountTypeRepository.findByPeutRecevoirChequesTrue()
            .map(accountTypeMapper::toResponse);
    }

    /**
     * Retrieves account types that allow cash transactions.
     *
     * @return Flux of AccountTypeResponse DTOs
     */
    @Transactional(readOnly = true)
    public Flux<AccountTypeResponse> findCashEnabled() {
        LOG.debug("Finding account types that allow cash transactions");
        return accountTypeRepository.findByPeutTransactionsEspecesTrue()
            .map(accountTypeMapper::toResponse);
    }

    /**
     * Retrieves account types that allow overdraft.
     *
     * @return Flux of AccountTypeResponse DTOs
     */
    @Transactional(readOnly = true)
    public Flux<AccountTypeResponse> findOverdraftEnabled() {
        LOG.debug("Finding account types with overdraft enabled");
        return accountTypeRepository.findByDecouvertAutoriseTrue()
            .map(accountTypeMapper::toResponse);
    }

    /**
     * Creates a new account type.
     *
     * @param request the create request DTO
     * @return Mono of created AccountTypeResponse
     * @throws DuplicateResourceException if code already exists
     */
    public Mono<AccountTypeResponse> create(CreateAccountTypeRequest request) {
        String code = request.getCode().toUpperCase();
        LOG.info("Creating account type with code={}", code);

        return accountTypeRepository.existsByCode(code)
            .flatMap(exists -> {
                if (exists) {
                    LOG.warn("Account type creation failed: code {} already exists", code);
                    return Mono.error(new DuplicateResourceException(RESOURCE_NAME, "code", code));
                }

                AccountType entity = accountTypeMapper.toEntity(request);
                return accountTypeRepository.save(entity)
                    .doOnSuccess(saved -> LOG.info("Account type created successfully: id={}, code={}",
                        saved.getId(), saved.getCode()))
                    .map(accountTypeMapper::toResponse);
            });
    }

    /**
     * Updates an existing account type.
     *
     * @param id the account type ID to update
     * @param request the update request DTO
     * @return Mono of updated AccountTypeResponse
     * @throws ResourceNotFoundException if account type not found
     * @throws DuplicateResourceException if new code already exists
     */
    public Mono<AccountTypeResponse> update(UUID id, UpdateAccountTypeRequest request) {
        LOG.info("Updating account type with id={}", id);

        return accountTypeRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(existing -> {
                // Check for duplicate code if code is being changed
                if (request.getCode() != null && !request.getCode().equalsIgnoreCase(existing.getCode())) {
                    return accountTypeRepository.existsByCodeAndIdNot(request.getCode().toUpperCase(), id)
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
     *
     * @param existing the existing entity
     * @param request the update request
     * @return Mono of updated AccountTypeResponse
     */
    private Mono<AccountTypeResponse> saveUpdatedAccountType(
            AccountType existing,
            UpdateAccountTypeRequest request) {
        AccountType updated = accountTypeMapper.updateEntity(existing, request);
        return accountTypeRepository.save(updated)
            .doOnSuccess(saved -> LOG.info("Account type updated successfully: id={}", saved.getId()))
            .map(accountTypeMapper::toResponse);
    }

    /**
     * Deletes an account type by its ID.
     * Also deletes all associated sub-types (CASCADE).
     *
     * @param id the account type ID to delete
     * @return Mono that completes when deletion is done
     * @throws ResourceNotFoundException if account type not found
     */
    public Mono<Void> delete(UUID id) {
        LOG.info("Deleting account type with id={}", id);

        return accountTypeRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(type -> accountTypeRepository.delete(type)
                .doOnSuccess(v -> LOG.info("Account type deleted successfully: id={}, code={}",
                    type.getId(), type.getCode())));
    }

    /**
     * Retrieves sub-types for a given account type.
     *
     * @param accountTypeId the parent account type ID
     * @param activeOnly if true, returns only active sub-types
     * @return Flux of AccountSubTypeResponse DTOs
     */
    @Transactional(readOnly = true)
    public Flux<AccountSubTypeResponse> findSubTypes(UUID accountTypeId, boolean activeOnly) {
        LOG.debug("Finding sub-types for account type id={}, activeOnly={}", accountTypeId, activeOnly);

        return accountTypeRepository.findById(accountTypeId)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, accountTypeId)))
            .flatMapMany(parent -> {
                Flux<com.rtcomops.treasury.entity.AccountSubType> subTypes = activeOnly
                    ? accountSubTypeRepository.findByAccountTypeIdAndIsActiveTrue(accountTypeId)
                    : accountSubTypeRepository.findByAccountTypeId(accountTypeId);

                return subTypes.map(subType -> accountTypeMapper.toSubTypeResponse(subType, parent));
            });
    }
}
