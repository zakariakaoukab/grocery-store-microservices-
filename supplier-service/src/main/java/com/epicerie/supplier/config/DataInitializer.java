package com.epicerie.supplier.config;

import com.epicerie.supplier.domain.Supplier;
import com.epicerie.supplier.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final SupplierRepository supplierRepository;

    @Override
    public void run(String... args) {
        if (supplierRepository.count() == 0) {
            log.info("Seeding initial suppliers...");

            List<Supplier> suppliers = List.of(
                    Supplier.builder()
                            .name("Green Valley Organic Farms")
                            .contactEmail("contact@greenvalley.com")
                            .contactPhone("+1-555-0101")
                            .rating(4.8)
                            .build(),
                    Supplier.builder()
                            .name("Heritage Dairy Co.")
                            .contactEmail("orders@heritagedairy.com")
                            .contactPhone("+1-555-0102")
                            .rating(4.9)
                            .build(),
                    Supplier.builder()
                            .name("Golden Grain Bakery Supplies")
                            .contactEmail("supply@goldengrain.com")
                            .contactPhone("+1-555-0103")
                            .rating(4.6)
                            .build(),
                    Supplier.builder()
                            .name("Mediterranean Olive Imports")
                            .contactEmail("info@mediterranean-olive.com")
                            .contactPhone("+1-555-0104")
                            .rating(4.7)
                            .build()
            );

            supplierRepository.saveAll(suppliers);
            log.info("Finished seeding initial suppliers.");
        }
    }
}
