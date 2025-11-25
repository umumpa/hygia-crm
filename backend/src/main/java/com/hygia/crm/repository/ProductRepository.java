package com.hygia.crm.repository;

import com.hygia.crm.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByItemCode(String itemCode);
    boolean existsByItemCode(String itemCode);
}

