package com.reliaquest.api.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class EmployeeApiResponse {
    @JsonProperty("data")
    private EmployeeServer data;

    @JsonProperty("status")
    private String status;
}