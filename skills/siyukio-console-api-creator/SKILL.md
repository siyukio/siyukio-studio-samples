---
name: siyukio-console-api-creator
description: Generate or update console TypeScript API modules per admin controller. Use when converting server `Admin{Context}Controller` + `Admin{Context}Paths` and related DTO contracts into `{console-project-name}/src/api/{domain}/{context}.ts`, resolving path constants and generating `postRequestWithAuth` functions plus request/response interfaces, with `Admin` treated as a fixed prefix and excluded from generated `{Context}` names.
---

# siyukio-console-api-creator

Create console API files from server controller contracts, one controller to one TypeScript API module.

This skill is fully self-contained in this `SKILL.md` file. Do not rely on external scripts or extra reference files.

## Required output contract

For each admin controller:

- Source: `{server-project-name}/{server-project-name}-{domain}/src/main/java/{package-path}/{domain}/api/Admin{Context}Controller.java`
- Target: `{console-project-name}/src/api/{domain}/{context}.ts`

Generate TypeScript code in this shape:

```typescript
import { postRequestWithAuth } from '@/utils/acp';
import type { PageRequest, PageResponse } from '@/api/model/commonModel';

export const {Context}Api = {
  Create: '/path/create',
  Update: '/path/update',
};

export interface {Context}CreateRequest {
  category: string;
  name: string;
  description?: string;
}

export interface {Context}CreateResponse {
  id: string;
}

export const create = (request: {Context}CreateRequest) => {
  return postRequestWithAuth<{Context}CreateResponse>({Context}Api.Create, request);
};
```

Keep generated code and comments in English only.

## Inputs to normalize first

Read and normalize from the workspace `AGENTS.md`:

- `{server-project-name}` (example: `siyukio-studio-server`)
- `{console-project-name}` (example: `siyukio-studio-console`)
- `{package-path}` and `{package-name}`

Then normalize per controller:

- `rawContext` = controller class name without `Controller` suffix (for example `AdminVariable`)
- `{Context}` = remove fixed `Admin` prefix from `rawContext` (for example `Variable`)
- `{context}` = lower camel variant of `{Context}` (for example `variable`)
- Domain module = `{server-project-name}-{domain}` from controller file path

`Admin` is a fixed prefix and is not part of `{Context}`.

## Discovery workflow

### 1) Locate controller files

Find all domain controllers:

```bash
find {server-project-name} -type f -path '*/src/main/java/*/api/Admin*Controller.java'
```

For each controller, read:

- Controller source (`api/Admin{Context}Controller.java`)
- Path constants source (`api/paths/Admin{Context}Paths.java`), resolved from controller imports
- DTO records under `api/dto/*.java` that are used by controller method request/response types

### 2) Extract endpoint contract from controller

For each method with `@ApiMapping(path = ... )`, extract:

- Path constant reference (for example `AdminVariablePaths.CREATE`)
- Method name (for example `create`)
- Request type from method parameter
- Response type from method return

Preserve method declaration order as the output function order.

### 3) Resolve concrete path strings

From `Admin{Context}Paths.java`, resolve path constant values.

Example:

- `AdminVariablePaths.CREATE` -> `"/admin/variable/createVariable"`

Use resolved strings in `export const {Context}Api`.
Example: `AdminVariableController` -> `VariableApi` and output file `variable.ts`.

### 4) Resolve DTO shapes

For each Java record used by endpoints:

- Convert each record field into TypeScript interface field
- Keep field order identical to Java record order
- Use optional `?` when field is explicitly optional

Optional rule:

- If `@ApiParameter(required = false)` exists on a field, mark it optional (`field?: type`)
- Otherwise keep it required (`field: type`)

If annotation is absent, keep required by default.

### 5) Handle pagination contracts

Detect paging usage from controller signatures:

- Request type `PageRequest<T>` requires `import type { PageRequest, PageResponse } from '@/api/model/commonModel';`
- Response type `PageResponse<T>` requires the same import

If neither appears in any endpoint within the controller, do not import page models.

### 6) Generate `{context}.ts`

Write one file per controller with these sections in order:

1. Imports
2. `export const {Context}Api`
3. Generated request/response/filter interfaces
4. Generated endpoint functions

Function rule:

- Request function name = controller method name (for example `create`, `list`, `get`, `update`, `remove`)
- Use `postRequestWithAuth` for all generated functions
- For non-void response:

```typescript
export const create = (request: {Context}CreateRequest) => {
  return postRequestWithAuth<{Context}CreateResponse>({Context}Api.Create, request);
};
```

- For `void` response:

```typescript
export const remove = (request: {Context}RemoveRequest) => {
  return postRequestWithAuth<void>({Context}Api.Remove, request);
};
```

## Naming and mapping rules

### API object key mapping

Convert path constant names to PascalCase keys:

- `CREATE` -> `Create`
- `GET` -> `Get`
- `LIST` -> `List`
- `UPDATE` -> `Update`
- `REMOVE` -> `Remove`
- `DELETE` -> `Delete`

### Type Mapping

| Java Type                   | TypeScript Type |
| --------------------------- | --------------- |
| String                      | string          |
| int / Integer / long / Long | number          |
| boolean / Boolean           | boolean         |
| List<T>                     | T[]             |
| LocalDateTime               | string          |

Additional practical mappings for Siyukio APIs:

- `double / Double / float / Float / BigDecimal` -> `number`
- `PageRequest<T>` -> `PageRequest<T>`
- `PageResponse<T>` -> `PageResponse<T>`
- `void` -> `void`

### Generic and nested type mapping

Apply recursively:

- `List<List<String>>` -> `string[][]`
- `PageResponse<List<Item>>` -> `PageResponse<Item[]>`

### DTO to interface name mapping

Use TypeScript interface names without the fixed `Admin` prefix.
If Java DTO name starts with `Admin`, remove that prefix for generated TypeScript names.

Examples:

- `AdminVariableCreateRequest` -> `export interface VariableCreateRequest`
- `AdminVariableListResponse` -> `export interface VariableListResponse`

## Generation constraints

- Do not create any runtime dependency beyond existing console utilities.
- Do not import backend Java concepts into frontend runtime.
- Do not invent endpoints not present in controller + paths constants.
- Keep each generated file focused on one controller.
- Keep formatting valid for TypeScript and existing ESLint setup.

## Validation steps

After generation, run from `{console-project-name}`:

```bash
pnpm run build:type
pnpm run lint -- src/api/**/*.ts
```

If lint script does not accept glob argument in current setup, run:

```bash
pnpm run lint
```

Then verify manually:

- Every `@ApiMapping` endpoint in controller has a matching TS function.
- Every path constant referenced by controller is mapped in `{Context}Api` with resolved string path.
- Every request/response DTO used by controller exists as a TS interface.
- Optional field mapping from `required = false` is correct.
- Pagination signatures use `PageRequest` and `PageResponse` correctly.

## Completion report format

When finishing generation, report:

- Controllers processed
- Generated or updated API files
- Verification commands executed and result
- Any unresolved type or mapping ambiguity
