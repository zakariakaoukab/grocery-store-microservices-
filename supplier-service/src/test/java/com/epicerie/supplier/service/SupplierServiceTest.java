package com.epicerie.supplier.service;

import com.epicerie.common.events.StockSuppliedEvent;
import com.epicerie.supplier.domain.Supplier;
import com.epicerie.supplier.repository.SupplierRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private SupplierService supplierService;

    @Test
    void supplyStock_shouldPublishStockSuppliedEvent() {
        Supplier supplier = Supplier.builder()
                .id("sup-1")
                .name("Green Valley")
                .rating(4.8)
                .build();

        when(supplierRepository.findById("sup-1")).thenReturn(Optional.of(supplier));

        StockSuppliedEvent event = supplierService.supplyStock("sup-1", "prod-1", 50, BigDecimal.valueOf(1.50));

        assertNotNull(event);
        assertEquals("sup-1", event.getSupplierId());
        assertEquals("prod-1", event.getProductId());
        assertEquals(50, event.getQuantity());
        verify(rabbitTemplate).convertAndSend(eq("epicerie.main.topic"), eq("supplier.stock.supplied"), any(StockSuppliedEvent.class));
    }

    @Test
    void getAverageRating_shouldComputeMeanRating() {
        when(supplierRepository.findAll()).thenReturn(List.of(
                Supplier.builder().rating(4.5).build(),
                Supplier.builder().rating(5.0).build()
        ));

        Double avg = supplierService.getAverageRating();
        assertEquals(4.75, avg);
    }
}
