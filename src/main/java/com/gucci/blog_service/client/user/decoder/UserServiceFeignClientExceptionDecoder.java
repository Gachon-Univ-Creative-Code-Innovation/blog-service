package com.gucci.blog_service.client.user.decoder;

import com.gucci.common.exception.CustomException;
import com.gucci.common.exception.ErrorCode;
import feign.Response;
import feign.codec.ErrorDecoder;

public class UserServiceFeignClientExceptionDecoder implements ErrorDecoder {
	@Override
	public Exception decode(String methodKey, Response response) {
		if (response.status() >= 400 && response.status() <= 499) {
			System.out.println("error "+response);
			return switch (response.status()) {
				case 400 -> new CustomException(ErrorCode.FAIL);
				default -> new CustomException(ErrorCode.FAIL);
			};
		} else {
			System.out.println("error "+response);
			System.out.println("error "+response.reason());
			System.out.println("error "+response.request());

			return new CustomException(ErrorCode.FAIL);
		}
	}
}
