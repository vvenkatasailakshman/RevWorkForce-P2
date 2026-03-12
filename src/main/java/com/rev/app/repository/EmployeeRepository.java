package com.rev.app.repository;

import com.rev.app.entity.Employee;
import com.rev.app.repository.projection.EmployeeSummary;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String>, JpaSpecificationExecutor<Employee> {

    Optional<Employee> findByUser_UserId(Long userId);

    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.manager m LEFT JOIN FETCH m.user WHERE e.empId = :empId")
    Optional<Employee> findByIdWithManagerAndUser(@Param("empId") String empId);

    @Query("SELECT e FROM Employee e WHERE e.manager.empId = :managerId AND e.user.isActive = 1")
    List<Employee> findByManagerId(@Param("managerId") String managerId);

    @Query("SELECT e FROM Employee e WHERE e.manager.empId = :managerId")
    List<Employee> findAllByManagerId(@Param("managerId") String managerId);

    Optional<Employee> findByUser_Email(String email);

    List<Employee> findByDepartment_DepartmentId(Long departmentId);

    // ⭐ SEARCH EMPLOYEES
    @Query("SELECT e FROM Employee e WHERE LOWER(e.firstName) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(e.empId) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(e.designation.designationName) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(e.department.departmentName) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Employee> searchEmployees(@Param("query") String query, Pageable pageable);

    // ⭐ EMPLOYEE SUMMARY
    @Query("SELECT e.empId as empId, e.firstName as firstName, e.lastName as lastName FROM Employee e")
    List<EmployeeSummary> findAllSummaries();

    List<EmployeeSummary> findByDepartment_DepartmentId(Long departmentId, Class<EmployeeSummary> type);

    // ⭐ NEW METHOD FOR STATUS FILTER
    Page<Employee> findByUser_IsActive(int isActive, Pageable pageable);

}