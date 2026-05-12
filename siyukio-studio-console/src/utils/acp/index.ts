import * as acp from '@agentclientprotocol/sdk';
import type { AxiosRequestConfig } from 'axios';
import axios from 'axios';

import type { WebSocketClientTransportOptions } from './websocketClientTransport';
import { createWebSocketClientTransport } from './websocketClientTransport';

export const acpBaseUrl = import.meta.env.VITE_ACP_BASE_URL || '';

let simpleAcpClientAuthorization = '';
const bearerPrefixRegex = /^Bearer\s+/i;
const trailingSlashRegex = /\/+$/;
const httpPrefixRegex = /^http:/;
const httpsPrefixRegex = /^https:/;

const getAccessTokenFromAuthorization = (authorization: string): string => {
  const normalizedAuthorization = authorization.trim();
  if (!normalizedAuthorization) {
    return '';
  }
  return normalizedAuthorization.replace(bearerPrefixRegex, '');
};

const buildDefaultAcpWebSocketUrl = (authorization: string): string => {
  if (!acpBaseUrl) {
    return '';
  }
  const wsBaseUrl = `${acpBaseUrl.replace(trailingSlashRegex, '')}/acp`
    .replace(httpPrefixRegex, 'ws:')
    .replace(httpsPrefixRegex, 'wss:');
  const accessToken = getAccessTokenFromAuthorization(authorization);
  return `${wsBaseUrl}?accessToken=${encodeURIComponent(accessToken)}`;
};

export class ApiError {
  code: number;
  message: string;
  constructor(code: number, message: string) {
    this.code = code;
    this.message = message;
  }
}

// Hook types
export type RequestPreHook = (path: string, data: any, options?: AxiosRequestConfig) => void;
export type RequestCompleteHook = (path: string, data: any, options?: AxiosRequestConfig) => void;
export type RequestErrorHook = (apiError: ApiError, path: string, data: any) => void;

// Default empty implementations
let defaultPreHook: RequestPreHook = () => {};
let defaultCompleteHook: RequestCompleteHook = () => {};
let defaultErrorHook: RequestErrorHook = () => {};

// Setters for external configuration
export const setRequestPreHook = (hook: RequestPreHook) => {
  defaultPreHook = hook;
};

export const setRequestCompleteHook = (hook: RequestCompleteHook) => {
  defaultCompleteHook = hook;
};

export const setRequestErrorHook = (hook: RequestErrorHook) => {
  defaultErrorHook = hook;
};

const authCache = {
  accessToken: '',
  refreshAt: 0,
  maxTime: 600000,
};

let defaultAuthProvider = (): Promise<string> => {
  return Promise.resolve('');
};

export const postRequest = async <T = any>(path: string, data: any = {}, config?: AxiosRequestConfig): Promise<T> => {
  defaultPreHook(path, data, config);

  const url = acpBaseUrl + path;
  try {
    const response = await axios.post<T>(url, data, config);
    const responseData = response.data as any;
    const { error } = responseData;
    if (error) {
      const apiError = new ApiError(error.code, error.message);
      defaultErrorHook(apiError, path, data);
      throw apiError;
    }
    return responseData;
  } catch (error: any) {
    if (error instanceof ApiError) {
      throw error;
    }
    console.error('POST error:', error);
    throw error;
  } finally {
    defaultCompleteHook(path, data, config);
  }
};

export const postRequestWithAuth = async <T = any>(
  path: string,
  data: any = {},
  config?: AxiosRequestConfig,
): Promise<T> => {
  config = config ?? {};
  config.headers = config.headers ?? {};
  config.headers.Authorization = await defaultAuthProvider();
  return postRequest(path, data, config);
};

export const getAccessToken = async () => {
  return await defaultAuthProvider();
};

export const setAccessToken = (accessToken: string) => {
  authCache.accessToken = accessToken;
  authCache.refreshAt = Date.now();
};

export const setDefaultAuthProvider = (authProvider: () => Promise<string>) => {
  defaultAuthProvider = authProvider;
};

export const setSimpleAcpClientAuthorization = (authorization: string) => {
  simpleAcpClientAuthorization = authorization;
};

export const createAndSetDefaultAuthProvider = (refreshApi: string, refreshToken: string) => {
  defaultAuthProvider = async () => {
    if (Date.now() - authCache.refreshAt <= authCache.maxTime && authCache.accessToken) {
      return authCache.accessToken;
    }
    const tokenResponse = await postRequest<any>(refreshApi, {
      refreshToken,
    });

    if (tokenResponse.error) {
      throw new Error(`${tokenResponse.error.message}`);
    }
    authCache.accessToken = tokenResponse.accessToken;
    authCache.refreshAt = Date.now();
    return tokenResponse.accessToken;
  };
};

export type AcpRequestPermissionHook = (
  params: acp.RequestPermissionRequest,
) => Promise<acp.RequestPermissionResponse> | acp.RequestPermissionResponse;
export type AcpSessionUpdateHook = (params: acp.SessionNotification) => Promise<void> | void;
export type AcpReadTextFileHook = (
  params: acp.ReadTextFileRequest,
) => Promise<acp.ReadTextFileResponse> | acp.ReadTextFileResponse;
export type AcpWriteTextFileHook = (
  params: acp.WriteTextFileRequest,
) => Promise<acp.WriteTextFileResponse> | acp.WriteTextFileResponse;

export interface SimpleAcpClientHooks {
  requestPermission?: AcpRequestPermissionHook;
  sessionUpdate?: AcpSessionUpdateHook;
  readTextFile?: AcpReadTextFileHook;
  writeTextFile?: AcpWriteTextFileHook;
}

