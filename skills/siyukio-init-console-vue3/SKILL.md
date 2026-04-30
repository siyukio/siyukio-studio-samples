---
name: siyukio-init-console-vue3
description: Initialize or sync a Siyukio Vue3 admin/console project from siyukio-tdesign-vue-next-starter using git subtree, then apply project-name/project-version from AGENTS.md into package.json and .env files plus optional branding SVG replacements.
---

# Goal

Initialize or refresh a Siyukio Vue3 console project with repeatable metadata and branding updates.

# Inputs

- `target-dir` (derived): must equal Console `project-name` resolved from `AGENTS.md` (no default value and no manual fallback)
- `template-url` (optional): default `https://github.com/siyukio/siyukio-tdesign-vue-next-starter.git`
- `template-remote` (optional): default `siyukio_tdesign_vue_next_starter`
- `template-branch` (optional): default `main`
- Optional override values:
  - `app-name` for `VITE_APP_NAME`
  - `watermark` for `VITE_WATERMARK`

# Rules

- Run from a git repository root or resolve the root with `git rev-parse --show-toplevel`.
- Require a clean working tree unless the user explicitly allows dirty execution.

# Workflow

1. Resolve repository and metadata.
   - Locate repo root and `AGENTS.md`.
   - Extract `project-version` from the Global YAML block.
   - Extract console `project-name` from the Console YAML block.
   - Set `target-dir = <console project-name>`.
   - Stop with a clear error if either value is missing.

2. Sync starter code with git subtree.
   - Ensure the template remote exists and points to the expected URL.
   - Detect whether `<target-dir>` already exists in `HEAD`:
     - Exists: run `git subtree pull --prefix=<target-dir> <template-remote> <template-branch> --squash`
     - Missing: run `git subtree add --prefix=<target-dir> <template-remote> <template-branch> --squash`

3. Update `<target-dir>/package.json`.
   - Set `name = <console project-name>`.
   - Set `version = <project-version>`.

4. Update every `<target-dir>/.env*` file.
   - Set `VITE_APP_NAME` to `<app-name>` when provided; otherwise use `<console project-name>`.
   - Set `VITE_WATERMARK` to `<watermark>` when provided; otherwise use `"<console project-name> <project-version>"`.
   - Replace existing keys when present; append keys when missing.

5. Apply optional branding assets (only when source files exist next to `AGENTS.md`).
   - `favicon.svg` -> `<target-dir>/public/favicon.svg`
   - `logo.svg` -> `<target-dir>/src/assets/assets-t-logo.svg`
   - `logo-full.svg` -> `<target-dir>/src/assets/assets-logo-full.svg`

6. Verify results.
   - Confirm subtree action completed (`add` or `pull`).
   - Confirm `<target-dir>/package.json` has expected `name` and `version`.
   - Confirm all `.env*` files contain updated `VITE_APP_NAME` and `VITE_WATERMARK`.
   - Confirm optional SVG replacements when source files were provided.

# Suggested Command Pattern

Use these commands as a baseline and adapt to the actual task context:

```bash
git rev-parse --show-toplevel
git status --porcelain
git remote get-url siyukio_tdesign_vue_next_starter || git remote add siyukio_tdesign_vue_next_starter https://github.com/siyukio/siyukio-tdesign-vue-next-starter.git
git remote set-url siyukio_tdesign_vue_next_starter https://github.com/siyukio/siyukio-tdesign-vue-next-starter.git
git ls-tree -d --name-only HEAD -- "<target-dir>"
```

Then run either `git subtree add` or `git subtree pull`, followed by direct file edits and verification commands.
