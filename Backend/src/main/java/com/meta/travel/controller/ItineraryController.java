package com.meta.travel.controller;

import com.meta.travel.common.Result;
import com.meta.travel.dto.request.ItineraryRequest;
import com.meta.travel.dto.response.ItinerarySummaryVO;
import com.meta.travel.dto.response.ItineraryVO;
import com.meta.travel.security.UserContext;
import com.meta.travel.service.ItineraryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 快速智慧旅行攻略规划接口（对应 pages/shopping/shopping.vue 与 pages/plan-history/plan-history.vue）
 * <p>大模型 API Key 保存在服务端。生成与保存拆为两步：/api/itinerary 仅生成不入库，
 * /api/itinerary/save 才将攻略存入历史并校验保存名额。</p>
 */
@RestController
@RequestMapping("/api/itinerary")
@RequiredArgsConstructor
public class ItineraryController {

    private final ItineraryService itineraryService;

    /** 生成攻略（仅生成不保存，名额已满也可正常预览） */
    @PostMapping
    public Result<ItineraryVO> generate(@Valid @RequestBody ItineraryRequest request) {
        ItineraryVO vo = itineraryService.generate(UserContext.currentUserId(), request);
        return Result.success("攻略生成成功", vo);
    }

    /** 保存攻略到规划历史（名额校验在此步，已满返回 4293） */
    @PostMapping("/save")
    public Result<ItineraryVO> save(@RequestBody ItineraryVO vo) {
        ItineraryVO saved = itineraryService.save(UserContext.currentUserId(), vo);
        return Result.success("已保存到规划历史", saved);
    }

    /** 我的攻略历史列表（摘要） */
    @GetMapping
    public Result<List<ItinerarySummaryVO>> list() {
        return Result.success(itineraryService.list(UserContext.currentUserId()));
    }

    /** 单份攻略详情 */
    @GetMapping("/{id}")
    public Result<ItineraryVO> detail(@PathVariable Long id) {
        return Result.success(itineraryService.detail(UserContext.currentUserId(), id));
    }

    /** 删除一份攻略（释放保存名额） */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        itineraryService.delete(UserContext.currentUserId(), id);
        return Result.success("攻略已删除", null);
    }
}
