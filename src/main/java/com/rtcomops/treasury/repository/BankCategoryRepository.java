package com.rtcomops.treasury.repository;

import com.rtcomops.treasury.entity.BankCategory;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface BankCategoryRepository extends R2dbcRepository<BankCategory, UUID> {
    Mono<BankCategory> findByCode(String code);
}