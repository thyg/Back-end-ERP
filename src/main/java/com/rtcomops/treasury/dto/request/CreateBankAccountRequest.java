package com.rtcomops.treasury.dto.request;

import com.rtcomops.treasury.validation.ValidIban;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for creating a new bank account.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBankAccountRequest {

    @NotNull(message = "Bank ID is required")
    private UUID bankId;

    @NotBlank(message = "Account name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Account number is required")
    @Size(min = 5, max = 50, message = "Account number must be between 5 and 50 characters")
    private String accountNumber;

    @Size(max = 34, message = "L'IBAN ne doit pas dépasser 34 caractères")
    @ValidIban
    private String iban;

    @Size(max = 11, message = "BIC must not exceed 11 characters")
    @Pattern(regexp = "^$|^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$", message = "Invalid BIC format")
    private String bic;

    @Pattern(regexp = "^(EUR|USD|XAF|XOF|GBP|CHF|CAD|JPY)$", 
             message = "Currency must be EUR, USD, XAF, XOF, GBP, CHF, CAD, or JPY")
    private String currency;

    private BigDecimal initialBalance;

    private Boolean isActive;
}
