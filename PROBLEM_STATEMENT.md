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
| C2 | **Upgrade the build.** Move the Spring Boot Gradle plugin to 3.5.x and the dependency-management plugin to a compatible version. | |
| C3 | **Fix the `javax` to `jakarta` break.** Spring Boot 3 uses Jakarta EE 9+, so the package names changed. | No `javax.persistence` or `javax.validation` left in `src`. App compiles. |
| C5 | **Clean up configuration properties.** Some properties in `application.properties` are no longer supported. | None of the old, unsupported properties remain, and you list them in your notes. |
| C6 | **Fix the vulnerable library.** Update `commons-text` to a fixed version. | |
| C7 | **Prove it works.** All tests pass, and your Bruno checks from 5.1 give the same results. | |

**Stuck on how to do a task?** Appendix A at the end of this document has generic before-and-after examples, hints and common errors. The examples use different class names from your project, so you still need to apply them yourself.

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




---

## Appendix A: Upgrade guide with examples

The examples below use made-up classes (such as `WebConfig`) so you can see the pattern. Apply the same pattern to your own code.

### A.1 How to approach any upgrade
1. **Change one thing at a time**, then build. Do not fix ten things and then compile.
2. **Commit after every task** (for example `git commit -m "C3: javax to jakarta"`). If you break something, `git diff` or `git restore` gets you back.
3. **Read only the first compile error.** Later errors are often caused by the first one.
4. **Tests are your safety net.** If a test fails after your change, the change is wrong or incomplete. Do not edit the test.
5. **If you are stuck for 15 minutes, ask a colleague or the instructor.** Asking is part of the job.

### A.2 Task C2: upgrade the build file
Only the plugin versions change. Spring Boot manages the versions of its own libraries (Spring, Hibernate, Jackson and so on), so you should not list those yourself.

```groovy
// BEFORE (build.gradle)
plugins {
    id 'org.springframework.boot' version '2.7.x'
    id 'io.spring.dependency-management' version '1.0.x.RELEASE'
}

// AFTER
plugins {
    id 'org.springframework.boot' version '3.5.x'
    id 'io.spring.dependency-management' version '1.1.x'
}
```

Use the real latest 3.5 patch version from https://spring.io/projects/spring-boot. Then run:

```bash
gradlew.bat compileJava
```

Compile errors at this stage are expected. They are the work for the next tasks.

### A.3 Task C3: `javax` becomes `jakarta`
Java EE moved to the Jakarta EE project, and the package names changed. Only the **EE** packages change.

```java
// BEFORE
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.Valid;

// AFTER
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.Valid;
```

Tips:
- Use your IDE's **Find in Files / Replace in Files** and replace `javax.persistence` with `jakarta.persistence`, then `javax.validation` with `jakarta.validation`. Do these two separately and check the preview.
- **Do not** blindly replace every `javax.`. Packages such as `javax.sql` and `javax.crypto` belong to the JDK itself and stay as they are.
- To check you got them all, search the project for `javax.`. On Windows: `findstr /s /i "javax\." src\*.java`. On macOS or Linux: `grep -r "javax\." src`.

### A.4 Spring Security 6: what you will meet while compiling
Once the build and the `jakarta` imports are fixed, the compiler will stop at the security configuration class. Spring Security 5 asked you to *extend a class* and override methods. Spring Security 6 asks you to *define beans*. A **bean** is just an object that Spring creates and manages for you; you declare it with `@Bean`.

Use this table to translate the old style into the new one:

| Old (Spring Security 5) | New (Spring Security 6) |
|---|---|
| `extends WebSecurityConfigurerAdapter` | A plain class with `@Bean` methods |
| `configure(HttpSecurity http)` | A `SecurityFilterChain` bean that takes `HttpSecurity` and returns `http.build()` |
| `authorizeRequests()` | `authorizeHttpRequests(...)` |
| `antMatchers(...)` | `requestMatchers(...)` |
| `.csrf().disable()` and `.and()` chaining | One lambda per setting, for example `.csrf(csrf -> csrf.disable())` |
| `.httpBasic()` | `.httpBasic(Customizer.withDefaults())` |
| `configure(AuthenticationManagerBuilder)` with `inMemoryAuthentication()` | A `UserDetailsService` bean, for example an `InMemoryUserDetailsManager` |

The shape of the new class is:

```java
@Configuration
@EnableWebSecurity
public class WebConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // ... your rules, using the lambda style from the table ...
        return http.build();
    }

    @Bean
    UserDetailsService userDetailsService() {
        // ... return a user manager holding the same user as before ...
    }
}
```

