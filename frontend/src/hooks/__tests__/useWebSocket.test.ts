import { renderHook, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { useWebSocket, useWorkProgressUpdates } from '../useWebSocket';
import { Client } from '@stomp/stompjs';

/**
 * WebSocket Hook Tests
 * Tests for WebSocket connection and subscription hooks
 * @author Moon Myung-seop
 */

// Mock @stomp/stompjs
vi.mock('@stomp/stompjs', () => ({
  Client: vi.fn(() => ({
    activate: vi.fn(),
    deactivate: vi.fn(),
    subscribe: vi.fn(() => ({
      unsubscribe: vi.fn(),
    })),
    publish: vi.fn(),
    connected: false,
    onConnect: null,
    onDisconnect: null,
    onStompError: null,
  })),
}));

// Mock SockJS
vi.mock('sockjs-client', () => ({
  default: vi.fn(() => ({})),
}));

// Mock localStorage
const localStorageMock = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn(),
};
Object.defineProperty(window, 'localStorage', {
  value: localStorageMock,
  writable: true,
});

describe('useWebSocket', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorageMock.getItem.mockImplementation((key: string) => {
      if (key === 'tenantId') return 'test-tenant';
      if (key === 'accessToken') return 'test-token';
      return null;
    });
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should initialize WebSocket client', () => {
    const { result } = renderHook(() => useWebSocket());

    expect(Client).toHaveBeenCalled();
    expect(result.current.isConnected).toBe(false);
    expect(result.current.error).toBeNull();
  });

  it('should create client with correct headers', () => {
    renderHook(() => useWebSocket());

    const clientConstructorCall = vi.mocked(Client).mock.calls[0][0];
    expect(clientConstructorCall.connectHeaders).toEqual({
      Authorization: 'Bearer test-token',
      'X-Tenant-ID': 'test-tenant',
    });
  });

  it('should activate client on mount', () => {
    const mockActivate = vi.fn();
    vi.mocked(Client).mockImplementation(
      () =>
        ({
          activate: mockActivate,
          deactivate: vi.fn(),
          subscribe: vi.fn(),
        } as any)
    );

    renderHook(() => useWebSocket());

    expect(mockActivate).toHaveBeenCalled();
  });

  it('should deactivate client on unmount', () => {
    const mockDeactivate = vi.fn();
    vi.mocked(Client).mockImplementation(
      () =>
        ({
          activate: vi.fn(),
          deactivate: mockDeactivate,
          subscribe: vi.fn(),
        } as any)
    );

    const { unmount } = renderHook(() => useWebSocket());
    unmount();

    expect(mockDeactivate).toHaveBeenCalled();
  });

  it('should handle connection', async () => {
    let onConnectCallback: (() => void) | null = null;

    vi.mocked(Client).mockImplementation(
      (config) =>
        ({
          activate: () => {
            onConnectCallback = config.onConnect as () => void;
            if (onConnectCallback) onConnectCallback();
          },
          deactivate: vi.fn(),
          subscribe: vi.fn(),
        } as any)
    );

    const onConnect = vi.fn();
    const { result } = renderHook(() => useWebSocket({ onConnect }));

    await waitFor(() => {
      expect(result.current.isConnected).toBe(true);
      expect(onConnect).toHaveBeenCalled();
    });
  });

  it('should handle disconnection', async () => {
    let onDisconnectCallback: (() => void) | null = null;

    vi.mocked(Client).mockImplementation(
      (config) =>
        ({
          activate: () => {
            const onConnect = config.onConnect as () => void;
            onConnect();
            onDisconnectCallback = config.onDisconnect as () => void;
          },
          deactivate: vi.fn(),
          subscribe: vi.fn(),
        } as any)
    );

    const onDisconnect = vi.fn();
    const { result } = renderHook(() => useWebSocket({ onDisconnect }));

    await waitFor(() => {
      expect(result.current.isConnected).toBe(true);
    });

    // Trigger disconnect
    if (onDisconnectCallback) onDisconnectCallback();

    await waitFor(() => {
      expect(result.current.isConnected).toBe(false);
      expect(onDisconnect).toHaveBeenCalled();
    });
  });

  it('should handle STOMP errors', async () => {
    let onStompErrorCallback: ((frame: any) => void) | null = null;

    vi.mocked(Client).mockImplementation(
      (config) =>
        ({
          activate: () => {
            onStompErrorCallback = config.onStompError as (frame: any) => void;
          },
          deactivate: vi.fn(),
          subscribe: vi.fn(),
        } as any)
    );

    const onError = vi.fn();
    const { result } = renderHook(() => useWebSocket({ onError }));

    const errorFrame = {
      headers: { message: 'Connection error' },
      body: 'Error details',
    };

    if (onStompErrorCallback) onStompErrorCallback(errorFrame);

    await waitFor(() => {
      expect(result.current.error).toBe('Connection error');
      expect(onError).toHaveBeenCalledWith(errorFrame);
    });
  });

  it('should set error when tenant ID is missing', () => {
    localStorageMock.getItem.mockReturnValue(null);

    const { result } = renderHook(() => useWebSocket());

    expect(result.current.error).toBe('Tenant ID not found');
  });

  describe('subscribe', () => {
    it('should subscribe to topic when connected', async () => {
      const mockSubscribe = vi.fn(() => ({
        unsubscribe: vi.fn(),
      }));

      vi.mocked(Client).mockImplementation(
        (config) =>
          ({
            activate: () => {
              const onConnect = config.onConnect as () => void;
              onConnect();
            },
            deactivate: vi.fn(),
            subscribe: mockSubscribe,
          } as any)
      );

      const { result } = renderHook(() => useWebSocket());

      await waitFor(() => {
        expect(result.current.isConnected).toBe(true);
      });

      const callback = vi.fn();
      result.current.subscribe('/topic/test', callback);

      expect(mockSubscribe).toHaveBeenCalledWith(
        '/topic/test',
        expect.any(Function)
      );
    });

    it('should not subscribe when disconnected', () => {
      const mockSubscribe = vi.fn();

      vi.mocked(Client).mockImplementation(
        () =>
          ({
            activate: vi.fn(),
            deactivate: vi.fn(),
            subscribe: mockSubscribe,
          } as any)
      );

      const { result } = renderHook(() => useWebSocket());

      const callback = vi.fn();
      result.current.subscribe('/topic/test', callback);

      expect(mockSubscribe).not.toHaveBeenCalled();
    });

    it('should parse and deliver messages', async () => {
      let messageHandler: ((message: any) => void) | null = null;

      const mockSubscribe = vi.fn((topic, handler) => {
        messageHandler = handler;
        return { unsubscribe: vi.fn() };
      });

      vi.mocked(Client).mockImplementation(
        (config) =>
          ({
            activate: () => {
              const onConnect = config.onConnect as () => void;
              onConnect();
            },
            deactivate: vi.fn(),
            subscribe: mockSubscribe,
          } as any)
      );

      const { result } = renderHook(() => useWebSocket());

      await waitFor(() => {
        expect(result.current.isConnected).toBe(true);
      });

      const callback = vi.fn();
      result.current.subscribe('/topic/test', callback);

      // Simulate message
      const message = {
        body: JSON.stringify({ data: 'test message' }),
      };

      if (messageHandler) messageHandler(message);

      expect(callback).toHaveBeenCalledWith({ data: 'test message' });
    });

    it('should handle malformed JSON', async () => {
      let messageHandler: ((message: any) => void) | null = null;

      const mockSubscribe = vi.fn((topic, handler) => {
        messageHandler = handler;
        return { unsubscribe: vi.fn() };
      });

      vi.mocked(Client).mockImplementation(
        (config) =>
          ({
            activate: () => {
              const onConnect = config.onConnect as () => void;
              onConnect();
            },
            deactivate: vi.fn(),
            subscribe: mockSubscribe,
          } as any)
      );

      const { result } = renderHook(() => useWebSocket());

      await waitFor(() => {
        expect(result.current.isConnected).toBe(true);
      });

      const callback = vi.fn();
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});

      result.current.subscribe('/topic/test', callback);

      // Simulate malformed message
      const message = {
        body: 'invalid json',
      };

      if (messageHandler) messageHandler(message);

      expect(callback).not.toHaveBeenCalled();
      expect(consoleSpy).toHaveBeenCalled();

      consoleSpy.mockRestore();
    });
  });

  describe('send', () => {
    it('should send message when connected', async () => {
      const mockPublish = vi.fn();

      vi.mocked(Client).mockImplementation(
        (config) =>
          ({
            activate: () => {
              const onConnect = config.onConnect as () => void;
              onConnect();
            },
            deactivate: vi.fn(),
            subscribe: vi.fn(),
            publish: mockPublish,
          } as any)
      );

      const { result } = renderHook(() => useWebSocket());

      await waitFor(() => {
        expect(result.current.isConnected).toBe(true);
      });

      result.current.send('/app/test', { data: 'test' });

      expect(mockPublish).toHaveBeenCalledWith({
        destination: '/app/test',
        body: JSON.stringify({ data: 'test' }),
      });
    });

    it('should not send when disconnected', () => {
      const mockPublish = vi.fn();

      vi.mocked(Client).mockImplementation(
        () =>
          ({
            activate: vi.fn(),
            deactivate: vi.fn(),
            subscribe: vi.fn(),
            publish: mockPublish,
          } as any)
      );

      const { result } = renderHook(() => useWebSocket());

      result.current.send('/app/test', { data: 'test' });

      expect(mockPublish).not.toHaveBeenCalled();
    });
  });
});

