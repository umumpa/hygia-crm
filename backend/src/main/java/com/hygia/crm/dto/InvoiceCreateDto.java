package com.hygia.crm.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceCreateDto {

    @NotBlank(message = "Invoice number is required")
    private String invoiceNumber;

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Invoice date is required")
    private LocalDate invoiceDate;

    private String note;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<InvoiceItemCreateDto> items;
}

