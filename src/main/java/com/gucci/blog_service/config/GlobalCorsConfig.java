package com.gucci.blog_service.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class GlobalCorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry
                // CORS를 허용할 경로 패턴. /**는 모든 엔드포인트를 의미
                .addMapping("/**")
                // 허용할 Origin 목록: React 개발 서버가 실행되는 주소
                .allowedOrigins("http://localhost:5173")
                // 허용할 HTTP 메서드 (필요한 경우에 맞춰 추가/삭제)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 허용할 헤더 (Authorization, Content-Type 등)
                .allowedHeaders("Authorization", "Content-Type", "Accept")
                // 필요 시 클라이언트 측으로 해당 헤더를 노출
                .exposedHeaders("Authorization")
                // 브라우저가 인증 쿠키 등을 보내려면 true
                .allowCredentials(true)
                // preflight 요청을 캐싱할 시간(초)
                .maxAge(3600);
    }
}