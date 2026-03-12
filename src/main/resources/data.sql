-- Initial Data for RevWorkforce (Oracle-safe: inserts only if row does not exist)

-- Departments
INSERT INTO department (department_name)
    SELECT 'IT' FROM dual WHERE NOT EXISTS (SELECT 1 FROM department WHERE department_name = 'IT');
INSERT INTO department (department_name)
    SELECT 'HR' FROM dual WHERE NOT EXISTS (SELECT 1 FROM department WHERE department_name = 'HR');
INSERT INTO department (department_name)
    SELECT 'Finance' FROM dual WHERE NOT EXISTS (SELECT 1 FROM department WHERE department_name = 'Finance');
INSERT INTO department (department_name)
    SELECT 'Marketing' FROM dual WHERE NOT EXISTS (SELECT 1 FROM department WHERE department_name = 'Marketing');
INSERT INTO department (department_name)
    SELECT 'Operations' FROM dual WHERE NOT EXISTS (SELECT 1 FROM department WHERE department_name = 'Operations');

-- Designations
INSERT INTO designation (designation_name)
    SELECT 'System Administrator' FROM dual WHERE NOT EXISTS (SELECT 1 FROM designation WHERE designation_name = 'System Administrator');
INSERT INTO designation (designation_name)
    SELECT 'IT Manager' FROM dual WHERE NOT EXISTS (SELECT 1 FROM designation WHERE designation_name = 'IT Manager');
INSERT INTO designation (designation_name)
    SELECT 'Java Developer' FROM dual WHERE NOT EXISTS (SELECT 1 FROM designation WHERE designation_name = 'Java Developer');
INSERT INTO designation (designation_name)
    SELECT 'HR Specialist' FROM dual WHERE NOT EXISTS (SELECT 1 FROM designation WHERE designation_name = 'HR Specialist');
INSERT INTO designation (designation_name)
    SELECT 'Financial Analyst' FROM dual WHERE NOT EXISTS (SELECT 1 FROM designation WHERE designation_name = 'Financial Analyst');

-- Users (password is 'password')
INSERT INTO users (email, password, role, is_active)
    SELECT 'admin@rev.com', '$2a$10$8.UnVuG9HHgffUDAlk8q7uy5AkNIvH6VTu2lO/k.3.y8uK07N9Dbm', 'ROLE_ADMIN', 1
    FROM dual WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@rev.com');
INSERT INTO users (email, password, role, is_active)
    SELECT 'manager@rev.com', '$2a$10$8.UnVuG9HHgffUDAlk8q7uy5AkNIvH6VTu2lO/k.3.y8uK07N9Dbm', 'ROLE_MANAGER', 1
    FROM dual WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'manager@rev.com');
INSERT INTO users (email, password, role, is_active)
    SELECT 'employee@rev.com', '$2a$10$8.UnVuG9HHgffUDAlk8q7uy5AkNIvH6VTu2lO/k.3.y8uK07N9Dbm', 'ROLE_EMPLOYEE', 1
    FROM dual WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'employee@rev.com');

-- Employees
-- RW001: Admin
INSERT INTO employee (emp_id, user_id, first_name, last_name, joining_date, department_id, designation_id, salary)
    SELECT 'RW001', (SELECT user_id FROM users WHERE email = 'admin@rev.com'), 'Rev', 'Admin', CURRENT_DATE, 1, 1, 100000.00
    FROM dual WHERE NOT EXISTS (SELECT 1 FROM employee WHERE emp_id = 'RW001');

-- RW002: Manager (Report to Admin)
INSERT INTO employee (emp_id, user_id, first_name, last_name, joining_date, department_id, designation_id, manager_id, salary)
    SELECT 'RW002', (SELECT user_id FROM users WHERE email = 'manager@rev.com'), 'Jane', 'Manager', CURRENT_DATE, 1, 2, 'RW001', 85000.00
    FROM dual WHERE NOT EXISTS (SELECT 1 FROM employee WHERE emp_id = 'RW002');

-- RW003: Employee (Reports to Manager RW002)
INSERT INTO employee (emp_id, user_id, first_name, last_name, joining_date, department_id, designation_id, manager_id, salary)
    SELECT 'RW003', (SELECT user_id FROM users WHERE email = 'employee@rev.com'), 'John', 'Employee', CURRENT_DATE, 1, 3, 'RW002', 60000.00
    FROM dual WHERE NOT EXISTS (SELECT 1 FROM employee WHERE emp_id = 'RW003');

-- Leave Types
INSERT INTO leave_type (leave_name, max_per_year)
    SELECT 'Sick Leave', 12 FROM dual WHERE NOT EXISTS (SELECT 1 FROM leave_type WHERE leave_name = 'Sick Leave');
INSERT INTO leave_type (leave_name, max_per_year)
    SELECT 'Casual Leave', 12 FROM dual WHERE NOT EXISTS (SELECT 1 FROM leave_type WHERE leave_name = 'Casual Leave');
INSERT INTO leave_type (leave_name, max_per_year)
    SELECT 'Privilege Leave', 15 FROM dual WHERE NOT EXISTS (SELECT 1 FROM leave_type WHERE leave_name = 'Privilege Leave');

-- Ensure existing employees are updated with the correct manager and designation
UPDATE employee SET designation_id = 1 WHERE emp_id = 'RW001';
UPDATE employee SET designation_id = 2, manager_id = 'RW001' WHERE emp_id = 'RW002';
UPDATE employee SET designation_id = 3, manager_id = 'RW002' WHERE emp_id = 'RW003';

-- Auto-assign manager for any ROLE_EMPLOYEE without a manager → assign to RW002 (Jane Manager)
UPDATE employee SET manager_id = 'RW002'
    WHERE manager_id IS NULL
    AND emp_id != 'RW001'
    AND emp_id != 'RW002'
    AND emp_id IN (SELECT e.emp_id FROM employee e JOIN users u ON e.user_id = u.user_id WHERE u.role = 'ROLE_EMPLOYEE');

-- Auto-assign manager for any ROLE_MANAGER without a manager → assign to RW001 (Admin)
UPDATE employee SET manager_id = 'RW001'
    WHERE manager_id IS NULL
    AND emp_id != 'RW001'
    AND emp_id IN (SELECT e.emp_id FROM employee e JOIN users u ON e.user_id = u.user_id WHERE u.role = 'ROLE_MANAGER');

-- Specifically assign employees Sneha (RW323, RW481) to Priya (RW656) so Priya has a team
UPDATE employee SET manager_id = 'RW656' WHERE emp_id IN ('RW323', 'RW481');
