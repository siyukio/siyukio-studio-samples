---
name: siyukio-console-page-update-drawer-creator
description: Create or update domain page update-drawer interactions for Siyukio console pages. Use when a page entry exists at `src/pages/{domain}/{context}/index.vue` and you need to create or modify `components/Update{Context}.vue` plus integrate it into the page to submit `update` API requests with edit-button interaction.
---

# Siyukio Console Page Update Drawer Creator

Create or update an `Update{Context}.vue` drawer component under a domain page and wire it into `{domain}/{context}/index.vue` for update-form interaction.

This skill is fully self-contained in this `SKILL.md` file. Do not rely on external scripts or extra reference files.

## Required input contract

Read these files first:

- Page entry: `{console-project-name}/src/pages/{domain}/{context}/index.vue`
- API file: `{console-project-name}/src/api/{domain}/{context}.ts`
- I18n file (required): `{console-project-name}/src/locales/lang/en_US/pages/{domain}/{context}.ts`
- I18n file (optional but recommended): `{console-project-name}/src/locales/lang/zh_CN/pages/{domain}/{context}.ts`

## Required output contract

Create or update this component file:

- `{console-project-name}/src/pages/{domain}/{context}/components/Update{Context}.vue`

Create or update this page file:

- `{console-project-name}/src/pages/{domain}/{context}/index.vue`

## Inputs to normalize first

- `{domain}` from page/API path
- `{context}` from path/file name in lower camel case (example: `variable`)
- `{Context}` as PascalCase (example: `Variable`)

From API file, discover:

- Update API function: `{context}.update`
- Get-detail API function: `{context}.get`
- Update request type: `{Context}UpdateRequest`
- Get response type: `{Context}GetResponse`

From i18n file, discover:

- Field labels: `pages.{context}.fields.*`
- Operation labels: `pages.{context}.operations.update`
- Message labels: `pages.{context}.messages.updateSuccess`

## Workflow

### 1) Create or update drawer component file

Create or update:

- `src/pages/{domain}/{context}/components/Update{Context}.vue`

Use this template structure:

```vue
<template>
  <form-drawer
    v-model:visible="drawerVisible"
    :title="t('pages.{context}.operations.update')"
    size="large"
    :confirm-text="t('pages.common.update')"
    :cancel-text="t('pages.common.cancel')"
    :form-data="formData"
    :form-rules="formRules"
    :label-width="120"
    @validate-success="handleValidateSuccess"
  >
    <t-row :gutter="[12, 20]">
      <t-col :span="12">
        <t-form-item :label="t('pages.{context}.fields.name')" name="name">
          <t-input v-model="formData.name" />
        </t-form-item>
      </t-col>
      <!-- More form fields, including readonly fields for createdAt/updatedAt -->
    </t-row>
  </form-drawer>
</template>

<script setup lang="ts">
import type { FormRule } from 'tdesign-vue-next';
import { MessagePlugin } from 'tdesign-vue-next';
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
  (e: 'success', data: any): void;
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

const formRules: Record<string, FormRule[]> = {
  name: [
    {
      required: true,
      message: '',
      type: 'error',
    },
  ],
};

const handleValidateSuccess = async (params: any) => {
  const { resolve, reject } = params;
  try {
    const res = await {context}.update(formData.value);
    MessagePlugin.success(t('pages.{context}.messages.updateSuccess'));
    drawerVisible.value = false;
    emit('success', res);
    resolve();
  } catch (error) {
    reject(error);
  }
};

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

### 2) Integrate or update drawer wiring in page `index.vue`

Update `src/pages/{domain}/{context}/index.vue` in two places.

Script section changes:

```vue
<script setup lang="ts">
// Import update drawer component
import Update{Context} from './components/Update{Context}.vue';

// Selected item ID for update/remove/detail
const selectId = ref('');

// Update drawer visible state
const updateDrawerVisible = ref(false);

// Edit trigger function
const handleClickEdit = (row: any) => {
  selectId.value = row.id;
  updateDrawerVisible.value = true;
};

// Update-success callback
const onUpdateSuccess = () => {
  fetchData();
};
</script>
```

Template section changes:

```vue
<template>
  <!-- Add drawer component under Drawer Components -->
  <update-{context} :id="selectId" :visible="updateDrawerVisible" @success="onUpdateSuccess" />

  <!-- Add edit button under Additional operations -->
  <t-link size="small" theme="primary" hover="color" @click="handleClickEdit(row)">
    {{ t('pages.common.edit') }}
  </t-link>
</template>
```

## Integration rules

- Use `FormDrawer` component at `@/components/form-drawer/index.vue`.
- Keep component name deterministic: `Update{Context}`.
- Use `v-model:visible` and emit `update:visible` for open/close state sync.
- Keep `id` in props and pass it to detail loading (`{context}.get`) before editing.
- Call `{context}.update(...)` in `handleValidateSuccess`.
- Emit `success` after successful update so list page can refresh.
- Keep generated code and comments in English only.

## Validation steps

From `{console-project-name}` run:

```bash
pnpm run build:type
pnpm run lint -- src/pages/{domain}/{context}/index.vue src/pages/{domain}/{context}/components/Update{Context}.vue
```

If lint script does not accept glob/path arguments, run:

```bash
pnpm run lint
```

Manual checks:

- Drawer opens/closes from edit action correctly.
- Drawer loads detail data by selected `id`.
- Form validation blocks invalid submit.
- Successful submit calls `update` API and refreshes list page.
- `pages.{context}.operations.update` and `pages.{context}.messages.updateSuccess` keys exist.

## Completion report format

When finishing, report:

- API + i18n inputs used
- Created/updated component file path
- Created/updated page file path
- Validation commands and outcomes
- Any unresolved i18n or field-mapping ambiguity
