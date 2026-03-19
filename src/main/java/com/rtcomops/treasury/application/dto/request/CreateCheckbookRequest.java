package com.rtcomops.treasury.application.dto.request;

import jakarta.validation.constraints.Max;
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
     * IBAN associated with the checkbook (optional - will be fetched from bank account if not provided).
     */
    @Size(max = 50, message = "L'IBAN ne doit pas depasser 50 caracteres")
    private String iban;

    /**
     * Common prefix/root for check numbers.
     */
    @NotBlank(message = "La racine/prefixe est obligatoire")
    @Size(max = 20, message = "La racine ne doit pas depasser 20 caracteres")
    private String prefix;

    /**
     * First check number in the range.
     */
    @NotNull(message = "Le numero de debut est obligatoire")
    @Min(value = 1, message = "Le numero de debut doit etre positif")
    private Integer startNumber;

    /**
     * Number of pages (checks) in the checkbook.
     */
    @NotNull(message = "Le nombre de feuilles est obligatoire")
    @Min(value = 1, message = "Minimum 1 feuille")
    @Max(value = 500, message = "Maximum 500 feuilles")
    private Integer numberOfPages;

    /**
     * Calculates endNumber from startNumber and numberOfPages.
     *
     * @return the calculated end number
     */
    public Integer calculateEndNumber() {
        if (startNumber != null && numberOfPages != null) {
            return startNumber + numberOfPages - 1;
        }
        return null;
    }
}
