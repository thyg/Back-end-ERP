package com.rtcomops.treasury.service;

import com.rtcomops.treasury.dto.request.CreateTransactionTypeRequest;
import com.rtcomops.treasury.dto.request.UpdateTransactionTypeRequest;
import com.rtcomops.treasury.dto.response.TransactionTypeResponse;
import com.rtcomops.treasury.entity.TransactionType;
import com.rtcomops.treasury.exception.DuplicateResourceException;
import com.rtcomops.treasury.exception.ResourceNotFoundException;
import com.rtcomops.treasury.mapper.TransactionTypeMapper;
import com.rtcomops.treasury.repository.TransactionTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Service layer for TransactionType operations.
 *
 * <p>Provides business logic for managing transaction types, including CRUD operations
 * with validation and error handling.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-10
 */
@Service
@Transactional
public class TransactionTypeService {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionTypeService.class);
    private static final String RESOURCE_NAME = "TransactionType";

    private final TransactionTypeRepository transactionTypeRepository;
    private final TransactionTypeMapper transactionTypeMapper;

    /**
     * Constructs the TransactionTypeService with required dependencies.
     *
     * @param transactionTypeRepository the transaction type repository
     * @param transactionTypeMapper the transaction type mapper
     */
    public TransactionTypeService(
            TransactionTypeRepository transactionTypeRepository,
            TransactionTypeMapper transactionTypeMapper) {
        this.transactionTypeRepository = transactionTypeRepository;
        this.transactionTypeMapper = transactionTypeMapper;
    }

    /**
     * Retrieves all transaction types ordered by code.
     *
     * @param activeOnly if true, returns only active transaction types
     * @return Flux of TransactionTypeResponse DTOs
     */
    @Transactional(readOnly = true)
    public Flux<TransactionTypeResponse> findAll(boolean activeOnly) {
        LOG.debug("Finding all transaction types, activeOnly={}", activeOnly);
        
        Flux<TransactionType> types = activeOnly 
            ? transactionTypeRepository.findAllActiveOrderByCode()
            : transactionTypeRepository.findAllOrderByCode();
        
        return types.map(transactionTypeMapper::toResponse);
    }

    /**
     * Retrieves a transaction type by its ID.
     *
     * @param id the transaction type ID
     * @return Mono of TransactionTypeResponse
     * @throws ResourceNotFoundException if transaction type not found
     */
    @Transactional(readOnly = true)
    public Mono<TransactionTypeResponse> findById(UUID id) {
        LOG.debug("Finding transaction type by id={}", id);
        
        return transactionTypeRepository.findById(id)
            .map(transactionTypeMapper::toResponse)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)));
    }

    /**
     * Retrieves transaction types by category.
     *
     * @param category the category to filter by (BANK, CASH, CHECK, OTHER)
     * @return Flux of TransactionTypeResponse DTOs
     */
    @Transactional(readOnly = true)
    public Flux<TransactionTypeResponse> findByCategory(String category) {
        LOG.debug("Finding transaction types by category={}", category);
        
        return transactionTypeRepository.findByCategory(category.toUpperCase())
            .map(transactionTypeMapper::toResponse);
    }

    /**
     * Creates a new transaction type.
     *
     * @param request the create request DTO
     * @return Mono of created TransactionTypeResponse
     * @throws DuplicateResourceException if code already exists
     */
    public Mono<TransactionTypeResponse> create(CreateTransactionTypeRequest request) {
        String code = request.getCode().toUpperCase();
        LOG.info("Creating transaction type with code={}", code);
        
        return transactionTypeRepository.existsByCode(code)
            .flatMap(exists -> {
                if (exists) {
                    LOG.warn("Transaction type creation failed: code {} already exists", code);
                    return Mono.error(new DuplicateResourceException(RESOURCE_NAME, "code", code));
                }
                
                TransactionType entity = transactionTypeMapper.toEntity(request);
                return transactionTypeRepository.save(entity)
                    .doOnSuccess(saved -> LOG.info("Transaction type created successfully: id={}, code={}", 
                        saved.getId(), saved.getCode()))
                    .map(transactionTypeMapper::toResponse);
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
    public Mono<TransactionTypeResponse> update(UUID id, UpdateTransactionTypeRequest request) {
        LOG.info("Updating transaction type with id={}", id);
        
        return transactionTypeRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(existing -> {
                // Check for duplicate code if code is being changed
                if (request.getCode() != null && !request.getCode().equalsIgnoreCase(existing.getCode())) {
                    return transactionTypeRepository.existsByCodeAndIdNot(request.getCode().toUpperCase(), id)
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
        TransactionType updated = transactionTypeMapper.updateEntity(existing, request);
        return transactionTypeRepository.save(updated)
            .doOnSuccess(saved -> LOG.info("Transaction type updated successfully: id={}", saved.getId()))
            .map(transactionTypeMapper::toResponse);
    }

    /**
     * Deletes a transaction type by its ID.
     *
     * @param id the transaction type ID to delete
     * @return Mono that completes when deletion is done
     * @throws ResourceNotFoundException if transaction type not found
     */
    public Mono<Void> delete(UUID id) {
        LOG.info("Deleting transaction type with id={}", id);
        
        return transactionTypeRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(type -> transactionTypeRepository.delete(type)
                .doOnSuccess(v -> LOG.info("Transaction type deleted successfully: id={}, code={}", 
                    type.getId(), type.getCode())));
    }
}
