package com.gucci.blog_service.client.matching.config;

import com.gucci.blog_service.client.matching.decoder.MatchingServiceFeignErrorDecoder;
import feign.Logger;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

public class MatchingServiceFeignConfig {
    @Bean
    public ErrorDecoder errorDecoder() {
        return new MatchingServiceFeignErrorDecoder();
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
}
