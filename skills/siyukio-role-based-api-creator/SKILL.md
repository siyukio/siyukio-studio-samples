---
name: siyukio-role-based-api-creator
description: Create or update role-based APIs in Siyukio Spring Boot domain modules by combining shared role constants and role-scoped controllers. Use when a domain must expose different APIs for roles such as USER, ADMIN, and INTERNAL, and enforce access via @ApiController(roles = {...}).
---

# siyukio-role-based-api-creator

Create or refine role-scoped controllers for one domain context, and enforce role authorization at controller level through shared constants.

## Scope

Create or update files under:

```
{project-name}/{project-name}-common/src/main/java/{package-path}/common/constants/
└── RolesConstants.java

{project-name}/{project-name}-{domain}/src/main/java/{package-path}/{domain}/api/
├── {role}/
│   └── {Context}ApiController.java
├── paths/
│   └── {Context}Paths.java
└── dto/
    ├── {Context}Request.java
    └── {Context}Response.java
```

## Use this skill when

- A domain needs separate APIs by role, such as user API, admin API, and internal API.
- You need to add role restrictions directly on controllers through `@ApiController(roles = {...})`.
- You want role values centralized in a shared constants interface.

## Do not use this skill when

- You only need standard APIs without role partitioning. Use `$siyukio-api-creator`.
- You only need shared constants or utilities without API changes. Use `$siyukio-common-creator`.
- You are changing domain model, policy, or application service only.

## Required inputs

- `{project-name}`, `{package-name}`, and `{package-path}`
- `{domain}`: module domain in kebab-case, for example `user-management`
- `{Context}`: business context in PascalCase, for example `User`
- `{context}`: service variable name in camelCase, for example `user`
- `{role}`: role package segment in lower-case, for example `user`, `admin`, `internal`
- `{ROLE}`: role constant key in `UPPER_SNAKE_CASE`, for example `USER`, `ADMIN`, `INTERNAL`
- Endpoint operations: `get`, `create`, `update`, `list`, `remove` (choose required subset)

## Execution workflow

### 1) Normalize role inputs

- Map role names to both package and constant forms:
  - package: `{role}` (lower-case)
  - constant: `{ROLE}` (upper snake case)
- Keep one source of truth for role constants: `RolesConstants`.
- Confirm controller naming and package:
  - Class: `{Context}ApiController`
  - Package: `{package-name}.{domain}.api.{role}`

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

### 3) Create or update role-scoped API controller

Use `$siyukio-api-creator` to generate or refine API layer artifacts, then place controller in role package:

`{project-name}/{project-name}-{domain}/src/main/java/{package-path}/{domain}/api/{role}/{Context}ApiController.java`

Controller rules:

- Annotate controller with explicit roles:
  - `@ApiController(roles = {RolesConstants.{ROLE}})`
- Prefer constants, never hard-coded role strings in annotations.
- Keep endpoint mappings on `{Context}Paths` constants.
- Reuse `api/dto` and `api/paths` conventions from `$siyukio-api-creator`.
- Inject `{Context}Service` from application layer.

Template:

```java
package {package-name}.{domain}.api.{role};

import {package-name}.common.constants.RolesConstants;
import {package-name}.{domain}.api.paths.{Context}Paths;
import io.github.siyukio.spring.annotations.api.ApiController;
import io.github.siyukio.spring.annotations.api.ApiMapping;

@ApiController(
        summary = "{Context} {ROLE} API",
        roles = {RolesConstants.{ROLE}}
)
public class {Context}ApiController {

    @ApiMapping(path = {Context}Paths.LIST, summary = "Query {Context} list")
    public void list() {
        // Implement with {Context}Service according to domain contract.
    }
}
```

### 4) Handle multi-role controller access when needed

- For shared access, declare multiple role constants:
  - `roles = {RolesConstants.ADMIN, RolesConstants.INTERNAL}`
- Keep roles on controller annotation unless endpoint-level override is explicitly required by project conventions.

### 5) Verify implementation

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
- Role-based controller exists under `api/{role}/`.
- `@ApiController` has `roles = {...}` using `RolesConstants`.
- API path strings are not hard-coded; they come from `{Context}Paths`.
- DTO and service signatures remain aligned with the application layer.
