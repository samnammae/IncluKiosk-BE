package com.samnammae.admin_service.config;

import com.samnammae.admin_service.service.StoreService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestConfig {
    @Bean
    public StoreService storeService() {
        return Mockito.mock(StoreService.class);
    }
}