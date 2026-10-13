package com.epicerie.common.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderCancelledEvent extends BaseEvent {
    private String orderId;
    private String reason;
    private List<OrderCreatedEvent.OrderItemEvent> items;
}
