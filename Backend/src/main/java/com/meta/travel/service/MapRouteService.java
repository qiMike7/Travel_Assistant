package com.meta.travel.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meta.travel.common.BusinessException;
import com.meta.travel.common.ResultCode;
import com.meta.travel.dto.response.MapRouteVO;
import com.meta.travel.entity.MapRoute;
import com.meta.travel.repository.MapRouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * 地图路线服务（对应 map-test.vue / shopping.vue 的 polyline）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MapRouteService {

    private final MapRouteRepository mapRouteRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<MapRouteVO> list() {
        return mapRouteRepository.findAllByOrderByIdAsc().stream().map(this::toVO).toList();
    }

    @Transactional(readOnly = true)
    public MapRouteVO getById(Long id) {
        MapRoute route = mapRouteRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "路线不存在"));
        return toVO(route);
    }

    private MapRouteVO toVO(MapRoute route) {
        MapRouteVO vo = new MapRouteVO();
        vo.setId(route.getId());
        vo.setName(route.getName());
        vo.setDescription(route.getDescription());
        vo.setWidth(route.getWidth());
        vo.setColor(route.getColor());
        vo.setArrowLine(route.getArrowLine());
        vo.setPoints(parsePoints(route.getPoints()));
        return vo;
    }

    private List<MapRouteVO.Point> parsePoints(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<MapRouteVO.Point>>() {
            });
        } catch (Exception e) {
            log.error("解析路线轨迹点失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
