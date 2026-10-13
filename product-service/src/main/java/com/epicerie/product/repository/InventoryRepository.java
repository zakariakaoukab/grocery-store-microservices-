package com.epicerie.product.repository;

import com.epicerie.product.domain.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, String> {
    Optional<Inventory> findByProductId(String productId);
}
