---
name: siyukio-module-creator
description: Create or update a complete Siyukio server domain module (module pom, parent/bootstrap wiring, and API/Application/Model layer scaffold) for Spring Boot. Use when adding a new business bounded context under `{server-project-name}-{domain}` and coordinating `$siyukio-model-creator`, `$siyukio-application-creator`, and `$siyukio-api-creator`.
---

# siyukio-module-creator

Create one domain module end-to-end in the Siyukio server project.

## Scope

Target module layout:

```text
{server-project-name}/{server-project-name}-{domain}/
├── pom.xml
├── src/main/java/{package-path}/{domain}/
│   ├── api/
│   ├── application/
│   └── model/
│       ├── entity/
│       ├── policy/
│       └── errors/
└── src/main/resources/{domain}/
```

Also update:

- `{server-project-name}/pom.xml`
- `{server-project-name}/{server-project-name}-bootstrap/pom.xml`

## Use this skill when

- Add a new domain/bounded-context module.
- Register a domain module into parent aggregation and bootstrap runtime profiles.
- Bootstrap API + Application + Domain layers in one workflow.

## Do not use this skill when

- Work only touches one layer. Use the corresponding focused skill:
  - `$siyukio-model-creator`
  - `$siyukio-application-creator`
  - `$siyukio-api-creator`
- Work is for web/desktop/console projects.

## Required inputs

- `{server-project-name}`: Maven aggregator artifact (kebab-case).
- `{package-name}`: base Java package.
- `{package-path}`: slash package path (example: `io/github/siyukio/samples`).
- `{domain}`: module suffix in kebab-case (example: `user-management`).
- `{Context}`: context name in PascalCase (example: `User`).
- `{Entity}`: primary domain entity in PascalCase (example: `User`).

Derived values:

- Module artifact: `{server-project-name}-{domain}`
- Java package root: `{package-name}.{domain}`

## Preconditions

- `{server-project-name}/pom.xml` exists and is a packaging `pom` aggregator.
- `{server-project-name}/{server-project-name}-bootstrap/pom.xml` exists.
- Generate all code/comments in English only.

## Execution workflow

### 1) Normalize and validate inputs

- Ensure `{domain}` is kebab-case.
- Ensure `{Context}` and `{Entity}` are PascalCase.
- Reuse existing module if already present; update incrementally instead of rewriting unrelated content.

### 2) Scaffold module directories

Create missing directories only:

```text
{server-project-name}/{server-project-name}-{domain}/src/main/java/{package-path}/{domain}/api
{server-project-name}/{server-project-name}-{domain}/src/main/java/{package-path}/{domain}/application
{server-project-name}/{server-project-name}-{domain}/src/main/java/{package-path}/{domain}/model/entity
{server-project-name}/{server-project-name}-{domain}/src/main/java/{package-path}/{domain}/model/policy
{server-project-name}/{server-project-name}-{domain}/src/main/java/{package-path}/{domain}/model/errors
{server-project-name}/{server-project-name}-{domain}/src/main/resources/{domain}
```

### 3) Create or align module `pom.xml`

File:

`{server-project-name}/{server-project-name}-{domain}/pom.xml`

Rules:

- Parent points to `{server-project-name}`.
- `artifactId` is `{server-project-name}-{domain}`.
- Keep existing dependencies; add missing required ones once:

```xml
<dependency>
    <groupId>{package-name}</groupId>
    <artifactId>{server-project-name}-common</artifactId>
</dependency>
<dependency>
    <groupId>io.github.siyukio</groupId>
    <artifactId>spring-siyukio-application</artifactId>
</dependency>
<dependency>
    <groupId>io.github.siyukio</groupId>
    <artifactId>spring-siyukio-application-acp</artifactId>
</dependency>
<dependency>
    <groupId>io.github.siyukio</groupId>
    <artifactId>spring-siyukio-postgresql</artifactId>
</dependency>
```

### 4) Register module in parent `pom.xml`

File:

`{server-project-name}/pom.xml`

Update idempotently:

1. Add `<module>{server-project-name}-{domain}</module>` under `<modules>` (once).
2. Add managed dependency under `<dependencyManagement><dependencies>`:

```xml
<dependency>
    <groupId>{package-name}</groupId>
    <artifactId>{server-project-name}-{domain}</artifactId>
    <version>${project.version}</version>
</dependency>
```

### 5) Wire bootstrap profiles

File:

`{server-project-name}/{server-project-name}-bootstrap/pom.xml`

Update idempotently:

- Ensure profile `{domain}` exists with:
  - `<deployment-profile>{domain}</deployment-profile>`
  - dependency on `{server-project-name}-{domain}`
- Ensure profile `full` includes dependency on `{server-project-name}-{domain}`.
- Do not duplicate dependencies if already present.

### 6) Generate layer code with sibling skills

Execute in this order and pass the same normalized variables:

1. `$siyukio-model-creator`
2. `$siyukio-application-creator`
3. `$siyukio-api-creator`

Minimum expected outputs:

- Model: `{Entity}.java` (+ optional `{Entity}Policy.java`, `{Entity}Errors.java`)
- Application: `{Context}Service.java`
- API: `{Context}Controller.java`, `{Context}Paths.java`, `{Context}Request.java`, `{Context}Response.java`

### 7) Consistency checks

- Package paths follow `{package-name}.{domain}.*`.
- API depends on Application; Application depends on Domain.
- No raw endpoint strings in controller methods (use `{Context}Paths`).
- New profile/module registration is additive and non-destructive.

## Verification

From `{server-project-name}/` run:

```bash
./mvnw -pl {server-project-name}-{domain},{server-project-name}-bootstrap -DskipTests compile
```

Optional (when tests exist):

```bash
./mvnw -pl {server-project-name}-{domain} test
```

Before finishing, confirm:

- Module compiles.
- Parent `modules` and `dependencyManagement` include the new module.
- Bootstrap `{domain}` and `full` profiles both reference the module.
- Layer files exist in expected directories.

## Related skills

- `$siyukio-model-creator`
- `$siyukio-application-creator`
- `$siyukio-api-creator`
- `$siyukio-unit-test-creator` (after module generation, when test scaffolding is required)
