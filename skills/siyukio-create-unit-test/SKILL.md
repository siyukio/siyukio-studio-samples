---
name: siyukio-create-unit-test
description: Create or refine integration-style unit tests for Siyukio Spring Boot domain modules. Use when adding or updating controller/service/client tests under src/test, wiring SpringBootTest with the local profile, validating request/response contracts, and covering positive/negative/edge scenarios without mocks.
---

# siyukio-create-unit-test

Create test classes for one Siyukio domain module with real Spring beans.

## Scope

Work inside one domain module:

```
{project-name}/{project-name}-domain-{domain}/
└── src/test/
    ├── java/{package-path}/
    │   ├── Test{Domain}Application.java
    │   └── {domain}/
    │       ├── api/{Context}ControllerTest.java
    │       ├── application/{Context}ServiceTest.java
    │       └── infrastructure/{Context}ClientTest.java
    └── resources/application-local.yml
```

## Use this skill when

- Add tests for new API endpoints or service logic in a Siyukio domain module.
- Fill missing positive/negative/edge cases in existing tests.
- Standardize module test bootstrapping (`@SpringBootTest` + `@ActiveProfiles("local")`).

## Do not use this skill when

- Work is API implementation. Use `$siyukio-create-api`.
- Work is application orchestration implementation. Use `$siyukio-create-application`.
- Work is domain model design only. Use `$siyukio-create-domain`.

## Mandatory rules

- Use real Spring beans; do not use Mockito, `@MockBean`, or hand-written stubs.
- Use `@SpringBootTest` and `@ActiveProfiles("local")`.
- Keep tests deterministic and isolated (prepare and verify their own data).

## Preconditions

- Target module exists: `{project-name}/{project-name}-domain-{domain}`.
- Local DB variables are available (from `AGENTS.md` Local Environment Configuration).
- Module has test dependency:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

## Execution workflow

### 1) Normalize inputs

Extract and normalize:

- `{domain}`: module/domain name.
- `{Domain}`: PascalCase form for test entry class.
- `{Context}` / `{context}`: test target context (for class and variable names).
- Target layer(s): `api`, `application`, `infrastructure`.

### 2) Prepare test profile config

Create or update:

`{project-name}/{project-name}-domain-{domain}/src/test/resources/application-local.yml`

Rules:

1. Read required environment variables from `AGENTS.md`.
2. Keep datasource and required runtime settings valid for local integration tests.
3. Replace unresolved placeholders when concrete values are required in this repository.

Example:

```yaml
spring:
  datasource:
    postgres:
      master:
        url: ${SIYUKIO_DB_MASTER_URL}
        username: ${SIYUKIO_DB_MASTER_USERNAME}
        password: ${SIYUKIO_DB_MASTER_PASSWORD}
```

### 3) Create test bootstrap class (once per module)

Create if missing:

`src/test/java/{package-path}/Test{Domain}Application.java`

Template:

```java
package {package-name};

import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "{package-name}")
public class Test{Domain}Application {
}
```

### 4) Generate or refine controller tests (preferred)

Target:

`src/test/java/{package-path}/{domain}/api/{Context}ControllerTest.java`

Guidelines:

- Inject `{Context}Controller` with `@Autowired`.
- Call controller methods directly.
- Cover at least:
  - success path,
  - invalid input or not-found path,
  - pagination/list boundary when list API exists.
- Assert both business fields and structural contracts (id, size/count, etc.).

Template:

```java
@SpringBootTest(classes = Test{Domain}Application.class)
@ActiveProfiles("local")
class {Context}ControllerTest {

    @Autowired
    private {Context}Controller {context}Controller;

    @Test
    void create_shouldPersistAndReturnResponse() {
        {Context}Request request = new {Context}Request(null, "Test Name", "Description");

        {Context}Response response = {context}Controller.create(request);

        assertNotNull(response.id());
        assertEquals("Test Name", response.name());
    }
}
```

### 5) Add service tests only for non-API logic

Target:

`src/test/java/{package-path}/{domain}/application/{Context}ServiceTest.java`

Rule:

- If a service method is already fully covered through controller tests, do not duplicate it.
- Add service tests for internal orchestration or non-API behaviors only.

### 6) Add infrastructure client tests when applicable

Target:

`src/test/java/{package-path}/{domain}/infrastructure/{Context}ClientTest.java`

Guidelines:

- Inject real client bean with `@Autowired`.
- Build real command objects.
- Assert returned result contract and key fields.

### 7) Verify

From repository root:

```bash
./mvnw -pl {project-name}/{project-name}-domain-{domain} -DskipTests compile
./mvnw -pl {project-name}/{project-name}-domain-{domain} test
```

If only one test class is touched, optionally run:

```bash
./mvnw -pl {project-name}/{project-name}-domain-{domain} -Dtest={Context}ControllerTest test
```

Then confirm:

- Tests pass with `local` profile.
- No mocks are introduced.
- New tests cover positive + negative + edge scenarios.

## Conventions checklist

- Test entry class: `Test{Domain}Application`
- API test package: `{package-name}.{domain}.api`
- Service test package: `{package-name}.{domain}.application`
- Infrastructure test package: `{package-name}.{domain}.infrastructure`
- Assertions: `org.junit.jupiter.api.Assertions`
- Pagination: `PageRequest` / `PageResponse` when list APIs exist
