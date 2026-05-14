---
name: siyukio-console-page-index-creator
description: Create or update domain context entry page `index.vue` for Siyukio console contexts from list/query API and page i18n contracts. Use when API exists at `src/api/{domain}/{context}.ts` and i18n exists at `src/locales/lang/en_US/pages/{domain}/{context}.ts`, and you need to generate or modify `src/pages/{domain}/{context}/index.vue` with query form, table, list-fetch flow, and remove/delete interaction when remove APIs are available.
---

# Siyukio Console Page Index Creator

Create or update the entry page `src/pages/{domain}/{context}/index.vue` from API + i18n contracts.

This skill is fully self-contained in this `SKILL.md` file. Do not rely on external scripts or extra reference files.

## Required input contract

Read these files first:

- API source: `{console-project-name}/src/api/{domain}/{context}.ts`
- I18n source (required): `{console-project-name}/src/locales/lang/en_US/pages/{domain}/{context}.ts`
- I18n source (optional but recommended): `{console-project-name}/src/locales/lang/zh_CN/pages/{domain}/{context}.ts`

## Required output contract

Create or update this file:

- `{console-project-name}/src/pages/{domain}/{context}/index.vue`

This skill handles creation and modification of the base index page from list/query contracts.

- Do not register menu in this skill.
- Do not implement create/update/detail drawer components in this skill.

## Inputs to normalize first

Read and normalize from workspace `AGENTS.md`:

- `{console-project-name}`

Then normalize from path + file names:

- `{domain}` from `src/api/{domain}/{context}.ts`
- `{context}` from file name, for example `variable`
- `{Context}` = PascalCase of `{context}`, for example `Variable`

API naming expectations from `src/api/{domain}/{context}.ts`:

- Query/list function: `list`
- Optional operations for operation-column visibility: `create`, `update`, `remove` or `delete`, `get`

Type naming expectations:

- `{Context}Filter`
- `{Context}ListResponse`

## Workflow and code template structure

### Mandatory layout rules

For page sections containing multiple elements (query forms, action areas, grouped controls), prefer `<t-row>` + `<t-col>` layout.

- Always set row gutter as `:gutter="[12, 20]"`.
- Use column span by required columns per row:
  - 3 columns in one row: `:span="4"`
  - 2 columns in one row: `:span="6"`
  - 1 column in one row: `:span="12"`
- For `<t-col>`, use only `:span` to control occupied width.
- Unless explicitly required, do not add responsive size props (`xs`, `sm`, `md`, `lg`, `xl`) or screen-specific layout branches.
- Default query form layout is 3 columns per row unless user explicitly requests 2-column or 1-column layout.
- Because English labels are often longer, use `<t-form :label-width="120">` as the default label width.
- Unless explicitly required, do not change the default form label width from `120`.
- When using a 2-column layout, keep both columns visually balanced in overall height.
- For multi-line inputs such as `<t-textarea>`, set a maximum height to prevent long content from stretching one column and breaking the two-column balance.
- For `<t-textarea>`, keep `maxRows` no greater than `12`, and set `minRows` equal to `maxRows` to avoid layout shifts caused by content growth.
- Keep action buttons inside `<t-col>` so they follow the same grid layout rules.
- Action buttons can exist directly inside `<t-col>` and do not need to be wrapped by `<t-form-item>`.
- For action areas with multiple buttons, `<t-col class="operation-container">` is allowed and recommended.
- Button component selection:
  - If a button handler does not call API, use `<t-button>`.
  - If a button handler calls API, use `<loading-button>`.

### 1) Create or update base `index.vue` from list/query API + i18n

Read API + i18n contracts and generate or modify the base query/list page.

Required extraction:

- Query fields from `queryFields`
- Table display candidates from `fields`
- Enum labels from `enums`
- Operation labels from `operations`
- Success/failure messages from `messages`

Base generation rules:

- Build query form model from `{context}.queryFields`
- Build table columns from intersection of `fields` keys and list response fields
- Build operation column skeleton for optional actions
- Ensure list/query flow is wired to `{context}.list(...)`

Base template:

