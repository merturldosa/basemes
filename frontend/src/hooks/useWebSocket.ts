import { useEffect, useRef, useState } from 'react';
import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

/**
 * WebSocket Hook
 * STOMP over WebSocket for real-time updates
 * @author Moon Myung-seop
 */

const WS_BASE_URL = import.meta.env.VITE_WS_BASE_URL || 'http://localhost:8080/ws';

interface UseWebSocketOptions {
  onConnect?: () => void;
  onDisconnect?: () => void;
  onError?: (error: any) => void;
}

export const useWebSocket = (options?: UseWebSocketOptions) => {
  const clientRef = useRef<Client | null>(null);
  const [isConnected, setIsConnected] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const tenantId = localStorage.getItem('tenantId');
    const token = localStorage.getItem('accessToken');

    if (!tenantId) {
      setError('Tenant ID not found');
      return;
    }

    // Create STOMP client with SockJS
    const client = new Client({
      webSocketFactory: () => new SockJS(WS_BASE_URL),
      connectHeaders: {
        Authorization: `Bearer ${token}`,
        'X-Tenant-ID': tenantId,
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: () => {
        setIsConnected(true);
        setError(null);
        options?.onConnect?.();
      },
      onDisconnect: () => {
        setIsConnected(false);
        options?.onDisconnect?.();
      },
      onStompError: (frame) => {
        setError(frame.headers?.message || 'WebSocket error');
        options?.onError?.(frame);
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
      clientRef.current = null;
    };
  }, []);

  const subscribe = (topic: string, callback: (message: any) => void) => {
    if (!clientRef.current || !isConnected) {
      return () => {};
    }

    const subscription = clientRef.current.subscribe(topic, (message: IMessage) => {
      try {
        const data = JSON.parse(message.body);
        callback(data);
      } catch {
        // Failed to parse WebSocket message
      }
    });

    return () => {
      subscription.unsubscribe();
    };
  };

  const send = (destination: string, body: any) => {
    if (!clientRef.current || !isConnected) {
      return;
    }

    clientRef.current.publish({
      destination,
      body: JSON.stringify(body),
    });
  };

  return {
    isConnected,
    error,
    subscribe,
    send,
  };
};

/**
 * Hook for work order updates
 */
export const useWorkOrderUpdates = (callback: (workOrder: any) => void) => {
  const { subscribe } = useWebSocket();
  const tenantId = localStorage.getItem('tenantId');

  useEffect(() => {
    if (!tenantId) return;

    const unsubscribe = subscribe(`/topic/work-orders/${tenantId}`, callback);
    return unsubscribe;
  }, [tenantId, callback]);
};

/**
 * Hook for work progress updates
 */
export const useWorkProgressUpdates = (callback: (progress: any) => void) => {
  const { subscribe } = useWebSocket();
  const tenantId = localStorage.getItem('tenantId');

  useEffect(() => {
    if (!tenantId) return;

    const unsubscribe = subscribe(`/topic/work-progress/${tenantId}`, callback);
    return unsubscribe;
  }, [tenantId, callback]);
};

/**
 * Hook for defect updates
 */
export const useDefectUpdates = (callback: (defect: any) => void) => {
  const { subscribe } = useWebSocket();
  const tenantId = localStorage.getItem('tenantId');

  useEffect(() => {
    if (!tenantId) return;

    const unsubscribe = subscribe(`/topic/defects/${tenantId}`, callback);
    return unsubscribe;
  }, [tenantId, callback]);
};

/**
 * Hook for SOP execution updates
 */
export const useSOPExecutionUpdates = (callback: (execution: any) => void) => {
  const { subscribe } = useWebSocket();
  const tenantId = localStorage.getItem('tenantId');

  useEffect(() => {
    if (!tenantId) return;

    const unsubscribe = subscribe(`/topic/sop-execution/${tenantId}`, callback);
    return unsubscribe;
  }, [tenantId, callback]);
};
