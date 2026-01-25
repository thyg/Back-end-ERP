package com.rtcomops.treasury.repository;

import com.rtcomops.treasury.entity.AccountConnectorField;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface AccountConnectorFieldRepository extends R2dbcRepository<AccountConnectorField, UUID> {
    @Query("SELECT * FROM treasury.account_connector_fields WHERE connector_type_id = :connectorTypeId ORDER BY display_order ASC")
    Flux<AccountConnectorField> findByConnectorTypeId(UUID connectorTypeId);
}