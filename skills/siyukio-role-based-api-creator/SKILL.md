---
name: siyukio-role-based-api-creator
description: Create or update role-based APIs in Siyukio Spring Boot domain modules with the default role set user, admin, and internal. Use when a domain must expose separate role APIs with role-specific controller, paths class, and DTO naming, keep API files under {domain}/api, {domain}/api/dto, {domain}/api/paths, enforce access via @ApiController(roles = {...}), and auto-invoke $siyukio-console-api-creator when admin API contracts change.
---

# siyukio-role-based-api-creator

Create or refine role-based API classes for one domain context with fixed default roles: user, admin, and internal.

## Scope

Create or update files under:

```
{server-project-name}/{server-project-name}-common/src/main/java/{package-path}/common/constants/
└── RolesConstants.java

{server-project-name}/{server-project-name}-{domain}/src/main/java/{package-path}/{domain}/api/
├── {Context}Controller.java
├── Admin{Context}Controller.java
├── Internal{Context}Controller.java
├── paths/
│   ├── {Context}Paths.java
│   ├── Admin{Context}Paths.java
│   └── Internal{Context}Paths.java
└── dto/
    ├── {Context}Request.java
    ├── {Context}Response.java
    ├── Admin{Context}Request.java
    ├── Admin{Context}Response.java
    ├── Internal{Context}Request.java
    └── Internal{Context}Response.java
```

## Use this skill when

- A domain needs default role APIs for user, admin, and internal.
- You need to add role restrictions directly on controllers through `@ApiController(roles = {...})`.
- You need deterministic naming for role-specific controllers, paths classes, and DTOs in one shared `api` package tree.
- You want role values centralized in `RolesConstants`.
- You need admin API changes to stay synchronized with console TypeScript API contracts.

## Do not use this skill when

- You only need standard APIs without role partitioning. Use `$siyukio-api-creator`.
- You only need shared constants or utilities without API changes. Use `$siyukio-common-creator`.
- You are changing domain model, policy, or application service only.

## Required inputs

- `{server-project-name}`, `{package-name}`, and `{package-path}`
- `{domain}`: module domain in kebab-case, for example `user-management`
- `{Context}`: business context in PascalCase, for example `User`
- `{context}`: service variable name in camelCase, for example `user`
- Endpoint operations: `get`, `create`, `update`, `list`, `remove` (choose required subset)

## Execution workflow

### 1) Normalize default role model

Use fixed role categories and naming prefixes:

- user:
  - role constant: `RolesConstants.USER`
  - controller: `{Context}Controller`
  - paths class: `{Context}Paths`
  - DTO (operation-based examples): `{Context}CreateRequest`, `{Context}UpdateRequest`, `{Context}Response`
- admin:
  - role constant: `RolesConstants.ADMIN`
  - controller: `Admin{Context}Controller`
  - paths class: `Admin{Context}Paths`
  - DTO (operation-based examples): `Admin{Context}CreateRequest`, `Admin{Context}UpdateRequest`, `Admin{Context}Response`
- internal:
  - role constant: `RolesConstants.INTERNAL`
  - controller: `Internal{Context}Controller`
  - paths class: `Internal{Context}Paths`
  - DTO (operation-based examples): `Internal{Context}CreateRequest`, `Internal{Context}UpdateRequest`, `Internal{Context}Response`

Keep controller package fixed to `{package-name}.{domain}.api`.

Paths naming contract (mandatory):

- user API paths class: `{Context}Paths`
- admin API paths class: `Admin{Context}Paths`
- internal API paths class: `Internal{Context}Paths`

Path value contract (mandatory):

- in `{Context}Paths`, each path value must be under `/{context}/*`
- in `Admin{Context}Paths`, each path value must be under `/admin/{context}/*`
- in `Internal{Context}Paths`, each path value must be under `/internal/{context}/*`

### 2) Create or update role constants first

Use `$siyukio-common-creator` to create or update:

`{server-project-name}/{server-project-name}-common/src/main/java/{package-path}/common/constants/RolesConstants.java`

Rules:

- Declare constants in Java interface form.
- Use `UPPER_SNAKE_CASE` names.
- Keep values stable and explicit (normally same as key text).
- Start with at least `USER`, `ADMIN`, and `INTERNAL`.

Template:

```java
package {package-name}.common.constants;

public interface RolesConstants {

    String USER = "USER";
    String ADMIN = "ADMIN";
    String INTERNAL = "INTERNAL";
}
```

### 3) Create or update role-based controllers in shared `api` package

Use `$siyukio-api-creator` to generate or refine API layer artifacts, then place controllers directly under:

`{server-project-name}/{server-project-name}-{domain}/src/main/java/{package-path}/{domain}/api/`

Controller rules:

