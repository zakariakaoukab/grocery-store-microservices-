package com.epicerie.common.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LowStockEvent extends BaseEvent {
    private String productId;
    private String productName;
    private Integer availableQuantity;
    private Integer threshold;
}
