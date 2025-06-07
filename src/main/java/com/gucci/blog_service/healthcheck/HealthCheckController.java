package com.gucci.blog_service.healthcheck;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/blog-service")
@RequiredArgsConstructor
public class HealthCheckController {
    private final Environment environment;

    @GetMapping("/health-check")
    public String healthCheck() {
        return "Blog Service is up and running on port: " + environment.getProperty("local.server.port");
    }

    @GetMapping("/test")
    public String test() {
        return "Blog Service Test Endpoint is working!!!";
    }
}
