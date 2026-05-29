---
name: siyukio-role-based-api-creator
description: Create or update role-based APIs in Siyukio Spring Boot domain modules with the default authorization role set user, admin, internal, app, and member. Use when a domain must expose separate role APIs with role-specific controller, paths class, and DTO naming, keep API files under {domain}/api, {domain}/api/dto, {domain}/api/paths, enforce access via @ApiController/@ApiMapping authorization, and auto-invoke $siyukio-console-api-creator when admin API contracts change.
---

# siyukio-role-based-api-creator

Create or refine role-based API classes for one domain context with fixed default roles: user, admin, internal, app, and member.

## Scope

Create or update files under:

```
{server-project-name}/{server-project-name}-{domain}/src/main/java/{package-path}/{domain}/api/
├── {Context}Controller.java
├── Admin{Context}Controller.java
├── Internal{Context}Controller.java
├── App{Context}Controller.java
├── Member{Context}Controller.java
├── paths/
│   ├── {Context}Paths.java
│   ├── Admin{Context}Paths.java
│   ├── Internal{Context}Paths.java
│   ├── App{Context}Paths.java
│   └── Member{Context}Paths.java
└── dto/
    ├── {Context}{Operation}Request.java
    ├── {Context}{Operation}Response.java
    ├── Admin{Context}{Operation}Request.java
    ├── Admin{Context}{Operation}Response.java
    ├── Internal{Context}{Operation}Request.java
    ├── Internal{Context}{Operation}Response.java
    ├── App{Context}{Operation}Request.java
    ├── App{Context}{Operation}Response.java
    ├── Member{Context}{Operation}Request.java
    └── Member{Context}{Operation}Response.java
```

## Use this skill when

- A domain needs default role APIs for user, admin, internal, app, and member.
- You need to add role restrictions through `@ApiController` and `@ApiMapping` with `authorization`.
- You need deterministic naming for role-specific controllers, paths classes, and DTOs in one shared `api` package tree.
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

### 1) Normalize authorization role model

Use fixed role categories and naming prefixes.

- user:
  - controller: `{Context}Controller`
  - paths class: `{Context}Paths`
  - DTO (operation-based examples): `{Context}CreateRequest`, `{Context}UpdateRequest`, `{Context}Response`
  - controller authorization: default user, do not configure `authorization` on `@ApiController`
  - mapping authorization: default user, do not configure `authorization` on `@ApiMapping`
- admin:
  - controller: `Admin{Context}Controller`
  - paths class: `Admin{Context}Paths`
  - DTO (operation-based examples): `Admin{Context}CreateRequest`, `Admin{Context}UpdateRequest`, `Admin{Context}Response`
  - controller authorization: `@ApiController(authorization = @Authorization(type = Token.PRINCIPAL_TYPE_ADMIN_USER))`
- internal:
  - controller: `Internal{Context}Controller`
  - paths class: `Internal{Context}Paths`
  - DTO (operation-based examples): `Internal{Context}CreateRequest`, `Internal{Context}UpdateRequest`, `Internal{Context}Response`
  - controller authorization: `@ApiController(authorization = @Authorization(type = Token.PRINCIPAL_TYPE_INTERNAL))`
- app:
  - controller: `App{Context}Controller`
  - paths class: `App{Context}Paths`
  - DTO (operation-based examples): `App{Context}CreateRequest`, `App{Context}UpdateRequest`, `App{Context}Response`
  - controller authorization: `@ApiController(authorization = @Authorization(type = Token.PRINCIPAL_TYPE_APP))`
- member:
  - controller: `Member{Context}Controller`
  - paths class: `Member{Context}Paths`
  - DTO (operation-based examples): `Member{Context}CreateRequest`, `Member{Context}UpdateRequest`, `Member{Context}Response`
  - controller authorization: `@ApiController(authorization = @Authorization(type = Token.PRINCIPAL_TYPE_MEMBER))`

Keep controller package fixed to `{package-name}.{domain}.api`.

Paths naming contract (mandatory):

