import { postRequestWithAuth } from '@/utils/acp';

import type { PageRequest, PageResponse } from './model/commonModel';

export const AdminVariableApi = {
  List: '/admin/variable/listVariable',
  Create: '/admin/variable/createVariable',
  Get: '/admin/variable/getVariable',
  Update: '/admin/variable/updateVariable',
};

export interface AdminVariableFilter {
  category?: string;
  key?: string;
  enabled?: boolean;
}

export interface AdminVariableListResponse {
  id: string;
  category: string;
  description: string;
  key: string;
  value: string;
  enabled: boolean;
  createdAt: string;
  createdAtTs: number;
  updatedAt: string;
  updatedAtTs: number;
}

export interface AdminVariableCreateRequest {
  category: string;
  description?: string;
  key: string;
  value: string;
}

export interface AdminVariableCreateResponse {
  id: string;
  category: string;
  description: string;
  key: string;
  value: string;
  enabled: boolean;
  createdAt: string;
  createdAtTs: number;
  updatedAt: string;
  updatedAtTs: number;
}

export interface AdminVariableGetRequest {
  id: string;
}

export interface AdminVariableGetResponse {
  id: string;
  category: string;
  description: string;
  key: string;
  value: string;
  enabled: boolean;
  createdAt: string;
  createdAtTs: number;
  updatedAt: string;
  updatedAtTs: number;
}

export interface AdminVariableUpdateRequest {
  id: string;
  category?: string;
  description?: string;
  key?: string;
  value?: string;
  enabled?: boolean;
}

export interface AdminVariableUpdateResponse {
  id: string;
  category: string;
  description: string;
  key: string;
  value: string;
  enabled: boolean;
  createdAt: string;
  createdAtTs: number;
  updatedAt: string;
  updatedAtTs: number;
}

export const list = (request: PageRequest<AdminVariableFilter>) => {
  return postRequestWithAuth<PageResponse<AdminVariableListResponse>>(AdminVariableApi.List, request);
};

export const create = (request: AdminVariableCreateRequest) => {
  return postRequestWithAuth<AdminVariableCreateResponse>(AdminVariableApi.Create, request);
};

export const get = (request: AdminVariableGetRequest) => {
  return postRequestWithAuth<AdminVariableGetResponse>(AdminVariableApi.Get, request);
};

export const update = (request: AdminVariableUpdateRequest) => {
  return postRequestWithAuth<AdminVariableUpdateResponse>(AdminVariableApi.Update, request);
};
