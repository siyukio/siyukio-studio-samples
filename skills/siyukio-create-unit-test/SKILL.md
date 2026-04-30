---
name: siyukio-create-unit-test
description: Create or update integration-style unit tests for Siyukio Spring Boot domain modules by using @SpringBootTest, @ActiveProfiles("local"), and real Spring beans (no mocks). Use when adding controller/application/infrastructure test classes, test bootstrap classes, local test configuration, and CRUD/validation/authorization/edge-case assertions.
---

# siyukio-create-unit-test

Generate test code for Siyukio server domain modules.

## Scope

Create or update files under:

```text
{project-name}/{project-name}-domain-{domain}/
├── pom.xml
└── src/test/
    ├── java/{package-path}/
    │   ├── TestApplication.java
    │   └── {domain}/
    │       ├── api/{Context}ControllerTest.java
    │       ├── application/{Context}ServiceTest.java      (optional)
    │       └── infrastructure/{Context}ClientTest.java    (optional)
    └── resources/
        ├── application.yml
        └── application-local.yml
```

## Use this skill when

- Add tests for controller CRUD or query endpoints.
- Add tests for application methods that are not covered by controller tests.
- Add tests for infrastructure clients that call external dependencies.
- Add or repair local test bootstrap and test profile configuration.

## Do not use this skill when

- Work is web/desktop/console (not supported in this project yet).
- Work is domain/API/application implementation without testing scope.
- Pure unit tests with mocks are required (this skill enforces real-bean integration style).

## Test policy

- Do not use mocks.
- Inject real objects via `@Autowired`.
- Use `@SpringBootTest(classes = TestApplication.class)`.
- Use `@ActiveProfiles("local")`.
- Keep assertions deterministic and scenario-focused.

## Preconditions

1. Ensure target module exists: `{project-name}/{project-name}-domain-{domain}`.
2. Ensure local test environment variables are available from `AGENTS.md`:
   - `SIYUKIO_DB_MASTER_URL`
   - `SIYUKIO_DB_MASTER_USERNAME`
   - `SIYUKIO_DB_MASTER_PASSWORD`
3. Ensure test package root exists: `src/test/java/{package-path}/{domain}`.

## Execution workflow

### 1) Normalize inputs

Extract and normalize:

- `{domain}`: module suffix in kebab-case (example: `user-management`)
- `{Domain}`: PascalCase variant used in class names (example: `UserManagement`)
- `{Context}`: PascalCase context (example: `User`)
- `{context}`: camelCase variable (example: `user`)

### 2) Ensure test dependency

Update `{project-name}/{project-name}-domain-{domain}/pom.xml`.
Add if missing:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

### 3) Ensure local test profile config

Copy both files from `{project-name}-bootstrap/src/main/resources` to
`{project-name}-domain-{domain}/src/test/resources`:

- `application.yml`
- `application-local.yml`

If the target `src/test/resources` directory does not exist, create it first.

### 4) Ensure test bootstrap class

Create if missing:
`src/test/java/{package-path}/TestApplication.java`

```java
package {package-name};

import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "{package-name}")
public class TestApplication {
}
```

### 5) Build a scenario matrix before writing tests

Cover at least:

- Positive path: create/get/update/list/delete success.
- Validation path: required-field or format failure.
- Not-found or disabled path.
- Permission/token path when endpoint/service requires user context.
- Edge path: empty result list, large page size, boundary values.

### 6) Generate controller tests first

Create or update:
`src/test/java/{package-path}/{domain}/api/{Context}ControllerTest.java`

Rules:

- Call controller methods directly.
- Prepare data in each test or helper methods.
- Use unique test data (suffix/prefix) to avoid collisions.
- Assert both identity fields and business fields.

Template:

```java
package {package-name}.{domain}.api;

import {package-name}.TestApplication;
import io.github.siyukio.tools.api.dto.PageRequest;
import io.github.siyukio.tools.api.dto.PageResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("local")
class {Context}ControllerTest {

    @Autowired
    private {Context}Controller {context}Controller;

    @Test
    void createAndGetShouldSucceed() {
        {Context}Request request = new {Context}Request(null, "name-1", "description-1");
        {Context}Response created = {context}Controller.create(request);

        assertNotNull(created.id());

        {Context}Response loaded = {context}Controller.get(created.id());
        assertEquals(created.id(), loaded.id());
        assertEquals("name-1", loaded.name());
    }

    @Test
    void listShouldReturnCreatedData() {
        {context}Controller.create(new {Context}Request(null, "name-2", "description-2"));
        PageResponse<{Context}Response> response = {context}Controller.list(new PageRequest(0, 10));

        assertNotNull(response);
        assertTrue(response.getTotal() >= 1);
    }
}
```

### 7) Generate service/client tests only when needed

Generate `application/{Context}ServiceTest.java` only for methods that are not exercised by controller tests.
Generate `infrastructure/{Context}ClientTest.java` only when client behavior needs direct verification.

### 8) Run verification

From `siyukio-studio-server/` run:

```bash
./mvnw test -pl {project-name}/{project-name}-domain-{domain}
```

If test setup changes broader modules, run a wider verification sweep:

```bash
./mvnw test
```

## Output checklist

- `spring-boot-starter-test` dependency present.
- `TestApplication` exists and compiles.
- Controller tests cover positive + negative + edge scenarios.
- Optional service/client tests are added only when controller coverage is insufficient.
- Maven tests pass for the target module.
