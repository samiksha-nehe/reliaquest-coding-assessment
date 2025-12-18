package com.reliaquest.api.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class EmployeeDeleteApiResponse {
    @JsonProperty("data")
    private Boolean data;

    @JsonProperty("status")
    private String status;
}