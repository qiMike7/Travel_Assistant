package com.meta.travel.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meta.travel.common.BusinessException;
import com.meta.travel.common.ResultCode;
import com.meta.travel.dto.request.ItineraryRequest;
import com.meta.travel.dto.response.ItinerarySummaryVO;
import com.meta.travel.dto.response.ItineraryVO;
import com.meta.travel.entity.Preference;
import com.meta.travel.entity.TravelPlan;
import com.meta.travel.repository.TravelPlanRepository;
import com.meta.travel.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 快速智慧旅行攻略规划服务（对应 pages/shopping/shopping.vue）
 * <p>根据目的地与行程偏好，通过 {@link LlmClient} 让大模型产出结构化 JSON 攻略，
 * 再解析为 {@link ItineraryVO} 供前端渲染分日行程与地图轨迹。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ItineraryService {

    private static final String SYSTEM_PROMPT = """
            你是一名专业的中文旅行规划师。请根据用户给出的目的地与偏好，制定一份可落地的分日旅行攻略。
            必须只输出一个 JSON 对象，不要包含任何解释性文字、注释或 Markdown 代码块围栏。
            JSON 结构严格如下：
            {
              "summary": "整体行程概览与出行贴士，80字以内",
              "totalBudget": 人均预估总花费(整数, 单位元),
              "plan": [
                {
                  "day": 第几天(整数, 从1开始),
                  "title": "当日主题, 例如: 西湖漫游 · 灵隐祈福",
                  "spots": [
                    {
                      "time": "09:00-11:30",
                      "name": "景点/场所名称",
                      "category": "景点|美食|住宿|交通|购物 之一",
                      "description": "玩什么、怎么去、亮点, 60字以内",
                      "tip": "实用贴士, 30字以内",
                      "latitude": 纬度(数字, 无法确定时填 null),
                      "longitude": 经度(数字, 无法确定时填 null)
                    }
                  ]
                }
              ]
            }
            要求：
            1. plan 的天数必须与用户给定的天数一致，每天安排 3-5 个 spots，覆盖早中晚。
            2. latitude/longitude 仅在你较有把握给出该地点真实坐标时填写，否则填 null，禁止编造。
            3. 内容要具体、贴合目的地，避免空泛套话。
            """;

    private final LlmClient llmClient;
    private final PreferenceService preferenceService;
    private final ObjectMapper objectMapper;
    private final TravelPlanRepository travelPlanRepository;
    private final UserService userService;

    /** 攻略保存上限：非会员 1 份 / 会员 5 份 */
    private static final int PLAN_LIMIT_NON_VIP = 1;
    private static final int PLAN_LIMIT_VIP = 5;

    /**
     * 生成攻略（仅生成，不保存）。
     * <p>是否存入历史由前端点击「保存到规划历史」调用 {@link #save} 决定，
     * 因此名额已满也能正常生成预览。</p>
     * <p>故意不加事务：大模型调用耗时较长，避免长时间占用数据库连接。</p>
     *
     * @param userId 当前用户（用于读取旅行偏好做个性化兜底）
     */
    public ItineraryVO generate(Long userId, ItineraryRequest request) {
        String userPrompt = buildUserPrompt(userId, request);

        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", SYSTEM_PROMPT),
                Map.of("role", "user", "content", userPrompt)
        );

        String reply = llmClient.chat("assistant", messages);
        ItineraryVO vo = parse(reply);
        // 模型偶尔漏填目的地/天数，用请求值兜底，保证前端展示完整
        vo.setDestination(request.getDestination());
        if (vo.getDays() == null || vo.getDays() <= 0) {
            vo.setDays(request.getDays());
        }
        // 仅生成不保存：id 置空，前端据此展示「保存到规划历史」按钮
        vo.setId(null);
        return vo;
    }

    /**
     * 显式保存一份攻略到规划历史。
     * <p>名额在此校验（非会员 1 份 / 会员 5 份），已满抛 4293 由前端引导删除历史或开通会员。</p>
     */
    @Transactional
    public ItineraryVO save(Long userId, ItineraryVO vo) {
        if (vo == null || !StringUtils.hasText(vo.getDestination())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "攻略内容为空，无法保存");
        }
        requireSaveQuota(userId);
        TravelPlan saved = persist(userId, vo);
        vo.setId(saved.getId());
        return vo;
    }

    /** 校验当前用户是否还有保存名额。 */
    private void requireSaveQuota(Long userId) {
        int limit = saveLimit(userId);
        long used = travelPlanRepository.countByUserId(userId);
        if (used >= limit) {
            throw new BusinessException(ResultCode.PLAN_LIMIT_REACHED,
                    "最多可保存 " + limit + " 份攻略，请删除旧攻略释放名额" + (limit == PLAN_LIMIT_NON_VIP ? "或开通会员" : ""));
        }
    }

    private int saveLimit(Long userId) {
        return userService.isVipActive(userService.getById(userId)) ? PLAN_LIMIT_VIP : PLAN_LIMIT_NON_VIP;
    }

    private TravelPlan persist(Long userId, ItineraryVO vo) {
        TravelPlan plan = new TravelPlan();
        plan.setUserId(userId);
        plan.setDestination(vo.getDestination());
        plan.setDays(vo.getDays() == null ? 0 : vo.getDays());
        plan.setSummary(truncate(vo.getSummary(), 500));
        plan.setTotalBudget(vo.getTotalBudget());
        try {
            plan.setContent(objectMapper.writeValueAsString(vo));
        } catch (Exception e) {
            log.error("序列化攻略失败: {}", e.getMessage());
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "攻略保存失败，请重试");
        }
        return travelPlanRepository.save(plan);
    }

    /** 我的攻略历史列表（摘要，最新在前）。 */
    @Transactional(readOnly = true)
    public List<ItinerarySummaryVO> list(Long userId) {
        return travelPlanRepository.findByUserIdOrderByIdDesc(userId).stream().map(p ->
                new ItinerarySummaryVO(p.getId(), p.getDestination(), p.getDays(), p.getSummary(),
                        DateUtil.format(p.getCreateTime()))
        ).toList();
    }

    /** 单份攻略详情（完整 ItineraryVO，含 id）。 */
    @Transactional(readOnly = true)
    public ItineraryVO detail(Long userId, Long id) {
        TravelPlan plan = travelPlanRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "攻略不存在"));
        try {
            ItineraryVO vo = objectMapper.readValue(plan.getContent(), ItineraryVO.class);
            vo.setId(plan.getId());
            return vo;
        } catch (Exception e) {
            log.error("解析攻略详情失败: id={} {}", id, e.getMessage());
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "攻略读取失败");
        }
    }

    /** 删除一份攻略，释放一个保存名额。 */
    @Transactional
    public void delete(Long userId, Long id) {
        TravelPlan plan = travelPlanRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "攻略不存在"));
        travelPlanRepository.delete(plan);
    }

    private String truncate(String text, int max) {
        if (text == null) {
            return null;
        }
        return text.length() <= max ? text : text.substring(0, max);
    }

    private String buildUserPrompt(Long userId, ItineraryRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("目的地：").append(request.getDestination()).append("；\n");
        sb.append("行程天数：").append(request.getDays()).append(" 天；\n");
        sb.append("出行人数：").append(request.getTravelers() == null ? 2 : request.getTravelers()).append(" 人；\n");

        Integer budget = request.getBudget();
        String style = request.getStyle();
        // 缺省项用用户旅行偏好补全
        if (userId != null && (budget == null || !StringUtils.hasText(style))) {
            Preference pref = preferenceService.get(userId);
            if (pref != null) {
                if (budget == null && pref.getBudget() != null) {
                    budget = pref.getBudget();
                }
                if (!StringUtils.hasText(style) && StringUtils.hasText(pref.getTravelMode())) {
                    style = pref.getTravelMode();
                }
            }
        }
        if (budget != null) {
            sb.append("每日人均预算：约 ").append(budget).append(" 元；\n");
        }
        if (StringUtils.hasText(style)) {
            sb.append("出行风格/兴趣：").append(style).append("；\n");
        }
        if (StringUtils.hasText(request.getNote())) {
            sb.append("特殊要求：").append(request.getNote()).append("；\n");
        }
        return sb.toString();
    }

    /**
     * 从模型回复中抽取 JSON 并解析。模型可能包裹 ```json 围栏或附带前后文字，需容错。
     */
    private ItineraryVO parse(String reply) {
        String json = extractJson(reply);
        if (json == null) {
            log.error("攻略生成失败，模型返回无法解析为 JSON: {}", abbreviate(reply));
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "攻略生成失败，请调整目的地或偏好后重试");
        }
        try {
            JsonNode root = objectMapper.readTree(json);
            return toVO(root);
        } catch (Exception e) {
            log.error("解析攻略 JSON 失败: {} | raw={}", e.getMessage(), abbreviate(json));
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "攻略解析失败，请稍后重试");
        }
    }

    private ItineraryVO toVO(JsonNode root) {
        ItineraryVO vo = new ItineraryVO();
        vo.setSummary(text(root, "summary"));
        vo.setTotalBudget(intOrNull(root, "totalBudget"));

        List<ItineraryVO.PlanDay> days = new ArrayList<>();
        JsonNode plan = root.path("plan");
        if (plan.isArray()) {
            int index = 1;
            for (JsonNode dayNode : plan) {
                ItineraryVO.PlanDay day = new ItineraryVO.PlanDay();
                Integer dayNo = intOrNull(dayNode, "day");
                day.setDay(dayNo != null ? dayNo : index);
                day.setTitle(text(dayNode, "title"));

                List<ItineraryVO.PlanSpot> spots = new ArrayList<>();
                JsonNode spotsNode = dayNode.path("spots");
                if (spotsNode.isArray()) {
                    for (JsonNode s : spotsNode) {
                        ItineraryVO.PlanSpot spot = new ItineraryVO.PlanSpot();
                        spot.setTime(text(s, "time"));
                        spot.setName(text(s, "name"));
                        spot.setCategory(text(s, "category"));
                        spot.setDescription(text(s, "description"));
                        spot.setTip(text(s, "tip"));
                        spot.setLatitude(doubleOrNull(s, "latitude"));
                        spot.setLongitude(doubleOrNull(s, "longitude"));
                        spots.add(spot);
                    }
                }
                day.setSpots(spots);
                days.add(day);
                index++;
            }
        }
        vo.setPlan(days);
        return vo;
    }

    /** 截取首个 '{' 到末个 '}' 之间的 JSON 主体。 */
    private String extractJson(String reply) {
        if (!StringUtils.hasText(reply)) {
            return null;
        }
        int start = reply.indexOf('{');
        int end = reply.lastIndexOf('}');
        if (start < 0 || end <= start) {
            return null;
        }
        return reply.substring(start, end + 1);
    }

    private String text(JsonNode node, String field) {
        JsonNode v = node.path(field);
        return v.isMissingNode() || v.isNull() ? null : v.asText();
    }

    private Integer intOrNull(JsonNode node, String field) {
        JsonNode v = node.path(field);
        return v.isNumber() ? v.asInt() : null;
    }

    private Double doubleOrNull(JsonNode node, String field) {
        JsonNode v = node.path(field);
        if (v.isNumber()) {
            double d = v.asDouble();
            // 过滤 (0,0) 这类明显无效的坐标
            if (d == 0.0) {
                return null;
            }
            return d;
        }
        return null;
    }

    private String abbreviate(String s) {
        if (s == null) {
            return null;
        }
        return s.length() <= 500 ? s : s.substring(0, 500) + "...";
    }
}
