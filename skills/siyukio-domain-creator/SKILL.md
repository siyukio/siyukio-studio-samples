---
name: siyukio-domain-creator
description: "Create or modify a complete Siyukio server domain feature through a fixed workflow that orchestrates module, model, API, application, and API test updates, with conditional console TypeScript API sync when admin APIs are involved. Use when implementing a new domain context or extending an existing domain end-to-end in project."
---

# siyukio-domain-creator

Create or update one Siyukio server domain with a fixed, full-stack domain workflow.

## Scope

Use this skill for server domain work as the primary lane.

If the task includes admin API changes, also sync the corresponding console TypeScript API module.

Do not use this skill for unrelated web, desktop, or pure console-only tasks.

## Required inputs

- `{server-project-name}`: server Maven aggregator artifact (example: `siyukio-studio-server`).
- `{console-project-name}`: console project root for API sync (example: `siyukio-studio-console`).
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

- Work inside `{server-project-name}` as the primary scope.
- Target branch and task flow must follow project `AGENTS.md`.
- Keep generated code and comments in English.

## Fixed workflow order

Always execute the following steps in order. Reuse normalized inputs across all steps.

### 1) Create or update domain module

Invoke:

`$siyukio-module-creator`

Responsibilities:

- Create/update `{server-project-name}-{domain}` module.
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

`$siyukio-role-based-api-creator`

Responsibilities:

- Create/update role-based controllers, paths, and role-specific DTOs.
- Keep endpoint contracts explicit for user, admin, and internal roles.

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

### 6) Verify server domain implementation before console sync

Run:

```bash
cd {server-project-name}
./mvnw -pl {server-project-name}-{domain} -DskipTests compile
./mvnw test -pl {server-project-name}-{domain} -Dtest={Context}*Test
```

Requirements:

- Complete this verification before syncing console API.
- If verification fails, continue fixing server domain code and tests until it passes.

### 7) Sync console API for admin endpoint changes (conditional, final step)

Invoke:

`$siyukio-console-api-creator`

Run this step only when either condition is true:

- Current task explicitly includes admin API work.
- This workflow modifies any admin controller/path/DTO contract.

Responsibilities:

- Sync corresponding `{Context}Api.ts` in `{console-project-name}/src/api/`.
- Keep generated TypeScript request/response interfaces aligned with verified admin endpoint contracts.

## Orchestration rules

- Keep one shared variable set across all seven steps (`{domain}`, `{Context}`, `{Entity}`, and package coordinates).
- Apply additive updates; avoid rewriting unrelated files.
- Preserve existing naming and test style unless user requests migration.
- If a target file already exists, update incrementally instead of recreating it.

## Verification

After step 6 server verification passes and step 7 (if required) completes, confirm:

- Module registration is correct.
- Model/API/Application artifacts are aligned on field names and method contracts.
- Server compile and `{Context}`-related tests passed before console API sync.
- If admin API changed, corresponding console `{Context}Api.ts` is synced.
- API tests for the target context are present and passing.

## Related skills

- `$siyukio-module-creator`
- `$siyukio-model-creator`
- `$siyukio-role-based-api-creator`
- `$siyukio-console-api-creator`
- `$siyukio-application-creator`
- `$siyukio-unit-test-creator`
