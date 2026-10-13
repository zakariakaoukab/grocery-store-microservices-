package com.epicerie.common.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StockSuppliedEvent extends BaseEvent {
    private String supplierId;
    private String productId;
    private Integer quantity;
    private BigDecimal unitCost;
    private String batchNumber;
}
