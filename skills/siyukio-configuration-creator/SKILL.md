---
name: siyukio-configuration-creator
description: Create or update repository and subproject configuration files by reading project metadata from AGENTS.md, then syncing console package/.env values and server parent/module pom.xml values. Use when AGENTS.md project name, version, or environment parameters changed and config files must be aligned.
---

# Siyukio Configuration Creator

## Goal

Synchronize configuration values in current repository from `AGENTS.md`.

## Inputs

- Repository root with `AGENTS.md`.
- Console project directory from `console-project-name`.
- Server project directory from `server-project-name`.

## Required extracted values

From `AGENTS.md`, extract:

- `{project-name}`: project display name from first level-1 heading (example: `Siyukio Studio`).
- `{project-version}`: value in Global YAML block.
- `{console-project-name}`: value in Console YAML block.
- `{server-project-name}`: value in Server YAML block.
- Required environment variables from **Local Environment Configuration** table:
  - `SIYUKIO_DB_MASTER_KEY`
  - `SIYUKIO_DB_MASTER_URL`
  - `SIYUKIO_DB_MASTER_USERNAME`
  - `SIYUKIO_DB_MASTER_PASSWORD`

Stop with a clear error if any required value is missing.

## Workflow

1. Read and parse `AGENTS.md`.
   - Resolve repository root first.
   - Parse the metadata values listed in **Required extracted values**.
   - Keep extracted environment variables in memory for reporting and downstream config generation.

2. Update console project configuration.
   - Edit `./{console-project-name}/package.json`:
     - Set `name = {console-project-name}`.
     - Set `version = {project-version}`.
   - Edit every `./{console-project-name}/.env.{suffix}` file:
     - Derive `{suffix}` from filename segment after `.env.`.
     - Build `{Suffix}` by converting `{suffix}` to title form (`development` -> `Development`, `mock` -> `Mock`, `site` -> `Site`, `test` -> `Test`).
     - Set both keys to exactly:
       - `VITE_APP_NAME={project-name} {Suffix}`
       - `VITE_WATERMARK={project-name} {Suffix}`
     - Replace existing keys when present; append keys when missing.

3. Update server project configuration.
   - Edit `./{server-project-name}/pom.xml`:
     - Set root `<artifactId>` to `{server-project-name}`.
     - Set root `<version>` to `{project-version}-SNAPSHOT`.
   - Edit each module POM `./{server-project-name}/{module-name}/pom.xml`:
     - Set `<parent><artifactId>` to `{server-project-name}`.
     - Set `<parent><version>` to `{project-version}-SNAPSHOT`.
   - Do not rewrite unrelated XML sections.

## Verification

Run checks before claiming completion:

1. Confirm extracted values from `AGENTS.md` are non-empty.
2. Confirm `./{console-project-name}/package.json` has expected `name` and `version`.
3. Confirm every `./{console-project-name}/.env.{suffix}` has expected:
   - `VITE_APP_NAME={project-name} {Suffix}`
   - `VITE_WATERMARK={project-name} {Suffix}`
4. Confirm `./{server-project-name}/pom.xml` root fields:
   - `<artifactId>{server-project-name}</artifactId>`
   - `<version>{project-version}-SNAPSHOT</version>`
5. Confirm all module parent fields:
   - `<parent><artifactId>{server-project-name}</artifactId>`
   - `<parent><version>{project-version}-SNAPSHOT</version>`

## Edit policy

- Keep changes minimal and reversible.
- Preserve existing formatting style when possible.
- Keep all added text in English.
