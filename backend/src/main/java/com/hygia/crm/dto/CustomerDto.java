package com.hygia.crm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {
    private Long id;
    private String nameStd;
    private Boolean isProspect;
    private CustomerRegionDto region;
    private String addressText;
    private String phone;
    private String email;
    private String paymentTerms;
    private String tier;
}

