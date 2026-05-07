---
name: siyukio-api-creator
description: Create or update the Siyukio API layer (controller, path constants, request/response DTOs) in Spring Boot domain modules. Use when adding endpoints, exposing application services through @ApiController/@ApiMapping, defining @ApiParameter DTO contracts, or enabling ACP access for specific APIs.
---

# siyukio-api-creator

Generate or refine the API layer for one domain context in a Siyukio Spring Boot module.

## Scope

Create or update files under:

```
{server-project-name}/{server-project-name}-{domain}/src/main/java/{package-path}/{domain}/api/
├── {Context}Controller.java
├── paths/
│   └── {Context}Paths.java
└── dto/
    ├── {Context}CreateRequest.java
    ├── {Context}UpdateRequest.java
    ├── {Context}RemoveRequest.java
    ├── {Context}DeleteRequest.java (optional, explicit hard delete only)
    ├── {Context}QueryRequest.java (optional for query APIs)
    └── {Context}Response.java
```

## Use this skill when

- Add a new API endpoint.
- Expose an existing Application Service method through HTTP/ACP.
- Introduce or adjust request/response DTO contracts.
- Standardize `@ApiController`, `@ApiMapping`, and `@ApiParameter` usage.

## Do not use this skill when

- Work is domain entity/errors/policy design only. Use `$siyukio-model-creator`.
- Work is application service orchestration only. Use `$siyukio-application-creator`.

## Preconditions

- Target module exists: `{server-project-name}/{server-project-name}-{domain}`.
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
- Operations: `get`, `create`, `update`, `list`, `remove`, `delete` (select required subset; `delete` is explicit hard delete only)
- Default deletion semantics: when requirements say "delete" without explicitly saying hard/physical delete, map it to `remove` (soft delete).
- DTO fields and explicit validation constraints (only when business requirements specify them)
- ACP requirement per endpoint (`acpAvailable = true` when needed)

### 2) Create or update DTOs

Write DTOs in `api/dto/` as Java records.

Rules:

- Every request DTO field must declare `@ApiParameter`; fields without it are filtered.
- Use `@ApiParameter` default strategy by default; when business requirements do not explicitly request field-level validation constraints, keep the annotation minimal (for example, only `name`, and set `required = false` only for optional fields).
- If a DTO field name is already self-explanatory, omit `description` in `@ApiParameter`.
- Add explicit constraints in `@ApiParameter` only when required by business requirements. Siyukio automatically enforces validation from `@ApiParameter` metadata (do not add extra `Validated` implementation or Bean Validation annotations on request DTO fields).
- Use `minLength`/`maxLength` for string length constraints.
- Use `pattern` for regex string constraints.
- Use `minimum`/`maximum` for number range constraints.
- Use `minItems`/`maxItems` for list size constraints.
- Keep DTO field names aligned with service method contracts.
- Command DTOs must be operation-specific and independent:
  - `create` uses `{Context}CreateRequest` only.
  - `update` uses `{Context}UpdateRequest` only.
  - `remove` (soft delete) uses `{Context}RemoveRequest` only.
  - `delete` (hard delete, explicit requirement only) uses `{Context}DeleteRequest` only.
  - Do not reuse command DTOs across `create`, `update`, `remove`, and `delete` operations.
  - Keep path constant name and command DTO name strictly aligned by operation:
    - `{Context}Paths.REMOVE` <-> `{Context}RemoveRequest`
    - `{Context}Paths.DELETE` <-> `{Context}DeleteRequest`
- `create` request fields are required by default (do not write `required = true`) unless a field is explicitly optional by business design.
- `update` request must include `id` as required; all non-id business fields are optional (`required = false`) and represent partial update intent.
- `remove` request must contain only `id` (required) for soft-delete intent.
- `delete` request must contain only `id` (required) and is used only when hard delete is explicitly requested.
- If the requirement says "delete" but does not explicitly require hard/physical delete, implement `remove` and skip `delete`.
- DTO reuse is only allowed for query APIs (`get`, `list`, `page`, `search`) when request shapes are truly the same.

Create request template:

```java
public record {Context}CreateRequest(
        @ApiParameter(name = "name")
        String string,

        @ApiParameter(name = "remark", required = false)
        String remark
) {
}
```

Create request template (explicit validation required):

```java
public record {Context}CreateRequest(
        @ApiParameter(name = "string", maxLength = 10, minLength = 1)
        String string,

        @ApiParameter(name = "regex string", pattern = "^[a-zA-Z0-9]{1,10}$")
        String regexString,

        @ApiParameter(name = "number", maximum = 10, minimum = 1)
        Integer number,

        @ApiParameter(name = "list", maxItems = 10, minItems = 1)
        List<String> list
) {
}
```

Update request template:

```java
public record {Context}UpdateRequest(
        @ApiParameter(name = "ID")
        String id,

        @ApiParameter(name = "name", required = false)
        String name,

        @ApiParameter(name = "remark", required = false)
        String remark
) {
}
```

Remove request template:

```java
public record {Context}RemoveRequest(
        @ApiParameter(name = "ID")
        String id
) {
}
```

Delete request template:

```java
public record {Context}DeleteRequest(
        @ApiParameter(name = "ID")
        String id
) {
}
```

Query request template (optional reusable DTO for query APIs):

