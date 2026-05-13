import { beforeEach, describe, expect, it, vi } from 'vitest';

const authorization =
  'eyJhbGciOiJFUzI1NiJ9.eyJoIjpmYWxzZSwic3ViIjoiaW50ZXJuYWwiLCJzIjpbIklOVEVSTkFMIl0sImV4cCI6NDg4ODg4MDQ3MCwiZSI6ImludGVybmFsIiwianRpIjoiYVY3V3NhUXBYRlJDV3FMQjhoZkdqIn0.mF5t5kbI-vi_5NsWJIEBnKo8r_OIihE_jKu021ytKG0qoOit7ZoEPo62eKW0wCMzP1y4vyXnMP66V5WKifIkaw';

describe('simpleAcpClient', () => {
  beforeEach(() => {
    vi.resetModules();
    vi.stubEnv('VITE_ACP_BASE_URL', 'http://localhost:8080');
  });

  it('connects to localhost ACP websocket and initializes with real authorization', async () => {
    const { createSimpleAcpClient, setSimpleAcpClientAuthorization } = await import('./index');
    setSimpleAcpClientAuthorization(authorization);

    const client = await createSimpleAcpClient();

    expect(client.socket.url.startsWith('ws://localhost:8080/acp?accessToken=')).toBe(true);
    expect(client.initializeResponse.protocolVersion).toBe(1);

    client.close();
    await Promise.race([
      client.closed,
      new Promise<void>((resolve) => {
        setTimeout(resolve, 2000);
      }),
    ]);
  });
});