export interface CreateSimpleAcpClientOptions {
  url?: string;
  authorization?: string;
  protocols?: string | string[];
  transport?: WebSocketClientTransportOptions;
  initialize?: Omit<acp.InitializeRequest, 'protocolVersion'> & {
    protocolVersion?: acp.ProtocolVersion;
  };
  hooks?: SimpleAcpClientHooks;
}

const defaultRequestPermissionHook: AcpRequestPermissionHook = async (params) => {
  const preferredOption =
    params.options.find((option) => option.kind === 'allow_once') ||
    params.options.find((option) => option.kind === 'allow_always') ||
    params.options[0];
  if (!preferredOption) {
    return {
      outcome: {
        outcome: 'cancelled',
      },
    };
  }
  return {
    outcome: {
      outcome: 'selected',
      optionId: preferredOption.optionId,
    },
  };
};

const createDefaultClientCapabilities = (hooks: SimpleAcpClientHooks): acp.ClientCapabilities | undefined => {
  const fsCapability: NonNullable<acp.ClientCapabilities['fs']> = {};
  if (hooks.readTextFile) {
    fsCapability.readTextFile = true;
  }
  if (hooks.writeTextFile) {
    fsCapability.writeTextFile = true;
  }
  if (!Object.keys(fsCapability).length) {
    return undefined;
  }
  return {
    fs: fsCapability,
  };
};

class SimpleAcpClientDelegate implements acp.Client {
  hooks: SimpleAcpClientHooks;

  constructor(hooks: SimpleAcpClientHooks = {}) {
    this.hooks = hooks;
  }

  async requestPermission(params: acp.RequestPermissionRequest): Promise<acp.RequestPermissionResponse> {
    const handler = this.hooks.requestPermission || defaultRequestPermissionHook;
    return await handler(params);
  }

  async sessionUpdate(params: acp.SessionNotification): Promise<void> {
    if (!this.hooks.sessionUpdate) {
      return;
    }
    await this.hooks.sessionUpdate(params);
  }

  async writeTextFile(params: acp.WriteTextFileRequest): Promise<acp.WriteTextFileResponse> {
    if (!this.hooks.writeTextFile) {
      throw new Error('ACP writeTextFile hook is not configured.');
    }
    return await this.hooks.writeTextFile(params);
  }

  async readTextFile(params: acp.ReadTextFileRequest): Promise<acp.ReadTextFileResponse> {
    if (!this.hooks.readTextFile) {
      throw new Error('ACP readTextFile hook is not configured.');
    }
    return await this.hooks.readTextFile(params);
  }
}

export class SimpleAcpClient {
  readonly socket: WebSocket;
  readonly connection: acp.ClientSideConnection;
  readonly initializeResponse: acp.InitializeResponse;

  private constructor(
    socket: WebSocket,
    connection: acp.ClientSideConnection,
    initializeResponse: acp.InitializeResponse,
  ) {
    this.socket = socket;
    this.connection = connection;
    this.initializeResponse = initializeResponse;
  }

  static async connect(options: CreateSimpleAcpClientOptions = {}): Promise<SimpleAcpClient> {
    const hooks = options.hooks ?? {};
    const authorization = options.authorization || simpleAcpClientAuthorization || authCache.accessToken;
    const resolvedUrl = options.url || buildDefaultAcpWebSocketUrl(authorization);
    if (!resolvedUrl) {
      throw new Error('ACP websocket url is required. Provide options.url or configure VITE_ACP_BASE_URL.');
    }

    const { socket, stream } = await createWebSocketClientTransport(resolvedUrl, {
      protocols: options.protocols,
      ...(options.transport ?? {}),
    });

    const delegate = new SimpleAcpClientDelegate(hooks);
    const connection = new acp.ClientSideConnection(() => delegate, stream);

    const initializeRequest: acp.InitializeRequest = {
      protocolVersion: options.initialize?.protocolVersion ?? acp.PROTOCOL_VERSION,
    };
    if (options.initialize?.clientInfo !== undefined) {
      initializeRequest.clientInfo = options.initialize.clientInfo;
    }
    if (options.initialize?._meta !== undefined) {
      initializeRequest._meta = options.initialize._meta;
    }

    const defaultClientCapabilities = createDefaultClientCapabilities(hooks);
    if (options.initialize?.clientCapabilities !== undefined) {
      initializeRequest.clientCapabilities = options.initialize.clientCapabilities;
    } else if (defaultClientCapabilities) {
      initializeRequest.clientCapabilities = defaultClientCapabilities;
    }

    const initializeResponse = await connection.initialize(initializeRequest);
    return new SimpleAcpClient(socket, connection, initializeResponse);
  }

  get signal(): AbortSignal {
    return this.connection.signal;
  }

  get closed(): Promise<void> {
    return this.connection.closed;
  }

  async newSession(params: acp.NewSessionRequest): Promise<acp.NewSessionResponse> {
    return await this.connection.newSession(params);
  }

  async prompt(params: acp.PromptRequest): Promise<acp.PromptResponse> {
    return await this.connection.prompt(params);
  }

  async cancel(params: acp.CancelNotification): Promise<void> {
    await this.connection.cancel(params);
  }

  close(code = 1000, reason = 'client closed'): void {
    if (this.socket.readyState === WebSocket.OPEN || this.socket.readyState === WebSocket.CONNECTING) {
      this.socket.close(code, reason);
    }
  }
}

export const createSimpleAcpClient = async (options: CreateSimpleAcpClientOptions = {}): Promise<SimpleAcpClient> => {
  return await SimpleAcpClient.connect(options);
};
