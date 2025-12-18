package com.reliaquest.api.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class EmployeeListApiResponse {
    @JsonProperty("data")
    private List<EmployeeServer> data;

    @JsonProperty("status")
    private String status;
}