package com.rtcomops.treasury.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankCategoryRequest {

    @NotBlank(message = "Le code est requis.")
    @Size(min = 2, max = 50, message = "Le code doit contenir entre 2 et 50 caractères.")
    private String code;

    @NotBlank(message = "Le libellé est requis.")
    @Size(min = 2, max = 100, message = "Le libellé doit contenir entre 2 et 100 caractères.")
    private String label;
}
