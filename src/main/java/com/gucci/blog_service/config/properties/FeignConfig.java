package com.gucci.blog_service.config.properties;


import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.gucci.blog_service.client")
public class FeignConfig {
}
