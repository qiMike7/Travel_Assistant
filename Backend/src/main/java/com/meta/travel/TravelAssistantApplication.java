package com.meta.travel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Meta-Tourism 智趣AI旅行助手 后端启动类
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class TravelAssistantApplication {

    public static void main(String[] args) {
        SpringApplication.run(TravelAssistantApplication.class, args);
    }
}
