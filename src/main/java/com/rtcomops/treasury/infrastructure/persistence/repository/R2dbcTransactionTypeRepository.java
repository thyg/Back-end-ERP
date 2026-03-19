package com.rtcomops.treasury.infrastructure.persistence.repository;

import com.rtcomops.treasury.infrastructure.persistence.entity.TransactionTypeEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * R2DBC repository for TransactionTypeEntity persistence operations.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-10
 */
@Repository
public interface R2dbcTransactionTypeRepository extends R2dbcRepository<TransactionTypeEntity, UUID> {

    Mono<TransactionTypeEntity> findByCode(String code);

    Mono<Boolean> existsByCode(String code);

    @Query("SELECT * FROM treasury.transaction_types WHERE is_active = true ORDER BY code ASC")
    Flux<TransactionTypeEntity> findAllActiveOrderByCode();

    @Query("SELECT * FROM treasury.transaction_types ORDER BY code ASC")
    Flux<TransactionTypeEntity> findAllOrderByCode();

    @Query("SELECT * FROM treasury.transaction_types WHERE category = :category AND is_active = true ORDER BY code ASC")
    Flux<TransactionTypeEntity> findByCategory(String category);

    @Query("SELECT COUNT(*) > 0 FROM treasury.transaction_types WHERE code = :code AND id != :id")
    Mono<Boolean> existsByCodeAndIdNot(String code, UUID id);
}
