package com.hygia.crm.repository;

import com.hygia.crm.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByCustomerIdOrderByInvoiceDateDesc(Long customerId);
}

