<template>
  <t-layout key="side">
    <login-header />
    <t-watermark
      :watermark-content="[{ text: watermark, fontColor }]"
      :line-space="24"
      :x="100"
      :y="120"
      :width="158"
      :height="22"
      :alpha="0.15"
      class="login-wrapper"
    >
      <div class="login-container">
        <div class="title-container">
          <h1 class="title margin-no">{{ t('pages.login.loginTitle') }}</h1>
          <h1 class="title">{{ appName }}</h1>
          <div class="sub-title">
            <p class="tip">{{ type === 'register' ? t('pages.login.existAccount') : t('pages.login.noAccount') }}</p>
            <p class="tip" @click="switchType(type === 'register' ? 'login' : 'register')">
              {{ type === 'register' ? t('pages.login.signIn') : t('pages.login.createAccount') }}
            </p>
          </div>
        </div>

        <login v-if="type === 'login'" />
        <register v-else @register-success="switchType('login')" />
        <tdesign-setting />
      </div>

      <footer class="copyright">{{ copyright }}</footer>
    </t-watermark>
  </t-layout>
</template>
<script setup lang="ts">
import { ref } from 'vue';

import { appName, watermark } from '@/config/global';
import TdesignSetting from '@/layouts/setting.vue';
import { t } from '@/locales';
import { useSettingStore } from '@/store';

import LoginHeader from './components/Header.vue';
import Login from './components/Login.vue';
import Register from './components/Register.vue';

defineOptions({
  name: 'LoginIndex',
});

const currentYear = new Date().getFullYear();
const copyright = `Copyright © 2021-${currentYear} ${appName}. All Rights Reserved`;

const settingStore = useSettingStore();
const fontColor = settingStore.brandTheme;

const type = ref('login');
const switchType = (val: string) => {
  type.value = val;
};
</script>
<style lang="less" scoped>
@import './index.less';
</style>
