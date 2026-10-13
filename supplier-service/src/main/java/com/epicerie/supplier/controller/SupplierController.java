package com.epicerie.supplier.controller;

import com.epicerie.common.events.StockSuppliedEvent;
import com.epicerie.supplier.domain.Supplier;
import com.epicerie.supplier.dto.SupplyStockRequest;
import com.epicerie.supplier.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    public ResponseEntity<List<Supplier>> getAllSuppliers() {
        return ResponseEntity.ok(supplierService.getAllSuppliers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Supplier> getSupplierById(@PathVariable String id) {
        return ResponseEntity.ok(supplierService.getSupplierById(id));
    }

    @PostMapping
    public ResponseEntity<Supplier> createSupplier(@RequestBody Supplier supplier) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierService.createSupplier(supplier));
    }

    @GetMapping("/stats/average-rating")
    public ResponseEntity<Map<String, Double>> getAverageRating() {
        return ResponseEntity.ok(Map.of("averageRating", supplierService.getAverageRating()));
    }

    @PostMapping("/{id}/supply")
    public ResponseEntity<StockSuppliedEvent> supplyStock(
            @PathVariable String id,
            @RequestBody SupplyStockRequest request) {
        StockSuppliedEvent event = supplierService.supplyStock(
                id,
                request.getProductId(),
                request.getQuantity(),
                request.getUnitCost()
        );
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(event);
    }
}
