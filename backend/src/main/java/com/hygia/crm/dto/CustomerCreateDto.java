package com.hygia.crm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerCreateDto {

    @NotBlank(message = "Name is required")
    private String nameStd;

    @Schema(
        description = "DEPRECATED: This field is ignored. isProspect is automatically calculated from tier (tier='Potential' → isProspect=true, otherwise false)",
        deprecated = true,
        hidden = true
    )
    @Deprecated
    private Boolean isProspect;

    @NotNull(message = "Region ID is required")
    private Long regionId;

    private String tier;

    private String addressText;

    private String phone;

    private String email;

    private String paymentTerms;

    private String notes;
}

