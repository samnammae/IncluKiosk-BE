package com.samnammae.order_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 모든 API 경로에 대해
                .allowedOrigins("http://localhost:8000",
                        "http://inclukiosk-fe.s3-website.ap-northeast-2.amazonaws.com",
                        "http://localhost:5173",
                        "http://localhost:3000",
                        "http://localhost:4200",
                        "http://127.0.0.1:3000",
                        "http://dap3hz5p3144z.cloudfront.net",
                        "https://dap3hz5p3144z.cloudfront.net",
                        "http://ec2-3-36-56-69.ap-northeast-2.compute.amazonaws.com:3000",
                        "http://inclukiosk.com",
                        "https://inclukiosk.com",
                        "http://www.inclukiosk.com",
                        "https://www.inclukiosk.com",
                        "http://43.201.112.13:3000",
                        "http://ec2-43-201-112-13.ap-northeast-2.compute.amazonaws.com",
                        " http://ec2-43-201-112-13.ap-northeast-2.compute.amazonaws.com:3000",
                        "https://dxt8mqr07jxb9.cloudfront.net",
                        "https://dxt8mqr07jxb9.cloudfront.net:3000"
                ) // 허용할 프론트 주소
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}