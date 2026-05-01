---
name: siyukio-application-creator
description: Create or update the Siyukio application service layer in Spring Boot domain modules. Use when implementing use-case orchestration between API and domain layers, adding query/command CRUD methods, enforcing domain policy checks, mapping DTOs and entities, or setting transaction boundaries.
---

# siyukio-application-creator

Create or refine one application service in a Siyukio domain module.

## Scope

Write or update files under:

```
{project-name}/{project-name}-{domain}/src/main/java/{package-path}/{domain}/application/
└── {Context}Service.java
```

## Use this skill when

- Add a new application service for one domain context.
- Implement use-case orchestration that calls domain policies and persistence.
- Add or adjust list/get/create/update/delete service methods.
- Align API contracts with application method signatures.

## Do not use this skill when

- Work is domain entity/errors/policy design only. Use `$siyukio-model-creator`.
- Work is controller/path/DTO exposure only. Use `$siyukio-api-creator`.
- Work is external integration/infrastructure only. Use `$siyukio-create-infrastructure`.

## Preconditions

- Target domain module exists: `{project-name}/{project-name}-{domain}`.
- Domain model entity exists: `{package-name}.{domain}.model.entity.{Entity}`.
- Service context is clear: `{Context}` (PascalCase), `{context}` (camelCase), `{entity}` (camelCase).
- API method contracts are known, or are created in the same task using `$siyukio-api-creator`.
- Required policy checks exist, or are added first using `$siyukio-model-creator`.

## Execution workflow

### 1) Normalize context inputs

Extract and normalize:

- `{domain}`: kebab-case domain module name (example: `user-management`)
- `{Context}`: PascalCase business context (example: `User`)
- `{entity}`: camelCase entity variable name (example: `user`)
- Required operations: `list`, `get`, `create`, `update`, `delete`
- Validation rules: existence, enabled state, uniqueness, ownership, etc.

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
`{project-name}/{project-name}-{domain}/src/main/java/{package-path}/{domain}/application/{Context}Service.java`

Implementation rules:

- Annotate class with `@Service` (and `@Slf4j` when logging is needed).
- Inject `PgEntityDao<{Entity}>` for persistence operations.
- Inject `{Entity}Policy` for business validation/invariants.
- Use `XDataUtils.copy(...)` for entity/DTO transformation.
- Use `XDataUtils.mergeNotNul(source, target)` for partial updates.
- Build list filters with `QueryBuilders.boolQuery()` and `termQuery/rangeQuery` as needed.
- Build sort with `SortBuilders.fieldSort(...).order(SortOrder.XXX)`.
- Add `@Transactional` on methods that perform multi-step write operations.

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
}
```

### 5) Apply consistency checks

Confirm:

- Package path is `{package-name}.{domain}.application`.
- Class name is `{Context}Service`.
- Method names/params/returns match API contracts.
- Validation logic is policy-first.
- Conversion style uses `XDataUtils` consistently.
- Query/list methods return `PageResponse<T>` where pagination is required.

## Verification

From repository root, run:

```bash
cd siyukio-studio-server
./mvnw -pl {project-name}-{domain} -DskipTests compile
```

If tests exist, run:

```bash
cd siyukio-studio-server
./mvnw -pl {project-name}-{domain} test -Dtest={Context}ServiceTest
```

Then verify:

- Compilation succeeds with no import/type errors.
- Service signatures and DTO shapes match controller expectations.
- Policy checks cover all validation-sensitive paths.
- Transactional boundaries are explicit and minimal.
