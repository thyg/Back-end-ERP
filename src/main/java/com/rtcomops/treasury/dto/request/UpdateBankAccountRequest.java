package com.rtcomops.treasury.dto.request;

import com.rtcomops.treasury.validation.ValidIban;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for updating an existing bank account.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBankAccountRequest {

    private UUID bankId;

    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Size(min = 5, max = 50, message = "Account number must be between 5 and 50 characters")
    private String accountNumber;

    @Size(max = 34, message = "L'IBAN ne doit pas dépasser 34 caractères")
    @ValidIban
    private String iban;

    @Size(max = 11, message = "BIC must not exceed 11 characters")
    @Pattern(regexp = "^$|^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$", message = "Invalid BIC format")
    private String bic;

    private Boolean isActive;
}
