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

## Upgrade notes to myself

These notes record the upgrade in the order I worked through it: what changed, why it changed, and where I can check the reasoning. The exercise's own explanations are in [PROBLEM_STATEMENT.md](PROBLEM_STATEMENT.md), especially Appendix A.

### 1. Upgrade the build to Spring Boot 3.5 (C2)

**File:** `build.gradle`, in the `plugins` block.

```groovy
// Before: Spring Boot 2.7.x and an older dependency-management plugin
id 'org.springframework.boot' version '2.7.x'
id 'io.spring.dependency-management' version '1.0.x.RELEASE'

// Now:
id 'org.springframework.boot' version '3.5.16'
id 'io.spring.dependency-management' version '1.1.7'
```

The Spring Boot Gradle plugin supplies Boot's build tasks and works with Spring Boot 3.5. The dependency-management plugin lets Boot's dependency management choose compatible versions for Spring, Hibernate, Jackson, and other managed libraries. I generally should not add explicit versions for those managed libraries unless I have a specific reason.

The project targets Java 17 (`sourceCompatibility = '17'`), which is the minimum Java version for Spring Boot 3. The exercise asks for a Boot 3.5.x release; the exact patch version is the one in `build.gradle`.

**Why:** This is the framework upgrade itself, not just a library version bump. Boot 3 includes newer major versions of Spring Framework, Spring Security, and Hibernate, and those introduce source and configuration changes described below.

### 2. Change Java EE imports from `javax` to `jakarta` (C3)

**Files:** `src/main/java/com/example/onboarding/model/Employee.java` and `src/main/java/com/example/onboarding/controller/EmployeeController.java`.

For example:

```java
// Before
import javax.persistence.Entity;
import javax.validation.constraints.NotBlank;
import javax.validation.Valid;

// After
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.Valid;
```

Boot 3 uses Jakarta EE APIs. Their packages moved from `javax.*` to `jakarta.*`, so JPA annotations such as `@Entity` and validation annotations such as `@NotBlank` must use the new package names. The annotations still serve the same purpose; the package imports changed.

This is **not** a rule to replace every `javax` in a project. Some packages, such as `javax.sql`, are still part of Java. Change the Java EE APIs that moved, not unrelated JDK packages.

**How I chose the imports:** I checked what each annotation or type is for, then used the Boot 3 / Jakarta package for that API. An IDE's “Organize Imports” or “Add Import” action can find a class used by its simple name; I should verify that the suggested package starts with `jakarta` for these migrated APIs. I can also check the class's package in the official API documentation. The compiler helps catch missing or invalid imports, but it cannot tell me whether I chose the semantically correct class if multiple classes have the same name.

### 3. Replace the removed Spring Security adapter with beans

**File:** `src/main/java/com/example/onboarding/config/WebConfig.java`.

Spring Security 5 commonly put configuration in a class extending `WebSecurityConfigurerAdapter`, with overridden `configure` methods. That adapter was removed in Spring Security 6. The replacement is a plain `@Configuration` class that declares the objects Spring needs as `@Bean` methods.

A **bean** is an object Spring creates and manages. Here, the `SecurityFilterChain` bean tells Spring how to handle HTTP requests, and the `UserDetailsService` bean tells it how to look up users.

#### The `SecurityFilterChain` and the `->` lambda syntax

The old style configured `HttpSecurity` through chained method calls such as `.csrf().disable()` and `.authorizeRequests()`. The new style uses lambdas to configure individual settings:

```java
.csrf(csrf -> csrf.disable())
.authorizeHttpRequests(authorize -> authorize
    .requestMatchers(PathRequest.toH2Console()).permitAll()
    .anyRequest().authenticated())
```

The `->` is Java's lambda operator. A lambda is a small function passed to another method:

```java
csrf -> csrf.disable()
```

Read this as: “when Spring gives this function a CSRF configuration object, call `disable()` on it.” `csrf` is just a local parameter name chosen by the author; it is not a special Spring or Java keyword. I could write the same thing more verbosely as `csrfConfigurer -> csrfConfigurer.disable()`. Java infers the parameter type from the method being called, so I usually do not need to write the type myself.

This lambda shape makes sense because the new methods accept a configuration callback: Spring supplies the right configurer, and the callback describes how to change it. For example, `authorize -> ...` receives the authorization configurer. The callback can contain several chained settings, as in the multi-line authorization example above.

The complete bean has this general shape:

```java
@Bean
SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(authorize ->
            authorize.anyRequest().authenticated());
    return http.build();
}
```

`HttpSecurity` is the builder. `http.build()` finishes the configuration and returns the `SecurityFilterChain` that Spring Security will use. Returning it is essential; the method is not just changing a local object. The real method also configures CSRF, the H2 console, HTTP Basic, and frame options.

#### Keep the H2 console accessible with `PathRequest`

```java
.requestMatchers(PathRequest.toH2Console()).permitAll()
```

`requestMatchers` needs a rule describing which request(s) it should match. `PathRequest.toH2Console()` is a Spring Boot helper that returns a matcher for the H2 console path. Using the helper is safer than writing a string such as `"/h2-console/**"`: it follows Boot's configured H2 console path and avoids ambiguity about how a plain string path should be interpreted. The exercise specifically recommends this helper for the H2 console.

