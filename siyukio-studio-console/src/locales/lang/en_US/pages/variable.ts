export default {
  fields: {
    id: 'Variable ID',
    category: 'Variable Category',
    description: 'Variable Description',
    key: 'Variable Key',
    value: 'Variable Value',
    enabled: 'Variable Enabled Status',
    createdAt: 'Created At',
    createdAtTs: 'Created At Timestamp',
    updatedAt: 'Updated At',
    updatedAtTs: 'Updated At Timestamp',
  },
  enums: {
    enabledEnum: {
      true: 'Enabled',
      false: 'Disabled',
    },
  },
  queryFields: {
    category: 'Variable Category',
    key: 'Variable Key',
    enabled: 'Variable Enabled Status',
  },
  validations: {
    idRequired: 'Variable ID is required',
    categoryRequired: 'Variable Category is required',
    keyRequired: 'Variable Key is required',
    valueRequired: 'Variable Value is required',
  },
  operations: {
    list: 'Query',
    get: 'View',
    create: 'Create',
    update: 'Update',
  },
  messages: {
    listSuccess: 'Query succeeded',
    listFailure: 'Query failed',
    getSuccess: 'View succeeded',
    getFailure: 'View failed',
    createSuccess: 'Create succeeded',
    createFailure: 'Create failed',
    updateSuccess: 'Update succeeded',
    updateFailure: 'Update failed',
  },
};
