package com.meta.travel.repository;

import com.meta.travel.entity.MapRoute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MapRouteRepository extends JpaRepository<MapRoute, Long> {

    List<MapRoute> findAllByOrderByIdAsc();
}
