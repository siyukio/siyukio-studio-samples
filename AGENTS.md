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

### Console

```yaml
project-name: siyukio-studio-console
app-name: Siyukio Studio
watermark: Siyukio Studio
#favicon: assets/favicon.ico
#logo: assets/logo.svg
#logo-full: assets/logo-full.svg
```

### Local Environment Configuration

**Required Environment Variables for Local Test:**

| Variable                   | Purpose           | Test Value                                   |
|----------------------------|-------------------|----------------------------------------------|
| SIYUKIO_DB_MASTER_KEY      | Database encrypt  | 06CVrBQL+6VZzbXYhxfXYIm40I/cS4Ern2DW7beR5JU= |
| SIYUKIO_DB_MASTER_URL      | Database url      | jdbc:postgresql://localhost:5432/root        |
| SIYUKIO_DB_MASTER_USERNAME | Database username | root                                         |
| SIYUKIO_DB_MASTER_PASSWORD | Database password | FYm7JqaEcptxUTgy                             |

## Language Policy

- All generated code, comments must be in English only.

## Commit Convention

Format: `<type>(<scope>): <description>`

### Types

`feat`, `fix`, `refactor`, `docs`, `style`, `test`, `chore`, `ci`, `build`, `perf`, `revert`

### Scopes

`server`, `console`, `web`, `desktop`

# Esexecution Workflow

## PR Target Policy

- Default PR base branch is `test/{project-version}`.
- PRs targeting `main` are prohibited unless explicitly approved by the user.

### Standard Workflow

1. **Ensure test base branch**: Check whether `test/{project-version}` exists. If not, create it from `main` and push it.
2. **Create working branch**: Create a feature branch from `test/{project-version}` using a valid prefix such as `feat/`, `fix/`, or `refactor/`.
3. **Implement**: Apply code changes under the relevant sub-project rules (Server only at this stage).
4. **Verify**: Run the required verification gates for the sub-project before committing.
5. **Commit**: Create a commit using the required format `<type>(<scope>): <intent>`, with Lore protocol trailers in the commit body.
6. **Push and open PR**: Push the branch and create a PR targeting `test/{project-version}`.
7. **Local cleanup**: Switch back to `test/{project-version}` and delete the submitted local feature branch.

### Failure Handling

- If verification fails, fix issues and repeat from Step 3.
- If branch conflicts occur, rebase or merge from `test/{project-version}` and rerun verification.
- Do not skip verification to accelerate PR submission.

