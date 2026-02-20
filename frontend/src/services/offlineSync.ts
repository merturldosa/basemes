import localforage from 'localforage';
import popService from './popService';
import sopOperatorService from './sopOperatorService';

/**
 * Offline Sync Service
 * Manages offline data queuing and synchronization
 * @author Moon Myung-seop
 */

// Configure localforage instances
const workProgressQueue = localforage.createInstance({
  name: 'SoIceMES',
  storeName: 'work_progress_queue',
});

const defectQueue = localforage.createInstance({
  name: 'SoIceMES',
  storeName: 'defect_queue',
});

const sopQueue = localforage.createInstance({
  name: 'SoIceMES',
  storeName: 'sop_queue',
});

export interface QueuedWorkProgress {
  id: string;
  timestamp: number;
  progressId: number;
  quantity: number;
  retryCount: number;
}

export interface QueuedDefect {
  id: string;
  timestamp: number;
  progressId: number;
  defectQuantity: number;
  defectType: string;
  defectReason?: string;
  defectLocation?: string;
  severity: 'CRITICAL' | 'MAJOR' | 'MINOR';
  notes?: string;
  retryCount: number;
}

export interface QueuedSOPStep {
  id: string;
  timestamp: number;
  executionId: number;
  stepId: number;
  passed: boolean;
  notes?: string;
  retryCount: number;
}

export interface SyncStatus {
  isOnline: boolean;
  isSyncing: boolean;
  queuedItems: number;
  lastSyncTime: number | null;
  failedItems: number;
}

class OfflineSyncService {
  private syncInProgress = false;
  private onlineListeners: Array<(isOnline: boolean) => void> = [];
  private syncStatusListeners: Array<(status: SyncStatus) => void> = [];
  private maxRetries = 3;

  constructor() {
    this.initializeConnectionMonitoring();
  }

  /**
   * Initialize connection status monitoring
   */
  private initializeConnectionMonitoring() {
    window.addEventListener('online', () => {
      this.notifyOnlineStatus(true);
      this.syncQueue();
    });

    window.addEventListener('offline', () => {
      this.notifyOnlineStatus(false);
    });

    // Initial sync if online
    if (navigator.onLine) {
      this.syncQueue();
    }
  }

  /**
   * Check if device is online
   */
  isOnline(): boolean {
    return navigator.onLine;
  }

  /**
   * Add listener for online status changes
   */
  onOnlineStatusChange(callback: (isOnline: boolean) => void) {
    this.onlineListeners.push(callback);
    return () => {
      this.onlineListeners = this.onlineListeners.filter((cb) => cb !== callback);
    };
  }

  /**
   * Add listener for sync status changes
   */
  onSyncStatusChange(callback: (status: SyncStatus) => void) {
    this.syncStatusListeners.push(callback);
    return () => {
      this.syncStatusListeners = this.syncStatusListeners.filter((cb) => cb !== callback);
    };
  }

  /**
   * Notify online status listeners
   */
  private notifyOnlineStatus(isOnline: boolean) {
    this.onlineListeners.forEach((callback) => callback(isOnline));
  }

  /**
   * Notify sync status listeners
   */
  private async notifySyncStatus() {
    const status = await this.getSyncStatus();
    this.syncStatusListeners.forEach((callback) => callback(status));
  }

  /**
   * Queue work progress record for offline sync
   */
  async queueWorkProgress(progressId: number, quantity: number): Promise<void> {
    const item: QueuedWorkProgress = {
      id: `wp_${Date.now()}_${Math.random()}`,
      timestamp: Date.now(),
      progressId,
      quantity,
      retryCount: 0,
    };

    await workProgressQueue.setItem(item.id, item);
    this.notifySyncStatus();
  }

  /**
   * Queue defect record for offline sync
   */
  async queueDefect(data: Omit<QueuedDefect, 'id' | 'timestamp' | 'retryCount'>): Promise<void> {
    const item: QueuedDefect = {
      id: `def_${Date.now()}_${Math.random()}`,
      timestamp: Date.now(),
      ...data,
      retryCount: 0,
    };

    await defectQueue.setItem(item.id, item);
    this.notifySyncStatus();
  }

  /**
   * Queue SOP step completion for offline sync
   */
  async queueSOPStep(
    executionId: number,
    stepId: number,
    passed: boolean,
    notes?: string
  ): Promise<void> {
    const item: QueuedSOPStep = {
      id: `sop_${Date.now()}_${Math.random()}`,
      timestamp: Date.now(),
      executionId,
      stepId,
      passed,
      notes,
      retryCount: 0,
    };

    await sopQueue.setItem(item.id, item);
    this.notifySyncStatus();
  }

