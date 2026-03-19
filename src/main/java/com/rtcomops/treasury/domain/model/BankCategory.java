package com.rtcomops.treasury.domain.model;

import java.util.UUID;

/**
 * Domain model representing a bank category.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
public class BankCategory {

    private UUID id;
    private String code;
    private String label;

    public BankCategory() {
    }

    public BankCategory(UUID id, String code, String label) {
        this.id = id;
        this.code = code;
        this.label = label;
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

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String code;
        private String label;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder label(String label) {
            this.label = label;
            return this;
        }

        public BankCategory build() {
            return new BankCategory(id, code, label);
        }
    }
}
