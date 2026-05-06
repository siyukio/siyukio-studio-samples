import { defineStore } from 'pinia';

import * as auth from '@/api/auth';
import * as user from '@/api/user';
import router from '@/router';
import { usePermissionStore } from '@/store';
import type { UserInfo } from '@/types/interface';
import { createAndSetDefaultAuthProvider, setAccessToken } from '@/utils/acp';

const InitUserInfo: UserInfo = {
  name: '', // 用户名，用于展示在页面右上角头像处
  roles: [], // 前端权限模型使用 如果使用请配置modules/permission-fe.ts使用
};

export const useUserStore = defineStore('user-me', {
  state: () => ({
    userInfo: { ...InitUserInfo },
    updateAt: 0,
    accessToken: '',
    refreshToken: '',
  }),
  getters: {
    token: (state) => {
      return state.refreshToken;
    },
    roles: (state) => {
      return state.userInfo?.roles;
    },
  },
  actions: {
    async login(req: auth.LoginRequest) {
      const res = await auth.login(req);
      this.accessToken = res.accessToken;
      this.refreshToken = res.refreshToken;
      await this.initAuthProvider();
    },
    async getUserInfo() {
      if (this.updateAt === 0 || Date.now() - this.updateAt > 30000) {
        this.userInfo.roles = [];
        const res = await user.me();
        this.updateAt = Date.now();
        this.userInfo = {
          name: res.nickname,
          roles: res.roles,
        };
      }
      return this.userInfo;
    },
    async initAuthProvider() {
      if (this.accessToken) {
        setAccessToken(this.accessToken);
      }
      if (this.refreshToken) {
        createAndSetDefaultAuthProvider(auth.AuthApi.Refresh, this.refreshToken);

        await this.getUserInfo();
        const permissionStore = usePermissionStore();
        await permissionStore.initRoutes();
      } else {
        router.push('/login');
      }
    },
    async logout() {
      this.refreshToken = '';
      this.accessToken = '';
      this.userInfo = { ...InitUserInfo };
      this.updateAt = 0;
    },
  },
  persist: {
    afterRestore: async (ctx) => {
      await ctx.store.initAuthProvider();
    },
    key: 'user-me',
    paths: ['refreshToken'],
  },
});
