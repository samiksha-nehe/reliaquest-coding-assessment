package com.reliaquest.api.controller;

import com.reliaquest.api.model.request.EmployeeCreation;
import com.reliaquest.api.model.response.Employee;
import com.reliaquest.api.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeControllerImplTest {

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeControllerImpl employeeController;

    private Employee testEmployee1;
    private Employee testEmployee2;
    private EmployeeCreation testEmployeeCreation;
    private UUID validUuid;

    @BeforeEach
    void setUp() {
        validUuid = UUID.randomUUID();

        testEmployee1 = new Employee();
        testEmployee1.setId(validUuid);
        testEmployee1.setEmployeeName("Will Walker");
        testEmployee1.setEmployeeSalary(75000);
        testEmployee1.setEmployeeAge(30);

        testEmployee2 = new Employee();
        testEmployee2.setId(UUID.randomUUID());
        testEmployee2.setEmployeeName("Millie Bobby");
        testEmployee2.setEmployeeSalary(85000);
        testEmployee2.setEmployeeAge(28);

        testEmployeeCreation = new EmployeeCreation();
        testEmployeeCreation.setName("Jere Fisher");
        testEmployeeCreation.setSalary(60000);
        testEmployeeCreation.setAge(25);
    }

    @Test
    void getAllEmployees_EmployeesExist() {
        List<Employee> expectedEmployees = Arrays.asList(testEmployee1, testEmployee2);
        when(employeeService.getAllEmployees()).thenReturn(expectedEmployees);

        ResponseEntity<List<Employee>> response = employeeController.getAllEmployees();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedEmployees, response.getBody());
        assertEquals(2, response.getBody().size());
        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    void getAllEmployees_EmployeesDoNotExist() {
        List<Employee> emptyList = Collections.emptyList();
        when(employeeService.getAllEmployees()).thenReturn(emptyList);

        ResponseEntity<List<Employee>> response = employeeController.getAllEmployees();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(emptyList, response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    void getEmployeesByNameSearch_ValidSearchString() {
        String searchString = "John";
        List<Employee> expectedEmployees = Arrays.asList(testEmployee1);
        when(employeeService.getEmployeesByNameSearch(searchString)).thenReturn(expectedEmployees);

        ResponseEntity<List<Employee>> response = employeeController.getEmployeesByNameSearch(searchString);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedEmployees, response.getBody());
        assertEquals(1, response.getBody().size());
        verify(employeeService, times(1)).getEmployeesByNameSearch(searchString);
    }

    @Test
    void getEmployeesByNameSearch_SearchStringIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> employeeController.getEmployeesByNameSearch(null)
        );

        assertEquals("Search string cannot be null or empty", exception.getMessage());
        verify(employeeService, never()).getEmployeesByNameSearch(any());
    }

    @Test
    void getEmployeesByNameSearch_SearchStringIsEmpty() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> employeeController.getEmployeesByNameSearch("")
        );

        assertEquals("Search string cannot be null or empty", exception.getMessage());
        verify(employeeService, never()).getEmployeesByNameSearch(any());
    }

    @Test
    void getEmployeesByNameSearch_SearchStringIsBlankException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> employeeController.getEmployeesByNameSearch("   ")
        );

        assertEquals("Search string cannot be null or empty", exception.getMessage());
        verify(employeeService, never()).getEmployeesByNameSearch(any());
    }

    @Test
    void getEmployeeById_ValidId() {
        when(employeeService.getEmployeeById(validUuid.toString())).thenReturn(testEmployee1);

        ResponseEntity<Employee> response = employeeController.getEmployeeById(validUuid.toString());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testEmployee1, response.getBody());
        verify(employeeService, times(1)).getEmployeeById(validUuid.toString());
    }

    @Test
    void getEmployeeById_NullId() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> employeeController.getEmployeeById(null)
        );

        assertEquals("Employee ID cannot be null or empty", exception.getMessage());
        verify(employeeService, never()).getEmployeeById(any());
    }

    @Test
    void getEmployeeById_EmptyId() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> employeeController.getEmployeeById("")
        );

        assertEquals("Employee ID cannot be null or empty", exception.getMessage());
        verify(employeeService, never()).getEmployeeById(any());
    }

    @Test
    void getEmployeeById_BlankId() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> employeeController.getEmployeeById("   ")
        );

        assertEquals("Employee ID cannot be null or empty", exception.getMessage());
        verify(employeeService, never()).getEmployeeById(any());
    }

    @Test
    void getEmployeeById_InvalidUUID() {
        String invalidUuid = "invalid-uuid";

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> employeeController.getEmployeeById(invalidUuid)
        );

        assertEquals("Invalid UUID format for Employee ID: " + invalidUuid, exception.getMessage());
        verify(employeeService, never()).getEmployeeById(any());
    }

    @Test
    void getHighestSalaryOfEmployees_HighestSalary() {
        Integer expectedSalary = 100000;
        when(employeeService.getHighestSalaryOfEmployees()).thenReturn(expectedSalary);

        ResponseEntity<Integer> response = employeeController.getHighestSalaryOfEmployees();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedSalary, response.getBody());
        verify(employeeService, times(1)).getHighestSalaryOfEmployees();
    }

    @Test
    void getHighestSalaryOfEmployees_NoEmployees() {
        Integer expectedSalary = 0;
        when(employeeService.getHighestSalaryOfEmployees()).thenReturn(expectedSalary);

        ResponseEntity<Integer> response = employeeController.getHighestSalaryOfEmployees();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedSalary, response.getBody());
        verify(employeeService, times(1)).getHighestSalaryOfEmployees();
    }

    @Test
    void getTopTenHighestEarningEmployeeNames() {
        List<String> expectedNames = Arrays.asList("John Doe", "Jane Smith", "Bob Johnson");
        when(employeeService.getTopTenHighestEarningEmployeeNames()).thenReturn(expectedNames);

        ResponseEntity<List<String>> response = employeeController.getTopTenHighestEarningEmployeeNames();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedNames, response.getBody());
        assertEquals(3, response.getBody().size());
        verify(employeeService, times(1)).getTopTenHighestEarningEmployeeNames();
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_NoEmployees() {
        List<String> emptyList = Collections.emptyList();
        when(employeeService.getTopTenHighestEarningEmployeeNames()).thenReturn(emptyList);

        ResponseEntity<List<String>> response = employeeController.getTopTenHighestEarningEmployeeNames();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(emptyList, response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(employeeService, times(1)).getTopTenHighestEarningEmployeeNames();
    }

    @Test
    void createEmployeeValid() {
        when(employeeService.createEmployee(testEmployeeCreation)).thenReturn(testEmployee1);

        ResponseEntity<Employee> response = employeeController.createEmployee(testEmployeeCreation);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testEmployee1, response.getBody());
        verify(employeeService, times(1)).createEmployee(testEmployeeCreation);
    }

    @Test
    void createEmployee_Service() {
        when(employeeService.createEmployee(any(EmployeeCreation.class))).thenReturn(testEmployee1);

        employeeController.createEmployee(testEmployeeCreation);

        verify(employeeService, times(1)).createEmployee(testEmployeeCreation);
    }

    @Test
    void deleteEmployeeById_ValidId() {
        String expectedName = "John Doe";
        when(employeeService.deleteEmployeeById(validUuid.toString())).thenReturn(expectedName);

        ResponseEntity<String> response = employeeController.deleteEmployeeById(validUuid.toString());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedName, response.getBody());
        verify(employeeService, times(1)).deleteEmployeeById(validUuid.toString());
    }

    @Test
    void deleteEmployeeById_NullId() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> employeeController.deleteEmployeeById(null)
        );

        assertEquals("Employee ID cannot be null or empty", exception.getMessage());
        verify(employeeService, never()).deleteEmployeeById(any());
    }

    @Test
    void deleteEmployeeById_EmptyId() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> employeeController.deleteEmployeeById("")
        );

        assertEquals("Employee ID cannot be null or empty", exception.getMessage());
        verify(employeeService, never()).deleteEmployeeById(any());
    }

    @Test
    void deleteEmployeeById_BlankId() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> employeeController.deleteEmployeeById("   ")
        );

        assertEquals("Employee ID cannot be null or empty", exception.getMessage());
        verify(employeeService, never()).deleteEmployeeById(any());
    }

    @Test
    void deleteEmployeeById_InvalidUUID() {
        String invalidUuid = "invalid-uuid";

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> employeeController.deleteEmployeeById(invalidUuid)
        );

        assertEquals("Invalid UUID format for Employee ID: " + invalidUuid, exception.getMessage());
        verify(employeeService, never()).deleteEmployeeById(any());
    }
}