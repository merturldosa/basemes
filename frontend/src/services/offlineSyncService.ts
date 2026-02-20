/**
 * Offline Data Sync Service
 * Handles offline data storage and synchronization
 * @author Moon Myung-seop
 */

import { apiClient } from './api';

// IndexedDB configuration
const DB_NAME = 'SoIceMES_POP';
const DB_VERSION = 1;
const STORES = {
  SYNC_QUEUE: 'syncQueue',
  WORK_ORDERS: 'workOrders',
  PRODUCTION_DATA: 'productionData',
  SOP_DATA: 'sopData',
};

export interface SyncQueueItem {
  id: string;
  timestamp: number;
  type: 'CREATE' | 'UPDATE' | 'DELETE';
  entity: string;
  data: any;
  retryCount: number;
  status: 'PENDING' | 'SYNCING' | 'SUCCESS' | 'FAILED';
  error?: string;
}

class OfflineSyncService {
  private db: IDBDatabase | null = null;
  private syncInProgress = false;
  private syncListeners: ((status: boolean) => void)[] = [];

  /**
   * Initialize IndexedDB
   */
  async init(): Promise<void> {
    return new Promise((resolve, reject) => {
      const request = indexedDB.open(DB_NAME, DB_VERSION);

      request.onerror = () => {
        console.error('Failed to open IndexedDB:', request.error);
        reject(request.error);
      };

      request.onsuccess = () => {
        this.db = request.result;
        console.log('IndexedDB initialized successfully');
        resolve();
      };

      request.onupgradeneeded = (event) => {
        const db = (event.target as IDBOpenDBRequest).result;

        // Create object stores
        if (!db.objectStoreNames.contains(STORES.SYNC_QUEUE)) {
          const syncStore = db.createObjectStore(STORES.SYNC_QUEUE, { keyPath: 'id' });
          syncStore.createIndex('status', 'status', { unique: false });
          syncStore.createIndex('timestamp', 'timestamp', { unique: false });
        }

        if (!db.objectStoreNames.contains(STORES.WORK_ORDERS)) {
          db.createObjectStore(STORES.WORK_ORDERS, { keyPath: 'workOrderId' });
        }

        if (!db.objectStoreNames.contains(STORES.PRODUCTION_DATA)) {
          const prodStore = db.createObjectStore(STORES.PRODUCTION_DATA, {
            keyPath: 'id',
            autoIncrement: true,
          });
          prodStore.createIndex('workOrderId', 'workOrderId', { unique: false });
          prodStore.createIndex('timestamp', 'timestamp', { unique: false });
        }

        if (!db.objectStoreNames.contains(STORES.SOP_DATA)) {
          db.createObjectStore(STORES.SOP_DATA, { keyPath: 'sopId' });
        }

        console.log('IndexedDB schema upgraded');
      };
    });
  }

