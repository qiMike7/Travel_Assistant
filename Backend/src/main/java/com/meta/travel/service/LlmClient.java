package com.meta.travel.service;

import com.meta.travel.common.BusinessException;
import com.meta.travel.common.ResultCode;
import com.meta.travel.config.LlmProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * 大模型对话客户端，封装 OpenAI 兼容的 /chat/completions 接口。
 * <p>API Key 仅保存在服务端配置中，避免前端直连暴露密钥。</p>
 */
@Slf4j
@Component
public class LlmClient {

    private final RestClient restClient;
    private final LlmProperties llmProperties;

    public LlmClient(RestClient.Builder builder, LlmProperties llmProperties) {
        this.llmProperties = llmProperties;
        // 必须设超时：否则大模型卡住时会长期占用 Tomcat 工作线程
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(Math.max(llmProperties.getTimeout(), 1)));
        this.restClient = builder.requestFactory((ClientHttpRequestFactory) factory).build();
    }

    /**
     * 当前 type 实际生效的模型名（供会话记录展示）。
     */
    public String modelFor(String type) {
        return resolve(type).model();
    }

    /**
     * 发送对话请求，返回助手回复文本。
     *
     * @param type     assistant=智慧问答，kefu=会员客服（默认均为 DeepSeek，可分开配置）
     * @param messages 对话消息列表，元素为 {role, content}
     */
    public String chat(String type, List<Map<String, String>> messages) {
        Config config = resolve(type);
        if (config.apiKey() == null || config.apiKey().isBlank()) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR,
                    "未配置大模型 API Key，请设置环境变量 LLM_API_KEY（DeepSeek：https://platform.deepseek.com）");
        }

        Map<String, Object> body = Map.of(
                "model", config.model(),
                "messages", messages,
                "stream", false
        );

        try {
            Map<?, ?> response = restClient.post()
                    .uri(config.baseUrl() + "/chat/completions")
                    .header("Authorization", "Bearer " + config.apiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);
            return extractContent(response);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用大模型失败: {}", e.getMessage(), e);
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "大模型服务暂时不可用，请稍后重试");
        }
    }

    /**
     * 解析 type 对应的服务配置：客服优先用自有配置，未配 Key 时回退到主模型。
     */
    private Config resolve(String type) {
        if ("kefu".equalsIgnoreCase(type)) {
            LlmProperties.Kefu kefu = llmProperties.getKefu();
            if (kefu.getApiKey() != null && !kefu.getApiKey().isBlank()) {
                return new Config(kefu.getBaseUrl(), kefu.getApiKey(), kefu.getModel());
            }
        }
        return new Config(llmProperties.getBaseUrl(), llmProperties.getApiKey(), llmProperties.getModel());
    }

    /** 一次调用需要的接口地址 / 密钥 / 模型 */
    private record Config(String baseUrl, String apiKey, String model) {
    }

    @SuppressWarnings("unchecked")
    private String extractContent(Map<?, ?> response) {
        if (response == null) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "大模型返回为空");
        }
        List<?> choices = (List<?>) response.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "大模型未返回有效结果");
        }
        Map<String, Object> message = (Map<String, Object>) ((Map<String, Object>) choices.get(0)).get("message");
        Object content = message == null ? null : message.get("content");
        return content == null ? "" : content.toString();
    }
}
