package com.epicerie.common.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderCreatedEvent extends BaseEvent {
    private String orderId;
    private String customerId;
    private List<OrderItemEvent> items;
    private BigDecimal totalAmount;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemEvent {
        private String productId;
        private Integer quantity;
        private BigDecimal unitPrice;
    }
}