Hints:
- Keep every behavior of the old class: the same URLs allowed, the same user, and the H2 console still reachable.
- If the app fails at **startup** with a message about patterns and "Spring MVC", the plain-string matcher for the H2 console is the cause. Spring Boot provides a ready-made helper for exactly this case, called `PathRequest.toH2Console()` (in `org.springframework.boot.autoconfigure.security.servlet`). Look it up.
- Your tests will tell you if you accidentally opened or closed the API.

### A.5 Task C5: properties that changed
The danger here is that **a removed property does not break the build or the startup**. It is silently ignored, so you must look for it on purpose.

How to find them:
1. Read each line of `src/main/resources/application.properties`.
2. For every line that sets a `server.*`, `spring.*` or `spring.jpa.*` property, search for the name together with "Spring Boot 3". Check the **Spring Boot 3.0 Migration Guide** and the **3.0 release notes**.
3. Optional helper: add the properties migrator to the build, start the app, and read the warnings it prints in the log.
   ```groovy
   dependencies {
       runtimeOnly 'org.springframework.boot:spring-boot-properties-migrator'
   }
   ```
   Remove it again when you are done. It is a diagnostic tool and should not ship. It may not report every removed property, so still check the migration guide.
4. Write down each property you remove or rename, and why you decided that.

Hibernate 6 also changed some behavior around generated ids and SQL dialects, so be suspicious of any property with `hibernate` in its name.

### A.6 Task C6: find and replace the vulnerable library
See which version is actually used, and which libraries pull it in:

```bash
gradlew.bat dependencyInsight --dependency commons-text --configuration runtimeClasspath
```

Libraries that Spring Boot manages (Spring, Jackson, Hibernate) are upgraded for you by the plugin. Libraries you declared yourself with an explicit version, like the one below, are **your** responsibility.

```groovy
// BEFORE
implementation 'org.apache.commons:commons-text:1.9'

// AFTER (use a current, fixed version from https://central.sonatype.com or https://mvnrepository.com)
implementation 'org.apache.commons:commons-text:1.x.x'
```

Run the `dependencyInsight` command again to confirm the new version is the one in use.

### A.7 Common errors and what they usually mean

| What you see | Usual cause | What to try |
|---|---|---|
| `package javax.persistence does not exist` | Boot 3 no longer includes the old packages | Task C3: change to `jakarta.persistence` |
| `cannot find symbol: class WebSecurityConfigurerAdapter` | It was removed in Spring Security 6 | See A.4: use beans |
| `cannot find symbol: method antMatchers` or `authorizeRequests` | Renamed in Spring Security 6 | Use `requestMatchers` and `authorizeHttpRequests` |
| `Dependency requires at least JVM runtime version 17` (or similar) | You are running Gradle on an older Java | Install JDK 17 or newer and check `java -version` and `JAVA_HOME` |
| App fails at startup, message mentions Spring MVC and patterns | A plain-string URL matcher is ambiguous for the H2 console | See the hint in A.4 |
| Test gets `403` instead of `201` | CSRF protection is on, or a security rule changed | Compare your new rules with the old ones |
| Test gets `401` when it should work | Credentials or the user bean is missing or wrong | Check the `UserDetailsService` bean |
| The app starts but `GET /api/employees` returns the wrong thing | Something changed silently | Re-run your baseline checks from section 5.1 and compare |

### A.8 Glossary for people new to Java
- **Dependency:** a library your project uses, declared in `build.gradle`.
- **Bean:** an object that Spring creates for you. `@Bean` marks a method that makes one.
- **Annotation:** a label like `@Entity` or `@Valid` that tells the framework how to treat a class or method.
- **Major version:** a big release (2.x to 3.x) that is allowed to remove old features. Minor and patch updates normally are not.
- **End of support:** the vendor no longer publishes fixes, including security fixes.
- **Breaking change:** a change that makes old code stop working until you update it.

### A.9 Final checklist before you finish
- [ ] `gradlew.bat test` ends with `BUILD SUCCESSFUL`.
- [ ] No `javax.persistence` or `javax.validation` left in `src`.
- [ ] No `WebSecurityConfigurerAdapter` left in `src`.
- [ ] The Spring Boot plugin version is 3.5.x in `build.gradle`.
- [ ] Your Bruno checks from section 5.1 give the same results as before the upgrade.
- [ ] The old, unsupported properties are gone, and `commons-text` is upgraded.
