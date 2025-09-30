package com.samnammae.chatbot_service.client;

import com.samnammae.chatbot_service.config.GeminiClientConfig;
import com.samnammae.chatbot_service.dto.request.GeminiRequest;
import com.samnammae.chatbot_service.dto.response.GeminiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "gemini-api",
        url = "https://generativelanguage.googleapis.com",
        configuration = GeminiClientConfig.class)
public interface GeminiClient {

    @PostMapping(value = "/v1/models/gemini-2.5-flash-lite:generateContent",
            headers = {"Content-Type=application/json"})
    GeminiResponse call(@RequestBody GeminiRequest request);
}