package com.reliaquest.api.service;

import com.reliaquest.api.model.request.EmployeeCreation;
import com.reliaquest.api.model.response.Employee;
import java.util.List;

public interface EmployeeService {
    List<Employee> getAllEmployees();

    List<Employee> getEmployeesByNameSearch(String searchString);

    Employee getEmployeeById(String id);

    Integer getHighestSalaryOfEmployees();

    List<String> getTopTenHighestEarningEmployeeNames();

    Employee createEmployee(EmployeeCreation employeeInput);

    String deleteEmployeeById(String id);
}