This rule permits the console. The next rule, `.anyRequest().authenticated()`, requires authentication for every other request, including the employee API.

#### Keep HTTP Basic and the H2 frame setting

```java
.httpBasic(Customizer.withDefaults())
.headers(headers ->
    headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
```

The new HTTP Basic method accepts a configuration callback. `Customizer.withDefaults()` is Spring Security's ready-made callback meaning “use the standard HTTP Basic settings; I have no custom settings to add.” It does not mean “skip HTTP Basic.” The import is `org.springframework.security.config.Customizer`.

The headers lambda keeps the previous `sameOrigin` frame policy. The H2 console uses frames, so allowing same-origin framing preserves its expected behavior without allowing framing from arbitrary sites.

The configuration also keeps CSRF disabled, as the original exercise configuration did. That preserves the exercise's API behavior, but disabling CSRF should be reconsidered for a real browser-facing application, especially if browsers automatically send the credentials.

#### Replace in-memory authentication configuration

The old `configure(AuthenticationManagerBuilder)` method used `inMemoryAuthentication()` to register the exercise user. Since the adapter and its override-based setup are gone, the new configuration exposes a `UserDetailsService` bean:

```java
@Bean
UserDetailsService userDetailsService() {
    return new InMemoryUserDetailsManager(
        User.withUsername("admin")
            .password("{noop}admin123")
            .roles("ADMIN")
            .build());
}
```

`UserDetailsService` is the Spring Security interface for loading user details during authentication. `InMemoryUserDetailsManager` is one implementation: it keeps the supplied user in application memory instead of loading users from a database. `User.withUsername(...)` builds the same kind of user record as the old configuration, and `.build()` produces the `UserDetails` object the manager stores. The username, password, and role remain the same as before.

`{noop}` tells Spring Security not to encode this exercise password. It is suitable only for this sample; real applications should store appropriately encoded passwords, not plain text.

#### How to know which imports to add

Imports name the Java classes used in the file. I used the classes required by the new method signatures and helpers, then let the IDE or compiler confirm their fully qualified names:

- `SecurityFilterChain` — the return type of the filter-chain bean.
- `UserDetailsService`, `User`, and `InMemoryUserDetailsManager` — the user lookup contract, user builder, and in-memory implementation.
- `Customizer` — supplies the default HTTP Basic configuration callback.
- `PathRequest` — Spring Boot's H2-console request matcher helper.
- `HttpSecurity` — the configuration builder passed into the filter-chain bean.

An IDE can add imports automatically, but I should check the package it selected. Official Javadocs and the Spring reference documentation show the fully qualified class names. If compilation says a type cannot be found, first check the spelling, package, and whether the corresponding Spring starter is present; do not guess an import from the class name alone.

### 4. Remove or rename unsupported configuration properties (C5)

**File:** `src/main/resources/application.properties`.

```properties
# Before
server.max-http-header-size=16KB

# After
server.max-http-request-header-size=16KB
```

The request-header-size setting was renamed. The new name makes clear that the limit is for incoming HTTP request headers, and retains the 16 KB value.

```properties
# Before
spring.jpa.hibernate.use-new-id-generator-mappings=true

# After
# Removed; Hibernate 6 uses the new ID generator mappings by default.
```

The Hibernate setting is no longer needed: Hibernate 6 uses the new ID generator mappings by default. Removing the old setting avoids keeping a property that no longer has a supported effect.

These two property migrations are also why the README records both the old and new names instead of just saying “cleaned up properties.”

### 5. Verification

After the source migrations, I ran:

```text
gradlew.bat test
```

The test suite completed with `BUILD SUCCESSFUL`. The tests cover creating and fetching an employee, input validation, and requiring authentication. I should run them again after any later code changes.

### Still to do

- **C6: Update Commons Text.** `build.gradle` still declares `org.apache.commons:commons-text:1.9`. The exercise identifies 1.9 as vulnerable and says the issue is fixed in 1.10.0 or later. This dependency has **not** been upgraded yet, so the overall upgrade exercise is not fully complete. After choosing a suitable fixed version, verify the resolved version with `gradlew.bat dependencyInsight --dependency commons-text --configuration runtimeClasspath`.

### References

- The provided [exercise instructions](PROBLEM_STATEMENT.md): Appendix A.2 (build), A.3 (Jakarta), A.4 (Security 6 migration and `PathRequest.toH2Console()`), A.5 (properties), and section 6 (Commons Text).
- [Spring Boot 3.0 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide) — official migration notes, including framework and Hibernate changes.
- [Spring Boot 3.5 application properties](https://docs.spring.io/spring-boot/3.5/appendix/application-properties/index.html) — property names and descriptions.
- [Spring Security Java configuration](https://docs.spring.io/spring-security/reference/servlet/configuration/java.html) and [request authorization](https://docs.spring.io/spring-security/reference/servlet/authorization/authorize-http-requests.html) — bean-based configuration and lambda DSL.
- [Spring Security in-memory authentication](https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/in-memory.html) — `InMemoryUserDetailsManager` and user setup.
- [Spring Boot `PathRequest` API](https://docs.spring.io/spring-boot/api/java/org/springframework/boot/autoconfigure/security/servlet/PathRequest.html) — helper used to match the H2 console.
