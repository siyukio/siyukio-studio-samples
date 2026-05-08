export default {
  fields: {
    id: '变量ID',
    category: '变量分类',
    description: '变量描述',
    key: '变量键',
    value: '变量值',
    enabled: '变量启用状态',
    createdAt: '创建时间',
    createdAtTs: '创建时间戳',
    updatedAt: '更新时间',
    updatedAtTs: '更新时间戳',
  },
  enums: {
    enabledEnum: {
      true: '启用',
      false: '禁用',
    },
  },
  queryFields: {
    category: '变量分类',
    key: '变量键',
    enabled: '变量启用状态',
  },
  validations: {
    idRequired: '请填写变量ID',
    categoryRequired: '请填写变量分类',
    keyRequired: '请填写变量键',
    valueRequired: '请填写变量值',
  },
  operations: {
    list: '查询',
    get: '查看',
    create: '创建',
    update: '更新',
  },
  messages: {
    listSuccess: '查询成功',
    listFailure: '查询失败',
    getSuccess: '查看成功',
    getFailure: '查看失败',
    createSuccess: '创建成功',
    createFailure: '创建失败',
    updateSuccess: '更新成功',
    updateFailure: '更新失败',
  },
};
