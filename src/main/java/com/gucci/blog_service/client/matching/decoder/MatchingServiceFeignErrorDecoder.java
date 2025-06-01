package com.gucci.blog_service.client.matching.decoder;

import com.gucci.common.exception.CustomException;
import com.gucci.common.exception.ErrorCode;
import feign.Response;
import feign.codec.ErrorDecoder;

public class MatchingServiceFeignErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response) {
        System.out.println("error "+response);
        System.out.println("error "+response.reason());
        System.out.println("error "+response.request());

        // 4xx / 5xx 모두 CustomException으로 래핑
        if (response.status() >= 400 && response.status() < 600) {
            return new CustomException(ErrorCode.FAIL);
        }
        return new RuntimeException("Unknown error: " + response.status());
    }
}
