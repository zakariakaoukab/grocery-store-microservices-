package com.epicerie.product.service;

import com.epicerie.common.events.OrderCreatedEvent;
import com.epicerie.product.domain.Inventory;
import com.epicerie.product.domain.Product;
import com.epicerie.product.repository.InventoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void handleOrderCreated_shouldReserveStockAndPublishSuccess() {
        // Given
        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setOrderId("order-1");
        OrderCreatedEvent.OrderItemEvent item = new OrderCreatedEvent.OrderItemEvent("prod-1", 5, BigDecimal.TEN);
        event.setItems(Collections.singletonList(item));

        Product product = Product.builder().id("prod-1").name("Test Product").build();
        Inventory inventory = Inventory.builder()
                .product(product)
                .availableQuantity(10)
                .reservedQuantity(0)
                .build();

        when(inventoryRepository.findByProductId("prod-1")).thenReturn(Optional.of(inventory));

        // When
        inventoryService.handleOrderCreated(event);

        // Then
        verify(inventoryRepository).save(inventory);
        verify(rabbitTemplate).convertAndSend(eq("epicerie.main.topic"), eq("inventory.reserved"), any(Object.class));
    }

    @Test
    void handleOrderCreated_shouldPublishFailureWhenInsufficientStock() {
        // Given
        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setOrderId("order-1");
        OrderCreatedEvent.OrderItemEvent item = new OrderCreatedEvent.OrderItemEvent("prod-1", 15, BigDecimal.TEN);
        event.setItems(Collections.singletonList(item));

        Product product = Product.builder().id("prod-1").name("Test Product").build();
        Inventory inventory = Inventory.builder()
                .product(product)
                .availableQuantity(10)
                .reservedQuantity(0)
                .build();

        when(inventoryRepository.findByProductId("prod-1")).thenReturn(Optional.of(inventory));

        // When
        inventoryService.handleOrderCreated(event);

        // Then
        verify(inventoryRepository, never()).save(any());
        verify(rabbitTemplate).convertAndSend(eq("epicerie.main.topic"), eq("inventory.failed"), any(Object.class));
    }
}
