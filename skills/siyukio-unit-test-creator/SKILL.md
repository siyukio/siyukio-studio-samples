---
name: siyukio-unit-test-creator
description: Create or modify controller-layer unit tests for Siyukio Spring Boot domain modules. Use when adding new controller tests, fixing or refactoring existing controller test classes, expanding endpoint scenario coverage, or aligning controller test bootstrap/configuration.
---

# siyukio-unit-test-creator

Create and modify test code for Siyukio server domain modules.

## Goal

Support two workflows:

- Create new controller tests when a context has no sufficient API-layer coverage.
- Modify existing controller tests when behavior changes, failures appear, or assertions/fixtures need repair.

## Scope

Create or update files under:

```text
{server-project-name}/{server-project-name}-{domain}/
├── pom.xml
└── src/test/
    ├── java/{package-path}/
    │   ├── TestApplication.java
    │   └── {domain}/
    │       ├── api/{Context}ControllerTest.java
    │       └── ... existing *Test.java files
    └── resources/
        ├── application.yml
        └── application-local.yml
```

## Use this skill when

- Add tests for new controller endpoint behavior.
- Repair failing controller tests after API contract or behavior changes.
- Refactor brittle tests (naming, fixtures, assertions) with no behavior regression.
- Extend scenario coverage for validation, not-found, authorization, and edge cases.

## Do not use this skill when

- Work is web/desktop/console side testing.
- Work is non-Java testing framework migration.
- The user explicitly asks for production logic changes unrelated to tests.

## Test style policy

- Modification mode: preserve style used by the target test class unless user requests migration.
- For any unit test, always use:
  - `@SpringBootTest(classes = TestApplication.class)`
  - `@ActiveProfiles("local")`
- Prefer real Spring beans via `@Autowired`.
- Never use any form of mock test (`Mockito`, mock beans, stubs/fakes used as mock replacements for business dependencies, etc.).
- Even if existing tests use mocks, new or modified tests must be converted to real-bean integration style.
- Keep assertions deterministic and scenario-focused.
- Do not rely on test execution order.

## Preconditions

1. Ensure target module exists: `{server-project-name}/{server-project-name}-{domain}`.
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
- Target mode:
  - `create` for new test classes.
  - `modify` for existing test classes.
- Target classes or methods when user provides explicit scope.

### 2) Discover current test conventions first

Inspect existing tests under `src/test/java/{package-path}/{domain}` and record:

- Existing class naming patterns (`{Context}ControllerTest`).
- Annotation style (`@SpringBootTest`, Mockito, profile usage).
- Assertion style (JUnit assertions, AssertJ, custom helpers).
- Fixture conventions (factory methods, builders, setup/teardown).

Use the discovered conventions to keep changes minimal and consistent.
If existing tests contain mock patterns, mark them for migration to non-mock, real-bean test style during this task.

### 3) Ensure test dependency

Update `{server-project-name}/{server-project-name}-{domain}/pom.xml`.
Add if missing:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

If the current module depends on `{server-project-name}-{domain}-client`, ensure the following dependencies are present (add if missing, do not duplicate):

```xml
<dependency>
    <groupId>{package-name}</groupId>
    <artifactId>{server-project-name}-{domain}-client</artifactId>
</dependency>
<dependency>
    <groupId>{package-name}</groupId>
    <artifactId>{server-project-name}-{domain}</artifactId>
    <scope>test</scope>
</dependency>
```

### 4) Ensure local test profile config (mandatory)

Copy both files from `{server-project-name}-bootstrap/src/main/resources` to
`{server-project-name}-{domain}/src/test/resources`:

- `application.yml`
- `application-local.yml`

If the target `src/test/resources` directory does not exist, create it first.

### 5) Ensure test bootstrap class (mandatory)

Create if missing:
`src/test/java/{package-path}/TestApplication.java`

```java
package {package-name};

import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "{package-name}")
public class TestApplication {
}
```

### 6) Build a scenario matrix before writing or changing tests

Cover at least:

- Positive path: create/get/update/list/delete success.
- Validation path: required-field or format failure.
- Not-found or disabled path.
- Permission/token path when endpoint/service requires user context.
- Edge path: empty result list, large page size, boundary values.

### 7) Apply mode-specific workflow

#### Mode A: Create tests

1. Create only `api/{Context}ControllerTest.java`.
2. Do not create `application/*Test.java` or `integration/*Test.java`.
3. Use unique test data (`name-{timestamp-or-suffix}` style) to avoid collisions.

#### Mode B: Modify tests

1. Read the existing target class and identify failing/obsolete scenarios.
2. Preserve test class structure and naming unless the user requests renaming.
3. Apply minimal edits:
   - Update request/response fixtures when contracts changed.
   - Tighten or correct assertions.
   - Replace flaky data/time/random behavior with deterministic values.
   - Add missing negative/edge scenarios required by the change.
4. Do not delete broad existing coverage unless it is invalid and replaced with equivalent coverage.

### 8) Controller test template (creation baseline)

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

### 9) Restrict output to controller tests

Do not generate `application/{Context}ServiceTest.java` or `integration/{Context}ClientTest.java`.
This skill only produces `api/{Context}ControllerTest.java` and updates existing controller tests.

### 10) Run verification

Run targeted tests first:

```bash
./mvnw test -pl {server-project-name}-{domain} -Dtest={Context}*Test
```

Then run full module tests:

From `{server-project-name}/` run:

```bash
./mvnw test -pl {server-project-name}-{domain}
```

If test setup changes broader modules, run a wider verification sweep:

```bash
./mvnw test
```

API unit-test retry rule:

- If API unit tests fail, fix and rerun.
- Maximum retry attempts: 3.
- If still not passing after 3 attempts, stop execution and output clear reasons:
  - failing command(s)
  - key error messages
  - why the issue could not be resolved within 3 attempts

## Output checklist

- `spring-boot-starter-test` dependency present.
- All unit tests use `@SpringBootTest(classes = TestApplication.class)` and `@ActiveProfiles("local")`.
- Test suites have `TestApplication` and `application-local.yml` in place.
- Created/modified tests cover positive + negative + edge scenarios.
- Only controller tests are created or modified; no service/client test files are generated.
- Existing test style is preserved in modification mode unless migration is explicitly requested.
- Test changes are minimal, deterministic, and readable.
- Maven tests pass for the target module.
