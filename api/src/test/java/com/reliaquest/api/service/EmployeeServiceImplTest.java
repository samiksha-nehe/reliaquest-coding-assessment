package com.reliaquest.api.service;

import com.reliaquest.api.config.EmployeeMock;
import com.reliaquest.api.model.request.EmployeeCreation;
import com.reliaquest.api.model.request.EmployeeDeletion;
import com.reliaquest.api.model.response.*;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.exception.TooManyRequestsException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private EmployeeMock mockEmployeeProperties;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private static final String BASE_URI = "http://localhost:8080/api/v1/employees";

    @BeforeEach
    void setUp() {
        when(mockEmployeeProperties.getUri()).thenReturn(BASE_URI);
    }

    @Test
    void getAllEmployees_EmployeesExist() {
        List<EmployeeServer> serverEmployees = createMockServerEmployees();
        EmployeeListApiResponse responseDto = new EmployeeListApiResponse();
        responseDto.setData(serverEmployees);

        when(restTemplate.exchange(
                eq(BASE_URI),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(EmployeeListApiResponse.class),
                eq(Map.of())
        )).thenReturn(ResponseEntity.ok(responseDto));

        List<Employee> result = employeeService.getAllEmployees();

        assertNotNull(result);
        assertEquals(2, result.size());

        Employee firstEmployee = result.get(0);
        assertEquals("John Doe", firstEmployee.getEmployeeName());
        assertEquals("john.doe@example.com", firstEmployee.getEmployeeEmail());
        assertEquals(50000, firstEmployee.getEmployeeSalary());
        assertEquals("Developer", firstEmployee.getEmployeeTitle());
        assertEquals(30, firstEmployee.getEmployeeAge());
    }

    @Test
    void getAllEmployees_NoEmployeesExist() {
        EmployeeListApiResponse responseDto = new EmployeeListApiResponse();
        responseDto.setData(null);

        when(restTemplate.exchange(
                eq(BASE_URI),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(EmployeeListApiResponse.class),
                eq(Map.of())
        )).thenReturn(ResponseEntity.ok(responseDto));

        List<Employee> result = employeeService.getAllEmployees();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getEmployeesByNameSearch_SearchStringMatches() {
        String searchString = "john";
        List<EmployeeServer> serverEmployees = createMockServerEmployees();
        EmployeeListApiResponse responseDto = new EmployeeListApiResponse();
        responseDto.setData(serverEmployees);

        when(restTemplate.exchange(
                eq(BASE_URI),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(EmployeeListApiResponse.class),
                eq(Map.of())
        )).thenReturn(ResponseEntity.ok(responseDto));

        List<Employee> result = employeeService.getEmployeesByNameSearch(searchString);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getEmployeeName());
    }

    @Test
    void getEmployeesByNameSearch_NoMatches() {
        String searchString = "nonexistent";
        List<EmployeeServer> serverEmployees = createMockServerEmployees();
        EmployeeListApiResponse responseDto = new EmployeeListApiResponse();
        responseDto.setData(serverEmployees);

        when(restTemplate.exchange(
                eq(BASE_URI),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(EmployeeListApiResponse.class),
                eq(Map.of())
        )).thenReturn(ResponseEntity.ok(responseDto));

        List<Employee> result = employeeService.getEmployeesByNameSearch(searchString);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getEmployeeById_EmployeeExists() {
        UUID employeeId = UUID.randomUUID();
        EmployeeServer serverEmployee = createMockServerEmployee(employeeId, "John Doe", "john.doe@example.com", 50000, "Developer", 30);
        EmployeeApiResponse responseDto = new EmployeeApiResponse();
        responseDto.setData(serverEmployee);

        when(restTemplate.exchange(
                eq(BASE_URI + "/" + employeeId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(EmployeeApiResponse.class),
                eq(Map.of())
        )).thenReturn(ResponseEntity.ok(responseDto));

        Employee result = employeeService.getEmployeeById(employeeId.toString());

        assertNotNull(result);
        assertEquals(employeeId, result.getId());
        assertEquals("John Doe", result.getEmployeeName());
    }

    @Test
    void getEmployeeById_EmployeeNotFound() {
        UUID employeeId = UUID.randomUUID();

        when(restTemplate.exchange(
                eq(BASE_URI + "/" + employeeId.toString()),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(EmployeeApiResponse.class),
                eq(Map.of())
        )).thenThrow(HttpClientErrorException.create(
                org.springframework.http.HttpStatus.NOT_FOUND,
                "Not Found",
                org.springframework.http.HttpHeaders.EMPTY,
                null,
                null
        ));

        EmployeeNotFoundException exception = assertThrows(
                EmployeeNotFoundException.class,
                () -> employeeService.getEmployeeById(employeeId.toString())
        );

        assertEquals("Employee with ID " + employeeId + " not found.", exception.getMessage());
    }

    @Test
    void getEmployeeById_ResponseDataIsNull() {
        UUID employeeId = UUID.randomUUID();
        EmployeeApiResponse responseDto = new EmployeeApiResponse();
        responseDto.setData(null);

        when(restTemplate.exchange(
                eq(BASE_URI + "/" + employeeId.toString()),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(EmployeeApiResponse.class),
                eq(Map.of())
        )).thenReturn(ResponseEntity.ok(responseDto));

        EmployeeNotFoundException exception = assertThrows(
                EmployeeNotFoundException.class,
                () -> employeeService.getEmployeeById(employeeId.toString())
        );

        assertEquals("Employee with ID " + employeeId + " not found.", exception.getMessage());
    }

    @Test
    void getHighestSalaryOfEmployees_EmployeesExist() {
        List<EmployeeServer> serverEmployees = Arrays.asList(
                createMockServerEmployee(UUID.randomUUID(), "John Doe", "john@example.com", 50000, "Developer", 30),
                createMockServerEmployee(UUID.randomUUID(), "Jane Smith", "jane@example.com", 75000, "Manager", 35),
                createMockServerEmployee(UUID.randomUUID(), "Bob Johnson", "bob@example.com", 60000, "Senior Developer", 32)
        );
        EmployeeListApiResponse responseDto = new EmployeeListApiResponse();
        responseDto.setData(serverEmployees);

        when(restTemplate.exchange(
                eq(BASE_URI),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(EmployeeListApiResponse.class),
                eq(Map.of())
        )).thenReturn(ResponseEntity.ok(responseDto));

        Integer result = employeeService.getHighestSalaryOfEmployees();

        assertEquals(75000, result);
    }

    @Test
    void getHighestSalaryOfEmployees_NoEmployeesExist() {
        EmployeeListApiResponse responseDto = new EmployeeListApiResponse();
        responseDto.setData(Arrays.asList());

        when(restTemplate.exchange(
                eq(BASE_URI),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(EmployeeListApiResponse.class),
                eq(Map.of())
        )).thenReturn(ResponseEntity.ok(responseDto));

        Integer result = employeeService.getHighestSalaryOfEmployees();

        assertEquals(-1, result);
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_MoreThanTenEmployees() {
        List<EmployeeServer> serverEmployees = createMockServerEmployeesForTopTen();
        EmployeeListApiResponse responseDto = new EmployeeListApiResponse();
        responseDto.setData(serverEmployees);

        when(restTemplate.exchange(
                eq(BASE_URI),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(EmployeeListApiResponse.class),
                eq(Map.of())
        )).thenReturn(ResponseEntity.ok(responseDto));

        List<String> result = employeeService.getTopTenHighestEarningEmployeeNames();

        assertNotNull(result);
        assertEquals(10, result.size());
        assertTrue(result.contains("Employee 15"));
        assertFalse(result.contains("Employee 1"));
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_EmployeesWithNullSalary() {
        List<EmployeeServer> serverEmployees = Arrays.asList(
                createMockServerEmployee(UUID.randomUUID(), "John Doe", "john@example.com", null, "Developer", 30),
                createMockServerEmployee(UUID.randomUUID(), "Jane Smith", "jane@example.com", 50000, "Manager", 35)
        );
        EmployeeListApiResponse responseDto = new EmployeeListApiResponse();
        responseDto.setData(serverEmployees);

        when(restTemplate.exchange(
                eq(BASE_URI),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(EmployeeListApiResponse.class),
                eq(Map.of())
        )).thenReturn(ResponseEntity.ok(responseDto));

        List<String> result = employeeService.getTopTenHighestEarningEmployeeNames();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Jane Smith", result.get(0));
    }

    @Test
    void createEmployee_Successful() {
        EmployeeCreation creationDto = new EmployeeCreation();
        creationDto.setName("New Employee");
        creationDto.setSalary(60000);
        creationDto.setAge(28);

        UUID newEmployeeId = UUID.randomUUID();
        EmployeeServer createdServerEmployee = createMockServerEmployee(newEmployeeId, "New Employee", "new@example.com", 60000, "Developer", 28);
        EmployeeApiResponse responseDto = new EmployeeApiResponse();
        responseDto.setData(createdServerEmployee);

        when(restTemplate.exchange(
                eq(BASE_URI),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(EmployeeApiResponse.class),
                eq(Map.of())
        )).thenReturn(ResponseEntity.ok(responseDto));

        Employee result = employeeService.createEmployee(creationDto);

        assertNotNull(result);
        assertEquals(newEmployeeId, result.getId());
        assertEquals("New Employee", result.getEmployeeName());
        assertEquals(60000, result.getEmployeeSalary());
    }

    @Test
    void createEmployee_ResponseNull() {
        EmployeeCreation creationDto = new EmployeeCreation();
        creationDto.setName("New Employee");

        when(restTemplate.exchange(
                eq(BASE_URI),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(EmployeeApiResponse.class),
                eq(Map.of())
        )).thenReturn(ResponseEntity.ok(null));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> employeeService.createEmployee(creationDto)
        );

        assertEquals("Failed to create employee. Response was null or empty.", exception.getMessage());
    }

    @Test
    void deleteEmployeeById() {
        UUID employeeId = UUID.randomUUID();
        String employeeName = "John Doe";

        // Mock getEmployeeById call
        EmployeeServer serverEmployee = createMockServerEmployee(employeeId, employeeName, "john@example.com", 50000, "Developer", 30);
        EmployeeApiResponse getResponseDto = new EmployeeApiResponse();
        getResponseDto.setData(serverEmployee);

        when(restTemplate.exchange(
                eq(BASE_URI + "/" + employeeId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(EmployeeApiResponse.class),
                eq(Map.of())
        )).thenReturn(ResponseEntity.ok(getResponseDto));

        // Mock delete call
        EmployeeDeleteApiResponse deleteResponseDto = new EmployeeDeleteApiResponse();
        deleteResponseDto.setData(true);

        when(restTemplate.exchange(
                eq(BASE_URI),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(EmployeeDeleteApiResponse.class),
                eq(Map.of())
        )).thenReturn(ResponseEntity.ok(deleteResponseDto));

        String result = employeeService.deleteEmployeeById(employeeId.toString());

        assertEquals(employeeName, result);

        // Verify the delete request body
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate, times(2)).exchange(
                anyString(),
                any(HttpMethod.class),
                entityCaptor.capture(),
                any(Class.class),
                eq(Map.of())
        );

        // Check the second call (delete call) has the correct body
        HttpEntity<?> deleteEntity = entityCaptor.getAllValues().get(1);
        assertNotNull(deleteEntity.getBody());
        assertTrue(deleteEntity.getBody() instanceof EmployeeDeletion);
        EmployeeDeletion deletionDto = (EmployeeDeletion) deleteEntity.getBody();
        assertEquals(employeeName, deletionDto.getName());
    }

    @Test
    void deleteEmployeeById_DeleteResponseNull() {
        UUID employeeId = UUID.randomUUID();
        String employeeName = "John Doe";

        // Mock getEmployeeById call
        EmployeeServer serverEmployee = createMockServerEmployee(employeeId, employeeName, "john@example.com", 50000, "Developer", 30);
        EmployeeApiResponse getResponseDto = new EmployeeApiResponse();
        getResponseDto.setData(serverEmployee);

        when(restTemplate.exchange(
                eq(BASE_URI + "/" + employeeId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(EmployeeApiResponse.class),
                eq(Map.of())
        )).thenReturn(ResponseEntity.ok(getResponseDto));

        // Mock delete call to return null
        when(restTemplate.exchange(
                eq(BASE_URI),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(EmployeeDeleteApiResponse.class),
                eq(Map.of())
        )).thenReturn(ResponseEntity.ok(null));

        String result = employeeService.deleteEmployeeById(employeeId.toString());

        assertEquals("", result);
    }

    @Test
    void makeHttpRequest_TooManyRequestsThrown() {
        EmployeeListApiResponse responseDto = new EmployeeListApiResponse();
        responseDto.setData(null);

        when(restTemplate.exchange(
                eq(BASE_URI),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(EmployeeListApiResponse.class),
                eq(Map.of())
        )).thenThrow(HttpClientErrorException.create(
                org.springframework.http.HttpStatus.TOO_MANY_REQUESTS,
                "Too Many Requests",
                org.springframework.http.HttpHeaders.EMPTY,
                null,
                null
        ));

        TooManyRequestsException exception = assertThrows(
                TooManyRequestsException.class,
                () -> employeeService.getAllEmployees()
        );

        assertEquals("Too many requests made to the employee service. Please try again later.", exception.getMessage());
    }

    @Test
    void makeHttpRequest_GenericException() {
        when(restTemplate.exchange(
                eq(BASE_URI),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(EmployeeListApiResponse.class),
                eq(Map.of())
        )).thenThrow(new RuntimeException("Generic error"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> employeeService.getAllEmployees()
        );

        assertTrue(exception.getMessage().contains("An error occurred while making the HTTP request"));
    }

    private List<EmployeeServer> createMockServerEmployees() {
        return Arrays.asList(
                createMockServerEmployee(UUID.randomUUID(), "John Doe", "john.doe@example.com", 50000, "Developer", 30),
                createMockServerEmployee(UUID.randomUUID(), "Jane Smith", "jane.smith@example.com", 60000, "Manager", 35)
        );
    }

    private List<EmployeeServer> createMockServerEmployeesForTopTen() {
        List<EmployeeServer> employees = Arrays.asList(
                createMockServerEmployee(UUID.randomUUID(), "Example 1", "emp1@example.com", 30000, "Junior", 25),
                createMockServerEmployee(UUID.randomUUID(), "Example 2", "emp2@example.com", 35000, "Junior", 26),
                createMockServerEmployee(UUID.randomUUID(), "Example 3", "emp3@example.com", 40000, "Junior", 27),
                createMockServerEmployee(UUID.randomUUID(), "Example 4", "emp4@example.com", 45000, "Mid", 28),
                createMockServerEmployee(UUID.randomUUID(), "Example 5", "emp5@example.com", 50000, "Mid", 29),
                createMockServerEmployee(UUID.randomUUID(), "Example 6", "emp6@example.com", 55000, "Mid", 30),
                createMockServerEmployee(UUID.randomUUID(), "Example 7", "emp7@example.com", 60000, "Senior", 31),
                createMockServerEmployee(UUID.randomUUID(), "Example 8", "emp8@example.com", 65000, "Senior", 32),
                createMockServerEmployee(UUID.randomUUID(), "Example 9", "emp9@example.com", 70000, "Senior", 33),
                createMockServerEmployee(UUID.randomUUID(), "Example 10", "emp10@example.com", 75000, "Lead", 34),
                createMockServerEmployee(UUID.randomUUID(), "Example 11", "emp11@example.com", 80000, "Lead", 35),
                createMockServerEmployee(UUID.randomUUID(), "Example 12", "emp12@example.com", 85000, "Manager", 36),
                createMockServerEmployee(UUID.randomUUID(), "Example 13", "emp13@example.com", 90000, "Manager", 37),
                createMockServerEmployee(UUID.randomUUID(), "Example 14", "emp14@example.com", 95000, "Director", 38),
                createMockServerEmployee(UUID.randomUUID(), "Example 15", "emp15@example.com", 100000, "Director", 39)
        );
        return employees;
    }

    private EmployeeServer createMockServerEmployee(UUID id, String name, String email, Integer salary, String title, Integer age) {
        EmployeeServer employee = new EmployeeServer();
        employee.setId(id);
        employee.setEmployeeName(name);
        employee.setEmployeeEmail(email);
        employee.setEmployeeSalary(salary);
        employee.setEmployeeTitle(title);
        employee.setEmployeeAge(age);
        return employee;
    }
}