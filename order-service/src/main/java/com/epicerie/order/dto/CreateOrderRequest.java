package com.epicerie.order.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateOrderRequest {
    private String customerId;
    private List<OrderItemDto> items;

    @Data
    public static class OrderItemDto {
        private String productId;
        private Integer quantity;
        private BigDecimal unitPrice;
    }
}
