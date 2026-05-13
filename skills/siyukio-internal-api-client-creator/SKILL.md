---
name: siyukio-internal-api-client-creator
description: Create or update Siyukio internal API client code for server domain modules. Use when internal API callers need typed client interfaces, synchronized DTO/path contracts, or when adding/updating `{Context}` internal endpoints and corresponding `@ApiClient` interfaces under `{server-project-name}-{domain}-client`.
---

# siyukio-internal-api-client-creator

Create or update one internal API client module and its client interface for a domain context.

## Scope

Work on these module pairs:

```text
{server-project-name}/{server-project-name}-{domain}/
└── src/main/java/{package-path}/{domain}/api/
    ├── {Context}InternalController.java
    ├── dto/
    │   └── {Context}InternalRequest.java / {Context}InternalResponse.java (or role-less variants)
    └── paths/
        └── {Context}InternalPaths.java

{server-project-name}/{server-project-name}-{domain}-client/
└── src/main/java/{package-path}/{domain}/client/
    ├── {Context}Client.java
    ├── dto/   (copied from internal API dto when needed)
    └── paths/ (copied from internal API paths when needed)
└── src/test/resources/
    └── application.yml
```

## Use this skill when

- Add or update internal API clients for cross-module/service calls.
- Keep `{Context}` internal API DTO/path contracts synchronized into `{server-project-name}-{domain}-client`.
- Generate interface-style API clients using `@ApiClient` + `@PostExchange` from internal controller signatures.

## Do not use this skill when

- Exposing public/user/admin API endpoints without internal callers.
- Only modifying domain model/application logic without API contract changes.
- Creating generic outbound integrations not based on Siyukio internal API contracts.

## Required inputs

- `{server-project-name}`: server aggregator artifact, for example `siyukio-studio-server`.
- `{package-name}`: Java base package.
- `{package-path}`: slash package path, for example `io/github/siyukio/samples`.
- `{domain}`: module suffix in kebab-case, for example `user-management`.
- `{Context}`: business context in PascalCase, for example `User`.

Derived:

- Internal module artifact: `{server-project-name}-{domain}`
- Client module artifact: `{server-project-name}-{domain}-client`
- Internal controller source: `{Context}InternalController`

## Preconditions

- `{server-project-name}/{server-project-name}-{domain}` exists.
- Generate all code/comments in English only.

## Execution workflow

### 1) Verify internal API exists in target domain module

Check under:

`{server-project-name}/{server-project-name}-{domain}/src/main/java/{package-path}/{domain}/api`

Rules:

- If `{Context}InternalController.java` exists, use it as the source of truth.
- If it does not exist, call `$siyukio-role-based-api-creator` first to create internal API artifacts, then continue.

Expected minimum source artifacts:

- `{Context}InternalController` with `@ApiMapping` methods.
- Internal paths class `{Context}InternalPaths`.
- Request/response DTOs used by internal methods.

### 2) Ensure client module exists

Target module:

`{server-project-name}/{server-project-name}-{domain}-client`

Rules:

- If missing, call `$siyukio-module-creator` to scaffold module structure for `{server-project-name}-{domain}-client`.
- Then align module `pom.xml` to include these dependencies (add if missing, do not duplicate):

```xml
<dependency>
    <groupId>io.github.siyukio</groupId>
    <artifactId>spring-siyukio-http-client</artifactId>
</dependency>
<dependency>
    <groupId>{package-name}</groupId>
    <artifactId>{server-project-name}-{domain}</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

### 3) Copy internal API contracts to client module

Copy the target `{Context}` internal API contract objects from domain module into client module.

Source:

- `.../{domain}/api/dto/` (request/response used by internal API)
- `.../{domain}/api/paths/{Context}InternalPaths.java`

Target (recommended):

- `.../{domain}/client/dto/`
- `.../{domain}/client/paths/`

Rules:

- Keep class names unchanged to reduce mapping overhead.
- Update package declarations/imports to client module package layout.
- Copy only `{Context}`-related DTOs/paths needed by current client methods.
- Reuse existing files in client module when already aligned.

### 4) Create or update `{Context}Client` interface

Create or update:

`{server-project-name}/{server-project-name}-{domain}-client/src/main/java/{package-path}/{domain}/client/{Context}Client.java`

Rules:

- Declare interface and annotate with `io.github.siyukio.tools.api.annotation.client.ApiClient`.
- Use `org.springframework.web.service.annotation.PostExchange` on every client method.
- Method name, request type, and response type must match `{Context}InternalController` `@ApiMapping` method signatures.
- Map `@PostExchange` value to matching `{Context}InternalPaths` constant.
- Keep interface updates additive and idempotent (do not remove unrelated methods).

Template:

```java
import io.github.siyukio.tools.api.annotation.client.ApiClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;

