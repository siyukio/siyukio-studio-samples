<template>
  <t-drawer
    v-model:visible="drawerVisible"
    :header="title"
    :close-btn="showCloseBtn"
    :size="size"
    :confirm-btn="null"
    :cancel-btn="null"
    destroy-on-close
    @close-btn-click="handleClose"
    @overlay-click="handleClose"
  >
    <slot />
    <template #footer>
      <slot name="footer">
        <t-space>
          <loading-button v-if="showConfirmBtn" theme="primary" @click="handleConfirm">
            {{ confirmText }}
          </loading-button>
          <t-button v-if="showCancelBtn" theme="default" variant="base" @click="handleCancel">
            {{ cancelText }}
          </t-button>
        </t-space>
      </slot>
    </template>
  </t-drawer>
</template>
<script setup lang="ts">
import { computed } from 'vue';

interface Props {
  visible: boolean;
  title?: string;
  size?: string;

  confirmText?: string;
  cancelText?: string;
  showConfirmBtn?: boolean;
  showCancelBtn?: boolean;
  showCloseBtn?: boolean;
}

interface Emits {
  (e: 'update:visible', value: boolean): void;
  (e: 'confirm', data?: any): void;
  (e: 'cancel', data?: any): void;
}

const props = withDefaults(defineProps<Props>(), {
  title: 'drawer',
  size: 'medium',
  confirmText: 'confirm',
  cancelText: 'cancel',
  showConfirmBtn: true,
  showCancelBtn: true,
  showCloseBtn: true,
});

const emit = defineEmits<Emits>();

const drawerVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value),
});

const handleConfirm = async () => {
  await new Promise((resolve, reject) => {
    emit('confirm', { resolve, reject });
  });
};

const handleCancel = () => {
  drawerVisible.value = false;
  emit('cancel');
};

const handleClose = () => {
  drawerVisible.value = false;
  emit('cancel');
};
</script>
<style lang="less" scoped></style>
