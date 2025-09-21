package com.samnammae.chatbot_service.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.samnammae.chatbot_service.client")
public class FeignClientConfig {
}
