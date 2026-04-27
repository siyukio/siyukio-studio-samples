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
project-version: 1.0.0
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

## Available Skills

### Server

| Skill                            | Purpose                                                                                         |
|----------------------------------|-------------------------------------------------------------------------------------------------|
| `$siyukio-init-springboot`       | Initialize Spring Boot project structure                                                        |
| `$siyukio-create-domain`         | Create domain models and policy logic                                                           |
| `$siyukio-create-api`            | Create domain API                                                                               |
| `$siyukio-create-application`    | Create application layer (accept API requests, call domain policy, operate model data)          |
| `$siyukio-create-acp`            | Initialize ACP server session handler                                                           |
| `$siyukio-create-domain-module`  | Create complete domain feature with module dependencies                                         |

### Web / Desktop / Console

(TBD)

## Task Execution Guidelines

**Restriction**: Currently only Server is under development. Web / Desktop / Console technical details are not yet finalized - reject any related tasks.

**PR Restriction**: All PRs must target `test/{project-version}` by default. PRs to `main` are not allowed unless explicitly approved.

### Development Tasks

Tasks that involve git commits (e.g., implementing features, fixing bugs, refactoring).

#### Development Task Workflow

1. **Check test branch** - If `test/{project-version}` does not exist, create it from `main` and push
2. **Create a feature branch** from `test/{project-version}` with appropriate prefix (e.g., `feat/`, `fix/`, `refactor/`)
2. **Implement changes** following the applicable sub-project skill
3. **Verify** using the sub-project's verification gates
4. **Commit** with Lore-compliant message format: `<type>(<scope>): <intent>`
5. **Push branch and create PR** to `test/{project-version}`
6. **Cleanup**: Switch back to `test/{project-version}` and delete the submitted local branch

#### Local Environment Configuration

**Required Environment Variables for Local Test:**

| Variable                   | Purpose           | Test Value                            |
|----------------------------|-------------------|---------------------------------------|
| SIYUKIO_DB_MASTER_URL      | Database url      | jdbc:postgresql://localhost:5432/root |
| SIYUKIO_DB_MASTER_USERNAME | Database username | root                                  |
| SIYUKIO_DB_MASTER_PASSWORD | Database password | FYm7JqaEcptxUTgy                      |

### Non-Development Tasks

Tasks that do not involve git commits (e.g., querying git status, reading/writing issues, reading/writing PRs, checking or creating development version branches `test/{project-version}`). No fixed workflow required - complete operations as needed.

If a PR is created for a feature branch, follow the same cleanup rule: switch back to `test/{project-version}` and delete the local feature branch after the PR is successfully submitted.

## Language Policy

- All generated code, comments must be in English only.

## Commit Convention

Format: `<type>(<scope>): <description>`

### Types

`feat`, `fix`, `refactor`, `docs`, `style`, `test`, `chore`, `ci`, `build`, `perf`, `revert`

### Scopes

`server`, `sample`, `common`, `config`, `dependency`

## Notes
