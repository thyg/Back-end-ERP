package com.rtcomops.treasury.infrastructure.persistence.repository;

import com.rtcomops.treasury.infrastructure.persistence.entity.AccountConnectorTypeEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

/**
 * R2DBC repository for AccountConnectorTypeEntity persistence operations.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
@Repository
public interface R2dbcAccountConnectorTypeRepository extends R2dbcRepository<AccountConnectorTypeEntity, UUID> {

    Flux<AccountConnectorTypeEntity> findByBankCategoryId(UUID bankCategoryId);
}
