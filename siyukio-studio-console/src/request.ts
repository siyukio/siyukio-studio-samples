import NProgress from 'nprogress';
import { MessagePlugin } from 'tdesign-vue-next';

import { setRequestCompleteHook, setRequestErrorHook, setRequestPreHook } from '@/utils/acp';

setRequestPreHook(() => {
  NProgress.start();
});

setRequestCompleteHook(() => {
  NProgress.done();
});

setRequestErrorHook((apiError) => {
  MessagePlugin.error(apiError.message);
});
