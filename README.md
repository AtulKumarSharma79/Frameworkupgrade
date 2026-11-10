# Onboarding Tracker (Spring Boot 2.7 starter for the upgrade exercise)

Working REST API on **Spring Boot 2.7.18** with embedded H2 and HTTP Basic security. Your task is to upgrade it to Spring Boot 3.5.x. Read [PROBLEM_STATEMENT.md](PROBLEM_STATEMENT.md) first.

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
