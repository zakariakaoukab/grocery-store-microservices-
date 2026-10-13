package com.epicerie.product.service;

import com.epicerie.product.domain.Inventory;
import com.epicerie.product.domain.Product;
import com.epicerie.product.dto.ProductRequest;
import com.epicerie.product.dto.ProductResponse;
import com.epicerie.product.repository.InventoryRepository;
import com.epicerie.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        if (productRepository.findBySku(request.getSku()).isPresent()) {
            throw new IllegalArgumentException("Product with SKU already exists: " + request.getSku());
        }

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .sku(request.getSku())
                .price(request.getPrice())
                .build();

        int initialStock = request.getInitialStock() != null ? request.getInitialStock() : 0;
        Inventory inventory = Inventory.builder()
                .product(product)
                .availableQuantity(initialStock)
                .reservedQuantity(0)
                .build();

        product.setInventory(inventory);
        Product saved = productRepository.save(product);

        log.info("Created product {} with initial stock {}", saved.getId(), initialStock);
        return toResponse(saved);
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ProductResponse getProductById(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
        return toResponse(product);
    }

    public ProductResponse getProductBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with SKU: " + sku));
        return toResponse(product);
    }

    @Transactional
    public ProductResponse updateProduct(String id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setSku(request.getSku());

        Product saved = productRepository.save(product);
        return toResponse(saved);
    }

    @Transactional
    public ProductResponse updateStock(String id, int quantityDelta) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));

        Inventory inventory = inventoryRepository.findByProductId(id)
                .orElseGet(() -> Inventory.builder()
                        .product(product)
                        .availableQuantity(0)
                        .reservedQuantity(0)
                        .build());

        inventory.setAvailableQuantity(Math.max(0, inventory.getAvailableQuantity() + quantityDelta));
        inventoryRepository.save(inventory);

        return toResponse(product);
    }

    @Transactional
    public void deleteProduct(String id) {
        productRepository.deleteById(id);
    }

    private ProductResponse toResponse(Product product) {
        Integer available = 0;
        Integer reserved = 0;
        if (product.getInventory() != null) {
            available = product.getInventory().getAvailableQuantity();
            reserved = product.getInventory().getReservedQuantity();
        }

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .sku(product.getSku())
                .price(product.getPrice())
                .availableStock(available)
                .reservedStock(reserved)
                .build();
    }
}
