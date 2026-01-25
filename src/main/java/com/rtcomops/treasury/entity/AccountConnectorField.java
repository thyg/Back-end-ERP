package com.rtcomops.treasury.entity;
// imports


import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;





@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Table(schema = "treasury", name = "account_connector_fields")
public class AccountConnectorField {
    @Id private UUID id;
    @Column("connector_type_id") private UUID connectorTypeId;
    @Column("field_key") private String fieldKey;
    private String label;
    @Column("field_type") private String fieldType;
    @Column("is_required") private Boolean isRequired;
    @Column("display_order") private Integer displayOrder;
}