package com.rtcomops.treasury.application.service;

import com.rtcomops.treasury.application.dto.request.CreateTransactionTypeRequest;
import com.rtcomops.treasury.application.dto.request.UpdateTransactionTypeRequest;
import com.rtcomops.treasury.application.dto.response.TransactionTypeResponse;
import com.rtcomops.treasury.domain.exception.DuplicateResourceException;
import com.rtcomops.treasury.domain.exception.ResourceNotFoundException;
import com.rtcomops.treasury.domain.model.TransactionType;
import com.rtcomops.treasury.application.port.in.TransactionTypeUseCase;
import com.rtcomops.treasury.domain.port.out.TransactionTypeRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain service for TransactionType operations.
 *
 * <p>Implements the TransactionTypeUseCase port and provides business logic
 * for managing transaction types, including CRUD operations with validation
 * and error handling.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-10
 */
@Service
@Transactional
public class TransactionTypeService implements TransactionTypeUseCase {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionTypeService.class);
    private static final String RESOURCE_NAME = "TransactionType";

    private final TransactionTypeRepositoryPort transactionTypeRepositoryPort;

    /**
     * Constructs the TransactionTypeService with required dependencies.
     *
     * @param transactionTypeRepositoryPort the transaction type repository port
     */
    public TransactionTypeService(TransactionTypeRepositoryPort transactionTypeRepositoryPort) {
        this.transactionTypeRepositoryPort = transactionTypeRepositoryPort;
    }

    /**
     * Retrieves all transaction types ordered by code.
     *
     * @param activeOnly if true, returns only active transaction types
     * @return Flux of TransactionTypeResponse DTOs
     */
    @Override
    @Transactional(readOnly = true)
    public Flux<TransactionTypeResponse> findAll(boolean activeOnly) {
        LOG.debug("Finding all transaction types, activeOnly={}", activeOnly);

        Flux<TransactionType> types = activeOnly
                ? transactionTypeRepositoryPort.findAllActiveOrderByCode()
                : transactionTypeRepositoryPort.findAllOrderByCode();

        return types.map(this::toResponse);
    }

    /**
     * Retrieves a transaction type by its ID.
     *
     * @param id the transaction type ID
     * @return Mono of TransactionTypeResponse
     * @throws ResourceNotFoundException if transaction type not found
     */
    @Override
    @Transactional(readOnly = true)
    public Mono<TransactionTypeResponse> findById(UUID id) {
        LOG.debug("Finding transaction type by id={}", id);

        return transactionTypeRepositoryPort.findById(id)
                .map(this::toResponse)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)));
    }

    /**
     * Retrieves transaction types by category.
     *
     * @param category the category to filter by (BANK, CASH, CHECK, OTHER)
     * @return Flux of TransactionTypeResponse DTOs
     */
    @Override
    @Transactional(readOnly = true)
    public Flux<TransactionTypeResponse> findByCategory(String category) {
        LOG.debug("Finding transaction types by category={}", category);

        return transactionTypeRepositoryPort.findByCategory(category.toUpperCase())
                .map(this::toResponse);
    }

    /**
     * Creates a new transaction type.
     *
     * @param request the create request DTO
     * @return Mono of created TransactionTypeResponse
     * @throws DuplicateResourceException if code already exists
     */
    @Override
    public Mono<TransactionTypeResponse> create(CreateTransactionTypeRequest request) {
        String code = request.getCode().toUpperCase();
        LOG.info("Creating transaction type with code={}", code);

        return transactionTypeRepositoryPort.existsByCode(code)
                .flatMap(exists -> {
                    if (exists) {
                        LOG.warn("Transaction type creation failed: code {} already exists", code);
                        return Mono.error(new DuplicateResourceException(RESOURCE_NAME, "code", code));
                    }

                    TransactionType entity = toEntity(request);
                    return transactionTypeRepositoryPort.save(entity)
                            .doOnSuccess(saved -> LOG.info("Transaction type created successfully: id={}, code={}",
                                    saved.getId(), saved.getCode()))
                            .map(this::toResponse);
                });
    }

    /**
     * Updates an existing transaction type.
     *
     * @param id the transaction type ID to update
     * @param request the update request DTO
     * @return Mono of updated TransactionTypeResponse
     * @throws ResourceNotFoundException if transaction type not found
     * @throws DuplicateResourceException if new code already exists
     */
    @Override
    public Mono<TransactionTypeResponse> update(UUID id, UpdateTransactionTypeRequest request) {
        LOG.info("Updating transaction type with id={}", id);

        return transactionTypeRepositoryPort.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
                .flatMap(existing -> {
                    // Check for duplicate code if code is being changed
                    if (request.getCode() != null && !request.getCode().equalsIgnoreCase(existing.getCode())) {
                        return transactionTypeRepositoryPort.existsByCodeAndIdNot(request.getCode().toUpperCase(), id)
                                .flatMap(exists -> {
                                    if (exists) {
                                        LOG.warn("Transaction type update failed: code {} already exists",
                                                request.getCode());
                                        return Mono.error(new DuplicateResourceException(
                                                RESOURCE_NAME, "code", request.getCode()));
                                    }
                                    return saveUpdatedTransactionType(existing, request);
                                });
                    }
                    return saveUpdatedTransactionType(existing, request);
                });
    }

    /**
     * Saves the updated transaction type entity.
     *
     * @param existing the existing entity
     * @param request the update request
     * @return Mono of updated TransactionTypeResponse
     */
    private Mono<TransactionTypeResponse> saveUpdatedTransactionType(
            TransactionType existing,
            UpdateTransactionTypeRequest request) {
        TransactionType updated = updateEntity(existing, request);
        return transactionTypeRepositoryPort.save(updated)
                .doOnSuccess(saved -> LOG.info("Transaction type updated successfully: id={}", saved.getId()))
                .map(this::toResponse);
    }

    /**
     * Deletes a transaction type by its ID.
     *
     * @param id the transaction type ID to delete
     * @return Mono that completes when deletion is done
     * @throws ResourceNotFoundException if transaction type not found
     */
    @Override
    public Mono<Void> delete(UUID id) {
        LOG.info("Deleting transaction type with id={}", id);

        return transactionTypeRepositoryPort.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
                .flatMap(type -> transactionTypeRepositoryPort.delete(type)
                        .doOnSuccess(v -> LOG.info("Transaction type deleted successfully: id={}, code={}",
                                type.getId(), type.getCode())));
    }

    // ========== Mapping Methods ==========

    /**
     * Converts a CreateTransactionTypeRequest to a TransactionType domain model.
     *
     * @param request the create request DTO
     * @return a new TransactionType domain model
     */
    private TransactionType toEntity(CreateTransactionTypeRequest request) {
        LocalDateTime now = LocalDateTime.now();

        return TransactionType.builder()
                .id(UUID.randomUUID())
                .code(request.getCode().toUpperCase())
                .label(request.getLabel())
                .category(request.getCategory().toUpperCase())
                .description(request.getDescription())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * Updates an existing TransactionType domain model with values from UpdateTransactionTypeRequest.
     *
     * @param existing the existing domain model
     * @param request the update request DTO
     * @return the updated TransactionType domain model
     */
    private TransactionType updateEntity(TransactionType existing, UpdateTransactionTypeRequest request) {
        if (request.getCode() != null) {
            existing.setCode(request.getCode().toUpperCase());
        }
        if (request.getLabel() != null) {
            existing.setLabel(request.getLabel());
        }
        if (request.getCategory() != null) {
            existing.setCategory(request.getCategory().toUpperCase());
        }
        if (request.getDescription() != null) {
            existing.setDescription(request.getDescription());
        }
        if (request.getIsActive() != null) {
            existing.setIsActive(request.getIsActive());
        }
        existing.setUpdatedAt(LocalDateTime.now());

        return existing;
    }

    /**
     * Converts a TransactionType domain model to a TransactionTypeResponse DTO.
     *
     * @param model the transaction type domain model
     * @return the response DTO
     */
    private TransactionTypeResponse toResponse(TransactionType model) {
        return TransactionTypeResponse.builder()
                .id(model.getId())
                .code(model.getCode())
                .label(model.getLabel())
                .category(model.getCategory())
                .description(model.getDescription())
                .isActive(model.getIsActive())
                .createdAt(model.getCreatedAt())
                .updatedAt(model.getUpdatedAt())
                .build();
    }
}
