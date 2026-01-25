package com.rtcomops.treasury.repository;

import com.rtcomops.treasury.entity.AccountConnectorType;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface AccountConnectorTypeRepository extends R2dbcRepository<AccountConnectorType, UUID> {
    Flux<AccountConnectorType> findByBankCategoryId(UUID bankCategoryId);
}