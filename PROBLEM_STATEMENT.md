# Exercise: Upgrade a Spring Boot Application to a New Major Version

## 1. Overview
You are given a working REST application (the Onboarding Tracker from the previous exercise) that runs on **Spring Boot 2.7**. That version is **out of open-source support**, so it no longer receives security fixes, and the libraries it brings in are flagged by vulnerability scanners.

**Your job is to upgrade the application to Spring Boot 3.5.x** so that it behaves exactly as before, builds cleanly, and passes its tests. This is a *major* version upgrade, so a version bump alone will not work: some code and configuration must change.

| | |
|---|---|
| **Starting point** | Spring Boot 2.7.18, Spring Framework 5.3, Hibernate 5, Spring Security 5.7 |
| **Target** | Spring Boot 3.5.x (Spring Framework 6, Hibernate 6, Spring Security 6) |
| **Java** | 17 or newer (Spring Boot 3 requires 17+; 21 or 25 is fine) |
| **Build tool** | Gradle (wrapper included) |
| **API client** | Bruno |
| **Time budget** | **4 hours in total, spread over 2 days** (plan below) |


## 3. Learning objectives
By the end you will be able to:

- Read a build or compile error from an upgrade and trace it to a breaking change.
- Migrate `javax.*` imports to `jakarta.*`.
- Replace removed Spring Security 5 configuration with the Spring Security 6 style.
- Find and fix configuration properties that changed or were removed.


## 4. Prerequisites
- **JDK 17 or newer.** Check with `java -version`.
- **Bruno** and an IDE (IntelliJ IDEA, or VS Code with the Extension Pack for Java).

## 5. Your task

### 5.1 Before you change anything (baseline)
1. Clone the starter project and run the tests: `gradlew.bat test`. They must pass.
2. Start the app. In Bruno, call both APIs with **HTTP Basic** :
   - `POST /api/employees` with `{ "name": "asha RAO", "role": "Engineer", "onboardingDate": "2026-11-10" }` returns `201` and the name comes back as `Asha Rao`.
   - `GET /api/employees` returns `200` and the list.
   - Both return `401` without credentials. [Check if this line is required?].
3. Create a git branch.

   
If port 8080 is busy, add `--args="--server.port=8081"` to `bootRun`.

### 5.2 Core tasks (everyone)
The application must keep the same behavior after each of these.

| # | Task | What "done" looks like |
|---|---|---|

| C2 | **Upgrade the build.** Move the Spring Boot Gradle plugin to 3.5.x and the dependency-management plugin to a compatible version. 
| C3 | **Fix the `javax` to `jakarta` break.** Spring Boot 3 uses Jakarta EE 9+, so the package names changed. | No `javax.persistence` or `javax.validation` left in `src`. App compiles. |

| C5 | **Clean up configuration properties.** Some properties in `application.properties` are no longer supported. | None of the old, unsupported properties remain, and you list them in your notes. |
| C6 | **Fix the vulnerable library.** Update `commons-text` to a fixed version. 
| C7 | **Prove it works.** All tests pass, and your Bruno checks from 5.1 give the same results. 


### 5.3 Stretch tasks ( optional)
- **S1: Use automation.** Run the OpenRewrite recipe `org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_5` on a fresh copy of the starter and compare its result with your manual upgrade. What did it miss?
- **S2: Find the silent change.** Run the app with `spring-boot-properties-migrator` on the classpath. List every property it reports.
- **S3: Run on a newer JDK.** Build and run on JDK 21 or 25 using a Gradle toolchain.
- **S4: Scan.** Use a dependency scanner (for example OWASP Dependency-Check or your company tool) before and after, and report what changed.
- **S5: Harden the test.** Add a test proving that `PUT`, `DELETE` and other unlisted methods are not publicly open, and that `/h2-console` is still reachable.



## 6. Background facts you can use
- Spring Boot 2.7 reached the end of open-source support in June 2023, and Spring Framework 5.3 followed in 2024. Fixes for new vulnerabilities are no longer published for them.
- `commons-text` 1.9 is affected by CVE-2022-42889 ("Text4Shell"), fixed in 1.10.0. (This app only calls `WordUtils`, which is not the vulnerable code path, but scanners still flag the version and it should be upgraded.)
- Spring Boot 3 requires Java 17 or newer and moves from `javax.*` to `jakarta.*`.
- Spring Boot 3 ships Hibernate 6 and Spring Security 6, which remove classes and methods that were deprecated earlier.
- Official guide: search for "Spring Boot 3.0 Migration Guide" on the Spring Boot GitHub wiki.


