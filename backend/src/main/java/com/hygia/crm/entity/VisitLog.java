package com.hygia.crm.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "visit_log", indexes = {
    @Index(name = "idx_customer_visit_at", columnList = "customer_id, visit_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VisitLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "visit_at", nullable = false)
    private OffsetDateTime visitAt;

    @Column(name = "type", length = 20)
    private String type;

    @Column(name = "result", length = 20)
    private String result;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "next_follow_up_at")
    private OffsetDateTime nextFollowUpAt;
}

