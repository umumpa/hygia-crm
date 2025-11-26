package com.hygia.crm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateDto {

    @NotBlank(message = "Item code is required")
    private String itemCode;

    private String description;

    private BigDecimal defaultUnitPrice;

    private String companyTag;

    private String productType;

    private String barcode;
}

