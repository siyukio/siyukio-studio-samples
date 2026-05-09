---
name: siyukio-console-page-detail-drawer-creator
description: Create or update domain page detail-drawer interactions for Siyukio console pages. Use when a page entry exists at `src/pages/{domain}/{context}/index.vue` and you need to create or modify `components/Detail{Context}.vue` plus integrate it into the page for read-only detail view interaction. Only use this skill when update API is unavailable; if update API exists, use `siyukio-console-page-update-drawer-creator` first.
---

# Siyukio Console Page Detail Drawer Creator

Create or update a `Detail{Context}.vue` drawer component under a domain page and wire it into `{domain}/{context}/index.vue` for read-only detail interaction.

This skill is fully self-contained in this `SKILL.md` file. Do not rely on external scripts or extra reference files.

## Required input contract

Read these files first:

- Page entry: `{console-project-name}/src/pages/{domain}/{context}/index.vue`
- API file: `{console-project-name}/src/api/{domain}/{context}.ts`
- I18n file (required): `{console-project-name}/src/locales/lang/en_US/pages/{domain}/{context}.ts`
- I18n file (optional but recommended): `{console-project-name}/src/locales/lang/zh_CN/pages/{domain}/{context}.ts`

## Precheck and routing rule

Before generating detail drawer code, inspect API exports in `src/api/{domain}/{context}.ts`.

- If `{context}.update` exists, stop this skill and use `$siyukio-console-page-update-drawer-creator`.
- If update API is unavailable and `{context}.get` exists, continue with this detail drawer workflow.

## Required output contract

Create or update this component file:

- `{console-project-name}/src/pages/{domain}/{context}/components/Detail{Context}.vue`

Create or update this page file:

- `{console-project-name}/src/pages/{domain}/{context}/index.vue`

## Inputs to normalize first

- `{domain}` from page/API path
- `{context}` from path/file name in lower camel case (example: `variable`)
- `{Context}` as PascalCase (example: `Variable`)

From API file, discover:

- Get-detail API function: `{context}.get`
- Get response type: `{Context}GetResponse`

From i18n file, discover:

- Field labels: `pages.{context}.fields.*`
- Operation labels: `pages.{context}.operations.get`

## Workflow

### 1) Create or update detail drawer component file

Create or update:

- `src/pages/{domain}/{context}/components/Detail{Context}.vue`

Use this template structure:

```vue
<template>
  <form-drawer
    v-model:visible="drawerVisible"
    :title="t('pages.{context}.operations.get')"
    size="large"
    :form-data="formData"
    :label-width="120"
    :show-confirm-btn="false"
    :show-cancel-btn="false"
  >
    <t-row :gutter="[12, 20]">
      <t-col :span="12">
        <t-form-item :label="t('pages.{context}.fields.name')" name="name">
          <t-input v-model="formData.name" readonly />
        </t-form-item>
      </t-col>
      <!-- More form fields, including readonly for all fields -->
    </t-row>
  </form-drawer>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';

import * as {context} from '@/api/{domain}/{context}';
import FormDrawer from '@/components/form-drawer/index.vue';
import { t } from '@/locales';

interface Props {
  visible: boolean;
  id: string;
}

interface Emits {
  (e: 'update:visible', value: boolean): void;
}

const props = withDefaults(defineProps<Props>(), {});
const emit = defineEmits<Emits>();

const drawerVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value),
});

const formData = ref<any>({
  id: props.id,
  name: '',
  // Default field values
});

const fetchData = async () => {
  try {
    const res = await {context}.get({ id: props.id });
    formData.value = res;
  } catch (e) {
    console.error(e);
  }
};

watch(drawerVisible, (newValue) => {
  if (newValue) {
    fetchData();
  }
});
</script>
```

### 2) Integrate or update detail drawer wiring in page `index.vue`

Update `src/pages/{domain}/{context}/index.vue` in two places.

Script section changes:

```vue
<script setup lang="ts">
// Import detail drawer component
import Detail{Context} from './components/Detail{Context}.vue';

// Selected item ID for update/remove/detail
const selectId = ref('');

// Detail drawer visible state
const detailDrawerVisible = ref(false);

// Detail trigger function
const handleClickDetail = (row: any) => {
  selectId.value = row.id;
  detailDrawerVisible.value = true;
};
</script>
```

Template section changes:

```vue
<template>
  <!-- Add drawer component under Drawer Components -->
  <detail-{context} :id="selectId" :visible="detailDrawerVisible" />

  <!-- Add detail button under Additional operations -->
  <t-link
    size="small"
    theme="primary"
    hover="color"
    @click="handleClickDetail(row)"
  >
    {{ t("pages.common.detail") }}
  </t-link>
</template>
```

## Integration rules

- Use `FormDrawer` component at `@/components/form-drawer/index.vue`.
- Keep component name deterministic: `Detail{Context}`.
- Use `v-model:visible` and emit `update:visible` for open/close state sync.
- Keep `id` in props and pass it to detail loading (`{context}.get`) when opening drawer.
- Keep all displayed fields in read-only mode in detail drawer.
- Keep generated code and comments in English only.

## Validation steps

From `{console-project-name}` run:

```bash
pnpm run build:type
pnpm run lint -- src/pages/{domain}/{context}/index.vue src/pages/{domain}/{context}/components/Detail{Context}.vue
```

If lint script does not accept glob/path arguments, run:

```bash
pnpm run lint
```

Manual checks:

- Drawer opens/closes from detail action correctly.
- Drawer loads detail data by selected `id`.
- Drawer contains no editable fields.
- `pages.{context}.operations.get` key exists.

## Completion report format

When finishing, report:

- API + i18n inputs used
- Whether update API exists and the routing decision
- Created/updated component file path
- Created/updated page file path
- Validation commands and outcomes
- Any unresolved i18n or field-mapping ambiguity
