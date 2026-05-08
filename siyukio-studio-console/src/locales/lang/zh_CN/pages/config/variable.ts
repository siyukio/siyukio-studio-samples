export default {
  fields: {
    id: 'ID',
    category: '分类',
    description: '描述',
    key: '键',
    value: '值',
    enabled: '状态',
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
    category: '分类',
    key: '键',
    enabled: '状态',
  },
  validations: {
    categoryRequired: '请填写分类',
    keyRequired: '请填写键',
    valueRequired: '请填写值',
    idRequired: '请填写ID',
  },
  operations: {
    list: '查询',
    create: '创建',
    get: '查看',
    update: '更新',
  },
  messages: {
    listSuccess: '查询成功',
    listFailure: '查询失败',
    createSuccess: '创建成功',
    createFailure: '创建失败',
    getSuccess: '查看成功',
    getFailure: '查看失败',
    updateSuccess: '更新成功',
    updateFailure: '更新失败',
  },
};