@ApiClient(url = "${internal.url}", headers = {
        "Authorization=${internal.authorization}"
})
public interface {Context}Client {

    @PostExchange({Context}InternalPaths.PATH_NAME)
    {Context}Response method(@RequestBody {Context}InternalRequest request);
}
```

Method-mapping checklist:

- Find each internal API method via `@ApiMapping(path = ...)`.
- Use the same method name in `{Context}Client`.
- Use the same request and response DTO types as internal controller contract.
- Keep `@PostExchange` path constant synchronized with `{Context}InternalPaths`.

### 5) Generate test config in client module

Create or update:

`{server-project-name}/{server-project-name}-{domain}-client/src/test/resources/application.yml`

with:

```yaml
internal:
  authorization: ${INTERNAL_AUTHORIZATION:}
  { context }:
    url: ${INTERNAL_{CONTEXT}_URL:http://localhost:8080}
```

Rules:

- Create `src/test/resources` if missing.
- Preserve unrelated existing YAML keys; only add/update `internal.authorization` and `internal.{context}.url` when needed.

Then create or update:

`{server-project-name}/{server-project-name}-{domain}-client/src/test/resources/application-local.yml`

with:

```yaml
INTERNAL_{CONTEXT}_URL: http://localhost:8080
```

Additional rules:

- Keep env key naming uppercase with underscore style, using `{CONTEXT}` uppercase placeholder.
- Keep placeholder structure consistent with `application.yml`.

### 6) Register module in parent `pom.xml`

Check parent pom:

`{server-project-name}/pom.xml`

Rules:

- Ensure `<modules>` contains:

```xml
<module>{server-project-name}-{domain}-client</module>
```

- If missing, add it once (do not duplicate).
- Ensure `<dependencyManagement><dependencies>` contains dependency for `{server-project-name}-{domain}-client`.
- If missing, add:

```xml
<dependency>
    <groupId>{package-name}</groupId>
    <artifactId>{server-project-name}-{domain}-client</artifactId>
    <version>${project.version}</version>
</dependency>
```

- Do not remove or rewrite unrelated existing parent module/dependency entries.

### 7) Consistency checks

Confirm:

- Internal API source exists (created when absent).
- Client module exists and has required HTTP client/test dependencies.
- Parent `{server-project-name}/pom.xml` registers `{server-project-name}-{domain}-client` in both `<modules>` and `<dependencyManagement><dependencies>`.
- `{Context}` DTO/paths are synchronized from internal API to client module.
- `{Context}Client` methods map one-to-one with target internal `@ApiMapping` methods.
- No hard-coded internal endpoint string inside client methods when path constants exist.
- Client test config exists at `src/test/resources/application.yml` with `internal.authorization` and `internal.{context}.url`.
- Client test override exists at `src/test/resources/application-local.yml` with `INTERNAL_{CONTEXT}_URL`.

## Verification

From `{server-project-name}/` run:

```bash
./mvnw -pl {server-project-name}-{domain},{server-project-name}-{domain}-client -DskipTests compile
```

If client tests exist:

```bash
./mvnw -pl {server-project-name}-{domain}-client test
```

Before finishing, confirm:

- Client module compiles.
- Internal API source and client interface signatures stay aligned.
- Contract copy (dto/paths) is complete for the changed `{Context}` methods.

## Related skills

- `$siyukio-role-based-api-creator`
- `$siyukio-module-creator`
- `$siyukio-api-creator`
- `$siyukio-unit-test-creator`
