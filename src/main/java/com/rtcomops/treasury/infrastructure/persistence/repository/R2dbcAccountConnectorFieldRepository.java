package com.rtcomops.treasury.infrastructure.persistence.repository;

import com.rtcomops.treasury.infrastructure.persistence.entity.AccountConnectorFieldEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

/**
 * R2DBC repository for AccountConnectorFieldEntity persistence operations.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
@Repository
public interface R2dbcAccountConnectorFieldRepository extends R2dbcRepository<AccountConnectorFieldEntity, UUID> {

    @Query("SELECT * FROM treasury.account_connector_fields WHERE connector_type_id = :connectorTypeId ORDER BY display_order ASC")
    Flux<AccountConnectorFieldEntity> findByConnectorTypeId(UUID connectorTypeId);
}
