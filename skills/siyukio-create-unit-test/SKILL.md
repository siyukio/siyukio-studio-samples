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
Generate unit tests for Siyukio Spring Boot applications. This skill focuses on testing API endpoints using the `@ApiMock` utility class which provides a clean way to perform HTTP requests against mocked API endpoints.

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

> **Note**: When `$siyukio-create-api` or `$siyukio-create-application` skill is executed successfully, if there is an explicit requirement to create corresponding unit tests, use this skill to create them. This skill can create unit tests for existing APIs or applications.
> </Prerequisites>

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

Extract local test environment variables from `AGENTS.md` and create:

Location:
`{project-name}/{project-name}-domain-{domain}/src/test/resources/application-local.yml`

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
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class Test{Domain}Application {

    public static void main(String[] args) {
        new SpringApplicationBuilder(Test{Domain}Application.class)
                .build()
                .run(args);
    }

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

Use `@ApiMock` for API endpoint testing. Each test method should:

1. Setup test data if needed
2. Perform the API call using `apiMock.perform()`
3. Assert the response

```java
package {package-name}.{domain}.api;

import {package-name}.{domain}.api.dto.{Context}Request;
import {package-name}.{domain}.api.dto.{Context}Response;
import {package-name}.{domain}.api.paths.{Context}Paths;
import io.github.siyukio.tools.test.api.ApiMock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
class {Context}ControllerTest {

    @Autowired
    private ApiMock apiMock;

    @Test
    void testCreate{Context}() {
        {Context}Response response = this.apiMock.perform(
                {Context}Paths.CREATE,
                new {Context}Request(null, "Test Name", "Description"),
                {Context}Response.class);

        assertNotNull(response.id());
        assertEquals("Test Name", response.name());
    }

    @Test
    void testGet{Context}() {
        // Create test data first
        {Context}Response created = this.apiMock.perform(
                {Context}Paths.CREATE,
                new {Context}Request(null, "Test Name", "Description"),
                {Context}Response.class);

        // Test GET endpoint
        {Context}Response response = this.apiMock.perform(
                {Context}Paths.GET,
                new {Context}Request(created.id(), null, null),
                {Context}Response.class);

        assertEquals(created.id(), response.id());
        assertEquals("Test Name", response.name());
    }

    @Test
    void testUpdate{Context}() {
        // Create test data first
        {Context}Response created = this.apiMock.perform(
                {Context}Paths.CREATE,
                new {Context}Request(null, "Original Name", "Description"),
                {Context}Response.class);

        // Test UPDATE endpoint
        {Context}Response updated = this.apiMock.perform(
                {Context}Paths.UPDATE,
                new {Context}Request(created.id(), "Updated Name", "Updated Description"),
                {Context}Response.class);

        assertEquals(created.id(), updated.id());
        assertEquals("Updated Name", updated.name());
        assertEquals("Updated Description", updated.description());
    }

    @Test
    void testList{Context}() {
        // Create test data
        this.apiMock.perform(
                {Context}Paths.CREATE,
                new {Context}Request(null, "Test 1", "Description 1"),
                {Context}Response.class);
        this.apiMock.perform(
                {Context}Paths.CREATE,
                new {Context}Request(null, "Test 2", "Description 2"),
                {Context}Response.class);

        // Test LIST endpoint
        PageResponse<{Context}Response> response = this.apiMock.perform(
                {Context}Paths.LIST,
                new PageRequest(0, 10),
                new ParameterizedTypeReference<PageResponse<{Context}Response>>() {});

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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
class {Context}ServiceTest {

    @Autowired
    private {Context}Service {context}Service;

    // TODO: Add tests for service methods not called by controllers
}
```

> **Note**: If an Application Service method is called by a Controller, skip creating a unit test for that method. Instead, create the unit test for the corresponding Controller method (Step 5). Only generate tests for service methods that are not exposed via API.

## Step 7: Generate Infrastructure Client Test Class (Optional)

For testing external service clients. Each domain can have its own infrastructure layer:

Location:
`{project-name}/{project-name}-domain-{domain}/src/test/java/{package-path}/{domain}/infrastructure/{Context}ClientTest.java`

```java
package {package-name}.{domain}.infrastructure;

import {package-name}.{domain}.infrastructure.{Context}Client;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
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
| API Mock Utility      | `ApiMock` from `io.github.siyukio.tools.test.api`              |
| Profile               | Use `local` profile for integration tests with real database   |
| Assertions            | Use JUnit 5 assertions from `org.junit.jupiter.api.Assertions` |

</Key_Conventions>

<Annotation_Reference>

### @ApiMock

The `@ApiMock` utility class provides a clean interface for testing API endpoints. It handles HTTP request/response
serialization and deserialization automatically.

```java
@Autowired
private ApiMock apiMock;

// Simple POST request
{Context}Response response = this.apiMock.perform(
        {Context}Paths.CREATE,
        new {Context}Request(null, "Name", "Description"),
        {Context}Response.class);

// With generic type reference for parameterized responses
ParameterizedTypeReference<PageResponse<{Context}Response>> typeRef =
        new ParameterizedTypeReference<PageResponse<{Context}Response>>() {};
PageResponse<{Context}Response> page = this.apiMock.perform(
        {Context}Paths.LIST,
        new PageRequest(0, 10),
        typeRef);
```

### PageRequest and PageResponse

Use built-in pagination classes for list endpoint testing:

```java
import io.github.siyukio.tools.api.dto.PageRequest;
import io.github.siyukio.tools.api.dto.PageResponse;

// Create page request
PageRequest request = new PageRequest(page, size);

// Use in API call
ParameterizedTypeReference<PageResponse<{Context}Response>> typeRef =
        new ParameterizedTypeReference<PageResponse<{Context}Response>>() {};
PageResponse<{Context}Response> response = this.apiMock.perform(path, request, typeRef);
```

</Annotation_Reference>

<Verification>
After implementation:
1. Run `./mvnw test -pl {project-name}/{project-name}-domain-{domain}` to execute tests
2. Verify all assertions pass
3. Check test coverage for all API endpoints
4. Ensure positive, negative, and edge case scenarios are covered
</Verification>
