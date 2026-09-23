package com.meta.travel.repository;

import com.meta.travel.entity.TravelPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TravelPlanRepository extends JpaRepository<TravelPlan, Long> {

    List<TravelPlan> findByUserIdOrderByIdDesc(Long userId);

    long countByUserId(Long userId);

    Optional<TravelPlan> findByIdAndUserId(Long id, Long userId);
}
