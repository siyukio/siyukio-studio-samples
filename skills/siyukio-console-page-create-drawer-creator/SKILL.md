---
name: siyukio-console-page-create-drawer-creator
description: Create or update domain page create-drawer interactions for Siyukio console pages. Use when a page entry exists at `src/pages/{domain}/{context}/index.vue` and you need to create or modify `components/Create{Context}.vue` plus integrate it into the page to submit `create` API requests.
---

# Siyukio Console Page Create Drawer Creator

Create or update a `Create{Context}.vue` drawer component under a domain page and wire it into `{domain}/{context}/index.vue` for create-form interaction.

This skill is fully self-contained in this `SKILL.md` file. Do not rely on external scripts or extra reference files.

## Required input contract

Read these files first:

- Page entry: `{console-project-name}/src/pages/{domain}/{context}/index.vue`
- API file: `{console-project-name}/src/api/{domain}/{context}.ts`
- I18n file (required): `{console-project-name}/src/locales/lang/en_US/pages/{domain}/{context}.ts`
- I18n file (optional but recommended): `{console-project-name}/src/locales/lang/zh_CN/pages/{domain}/{context}.ts`

## Required output contract

Create or update this component file:

- `{console-project-name}/src/pages/{domain}/{context}/components/Create{Context}.vue`

Create or update this page file:

- `{console-project-name}/src/pages/{domain}/{context}/index.vue`

## Inputs to normalize first

- `{domain}` from page/API path
- `{context}` from path/file name in lower camel case (example: `variable`)
- `{Context}` as PascalCase (example: `Variable`)

From API file, discover:

- Create API function: `{context}.create`
- Create request type: `{Context}CreateRequest`
- Create response type: `{Context}CreateResponse`

From i18n file, discover:

- Field labels: `pages.{context}.fields.*`
- Operation labels: `pages.{context}.operations.create`
- Message labels: `pages.{context}.messages.createSuccess`

## Workflow

### 1) Create or update drawer component file

Create or update:

- `src/pages/{domain}/{context}/components/Create{Context}.vue`

Use this template structure:

```vue
<template>
  <form-drawer
    v-model:visible="drawerVisible"
    :title="t('pages.{context}.operations.create')"
    size="large"
    :confirm-text="t('pages.common.create')"
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
      <!-- More form fields -->
    </t-row>
  </form-drawer>
</template>

<script setup lang="ts">
import type { FormRule } from 'tdesign-vue-next';
import { MessagePlugin } from 'tdesign-vue-next';
import { computed, ref } from 'vue';

import * as {context} from '@/api/{domain}/{context}';
import FormDrawer from '@/components/form-drawer/index.vue';
import { t } from '@/locales';

interface Props {
  visible: boolean;
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
    const res = await {context}.create(formData.value);
    MessagePlugin.success(t('pages.{context}.messages.createSuccess'));
    drawerVisible.value = false;
    emit('success', res);
    resolve();
  } catch (error) {
    reject(error);
  }
};
</script>
```

Available `<t-form-item>` field component templates:

```vue
<template>
  <!-- Text input -->
  <t-input />
  <!-- Multi-line text input -->
  <t-textarea />
  <!-- True/false switch -->
  <t-switch />
  <!-- Number input -->
  <t-input-number />
  <!-- Select -->
  <t-select />
  <!-- Date picker -->
  <t-date-picker />
  <!-- Code input, supported format values: json (default), markdown, groovy -->
  <code-input format="json" />
</template>
```

```vue
<script setup lang="ts">
// Import CodeInput when using <code-input>
import CodeInput from '@/components/code-input/index.vue';
</script>
```

### 2) Integrate or update drawer wiring in page `index.vue`

Update `src/pages/{domain}/{context}/index.vue` in two places.

Script section changes:

```vue
<script setup lang="ts">
// Import create drawer component
import Create{Context} from './components/Create{Context}.vue';

// Create drawer visible state
const createDrawerVisible = ref(false);

// Create-success callback
const onCreateSuccess = () => {
  pagination.value.current = 1;
  fetchData();
};
</script>
```

Template section changes:

```vue
<template>
  <!-- Add create button under Other operations -->
  <t-button theme="primary" @click="createDrawerVisible = true">
    {{ t('pages.common.create') }}
  </t-button>

  <!-- Add drawer component under Drawer Components -->
  <create-{context} :visible="createDrawerVisible" @success="onCreateSuccess" />
</template>
```

## Integration rules

- Use `FormDrawer` component at `@/components/form-drawer/index.vue`.
- Keep component name deterministic: `Create{Context}`.
- Use `v-model:visible` and emit `update:visible` for open/close state sync.
- Call `{context}.create(...)` in `handleValidateSuccess`.
- Emit `success` after successful creation so list page can refresh.
- Keep generated code and comments in English only.

## Validation steps

From `{console-project-name}` run:

```bash
pnpm run build:type
pnpm run lint -- src/pages/{domain}/{context}/index.vue src/pages/{domain}/{context}/components/Create{Context}.vue
```

If lint script does not accept glob/path arguments, run:

```bash
pnpm run lint
```

Manual checks:

- Drawer opens/closes from create button correctly.
- Form validation blocks invalid submit.
- Successful submit calls `create` API and refreshes list page.
- `pages.{context}.operations.create` and `pages.{context}.messages.createSuccess` keys exist.

## Completion report format

When finishing, report:

- API + i18n inputs used
- Created/updated component file path
- Created/updated page file path
- Validation commands and outcomes
- Any unresolved i18n or field-mapping ambiguity
