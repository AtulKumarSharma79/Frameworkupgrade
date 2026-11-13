# Onboarding Tracker

Working REST API on **Spring Boot 4.1.2** and **Java 25** with embedded H2 and HTTP Basic security.

The migration uses Jakarta Persistence and Validation APIs, Spring Security's `SecurityFilterChain` configuration, and the `spring-boot-starter-webmvc` starter. The removed `spring.jpa.hibernate.use-new-id-generator-mappings` setting was dropped; `server.max-http-header-size` was replaced by `server.max-http-request-header-size`.

## Run
```
gradlew.bat bootRun        (macOS/Linux: ./gradlew bootRun)
gradlew.bat test
```
Credentials for the API: `admin` / `admin123` (HTTP Basic).

## APIs
- `POST /api/employees` with `{ "name": "...", "role": "...", "onboardingDate": "yyyy-MM-dd" }` returns `201`. The name is stored in Title Case.
- `GET /api/employees` returns `200` with all employees.
- Invalid input returns `400`. No credentials returns `401`.
