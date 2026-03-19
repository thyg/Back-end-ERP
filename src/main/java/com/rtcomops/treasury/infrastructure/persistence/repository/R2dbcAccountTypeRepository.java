package com.rtcomops.treasury.infrastructure.persistence.repository;

import com.rtcomops.treasury.infrastructure.persistence.entity.AccountTypeEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * R2DBC repository for AccountTypeEntity persistence operations.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
@Repository
public interface R2dbcAccountTypeRepository extends R2dbcRepository<AccountTypeEntity, UUID> {

    Mono<AccountTypeEntity> findByCode(String code);

    Mono<Boolean> existsByCode(String code);

    @Query("SELECT * FROM treasury.account_types WHERE is_active = true ORDER BY ordre_affichage ASC, code ASC")
    Flux<AccountTypeEntity> findAllActiveOrderByOrdre();

    @Query("SELECT * FROM treasury.account_types ORDER BY ordre_affichage ASC, code ASC")
    Flux<AccountTypeEntity> findAllOrderByOrdre();

    @Query("SELECT * FROM treasury.account_types WHERE peut_emettre_cheques = true AND is_active = true ORDER BY ordre_affichage ASC")
    Flux<AccountTypeEntity> findByPeutEmettreChequesTrue();

    @Query("SELECT * FROM treasury.account_types WHERE peut_recevoir_cheques = true AND is_active = true ORDER BY ordre_affichage ASC")
    Flux<AccountTypeEntity> findByPeutRecevoirChequesTrue();

    @Query("SELECT * FROM treasury.account_types WHERE peut_transactions_especes = true AND is_active = true ORDER BY ordre_affichage ASC")
    Flux<AccountTypeEntity> findByPeutTransactionsEspecesTrue();

    @Query("SELECT * FROM treasury.account_types WHERE decouvert_autorise = true AND is_active = true ORDER BY ordre_affichage ASC")
    Flux<AccountTypeEntity> findByDecouvertAutoriseTrue();

    @Query("SELECT COUNT(*) > 0 FROM treasury.account_types WHERE code = :code AND id != :id")
    Mono<Boolean> existsByCodeAndIdNot(String code, UUID id);
}
