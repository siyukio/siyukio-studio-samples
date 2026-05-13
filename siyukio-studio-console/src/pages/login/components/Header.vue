<template>
  <header class="login-header">
    <logo-full-icon class="logo" />
    <div class="operations-container">
      <t-button theme="default" shape="square" variant="text" @click="navToHelper">
        <t-icon name="help-circle" class="icon" />
      </t-button>
      <t-dropdown trigger="click">
        <t-button theme="default" shape="square" variant="text">
          <translate-icon />
        </t-button>
        <template #dropdown>
          <t-dropdown-item
            v-for="(lang, index) in langList"
            :key="index"
            :value="lang.value"
            @click="(options) => changeLang(options.value as string)"
            >{{ lang.content }}
          </t-dropdown-item>
        </template>
      </t-dropdown>
      <t-button theme="default" shape="square" variant="text" @click="toggleSettingPanel">
        <t-icon name="setting" class="icon" />
      </t-button>
    </div>
  </header>
</template>
<script setup lang="ts">
import { TranslateIcon } from 'tdesign-icons-vue-next';

import LogoFullIcon from '@/assets/assets-logo-full.svg?component';
import { langList } from '@/locales';
import { useLocale } from '@/locales/useLocale';
import { useSettingStore } from '@/store';

const settingStore = useSettingStore();
const toggleSettingPanel = () => {
  settingStore.updateConfig({
    showSettingPanel: true,
  });
};

const navToHelper = () => {
  window.open('http://tdesign.tencent.com/starter/docs/get-started');
};

const { changeLocale } = useLocale();
const changeLang = (lang: string) => {
  changeLocale(lang);
};
</script>
<style lang="less" scoped>
.login-header {
  padding: 0 var(--td-comp-paddingLR-xl);
  display: flex;
  justify-content: space-between;
  align-items: center;
  backdrop-filter: blur(10px);
  color: var(--td-text-color-primary);
  height: var(--td-comp-size-xxxl);

  .logo {
    width: 178px;
    height: var(--td-comp-size-s);
    fill: var(--td-brand-color);
  }

  .operations-container {
    display: flex;
    align-items: center;

    .t-button {
      margin-left: var(--td-comp-margin-l);
    }
  }
}
</style>
