package com.reliaquest.api.service;

import com.reliaquest.api.config.EmployeeMock;
import com.reliaquest.api.model.request.EmployeeCreation;
import com.reliaquest.api.model.request.EmployeeDeletion;
import com.reliaquest.api.model.response.*;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.exception.ResourceNotFoundException;
import com.reliaquest.api.exception.TooManyRequestsException;

import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpClientErrorException.*;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeMock employeeMock;

    private final RestTemplate restTemplate;

    @Autowired
    public EmployeeServiceImpl(RestTemplate restTemplate, EmployeeMock employeeMock) {
        this.restTemplate = restTemplate;
        this.employeeMock = employeeMock;
    }

    @Override
    public List<Employee> getAllEmployees() {
        List<Employee> employees = new ArrayList<>();

        List<EmployeeServer> allEmployees = fetchAllEmployees();
        for (EmployeeServer employeeData : allEmployees) {
            Employee employee = convertToEmployee(employeeData);
            employees.add(employee);
        }
        return employees;
    }

    @Override
    public List<Employee> getEmployeesByNameSearch(String searchString) {
        List<Employee> employees = new ArrayList<>();
        List<EmployeeServer> allEmployees = fetchAllEmployees();

        for (EmployeeServer employeeData : allEmployees) {
            if (employeeData.getEmployeeName().toLowerCase().contains(searchString.toLowerCase())) {
                Employee employee = convertToEmployee(employeeData);
                employees.add(employee);
            }
        }
        return employees;
    }

    @Override
    public Employee getEmployeeById(String id) {
        EmployeeApiResponse response;

        try {
            response = makeHttpRequest(
                    employeeMock.getUri() + "/" + id,
                    HttpMethod.GET,
                    null,
                    EmployeeApiResponse.class,
                    null,
                    null
            );
        } catch (ResourceNotFoundException ex) {
            throw new EmployeeNotFoundException("Employee with ID " + id + " not found.");
        }

        if (response != null && response.getData() != null) {
            log.info("Successfully fetched employee: {}", response);

            EmployeeServer employeeDto = response.getData();
            return convertToEmployee(employeeDto);
        } else {
            throw new EmployeeNotFoundException("Employee with ID " + id + " not found.");
        }
    }

    @Override
    public Integer getHighestSalaryOfEmployees() {
        List<EmployeeServer> allEmployees = fetchAllEmployees();

        return allEmployees.stream()
                .map(EmployeeServer::getEmployeeSalary)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .max()
                .orElse(-1);
    }

    @Override
    public List<String> getTopTenHighestEarningEmployeeNames() {
        List<EmployeeServer> allEmployees = fetchAllEmployees();

        PriorityQueue<EmployeeServer> minHeap = new PriorityQueue<>((a, b) -> Integer.compare(a.getEmployeeSalary(), b.getEmployeeSalary()));
        List<String> topTenNames = new ArrayList<>();

        for (EmployeeServer employeeData : allEmployees) {
            if (employeeData.getEmployeeSalary() == null) continue;

            minHeap.offer(employeeData);
            if (minHeap.size() > 10)
                minHeap.poll();
        }

        while (!minHeap.isEmpty())
            topTenNames.add(minHeap.poll().getEmployeeName());

        log.info("Top ten highest earning employees: {}", topTenNames);
        return topTenNames;
    }

    @Override
    public Employee createEmployee(EmployeeCreation employeeInput) {
        EmployeeApiResponse response = makeHttpRequest(
                employeeMock.getUri(),
                HttpMethod.POST,
                null,
                EmployeeApiResponse.class,
                null,
                employeeInput
        );

        if (response != null && response.getData() != null) {
            log.info("Successfully created employee: {}", response.getData());
            return convertToEmployee(response.getData());
        } else {
            throw new RuntimeException("Failed to create employee as the response was null.");
        }
    }

    @Override
    public String deleteEmployeeById(String id) {
        Employee employee = getEmployeeById(id);

        EmployeeDeleteApiResponse response = makeHttpRequest(
                employeeMock.getUri(),
                HttpMethod.DELETE,
                null,
                EmployeeDeleteApiResponse.class,
                null,
                new EmployeeDeletion(employee.getEmployeeName())
        );

        if (response != null && response.getData() != null) {
            log.info("Successfully deleted employee: {}", response.getData());
            return employee.getEmployeeName();
        } else {
            log.warn("Failed to delete employee with ID: {}", id);
            return "";
        }
    }

    private List<EmployeeServer> fetchAllEmployees() {
        EmployeeListApiResponse response = makeHttpRequest(
                employeeMock.getUri(),
                HttpMethod.GET,
                null,
                EmployeeListApiResponse.class,
                null,
                null
        );

        if (response == null || response.getData() == null) {
            log.warn("No employees found.");
            return List.of();
        }

        log.info("Successfully fetched {} employees", response.getData().size());
        return response.getData();
    }

    private Employee convertToEmployee(EmployeeServer employeeData) {
        return Employee.builder()
                .id(employeeData.getId())
                .employeeEmail(employeeData.getEmployeeEmail())
                .employeeName(employeeData.getEmployeeName())
                .employeeSalary(employeeData.getEmployeeSalary())
                .employeeTitle(employeeData.getEmployeeTitle())
                .employeeAge(employeeData.getEmployeeAge())
                .build();
    }

    private <T> T makeHttpRequest(
            String url,
            HttpMethod httpMethod,
            HttpHeaders headers,
            Class<T> responseType,
            Map<String, ?> uriVariables,
            Object requestBody
    ) throws HttpClientErrorException {
        HttpEntity<?> entity = (requestBody != null) ? new HttpEntity<>(requestBody, headers)
                : new HttpEntity<>(headers);

        try {
            ResponseEntity<T> response = restTemplate.exchange(
                    url,
                    httpMethod,
                    entity,
                    responseType,
                    uriVariables != null ? uriVariables : Map.of()
            );
            return response.getBody();
        } catch (TooManyRequests ex) {
            throw new TooManyRequestsException("Too many requests made to the employee service. Please try again later.");
        } catch (NotFound ex) {
            throw new ResourceNotFoundException("Resource not found at URL: " + url);
        } catch (Exception ex) {
            throw new RuntimeException("An error occurred while making the HTTP request: " + ex.getMessage(), ex);
        }
    }
}