package com.rtcomops.treasury.service;

import com.rtcomops.treasury.dto.request.CreateCheckRequest;
import com.rtcomops.treasury.dto.request.UpdateCheckRequest;
import com.rtcomops.treasury.dto.response.CheckResponse;
import com.rtcomops.treasury.entity.Check;
import com.rtcomops.treasury.enums.AuditAction;
import com.rtcomops.treasury.enums.AuditModule;
import com.rtcomops.treasury.exception.DuplicateResourceException;
import com.rtcomops.treasury.exception.ResourceNotFoundException;
import com.rtcomops.treasury.mapper.CheckMapper;
import com.rtcomops.treasury.repository.BankAccountRepository;
import com.rtcomops.treasury.repository.CheckRepository;
import com.rtcomops.treasury.service.AuditLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Service layer for Check operations.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Service
@Transactional
public class CheckService {

    private static final Logger LOG = LoggerFactory.getLogger(CheckService.class);
    private static final String RESOURCE_NAME = "Check";

    private final CheckRepository checkRepository;
    private final BankAccountRepository accountRepository;
    private final CheckMapper checkMapper;
    private final AuditLogService auditLogService;

    public CheckService(
            CheckRepository checkRepository,
            BankAccountRepository accountRepository,
            CheckMapper checkMapper,
            AuditLogService auditLogService) {
        this.checkRepository = checkRepository;
        this.accountRepository = accountRepository;
        this.checkMapper = checkMapper;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public Flux<CheckResponse> findAll() {
        LOG.debug("Finding all checks");
        return checkRepository.findAllOrderByDateDesc()
            .flatMap(this::enrichWithAccountName);
    }

    @Transactional(readOnly = true)
    public Mono<CheckResponse> findById(UUID id) {
        LOG.debug("Finding check by id={}", id);
        return checkRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(this::enrichWithAccountName);
    }

    @Transactional(readOnly = true)
    public Flux<CheckResponse> findByType(String checkType) {
        LOG.debug("Finding checks by type={}", checkType);
        return checkRepository.findByCheckType(checkType.toUpperCase())
            .flatMap(this::enrichWithAccountName);
    }

    @Transactional(readOnly = true)
    public Flux<CheckResponse> findByStatus(String status) {
        LOG.debug("Finding checks by status={}", status);
        return checkRepository.findByStatus(status.toUpperCase())
            .flatMap(this::enrichWithAccountName);
    }

    @Transactional(readOnly = true)
    public Flux<CheckResponse> findByTypeAndStatus(String checkType, String status) {
        LOG.debug("Finding checks by type={} and status={}", checkType, status);
        return checkRepository.findByCheckTypeAndStatus(checkType.toUpperCase(), status.toUpperCase())
            .flatMap(this::enrichWithAccountName);
    }

    @Transactional(readOnly = true)
    public Flux<CheckResponse> findByAccountId(UUID accountId) {
        LOG.debug("Finding checks by accountId={}", accountId);
        return checkRepository.findByBankAccountId(accountId)
            .flatMap(this::enrichWithAccountName);
    }

    @Transactional(readOnly = true)
    public Flux<CheckResponse> findPendingChecksDueBefore(LocalDate date) {
        LOG.debug("Finding pending checks due before {}", date);
        return checkRepository.findPendingChecksDueBefore(date)
            .flatMap(this::enrichWithAccountName);
    }

    public Mono<CheckResponse> create(CreateCheckRequest request) {
        LOG.info("Creating check number={} for account={}", request.getCheckNumber(), request.getBankAccountId());
        
        return accountRepository.findById(request.getBankAccountId())
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("BankAccount", request.getBankAccountId())))
            .flatMap(account -> checkRepository.existsByCheckNumberAndAccountAndType(
                    request.getCheckNumber(), request.getBankAccountId(), request.getCheckType().toUpperCase())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new DuplicateResourceException(
                            RESOURCE_NAME, "checkNumber", request.getCheckNumber()));
                    }
                    Check entity = checkMapper.toEntity(request);
                    return checkRepository.save(entity)
                        .flatMap(saved -> auditLogService.log(
                                AuditModule.CHECK,
                                AuditAction.CREATE,
                                saved.getId(),
                                saved.getCheckNumber(),
                                null,
                                saved,
                                "Création du chèque n°" + saved.getCheckNumber()
                        ).thenReturn(saved))
                        .doOnSuccess(saved -> LOG.info("Check created: id={}", saved.getId()))
                        .map(saved -> checkMapper.toResponseWithAccountName(saved, account.getName()));
                }));
    }

    public Mono<CheckResponse> update(UUID id, UpdateCheckRequest request) {
        LOG.info("Updating check id={}", id);
        
        return checkRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(existing -> {
                Mono<Void> validation = Mono.empty();
                
                if (request.getCheckNumber() != null && 
                    !request.getCheckNumber().equals(existing.getCheckNumber())) {
                    validation = checkRepository.existsByCheckNumberAndAccountAndTypeAndIdNot(
                            request.getCheckNumber(), 
                            existing.getBankAccountId(), 
                            existing.getCheckType(), 
                            id)
                        .flatMap(exists -> {
                            if (exists) {
                                return Mono.error(new DuplicateResourceException(
                                    RESOURCE_NAME, "checkNumber", request.getCheckNumber()));
                            }
                            return Mono.empty();
                        });
                }
                
                return validation.then(Mono.defer(() -> {
                    Check updated = checkMapper.updateEntity(existing, request);
                    return checkRepository.save(updated)
                        .flatMap(saved -> auditLogService.log(
                                AuditModule.CHECK,
                                AuditAction.UPDATE,
                                saved.getId(),
                                saved.getCheckNumber(),
                                existing,
                                saved,
                                "Mise à jour du chèque n°" + saved.getCheckNumber()
                        ).thenReturn(saved))
                        .flatMap(this::enrichWithAccountName);
                }));
            });
    }

    public Mono<Void> delete(UUID id) {
        LOG.info("Deleting check id={}", id);
        
        return checkRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(check -> auditLogService.log(
                    AuditModule.CHECK,
                    AuditAction.DELETE,
                    check.getId(),
                    check.getCheckNumber(),
                    check,
                    null,
                    "Suppression du chèque n°" + check.getCheckNumber()
            ).then(checkRepository.delete(check).then(Mono.fromRunnable(() -> LOG.info("Check deleted: id={}", id)))));
    }

    public Mono<CheckResponse> deposit(UUID id, LocalDate depositDate) {
        LOG.info("Depositing check id={} on date={}", id, depositDate);
        
        return checkRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(existing -> {
                Check original = existing.toBuilder().build(); // Create a copy for the audit log
                existing.setStatus("DEPOSITED");
                existing.setDepositDate(depositDate);
                existing.setNew(false);
                return checkRepository.save(existing)
                    .flatMap(saved -> auditLogService.log(
                            AuditModule.CHECK,
                            AuditAction.DEPOSIT,
                            saved.getId(),
                            saved.getCheckNumber(),
                            original,
                            saved,
                            "Remise en banque du chèque n°" + saved.getCheckNumber()
                    ).thenReturn(saved))
                    .flatMap(this::enrichWithAccountName);
            });
    }

    public Mono<CheckResponse> cash(UUID id, LocalDate cashedDate) {
        LOG.info("Cashing check id={} on date={}", id, cashedDate);
        
        return checkRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(existing -> {
                Check original = existing.toBuilder().build();
                existing.setStatus("CASHED");
                existing.setCashedDate(cashedDate);
                existing.setNew(false);
                return checkRepository.save(existing)
                    .flatMap(saved -> auditLogService.log(
                            AuditModule.CHECK,
                            AuditAction.CASH,
                            saved.getId(),
                            saved.getCheckNumber(),
                            original,
                            saved,
                            "Encaissement du chèque n°" + saved.getCheckNumber()
                    ).thenReturn(saved))
                    .flatMap(this::enrichWithAccountName);
            });
    }

    public Mono<CheckResponse> reject(UUID id, String reason) {
        LOG.info("Rejecting check id={}, reason={}", id, reason);
        
        return checkRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(existing -> {
                Check original = existing.toBuilder().build();
                existing.setStatus("REJECTED");
                existing.setRejectionReason(reason);
                existing.setNew(false);
                return checkRepository.save(existing)
                    .flatMap(saved -> auditLogService.log(
                            AuditModule.CHECK,
                            AuditAction.REJECT,
                            saved.getId(),
                            saved.getCheckNumber(),
                            original,
                            saved,
                            "Rejet du chèque n°" + saved.getCheckNumber() + ". Raison: " + reason
                    ).thenReturn(saved))
                    .flatMap(this::enrichWithAccountName);
            });
    }

    public Mono<CheckResponse> cancel(UUID id) {
        LOG.info("Cancelling check id={}", id);
        
        return checkRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(existing -> {
                Check original = existing.toBuilder().build();
                existing.setStatus("CANCELLED");
                existing.setNew(false);
                return checkRepository.save(existing)
                    .flatMap(saved -> auditLogService.log(
                            AuditModule.CHECK,
                            AuditAction.CANCEL,
                            saved.getId(),
                            saved.getCheckNumber(),
                            original,
                            saved,
                            "Annulation du chèque n°" + saved.getCheckNumber()
                    ).thenReturn(saved))
                    .flatMap(this::enrichWithAccountName);
            });
    }

    private Mono<CheckResponse> enrichWithAccountName(Check check) {
        return accountRepository.findById(check.getBankAccountId())
            .map(account -> checkMapper.toResponseWithAccountName(check, account.getName()))
            .defaultIfEmpty(checkMapper.toResponse(check));
    }
}
