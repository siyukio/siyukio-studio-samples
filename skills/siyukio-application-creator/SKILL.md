---
name: siyukio-application-creator
description: Create or update the Siyukio application service layer in Spring Boot domain modules. Use when implementing use-case orchestration between API and domain layers, adding query/command CRUD methods, enforcing domain policy checks, mapping DTOs and entities, or setting transaction boundaries.
---

# siyukio-application-creator

Create or refine one application service in a Siyukio domain module.

## Scope

Write or update files under:

```
{server-project-name}/{server-project-name}-{domain}/src/main/java/{package-path}/{domain}/application/
└── {Context}Service.java
```

## Use this skill when

- Add a new application service for one domain context.
- Implement use-case orchestration that calls domain policies and persistence.
- Add or adjust list/get/create/update/remove service methods.
- Add `delete` methods only when physical/hard delete is explicitly required.
- Align API contracts with application method signatures.

## Do not use this skill when

- Work is domain entity/errors/policy design only. Use `$siyukio-model-creator`.
- Work is controller/path/DTO exposure only. Use `$siyukio-api-creator`.
- Work is external integration only. Use `$siyukio-integration-creator`.

## Preconditions

- Target domain module exists: `{server-project-name}/{server-project-name}-{domain}`.
- Domain model entity exists: `{package-name}.{domain}.model.entity.{Entity}`.
- Domain model entity includes `enabled` field for soft-delete semantics.
- Service context is clear: `{Context}` (PascalCase), `{context}` (camelCase), `{entity}` (camelCase).
- API method contracts are known, or are created in the same task using `$siyukio-api-creator`.
- Required policy checks exist, or are added first using `$siyukio-model-creator`.

## Execution workflow

### 1) Normalize context inputs

Extract and normalize:

- `{domain}`: kebab-case domain module name (example: `user-management`)
- `{Context}`: PascalCase business context (example: `User`)
- `{entity}`: camelCase entity variable name (example: `user`)
- Required operations: `list`, `get`, `create`, `update`, `remove` (`delete` only for explicit hard-delete requirements)
- Validation rules: existence, enabled state, uniqueness, ownership, etc.
- Default deletion semantics: if requirement says "delete" without explicitly requiring physical/hard delete, implement `remove` (soft delete).

### 2) Lock method contracts

Define service methods from intended usage:

- Controller-facing methods: keep signatures aligned with controller methods.
- Controller-facing methods: use API DTOs for parameters/results.
- Internal-only methods: use `record {Method}Command` and `record {Method}Result` inside service.
- Add `Token token` parameter only when user context is required.

### 3) Ensure domain policy coverage

Before writing service logic, ensure policy methods exist in:
`{package-name}.{domain}.model.policy.{Entity}Policy`

Common policy methods:

- `check{Entity}Exists(id)`
- `check{Entity}Enabled(id)`
- `check{Entity}NameUnique(...)`

Rules:

- Route validation/business rule checks through policy methods.
- Do not perform validation queries directly in service via DAO.

### 4) Implement `{Context}Service`

Create or update:
`{server-project-name}/{server-project-name}-{domain}/src/main/java/{package-path}/{domain}/application/{Context}Service.java`

Implementation rules:

- Annotate class with `@Service` (and `@Slf4j` when logging is needed).
- If requirements ask to run specific business logic when service starts, implement `CommandLineRunner` in the service class.
- Inject `PgEntityDao<{Entity}>` for persistence operations.
- Inject `{Entity}Policy` for business validation/invariants.
- Use `XDataUtils.copy(...)` for entity/DTO transformation.
- Use `XDataUtils.mergeNotNul(source, target)` for partial updates.
- Treat `{Entity}` as a record class annotated with Lombok `@Builder`.
- Construct `{Entity}` instances via `{Entity}.builder()...build()` for readability; do not instantiate with `new {Entity}(...)`.
- When creating or updating `{Entity}`, ignore audit fields `createdAt`, `createdAtTs`, `updatedAt`, and `updatedAtTs`; these fields are automatically assigned.
- Treat `{Entity}.salt` as an internal field automatically maintained by `PgEntityDao` encryption/decryption flow; do not assign it in service code.
- Build list filters with `QueryBuilders.boolQuery()` and `termQuery/rangeQuery` as needed.
- Add default enabled filter for list/page queries unless requirements explicitly include disabled records.
- Build sort with `SortBuilders.fieldSort(...).order(SortOrder.XXX)`.
- If sorting is not explicitly specified, use `SortBuilders.fieldSort("updatedAtTs").order(SortOrder.DESC)` for list/page queries.
- Implement `remove` as soft delete by setting entity `enabled` to `false` and persisting via `pgEntityData.update(...)` (or equivalent project DAO update call).
- Implement `delete` only when physical deletion is explicitly required.
- Add `@Transactional` on methods that perform multi-step write operations.

Startup-runner template:

```java
@Service
public class {Context}Service implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        // Run business code once
    }
}
```

Minimal template:

```java
@Slf4j
@Service
public class {Context}Service {

    @Autowired
    private PgEntityDao<{Entity}> {entity}PgEntityDao;

    @Autowired
    private {Entity}Policy {entity}Policy;

    public {Context}Response get{Context}(Get{Context}Request request) {
        {Entity} {entity} = {entity}Policy.check{Entity}Exists(request.id());
        return XDataUtils.copy({entity}, {Context}Response.class);
    }

    @Transactional
    public {Context}Response create{Context}(Create{Context}Request request) {
        {entity}Policy.check{Entity}NameUnique(request.name(), null);
        {Entity} saved = {entity}PgEntityDao.insert({Entity}.builder().name(request.name()).build());
        return XDataUtils.copy(saved, {Context}Response.class);
    }

    @Transactional
    public void remove{Context}(Remove{Context}Request request) {
        {Entity} {entity} = {entity}Policy.check{Entity}Exists(request.id());
        pgEntityData.update({entity}.withEnabled(false));
    }
}
```

### 5) Apply consistency checks

Confirm:

- Package path is `{package-name}.{domain}.application`.
- Class name is `{Context}Service`.
- Method names/params/returns match API contracts.
- Validation logic is policy-first.
- Conversion style uses `XDataUtils` consistently.
- `{Entity}` creation uses `{Entity}.builder()...build()` and never `new {Entity}(...)`.
- `salt`, `createdAt`, `createdAtTs`, `updatedAt`, and `updatedAtTs` are not manually assigned in service-level entity operations.
- Query/list methods return `PageResponse<T>` where pagination is required.
- Remove-by-default behavior is soft delete (`remove`), not physical delete (`delete`).
- List/page methods use default sort `updatedAtTs` descending when no explicit sort rule is provided.

## Verification

From `{server-project-name}/` run:

```bash
./mvnw -pl {server-project-name}-{domain} -DskipTests compile
```

If tests exist, run:

```bash
./mvnw -pl {server-project-name}-{domain} test -Dtest={Context}ServiceTest
```

Then verify:

- Compilation succeeds with no import/type errors.
- Service signatures and DTO shapes match controller expectations.
- Policy checks cover all validation-sensitive paths.
- Transactional boundaries are explicit and minimal.
