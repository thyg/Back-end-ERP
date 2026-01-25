package com.rtcomops.treasury.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;


// imports
@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Table(schema = "treasury", name = "account_connector_types")
public class AccountConnectorType {
    @Id private UUID id;
    private String code;
    private String name;
@Column("bank_category_id")
private UUID bankCategoryId;
}