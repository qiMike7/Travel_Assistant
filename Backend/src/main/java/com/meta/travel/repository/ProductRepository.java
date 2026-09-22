package com.meta.travel.repository;

import com.meta.travel.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByStatusOrderByIdAsc(Integer status);

    List<Product> findByCategoryAndStatusOrderByIdAsc(String category, Integer status);

    /** 下单时行锁读商品，避免并发超卖（库存 read-modify-write 必须串行化） */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findForUpdate(@Param("id") Long id);
}
