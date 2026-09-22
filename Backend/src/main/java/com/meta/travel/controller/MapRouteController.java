package com.meta.travel.controller;

import com.meta.travel.common.Result;
import com.meta.travel.dto.response.MapRouteVO;
import com.meta.travel.security.PublicApi;
import com.meta.travel.service.MapRouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 地图路线接口（对应 map-test.vue / shopping.vue 的 polyline）
 */
@PublicApi
@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class MapRouteController {

    private final MapRouteService mapRouteService;

    @GetMapping
    public Result<List<MapRouteVO>> list() {
        return Result.success(mapRouteService.list());
    }

    @GetMapping("/{id}")
    public Result<MapRouteVO> detail(@PathVariable Long id) {
        return Result.success(mapRouteService.getById(id));
    }
}
