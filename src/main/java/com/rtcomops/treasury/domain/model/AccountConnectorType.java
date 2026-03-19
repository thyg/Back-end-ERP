package com.rtcomops.treasury.domain.model;

import java.util.UUID;

/**
 * Domain model representing an account connector type.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
public class AccountConnectorType {

    private UUID id;
    private String code;
    private String name;
    private UUID bankCategoryId;

    public AccountConnectorType() {
    }

    public AccountConnectorType(UUID id, String code, String name, UUID bankCategoryId) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.bankCategoryId = bankCategoryId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getBankCategoryId() {
        return bankCategoryId;
    }

    public void setBankCategoryId(UUID bankCategoryId) {
        this.bankCategoryId = bankCategoryId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String code;
        private String name;
        private UUID bankCategoryId;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder bankCategoryId(UUID bankCategoryId) {
            this.bankCategoryId = bankCategoryId;
            return this;
        }

        public AccountConnectorType build() {
            return new AccountConnectorType(id, code, name, bankCategoryId);
        }
    }
}
