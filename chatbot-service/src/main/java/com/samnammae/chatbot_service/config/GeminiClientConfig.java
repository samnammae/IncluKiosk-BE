package com.samnammae.chatbot_service.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class GeminiClientConfig {

    @Value("${GEMINI_API_KEY}")
    private String apiKey;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> requestTemplate.query("key", apiKey);
    }
}