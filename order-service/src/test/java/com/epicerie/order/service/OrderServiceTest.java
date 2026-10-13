package com.epicerie.order.service;

import com.epicerie.common.events.InventoryReservationFailedEvent;
import com.epicerie.common.events.InventoryReservedEvent;
import com.epicerie.order.domain.Order;
import com.epicerie.order.domain.OrderStatus;
import com.epicerie.order.dto.CreateOrderRequest;
import com.epicerie.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private OrderService orderService;

    private CreateOrderRequest createOrderRequest;

    @BeforeEach
    void setUp() {
        createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setCustomerId("cust-123");
        
        CreateOrderRequest.OrderItemDto item = new CreateOrderRequest.OrderItemDto();
        item.setProductId("prod-1");
        item.setQuantity(2);
        item.setUnitPrice(BigDecimal.valueOf(10.0));
        
        createOrderRequest.setItems(Collections.singletonList(item));
    }

    @Test
    void createOrder_shouldSaveOrderAndPublishEvent() {
        // Given
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId("order-uuid");
            return order;
        });

        // When
        Order result = orderService.createOrder(createOrderRequest);

        // Then
        assertNotNull(result);
        assertEquals("cust-123", result.getCustomerId());
        assertEquals(OrderStatus.PENDING, result.getStatus());
        assertEquals(BigDecimal.valueOf(20.0), result.getTotalAmount());
        
        verify(orderRepository).save(any(Order.class));
        verify(rabbitTemplate).convertAndSend(eq("epicerie.main.topic"), eq("order.order.created"), any(Object.class));
    }

    @Test
    void handleInventoryReserved_shouldUpdateStatusToValidated() {
        Order order = Order.builder().id("order-1").status(OrderStatus.PENDING).build();
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));

        orderService.handleInventoryReserved(new InventoryReservedEvent("order-1"));

        assertEquals(OrderStatus.VALIDATED, order.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void handleInventoryFailed_shouldUpdateStatusToCancelled() {
        Order order = Order.builder().id("order-1").status(OrderStatus.PENDING).build();
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));

        orderService.handleInventoryFailed(new InventoryReservationFailedEvent("order-1", "Out of stock"));

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(orderRepository).save(order);
    }
}
