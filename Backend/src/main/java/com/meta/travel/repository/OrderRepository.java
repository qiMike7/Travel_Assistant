package com.meta.travel.repository;

import com.meta.travel.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserIdOrderByCreateTimeDesc(Long userId);

    Optional<Order> findByIdAndUserId(Long id, Long userId);
}
