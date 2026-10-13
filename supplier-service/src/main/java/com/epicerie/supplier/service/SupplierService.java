package com.epicerie.supplier.service;

import com.epicerie.common.events.LowStockEvent;
import com.epicerie.common.events.StockSuppliedEvent;
import com.epicerie.supplier.config.RabbitMQConfig;
import com.epicerie.supplier.domain.Supplier;
import com.epicerie.supplier.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final RabbitTemplate rabbitTemplate;

    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    public Supplier getSupplierById(String id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found: " + id));
    }

    public Supplier createSupplier(Supplier supplier) {
        return supplierRepository.save(supplier);
    }

    public Double getAverageRating() {
        return supplierRepository.findAll().stream()
                .mapToDouble(s -> s.getRating() != null ? s.getRating() : 0.0)
                .average()
                .orElse(0.0);
    }

    public StockSuppliedEvent supplyStock(String supplierId, String productId, Integer quantity, BigDecimal unitCost) {
        Supplier supplier = getSupplierById(supplierId);
        String batchNumber = "BATCH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        StockSuppliedEvent event = new StockSuppliedEvent(
                supplier.getId(),
                productId,
                quantity,
                unitCost != null ? unitCost : BigDecimal.ZERO,
                batchNumber
        );

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.STOCK_SUPPLIED_ROUTING_KEY, event);
        log.info("StockSuppliedEvent published by supplier '{}' ({}) for product: {}, quantity: {}, batch: {}",
                supplier.getName(), supplier.getId(), productId, quantity, batchNumber);

        return event;
    }

    @RabbitListener(queues = RabbitMQConfig.STOCK_LOW_QUEUE)
    public void handleLowStockAlert(LowStockEvent event) {
        log.warn("ALERT: Product '{}' ({}) has low stock: {} remaining (threshold: {}). Replenishment required!",
                event.getProductName(), event.getProductId(), event.getAvailableQuantity(), event.getThreshold());
    }
}
