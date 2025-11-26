package com.hygia.crm.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "customer", uniqueConstraints = {
    @UniqueConstraint(columnNames = "name_std")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name_std", nullable = false, unique = true)
    private String nameStd;

    @Column(name = "is_prospect", nullable = false)
    private Boolean isProspect = false;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @Column(name = "address_text")
    private String addressText;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "payment_terms")
    private String paymentTerms;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "tier", nullable = false)
    private String tier = "Potential";

    /**
     * Automatically set isProspect based on tier before persist/update.
     * tier == "Potential" → isProspect = true
     * Otherwise → isProspect = false
     */
    @PrePersist
    @PreUpdate
    private void updateIsProspectFromTier() {
        if (tier != null && "Potential".equalsIgnoreCase(tier)) {
            this.isProspect = true;
        } else {
            this.isProspect = false;
        }
    }
}

