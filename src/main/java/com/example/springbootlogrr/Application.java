package com.example.springbootlogrr;

import com.example.springbootlogrr.interceptor.EvRequestLoggingFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.CommonsRequestLoggingFilter;


@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}


	@Bean
	public CommonsRequestLoggingFilter logFilter() {
		EvRequestLoggingFilter filter = new EvRequestLoggingFilter();

		filter.setIncludeQueryString(true);
		filter.setIncludePayload(true);
		filter.setMaxPayloadLength(10000);
		filter.setIncludeHeaders(true);
		filter.setIncludeClientInfo(true);
		filter.setIncludeResponseStatus(true);
		filter.setIncludeResponseHeaders(true);
		filter.setIncludeResponseBody(true);
		filter.setBeforeMessagePrefix("Before request data: ");
		filter.setAfterMessagePrefix("After request data: ");

		return filter;
	}

}
