import { describe, it, expect, vi, beforeEach } from 'vitest';
import popService from '../popService';
import apiClient from '../../utils/apiClient';

/**
 * POP Service Tests
 * Mock API tests for POP service methods
 * @author Moon Myung-seop
 */

// Mock apiClient
vi.mock('../../utils/apiClient', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}));

describe('popService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('getActiveWorkOrders', () => {
    it('should fetch active work orders', async () => {
      const mockWorkOrders = [
        {
          workOrderId: 1,
          workOrderNo: 'WO-001',
          productCode: 'PROD-001',
          productName: 'Test Product',
          plannedQuantity: 100,
          status: 'READY',
        },
      ];

      vi.mocked(apiClient.get).mockResolvedValue({ data: mockWorkOrders });

      const result = await popService.getActiveWorkOrders();

      expect(apiClient.get).toHaveBeenCalledWith('/pop/work-orders/active', {});
      expect(result).toEqual(mockWorkOrders);
    });

    it('should fetch active work orders for specific operator', async () => {
      const operatorId = 123;
      vi.mocked(apiClient.get).mockResolvedValue({ data: [] });

      await popService.getActiveWorkOrders(operatorId);

      expect(apiClient.get).toHaveBeenCalledWith('/pop/work-orders/active', {
        operatorId,
      });
    });

    it('should handle errors', async () => {
      const error = new Error('Network error');
      vi.mocked(apiClient.get).mockRejectedValue(error);

      await expect(popService.getActiveWorkOrders()).rejects.toThrow('Network error');
    });
  });

  describe('startWorkOrder', () => {
    it('should start a work order', async () => {
      const mockProgress = {
        progressId: 1,
        workOrderNo: 'WO-001',
        status: 'IN_PROGRESS',
        startTime: '09:00:00',
      };

      vi.mocked(apiClient.post).mockResolvedValue({ data: mockProgress });

      const result = await popService.startWorkOrder(1, 123);

      expect(apiClient.post).toHaveBeenCalledWith(
        '/pop/work-orders/1/start',
        null,
        { operatorId: 123 }
      );
      expect(result).toEqual(mockProgress);
    });

    it('should handle start errors', async () => {
      vi.mocked(apiClient.post).mockRejectedValue(new Error('Already started'));

      await expect(popService.startWorkOrder(1, 123)).rejects.toThrow('Already started');
    });
  });

  describe('recordProgress', () => {
    it('should record production progress', async () => {
      const mockProgress = {
        progressId: 1,
        producedQuantity: 50,
        goodQuantity: 48,
        defectQuantity: 2,
      };

      vi.mocked(apiClient.post).mockResolvedValue({ data: mockProgress });

      const result = await popService.recordProgress(1, 50);

      expect(apiClient.post).toHaveBeenCalledWith('/pop/work-progress/record', {
        progressId: 1,
        producedQuantity: 50,
        goodQuantity: 50,
      });
      expect(result).toEqual(mockProgress);
    });

    it('should record progress with good and defect quantities', async () => {
      const mockProgress = {
        progressId: 1,
        producedQuantity: 100,
        goodQuantity: 95,
        defectQuantity: 5,
      };

      vi.mocked(apiClient.post).mockResolvedValue({ data: mockProgress });

      const result = await popService.recordProgress(1, 100, 95);

      expect(apiClient.post).toHaveBeenCalledWith('/pop/work-progress/record', {
        progressId: 1,
        producedQuantity: 100,
        goodQuantity: 95,
      });
      expect(result).toEqual(mockProgress);
    });
  });

  describe('recordDefect', () => {
    it('should record a defect', async () => {
      const defectData = {
        defectQuantity: 5,
        defectType: '외관 불량',
        defectReason: '스크래치',
        severity: 'MINOR' as const,
      };

      const mockDefect = {
        defectId: 1,
        ...defectData,
        status: 'REPORTED',
      };

      vi.mocked(apiClient.post).mockResolvedValue({ data: mockDefect });

      const result = await popService.recordDefect(1, defectData);

      expect(apiClient.post).toHaveBeenCalledWith('/pop/work-progress/defect', {
        progressId: 1,
        ...defectData,
      });
      expect(result).toEqual(mockDefect);
    });

    it('should record defect with optional fields', async () => {
      const defectData = {
        defectQuantity: 3,
        defectType: '치수 불량',
        defectLocation: '상단',
        severity: 'MAJOR' as const,
        notes: '재작업 필요',
      };

      vi.mocked(apiClient.post).mockResolvedValue({ data: {} });

      await popService.recordDefect(1, defectData);

      expect(apiClient.post).toHaveBeenCalledWith('/pop/work-progress/defect', {
        progressId: 1,
        ...defectData,
      });
    });
  });

  describe('pauseWork', () => {
    it('should pause work', async () => {
      const mockProgress = {
        progressId: 1,
        status: 'PAUSED',
        pauseCount: 1,
      };

      vi.mocked(apiClient.post).mockResolvedValue({ data: mockProgress });

      const result = await popService.pauseWork(1, '휴식');

      expect(apiClient.post).toHaveBeenCalledWith('/pop/work-orders/1/pause', {
        pauseReason: '휴식',
      });
      expect(result).toEqual(mockProgress);
    });

    it('should pause without reason', async () => {
      vi.mocked(apiClient.post).mockResolvedValue({ data: {} });

      await popService.pauseWork(1);

      expect(apiClient.post).toHaveBeenCalledWith('/pop/work-orders/1/pause', {
        pauseReason: undefined,
      });
    });
  });

  describe('resumeWork', () => {
    it('should resume work', async () => {
      const mockProgress = {
        progressId: 1,
        status: 'IN_PROGRESS',
        totalPauseDuration: 15,
      };

      vi.mocked(apiClient.post).mockResolvedValue({ data: mockProgress });

      const result = await popService.resumeWork(1);

      expect(apiClient.post).toHaveBeenCalledWith('/pop/work-orders/1/resume', null);
      expect(result).toEqual(mockProgress);
    });
  });

  describe('completeWorkOrder', () => {
    it('should complete work order', async () => {
      const mockWorkOrder = {
        workOrderId: 1,
        status: 'COMPLETED',
        actualQuantity: 100,
        goodQuantity: 95,
        defectQuantity: 5,
      };

      vi.mocked(apiClient.post).mockResolvedValue({ data: mockWorkOrder });

      const result = await popService.completeWorkOrder(1, '정상 완료');

      expect(apiClient.post).toHaveBeenCalledWith('/pop/work-orders/1/complete', {
        remarks: '정상 완료',
      });
      expect(result).toEqual(mockWorkOrder);
    });

    it('should complete without remarks', async () => {
      vi.mocked(apiClient.post).mockResolvedValue({ data: {} });

      await popService.completeWorkOrder(1);

      expect(apiClient.post).toHaveBeenCalledWith('/pop/work-orders/1/complete', {
        remarks: undefined,
      });
    });
  });

  describe('getWorkProgress', () => {
    it('should get work progress', async () => {
      const mockProgress = {
        progressId: 1,
        workOrderNo: 'WO-001',
        producedQuantity: 50,
        goodQuantity: 48,
        defectQuantity: 2,
        status: 'IN_PROGRESS',
      };

      vi.mocked(apiClient.get).mockResolvedValue({ data: mockProgress });

      const result = await popService.getWorkProgress(1);

      expect(apiClient.get).toHaveBeenCalledWith('/pop/work-orders/1/progress');
      expect(result).toEqual(mockProgress);
    });
  });

  describe('getTodayStatistics', () => {
    it('should get today statistics for all operators', async () => {
      const mockStats = {
        totalProduced: 500,
        totalGood: 475,
        totalDefect: 25,
        defectRate: 5.0,
        activeWorkOrders: 3,
        completedWorkOrders: 2,
      };

      vi.mocked(apiClient.get).mockResolvedValue({ data: mockStats });

      const result = await popService.getTodayStatistics();

      expect(apiClient.get).toHaveBeenCalledWith('/pop/statistics/today', {});
      expect(result).toEqual(mockStats);
    });

    it('should get today statistics for specific operator', async () => {
      const operatorId = 123;
      vi.mocked(apiClient.get).mockResolvedValue({ data: {} });

      await popService.getTodayStatistics(operatorId);

      expect(apiClient.get).toHaveBeenCalledWith('/pop/statistics/today', {
        operatorId,
      });
    });
  });

  describe('scanBarcode', () => {
    it('should scan work order barcode', async () => {
      const mockWorkOrder = {
        workOrderId: 1,
        workOrderNo: 'WO-001',
        type: 'WORK_ORDER',
      };

      vi.mocked(apiClient.post).mockResolvedValue({ data: mockWorkOrder });

      const result = await popService.scanBarcode('WO-001', 'WORK_ORDER');

      expect(apiClient.post).toHaveBeenCalledWith('/pop/scan', {
        barcode: 'WO-001',
        barcodeType: 'WORK_ORDER',
      });
      expect(result).toEqual(mockWorkOrder);
    });

    it('should scan material barcode', async () => {
      const mockMaterial = {
        materialId: 1,
        materialCode: 'MAT-001',
        type: 'MATERIAL',
      };

      vi.mocked(apiClient.post).mockResolvedValue({ data: mockMaterial });

      const result = await popService.scanBarcode('MAT-001', 'MATERIAL');

      expect(apiClient.post).toHaveBeenCalledWith('/pop/scan', {
        barcode: 'MAT-001',
        barcodeType: 'MATERIAL',
      });
      expect(result).toEqual(mockMaterial);
    });

    it('should handle invalid barcode', async () => {
      vi.mocked(apiClient.post).mockRejectedValue(new Error('Invalid barcode'));

      await expect(popService.scanBarcode('INVALID', 'WORK_ORDER')).rejects.toThrow(
        'Invalid barcode'
      );
    });
  });

  describe('error handling', () => {
    it('should propagate API errors', async () => {
      const apiError = {
        response: {
          status: 404,
          data: {
            message: 'Work order not found',
          },
        },
      };

      vi.mocked(apiClient.get).mockRejectedValue(apiError);

      await expect(popService.getWorkProgress(999)).rejects.toEqual(apiError);
    });

    it('should handle network errors', async () => {
      const networkError = new Error('Network connection failed');
      vi.mocked(apiClient.post).mockRejectedValue(networkError);

      await expect(popService.startWorkOrder(1, 123)).rejects.toThrow(
        'Network connection failed'
      );
    });
  });
});
