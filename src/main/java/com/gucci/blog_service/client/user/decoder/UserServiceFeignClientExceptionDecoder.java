package com.gucci.blog_service.client.user.decoder;

import com.gucci.common.exception.CustomException;
import com.gucci.common.exception.ErrorCode;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class UserServiceFeignClientExceptionDecoder implements ErrorDecoder {
	private final Logger logger = LoggerFactory.getLogger(UserServiceFeignClientExceptionDecoder.class);

	@Override
	public Exception decode(String methodKey, Response response) {
		if (response.status() >= 400 && response.status() <= 499) {
			logger.debug("error "+response);
			return switch (response.status()) {
				case 400 -> new CustomException(ErrorCode.FAIL);
				default -> new CustomException(ErrorCode.FAIL);
			};
		} else {
			logger.debug("error response"+response);
			logger.debug("errorr esponse.reason() "+response.reason());
			logger.debug("error response.request()"+response.request());

			return new CustomException(ErrorCode.FAIL);
		}
	}
}
