package com.epicerie.product.config;

import com.epicerie.product.dto.ProductRequest;
import com.epicerie.product.repository.ProductRepository;
import com.epicerie.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final ProductService productService;

    @Override
    public void run(String... args) {
        if (productRepository.count() == 0) {
            log.info("Seeding initial grocery products...");

            List<ProductRequest> seedProducts = List.of(
                    ProductRequest.builder()
                            .name("Organic Whole Milk 1L")
                            .description("Fresh organic whole milk from local dairy farms")
                            .sku("MILK-ORG-1L")
                            .price(new BigDecimal("2.49"))
                            .initialStock(50)
                            .build(),
                    ProductRequest.builder()
                            .name("Artisan Sourdough Bread")
                            .description("Freshly baked rustic sourdough loaf")
                            .sku("BREAD-SOURDOUGH")
                            .price(new BigDecimal("3.99"))
                            .initialStock(30)
                            .build(),
                    ProductRequest.builder()
                            .name("Free Range Eggs (12-pack)")
                            .description("Grade A large brown free-range eggs")
                            .sku("EGGS-FR-12")
                            .price(new BigDecimal("4.29"))
                            .initialStock(40)
                            .build(),
                    ProductRequest.builder()
                            .name("Arabica Whole Bean Coffee 500g")
                            .description("Medium roast 100% Arabica premium coffee beans")
                            .sku("COFFEE-ARABICA-500G")
                            .price(new BigDecimal("8.99"))
                            .initialStock(25)
                            .build(),
                    ProductRequest.builder()
                            .name("Extra Virgin Olive Oil 750ml")
                            .description("Cold-pressed extra virgin Mediterranean olive oil")
                            .sku("OIL-EVOO-750ML")
                            .price(new BigDecimal("9.49"))
                            .initialStock(20)
                            .build(),
                    ProductRequest.builder()
                            .name("Italian Penne Rigate 500g")
                            .description("Bronze-cut durum wheat semolina pasta")
                            .sku("PASTA-PENNE-500G")
                            .price(new BigDecimal("1.79"))
                            .initialStock(60)
                            .build(),
                    ProductRequest.builder()
                            .name("Sharp Cheddar Cheese 250g")
                            .description("Aged sharp white cheddar cheese block")
                            .sku("CHEESE-CHEDDAR-250G")
                            .price(new BigDecimal("4.50"))
                            .initialStock(35)
                            .build()
            );

            for (ProductRequest pr : seedProducts) {
                try {
                    productService.createProduct(pr);
                } catch (Exception e) {
                    log.warn("Could not seed product {}: {}", pr.getName(), e.getMessage());
                }
            }

            log.info("Finished seeding initial grocery products.");
        }
    }
}
