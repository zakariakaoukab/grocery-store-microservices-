package com.epicerie.product.service;

import com.epicerie.product.domain.Inventory;
import com.epicerie.product.domain.Product;
import com.epicerie.product.dto.ProductRequest;
import com.epicerie.product.dto.ProductResponse;
import com.epicerie.product.repository.InventoryRepository;
import com.epicerie.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void createProduct_shouldSaveProductAndInventory() {
        ProductRequest request = ProductRequest.builder()
                .name("Organic Milk")
                .description("Fresh organic milk")
                .sku("MILK-001")
                .price(BigDecimal.valueOf(2.99))
                .initialStock(20)
                .build();

        when(productRepository.findBySku("MILK-001")).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            p.setId("prod-123");
            return p;
        });

        ProductResponse response = productService.createProduct(request);

        assertNotNull(response);
        assertEquals("prod-123", response.getId());
        assertEquals("Organic Milk", response.getName());
        assertEquals(20, response.getAvailableStock());
        assertEquals(0, response.getReservedStock());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void updateStock_shouldAdjustAvailableStock() {
        Product product = Product.builder().id("prod-1").name("Milk").sku("MILK-1").price(BigDecimal.TEN).build();
        Inventory inventory = Inventory.builder().id("inv-1").product(product).availableQuantity(10).reservedQuantity(2).build();
        product.setInventory(inventory);

        when(productRepository.findById("prod-1")).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProductId("prod-1")).thenReturn(Optional.of(inventory));

        ProductResponse response = productService.updateStock("prod-1", 15);

        assertEquals(25, response.getAvailableStock());
        verify(inventoryRepository).save(inventory);
    }
}