  /**
   * Add item to sync queue
   */
  async addToSyncQueue(item: Omit<SyncQueueItem, 'id' | 'timestamp' | 'retryCount' | 'status'>): Promise<string> {
    if (!this.db) await this.init();

    const queueItem: SyncQueueItem = {
      ...item,
      id: `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
      timestamp: Date.now(),
      retryCount: 0,
      status: 'PENDING',
    };

    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction([STORES.SYNC_QUEUE], 'readwrite');
      const store = transaction.objectStore(STORES.SYNC_QUEUE);
      const request = store.add(queueItem);

      request.onsuccess = () => {
        console.log('Added to sync queue:', queueItem.id);
        resolve(queueItem.id);

        // Trigger sync if online
        if (navigator.onLine) {
          this.syncQueue();
        }
      };

      request.onerror = () => {
        console.error('Failed to add to sync queue:', request.error);
        reject(request.error);
      };
    });
  }

  /**
   * Get all pending sync items
   */
  async getPendingSyncItems(): Promise<SyncQueueItem[]> {
    if (!this.db) await this.init();

    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction([STORES.SYNC_QUEUE], 'readonly');
      const store = transaction.objectStore(STORES.SYNC_QUEUE);
      const index = store.index('status');
      const request = index.getAll('PENDING');

      request.onsuccess = () => {
        resolve(request.result || []);
      };

      request.onerror = () => {
        console.error('Failed to get pending items:', request.error);
        reject(request.error);
      };
    });
  }

  /**
   * Update sync item status
   */
  async updateSyncItemStatus(
    id: string,
    status: SyncQueueItem['status'],
    error?: string
  ): Promise<void> {
    if (!this.db) await this.init();

    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction([STORES.SYNC_QUEUE], 'readwrite');
      const store = transaction.objectStore(STORES.SYNC_QUEUE);
      const getRequest = store.get(id);

      getRequest.onsuccess = () => {
        const item = getRequest.result;
        if (item) {
          item.status = status;
          if (error) item.error = error;
          if (status === 'FAILED') item.retryCount++;

          const updateRequest = store.put(item);
          updateRequest.onsuccess = () => resolve();
          updateRequest.onerror = () => reject(updateRequest.error);
        } else {
          resolve();
        }
      };

      getRequest.onerror = () => reject(getRequest.error);
    });
  }

  /**
   * Delete sync item
   */
  async deleteSyncItem(id: string): Promise<void> {
    if (!this.db) await this.init();

    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction([STORES.SYNC_QUEUE], 'readwrite');
      const store = transaction.objectStore(STORES.SYNC_QUEUE);
      const request = store.delete(id);

      request.onsuccess = () => resolve();
      request.onerror = () => reject(request.error);
    });
  }

  /**
   * Sync queue to server
   */
  async syncQueue(): Promise<void> {
    if (this.syncInProgress || !navigator.onLine) {
      console.log('Sync skipped: already in progress or offline');
      return;
    }

    this.syncInProgress = true;
    this.notifySyncListeners(true);

    try {
      const pendingItems = await this.getPendingSyncItems();
      console.log(`Syncing ${pendingItems.length} items...`);

      for (const item of pendingItems) {
        try {
          await this.updateSyncItemStatus(item.id, 'SYNCING');

          // Execute actual API call based on entity type and action
          await this.syncItem(item);

          // Mark as success and delete
          await this.updateSyncItemStatus(item.id, 'SUCCESS');
          await this.deleteSyncItem(item.id);

          console.log('Synced item:', item.id);
        } catch (error) {
          console.error('Failed to sync item:', item.id, error);

          // Mark as failed (will retry later if retry count < max)
          if (item.retryCount < 3) {
            await this.updateSyncItemStatus(item.id, 'PENDING', error instanceof Error ? error.message : 'Unknown error');
          } else {
            await this.updateSyncItemStatus(item.id, 'FAILED', error instanceof Error ? error.message : 'Max retries exceeded');
          }
        }
      }

      console.log('Sync completed');
    } catch (error) {
      console.error('Sync queue error:', error);
    } finally {
      this.syncInProgress = false;
      this.notifySyncListeners(false);
    }
  }

  /**
   * Execute actual API call based on entity type and action
   */
  private async syncItem(item: SyncQueueItem): Promise<void> {
    const { entity, type: action, data } = item;

    switch (entity) {
      case 'WORK_ORDER':
        if (action === 'CREATE') await apiClient.post('/work-orders', data);
        else if (action === 'UPDATE') await apiClient.put(`/work-orders/${data.id}`, data);
        else if (action === 'DELETE') await apiClient.delete(`/work-orders/${data.id}`);
        break;
      case 'DEFECT':
        if (action === 'CREATE') await apiClient.post('/defects', data);
        else if (action === 'UPDATE') await apiClient.put(`/defects/${data.id}`, data);
        else if (action === 'DELETE') await apiClient.delete(`/defects/${data.id}`);
        break;
      case 'SOP_STEP':
        if (action === 'UPDATE') await apiClient.put(`/sops/executions/${data.executionId}/steps/${data.stepId}`, data);
        break;
      case 'PRODUCTION_DATA':
        if (action === 'CREATE') await apiClient.post('/production-results', data);
        else if (action === 'UPDATE') await apiClient.put(`/production-results/${data.id}`, data);
        break;
      default: {
        // Generic fallback: derive endpoint from entity name
        const endpoint = `/${entity.toLowerCase().replace(/_/g, '-')}s`;
        if (action === 'CREATE') await apiClient.post(endpoint, data);
        else if (action === 'UPDATE') await apiClient.put(`${endpoint}/${data.id}`, data);
        else if (action === 'DELETE') await apiClient.delete(`${endpoint}/${data.id}`);
        break;
      }
    }
  }

  /**
   * Save data locally
   */
  async saveLocal(storeName: string, data: any): Promise<void> {
    if (!this.db) await this.init();

    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction([storeName], 'readwrite');
      const store = transaction.objectStore(storeName);
      const request = store.put(data);

      request.onsuccess = () => resolve();
      request.onerror = () => reject(request.error);
    });
  }

  /**
   * Get data from local storage
   */
  async getLocal(storeName: string, key: any): Promise<any> {
    if (!this.db) await this.init();

    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction([storeName], 'readonly');
      const store = transaction.objectStore(storeName);
      const request = store.get(key);

      request.onsuccess = () => resolve(request.result);
      request.onerror = () => reject(request.error);
    });
  }

  /**
   * Get all data from local storage
   */
  async getAllLocal(storeName: string): Promise<any[]> {
    if (!this.db) await this.init();

    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction([storeName], 'readonly');
      const store = transaction.objectStore(storeName);
      const request = store.getAll();

      request.onsuccess = () => resolve(request.result || []);
      request.onerror = () => reject(request.error);
    });
  }

  /**
   * Clear local storage
   */
  async clearLocal(storeName: string): Promise<void> {
    if (!this.db) await this.init();

    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction([storeName], 'readwrite');
      const store = transaction.objectStore(storeName);
      const request = store.clear();

      request.onsuccess = () => resolve();
      request.onerror = () => reject(request.error);
    });
  }

  /**
   * Register sync listener
   */
  onSyncStatusChange(listener: (isSyncing: boolean) => void): () => void {
    this.syncListeners.push(listener);
    return () => {
      this.syncListeners = this.syncListeners.filter((l) => l !== listener);
    };
  }

  /**
   * Notify sync listeners
   */
  private notifySyncListeners(isSyncing: boolean): void {
    this.syncListeners.forEach((listener) => listener(isSyncing));
  }

  /**
   * Setup auto-sync on network reconnect
   */
  setupAutoSync(): void {
    window.addEventListener('online', () => {
      console.log('Network reconnected, starting sync...');
      this.syncQueue();
    });

    // Periodic sync every 5 minutes if online
    setInterval(() => {
      if (navigator.onLine && !this.syncInProgress) {
        this.syncQueue();
      }
    }, 5 * 60 * 1000);
  }
}

// Export singleton instance
const offlineSyncService = new OfflineSyncService();
export default offlineSyncService;
