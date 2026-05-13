import type { PageRequest, PageResponse } from '@/api/model/commonModel';
import { postRequestWithAuth } from '@/utils/acp';

export const VariableApi = {
  List: '/admin/variable/listVariable',
  Create: '/admin/variable/createVariable',
  Get: '/admin/variable/getVariable',
  Update: '/admin/variable/updateVariable',
};

export interface VariableFilter {
  category?: string;
  key?: string;
  enabled?: boolean;
}

export interface VariableCreateRequest {
  category: string;
  description?: string;
  key: string;
  value: string;
}

export interface VariableCreateResponse {
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

export interface VariableGetRequest {
  id: string;
}

export interface VariableGetResponse {
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

export interface VariableListResponse {
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

export interface VariableUpdateRequest {
  id: string;
  category?: string;
  description?: string;
  key?: string;
  value?: string;
  enabled?: boolean;
}

export interface VariableUpdateResponse {
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

export const list = (request: PageRequest<VariableFilter>) => {
  return postRequestWithAuth<PageResponse<VariableListResponse>>(VariableApi.List, request);
};

export const create = (request: VariableCreateRequest) => {
  return postRequestWithAuth<VariableCreateResponse>(VariableApi.Create, request);
};

export const get = (request: VariableGetRequest) => {
  return postRequestWithAuth<VariableGetResponse>(VariableApi.Get, request);
};

export const update = (request: VariableUpdateRequest) => {
  return postRequestWithAuth<VariableUpdateResponse>(VariableApi.Update, request);
};
