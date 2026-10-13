package com.epicerie.order.repository;

import com.epicerie.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByCustomerId(String customerId);
}
