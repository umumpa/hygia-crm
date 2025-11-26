package com.hygia.crm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private Long id;
    private String itemCode;
    private String description;
    private BigDecimal defaultUnitPrice;
    private String companyTag;
    private String productType;
    private String barcode;
    private Boolean active;
}

