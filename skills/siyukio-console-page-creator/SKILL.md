---
name: siyukio-console-page-creator
description: Create or update Siyukio console domain context pages and related menu configuration from console API and i18n contracts. Use when an API module exists at `src/api/{domain}/{context}.ts` and page i18n exists at `src/locales/lang/en_US/pages/{domain}/{context}.ts`, and you need to create or modify `index.vue`, then create or update menu routes in `mock/menu.json` with icon names selected from `tdesign-icons-vue-next` `icons.d.ts`.
---

# Siyukio Console Page Creator

Create or update domain context pages from console API + i18n files, then create or update menu config for navigation.

This skill is fully self-contained in this `SKILL.md` file. Do not rely on external scripts or extra reference files.

## Required input contract

Read these files first:

- API source: `{console-project-name}/src/api/{domain}/{context}.ts`
- I18n source (required): `{console-project-name}/src/locales/lang/en_US/pages/{domain}/{context}.ts`
- I18n source (optional but recommended): `{console-project-name}/src/locales/lang/zh_CN/pages/{domain}/{context}.ts`

## Required output contract

Create or update these files:

- `{console-project-name}/src/pages/{domain}/{context}/index.vue`

Create or update menu config:

- Update `{console-project-name}/mock/menu.json`.

## Inputs to normalize first

Read and normalize from workspace `AGENTS.md`:

- `{console-project-name}`

Then normalize from path + file names:

- `{domain}` from `src/api/{domain}/{context}.ts`
- `{context}` from file name, for example `variable`
- `{Context}` = PascalCase of `{context}`, for example `Variable`

API naming expectations from `src/api/{domain}/{context}.ts`:

- API object: `{Context}Api`
- Query/list function: `list`
- Create function: `create` (optional)
- Update function: `update` (optional)
- Remove/delete function: `remove` or `delete` (optional)

Type naming expectations:

- `{Context}Filter`
- `{Context}ListResponse`
- `{Context}CreateRequest` / `{Context}CreateResponse`
- `{Context}UpdateRequest` / `{Context}UpdateResponse`

## Workflow

### 1) Create or update base `index.vue` from list/query API + i18n

Use skill `$siyukio-console-page-index-creator` to create or update `src/pages/{domain}/{context}/index.vue` from list/query API and i18n contracts.

Do not describe or implement base `index.vue` details inside this skill.

### 2) Add create interaction when create/add API exists

If API includes create/add capability (`create` or `add`), use skill `$siyukio-console-page-create-drawer-creator` to generate create-data interaction.

### 3) Add update-or-detail interaction by API capability

Check whether update API exists in `src/api/{domain}/{context}.ts`.

- If update API exists (`update`), use skill `$siyukio-console-page-update-drawer-creator` to implement update-data interaction.
- If update API does not exist, use skill `$siyukio-console-page-detail-drawer-creator` to implement detail-view interaction.

### 4) Register menu

Update menu list in `{console-project-name}/mock/menu.json` under key `list`.

Menu route shape:

```typescript
{
  path: '/{domain}',
  name: '{Domain}',
  component: 'LAYOUT',
  redirect: '/{domain}/{context}',
  meta: {
    title: {
      zh_CN: '{Context}',
      en_US: '{Context}',
    },
    icon: 'view-list',
  },
  children: [
    {
      path: '{context}',
      name: '{Context}List',
      component: '/{domain}/{context}/index',
      meta: {
        title: {
          zh_CN: '{Context}',
          en_US: '{Context}',
        },
      },
    },
  ],
}
```

### 5) Pick icon from `tdesign-icons-vue-next`

Select `meta.icon` from icon entries declared in:

- `{console-project-name}/node_modules/**/tdesign-icons-vue-next/**/icons.d.ts`

Selection workflow:

1. Search `icons.d.ts` for semantic matches to `{domain}` and `{context}`.
2. Pick a stable generic icon when domain-specific icon is unclear.
3. Use the kebab-case icon string from the component path, not the exported TypeScript symbol name.

Mapping example:

- `export { default as ViewListIcon } from './components/view-list';` -> `meta.icon = 'view-list'`
- `export { default as SettingIcon } from './components/setting';` -> `meta.icon = 'setting'`

## Generation constraints

- Keep generated code and comments in English only.
- Do not add new dependencies.
- Do not invent API methods not present in `src/api/{domain}/{context}.ts`.
- Do not invent i18n keys outside defined page i18n sections.
- Keep page names and route names stable and deterministic when creating or modifying.
- Delegate base page generation to `$siyukio-console-page-index-creator`.
- Do not implement create/add drawer details directly in this skill; route that work to `$siyukio-console-page-create-drawer-creator` when applicable.
- Route update interaction to `$siyukio-console-page-update-drawer-creator` when `update` API exists.
- Route detail interaction to `$siyukio-console-page-detail-drawer-creator` when `update` API is missing.

## Validation steps

From `{console-project-name}` run:

```bash
pnpm run build:type
pnpm run lint -- src/pages/{domain}/{context}/**/*.vue src/pages/{domain}/{context}/**/*.ts
```

If lint script does not accept the glob argument, run:

```bash
pnpm run lint
```

Manual checks:

- Page imports API from `@/api/{domain}/{context}`.
- All `t('pages.{context}.*')` keys exist in both `en_US` and `zh_CN` page files.
- List page calls `list` and refreshes table correctly.
- `mock/menu.json` is valid JSON and contains the new route under `list`.
- Menu route points to `/{domain}/{context}` and component `/{domain}/{context}/index`.
- `meta.icon` value exists in `icons.d.ts` component names.

## Completion report format

When finishing, report:

- API + i18n inputs used
- Created/updated page files
- Created/updated menu config files
- Validation commands and outcomes
- Any unresolved label or menu-title ambiguity
