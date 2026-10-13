package com.epicerie.order.service;

import com.epicerie.common.events.InventoryReservationFailedEvent;
import com.epicerie.common.events.InventoryReservedEvent;
import com.epicerie.common.events.OrderCancelledEvent;
import com.epicerie.common.events.OrderCreatedEvent;
import com.epicerie.order.config.RabbitMQConfig;
import com.epicerie.order.domain.Order;
import com.epicerie.order.domain.OrderItem;
import com.epicerie.order.domain.OrderStatus;
import com.epicerie.order.dto.CreateOrderRequest;
import com.epicerie.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        log.info("Creating order for customer: {}", request.getCustomerId());

        BigDecimal totalAmount = request.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
                .customerId(request.getCustomerId())
                .status(OrderStatus.PENDING)
                .totalAmount(totalAmount)
                .createdAt(LocalDateTime.now())
                .build();

        List<OrderItem> items = request.getItems().stream()
                .map(dto -> OrderItem.builder()
                        .order(order)
                        .productId(dto.getProductId())
                        .quantity(dto.getQuantity())
                        .unitPrice(dto.getUnitPrice())
                        .build())
                .collect(Collectors.toList());

        order.setItems(items);
        Order savedOrder = orderRepository.save(order);

        OrderCreatedEvent event = new OrderCreatedEvent(
                savedOrder.getId(),
                savedOrder.getCustomerId(),
                savedOrder.getItems().stream()
                        .map(item -> new OrderCreatedEvent.OrderItemEvent(item.getProductId(), item.getQuantity(), item.getUnitPrice()))
                        .collect(Collectors.toList()),
                savedOrder.getTotalAmount()
        );

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ORDER_CREATED_ROUTING_KEY, event);
        log.info("OrderCreatedEvent published for order: {}", savedOrder.getId());

        return savedOrder;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(String id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
    }

    public List<Order> getOrdersByCustomerId(String customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    @Transactional
    public Order cancelOrder(String id, String reason) {
        Order order = getOrderById(id);
        if (order.getStatus() == OrderStatus.CANCELLED) {
            return order;
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.save(order);

        List<OrderCreatedEvent.OrderItemEvent> itemEvents = order.getItems() != null
                ? order.getItems().stream()
                .map(item -> new OrderCreatedEvent.OrderItemEvent(item.getProductId(), item.getQuantity(), item.getUnitPrice()))
                .collect(Collectors.toList())
                : List.of();

        OrderCancelledEvent event = new OrderCancelledEvent(
                savedOrder.getId(),
                reason != null ? reason : "Order cancelled by customer",
                itemEvents
        );

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ORDER_CANCELLED_ROUTING_KEY, event);
        log.info("OrderCancelledEvent published for order: {}", savedOrder.getId());

        return savedOrder;
    }

    @RabbitListener(queues = RabbitMQConfig.INVENTORY_RESERVED_QUEUE)
    @Transactional
    public void handleInventoryReserved(InventoryReservedEvent event) {
        log.info("Received InventoryReservedEvent for order: {}", event.getOrderId());
        orderRepository.findById(event.getOrderId()).ifPresent(order -> {
            order.setStatus(OrderStatus.VALIDATED);
            orderRepository.save(order);
            log.info("Order {} status updated to VALIDATED", event.getOrderId());
        });
    }

    @RabbitListener(queues = RabbitMQConfig.INVENTORY_FAILED_QUEUE)
    @Transactional
    public void handleInventoryFailed(InventoryReservationFailedEvent event) {
        log.warn("Received InventoryFailedEvent for order: {}. Reason: {}", event.getOrderId(), event.getReason());
        orderRepository.findById(event.getOrderId()).ifPresent(order -> {
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            log.info("Order {} status updated to CANCELLED", event.getOrderId());
        });
    }
}
