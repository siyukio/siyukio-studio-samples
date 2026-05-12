---
name: siyukio-integration-creator
description: Create or update integration clients/adapters in Siyukio Spring Boot domain modules for external REST, gRPC, SDK, or message integrations. Use when adding outbound service access, or shared integration components that must stay free of domain business logic.
---

# siyukio-integration-creator

Create integration-layer integration components that can be reused by application services.

## Scope

Write or update files under:

```
{server-project-name}/{server-project-name}-{domain}/src/main/java/{package-path}/{domain}/integration/
└── {Context}Client.java
```

## Use this skill when

- Add a new external service integration.
- Introduce or refactor a shared outbound client.
- Implement a simple outbound HTTP call directly with `@ApiClient`.
- Add request/response transport DTO mapping.
- Standardize retry, timeout, and error translation behavior.

## Do not use this skill when

- Implement domain model entities, policies, or errors. Use `$siyukio-model-creator`.
- Implement domain application orchestration/use-case logic. Use `$siyukio-application-creator`.
- Expose domain API endpoints. Use `$siyukio-api-creator`.

## Preconditions

- Module exists: `{server-project-name}/{server-project-name}-{domain}`.
- Package base exists: `{server-project-name}-{domain}.integration`.
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
- If the request/response schema is uncertain, use `JSONObject` directly for input/output payloads.
- If the request/response schema is clear, define immutable `record` types.
- Record type names must end with `Command` and `Result`.
- Record types must be defined inside the client type.

### 3) Choose integration implementation style

- For simple external HTTP access, prefer direct `@ApiClient` interface implementation.
- For complex integrations (custom retries, signing, multi-step flows, SDK orchestration), use class-based client implementation.

### 4) Implement `{Context}Client`

Create or update:
`{server-project-name}/{server-project-name}-{domain}/src/main/java/{package-path}/{domain}/integration/{Context}Client.java`

Template:

```java
package {server-project-name}-{domain}.integration;

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

Simple HTTP template:

```java
package {server-project-name}-{domain}.integration;

import io.github.siyukio.tools.api.annotation.client.ApiClient;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;

@ApiClient(url = "${integration.{context}.url}")
public interface {Context}Client {

    @PostExchange("/query")
    JSONObject query(@RequestBody JSONObject command);

    record QueryCommand(
            String requestId
    ) {
    }

    record QueryResult(
            String status
    ) {
    }
}
```

### 5) Add optional client configuration inline

Define client configuration as a nested `record` inside `{Context}Client` when endpoint, timeout, or auth settings are needed.

Rules:

- Bind properties explicitly (for example `@ConfigurationProperties`).
- Keep secrets in environment or external configuration.
- Do not hardcode tokens, passwords, or endpoint secrets.
- Do not create a standalone `config/` subdirectory for client configuration.

### 6) Apply integration conventions

- Package: `{server-project-name}-{domain}.integration`
- Class naming: `{Context}Client`
- Input type naming: `{Operation}Command`
- Output type naming: `{Operation}Result`
- When schema is uncertain: allow `JSONObject` for request/response types.
- When schema is confirmed: prefer nested `record` `{Operation}Command` and `{Operation}Result`.
- Logging: include request IDs/correlation IDs, avoid sensitive payload leakage
- Error handling: translate low-level exceptions into integration-oriented messages

## Verification

From `{server-project-name}/` run:

```bash
./mvnw -pl {server-project-name}-{domain} -DskipTests compile
```

If related tests exist, run:

```bash
./mvnw -pl {server-project-name}-{domain} test -Dtest={Context}ClientTest
```

Then confirm:

- Client APIs are deterministic and type-safe.
- Public client contract is independent from domain entities.
- Configuration and secrets handling follow project conventions.
- Logging and exception mapping are safe and actionable.