- Use controller naming by role:
  - user: `{Context}Controller`
  - admin: `Admin{Context}Controller`
  - internal: `Internal{Context}Controller`
- Use paths class naming by role:
  - user: `{Context}Paths`
  - admin: `Admin{Context}Paths`
  - internal: `Internal{Context}Paths`
- Annotate each controller with explicit role:
  - user: `@ApiController(roles = {RolesConstants.USER})`
  - admin: `@ApiController(roles = {RolesConstants.ADMIN})`
  - internal: `@ApiController(roles = {RolesConstants.INTERNAL})`
- Prefer constants, never hard-coded role strings in annotations.
- Keep endpoint mappings on role-specific paths constants only.
- Keep files inside `api`, `api/dto`, `api/paths` only.
- Inject `{Context}Service` from application layer.

Template:

```java
package {package-name}.{domain}.api;

import {package-name}.common.constants.RolesConstants;
import {package-name}.{domain}.api.paths.{Context}Paths;
import io.github.siyukio.spring.annotations.api.ApiController;
import io.github.siyukio.spring.annotations.api.ApiMapping;

@ApiController(
        summary = "{Context} user API",
        roles = {RolesConstants.USER}
)
public class {Context}Controller {

    @ApiMapping(path = {Context}Paths.LIST, summary = "Query {Context} list")
    public void list() {
        // Implement with {Context}Service according to domain contract.
    }
}
```

For admin and internal controllers, import and use their corresponding path classes:

- `Admin{Context}Controller` -> `Admin{Context}Paths`
- `Internal{Context}Controller` -> `Internal{Context}Paths`

### 4) Create or update DTO naming by role

Under `{domain}/api/dto`, keep role-specific DTO names:

- user examples: `{Context}CreateRequest`, `{Context}UpdateRequest`, `{Context}Response`
- admin examples: `Admin{Context}CreateRequest`, `Admin{Context}UpdateRequest`, `Admin{Context}Response`
- internal examples: `Internal{Context}CreateRequest`, `Internal{Context}UpdateRequest`, `Internal{Context}Response`

For operation-specific DTOs, keep the same role prefix rule:

- user APIs: `{Context}{Operation}Request` / `{Context}{Operation}Response`
- admin APIs: `Admin{Context}{Operation}Request` / `Admin{Context}{Operation}Response`
- internal APIs: `Internal{Context}{Operation}Request` / `Internal{Context}{Operation}Response`

Keep request/response classes aligned with controller endpoints and application service contracts.

### 5) Verify implementation

From `{server-project-name}/` run:

```bash
./mvnw -pl {server-project-name}-common,{server-project-name}-{domain} -DskipTests compile
```

If controller tests exist, run:

```bash
./mvnw -pl {server-project-name}-{domain} test -Dtest={Context}ApiControllerTest
```

Then confirm:

- `RolesConstants` exists in common module and includes requested roles.
- Role-based controllers exist under `api/` with names:
  - `{Context}Controller`
  - `Admin{Context}Controller`
  - `Internal{Context}Controller`
- `@ApiController` roles use `RolesConstants` and match each controller role.
- API path strings are not hard-coded; they come from role-specific classes:
  - user: `{Context}Paths`
  - admin: `Admin{Context}Paths`
  - internal: `Internal{Context}Paths`
- Path values in each role-specific paths class follow the required base pattern:
  - `{Context}Paths` -> `/{context}/*`
  - `Admin{Context}Paths` -> `/admin/{context}/*`
  - `Internal{Context}Paths` -> `/internal/{context}/*`
- DTO names follow the role convention in `api/dto`.
- DTO and service signatures remain aligned with the application layer.
- If admin API contracts changed, the matching console API files are updated in the same task run.

### 6) Sync console API for admin endpoint changes (conditional, final step)

At the end of the task, detect whether this task introduced or changed admin role API contracts.

Treat any of the following as admin API changes:

- Added or modified `Admin{Context}Controller.java`
- Added or modified `Admin{Context}Paths.java`
- Added, removed, or modified admin DTOs under `api/dto/` (for example `Admin{Context}*Request` and `Admin{Context}*Response`)
- Added, removed, or modified `@ApiMapping` methods, request/response DTO types, or path constant bindings in admin controllers

If admin API changes exist:

- Invoke `$siyukio-console-api-creator`.
- Sync only the changed admin controller contracts to corresponding console TypeScript API files under:
  - `{console-project-name}/src/api/{Context}Api.ts`

If no admin API changes exist:

- Skip console API sync and state explicitly in the completion report that no admin endpoint contract changes were detected.

## Acceptance criteria (mandatory)

- If admin API contracts change, console API must be synchronized via `$siyukio-console-api-creator` before task completion.
- Any result with admin API contract changes but without corresponding console API sync is incomplete.
