package com.meta.travel.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 大模型（LLM）代理配置
 */
@Data
@ConfigurationProperties(prefix = "travel.llm")
public class LlmProperties {

    /** OpenAI 兼容接口地址（默认 DeepSeek） */
    private String baseUrl = "https://api.deepseek.com/v1";

    /** API Key */
    private String apiKey;

    /** 默认模型 */
    private String model = "deepseek-chat";

    /** 读/写超时（秒），大模型响应较慢，建议 60 以上 */
    private int timeout = 60;

    /** 下发给模型的历史消息条数上限，防止上下文无限增长 */
    private int historyLimit = 20;

    /** 会员客服配置（默认同为 DeepSeek，可指向其它厂商） */
    private Kefu kefu = new Kefu();

    @Data
    public static class Kefu {
        private String baseUrl = "https://api.deepseek.com/v1";
        private String apiKey;
        private String model = "deepseek-chat";
    }
}
