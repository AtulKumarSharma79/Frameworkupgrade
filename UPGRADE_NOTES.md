# Spring Boot Upgrade Notes

## 1. Upgrade Summary

- Original Spring Boot version: 2.7.18
- Upgraded Spring Boot version: 3.5.9
- Java version: 21
- Gradle version: 8.14

## 2. Build Configuration (C2)

- Updated the Spring Boot Gradle plugin from 2.7.18 to 3.5.9.
- Updated the dependency-management plugin to a compatible version, if applicable.

## 3. Jakarta Migration (C3)

Replaced the following imports:

- `javax.persistence.*` → `jakarta.persistence.*`
- `javax.validation.*` → `jakarta.validation.*`

Updated `Employee.java` and `EmployeeController.java`.

## 4. Spring Security Migration

- Removed `WebSecurityConfigurerAdapter`.
- Replaced the old security configuration with a `SecurityFilterChain` bean.
- Replaced the old authentication configuration with a `UserDetailsService` bean.
- Preserved HTTP Basic authentication and the existing admin user.
- Preserved authentication requirements for the employee APIs.

## 5. Configuration Properties (C5)

| Old Property | Action | Reason |
|---|---|---|
| `server.max-http-header-size` | Renamed to `server.max-http-request-header-size` | Property renamed in Spring Boot 3 |
| `spring.jpa.hibernate.use-new-id-generator-mappings` | Removed | Legacy identifier-generation property no longer supported |

## 6. Database Compatibility

- Encountered an H2 database file-format incompatibility after upgrading.
- Backed up the original database and recreated the workshop database using the newer H2 version.

## 7. Verification

- Java compilation: Passed
- Application startup: Passed
- POST `/api/employees`: Passed
- GET `/api/employees`: Passed
- Gradle test task: Passed
