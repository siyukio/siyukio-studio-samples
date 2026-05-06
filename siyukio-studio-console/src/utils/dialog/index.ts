// utils/confirmDialog.ts
import { DialogPlugin } from 'tdesign-vue-next';
import { h } from 'vue';

import { t } from '@/locales';

interface ConfirmDialogOptions {
  title?: string;
  content: string;
  confirmText?: string;
  confirmTheme?: 'default' | 'warning' | 'danger' | 'success';
  cancelText?: string;
  theme?: 'default' | 'warning' | 'danger' | 'success';
  onConfirm: () => Promise<void> | void;
  onCancel?: () => void;
  onSuccess?: () => void;
}

export const showConfirmDialog = (options: ConfirmDialogOptions): void => {
  const {
    title = t('pages.common.confirm'),
    content,
    confirmText = t('pages.common.confirm'),
    confirmTheme = 'warning',
    cancelText = t('pages.common.cancel'),
    theme = 'warning',
    onConfirm,
    onCancel,
    onSuccess,
  } = options;

  const confirmDialog = DialogPlugin.confirm({
    header: title,
    body: content,
    confirmBtn: {
      content: confirmText,
      theme: confirmTheme,
    },
    cancelBtn: cancelText,
    theme,
    onConfirm: async () => {
      confirmDialog.setConfirmLoading(true);
      confirmDialog.update({
        confirmBtn: {
          content: confirmText,
          theme: confirmTheme,
          loading: true,
        },
        cancelBtn: {
          content: cancelText,
          disabled: true,
        },
        closeBtn: false,
        closeOnOverlayClick: false,
        closeOnEscKeydown: false,
      });
      try {
        await onConfirm();
        onSuccess?.();
        confirmDialog.destroy();
      } catch (error) {
        confirmDialog.update({
          body: () =>
            h('div', {
              innerHTML: `<div>${content}</div><div style="color: var(--td-error-color)">${error.message}</div>`,
            }),
          confirmBtn: {
            content: confirmText,
            theme: confirmTheme,
            loading: false,
          },
          cancelBtn: {
            content: cancelText,
            disabled: false,
          },
          closeBtn: true,
          closeOnOverlayClick: true,
          closeOnEscKeydown: true,
        });
      }
    },
    onCancel: () => {
      onCancel?.();
      confirmDialog.destroy();
    },
  });
};

export const showRemoveConfirm = (itemName: string, onConfirm: () => Promise<void> | void, onSuccess?: () => void) => {
  return showConfirmDialog({
    title: t('pages.common.removeConfirm'),
    content: t('pages.common.removeConfirmContent', { item: itemName }),
    confirmText: t('pages.common.remove'),
    confirmTheme: 'danger',
    theme: 'danger',
    onConfirm,
    onSuccess,
  });
};

export const showActionConfirm = (
  actionName: string,
  onConfirm: () => Promise<void> | void,
  onSuccess?: () => void,
) => {
  return showConfirmDialog({
    title: t('pages.common.actionConfirm'),
    content: t('pages.common.actionConfirmContent', { action: actionName }),
    onConfirm,
    onSuccess,
  });
};