```vue
<template>
  <div class="common-container">
    <!-- Query Form -->
    <t-form
      :data="formData"
      :label-width="120"
      colon
      @reset="onReset"
      @submit="onSubmit"
    >
      <t-row :gutter="[12, 20]">
        <t-col :span="4">
          <!-- Query form item -->
          <t-form-item :label="t('pages.{context}.queryFields.name')">
            <t-input v-model="formData.name" clearable />
          </t-form-item>
        </t-col>
        <t-col :span="4">
          <!-- Query form item -->
          <t-form-item :label="t('pages.{context}.queryFields.enabled')">
            <t-select v-model="formData.enabled" clearable />
          </t-form-item>
        </t-col>
        <t-col :span="4" class="operation-container">
          <loading-button theme="primary" type="submit">
            {{ t("pages.common.query") }}
          </loading-button>
          <t-button type="reset" variant="base" theme="default">
            {{ t("pages.common.reset") }}
          </t-button>
          <!-- Other operations -->
        </t-col>
      </t-row>
    </t-form>

    <t-divider></t-divider>

    <!-- Data Table -->
    <t-table
      :data="data"
      :columns="COLUMNS"
      :row-key="rowKey"
      :pagination="pagination"
      hover
      size="small"
      bordered
    >
      <!-- Custom column templates (e.g., enums, long text) -->
      <template #customColumn="{ row }">
        <!-- Custom rendering logic -->
      </template>

      <!-- Operation Column -->
      <template #op="{ row }">
        <t-space direction="vertical">
          <!-- Additional operations (optional), for example remove -->
        </t-space>
      </template>
    </t-table>

    <!-- Drawer Components -->
  </div>
</template>

<script setup lang="ts">
import type { PrimaryTableCol } from 'tdesign-vue-next';
import { MessagePlugin } from 'tdesign-vue-next';
import { onActivated, ref } from 'vue';

import * as {context} from '@/api/{domain}/{context}';
import { t } from '@/locales';

defineOptions({
  name: '{Context}List',
});

const COLUMNS: PrimaryTableCol[] = [
  {
    title: t('pages.{context}.fields.id'),
    fixed: 'left',
    width: 100,
    ellipsis: true,
    align: 'left',
    colKey: 'id',
  },
  { title: t('pages.{context}.fields.name'), colKey: 'name', width: 120, ellipsis: true },
  {
    align: 'left',
    fixed: 'right',
    width: 60,
    colKey: 'op',
    title: t('pages.common.operation'),
  },
];

interface FormData {
  enabled?: boolean;
  name?: string;
}

const searchForm = {};
const rowKey = 'id';
const data = ref([]);
const pagination = ref({
  pageSize: 20,
  total: 100,
  current: 1,
  showJumper: true,
});

const formData = ref<FormData>({ ...searchForm });

const onReset = (val: unknown) => {
  console.log(val);
};

const onSubmit = () => {
  fetchData();
};

const fetchData = async () => {
  try {
    const res = await {context}.list({ filter: formData.value });
    data.value = res.items;
    pagination.value = {
      ...pagination.value,
      total: res.total,
    };
  } catch (e) {
    console.error(e);
    MessagePlugin.error(String(e));
  }
};

onActivated(() => {
  fetchData();
});
</script>
```

### 2) Add remove interaction when remove/delete API exists

Keep remove interaction rules and code next to this step.

API mapping rule:

- If API has `remove`, call `{context}.remove({ id: row.id })`.
- If API has `delete` but no `remove`, call `{context}.delete({ id: row.id })`.
- If neither exists, skip remove interaction.

Script extension:

```vue
<script setup lang="ts">
import { showRemoveConfirm } from "@/utils/dialog";

const handleClickRemove = (row: any) => {
  const removeApi = { context }.remove ?? { context }.delete;
  showRemoveConfirm(
    `[${row.name}]`,
    async () => {
      await removeApi({ id: row.id });
      MessagePlugin.success(t("pages.{context}.messages.removeSuccess"));
    },
    () => {
      fetchData();
    },
  );
};
</script>
```

Template extension (add under `<!-- Additional operations (optional) -->` in `#op` slot):

```vue
<t-link
  size="small"
  theme="primary"
  hover="color"
  @click="handleClickRemove(row)"
>
  {{ t('pages.common.remove') }}
</t-link>
```

## Generation constraints

- Keep generated code and comments in English only.
- Do not add new dependencies.
- Do not invent API methods not present in `src/api/{domain}/{context}.ts`.
- Do not invent i18n keys outside defined page i18n sections.
- Keep page names stable and deterministic (`{Context}List`).
- Keep scope to base index page only. Do not include menu updates or drawer implementations.
- When remove API exists, use `showRemoveConfirm` from `@/utils/dialog` and show `pages.{context}.messages.removeSuccess` after removal.
- For multi-element layout, use `<t-row :gutter="[12, 20]">` with `<t-col>` spans following the mandatory rules (3-col `4`, 2-col `6`, 1-col `12`).
- Use `<t-form :label-width="120">` as default to keep long English labels readable and aligned.
- Unless explicitly required, keep form label width fixed at `120`.
- Use only `:span` on `<t-col>` for width allocation; unless explicitly required, do not add responsive breakpoint props or screen-size-specific adaptations.
- In 2-column layouts, keep both columns balanced in height; for `<t-textarea>` and other multi-line fields, enforce a max height to avoid one-sided stretching.
- For `<t-textarea>`, enforce `maxRows <= 12` and `minRows === maxRows` to prevent content-driven height changes.
- Buttons may be placed directly inside `<t-col>` without `<t-form-item>` wrapping.
- For multiple action buttons, use `<t-col class="operation-container">` as the container.
- Use `<t-button>` for non-API handlers and `<loading-button>` for API-calling handlers.
- Do not add custom CSS in a `<style>` block inside `index.vue`.
- If custom CSS is truly necessary, explain the reason first and do not apply the style change until the user confirms.

## Validation steps

From `{console-project-name}` run:

```bash
pnpm run build:type
pnpm run lint -- src/pages/{domain}/{context}/index.vue
```

If lint script does not accept path arguments, run:

```bash
pnpm run lint
```

Manual checks:

- Page imports API from `@/api/{domain}/{context}`.
- All `t('pages.{context}.*')` keys exist in both `en_US` and `zh_CN` page files.
- List page calls `list` and refreshes table correctly.
- Query form fields match `queryFields` contract.
- If remove/delete API exists, remove action shows confirm dialog, calls API, and refreshes table after success.

## Completion report format

When finishing, report:

- API + i18n inputs used
- Created/updated page files
- Validation commands and outcomes
- Any unresolved field or label ambiguity
