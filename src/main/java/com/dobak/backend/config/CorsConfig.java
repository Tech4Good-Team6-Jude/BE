package com.dobak.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 해커톤 개발 단계용 CORS 전체 허용 설정.
 * FE 개발 서버(localhost:3000 등)에서 자유롭게 호출할 수 있도록 열어둠.
 * 배포 전에는 allowedOriginPatterns를 실제 도메인으로 좁힐 것.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
