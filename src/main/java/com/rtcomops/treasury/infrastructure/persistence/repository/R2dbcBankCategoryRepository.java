package com.rtcomops.treasury.infrastructure.persistence.repository;

import com.rtcomops.treasury.infrastructure.persistence.entity.BankCategoryEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * R2DBC repository for BankCategoryEntity persistence operations.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
@Repository
public interface R2dbcBankCategoryRepository extends R2dbcRepository<BankCategoryEntity, UUID> {

    Mono<BankCategoryEntity> findByCode(String code);
}
