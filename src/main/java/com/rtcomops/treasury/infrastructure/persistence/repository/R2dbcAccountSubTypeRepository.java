package com.rtcomops.treasury.infrastructure.persistence.repository;

import com.rtcomops.treasury.infrastructure.persistence.entity.AccountSubTypeEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * R2DBC repository for AccountSubTypeEntity persistence operations.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
@Repository
public interface R2dbcAccountSubTypeRepository extends R2dbcRepository<AccountSubTypeEntity, UUID> {

    @Query("SELECT * FROM treasury.account_sub_types WHERE account_type_id = :accountTypeId ORDER BY ordre_affichage ASC, code ASC")
    Flux<AccountSubTypeEntity> findByAccountTypeId(UUID accountTypeId);

    @Query("SELECT * FROM treasury.account_sub_types WHERE account_type_id = :accountTypeId AND is_active = true ORDER BY ordre_affichage ASC, code ASC")
    Flux<AccountSubTypeEntity> findByAccountTypeIdAndIsActiveTrue(UUID accountTypeId);

    @Query("SELECT * FROM treasury.account_sub_types WHERE account_type_id = :accountTypeId AND code = :code")
    Mono<AccountSubTypeEntity> findByAccountTypeIdAndCode(UUID accountTypeId, String code);

    @Query("SELECT COUNT(*) > 0 FROM treasury.account_sub_types WHERE account_type_id = :accountTypeId AND code = :code")
    Mono<Boolean> existsByAccountTypeIdAndCode(UUID accountTypeId, String code);

    @Query("SELECT COUNT(*) > 0 FROM treasury.account_sub_types WHERE account_type_id = :accountTypeId AND code = :code AND id != :id")
    Mono<Boolean> existsByAccountTypeIdAndCodeAndIdNot(UUID accountTypeId, String code, UUID id);

    @Query("SELECT * FROM treasury.account_sub_types WHERE is_active = true ORDER BY ordre_affichage ASC, code ASC")
    Flux<AccountSubTypeEntity> findAllActiveOrderByOrdre();

    @Query("SELECT * FROM treasury.account_sub_types ORDER BY ordre_affichage ASC, code ASC")
    Flux<AccountSubTypeEntity> findAllOrderByOrdre();

    @Query("SELECT COUNT(*) FROM treasury.account_sub_types WHERE account_type_id = :accountTypeId")
    Mono<Long> countByAccountTypeId(UUID accountTypeId);

    @Query("DELETE FROM treasury.account_sub_types WHERE account_type_id = :accountTypeId")
    Mono<Void> deleteByAccountTypeId(UUID accountTypeId);
}
