<template>
  <t-space direction="vertical" style="width: 100%" size="1px">
    <t-space style="margin-top: 4px" size="1px">
      <t-button v-if="format === 'json'" size="small" theme="primary" variant="text" @click="handleClickFormat">{{
        t('components.format')
      }}</t-button>
      <t-button size="small" theme="primary" variant="text" @click="handleClickFullscreen">{{
        t('components.fullscreen')
      }}</t-button>
      <t-button size="small" theme="primary" variant="text" @click="handleClickCopyToClipboard">{{
        t('components.copy')
      }}</t-button>
    </t-space>
    <codemirror
      :model-value="modelValue"
      :style="{ height: props.height, width: props.width }"
      :extensions="extensions"
      @change="onChange"
    />
  </t-space>
</template>
<script setup lang="ts">
import { java } from '@codemirror/lang-java';
import { json, jsonParseLinter } from '@codemirror/lang-json';
import { markdown, markdownLanguage } from '@codemirror/lang-markdown';
import { linter } from '@codemirror/lint';
import { search } from '@codemirror/search';
import { oneDark } from '@codemirror/theme-one-dark';
import { DialogPlugin, MessagePlugin, Watermark } from 'tdesign-vue-next';
import { h, ref } from 'vue';
import { Codemirror } from 'vue-codemirror';

import { watermark } from '@/config/global';
import { t } from '@/locales';
import { useSettingStore } from '@/store';

const props = defineProps({
  header: {
    type: String,
    default: '',
  },
  modelValue: {
    type: String,
    required: true,
  },
  height: {
    type: String,
    default: '300px',
  },
  width: {
    type: String,
    default: '100%',
  },
  format: {
    type: String as () => 'json' | 'markdown' | 'groovy',
    default: 'json',
    validator: (v: string) => ['json', 'markdown', 'groovy'].includes(v),
  },
});
const emit = defineEmits(['update:modelValue']);

const settingStore = useSettingStore();
const fontColor = settingStore.brandTheme;

const extensions = ref([]);
if (props.format === 'json') {
  extensions.value = [oneDark, search(), json(), linter(jsonParseLinter(), { autoPanel: true })];
} else if (props.format === 'groovy') {
  extensions.value = [oneDark, java()];
} else {
  extensions.value = [oneDark, markdown({ base: markdownLanguage })];
}

const onChange = (newValue: any) => {
  emit('update:modelValue', newValue);
};

const handleClickFormat = () => {
  try {
    if (props.format === 'json' && props.modelValue) {
      const parsed = JSON.parse(props.modelValue);
      const formatted = JSON.stringify(parsed, null, 2);
      emit('update:modelValue', formatted);
    }
  } catch (error) {
    console.error('JSON parse error:', error);
  }
};

const handleClickCopyToClipboard = () => {
  if (navigator.clipboard) {
    navigator.clipboard.writeText(props.modelValue);
  }
  MessagePlugin.success(t('components.copySuccess'));
};

const handleClickFullscreen = () => {
  const confirmDia = DialogPlugin({
    header: props.header,
    body: () => {
      return h(
        Watermark,
        {
          watermarkContent: { text: watermark, fontColor },
          lineSpace: 24,
          x: 100,
          y: 120,
          width: 158,
          height: 22,
          alpha: 0.15,
        },
        () =>
          h(Codemirror, {
            modelValue: props.modelValue,
            extensions: extensions.value,
            style: { height: '790px', width: '100%' },
            onChange,
          }),
      );
    },
    showOverlay: true,
    footer: false,
    mode: 'full-screen',
    onClose: () => {
      confirmDia.hide();
    },
  });
};
</script>
<style lang="less" scoped></style>