describe('useWorkProgressUpdates', () => {
  it('should subscribe to work progress topic', async () => {
    const mockSubscribe = vi.fn(() => vi.fn());

    vi.mocked(Client).mockImplementation(
      (config) =>
        ({
          activate: () => {
            const onConnect = config.onConnect as () => void;
            onConnect();
          },
          deactivate: vi.fn(),
          subscribe: mockSubscribe,
        } as any)
    );

    const callback = vi.fn();
    renderHook(() => useWorkProgressUpdates(callback));

    await waitFor(() => {
      expect(mockSubscribe).toHaveBeenCalledWith(
        '/topic/work-progress/test-tenant',
        expect.any(Function)
      );
    });
  });

  it('should not subscribe when tenant ID is missing', () => {
    localStorageMock.getItem.mockReturnValue(null);

    const mockSubscribe = vi.fn();

    vi.mocked(Client).mockImplementation(
      () =>
        ({
          activate: vi.fn(),
          deactivate: vi.fn(),
          subscribe: mockSubscribe,
        } as any)
    );

    const callback = vi.fn();
    renderHook(() => useWorkProgressUpdates(callback));

    expect(mockSubscribe).not.toHaveBeenCalled();
  });

  it('should unsubscribe on unmount', async () => {
    const mockUnsubscribe = vi.fn();
    const mockSubscribe = vi.fn(() => mockUnsubscribe);

    vi.mocked(Client).mockImplementation(
      (config) =>
        ({
          activate: () => {
            const onConnect = config.onConnect as () => void;
            onConnect();
          },
          deactivate: vi.fn(),
          subscribe: mockSubscribe,
        } as any)
    );

    const callback = vi.fn();
    const { unmount } = renderHook(() => useWorkProgressUpdates(callback));

    await waitFor(() => {
      expect(mockSubscribe).toHaveBeenCalled();
    });

    unmount();

    expect(mockUnsubscribe).toHaveBeenCalled();
  });
});
