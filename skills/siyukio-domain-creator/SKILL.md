---
name: siyukio-domain-creator
description: "Create or modify a complete Siyukio server domain feature through a fixed workflow that orchestrates module, model, API, application, and API test updates. Use when implementing a new domain context or extending an existing domain end-to-end in project."
---

# siyukio-domain-creator

Create or update one Siyukio server domain with a fixed, full-stack domain workflow.

## Scope

Use this skill for server domain work only.

Do not use this skill for web, desktop, or console projects.

## Required inputs

- `{project-name}`: server Maven aggregator artifact (example: `siyukio-studio-server`).
- `{package-name}`: Java base package (example: `io.github.siyukio.samples`).
- `{package-path}`: slash format of package (example: `io/github/siyukio/samples`).
- `{domain}`: domain module suffix in kebab-case (example: `user-management`).
- `{Context}`: domain context in PascalCase (example: `User`).
- `{Entity}`: primary entity in PascalCase (example: `User`).
- Required change intent for each layer:
  - module structure and registration changes
  - model entity/errors/policy changes
  - API endpoint and DTO changes
  - application service orchestration changes
  - API test scenarios to create/update

## Preconditions

- Work inside `{project-name}` only.
- Target branch and task flow must follow project `AGENTS.md`.
- Keep generated code and comments in English.

## Fixed workflow order

Always execute the following steps in order. Reuse normalized inputs across all steps.

### 1) Create or update domain module

Invoke:

`$siyukio-module-creator`

Responsibilities:

- Create/update `{project-name}-{domain}` module.
- Keep parent/aggregate and bootstrap profile wiring consistent.
- Prepare API/Application/Model layer locations.

### 2) Create or update domain model

Invoke:

`$siyukio-model-creator`

Responsibilities:

- Create/update entity, errors, and policy artifacts.
- Apply table fields, indexes, constraints, and domain invariants.

### 3) Create or update domain controller (API layer)

Invoke:

`$siyukio-api-creator`

Responsibilities:

- Create/update controller, paths, and request/response DTOs.
- Keep endpoint contracts explicit for the target context.

### 4) Create or update domain application service

Invoke:

`$siyukio-application-creator`

Responsibilities:

- Create/update `{Context}Service` orchestration logic.
- Align use-case logic with model policy and API contracts.

### 5) Create or update domain API tests

Invoke:

`$siyukio-unit-test-creator`

Responsibilities:

- Create/update API test cases for the domain context.
- Prioritize `api/{Context}ControllerTest.java` coverage, then add service/integration tests only when required.

## Orchestration rules

- Keep one shared variable set across all five steps (`{domain}`, `{Context}`, `{Entity}`, and package coordinates).
- Apply additive updates; avoid rewriting unrelated files.
- Preserve existing naming and test style unless user requests migration.
- If a target file already exists, update incrementally instead of recreating it.

## Verification

After all five steps complete, run:

```bash
cd {project-name}
./mvnw -pl {project-name}-{domain} -DskipTests compile
./mvnw test -pl {project-name}-{domain} -Dtest={Context}*Test
```

Confirm before finishing:

- Module registration is correct.
- Model/API/Application artifacts are aligned on field names and method contracts.
- API tests for the target context are present and passing.

## Related skills

- `$siyukio-module-creator`
- `$siyukio-model-creator`
- `$siyukio-api-creator`
- `$siyukio-application-creator`
- `$siyukio-unit-test-creator`
