package com.rtcomops.treasury.dto.response;

import com.rtcomops.treasury.entity.AccountConnectorField;
import com.rtcomops.treasury.entity.AccountConnectorType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountConnectorResponse {
    private AccountConnectorType connectorType;
    private List<AccountConnectorField> fields;
}