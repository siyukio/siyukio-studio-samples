import type { Stream } from '@agentclientprotocol/sdk';

export interface WebSocketClientTransportOptions {
  closeSocketOnAbort?: boolean;
  closeSocketOnCancel?: boolean;
  closeSocketOnWritableClose?: boolean;
  ndjson?: boolean;
}

export type CreateWebSocketClientTransportOptions = WebSocketClientTransportOptions & {
  protocols?: string | string[];
};

const socketStateNameMap = {
  [WebSocket.CONNECTING]: 'CONNECTING',
  [WebSocket.OPEN]: 'OPEN',
  [WebSocket.CLOSING]: 'CLOSING',
  [WebSocket.CLOSED]: 'CLOSED',
} as const;

const createSocketStateError = (socket: WebSocket) => {
  const stateName = socketStateNameMap[socket.readyState as keyof typeof socketStateNameMap] ?? 'UNKNOWN';
  return new Error(`ACP websocket is not open (state=${stateName}).`);
};

const waitForSocketOpen = async (socket: WebSocket): Promise<void> => {
  if (socket.readyState === WebSocket.OPEN) {
    return;
  }
  if (socket.readyState !== WebSocket.CONNECTING) {
    throw createSocketStateError(socket);
  }
  await new Promise<void>((resolve, reject) => {
    const handleOpen = () => {
      cleanup();
      resolve();
    };
    const handleClose = () => {
      cleanup();
      reject(createSocketStateError(socket));
    };
    const handleError = () => {
      cleanup();
      reject(new Error('ACP websocket open failed.'));
    };
    const cleanup = () => {
      socket.removeEventListener('open', handleOpen);
      socket.removeEventListener('close', handleClose);
      socket.removeEventListener('error', handleError);
    };
    socket.addEventListener('open', handleOpen, { once: true });
    socket.addEventListener('close', handleClose, { once: true });
    socket.addEventListener('error', handleError, { once: true });
  });
};

const decodeSocketMessagePayload = async (payload: unknown): Promise<string> => {
  if (typeof payload === 'string') {
    return payload;
  }
  if (payload instanceof ArrayBuffer) {
    return new TextDecoder().decode(payload);
  }
  if (payload instanceof Blob) {
    return await payload.text();
  }
  if (ArrayBuffer.isView(payload)) {
    return new TextDecoder().decode(payload);
  }
  throw new Error('Unsupported websocket payload for ACP transport.');
};

const parseAndEnqueueMessages = (payload: string, controller: ReadableStreamDefaultController<unknown>) => {
  const lines = payload.split('\n');
  for (const line of lines) {
    const trimmed = line.trim();
    if (!trimmed) {
      continue;
    }
    try {
      controller.enqueue(JSON.parse(trimmed));
    } catch (error) {
      console.error('Failed to parse ACP websocket message:', trimmed, error);
    }
  }
};

const closeSocketIfActive = (socket: WebSocket, code: number, reason: string) => {
  if (socket.readyState === WebSocket.OPEN || socket.readyState === WebSocket.CONNECTING) {
    socket.close(code, reason);
  }
};

export const websocketClientTransport = (socket: WebSocket, options: WebSocketClientTransportOptions = {}): Stream => {
  const readable = new ReadableStream<unknown>({
    start(controller) {
      let isClosed = false;
      const finishReadable = () => {
        if (isClosed) {
          return;
        }
        isClosed = true;
        cleanup();
        controller.close();
      };
      const failReadable = (error: unknown) => {
        if (isClosed) {
          return;
        }
        isClosed = true;
        cleanup();
        controller.error(error);
      };
      const handleMessage = (event: MessageEvent<unknown>) => {
        void decodeSocketMessagePayload(event.data)
          .then((payload) => {
            if (isClosed) {
              return;
            }
            parseAndEnqueueMessages(payload, controller);
          })
          .catch((error) => {
            failReadable(error);
          });
      };
      const handleClose = () => {
        finishReadable();
      };
      const handleError = () => {
        failReadable(new Error('ACP websocket transport error.'));
      };
      const cleanup = () => {
        socket.removeEventListener('message', handleMessage);
        socket.removeEventListener('close', handleClose);
        socket.removeEventListener('error', handleError);
      };

      socket.addEventListener('message', handleMessage);
      socket.addEventListener('close', handleClose);
      socket.addEventListener('error', handleError);
    },
    cancel() {
      if (options.closeSocketOnCancel ?? true) {
        closeSocketIfActive(socket, 1000, 'ACP stream cancelled');
      }
    },
  });

  const writable = new WritableStream<unknown>({
    async write(message) {
      await waitForSocketOpen(socket);
      const serializedMessage = JSON.stringify(message);
      socket.send(options.ndjson ? `${serializedMessage}\n` : serializedMessage);
    },
    close() {
      if (options.closeSocketOnWritableClose ?? false) {
        closeSocketIfActive(socket, 1000, 'ACP writable closed');
      }
    },
    abort() {
      if (options.closeSocketOnAbort ?? true) {
        closeSocketIfActive(socket, 1000, 'ACP writable aborted');
      }
    },
  });

  return {
    readable: readable as ReadableStream<any>,
    writable: writable as WritableStream<any>,
  };
};

export const createWebSocketClientTransport = async (
  url: string,
  options: CreateWebSocketClientTransportOptions = {},
): Promise<{
  socket: WebSocket;
  stream: Stream;
}> => {
  const socket = options.protocols ? new WebSocket(url, options.protocols) : new WebSocket(url);
  await waitForSocketOpen(socket);
  const stream = websocketClientTransport(socket, options);
  return { socket, stream };
};
