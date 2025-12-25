package com.rtcomops.treasury.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for creating a new checkbook.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCheckbookRequest {

    /**
     * The bank account ID this checkbook belongs to.
     */
    @NotNull(message = "Le compte bancaire est obligatoire")
    private UUID bankAccountId;

    /**
     * RIB associated with the checkbook.
     */
    @NotBlank(message = "Le RIB est obligatoire")
    @Size(max = 50, message = "Le RIB ne doit pas dépasser 50 caractères")
    private String rib;

    /**
     * Common prefix/root for check numbers.
     */
    @NotBlank(message = "La racine/préfixe est obligatoire")
    @Size(max = 20, message = "La racine ne doit pas dépasser 20 caractères")
    private String prefix;

    /**
     * First check number in the range.
     */
    @NotNull(message = "Le numéro de début est obligatoire")
    @Min(value = 1, message = "Le numéro de début doit être positif")
    private Integer startNumber;

    /**
     * Last check number in the range.
     */
    @NotNull(message = "Le numéro de fin est obligatoire")
    @Min(value = 1, message = "Le numéro de fin doit être positif")
    private Integer endNumber;
}
