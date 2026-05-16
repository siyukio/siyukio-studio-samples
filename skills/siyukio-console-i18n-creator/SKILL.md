---
name: siyukio-console-i18n-creator
description: Create or update Siyukio console page i18n files from admin API controller, DTO, and paths definitions. Use when adding or changing Admin*Controller contracts and you need synchronized zh_CN and en_US locale files under src/locales/lang/*/pages/{domain}, including context file creation and pages index.ts registration.
---

# Siyukio Console I18n Creator

## Overview

Generate page-level i18n files for admin APIs in both `zh_CN` and `en_US`, then keep page index registration synchronized.
Always derive translation keys and labels from API contracts instead of ad hoc page text.

## Output contract

Write files into:

```text
{console-project-name}/src/locales/lang
├── zh_CN/pages/
│   ├── index.ts
│   ├── {domain}/
│   │   ├── {context}.ts
│   └── ...
└── en_US/pages/
    ├── index.ts
    ├── {domain}/
    │   ├── {context}.ts
    └── ...
```

Each language file uses this shape:

```typescript
export default {
  fields: {
    {field}: 'Field label',
  },
  enums: {
    {field}Enum: {
      true: 'True label',
      false: 'False label',
    },
  },
  queryFields: {
    {field}: 'Field label',
  },
  validations: {
    {validation}: 'Form field validation message',
  },
  operations: {
    {operation}: 'Operation label',
  },
  messages: {
    {operate}Success: 'Operate success message',
    {operate}Failure: 'Operate failure message',
  },
};
```

Register in `pages/index.ts` when `{context}.ts` is newly created:

```typescript
import {context} from './{domain}/{context}';

export default {
  ...,
  {context},
};
```

## Required inputs

Read from workspace `AGENTS.md` first:

- `server-project-name`
- `console-project-name`
- `package-path`

Then resolve per admin API context:

- Controller: `{server-project-name}/{server-project-name}-{domain}/src/main/java/.../api/Admin{Context}Controller.java`
- Paths: `{server-project-name}/{server-project-name}-{domain}/src/main/java/.../api/paths/Admin{Context}Paths.java`
- DTOs: `{server-project-name}/{server-project-name}-{domain}/src/main/java/.../api/dto/*.java`

## Context normalization

For each `Admin{Context}Controller`:

1. `rawContext` = class name without `Controller` suffix, for example `AdminVariable`.
2. `baseContext` = remove leading `Admin`, for example `Variable`.
3. `{context}` = lower camel of `baseContext`, for example `variable`.
4. `{domain}` = module suffix from `{server-project-name}-{domain}` in the controller path, for example `config`.
5. Target i18n files:
   - `.../zh_CN/pages/{domain}/{context}.ts`
   - `.../en_US/pages/{domain}/{context}.ts`

If a controller does not use the `Admin` prefix, still convert class name to lower camel and use it as `{context}`.

## Extraction rules

### Label brevity rule

Apply to all generated labels in `fields`, `queryFields`, and `operations`:

- Keep labels as short as possible while preserving meaning.
- Prefer a single core noun/verb phrase; avoid redundant qualifiers.
- Avoid repeating obvious context words (for example the resource name) when the page context already makes them clear.
- For `zh_CN`, labels should usually be no more than 6 Chinese characters.
- For `en_US`, labels should usually be a single word.
- If an `en_US` word would exceed 12 letters, consider a clear and common abbreviation.

### 1) fields

Collect field keys from request/response DTO records used by create/get/update/list style endpoints.

Label priority:

1. `@ApiParameter(description = "...")`
2. Humanized field name from camelCase/snake_case

Language generation:

- `en_US`: concise single-word label by default, for example `createdAtTs` -> `CreatedTS`
- `zh_CN`: concise natural Chinese (typically <= 6 characters), for example `createdAtTs` -> `创建时间`

### 2) enums

Generate enum blocks for:

- Boolean/Boolean wrapper fields
- Java enum typed fields when present

Boolean labels:

- If field name implies enable status (`enabled`, `active`, `disabled`): use semantic pair
  - en: `Enabled` / `Disabled`
  - zh: `启用` / `禁用`
- Otherwise:
  - en: `True` / `False`
  - zh: `是` / `否`

Enum key format: `{field}Enum`.

### 3) queryFields

Primary source:

- `*Filter` DTO used by list endpoints

Fallback source:

- Request DTO fields used in query-like endpoints (`list`, `search`, `page`, `get`)

Label mapping follows `fields` rules.

### 4) validations

Generate validation messages from required API fields:

- Source: `@ApiParameter(required = true)` in request DTOs
- Key format: `{field}Required`

Message format:

- en: `{Field Label} is required`
- zh: `请填写{字段名}`

### 5) operations

Derive from controller method names and/or path constants:

- `list`, `get`, `create`, `update`, `remove`, `delete`, `enable`, `disable`

Recommended labels:

- en:
  - `list`: `Query`
  - `get`: `View`
  - `create`: `Create`
  - `update`: `Update`
  - `remove`/`delete`: `Delete`
  - `enable`: `Enable`
  - `disable`: `Disable`
- zh:
  - `list`: `查询`
  - `get`: `查看`
  - `create`: `创建`
  - `update`: `更新`
  - `remove`/`delete`: `删除`
  - `enable`: `启用`
  - `disable`: `禁用`

### 6) messages

For each operation key `{op}`, generate:

- `{op}Success`
- `{op}Failure`

Message format:

- en: `{Operation Label} succeeded` / `{Operation Label} failed`
- zh: `{操作名}成功` / `{操作名}失败`

## Update workflow

1. Locate all `Admin*Controller.java` files in server modules.
2. Resolve each controller's paths class and endpoint methods.
3. Resolve DTO contracts and collect fields, optionality, and descriptions.
4. Create or update:
   - `zh_CN/pages/{domain}/{context}.ts`
   - `en_US/pages/{domain}/{context}.ts`
5. If `{domain}/{context}.ts` is newly created, register it into both language `pages/index.ts` files.
6. Keep existing unrelated entries untouched. Do not reorder unrelated exports unless needed for lint/format consistency.

## Quality gates

From `{console-project-name}` run:

```bash
pnpm run build:type
pnpm run lint -- src/locales/lang/**/*.ts
```

If lint script does not accept the glob argument, run:

```bash
pnpm run lint
```

Manual checks:

- Both `zh_CN` and `en_US` have the same key structure for each `{context}` file.
- `fields`, `enums`, `queryFields`, `validations`, `operations`, `messages` all exist.
- New `{context}` is registered in both language `pages/index.ts`.
- No duplicate imports or duplicate keys in index files.

## Completion report format

When finishing, report:

- Admin controllers processed
- Created/updated locale files
- Whether index registration was added
- Validation commands and outcomes
- Any unresolved translation ambiguity
