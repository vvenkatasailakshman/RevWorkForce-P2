package com.rev.app.service;

import com.rev.app.dto.EmployeeDto;
import com.rev.app.entity.Department;
import com.rev.app.entity.Designation;
import com.rev.app.entity.Employee;
import com.rev.app.entity.User;
import com.rev.app.mapper.DTOMapper;
import com.rev.app.repository.DepartmentRepository;
import com.rev.app.repository.DesignationRepository;
import com.rev.app.repository.EmployeeRepository;
import com.rev.app.repository.UserRepository;
import com.rev.app.exceptions.ResourceNotFoundException;
import com.rev.app.repository.projection.EmployeeSummary;
import com.rev.app.repository.specification.EmployeeSpecification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DesignationRepository designationRepository;

    @Autowired
    private DTOMapper dtoMapper;

    @Autowired
    private LeaveService leaveService;

    @Autowired
    private UserService userService;


    // ================= CREATE EMPLOYEE =================
    @Override
    @Transactional
    public EmployeeDto createEmployee(EmployeeDto dto) {

        if (employeeRepository.existsById(dto.getEmpId())) {
            throw new com.rev.app.exceptions.BusinessException(
                    "Employee with ID " + dto.getEmpId() + " already exists.");
        }

        String password =
                (dto.getPassword() != null && !dto.getPassword().trim().isEmpty())
                        ? dto.getPassword()
                        : "Welcome@123";

        User user =
                userService.createUser(
                        dto.getEmail(),
                        password,
                        User.Role.valueOf(dto.getRole()));

        Department dept =
                departmentRepository.findById(dto.getDepartmentId()).orElse(null);

        Employee manager =
                dto.getManagerId() != null
                        ? employeeRepository.findById(dto.getManagerId()).orElse(null)
                        : null;

        Designation designation =
                dto.getDesignationId() != null
                        ? designationRepository.findById(dto.getDesignationId()).orElse(null)
                        : null;

        Employee employee =
                new Employee(
                        dto.getEmpId(),
                        user,
                        dto.getFirstName(),
                        dto.getLastName(),
                        dto.getPhone(),
                        dto.getAddress(),
                        dto.getEmergencyContact(),
                        dto.getDob(),
                        dto.getJoiningDate(),
                        dept,
                        designation,
                        manager,
                        dto.getSalary(),
                        dto.getSsId());

        Employee saved = employeeRepository.save(employee);

        leaveService.initializeLeaveBalances(saved.getEmpId());

        return dtoMapper.toEmployeeDto(saved);
    }


    // ================= UPDATE EMPLOYEE =================
    @Override
    @Transactional
    public EmployeeDto updateEmployee(EmployeeDto dto) {

        Employee employee =
                employeeRepository
                        .findById(dto.getEmpId())
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Employee not found with ID: " + dto.getEmpId()));

        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setPhone(dto.getPhone());
        employee.setAddress(dto.getAddress());
        employee.setEmergencyContact(dto.getEmergencyContact());

        if (dto.getDesignationId() != null) {
            employee.setDesignation(
                    designationRepository.findById(dto.getDesignationId()).orElse(null));
        }

        employee.setSalary(dto.getSalary());
        employee.setDob(dto.getDob());
        employee.setJoiningDate(dto.getJoiningDate());

        if (dto.getDepartmentId() != null) {
            employee.setDepartment(
                    departmentRepository.findById(dto.getDepartmentId()).orElse(null));
        }

        if (dto.getManagerId() != null) {
            employee.setManager(
                    employeeRepository.findById(dto.getManagerId()).orElse(null));
        }

        if (dto.getRole() != null && employee.getUser() != null) {
            employee.getUser().setRole(User.Role.valueOf(dto.getRole()));
        }

        return dtoMapper.toEmployeeDto(employeeRepository.save(employee));
    }


    // ================= GET EMPLOYEE =================
    @Override
    public EmployeeDto getEmployeeById(String empId) {

        return employeeRepository
                .findById(empId)
                .map(dtoMapper::toEmployeeDto)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Employee not found with ID: " + empId));
    }


    @Override
    public EmployeeDto getEmployeeByUserId(Long userId) {

        return employeeRepository
                .findByUser_UserId(userId)
                .map(dtoMapper::toEmployeeDto)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Employee not found for User ID: " + userId));
    }


    // ================= GET ALL EMPLOYEES =================
    @Override
    public org.springframework.data.domain.Page<EmployeeDto> getAllEmployees(
            int page, int size, String sortBy) {

        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(
                        page,
                        size,
                        org.springframework.data.domain.Sort.by(sortBy));

        return employeeRepository.findAll(pageable).map(dtoMapper::toEmployeeDto);
    }


    // ================= SEARCH EMPLOYEES =================
    @Override
    public org.springframework.data.domain.Page<EmployeeDto> searchEmployees(
            String query, int page, int size, String sortBy) {

        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(
                        page,
                        size,
                        org.springframework.data.domain.Sort.by(sortBy));

        return employeeRepository
                .searchEmployees(query, pageable)
                .map(dtoMapper::toEmployeeDto);
    }


    // ================= STATUS FILTER =================
    @Override
    public org.springframework.data.domain.Page<EmployeeDto> getEmployeesByStatus(
            int status, int page, int size, String sortBy) {

        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(
                        page,
                        size,
                        org.springframework.data.domain.Sort.by(sortBy));

        return employeeRepository
                .findByUser_IsActive(status, pageable)
                .map(dtoMapper::toEmployeeDto);
    }


    // ================= GET MANAGERS =================
    @Override
    public List<EmployeeDto> getManagers() {

        return employeeRepository
                .findAll()
                .stream()
                .filter(e ->
                        e.getUser() != null &&
                        e.getUser().getRole() == User.Role.ROLE_MANAGER)
                .map(dtoMapper::toEmployeeDto)
                .collect(Collectors.toList());
    }


    // ⭐ NEW METHOD → GET ADMINS
    @Override
    public List<EmployeeDto> getAdmins() {

        return employeeRepository
                .findAll()
                .stream()
                .filter(e ->
                        e.getUser() != null &&
                        e.getUser().getRole() == User.Role.ROLE_ADMIN)
                .map(dtoMapper::toEmployeeDto)
                .collect(Collectors.toList());
    }


    // ================= DIRECT REPORTS =================
    @Override
    public List<EmployeeDto> getDirectReports(String managerId) {

        return employeeRepository
                .findByManagerId(managerId)
                .stream()
                .map(dtoMapper::toEmployeeDto)
                .collect(Collectors.toList());
    }


    // ================= DEACTIVATE EMPLOYEE =================
    @Override
    @Transactional
    public void deleteEmployee(String empId) {

        employeeRepository
                .findById(empId)
                .ifPresent(emp -> {

                    User user = emp.getUser();

                    if (user != null) {

                        user.setIsActive(2);

                        userRepository.save(user);
                    }
                });
    }


    // ================= REACTIVATE EMPLOYEE =================
    @Override
    @Transactional
    public void reactivateEmployee(String empId) {

        employeeRepository
                .findById(empId)
                .ifPresent(emp -> {

                    User user = emp.getUser();

                    if (user != null) {

                        user.setIsActive(1);

                        userRepository.save(user);
                    }
                });
    }


    // ================= UPDATE PROFILE =================
    @Override
    @Transactional
    public EmployeeDto updateProfile(String empId, EmployeeDto dto) {

        Employee employee =
                employeeRepository
                        .findById(empId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Employee not found with ID: " + empId));

        employee.setPhone(dto.getPhone());
        employee.setAddress(dto.getAddress());
        employee.setEmergencyContact(dto.getEmergencyContact());

        return dtoMapper.toEmployeeDto(employeeRepository.save(employee));
    }


    // ================= EXISTS =================
    @Override
    public boolean isEmployeeExists(String empId) {
        return employeeRepository.existsById(empId);
    }


    // ================= SUMMARY =================
    @Override
    public List<EmployeeSummary> getEmployeeSummaries() {
        return employeeRepository.findAllSummaries();
    }


    // ================= FILTER =================
    @Override
    public List<EmployeeDto> filterEmployees(
            String department,
            String designation,
            String search) {

        return employeeRepository
                .findAll(
                        EmployeeSpecification.filterEmployees(
                                department,
                                designation,
                                search))
                .stream()
                .map(dtoMapper::toEmployeeDto)
                .collect(Collectors.toList());
    }

}