package com.hygia.crm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDto {
    private Long id;
    private String invoiceNumber;
    private Long customerId;
    private String customerName;
    private LocalDate invoiceDate;
    private BigDecimal totalAmount;
    private String note;
    private List<InvoiceItemDto> items;
}

