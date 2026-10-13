package com.epicerie.supplier.repository;

import com.epicerie.supplier.domain.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<Supplier, String> {
}
