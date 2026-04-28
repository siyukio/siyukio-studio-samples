---
name: siyukio-create-infrastructure
description: "Generate Infrastructure layer components for Siyukio-based Spring Boot applications, such as external service clients"
triggers:
  - "add infrastructure"
  - "create infrastructure"
  - "new infrastructure"
  - "add client"
  - "create client"
  - "new client"
---

<Purpose>
Generate Infrastructure layer components for Siyukio Spring Boot applications. Infrastructure components are non-business common components, such as clients for accessing external services (REST APIs, gRPC, etc.).

Infrastructure layer structure under common module:

```
{project-name}-common/
└── infrastructure/
    └── {Service}Client.java
```

</Purpose>

<Use_When>

- Creating a client for external service access
- Implementing integration with third-party APIs
- Creating HTTP/REST/gRPC clients
- Implementing data transformation from external services

</Use_When>

<Do_Not_Use_When>

- If the component needs to operate on domain models (entities, policies), use `$siyukio-create-application` instead
- Infrastructure components should not contain business logic

</Do_Not_Use_When>

<Prerequisites>

<Execution_Policy>
- Infrastructure components are independent of domain modules
- They typically reside in the common module
- They provide integration capabilities for other application components
</Execution_Policy>

Requirements:

- Target module: `{project-name}-common/`
- Target file location: `src/main/java/{package-path}/common/infrastructure/`

</Prerequisites>

<Execution_Protocol>

## Step 1: Determine Infrastructure Component Requirements

From the argument, extract:

- `{Service}`: The external service name (PascalCase, e.g., `Wechat`, `Payment`, `Sms`)
- `{service}`: The service variable name (camelCase, e.g., `wechat`, `payment`, `sms`)
- Operations: what operations are needed (send, fetch, query, etc.)

## Step 2: Generate Infrastructure Component

Location: `{project-name}-common/src/main/java/{package-path}/common/infrastructure/{Service}Client.java`

**Component structure:**

```java
package {package-name}.common.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Client for {Service} external service integration.
 */
@Slf4j
@Component
public class {Service}Client {

    /**
     * {method} operation.
     */
    public {method}Result {method}({method}Command command) {
        // implementation
    }

    /**
     * Command for {method} operation.
     */
    public record {method}Command(
            {fields}
    ) {}

    /**
     * Result for {method} operation.
     */
    public record {method}Result(
            {resultFields}
    ) {}
}
```

</Execution_Protocol>

<Key_Conventions>

| Item            | Convention                                         |
| --------------- | -------------------------------------------------- |
| Package         | `{package-name}.common.infrastructure`             |
| Package Path    | `{package-path}/common/infrastructure/`            |
| Component class | `{Service}Client.java` with `@Component`           |
| Command type    | `record {method}Command(...)` for input parameters |
| Result type     | `record {method}Result(...)` for return values     |
| Logging         | Use `@Slf4j` for logging                           |

</Key_Conventions>

<Verification>
After implementation:
1. Run `./mvnw compile` to verify code compiles
2. Check all imports are correct
3. Verify record Command and Result field types match requirements
4. If `{Service}Client` has unit test, run `./mvnw test -DskipTests=false -pl {project-name}/{project-name}-domain-{domain}` to verify the unit test passes
</Verification>
