<!-- LoadingButton.vue -->
<template>
  <t-button :loading="loading" @click="handleClick">
    <slot />
  </t-button>
</template>
<script setup lang="ts">
import { ref } from 'vue';

interface Props {
  disabled?: boolean;
  onClick?: (event: MouseEvent) => Promise<void> | void;
}

const props = withDefaults(defineProps<Props>(), {
  disabled: false,
});

const loading = ref(false);

const handleClick = async (_e: MouseEvent) => {
  if (loading.value || props.disabled) return;

  try {
    loading.value = true;
    if (props.onClick) {
      const result = props.onClick(_e);
      if (result instanceof Promise) {
        await result;
      }
    }
  } finally {
    loading.value = false;
  }
};
</script>
