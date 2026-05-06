---
name: siyukio-role-based-api-creator
description: Create or update role-based APIs in Siyukio Spring Boot domain modules with the default role set user, admin, and internal. Use when a domain must expose separate role APIs with role-specific controller, paths class, and DTO naming, keep API files under {domain}/api, {domain}/api/dto, {domain}/api/paths, and enforce access via @ApiController(roles = {...}).
---

# siyukio-role-based-api-creator

Create or refine role-based API classes for one domain context with fixed default roles: user, admin, and internal.

## Scope

Create or update files under:

```
{project-name}/{project-name}-common/src/main/java/{package-path}/common/constants/
└── RolesConstants.java

{project-name}/{project-name}-{domain}/src/main/java/{package-path}/{domain}/api/
├── {Context}Controller.java
├── {Context}AdminController.java
├── {Context}InternalController.java
├── paths/
│   ├── {Context}Paths.java
│   ├── {Context}AdminPaths.java
│   └── {Context}InternalPaths.java
└── dto/
    ├── {Context}Request.java
    ├── {Context}Response.java
    ├── {Context}AdminRequest.java
    ├── {Context}AdminResponse.java
    ├── {Context}InternalRequest.java
    └── {Context}InternalResponse.java
```

## Use this skill when

- A domain needs default role APIs for user, admin, and internal.
- You need to add role restrictions directly on controllers through `@ApiController(roles = {...})`.
- You need deterministic naming for role-specific controllers, paths classes, and DTOs in one shared `api` package tree.
- You want role values centralized in `RolesConstants`.

## Do not use this skill when

- You only need standard APIs without role partitioning. Use `$siyukio-api-creator`.
- You only need shared constants or utilities without API changes. Use `$siyukio-common-creator`.
- You are changing domain model, policy, or application service only.

## Required inputs

- `{project-name}`, `{package-name}`, and `{package-path}`
- `{domain}`: module domain in kebab-case, for example `user-management`
- `{Context}`: business context in PascalCase, for example `User`
- `{context}`: service variable name in camelCase, for example `user`
- Endpoint operations: `get`, `create`, `update`, `list`, `remove` (choose required subset)

## Execution workflow

### 1) Normalize default role model

Use fixed role categories and naming suffixes:

- user:
  - role constant: `RolesConstants.USER`
  - controller: `{Context}Controller`
  - paths class: `{Context}Paths`
  - DTO: `{Context}Request`, `{Context}Response`
- admin:
  - role constant: `RolesConstants.ADMIN`
  - controller: `{Context}AdminController`
  - paths class: `{Context}AdminPaths`
  - DTO: `{Context}AdminRequest`, `{Context}AdminResponse`
- internal:
  - role constant: `RolesConstants.INTERNAL`
  - controller: `{Context}InternalController`
  - paths class: `{Context}InternalPaths`
  - DTO: `{Context}InternalRequest`, `{Context}InternalResponse`

Keep controller package fixed to `{package-name}.{domain}.api`.

Paths naming contract (mandatory):

- user API paths class: `{Context}Paths`
- admin API paths class: `{Context}AdminPaths`
- internal API paths class: `{Context}InternalPaths`

Path value contract (mandatory):

- in `{Context}Paths`, each path value must be under `/{context}/*`
- in `{Context}AdminPaths`, each path value must be under `/admin/{context}/*`
- in `{Context}InternalPaths`, each path value must be under `/internal/{context}/*`

### 2) Create or update role constants first

Use `$siyukio-common-creator` to create or update:

`{project-name}/{project-name}-common/src/main/java/{package-path}/common/constants/RolesConstants.java`

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

`{project-name}/{project-name}-{domain}/src/main/java/{package-path}/{domain}/api/`

Controller rules:

- Use controller naming by role:
  - user: `{Context}Controller`
  - admin: `{Context}AdminController`
  - internal: `{Context}InternalController`
- Use paths class naming by role:
  - user: `{Context}Paths`
  - admin: `{Context}AdminPaths`
  - internal: `{Context}InternalPaths`
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

- `{Context}AdminController` -> `{Context}AdminPaths`
- `{Context}InternalController` -> `{Context}InternalPaths`

### 4) Create or update DTO naming by role

Under `{domain}/api/dto`, keep role-specific DTO names:

- user: `{Context}Request`, `{Context}Response`
- admin: `{Context}AdminRequest`, `{Context}AdminResponse`
- internal: `{Context}InternalRequest`, `{Context}InternalResponse`

Keep request/response classes aligned with controller endpoints and application service contracts.

### 5) Handle multi-role controller access when needed

- Keep one default role per controller class.
- If a controller must support multiple roles, declare multiple constants explicitly:
  - `roles = {RolesConstants.ADMIN, RolesConstants.INTERNAL}`
- Keep this as an exception, not the default mode.

### 6) Verify implementation

From `{project-name}/` run:

```bash
./mvnw -pl {project-name}-common,{project-name}-{domain} -DskipTests compile
```

If controller tests exist, run:

```bash
./mvnw -pl {project-name}-{domain} test -Dtest={Context}ApiControllerTest
```

Then confirm:

- `RolesConstants` exists in common module and includes requested roles.
- Role-based controllers exist under `api/` with names:
  - `{Context}Controller`
  - `{Context}AdminController`
  - `{Context}InternalController`
- `@ApiController` roles use `RolesConstants` and match each controller role.
- API path strings are not hard-coded; they come from role-specific classes:
  - user: `{Context}Paths`
  - admin: `{Context}AdminPaths`
  - internal: `{Context}InternalPaths`
- Path values in each role-specific paths class follow the required base pattern:
  - `{Context}Paths` -> `/{context}/*`
  - `{Context}AdminPaths` -> `/admin/{context}/*`
  - `{Context}InternalPaths` -> `/internal/{context}/*`
- DTO names follow the role convention in `api/dto`.
- DTO and service signatures remain aligned with the application layer.
