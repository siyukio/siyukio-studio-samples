---
name: siyukio-create-unit-test
description: "Generate unit tests for Siyukio-based Spring Boot applications"
triggers:
  - "add unit test"
  - "create unit test"
  - "new unit test"
  - "add test"
  - "create test"
  - "new test"
  - "write test"
---

<Purpose>
Generate unit tests for Siyukio Spring Boot applications. This skill focuses on testing API endpoints by directly injecting Controller objects and calling their methods.

Test structure under domain module:

```
src/test/
├── java/{package-path}/
│   ├── Test{Domain}Application.java   (Spring Boot test entry)
│   └── {domain}/
│       ├── application/
│       │   └── {Context}ServiceTest.java
│       ├── api/
│       │   └── {Context}ControllerTest.java
│       └── infrastructure/
│           └── {Context}ClientTest.java
└── resources/
    └── application-local.yml           (local test configuration)
```

</Purpose>

<Use_When>

- Creating unit tests for API endpoints
- Testing Application Service layer methods
- Validating API request/response DTOs
- Testing CRUD operations with database integration
- Verifying authorization and parameter validation

</Use_When>

<Prerequisites>

- Target domain module must exist: `{project-name}/{project-name}-domain-{domain}/`
- Target test location: `src/test/java/{package-path}/{domain}/`
- Local test configuration in `src/test/resources/application-local.yml`

</Prerequisites>

**RULES**:

1. **DO NOT use mocks** - All test objects must be real instances injected via `@Autowired`
2. All tests must use `@SpringBootTest` with `@ActiveProfiles("local")` for full integration testing

<Execution_Protocol>

## Step 1: Add Test Dependencies to Module pom.xml (if not exists)

Location:
`{project-name}/{project-name}-domain-{domain}/pom.xml`

Add the following dependency inside `<dependencies>` section:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

## Step 2: Prepare Local Test Configuration (if not exists)

> **Note**: Environment variables for local testing should be obtained from the **Local Environment Configuration** section in `AGENTS.md`. Refer to that file for the required variables and their expected values.

1. **Read `AGENTS.md`** and extract the **Local Environment Configuration** table
2. **Read the existing `application-local.yml` template** (if exists) to identify any variable placeholders (e.g., `${SIYUKIO_DB_MASTER_URL}`)
3. **Replace variable placeholders** with actual values from the AGENTS.md configuration table

Location:
`{project-name}/{project-name}-domain-{domain}/src/test/resources/application-local.yml`

Example (with placeholders):

```yaml
spring:
  datasource:
    postgres:
      master:
        url: ${SIYUKIO_DB_MASTER_URL}
        username: ${SIYUKIO_DB_MASTER_USERNAME}
        password: ${SIYUKIO_DB_MASTER_PASSWORD}
```

## Step 3: Generate Spring Boot Test Entry (if not exists)

Location:
`{project-name}/{project-name}-domain-{domain}/src/test/java/{package-path}/Test{Domain}Application.java`

One entry per module. Only create if not exists.

```java
package {package-name};

import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "{package-name}")
public class TestApplication {
}
```

## Step 4: Analyze Code for Test Generation

Based on the API Controller, Application Service, and DTOs, identify:

- Test scenarios (positive, negative, edge cases)
- Required test data
- Assertions for response validation
- Dependencies on other services
- Database state requirements

## Step 5: Generate API Controller Test Class (Optional)

Location:
`{project-name}/{project-name}-domain-{domain}/src/test/java/{package-path}/{domain}/api/{Context}ControllerTest.java`

Inject Controller directly and call its methods. Each test method should:

1. Setup test data if needed
2. Call controller method directly
3. Assert the response

