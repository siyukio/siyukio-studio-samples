<template>
  <common-drawer
    v-model:visible="drawerVisible"
    :title="title"
    :size="size"
    :confirm-text="confirmText"
    :cancel-text="cancelText"
    :show-confirm-btn="showConfirmBtn"
    :show-cancel-btn="showCancelBtn"
    :show-close-btn="showCloseBtn"
    @confirm="handleConfirm"
    @cancel="handleCancel"
  >
    <t-form ref="formRef" reset-type="initial" :data="formData" :rules="formRules" :label-width="labelWidth">
      <slot />
    </t-form>
  </common-drawer>
</template>
<script setup lang="ts">
import type { FormInstanceFunctions, FormRule } from 'tdesign-vue-next';
import { computed, ref } from 'vue';

import CommonDrawer from '@/components/common-drawer/index.vue';

interface Props {
  visible: boolean;
  title?: string;
  size?: string;
  confirmText?: string;
  cancelText?: string;
  showConfirmBtn?: boolean;
  showCancelBtn?: boolean;
  showCloseBtn?: boolean;
  formData: any;
  formRules: Record<string, FormRule[]>;
  labelWidth?: string | number;
}

interface Emits {
  (e: 'update:visible', value: boolean): void;
  (e: 'validate-success', data: any): void;
}

const props = withDefaults(defineProps<Props>(), {
  showConfirmBtn: true,
  showCancelBtn: true,
  showCloseBtn: true,
});

const emit = defineEmits<Emits>();

const drawerVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value),
});

const formRef = ref<FormInstanceFunctions>();

const handleValidateSuccess = async (): Promise<boolean> => {
  try {
    await new Promise((resolve, reject) => {
      emit('validate-success', { resolve, reject });
    });
  } catch (error) {
    console.error('handleValidateSuccess failed:', error);
    return false;
  }
  return true;
};

const handleConfirm = async (params: any) => {
  const { resolve } = params;
  const valid = await formRef.value?.validate();
  if (valid === true) {
    const result = await handleValidateSuccess();
    if (result) {
      formRef.value.reset();
    }
  }
  resolve();
};

const handleCancel = () => {
  formRef.value.reset();
};
</script>
