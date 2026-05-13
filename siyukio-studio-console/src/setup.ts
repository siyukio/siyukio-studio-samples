import type { App } from 'vue';

import LoadingButton from '@/components/loading-button/index.vue';

export const setupComponents = (app: App) => {
  app.component('LoadingButton', LoadingButton);
};