  /**
   * Get current sync status
   */
  async getSyncStatus(): Promise<SyncStatus> {
    const workProgressKeys = await workProgressQueue.keys();
    const defectKeys = await defectQueue.keys();
    const sopKeys = await sopQueue.keys();

    const queuedItems = workProgressKeys.length + defectKeys.length + sopKeys.length;

    // Count failed items (retry count >= maxRetries)
    let failedItems = 0;

    for (const key of workProgressKeys) {
      const item = await workProgressQueue.getItem<QueuedWorkProgress>(key);
      if (item && item.retryCount >= this.maxRetries) {
        failedItems++;
      }
    }

    for (const key of defectKeys) {
      const item = await defectQueue.getItem<QueuedDefect>(key);
      if (item && item.retryCount >= this.maxRetries) {
        failedItems++;
      }
    }

    for (const key of sopKeys) {
      const item = await sopQueue.getItem<QueuedSOPStep>(key);
      if (item && item.retryCount >= this.maxRetries) {
        failedItems++;
      }
    }

    const lastSyncTime = localStorage.getItem('lastSyncTime');

    return {
      isOnline: this.isOnline(),
      isSyncing: this.syncInProgress,
      queuedItems,
      lastSyncTime: lastSyncTime ? parseInt(lastSyncTime) : null,
      failedItems,
    };
  }

  /**
   * Sync all queued items
   */
  async syncQueue(): Promise<void> {
    if (!this.isOnline()) {
      return;
    }

    if (this.syncInProgress) {
      return;
    }

    this.syncInProgress = true;
    this.notifySyncStatus();

    try {
      // Sync work progress records
      await this.syncWorkProgressQueue();

      // Sync defect records
      await this.syncDefectQueue();

      // Sync SOP steps
      await this.syncSOPQueue();

      localStorage.setItem('lastSyncTime', Date.now().toString());
    } catch {
      // Sync error occurred - will retry on next sync cycle
    } finally {
      this.syncInProgress = false;
      this.notifySyncStatus();
    }
  }

  /**
   * Sync work progress queue
   */
  private async syncWorkProgressQueue(): Promise<void> {
    const keys = await workProgressQueue.keys();

    for (const key of keys) {
      const item = await workProgressQueue.getItem<QueuedWorkProgress>(key);
      if (!item) continue;

      if (item.retryCount >= this.maxRetries) {
        // Max retries reached - skip this item
        continue;
      }

      try {
        await popService.recordProgress(item.progressId, item.quantity);
        await workProgressQueue.removeItem(key);
      } catch {
        // Failed to sync work progress - increment retry count
        item.retryCount++;
        await workProgressQueue.setItem(key, item);
      }
    }
  }

  /**
   * Sync defect queue
   */
  private async syncDefectQueue(): Promise<void> {
    const keys = await defectQueue.keys();

    for (const key of keys) {
      const item = await defectQueue.getItem<QueuedDefect>(key);
      if (!item) continue;

      if (item.retryCount >= this.maxRetries) {
        // Max retries reached - skip this item
        continue;
      }

      try {
        await popService.recordDefect(item.progressId, {
          defectQuantity: item.defectQuantity,
          defectType: item.defectType,
          defectReason: item.defectReason,
          defectLocation: item.defectLocation,
          severity: item.severity,
          notes: item.notes,
        });
        await defectQueue.removeItem(key);
      } catch {
        // Failed to sync defect - increment retry count
        item.retryCount++;
        await defectQueue.setItem(key, item);
      }
    }
  }

  /**
   * Sync SOP queue
   */
  private async syncSOPQueue(): Promise<void> {
    const keys = await sopQueue.keys();

    for (const key of keys) {
      const item = await sopQueue.getItem<QueuedSOPStep>(key);
      if (!item) continue;

      if (item.retryCount >= this.maxRetries) {
        // Max retries reached - skip this item
        continue;
      }

      try {
        await sopOperatorService.completeStep(item.executionId, item.stepId, item.passed, item.notes);
        await sopQueue.removeItem(key);
      } catch {
        // Failed to sync SOP step - increment retry count
        item.retryCount++;
        await sopQueue.setItem(key, item);
      }
    }
  }

  /**
   * Clear all failed items from queue
   */
  async clearFailedItems(): Promise<void> {
    const queues = [
      { queue: workProgressQueue, name: 'work progress' },
      { queue: defectQueue, name: 'defect' },
      { queue: sopQueue, name: 'SOP' },
    ];

    for (const { queue, name } of queues) {
      const keys = await queue.keys();
      for (const key of keys) {
        const item = await queue.getItem<any>(key);
        if (item && item.retryCount >= this.maxRetries) {
          await queue.removeItem(key);
        }
      }
    }

    this.notifySyncStatus();
  }

  /**
   * Clear entire queue (use with caution)
   */
  async clearAllQueues(): Promise<void> {
    await workProgressQueue.clear();
    await defectQueue.clear();
    await sopQueue.clear();
    this.notifySyncStatus();
  }

  /**
   * Handle conflict resolution (last-write-wins strategy)
   */
  handleConflict<T>(localData: T, serverData: T): T {
    // Simple last-write-wins: prefer server data
    return serverData;
  }
}

// Export singleton instance
const offlineSyncService = new OfflineSyncService();
export default offlineSyncService;