- user API paths class: `{Context}Paths`
- admin API paths class: `Admin{Context}Paths`
- internal API paths class: `Internal{Context}Paths`
- app API paths class: `App{Context}Paths`
- member API paths class: `Member{Context}Paths`

Path value contract (mandatory):

- in `{Context}Paths`, each path value must be under `/{context}/*`
- in `Admin{Context}Paths`, each path value must be under `/admin/{context}/*`
- in `Internal{Context}Paths`, each path value must be under `/internal/{context}/*`
- in `App{Context}Paths`, each path value must be under `/app/{context}/*`
- in `Member{Context}Paths`, each path value must be under `/member/{context}/*`

Authorization precedence contract (mandatory):

- `@ApiController` defines the default authorization for the controller.
- `@ApiMapping` defines method-level authorization.
- If `@ApiMapping` has `authorization`, it overrides `@ApiController` authorization.
- For user-role endpoints, both `@ApiController` and `@ApiMapping` should normally omit `authorization`.

### 2) Create or update role-based controllers in shared `api` package

Use `$siyukio-api-creator` to generate or refine API layer artifacts, then place controllers directly under:

`{server-project-name}/{server-project-name}-{domain}/src/main/java/{package-path}/{domain}/api/`

Controller rules:

- Use controller naming by role:
  - user: `{Context}Controller`
  - admin: `Admin{Context}Controller`
  - internal: `Internal{Context}Controller`
  - app: `App{Context}Controller`
  - member: `Member{Context}Controller`
- Use paths class naming by role:
  - user: `{Context}Paths`
  - admin: `Admin{Context}Paths`
  - internal: `Internal{Context}Paths`
  - app: `App{Context}Paths`
  - member: `Member{Context}Paths`
- Annotate controllers with authorization:
  - user: `@ApiController(summary = "...")` (no `authorization` needed)
  - admin: `@ApiController(summary = "...", authorization = @Authorization(type = Token.PRINCIPAL_TYPE_ADMIN_USER))`
  - internal: `@ApiController(summary = "...", authorization = @Authorization(type = Token.PRINCIPAL_TYPE_INTERNAL))`
  - app: `@ApiController(summary = "...", authorization = @Authorization(type = Token.PRINCIPAL_TYPE_APP))`
  - member: `@ApiController(summary = "...", authorization = @Authorization(type = Token.PRINCIPAL_TYPE_MEMBER))`
- Keep endpoint mappings on role-specific paths constants only.
- Configure `@ApiMapping.authorization` only when method-level authorization differs from controller-level default.
- If both controller and method specify authorization, method-level (`@ApiMapping`) takes precedence.
- Keep files inside `api`, `api/dto`, `api/paths` only.
- Inject `{Context}Service` from application layer.

Template:

```java
package {package-name}.{domain}.api;

import {package-name}.{domain}.api.paths.{Context}Paths;
import io.github.siyukio.spring.annotations.api.ApiController;
import io.github.siyukio.spring.annotations.api.ApiMapping;
import io.github.siyukio.spring.annotations.auth.Authorization;
import io.github.siyukio.tools.api.token.Token;

@ApiController(
        summary = "{Context} user API"
)
public class {Context}Controller {

    @ApiMapping(path = {Context}Paths.LIST, summary = "Query {Context} list")
    public void list() {
        // Implement with {Context}Service according to domain contract.
    }
}
```

Admin controller template:

```java
@ApiController(
        summary = "{Context} admin API",
        authorization = @Authorization(type = Token.PRINCIPAL_TYPE_ADMIN_USER)
)
public class Admin{Context}Controller {
    // ...
}
```

Internal controller template:

```java
@ApiController(
        summary = "{Context} internal API",
        authorization = @Authorization(type = Token.PRINCIPAL_TYPE_INTERNAL)
)
public class Internal{Context}Controller {
    // ...
}
```

App controller template:

```java
@ApiController(
        summary = "{Context} app API",
        authorization = @Authorization(type = Token.PRINCIPAL_TYPE_APP)
)
public class App{Context}Controller {
    // ...
}
```

