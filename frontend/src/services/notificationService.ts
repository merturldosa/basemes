/**
 * Notification Service
 * WebSocket client for real-time notifications
 * @author Moon Myung-seop
 */

import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export interface Notification {
  notificationId: number;
  notificationType: 'INFO' | 'WARNING' | 'ERROR' | 'SUCCESS';
  category: string;
  title: string;
  message: string;
  referenceType?: string;
  referenceId?: number;
  referenceUrl?: string;
  isRead: boolean;
  priority: 'LOW' | 'NORMAL' | 'HIGH' | 'URGENT';
  createdAt: string;
}

type NotificationCallback = (notification: Notification) => void;

class NotificationService {
  private client: Client | null = null;
  private callbacks: NotificationCallback[] = [];
  private connected = false;

  /**
   * Connect to WebSocket server
   */
  connect(userId: number, tenantId: string): void {
    if (this.connected) {
      return;
    }

    // Create SockJS instance
    const socket = new SockJS(`${import.meta.env.VITE_API_URL}/ws`);

    // Create STOMP client
    this.client = new Client({
      webSocketFactory: () => socket as WebSocket,
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      debug: () => {
        // No-op: STOMP debug output suppressed
      },
      onConnect: () => {
        this.connected = true;

        // Subscribe to user-specific notifications
        this.client?.subscribe(`/user/queue/notifications`, (message) => {
          const notification: Notification = JSON.parse(message.body);
          this.handleNotification(notification);
        });

        // Subscribe to tenant-wide broadcast notifications
        this.client?.subscribe(`/topic/notifications/${tenantId}`, (message) => {
          const notification: Notification = JSON.parse(message.body);
          this.handleNotification(notification);
        });
      },
      onDisconnect: () => {
        this.connected = false;
      },
      onStompError: () => {
        // STOMP error occurred - handled silently
      },
    });

    // Activate the client
    this.client.activate();
  }

  /**
   * Disconnect from WebSocket server
   */
  disconnect(): void {
    if (this.client) {
      this.client.deactivate();
      this.client = null;
      this.connected = false;
    }
  }

  /**
   * Register callback for notifications
   */
  onNotification(callback: NotificationCallback): () => void {
    this.callbacks.push(callback);

    // Return unsubscribe function
    return () => {
      this.callbacks = this.callbacks.filter((cb) => cb !== callback);
    };
  }

  /**
   * Handle incoming notification
   */
  private handleNotification(notification: Notification): void {
    // Call all registered callbacks
    this.callbacks.forEach((callback) => {
      try {
        callback(notification);
      } catch {
        // Notification callback error - handled silently
      }
    });

    // Play notification sound
    this.playNotificationSound(notification.priority);

    // Show browser notification if permitted
    this.showBrowserNotification(notification);
  }

  /**
   * Play notification sound
   */
  private playNotificationSound(priority: string): void {
    try {
      // Different sounds for different priorities
      const audio = new Audio('/sounds/notification.mp3');
      audio.volume = priority === 'URGENT' ? 1.0 : 0.5;
      audio.play().catch(() => { /* Sound playback not available */ });
    } catch (error) {
      // Ignore sound errors
    }
  }

  /**
   * Show browser notification
   */
  private showBrowserNotification(notification: Notification): void {
    if ('Notification' in window && Notification.permission === 'granted') {
      new Notification(notification.title, {
        body: notification.message,
        icon: '/icons/icon-192x192.png',
        tag: `notification-${notification.notificationId}`,
        requireInteraction: notification.priority === 'URGENT',
      });
    }
  }

  /**
   * Request browser notification permission
   */
  async requestPermission(): Promise<NotificationPermission> {
    if ('Notification' in window) {
      return await Notification.requestPermission();
    }
    return 'denied';
  }

  /**
   * Check if connected
   */
  isConnected(): boolean {
    return this.connected;
  }
}

// Export singleton instance
export const notificationService = new NotificationService();
export default notificationService;
