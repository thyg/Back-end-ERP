package com.rtcomops.treasury.infrastructure.persistence.repository;

import com.rtcomops.treasury.infrastructure.persistence.entity.BankAccountEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

/**
 * R2DBC repository for BankAccountEntity persistence operations.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
@Repository
public interface R2dbcBankAccountRepository extends R2dbcRepository<BankAccountEntity, UUID> {

    @Query("SELECT * FROM treasury.bank_accounts WHERE is_active = true ORDER BY name ASC")
    Flux<BankAccountEntity> findAllActiveOrderByName();

    @Query("SELECT * FROM treasury.bank_accounts ORDER BY name ASC")
    Flux<BankAccountEntity> findAllOrderByName();

    @Query("SELECT * FROM treasury.bank_accounts WHERE bank_id = :bankId ORDER BY name ASC")
    Flux<BankAccountEntity> findByBankId(UUID bankId);
}
