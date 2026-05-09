<template>
  <form-drawer
    v-model:visible="drawerVisible"
    :title="t('pages.variable.operations.update')"
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
        <t-form-item :label="t('pages.variable.fields.id')" name="id">
          <t-input v-model="formData.id" readonly />
        </t-form-item>
      </t-col>
      <t-col :span="12">
        <t-form-item :label="t('pages.variable.fields.enabled')" name="enabled">
          <t-switch v-model="formData.enabled" />
        </t-form-item>
      </t-col>
      <t-col :span="12">
        <t-form-item :label="t('pages.variable.fields.category')" name="category">
          <t-input v-model="formData.category" />
        </t-form-item>
      </t-col>
      <t-col :span="12">
        <t-form-item :label="t('pages.variable.fields.key')" name="key">
          <t-input v-model="formData.key" />
        </t-form-item>
      </t-col>
      <t-col :span="24">
        <t-form-item :label="t('pages.variable.fields.value')" name="value">
          <code-input v-model="formData.value" format="json" />
        </t-form-item>
      </t-col>
      <t-col :span="24">
        <t-form-item :label="t('pages.variable.fields.description')" name="description">
          <t-input v-model="formData.description" />
        </t-form-item>
      </t-col>
      <t-col :span="12">
        <t-form-item :label="t('pages.variable.fields.createdAt')" name="createdAt">
          <t-input v-model="formData.createdAt" readonly />
        </t-form-item>
      </t-col>
      <t-col :span="12">
        <t-form-item :label="t('pages.variable.fields.updatedAt')" name="updatedAt">
          <t-input v-model="formData.updatedAt" readonly />
        </t-form-item>
      </t-col>
    </t-row>
  </form-drawer>
</template>
<script setup lang="ts">
import type { FormRule } from 'tdesign-vue-next';
import { MessagePlugin } from 'tdesign-vue-next';
import { computed, ref, watch } from 'vue';

import * as variable from '@/api/config/variable';
import CodeInput from '@/components/code-input/index.vue';
import FormDrawer from '@/components/form-drawer/index.vue';
import { t } from '@/locales';

interface Props {
  visible: boolean;
  id: string;
}

interface Emits {
  (e: 'update:visible', value: boolean): void;
  (e: 'success', data: variable.VariableUpdateResponse): void;
}

interface FormData {
  id: string;
  category: string;
  key: string;
  value: string;
  description: string;
  enabled: boolean;
  createdAt: string;
  updatedAt: string;
}

const props = withDefaults(defineProps<Props>(), {});
const emit = defineEmits<Emits>();

const drawerVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value),
});

const formData = ref<FormData>({
  id: props.id,
  category: '',
  key: '',
  value: '',
  description: '',
  enabled: true,
  createdAt: '',
  updatedAt: '',
});

const formRules: Record<string, FormRule[]> = {
  category: [
    {
      required: true,
      message: t('pages.variable.validations.categoryRequired'),
      type: 'error',
    },
  ],
  key: [
    {
      required: true,
      message: t('pages.variable.validations.keyRequired'),
      type: 'error',
    },
  ],
  value: [
    {
      required: true,
      message: t('pages.variable.validations.valueRequired'),
      type: 'error',
    },
  ],
};

const handleValidateSuccess = async (params: { resolve: () => void; reject: (reason?: unknown) => void }) => {
  const { resolve, reject } = params;
  try {
    const res = await variable.update({
      id: formData.value.id,
      category: formData.value.category,
      key: formData.value.key,
      value: formData.value.value,
      description: formData.value.description,
      enabled: formData.value.enabled,
    });
    MessagePlugin.success(t('pages.variable.messages.updateSuccess'));
    drawerVisible.value = false;
    emit('success', res);
    resolve();
  } catch (error) {
    reject(error);
  }
};

const fetchData = async () => {
  if (!props.id) {
    return;
  }
  try {
    const res = await variable.get({ id: props.id });
    formData.value = {
      id: res.id,
      category: res.category,
      key: res.key,
      value: res.value,
      description: res.description,
      enabled: res.enabled,
      createdAt: res.createdAt,
      updatedAt: res.updatedAt,
    };
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
