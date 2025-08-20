package com.samnammae.order_service.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.samnammae.order_service.client")
public class FeignClientConfig {
}
