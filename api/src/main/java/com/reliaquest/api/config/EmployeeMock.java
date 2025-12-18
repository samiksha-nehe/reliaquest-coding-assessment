package com.reliaquest.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "mock.employee")
public class EmployeeMock {
    private String uri;
    private Integer connectTimeout;
    private Integer readTimeout;
}