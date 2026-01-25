package com.rtcomops.treasury.entity;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Table(schema = "treasury", name = "bank_categories")
public class BankCategory {
    @Id private UUID id;
    private String code;
    private String label;
}