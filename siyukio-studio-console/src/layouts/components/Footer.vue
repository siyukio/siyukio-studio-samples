<template>
  <div :class="`${prefix}-footer`">{{ copyright }}</div>
</template>
<script setup lang="ts">
import { appName, prefix } from '@/config/global';

const currentYear = new Date().getFullYear();
const copyright = `Copyright © 2021-${currentYear} ${appName}. All Rights Reserved`;
</script>
<style lang="less" scoped>
.@{starter-prefix}-footer {
  color: var(--td-text-color-placeholder);
  line-height: 20px;
  text-align: center;
}
</style>
