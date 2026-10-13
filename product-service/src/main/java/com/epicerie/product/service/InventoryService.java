package com.epicerie.product.service;

import com.epicerie.common.events.InventoryReservationFailedEvent;
import com.epicerie.common.events.InventoryReservedEvent;
import com.epicerie.common.events.LowStockEvent;
import com.epicerie.common.events.OrderCancelledEvent;
import com.epicerie.common.events.OrderCreatedEvent;
import com.epicerie.common.events.StockSuppliedEvent;
import com.epicerie.product.config.RabbitMQConfig;
import com.epicerie.product.domain.Inventory;
import com.epicerie.product.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private static final int LOW_STOCK_THRESHOLD = 5;

    private final InventoryRepository inventoryRepository;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMQConfig.ORDER_CREATED_QUEUE)
    @Transactional
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent for order: {}", event.getOrderId());
        
        try {
            for (OrderCreatedEvent.OrderItemEvent item : event.getItems()) {
                Inventory inventory = inventoryRepository.findByProductId(item.getProductId())
                        .orElseThrow(() -> new RuntimeException("Product not found: " + item.getProductId()));

                if (inventory.getAvailableQuantity() < item.getQuantity()) {
                    throw new RuntimeException("Insufficient stock for product: " + item.getProductId());
                }

                inventory.setAvailableQuantity(inventory.getAvailableQuantity() - item.getQuantity());
                inventory.setReservedQuantity(inventory.getReservedQuantity() + item.getQuantity());
                inventoryRepository.save(inventory);

                if (inventory.getAvailableQuantity() <= LOW_STOCK_THRESHOLD) {
                    String prodName = inventory.getProduct() != null ? inventory.getProduct().getName() : item.getProductId();
                    log.warn("Stock is low for product {}: {} remaining", prodName, inventory.getAvailableQuantity());
                    rabbitTemplate.convertAndSend(
                            RabbitMQConfig.EXCHANGE,
                            RabbitMQConfig.STOCK_LOW_ROUTING_KEY,
                            new LowStockEvent(item.getProductId(), prodName, inventory.getAvailableQuantity(), LOW_STOCK_THRESHOLD)
                    );
                }
            }

            log.info("Inventory reserved successfully for order: {}", event.getOrderId());
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.INVENTORY_RESERVED_ROUTING_KEY, new InventoryReservedEvent(event.getOrderId()));
            
        } catch (Exception e) {
            log.error("Failed to reserve inventory for order: {}. Reason: {}", event.getOrderId(), e.getMessage());
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.INVENTORY_FAILED_ROUTING_KEY, new InventoryReservationFailedEvent(event.getOrderId(), e.getMessage()));
        }
    }

    @RabbitListener(queues = RabbitMQConfig.ORDER_CANCELLED_QUEUE)
    @Transactional
    public void handleOrderCancelled(OrderCancelledEvent event) {
        log.info("Received OrderCancelledEvent for order: {}. Restoring stock...", event.getOrderId());
        if (event.getItems() == null) {
            return;
        }

        for (OrderCreatedEvent.OrderItemEvent item : event.getItems()) {
            inventoryRepository.findByProductId(item.getProductId()).ifPresent(inventory -> {
                int quantity = item.getQuantity() != null ? item.getQuantity() : 0;
                inventory.setReservedQuantity(Math.max(0, inventory.getReservedQuantity() - quantity));
                inventory.setAvailableQuantity(inventory.getAvailableQuantity() + quantity);
                inventoryRepository.save(inventory);
                log.info("Restored {} units of product {} to available inventory", quantity, item.getProductId());
            });
        }
    }

    @RabbitListener(queues = RabbitMQConfig.STOCK_SUPPLIED_QUEUE)
    @Transactional
    public void handleStockSupplied(StockSuppliedEvent event) {
        log.info("Received StockSuppliedEvent for product: {}, quantity: {}", event.getProductId(), event.getQuantity());
        inventoryRepository.findByProductId(event.getProductId()).ifPresent(inventory -> {
            inventory.setAvailableQuantity(inventory.getAvailableQuantity() + event.getQuantity());
            inventoryRepository.save(inventory);
            log.info("Stock updated for product {}. New available quantity: {}", event.getProductId(), inventory.getAvailableQuantity());
        });
    }
}
