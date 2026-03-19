package com.rtcomops.treasury.application.port.in;

import com.rtcomops.treasury.application.dto.request.AutoReconcileRequest;
import com.rtcomops.treasury.application.dto.request.ReconcileManualRequest;
import com.rtcomops.treasury.application.dto.response.ReconciliationMatchResponse;
import com.rtcomops.treasury.application.dto.response.ReconciliationSummaryResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Input port for Reconciliation use cases.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
public interface ReconciliationUseCase {

    Mono<ReconciliationMatchResponse> reconcileManual(ReconcileManualRequest request);

    Mono<Void> unmatch(UUID matchId);

    Flux<ReconciliationMatchResponse> reconcileAuto(AutoReconcileRequest request);

    Mono<ReconciliationSummaryResponse> getSummary(UUID bankStatementId);

    Flux<ReconciliationMatchResponse> findMatchesByLineId(UUID statementLineId);

    Mono<Void> reconcileCheckDeposit(UUID checkDepositId, UUID statementLineId);
}
