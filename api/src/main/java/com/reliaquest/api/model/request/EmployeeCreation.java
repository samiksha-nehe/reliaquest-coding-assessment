package com.reliaquest.api.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EmployeeCreation {
    @JsonProperty("name")
    @NotBlank(message = "Name cannot be blank")
    private String name;

    @JsonProperty("salary")
    @NotNull(message = "Salary is required")
    @Min(value = 1, message = "Salary must be greater than 0")
    private Integer salary;

    @JsonProperty("age")
    @NotNull(message = "Age is required")
    private Integer age;

    @JsonProperty("title")
    @NotBlank(message = "Title cannot be blank")
    private String title;

    @JsonProperty("email")
    @NotBlank(message = "Email cannot be blank")
    private String email;
}