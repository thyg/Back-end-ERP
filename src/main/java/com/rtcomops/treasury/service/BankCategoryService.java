package com.rtcomops.treasury.service;

import com.rtcomops.treasury.entity.BankCategory;
import com.rtcomops.treasury.exception.DuplicateResourceException;
import com.rtcomops.treasury.exception.ResourceNotFoundException;
import com.rtcomops.treasury.repository.BankCategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@Transactional
public class BankCategoryService {

    private static final Logger LOG = LoggerFactory.getLogger(BankCategoryService.class);

    private final BankCategoryRepository bankCategoryRepository;

    public BankCategoryService(BankCategoryRepository bankCategoryRepository) {
        this.bankCategoryRepository = bankCategoryRepository;
    }

    @Transactional(readOnly = true)
    public Flux<BankCategory> findAll() {
        LOG.debug("Finding all bank categories");
        return bankCategoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Mono<BankCategory> findById(UUID id) {
        return bankCategoryRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("BankCategory", id)));
    }

    public Mono<BankCategory> create(String code, String label) {
        LOG.debug("Creating bank category with code={}", code);
        return bankCategoryRepository.findByCode(code)
                .flatMap(existing -> Mono.<BankCategory>error(
                        new DuplicateResourceException("BankCategory", "code", code)))
                .switchIfEmpty(Mono.defer(() -> {
                    BankCategory category = BankCategory.builder()
                            .code(code.toUpperCase())
                            .label(label)
                            .build();
                    return bankCategoryRepository.save(category);
                }));
    }

    public Mono<BankCategory> update(UUID id, String code, String label) {
        LOG.debug("Updating bank category id={}", id);
        return bankCategoryRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("BankCategory", id)))
                .flatMap(existing -> {
                    // Vérifier unicité du code si changé
                    if (code != null && !code.equalsIgnoreCase(existing.getCode())) {
                        return bankCategoryRepository.findByCode(code.toUpperCase())
                                .flatMap(dup -> Mono.<BankCategory>error(
                                        new DuplicateResourceException("BankCategory", "code", code)))
                                .switchIfEmpty(Mono.defer(() -> {
                                    existing.setCode(code.toUpperCase());
                                    if (label != null) existing.setLabel(label);
                                    return bankCategoryRepository.save(existing);
                                }));
                    }
                    if (label != null) existing.setLabel(label);
                    return bankCategoryRepository.save(existing);
                });
    }

    public Mono<Void> delete(UUID id) {
        LOG.debug("Deleting bank category id={}", id);
        return bankCategoryRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("BankCategory", id)))
                .flatMap(bankCategoryRepository::delete);
    }
}
