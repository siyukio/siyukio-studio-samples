export default {
  fields: {
    id: 'ID',
    category: 'Category',
    description: 'Description',
    key: 'Key',
    value: 'Value',
    enabled: 'Status',
    createdAt: 'Created At',
    createdAtTs: 'Created Timestamp',
    updatedAt: 'Updated At',
    updatedAtTs: 'Updated Timestamp',
  },
  enums: {
    enabledEnum: {
      true: 'Enabled',
      false: 'Disabled',
    },
  },
  queryFields: {
    category: 'Category',
    key: 'Key',
    enabled: 'Status',
  },
  validations: {
    categoryRequired: 'Category is required',
    keyRequired: 'Key is required',
    valueRequired: 'Value is required',
    idRequired: 'ID is required',
  },
  operations: {
    list: 'Query',
    create: 'Create',
    get: 'View',
    update: 'Update',
  },
  messages: {
    listSuccess: 'Query succeeded',
    listFailure: 'Query failed',
    createSuccess: 'Create succeeded',
    createFailure: 'Create failed',
    getSuccess: 'View succeeded',
    getFailure: 'View failed',
    updateSuccess: 'Update succeeded',
    updateFailure: 'Update failed',
  },
};
