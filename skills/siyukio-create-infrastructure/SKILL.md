---
name: siyukio-create-infrastructure
description: Create or update infrastructure clients/adapters in Siyukio Spring Boot common modules for external REST, gRPC, SDK, or message integrations. Use when adding outbound service access, transport DTO mapping, timeout/retry/error handling, or shared integration components that must stay free of domain business logic.
---

# siyukio-create-infrastructure

Create infrastructure-layer integration components that can be reused by application services.

## Scope

Write or update files under:

```
{project-name}/{project-name}-common/src/main/java/{package-path}/common/infrastructure/
├── {Service}Client.java
└── config/
    └── {Service}ClientConfig.java   (optional)
```

## Use this skill when

- Add a new external service integration.
- Introduce or refactor a shared outbound client.
- Add request/response transport DTO mapping.
- Standardize retry, timeout, and error translation behavior.

## Do not use this skill when

- Implement domain entities, policies, or invariants. Use `$siyukio-create-domain`.
- Implement application orchestration/use-case logic. Use `$siyukio-create-application`.
- Expose API endpoints. Use `$siyukio-create-api`.

## Preconditions

- Module exists: `{project-name}/{project-name}-common`.
- Package base exists: `{package-name}.common.infrastructure`.
- External service contract is known: endpoints/protocol, auth mode, and required operations.
- No business rules are expected inside infrastructure classes.

## Execution workflow

### 1) Normalize integration inputs

Extract and normalize:

- `{Service}`: PascalCase service name (example: `Payment`, `Wechat`, `Sms`).
- `{service}`: camelCase field name (example: `payment`, `wechat`, `sms`).
- Operations: required verbs and signatures (`send`, `query`, `fetch`, etc.).
- Cross-cutting concerns: timeout, retry, idempotency, and logging/redaction needs.

### 2) Design a stable client contract

Define one public method per operation with explicit input/output types.

Rules:

- Keep method names business-neutral and transport-aware.
- Prefer immutable `record` command/result types for each operation.
- Keep framework-specific payload types out of public client contracts when possible.

### 3) Implement `{Service}Client`

Create or update:
`{project-name}/{project-name}-common/src/main/java/{package-path}/common/infrastructure/{Service}Client.java`

Template:

```java
package {package-name}.common.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class {Service}Client {

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
}
```

### 4) Add optional client configuration

Create `config/{Service}ClientConfig.java` only when endpoint or credential settings are needed.

Rules:

- Bind properties explicitly (for example `@ConfigurationProperties`).
- Keep secrets in environment or external configuration.
- Do not hardcode tokens, passwords, or endpoint secrets.

### 5) Apply infrastructure conventions

- Package: `{package-name}.common.infrastructure`
- Class naming: `{Service}Client`
- Input type naming: `{Operation}Command`
- Output type naming: `{Operation}Result`
- Logging: include request IDs/correlation IDs, avoid sensitive payload leakage
- Error handling: translate low-level exceptions into integration-oriented messages

## Verification

From repository root, run:

```bash
cd siyukio-studio-server
./mvnw -pl siyukio-studio-server-common -DskipTests compile
```

If related tests exist, run:

```bash
cd siyukio-studio-server
./mvnw -pl siyukio-studio-server-common test -Dtest={Service}ClientTest
```

Then confirm:

- Client APIs are deterministic and type-safe.
- Public client contract is independent from domain entities.
- Configuration and secrets handling follow project conventions.
- Logging and exception mapping are safe and actionable.
