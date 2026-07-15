package com.dobak.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 해커톤 개발 단계용 CORS 전체 허용 설정 + 업로드 파일 정적 서빙.
 * FE 개발 서버(localhost:3000 등)에서 자유롭게 호출할 수 있도록 열어둠.
 * 배포 전에는 allowedOriginPatterns를 실제 도메인으로 좁힐 것.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /** FileStorageService와 동일한 값 — 저장 위치와 서빙 위치가 항상 같이 가도록 같은 프로퍼티를 씀 */
    @Value("${file.storage.base-path}")
    private String fileStorageBasePath;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }

    /**
     * FileStorageService.save()가 돌려주는 "/files/{subDir}/{fileName}" URL을 실제로 서빙한다.
     * 이게 없으면 캡처 이미지/발음 녹음 파일이 디스크에는 있어도 HTTP로 못 꺼내온다.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = fileStorageBasePath.endsWith("/") ? fileStorageBasePath : fileStorageBasePath + "/";
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + location);
    }
}
