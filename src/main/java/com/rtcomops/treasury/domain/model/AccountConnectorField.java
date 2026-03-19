package com.rtcomops.treasury.domain.model;

import java.util.UUID;

/**
 * Domain model representing an account connector field.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
public class AccountConnectorField {

    private UUID id;
    private UUID connectorTypeId;
    private String fieldKey;
    private String label;
    private String fieldType;
    private Boolean isRequired;
    private Integer displayOrder;

    public AccountConnectorField() {
    }

    public AccountConnectorField(UUID id, UUID connectorTypeId, String fieldKey, String label,
                                  String fieldType, Boolean isRequired, Integer displayOrder) {
        this.id = id;
        this.connectorTypeId = connectorTypeId;
        this.fieldKey = fieldKey;
        this.label = label;
        this.fieldType = fieldType;
        this.isRequired = isRequired;
        this.displayOrder = displayOrder;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getConnectorTypeId() {
        return connectorTypeId;
    }

    public void setConnectorTypeId(UUID connectorTypeId) {
        this.connectorTypeId = connectorTypeId;
    }

    public String getFieldKey() {
        return fieldKey;
    }

    public void setFieldKey(String fieldKey) {
        this.fieldKey = fieldKey;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public Boolean getIsRequired() {
        return isRequired;
    }

    public void setIsRequired(Boolean isRequired) {
        this.isRequired = isRequired;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID connectorTypeId;
        private String fieldKey;
        private String label;
        private String fieldType;
        private Boolean isRequired;
        private Integer displayOrder;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder connectorTypeId(UUID connectorTypeId) {
            this.connectorTypeId = connectorTypeId;
            return this;
        }

        public Builder fieldKey(String fieldKey) {
            this.fieldKey = fieldKey;
            return this;
        }

        public Builder label(String label) {
            this.label = label;
            return this;
        }

        public Builder fieldType(String fieldType) {
            this.fieldType = fieldType;
            return this;
        }

        public Builder isRequired(Boolean isRequired) {
            this.isRequired = isRequired;
            return this;
        }

        public Builder displayOrder(Integer displayOrder) {
            this.displayOrder = displayOrder;
            return this;
        }

        public AccountConnectorField build() {
            return new AccountConnectorField(id, connectorTypeId, fieldKey, label, fieldType, isRequired, displayOrder);
        }
    }
}
