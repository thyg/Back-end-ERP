package com.rtcomops.treasury.application.dto.response;

import com.rtcomops.treasury.domain.model.AccountConnectorField;
import com.rtcomops.treasury.domain.model.AccountConnectorType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for account connector type with its fields.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountConnectorResponse {
    private AccountConnectorType connectorType;
    private List<AccountConnectorField> fields;
}
