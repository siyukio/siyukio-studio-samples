---
name: siyukio-integration-creator
description: Create or update integration clients/adapters in Siyukio Spring Boot domain modules for external REST, gRPC, SDK, or message integrations. Use when adding outbound service access, or shared integration components that must stay free of domain business logic.
---

# siyukio-integration-creator

Create integration-layer integration components that can be reused by application services.

## Scope

Write or update files under:

```
{project-name}/{project-name}-{domain}/src/main/java/{package-path}/{domain}/integration/
└── {Context}Client.java
```

## Use this skill when

- Add a new external service integration.
- Introduce or refactor a shared outbound client.
- Add request/response transport DTO mapping.
- Standardize retry, timeout, and error translation behavior.

## Do not use this skill when

- Implement domain model entities, policies, or errors. Use `$siyukio-model-creator`.
- Implement domain application orchestration/use-case logic. Use `$siyukio-application-creator`.
- Expose domain API endpoints. Use `$siyukio-api-creator`.

## Preconditions

- Module exists: `{project-name}/{project-name}-{domain}`.
- Package base exists: `{project-name}-{domain}.integration`.
- External service contract is known: endpoints/protocol, auth mode, and required operations.
- No business rules are expected inside integration classes.

## Execution workflow

### 1) Normalize integration inputs

Extract and normalize:

- `{Context}`: PascalCase integration context name (example: `Payment`, `Wechat`, `Sms`).
- `{context}`: camelCase field name (example: `payment`, `wechat`, `sms`).
- Operations: required verbs and signatures (`send`, `query`, `fetch`, etc.).
- Cross-cutting concerns: timeout, retry, idempotency, and logging/redaction needs.

### 2) Design a stable client contract

Define one public method per operation with explicit input/output types.

Rules:

- Keep method names business-neutral and transport-aware.
- Prefer immutable `record` command/result types for each operation.
- Keep framework-specific payload types out of public client contracts when possible.

### 3) Implement `{Context}Client`

Create or update:
`{project-name}/{project-name}-{domain}/src/main/java/{package-path}/{domain}/integration/{Context}Client.java`

Template:

```java
package {project-name}-{domain}.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class {Context}Client {

    public QueryResult query(QueryCommand command) {
        // 1. Validate transport-level input
        // 2. Build outbound request
        // 3. Call external service
        // 4. Map response or translate exception
        return new QueryResult(true, null);
    }

    public record QueryCommand(
            String requestId,
            String payload
    ) {
    }

    public record QueryResult(
            boolean success,
            String message
    ) {
    }

    public record ClientConfig(
            String baseUrl,
            int timeoutMillis,
            int retryTimes
    ) {
    }
}
```

### 4) Add optional client configuration inline

Define client configuration as a nested `record` inside `{Context}Client` when endpoint, timeout, or auth settings are needed.

Rules:

- Bind properties explicitly (for example `@ConfigurationProperties`).
- Keep secrets in environment or external configuration.
- Do not hardcode tokens, passwords, or endpoint secrets.
- Do not create a standalone `config/` subdirectory for client configuration.

### 5) Apply integration conventions

- Package: `{project-name}-{domain}.integration`
- Class naming: `{Context}Client`
- Input type naming: `{Operation}Command`
- Output type naming: `{Operation}Result`
- Logging: include request IDs/correlation IDs, avoid sensitive payload leakage
- Error handling: translate low-level exceptions into integration-oriented messages

## Verification

From `{project-name}/` run:

```bash
./mvnw -pl {project-name}-{domain} -DskipTests compile
```

If related tests exist, run:

```bash
./mvnw -pl {project-name}-{domain} test -Dtest={Context}ClientTest
```

Then confirm:

- Client APIs are deterministic and type-safe.
- Public client contract is independent from domain entities.
- Configuration and secrets handling follow project conventions.
- Logging and exception mapping are safe and actionable.