Member controller template:

```java
@ApiController(
        summary = "{Context} member API",
        authorization = @Authorization(type = Token.PRINCIPAL_TYPE_MEMBER)
)
public class Member{Context}Controller {
    // ...
}
```

Method-level override template:

```java
@ApiMapping(
        path = Admin{Context}Paths.LIST,
        summary = "Query admin {Context} list",
        authorization = @Authorization(type = Token.PRINCIPAL_TYPE_INTERNAL)
)
public void listAsInternalOnly() {
    // This method authorization overrides controller authorization.
}
```

For role-specific controllers, import and use their corresponding path classes:

- `Admin{Context}Controller` -> `Admin{Context}Paths`
- `Internal{Context}Controller` -> `Internal{Context}Paths`
- `App{Context}Controller` -> `App{Context}Paths`
- `Member{Context}Controller` -> `Member{Context}Paths`

### 3) Create or update DTO naming by role

Under `{domain}/api/dto`, keep role-specific DTO names:

- user examples: `{Context}CreateRequest`, `{Context}UpdateRequest`, `{Context}Response`
- admin examples: `Admin{Context}CreateRequest`, `Admin{Context}UpdateRequest`, `Admin{Context}Response`
- internal examples: `Internal{Context}CreateRequest`, `Internal{Context}UpdateRequest`, `Internal{Context}Response`
- app examples: `App{Context}CreateRequest`, `App{Context}UpdateRequest`, `App{Context}Response`
- member examples: `Member{Context}CreateRequest`, `Member{Context}UpdateRequest`, `Member{Context}Response`

For operation-specific DTOs, keep the same role prefix rule:

- user APIs: `{Context}{Operation}Request` / `{Context}{Operation}Response`
- admin APIs: `Admin{Context}{Operation}Request` / `Admin{Context}{Operation}Response`
- internal APIs: `Internal{Context}{Operation}Request` / `Internal{Context}{Operation}Response`
- app APIs: `App{Context}{Operation}Request` / `App{Context}{Operation}Response`
- member APIs: `Member{Context}{Operation}Request` / `Member{Context}{Operation}Response`

Keep request/response classes aligned with controller endpoints and application service contracts.

### 4) Verify implementation

From `{server-project-name}/` run:

```bash
./mvnw -pl {server-project-name}-{domain} -DskipTests compile
```

If controller tests exist, run:

```bash
./mvnw -pl {server-project-name}-{domain} test -Dtest={Context}ApiControllerTest
```

Then confirm:

- Role-based controllers exist under `api/` with names:
  - `{Context}Controller`
  - `Admin{Context}Controller`
  - `Internal{Context}Controller`
  - `App{Context}Controller`
  - `Member{Context}Controller`
- `@ApiController` and `@ApiMapping` do not use `roles`.
- User APIs keep default authorization by omitting `authorization` on controller/method unless override is needed.
- Non-user controller authorizations match required `Token.PRINCIPAL_TYPE_*` constants.
- When method-level authorization is configured in `@ApiMapping`, it overrides controller-level authorization by design.
- API path strings are not hard-coded; they come from role-specific classes:
  - user: `{Context}Paths`
  - admin: `Admin{Context}Paths`
  - internal: `Internal{Context}Paths`
  - app: `App{Context}Paths`
  - member: `Member{Context}Paths`
- Path values in each role-specific paths class follow the required base pattern:
  - `{Context}Paths` -> `/{context}/*`
  - `Admin{Context}Paths` -> `/admin/{context}/*`
  - `Internal{Context}Paths` -> `/internal/{context}/*`
  - `App{Context}Paths` -> `/app/{context}/*`
  - `Member{Context}Paths` -> `/member/{context}/*`
- DTO names follow the role convention in `api/dto`.
- DTO and service signatures remain aligned with the application layer.
- If admin API contracts changed, the matching console API files are updated in the same task run.

### 5) Sync console API for admin endpoint changes (conditional, final step)

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