```java
package {package-name}.{domain}.api;

import {package-name}.TestApplication;
import {package-name}.{domain}.api.{Context}Controller;
import {package-name}.{domain}.api.dto.{Context}Request;
import {package-name}.{domain}.api.dto.{Context}Response;
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
    void testCreate{Context}() {
        {Context}Request request = new {Context}Request(null, "Test Name", "Description");
        {Context}Response response = {context}Controller.create(request);

        assertNotNull(response.id());
        assertEquals("Test Name", response.name());
    }

    @Test
    void testGet{Context}() {
        // Create test data first
        {Context}Request createRequest = new {Context}Request(null, "Test Name", "Description");
        {Context}Response created = {context}Controller.create(createRequest);

        // Test GET endpoint
        {Context}Response response = {context}Controller.get(created.id());

        assertEquals(created.id(), response.id());
        assertEquals("Test Name", response.name());
    }

    @Test
    void testUpdate{Context}() {
        // Create test data first
        {Context}Request createRequest = new {Context}Request(null, "Original Name", "Description");
        {Context}Response created = {context}Controller.create(createRequest);

        // Test UPDATE endpoint
        {Context}Request updateRequest = new {Context}Request(created.id(), "Updated Name", "Updated Description");
        {Context}Response updated = {context}Controller.update(updateRequest);

        assertEquals(created.id(), updated.id());
        assertEquals("Updated Name", updated.name());
        assertEquals("Updated Description", updated.description());
    }

    @Test
    void testList{Context}() {
        // Create test data
        {context}Controller.create(new {Context}Request(null, "Test 1", "Description 1"));
        {context}Controller.create(new {Context}Request(null, "Test 2", "Description 2"));

        // Test LIST endpoint
        PageResponse<{Context}Response> response = {context}Controller.list(new PageRequest(0, 10));

        assertNotNull(response);
        assertTrue(response.getTotal() >= 2);
    }
}
```

## Step 6: Generate Application Service Test Class (Optional)

For more granular testing of service methods not exposed via API:

Location:
`{project-name}/{project-name}-domain-{domain}/src/test/java/{package-path}/{domain}/application/{Context}ServiceTest.java`

```java
package {package-name}.{domain}.application;

import {package-name}.TestApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("local")
class {Context}ServiceTest {

    @Autowired
    private {Context}Service {context}Service;

    // TODO: Add tests for service methods not called by controllers
}
```

> **Note**: If an Application Service method is called by a Controller, skip creating a unit test for that method. Instead, create the unit test for the corresponding Controller method (Step 5). Only generate tests for service methods that are not exposed via API.

## Step 7: Generate Infrastructure Client Test Class (Optional)

For testing external service clients. Each domain can have its own infrastructure layer. Use `@SpringBootTest` with real objects injected via `@Autowired`. Do NOT use mocks.

Location:
`{project-name}/{project-name}-domain-{domain}/src/test/java/{package-path}/{domain}/infrastructure/{Context}ClientTest.java`

```java
package {package-name}.{domain}.infrastructure;

import {package-name}.TestApplication;
import {package-name}.{domain}.infrastructure.{Context}Client;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("local")
class {Context}ClientTest {

    @Autowired
    private {Context}Client {context}Client;

    @Test
    void test{Method}() {
        // Command and Result are defined inside {Context}Client
        {Method}Command command = new {Method}Command({commandFields});
        {Method}Result result = {context}Client.{method}(command);

        assertNotNull(result);
        // Add assertions based on expected result
    }
}
```

## Step 8: Execute Tests

Run tests for a specific domain module:

```bash
./mvnw test -DskipTests=false -pl {project-name}/{project-name}-domain-{domain}
```

</Execution_Protocol>

<Key_Conventions>

| Item                  | Convention                                                     |
| --------------------- | -------------------------------------------------------------- |
| Test Entry            | `Test{Domain}Application.java` in package root                 |
| API Test Location     | `src/test/java/{package-path}/{domain}/api/`                   |
| Service Test Location | `src/test/java/{package-path}/{domain}/application/`           |
| Client Test Location  | `src/test/java/{package-path}/{domain}/infrastructure/`        |
| Test Configuration    | `src/test/resources/application-local.yml`                     |
| Test Annotation       | `@SpringBootTest` + `@ActiveProfiles("local")`                 |
| Profile               | Use `local` profile for integration tests with real database   |
| Assertions            | Use JUnit 5 assertions from `org.junit.jupiter.api.Assertions` |

</Key_Conventions>

<Annotation_Reference>

### PageRequest and PageResponse

Use built-in pagination classes for list endpoint testing:

```java
import io.github.siyukio.tools.api.dto.PageRequest;
import io.github.siyukio.tools.api.dto.PageResponse;

// Create page request
PageRequest request = new PageRequest(page, size);

// Call controller method
PageResponse<{Context}Response> response = {context}Controller.list(request);
```

</Annotation_Reference>

<Verification>
After implementation:
1. Run `./mvnw test -pl {project-name}/{project-name}-domain-{domain}` to execute tests
2. Verify all assertions pass
3. Check test coverage for all API endpoints
4. Ensure positive, negative, and edge case scenarios are covered
</Verification>
