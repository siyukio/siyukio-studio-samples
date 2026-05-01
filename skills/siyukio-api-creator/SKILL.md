---
name: siyukio-api-creator
description: Create or update the Siyukio API layer (controller, path constants, request/response DTOs) in Spring Boot domain modules. Use when adding endpoints, exposing application services through @ApiController/@ApiMapping, defining @ApiParameter DTO contracts, or enabling ACP access for specific APIs.
---

# siyukio-api-creator

Generate or refine the API layer for one domain context in a Siyukio Spring Boot module.

## Scope

Create or update files under:

```
{project-name}/{project-name}-domain-{domain}/src/main/java/{package-path}/{domain}/api/
├── {Context}Controller.java
├── paths/
│   └── {Context}Paths.java
└── dto/
    ├── {Context}Request.java
    └── {Context}Response.java
```

## Use this skill when

- Add a new API endpoint.
- Expose an existing Application Service method through HTTP/ACP.
- Introduce or adjust request/response DTO contracts.
- Standardize `@ApiController`, `@ApiMapping`, and `@ApiParameter` usage.

## Do not use this skill when

- Work is domain model or policy design only. Use `$siyukio-create-domain`.
- Work is application service orchestration only. Use `$siyukio-create-application`.

## Preconditions

- Target module exists: `{project-name}/{project-name}-domain-{domain}`.
- Target service exists or is being created: `{package-name}.{domain}.application.{Context}Service`.
- Module has dependency:

```xml
<dependency>
    <groupId>io.github.siyukio</groupId>
    <artifactId>spring-siyukio-application-acp</artifactId>
</dependency>
```

- If `signature = true` is used in `@ApiMapping`, configure `spring.siyukio.signature.salt`.
- If token authorization is used (default), configure `spring.siyukio.jwt.*`.

## Execution workflow

### 1) Normalize inputs

Extract and normalize:

- `{domain}`: kebab-case module domain (example: `user-management`)
- `{Context}`: PascalCase business context (example: `User`)
- `{context}`: camelCase variable name (example: `user`)
- Operations: `get`, `create`, `update`, `list`, `remove` (select required subset)
- DTO fields and validation constraints
- ACP requirement per endpoint (`acpAvailable = true` when needed)

### 2) Create or update DTOs

Write DTOs in `api/dto/` as Java records.

Rules:

- Request DTO must implement `Validated`.
- Every DTO field must declare `@ApiParameter`; fields without it are filtered.
- Put Jakarta validation annotations on request fields (`@NotBlank`, `@Size`, etc.).
- Keep DTO field names aligned with service method contracts.

Request template:

```java
public record {Context}Request(
        @ApiParameter(description = "ID", required = true)
        String id,

        @ApiParameter(description = "Name", required = true)
        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must not exceed 100 characters")
        String name
) implements Validated {
}
```

Response template:

```java
public record {Context}Response(
        @ApiParameter(description = "ID")
        String id,

        @ApiParameter(description = "Name")
        String name
) {
}
```

### 3) Create or update path constants

Define all endpoint paths in `api/paths/{Context}Paths.java`.

Rules:

- Keep controller methods free of raw path strings.
- Use `/{domain}/{operation}` for simple single-context modules.
- Use `/{domain}/{operation}{Context}` when disambiguation is needed.

Template:

```java
public interface {Context}Paths {
    String LIST = "/{domain}/list{Context}";
    String CREATE = "/{domain}/create{Context}";
    String GET = "/{domain}/get{Context}";
    String UPDATE = "/{domain}/update{Context}";
    String REMOVE = "/{domain}/remove{Context}";
}
```

### 4) Create or update controller

Implement `api/{Context}Controller.java` with `@ApiController`.

Controller rules:

- Inject `{Context}Service`.
- Use `@ApiMapping(path = {Context}Paths.X, summary = "...")`.
- Accept `Token token` when user context is required.
- Use request DTOs for command-style operations.
- Use `PageRequest` / `PageResponse` for pagination endpoints.
- Set `acpAvailable = true` only for endpoints that must be callable from ACP.

Template:

```java
@ApiController(summary = "{Context} API")
public class {Context}Controller {

    @Autowired
    private {Context}Service {context}Service;

    @ApiMapping(path = {Context}Paths.GET, summary = "Get {Context} by ID")
    public {Context}Response get(Token token, @ApiParameter(description = "Request") {Context}Request request) {
        return {context}Service.getById(request.id());
    }

    @ApiMapping(path = {Context}Paths.LIST, summary = "Query {Context} list")
    public PageResponse<{Context}Response> list(
            Token token,
            @ApiParameter(description = "Page request") PageRequest request
    ) {
        return {context}Service.queryPage(request);
    }
}
```

### 5) Validate annotation semantics

- `@ApiController`: define context-level API group metadata.
- `@ApiMapping`: control endpoint behavior.
  - `authorization` default: `true`
  - `signature` default: `false`
  - `acpAvailable` default: `false`
- `@ApiParameter`: define field visibility and API contract metadata.

### 6) Verify implementation

From repository root, run:

```bash
./mvnw -pl {project-name}/{project-name}-domain-{domain} -DskipTests compile
```

If controller tests exist, run:

```bash
./mvnw -pl {project-name}/{project-name}-domain-{domain} test -Dtest={Context}ControllerTest
```

Then confirm:

- Controller method signatures match `{Context}Service` methods.
- DTO fields and types match service expectations.
- All mappings use constants from `{Context}Paths`.
- New endpoints are POST JSON contract compatible with current project conventions.
- ACP-enabled endpoints are explicitly marked and intentional.

## Conventions checklist

- Package: `{package-name}.{domain}.api`
- Controller: `{Context}Controller`
- Paths constants: `{Context}Paths` in `api/paths`
- Request DTO: `{Context}Request` (record + `Validated`)
- Response DTO: `{Context}Response` (record)
- DTO field annotations: all fields use `@ApiParameter`
- Pagination: use framework `PageRequest` / `PageResponse`
- Operations vocabulary: `get`, `create`, `update`, `list`, `remove`
