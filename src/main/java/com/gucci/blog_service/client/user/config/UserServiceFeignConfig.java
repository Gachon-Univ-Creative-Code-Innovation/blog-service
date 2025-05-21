package com.gucci.blog_service.client.user.config;

import com.gucci.blog_service.client.user.decoder.UserServiceFeignClientExceptionDecoder;
import feign.Logger;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

public class UserServiceFeignConfig {
    @Bean
    ErrorDecoder errorDecoder() {
        return new UserServiceFeignClientExceptionDecoder();
    } //예외처리 커스터마이징

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
}