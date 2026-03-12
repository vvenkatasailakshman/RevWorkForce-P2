<<<<<<< HEAD
# RevWorkforce HRM Application

RevWorkforce is a full-stack monolithic Human Resource Management (HRM) web application.

## Prerequisites

- **Java 21**: Ensure you have JDK 21 installed.
- **Oracle Database**: The application is configured to use Oracle. Ensure an instance is running.
- **Maven**: The project includes a Maven wrapper (`mvnw`), so a local Maven installation is optional.

## Setup Instructions

### 1. Database Configuration
Open `src/main/resources/application.properties` and update the datasource credentials to match your local Oracle setup:
```properties
spring.datasource.url=jdbc:oracle:thin:@localhost:1521:xe
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD
```

### 2. Schema Initialization
Run the SQL script located at:
`src/main/resources/schema.sql`
This will create all necessary tables for employees, leaves, performance reviews, etc.

### 3. Build & Run
From the project root directory, run the following command:
```powershell
./mvnw spring-boot:run
```

## Accessing the Application

- **Web Interface**: [http://localhost:8080](http://localhost:8080)
- **Login**: Use the `users` table entries to login. (Default password for new employees created via Admin is `Welcome@123`).
- **REST API**: Detailed endpoints are available under `/api/**` for Postman testing.

## Project Structure

- `src/main/java/com/rev/app/controller`: Thymeleaf Web Controllers.
- `src/main/java/com/rev/app/rest`: REST API Controllers.
- `src/main/java/com/rev/app/service`: Business Logic.
- `src/main/java/com/rev/app/entity`: JPA Entities.
- `src/main/resources/templates`: HTML Views.
- `src/main/resources/static`: Assets (CSS/JS).
=======
# RevWorkForce-P2
RevWorkforce is a full-stack monolithic Human Resource Management (HRM) web application designed to streamline employee management, leave tracking, and performance review processes.  The application features responsive design, role-based dashboards, and automated notifications.
>>>>>>> 6deb37fa822a6942e24afb40b218b6f0353a2840
