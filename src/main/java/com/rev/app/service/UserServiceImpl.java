package com.rev.app.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rev.app.dto.RegistrationDto;
import com.rev.app.entity.Department;
import com.rev.app.entity.Designation;
import com.rev.app.entity.Employee;
import com.rev.app.entity.User;
import com.rev.app.repository.DepartmentRepository;
import com.rev.app.repository.DesignationRepository;
import com.rev.app.repository.EmployeeRepository;
import com.rev.app.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DesignationRepository designationRepository;

    @Autowired
    private LeaveService leaveService;

    // ⭐ Notification Service
    @Autowired
    private NotificationService notificationService;

    // ================= CREATE USER =================
    @Override
    @Transactional
    public User createUser(String email, String password, User.Role role) {

        String normalizedEmail = email.toLowerCase().trim();

        if (userRepository.findByEmailIgnoreCase(normalizedEmail).isPresent()) {
            throw new com.rev.app.exceptions.BusinessException(
                    "User with email " + normalizedEmail + " already exists.");
        }

        int activeStatus = (role == User.Role.ROLE_ADMIN) ? 1 : 0;

        User user = new User(
                null,
                normalizedEmail,
                passwordEncoder.encode(password),
                role,
                activeStatus,
                null,
                null
        );

        return userRepository.save(user);
    }

    // ================= REGISTER USER =================
    @Override
    @Transactional
    public User registerUser(RegistrationDto dto) {

        String normalizedEmail = dto.getEmail().toLowerCase().trim();

        if (userRepository.findByEmailIgnoreCase(normalizedEmail).isPresent()) {
            throw new com.rev.app.exceptions.BusinessException(
                    "User with email " + normalizedEmail + " already exists.");
        }

        User.Role userRole = User.Role.valueOf(dto.getRole());

        int activeStatus = (userRole == User.Role.ROLE_ADMIN) ? 1 : 0;

        User user = new User(
                null,
                normalizedEmail,
                passwordEncoder.encode(dto.getPassword()),
                userRole,
                activeStatus,
                null,
                null
        );

        User savedUser = userRepository.save(user);

        // ================= GET DEPARTMENT =================
        Department dept = null;
        if (dto.getDepartmentId() != null) {
            dept = departmentRepository.findById(dto.getDepartmentId()).orElse(null);
        }

        // ================= GET DESIGNATION =================
        Designation designation = null;
        if (dto.getDesignationId() != null) {
            designation = designationRepository.findById(dto.getDesignationId()).orElse(null);
        }

        // ================= FIND MANAGER =================
        Employee manager = null;

        if (dto.getManagerId() != null && !dto.getManagerId().isEmpty()) {
            manager = employeeRepository.findById(dto.getManagerId()).orElse(null);
        }

        if (manager == null) {

            if (userRole == User.Role.ROLE_EMPLOYEE) {

                manager = employeeRepository.findAll().stream()
                        .filter(e -> e.getUser() != null &&
                                e.getUser().getRole() == User.Role.ROLE_MANAGER)
                        .findFirst()
                        .orElse(null);

            } else if (userRole == User.Role.ROLE_MANAGER) {

                manager = employeeRepository.findAll().stream()
                        .filter(e -> e.getUser() != null &&
                                e.getUser().getRole() == User.Role.ROLE_ADMIN)
                        .findFirst()
                        .orElse(null);
            }
        }

        // ================= CREATE EMPLOYEE =================
        Employee employee = new Employee(
                "RW" + String.format("%03d", (int) (Math.random() * 1000)),
                savedUser,
                dto.getFirstName(),
                dto.getLastName(),
                null,
                null,
                null,
                null,
                LocalDate.now(),
                dept,
                designation,
                manager,
                null,
                null
        );

        employeeRepository.save(employee);

        // ================= INITIALIZE LEAVE =================
        leaveService.initializeLeaveBalances(employee.getEmpId());

        // ================= SEND ADMIN NOTIFICATIONS =================

        String message;

        if (userRole == User.Role.ROLE_MANAGER) {
            message = "New manager registered: " + dto.getFirstName() + " " + dto.getLastName();
        } else {
            message = "New employee registered: " + dto.getFirstName() + " " + dto.getLastName();
        }

        // ⭐ Find ALL admins
        List<User> admins = userRepository.findAll()
                .stream()
                .filter(u -> u.getRole() == User.Role.ROLE_ADMIN)
                .toList();

        // ⭐ Send notification to each admin
        for (User admin : admins) {

            notificationService.sendNotification(
                    admin.getUserId(),
                    message,
                    "REGISTRATION"
            );
        }

        return savedUser;
    }

    // ================= FIND USER BY EMAIL =================
    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email.toLowerCase().trim());
    }

    // ================= UPDATE PASSWORD =================
    @Override
    @Transactional
    public void updatePassword(Long userId, String newPassword) {

        userRepository.findById(userId).ifPresent(user -> {

            user.setPassword(passwordEncoder.encode(newPassword));

            userRepository.save(user);

        });
    }

    // ================= SET ACTIVE STATUS =================
    @Override
    @Transactional
    public void setActiveStatus(Long userId, boolean isActive) {

        userRepository.findById(userId).ifPresent(user -> {

            user.setIsActive(isActive ? 1 : 0);

            userRepository.save(user);

        });
    }

    // ================= ADMIN FILTER METHODS =================

    @Override
    public List<User> getUsersByStatus(int status) {
        return userRepository.findByIsActive(status);
    }

    @Override
    public long countUsersByStatus(int status) {
        return userRepository.countByIsActive(status);
    }
}