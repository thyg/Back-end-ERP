package com.rtcomops.treasury.infrastructure.persistence.adapter;

import com.rtcomops.treasury.domain.model.Check;
import com.rtcomops.treasury.domain.port.out.CheckRepositoryPort;
import com.rtcomops.treasury.infrastructure.persistence.mapper.CheckPersistenceMapper;
import com.rtcomops.treasury.infrastructure.persistence.repository.R2dbcCheckRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Adapter implementing the CheckRepositoryPort using Spring Data R2DBC.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Component
public class CheckRepositoryAdapter implements CheckRepositoryPort {

    private final R2dbcCheckRepository repository;
    private final CheckPersistenceMapper mapper;

    public CheckRepositoryAdapter(R2dbcCheckRepository repository,
                                  CheckPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<Check> findById(UUID id) {
        return repository.findById(id)
            .map(mapper::toDomain);
    }

    @Override
    public Flux<Check> findAllById(List<UUID> ids) {
        return repository.findAllById(ids)
            .map(mapper::toDomain);
    }

    @Override
    public Flux<Check> findAllOrderByDateDesc() {
        return repository.findAllOrderByDateDesc()
            .map(mapper::toDomain);
    }

    @Override
    public Flux<Check> findByBankAccountId(UUID accountId) {
        return repository.findByBankAccountId(accountId)
            .map(mapper::toDomain);
    }

    @Override
    public Flux<Check> findByCheckType(String checkType) {
        return repository.findByCheckType(checkType)
            .map(mapper::toDomain);
    }

    @Override
    public Flux<Check> findByStatus(String status) {
        return repository.findByStatus(status)
            .map(mapper::toDomain);
    }

    @Override
    public Flux<Check> findByCheckTypeAndStatus(String checkType, String status) {
        return repository.findByCheckTypeAndStatus(checkType, status)
            .map(mapper::toDomain);
    }

    @Override
    public Flux<Check> findByBankAccountIdAndCheckType(UUID accountId, String checkType) {
        return repository.findByBankAccountIdAndCheckType(accountId, checkType)
            .map(mapper::toDomain);
    }

    @Override
    public Flux<Check> findPendingChecksDueBefore(LocalDate date) {
        return repository.findPendingChecksDueBefore(date)
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsByCheckNumberAndAccountAndType(String checkNumber, UUID accountId, String checkType) {
        return repository.existsByCheckNumberAndAccountAndType(checkNumber, accountId, checkType);
    }

    @Override
    public Mono<Boolean> existsByCheckNumberAndAccountAndTypeAndIdNot(String checkNumber, UUID accountId, String checkType, UUID id) {
        return repository.existsByCheckNumberAndAccountAndTypeAndIdNot(checkNumber, accountId, checkType, id);
    }

    @Override
    public Flux<Check> findByCheckbookId(UUID checkbookId) {
        return repository.findByCheckbookId(checkbookId)
            .map(mapper::toDomain);
    }

    @Override
    public Flux<Check> findByCheckDepositId(UUID checkDepositId) {
        return repository.findByCheckDepositId(checkDepositId)
            .map(mapper::toDomain);
    }

    @Override
    public Flux<Check> findOverdueChecks() {
        return repository.findOverdueChecks()
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Check> save(Check check) {
        return repository.existsById(check.getId())
            .flatMap(exists -> {
                if (exists) {
                    return repository.save(mapper.toEntityForUpdate(check));
                } else {
                    return repository.save(mapper.toEntity(check));
                }
            })
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Void> delete(Check check) {
        return repository.deleteById(check.getId());
    }

    // Statistics methods
    @Override
    public Mono<Integer> countByStatus(String status) {
        return repository.countByStatus(status);
    }

    @Override
    public Mono<BigDecimal> sumAmountByStatus(String status) {
        return repository.sumAmountByStatus(status);
    }

    @Override
    public Mono<Integer> countByCheckType(String checkType) {
        return repository.countByCheckType(checkType);
    }

    @Override
    public Mono<BigDecimal> sumAmountByCheckType(String checkType) {
        return repository.sumAmountByCheckType(checkType);
    }

    @Override
    public Mono<Integer> countAllExcludingCancelled() {
        return repository.countAllExcludingCancelled();
    }

    @Override
    public Mono<BigDecimal> sumTotalAmountExcludingCancelled() {
        return repository.sumTotalAmountExcludingCancelled();
    }

    @Override
    public Mono<Integer> countOverdueChecks() {
        return repository.countOverdueChecks();
    }

    @Override
    public Mono<BigDecimal> sumOverdueAmount() {
        return repository.sumOverdueAmount();
    }
}
