package com.reliaquest.api.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder, EmployeeMock employeeMock) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(employeeMock.getConnectTimeout()))
                .setReadTimeout(Duration.ofSeconds(employeeMock.getReadTimeout()))
                .build();
    }
}