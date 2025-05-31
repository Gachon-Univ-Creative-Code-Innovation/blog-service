package com.gucci.blog_service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/blog-service")
@RequiredArgsConstructor
public class HealthCheckController {
    private final Environment environment;
    @RequestMapping("/health-check")
    public String healthCheck() {
        return "Blog Service is running on port : "
                + environment.getProperty("local.server.port");
    }
}
