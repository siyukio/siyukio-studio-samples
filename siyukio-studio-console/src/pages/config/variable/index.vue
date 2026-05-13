<template>
  <div class="common-container">
    <t-form :data="formData" :label-width="120" colon @reset="onReset" @submit="onSubmit">
      <t-row>
        <t-col :span="10">
          <t-row :gutter="[12, 16]">
            <t-col :span="4">
              <t-form-item :label="t('pages.variable.queryFields.category')" name="category">
                <t-input v-model="formData.category" clearable />
              </t-form-item>
            </t-col>
            <t-col :span="4">
              <t-form-item :label="t('pages.variable.queryFields.key')" name="key">
                <t-input v-model="formData.key" clearable />
              </t-form-item>
            </t-col>
            <t-col :span="4">
              <t-form-item :label="t('pages.variable.queryFields.enabled')" name="enabled">
                <t-select v-model="formData.enabled" :options="enabledOptions" clearable />
              </t-form-item>
            </t-col>
          </t-row>
        </t-col>
        <t-col :span="2" class="operation-container">
          <t-button theme="primary" type="submit">
            {{ t('pages.common.query') }}
          </t-button>
          <t-button type="reset" variant="base" theme="default">
            {{ t('pages.common.reset') }}
          </t-button>
          <t-button theme="primary" variant="outline" @click="createDrawerVisible = true">
            {{ t('pages.common.create') }}
          </t-button>
        </t-col>
      </t-row>
    </t-form>

    <t-divider />

    <t-table
      :data="data"
      :columns="COLUMNS"
      :row-key="rowKey"
      :pagination="pagination"
      hover
      size="small"
      bordered
      @page-change="onPageChange"
    >
      <template #enabled="{ row }">
        {{ row.enabled ? t('pages.variable.enums.enabledEnum.true') : t('pages.variable.enums.enabledEnum.false') }}
      </template>

      <template #op="{ row }">
        <t-space direction="vertical">
          <t-link size="small" theme="primary" hover="color" @click="handleClickEdit(row)">
            {{ t('pages.common.edit') }}
          </t-link>
        </t-space>
      </template>
    </t-table>

    <create-variable :visible="createDrawerVisible" @success="onCreateSuccess" />
    <update-variable :id="selectId" :visible="updateDrawerVisible" @success="onUpdateSuccess" />
  </div>
</template>
<script setup lang="ts">
import type { PrimaryTableCol } from 'tdesign-vue-next';
import { MessagePlugin } from 'tdesign-vue-next';
import { onActivated, ref } from 'vue';

import * as variable from '@/api/config/variable';
import { t } from '@/locales';

import CreateVariable from './components/CreateVariable.vue';
import UpdateVariable from './components/UpdateVariable.vue';

defineOptions({
  name: 'VariableList',
});

const COLUMNS: PrimaryTableCol[] = [
  {
    title: t('pages.variable.fields.id'),
    fixed: 'left',
    width: 120,
    ellipsis: true,
    align: 'left',
    colKey: 'id',
  },
  { title: t('pages.variable.fields.category'), colKey: 'category', width: 120, ellipsis: true },
  { title: t('pages.variable.fields.key'), colKey: 'key', width: 180, ellipsis: true },
  { title: t('pages.variable.fields.value'), colKey: 'value', width: 220, ellipsis: true },
  { title: t('pages.variable.fields.enabled'), colKey: 'enabled', width: 100, ellipsis: true },
  { title: t('pages.variable.fields.updatedAt'), colKey: 'updatedAt', width: 180, ellipsis: true },
  {
    align: 'left',
    fixed: 'right',
    width: 80,
    colKey: 'op',
    title: t('pages.common.operation'),
  },
];

interface FormData {
  category?: string;
  key?: string;
  enabled?: boolean;
}

const defaultFormData: FormData = {
  category: '',
  key: '',
  enabled: undefined,
};

const enabledOptions = [
  { label: t('pages.variable.enums.enabledEnum.true'), value: true },
  { label: t('pages.variable.enums.enabledEnum.false'), value: false },
];

const rowKey = 'id';
const data = ref<variable.VariableListResponse[]>([]);
const pagination = ref({
  pageSize: 20,
  total: 0,
  current: 1,
  showJumper: true,
});

const formData = ref<FormData>({ ...defaultFormData });

const createDrawerVisible = ref(false);
const updateDrawerVisible = ref(false);
const selectId = ref('');

const onReset = () => {
  formData.value = { ...defaultFormData };
  pagination.value.current = 1;
  fetchData();
};

const onSubmit = () => {
  pagination.value.current = 1;
  fetchData();
};

const onPageChange = (pageInfo: { pageSize?: number; current?: number }) => {
  pagination.value = {
    ...pagination.value,
    current: pageInfo?.current ?? pagination.value.current,
    pageSize: pageInfo?.pageSize ?? pagination.value.pageSize,
  };
  fetchData();
};

const handleClickEdit = (row: variable.VariableListResponse) => {
  selectId.value = row.id;
  updateDrawerVisible.value = true;
};

const onCreateSuccess = () => {
  pagination.value.current = 1;
  fetchData();
};

const onUpdateSuccess = () => {
  fetchData();
};

const fetchData = async () => {
  try {
    const res = await variable.list({
      page: pagination.value.current,
      size: pagination.value.pageSize,
      filter: formData.value,
    });
    data.value = res.items;
    pagination.value = {
      ...pagination.value,
      total: res.total,
    };
  } catch (e) {
    console.error(e);
    MessagePlugin.error(t('pages.variable.messages.listFailure'));
  }
};

onActivated(() => {
  fetchData();
});
</script>
