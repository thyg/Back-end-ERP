package com.rtcomops.treasury.service;

import com.rtcomops.treasury.dto.request.CreateBankRequest;
import com.rtcomops.treasury.dto.request.UpdateBankRequest;
import com.rtcomops.treasury.dto.response.BankResponse;
import com.rtcomops.treasury.entity.Bank;
import com.rtcomops.treasury.enums.AuditAction;
import com.rtcomops.treasury.enums.AuditModule;
import com.rtcomops.treasury.exception.DuplicateResourceException;
import com.rtcomops.treasury.exception.ResourceNotFoundException;
import com.rtcomops.treasury.mapper.BankMapper;
import com.rtcomops.treasury.repository.BankRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Service layer for Bank operations.
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
public class BankService {

    private static final Logger LOG = LoggerFactory.getLogger(BankService.class);
    private static final String RESOURCE_NAME = "Bank";

    private final BankRepository bankRepository;
    private final BankMapper bankMapper;
    private final AuditLogService auditLogService;

    /**
     * Constructs the BankService with required dependencies.
     *
     * @param bankRepository the bank repository
     * @param bankMapper the bank mapper
     * @param auditLogService the audit log service
     */
    public BankService(BankRepository bankRepository, BankMapper bankMapper, AuditLogService auditLogService) {
        this.bankRepository = bankRepository;
        this.bankMapper = bankMapper;
        this.auditLogService = auditLogService;
    }

    /**
     * Retrieves all banks ordered by name.
     *
     * @param activeOnly if true, returns only active banks
     * @return Flux of BankResponse DTOs
     */
    @Transactional(readOnly = true)
    public Flux<BankResponse> findAll(boolean activeOnly) {
        LOG.debug("Finding all banks, activeOnly={}", activeOnly);
        
        Flux<Bank> banks = activeOnly 
            ? bankRepository.findAllActiveOrderByName()
            : bankRepository.findAllOrderByName();
        
        return banks.map(bankMapper::toResponse);
    }

    /**
     * Retrieves a bank by its ID.
     *
     * @param id the bank ID
     * @return Mono of BankResponse
     * @throws ResourceNotFoundException if bank not found
     */
    @Transactional(readOnly = true)
    public Mono<BankResponse> findById(UUID id) {
        LOG.debug("Finding bank by id={}", id);
        
        return bankRepository.findById(id)
            .map(bankMapper::toResponse)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)));
    }

    /**
     * Retrieves a bank by its code.
     *
     * @param code the bank code
     * @return Mono of BankResponse
     * @throws ResourceNotFoundException if bank not found
     */
    @Transactional(readOnly = true)
    public Mono<BankResponse> findByCode(String code) {
        LOG.debug("Finding bank by code={}", code);
        
        return bankRepository.findByCode(code.toUpperCase())
            .map(bankMapper::toResponse)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, "code", code)));
    }

    /**
     * Creates a new bank.
     *
     * @param request the create request DTO
     * @return Mono of created BankResponse
     * @throws DuplicateResourceException if code already exists
     */
    public Mono<BankResponse> create(CreateBankRequest request) {
        String code = request.getCode().toUpperCase();
        LOG.info("Creating bank with code={}", code);
        
        return bankRepository.existsByCode(code)
            .flatMap(exists -> {
                if (exists) {
                    LOG.warn("Bank creation failed: code {} already exists", code);
                    return Mono.error(new DuplicateResourceException(RESOURCE_NAME, "code", code));
                }
                
                Bank entity = bankMapper.toEntity(request);
                return bankRepository.save(entity)
                    .doOnSuccess(saved -> LOG.info("Bank created successfully: id={}, code={}", 
                        saved.getId(), saved.getCode()))
                    .flatMap(saved -> auditLogService.log(
                        AuditModule.BANK,
                        AuditAction.CREATE,
                        saved.getId(),
                        "Banque " + saved.getCode(),
                        null,
                        saved,
                        "Création de la banque " + saved.getName()
                    ).thenReturn(saved))
                    .map(bankMapper::toResponse);
            });
    }

    /**
     * Updates an existing bank.
     *
     * @param id the bank ID to update
     * @param request the update request DTO
     * @return Mono of updated BankResponse
     * @throws ResourceNotFoundException if bank not found
     * @throws DuplicateResourceException if new code already exists
     */
    public Mono<BankResponse> update(UUID id, UpdateBankRequest request) {
        LOG.info("Updating bank with id={}", id);
        
        return bankRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(existing -> {
                // Check for duplicate code if code is being changed
                if (request.getCode() != null && !request.getCode().equalsIgnoreCase(existing.getCode())) {
                    return bankRepository.existsByCodeAndIdNot(request.getCode().toUpperCase(), id)
                        .flatMap(exists -> {
                            if (exists) {
                                LOG.warn("Bank update failed: code {} already exists", request.getCode());
                                return Mono.error(new DuplicateResourceException(
                                    RESOURCE_NAME, "code", request.getCode()));
                            }
                            return saveUpdatedBank(existing, request);
                        });
                }
                return saveUpdatedBank(existing, request);
            });
    }

    /**
     * Saves the updated bank entity.
     *
     * @param existing the existing bank entity
     * @param request the update request
     * @return Mono of updated BankResponse
     */
    private Mono<BankResponse> saveUpdatedBank(Bank existing, UpdateBankRequest request) {
        Bank updated = bankMapper.updateEntity(existing, request);
        return bankRepository.save(updated)
            .doOnSuccess(saved -> LOG.info("Bank updated successfully: id={}", saved.getId()))
            .flatMap(saved -> auditLogService.log(
                AuditModule.BANK,
                AuditAction.UPDATE,
                saved.getId(),
                "Banque " + saved.getCode(),
                existing,
                saved,
                "Mise à jour de la banque " + saved.getName()
            ).thenReturn(saved))
            .map(bankMapper::toResponse);
    }

    /**
     * Deletes a bank by its ID.
     *
     * @param id the bank ID to delete
     * @return Mono that completes when deletion is done
     * @throws ResourceNotFoundException if bank not found
     */
    public Mono<Void> delete(UUID id) {
        LOG.info("Deleting bank with id={}", id);
        
        return bankRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(bank -> auditLogService.log(
                    AuditModule.BANK,
                    AuditAction.DELETE,
                    bank.getId(),
                    "Banque " + bank.getCode(),
                    bank,
                    null,
                    "Suppression de la banque " + bank.getName()
                ).then(bankRepository.delete(bank)
                .doOnSuccess(v -> LOG.info("Bank deleted successfully: id={}, code={}", 
                    bank.getId(), bank.getCode()))));
    }
}
