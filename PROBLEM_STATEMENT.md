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
| C6 | **Fix the vulnerable library.** Update `commons-text` to a fixed version. | `before.txt` shows the old version, `after.txt` shows the new one. |
| C7 | **Prove it works.** All tests pass, and your Bruno checks from 5.1 give the same results. | `gradlew.bat test` is `BUILD SUCCESSFUL`. Screenshot of Bruno. |
| C8 | **Finish your notes.** Fill in `MIGRATION_NOTES.md` (template in section 8). | Every change is listed with the reason. |

### 5.3 Stretch tasks (for experienced developers, optional)
- **S1: Use automation.** Run the OpenRewrite recipe `org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_5` on a fresh copy of the starter and compare its result with your manual upgrade. What did it miss?
- **S2: Find the silent change.** Run the app with `spring-boot-properties-migrator` on the classpath. List every property it reports.
- **S3: Run on a newer JDK.** Build and run on JDK 21 or 25 using a Gradle toolchain.
- **S4: Scan.** Use a dependency scanner (for example OWASP Dependency-Check or your company tool) before and after, and report what changed.
- **S5: Harden the test.** Add a test proving that `PUT`, `DELETE` and other unlisted methods are not publicly open, and that `/h2-console` is still reachable.

## 6. Suggested time plan (4 hours total)

| Day | Time | Steps |
|---|---|---|
| **Day 1** | 30 min | Set up, run tests, baseline checks in Bruno, create branch, save `before.txt` (5.1) |
| | 30 min | Read section 7 and write the upgrade plan (C1) |
| | 60 min | Upgrade the build and fix the `jakarta` break (C2, C3) |
| **Day 2** | 60 min | Replace the security configuration (C4) |
| | 30 min | Properties and the vulnerable library (C5, C6) |
| | 30 min | Verify with tests and Bruno, write notes (C7, C8) |

Track your hours. **Stop at 4 hours** and submit what you have, with notes on what is left. Partial progress with good notes is better than a rushed finish.

### How to work through errors
1. Run `gradlew.bat compileJava`. Read the **first** error only.
2. Search the error text together with "Spring Boot 3 migration".
3. Fix one thing, rerun, and repeat. Commit after each task so you can go back.

## 7. Background facts you can use
- Spring Boot 2.7 reached the end of open-source support in June 2023, and Spring Framework 5.3 followed in 2024. Fixes for new vulnerabilities are no longer published for them.
- `commons-text` 1.9 is affected by CVE-2022-42889 ("Text4Shell"), fixed in 1.10.0. (This app only calls `WordUtils`, which is not the vulnerable code path, but scanners still flag the version and it should be upgraded.)
- Spring Boot 3 requires Java 17 or newer and moves from `javax.*` to `jakarta.*`.
- Spring Boot 3 ships Hibernate 6 and Spring Security 6, which remove classes and methods that were deprecated earlier.
- Official guide: search for "Spring Boot 3.0 Migration Guide" on the Spring Boot GitHub wiki.

## 8. Notes template (`MIGRATION_NOTES.md`)
```
# Upgrade notes
## Why we upgraded
## Versions: before -> after   (Spring Boot, Spring Framework, Hibernate, Spring Security, commons-text, Java)
## Changes I made (one line each: file, what changed, why)
## Errors I hit and how I fixed them
## Properties that were removed or renamed
## How I verified it (tests, Bruno checks)
## Not done / open questions
## Hours spent
```

## 9. Evaluation

| Criterion | Weight |
|---|---|
| App builds on Spring Boot 3.5.x and all tests pass | 35% |
| Behavior unchanged in Bruno (create, fetch, `400`s, `401` without credentials) | 20% |
| Security configuration migrated correctly (C4) | 15% |
| Properties and vulnerable library handled (C5, C6) | 10% |
| Quality of `MIGRATION_NOTES.md` (reasons, errors, verification) | 20% |

Stretch tasks earn recognition, not extra marks, so beginners are not at a disadvantage.

## 10. Rules
- Do not delete or weaken tests to make them pass. You may fix tests only where the framework change requires it, and you must say so in your notes.
- Do not remove security: unauthenticated requests must still get `401`.
- Do not copy another team member's solution. Pairing and discussion are welcome.
- Stay within 4 hours.

## 11. Submission
Push your branch `upgrade-boot-3` (or send the project folder without `build/`, `.gradle/` and `data/`), together with `MIGRATION_NOTES.md`, `before.txt`, `after.txt`, and a Bruno screenshot showing `201`, `200` and `401`. Check with your instructor how to submit.
