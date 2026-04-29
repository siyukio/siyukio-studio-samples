<!-- This file is Project-Level AGENTS.md -->
<!-- This file SUPPLEMENTS the global ~/.codex/AGENTS.md, it does NOT replace it. -->
<!-- Managed manually - omx setup preserves this file when --force is not used. -->

# Siyukio Studio

## Project Overview

Siyukio Studio is a comprehensive project that includes web frontend, desktop application, admin console frontend,
and a server backend, demonstrating the usage of the Siyukio framework. It provides working examples to help developers
understand how to use various features of the framework.

## Project Structure

```
siyukio-studio-samples/
├── siyukio-studio-web/       # Web frontend (TBD)
├── siyukio-studio-desktop/   # Desktop application (TBD)
├── siyukio-studio-console/   # Admin console frontend (TBD)
└── siyukio-studio-server/    # Server project (Spring Boot)
```

## Context Parameters

### Global

```yaml
project-version: 1.0.1
```

### Server

```yaml
project-name: siyukio-studio-server
package-name: io.github.siyukio.samples
package-path: /io/github/siyukio/samples
java-version: 21
maven-version: 3.9
```

### Web / Desktop / Console

(TBD)

## Task Execution Guidelines

**Restriction**: Currently only Server is under development. Web / Desktop / Console technical details are not yet finalized - reject any related tasks.

**PR Restriction**: All PRs must target `test/{project-version}` by default. PRs to `main` are not allowed unless explicitly approved.

<Execution_Policy>

- All tasks must follow the workflow described in this document.
- When executing specific steps, prefer to use skills with `siyukio` prefix.

</Execution_Policy>

### Task Workflow

**NOTE**: You may resume from any step during execution, but you MUST complete the remaining workflow from that step onwards. For example, if the current task is only to submit a PR, you can start from step 4 and continue to step 7.

1. **Check test branch** - If `test/{project-version}` does not exist, create it from `main` and push
2. **Create a feature branch** from `test/{project-version}` with appropriate prefix (e.g., `feat/`, `fix/`, `refactor/`)
3. **Implement changes** following the applicable sub-project skill
4. **Verify** using the sub-project's verification gates
5. **Commit** with Lore-compliant message format: `<type>(<scope>): <intent>`
6. **Push branch and create PR** to `test/{project-version}`
7. **Cleanup**: Switch back to `test/{project-version}` and delete the submitted local branch

### Local Environment Configuration

**Required Environment Variables for Local Test:**

| Variable                   | Purpose           | Test Value                            |
|----------------------------|-------------------|---------------------------------------|
| SIYUKIO_DB_MASTER_URL      | Database url      | jdbc:postgresql://localhost:5432/root |
| SIYUKIO_DB_MASTER_USERNAME | Database username | root                                  |
| SIYUKIO_DB_MASTER_PASSWORD | Database password | FYm7JqaEcptxUTgy                      |

## Language Policy

- All generated code, comments must be in English only.

## Commit Convention

Format: `<type>(<scope>): <description>`

### Types

`feat`, `fix`, `refactor`, `docs`, `style`, `test`, `chore`, `ci`, `build`, `perf`, `revert`

### Scopes

`server`, `sample`, `common`, `config`, `dependency`

## Notes
