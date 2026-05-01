---
name: siyukio-create-module
description: Create or update a complete Siyukio server domain module (module pom, parent/bootstrap wiring, and API/Application/Domain layer scaffold) for Spring Boot. Use when adding a new business bounded context under `{project-name}-{domain}` and coordinating `$siyukio-domain-creator`, `$siyukio-application-creator`, and `$siyukio-api-creator`.
---

# siyukio-create-module

Create one domain module end-to-end in the Siyukio server project.

## Scope

Target module layout:

```text
{project-name}/{project-name}-{domain}/
├── pom.xml
├── src/main/java/{package-path}/{domain}/
│   ├── api/
│   ├── application/
│   └── domain/
│       ├── model/
│       ├── policy/
│       └── errors/
└── src/main/resources/{domain}/
```

Also update:

- `{project-name}/pom.xml`
- `{project-name}/{project-name}-bootstrap/pom.xml`

## Use this skill when

- Add a new domain/bounded-context module.
- Register a domain module into parent aggregation and bootstrap runtime profiles.
- Bootstrap API + Application + Domain layers in one workflow.

## Do not use this skill when

- Work only touches one layer. Use the corresponding focused skill:
  - `$siyukio-domain-creator`
  - `$siyukio-application-creator`
  - `$siyukio-api-creator`
- Work is for web/desktop/console projects.

## Required inputs

- `{project-name}`: Maven aggregator artifact (kebab-case).
- `{package-name}`: base Java package.
- `{package-path}`: slash package path (example: `io/github/siyukio/samples`).
- `{domain}`: module suffix in kebab-case (example: `user-management`).
- `{Context}`: context name in PascalCase (example: `User`).
- `{Entity}`: primary domain entity in PascalCase (example: `User`).

Derived values:

- Module artifact: `{project-name}-{domain}`
- Java package root: `{package-name}.{domain}`

## Preconditions

- `{project-name}/pom.xml` exists and is a packaging `pom` aggregator.
- `{project-name}/{project-name}-bootstrap/pom.xml` exists.
- Generate all code/comments in English only.

## Execution workflow

### 1) Normalize and validate inputs

- Ensure `{domain}` is kebab-case.
- Ensure `{Context}` and `{Entity}` are PascalCase.
- Reuse existing module if already present; update incrementally instead of rewriting unrelated content.

### 2) Scaffold module directories

Create missing directories only:

```text
{project-name}/{project-name}-{domain}/src/main/java/{package-path}/{domain}/api
{project-name}/{project-name}-{domain}/src/main/java/{package-path}/{domain}/application
{project-name}/{project-name}-{domain}/src/main/java/{package-path}/{domain}/domain/model
{project-name}/{project-name}-{domain}/src/main/java/{package-path}/{domain}/domain/policy
{project-name}/{project-name}-{domain}/src/main/java/{package-path}/{domain}/domain/errors
{project-name}/{project-name}-{domain}/src/main/resources/{domain}
```

### 3) Create or align module `pom.xml`

File:

`{project-name}/{project-name}-{domain}/pom.xml`

Rules:

- Parent points to `{project-name}`.
- `artifactId` is `{project-name}-{domain}`.
- Keep existing dependencies; add missing required ones once:

```xml
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

`{project-name}/pom.xml`

Update idempotently:

1. Add `<module>{project-name}-{domain}</module>` under `<modules>` (once).
2. Add managed dependency under `<dependencyManagement><dependencies>`:

```xml
<dependency>
    <groupId>{package-name}</groupId>
    <artifactId>{project-name}-{domain}</artifactId>
    <version>${project.version}</version>
</dependency>
```

### 5) Wire bootstrap profiles

File:

`{project-name}/{project-name}-bootstrap/pom.xml`

Update idempotently:

- Ensure profile `{domain}` exists with:
  - `<deployment-profile>{domain}</deployment-profile>`
  - dependency on `{project-name}-{domain}`
- Ensure profile `full` includes dependency on `{project-name}-{domain}`.
- Do not duplicate dependencies if already present.

### 6) Generate layer code with sibling skills

Execute in this order and pass the same normalized variables:

1. `$siyukio-domain-creator`
2. `$siyukio-application-creator`
3. `$siyukio-api-creator`

Minimum expected outputs:

- Domain: `{Entity}.java` (+ optional `{Entity}Policy.java`, `{Entity}Errors.java`)
- Application: `{Context}Service.java`
- API: `{Context}Controller.java`, `{Context}Paths.java`, `{Context}Request.java`, `{Context}Response.java`

### 7) Consistency checks

- Package paths follow `{package-name}.{domain}.*`.
- API depends on Application; Application depends on Domain.
- No raw endpoint strings in controller methods (use `{Context}Paths`).
- New profile/module registration is additive and non-destructive.

## Verification

From repository root:

```bash
cd {project-name}
./mvnw -pl {project-name}-{domain},{project-name}-bootstrap -DskipTests compile
```

Optional (when tests exist):

```bash
cd {project-name}
./mvnw -pl {project-name}-{domain} test
```

Before finishing, confirm:

- Module compiles.
- Parent `modules` and `dependencyManagement` include the new module.
- Bootstrap `{domain}` and `full` profiles both reference the module.
- Layer files exist in expected directories.

## Related skills

- `$siyukio-domain-creator`
- `$siyukio-application-creator`
- `$siyukio-api-creator`
- `$siyukio-create-unit-test` (after module generation, when test scaffolding is required)
