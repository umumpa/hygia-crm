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
public class InvoiceItemDto {
    private Long id;
    private Long productId;
    private String itemCode;
    private String description;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal amount;
}