```java
public record {Context}QueryRequest(
        @ApiParameter(name = "ID", required = false)
        String id,

        @ApiParameter(name = "keyword", required = false)
        String keyword
) {
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
- Use `remove` path for soft delete semantics as the default deletion behavior.
- Use `delete` path only for explicitly requested physical delete semantics.
- Keep path constant names and command DTO names consistent for each operation (`REMOVE` + `RemoveRequest`, `DELETE` + `DeleteRequest`).

Template:

```java
public interface {Context}Paths {
    String LIST = "/{domain}/list{Context}";
    String CREATE = "/{domain}/create{Context}";
    String GET = "/{domain}/get{Context}";
    String UPDATE = "/{domain}/update{Context}";
    String REMOVE = "/{domain}/remove{Context}";
    String DELETE = "/{domain}/delete{Context}";
}
```

### 4) Create or update controller

Implement `api/{Context}Controller.java` with `@ApiController`.

Controller rules:

- Inject `{Context}Service`.
- Use `@ApiMapping(path = {Context}Paths.X, summary = "...")`.
- Accept `Token token` when user context is required.
- Use operation-specific command DTOs:
  - `create` -> `{Context}CreateRequest`
  - `update` -> `{Context}UpdateRequest`
  - `remove` (soft delete, default for unspecified delete requests) -> `{Context}RemoveRequest`
  - `delete` (hard delete, explicit requirement only) -> `{Context}DeleteRequest`
- Reuse DTOs only for query-style operations when request shapes are identical.
- Use `PageRequest` / `PageResponse` for pagination endpoints.
- Set `acpAvailable = true` only for endpoints that must be callable from ACP.

Template:

```java
@ApiController(summary = "{Context} API")
public class {Context}Controller {

    @Autowired
    private {Context}Service {context}Service;

    @ApiMapping(path = {Context}Paths.GET, summary = "Get {Context} by ID")
    public {Context}Response get(Token token, @ApiParameter(description = "Request") {Context}QueryRequest request) {
        return {context}Service.getById(request.id());
    }

    @ApiMapping(path = {Context}Paths.CREATE, summary = "Create {Context}")
    public String create(Token token, @ApiParameter(description = "Request") {Context}CreateRequest request) {
        return {context}Service.create(request);
    }

    @ApiMapping(path = {Context}Paths.UPDATE, summary = "Update {Context}")
    public void update(Token token, @ApiParameter(description = "Request") {Context}UpdateRequest request) {
        {context}Service.update(request);
    }

    @ApiMapping(path = {Context}Paths.REMOVE, summary = "Soft remove {Context}")
    public void remove(Token token, @ApiParameter(description = "Request") {Context}RemoveRequest request) {
        {context}Service.remove(request.id());
    }

    @ApiMapping(path = {Context}Paths.DELETE, summary = "Delete {Context} permanently")
    public void delete(Token token, @ApiParameter(description = "Request") {Context}DeleteRequest request) {
        {context}Service.delete(request.id());
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
- `@ApiParameter`: define field visibility and API contract metadata; `required` defaults to `true`, so do not set `required = true`; set `required = false` only for optional fields. Omit `description` when the DTO field name is already clear. Validation constraints (`minLength`, `maxLength`, `pattern`, `minimum`, `maximum`, `minItems`, `maxItems`) are optional and should be added only when explicitly required.
- Request DTO validation is automatically applied from `@ApiParameter`; no `Validated` interface implementation is required.

### 6) Verify implementation

From `{server-project-name}/` run:

```bash
./mvnw -pl {server-project-name}-{domain} -DskipTests compile
```

If controller tests exist, run:

```bash
./mvnw -pl {server-project-name}-{domain} test -Dtest={Context}ControllerTest
```

Then confirm:

- Controller method signatures match `{Context}Service` methods.
- DTO fields and types match service expectations.
- All mappings use constants from `{Context}Paths`.
- Unspecified "delete" requirements are implemented as `remove` endpoints (soft delete) using `{Context}RemoveRequest`.
- `delete` endpoints are created only when physical delete is explicitly requested and use `{Context}DeleteRequest`.
- Path constants and command DTO names are operation-aligned (`REMOVE` + `RemoveRequest`, `DELETE` + `DeleteRequest`).
- New endpoints are POST JSON contract compatible with current project conventions.
- ACP-enabled endpoints are explicitly marked and intentional.

## Conventions checklist

- Package: `{package-name}.{domain}.api`
- Controller: `{Context}Controller`
- Paths constants: `{Context}Paths` in `api/paths`
- Create DTO: `{Context}CreateRequest` (record, fields required by default)
- Update DTO: `{Context}UpdateRequest` (record, only `id` required)
- Remove DTO: `{Context}RemoveRequest` (record, only `id`, soft delete; default for unspecified delete requirements)
- Delete DTO: `{Context}DeleteRequest` (record, only `id`, physical delete; explicit requirement only)
- Query DTO: `{Context}QueryRequest` (record, reusable only among query APIs)
- Response DTO: `{Context}Response` (record)
- Request DTO contract: all request DTO fields declare `@ApiParameter`; no `Validated` implementation
- DTO field annotations: all fields use `@ApiParameter`
- Validation strategy: use `@ApiParameter` default strategy unless business requirements explicitly define additional constraints
- Pagination: use framework `PageRequest` / `PageResponse`
- Operations vocabulary: `get`, `create`, `update`, `list`, `remove` (default delete), `delete` (explicit hard delete only)
