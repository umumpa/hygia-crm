package com.hygia.crm.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "product", uniqueConstraints = {
    @UniqueConstraint(columnNames = "item_code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_code", nullable = false, unique = true)
    private String itemCode;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "default_unit_price", precision = 19, scale = 2)
    private BigDecimal defaultUnitPrice;

    @Column(name = "company_tag")
    private String companyTag;

    @Column(name = "product_type")
    private String productType;

    @Column(name = "barcode")
    private String barcode;

    @Column(name = "active", nullable = false)
    private Boolean active = true;
}

