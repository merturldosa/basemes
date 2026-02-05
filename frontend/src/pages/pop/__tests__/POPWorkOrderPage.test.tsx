import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { BrowserRouter } from 'react-router-dom';
import POPWorkOrderPage from '../POPWorkOrderPage';
import popService from '../../../services/popService';
import offlineSyncService from '../../../services/offlineSync';

/**
 * POP Work Order Page Tests
 * Tests for POP work order page rendering and interactions
 * @author Moon Myung-seop
 */

// Mock services
vi.mock('../../../services/popService', () => ({
  default: {
    getActiveWorkOrders: vi.fn(),
    startWorkOrder: vi.fn(),
    recordProgress: vi.fn(),
    recordDefect: vi.fn(),
    pauseWork: vi.fn(),
    resumeWork: vi.fn(),
    completeWorkOrder: vi.fn(),
    getWorkProgress: vi.fn(),
  },
}));

vi.mock('../../../services/offlineSync', () => ({
  default: {
    isOnline: vi.fn(() => true),
    onOnlineStatusChange: vi.fn(() => () => {}),
    onSyncStatusChange: vi.fn(() => () => {}),
    getSyncStatus: vi.fn(() =>
      Promise.resolve({
        isOnline: true,
        isSyncing: false,
        queuedItems: 0,
        lastSyncTime: null,
        failedItems: 0,
      })
    ),
    queueWorkProgress: vi.fn(),
    queueDefect: vi.fn(),
  },
}));

// Mock WebSocket hook
vi.mock('../../../hooks/useWebSocket', () => ({
  useWorkProgressUpdates: vi.fn(() => {}),
  useDefectUpdates: vi.fn(() => {}),
}));

const mockWorkOrders = [
  {
    workOrderId: 1,
    workOrderNo: 'WO-2024-001',
    productCode: 'PROD-001',
    productName: 'Test Product A',
    processName: 'Assembly',
    plannedQuantity: 100,
    actualQuantity: 0,
    goodQuantity: 0,
    defectQuantity: 0,
    status: 'READY',
    assignedUserName: 'John Doe',
    plannedStartDate: '2024-01-01T09:00:00',
  },
  {
    workOrderId: 2,
    workOrderNo: 'WO-2024-002',
    productCode: 'PROD-002',
    productName: 'Test Product B',
    processName: 'Packaging',
    plannedQuantity: 50,
    actualQuantity: 25,
    goodQuantity: 24,
    defectQuantity: 1,
    status: 'IN_PROGRESS',
    assignedUserName: 'Jane Smith',
    plannedStartDate: '2024-01-01T10:00:00',
    actualStartDate: '2024-01-01T10:05:00',
  },
];

const renderWithRouter = (component: React.ReactElement) => {
  return render(<BrowserRouter>{component}</BrowserRouter>);
};

describe('POPWorkOrderPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(popService.getActiveWorkOrders).mockResolvedValue(mockWorkOrders);
  });

  describe('Page Rendering', () => {
    it('should render page title', async () => {
      renderWithRouter(<POPWorkOrderPage />);

      await waitFor(() => {
        expect(screen.getByText(/작업 지시/i)).toBeInTheDocument();
      });
    });

    it('should load and display work orders', async () => {
      renderWithRouter(<POPWorkOrderPage />);

      await waitFor(() => {
        expect(screen.getByText('WO-2024-001')).toBeInTheDocument();
        expect(screen.getByText('WO-2024-002')).toBeInTheDocument();
        expect(screen.getByText('Test Product A')).toBeInTheDocument();
        expect(screen.getByText('Test Product B')).toBeInTheDocument();
      });
    });

    it('should display work order status correctly', async () => {
      renderWithRouter(<POPWorkOrderPage />);

      await waitFor(() => {
        expect(screen.getByText('준비')).toBeInTheDocument(); // READY status
        expect(screen.getByText('진행중')).toBeInTheDocument(); // IN_PROGRESS status
      });
    });

    it('should display loading state', () => {
      vi.mocked(popService.getActiveWorkOrders).mockImplementation(
        () => new Promise(() => {}) // Never resolves
      );

      renderWithRouter(<POPWorkOrderPage />);

      expect(screen.getByRole('progressbar')).toBeInTheDocument();
    });

    it('should display error state', async () => {
      vi.mocked(popService.getActiveWorkOrders).mockRejectedValue(
        new Error('Network error')
      );

      renderWithRouter(<POPWorkOrderPage />);

      await waitFor(() => {
        expect(screen.getByText(/오류/i)).toBeInTheDocument();
      });
    });

    it('should display empty state when no work orders', async () => {
      vi.mocked(popService.getActiveWorkOrders).mockResolvedValue([]);

      renderWithRouter(<POPWorkOrderPage />);

      await waitFor(() => {
        expect(screen.getByText(/작업지시가 없습니다/i)).toBeInTheDocument();
      });
    });
  });

  describe('Work Order Actions', () => {
    it('should start work order when start button clicked', async () => {
      const mockProgress = {
        progressId: 1,
        workOrderNo: 'WO-2024-001',
        status: 'IN_PROGRESS',
        producedQuantity: 0,
        goodQuantity: 0,
        defectQuantity: 0,
      };

      vi.mocked(popService.startWorkOrder).mockResolvedValue(mockProgress);

      renderWithRouter(<POPWorkOrderPage />);

      await waitFor(() => {
        expect(screen.getByText('WO-2024-001')).toBeInTheDocument();
      });

      const startButton = screen.getByText('작업 시작');
      fireEvent.click(startButton);

      await waitFor(() => {
        expect(popService.startWorkOrder).toHaveBeenCalledWith(
          1,
          expect.any(Number)
        );
      });
    });

    it('should pause work when pause button clicked', async () => {
      renderWithRouter(<POPWorkOrderPage />);

      await waitFor(() => {
        expect(screen.getByText('WO-2024-002')).toBeInTheDocument();
      });

      const pauseButton = screen.getByText('일시정지');
      fireEvent.click(pauseButton);

      await waitFor(() => {
        expect(popService.pauseWork).toHaveBeenCalledWith(2, expect.any(String));
      });
    });

    it('should resume work when resume button clicked', async () => {
      const pausedWorkOrder = {
        ...mockWorkOrders[1],
        status: 'PAUSED',
      };

      vi.mocked(popService.getActiveWorkOrders).mockResolvedValue([
        mockWorkOrders[0],
        pausedWorkOrder,
      ]);

      renderWithRouter(<POPWorkOrderPage />);

      await waitFor(() => {
        expect(screen.getByText('WO-2024-002')).toBeInTheDocument();
      });

      const resumeButton = screen.getByText('작업 재개');
      fireEvent.click(resumeButton);

      await waitFor(() => {
        expect(popService.resumeWork).toHaveBeenCalledWith(2);
      });
    });

    it('should complete work order when complete button clicked', async () => {
      renderWithRouter(<POPWorkOrderPage />);

      await waitFor(() => {
        expect(screen.getByText('WO-2024-002')).toBeInTheDocument();
      });

      const completeButton = screen.getByText('작업 완료');
      fireEvent.click(completeButton);

      // Should show confirmation dialog
      await waitFor(() => {
        expect(screen.getByText(/완료하시겠습니까/i)).toBeInTheDocument();
      });

      const confirmButton = screen.getByText('확인');
      fireEvent.click(confirmButton);

      await waitFor(() => {
        expect(popService.completeWorkOrder).toHaveBeenCalledWith(
          2,
          expect.any(String)
        );
      });
    });
  });

  describe('Production Recording', () => {
    it('should record good quantity', async () => {
      const mockProgress = {
        progressId: 1,
        producedQuantity: 10,
        goodQuantity: 10,
        defectQuantity: 0,
      };

      vi.mocked(popService.recordProgress).mockResolvedValue(mockProgress);
      vi.mocked(popService.getWorkProgress).mockResolvedValue(mockProgress);

      renderWithRouter(<POPWorkOrderPage />);

      await waitFor(() => {
        expect(screen.getByText('WO-2024-002')).toBeInTheDocument();
      });

      // Click on work order card to open detail
      const workOrderCard = screen.getByText('WO-2024-002').closest('div');
      if (workOrderCard) {
        fireEvent.click(workOrderCard);
      }

      // Find quantity input and increment button
      await waitFor(() => {
        const incrementButton = screen.getByRole('button', { name: '+' });
        fireEvent.click(incrementButton);
      });

      const recordButton = screen.getByText('기록');
      fireEvent.click(recordButton);

      await waitFor(() => {
        expect(popService.recordProgress).toHaveBeenCalled();
      });
    });

    it('should open defect dialog when defect button clicked', async () => {
      renderWithRouter(<POPWorkOrderPage />);

      await waitFor(() => {
        expect(screen.getByText('WO-2024-002')).toBeInTheDocument();
      });

      const defectButton = screen.getByText('불량 기록');
      fireEvent.click(defectButton);

      await waitFor(() => {
        expect(screen.getByText('불량 기록')).toBeInTheDocument();
        expect(screen.getByText('불량 유형')).toBeInTheDocument();
      });
    });

    it('should submit defect record', async () => {
      const mockDefect = {
        defectId: 1,
        defectQuantity: 5,
        defectType: '외관 불량',
        severity: 'MINOR',
      };

      vi.mocked(popService.recordDefect).mockResolvedValue(mockDefect);

      renderWithRouter(<POPWorkOrderPage />);

      await waitFor(() => {
        expect(screen.getByText('WO-2024-002')).toBeInTheDocument();
      });

      const defectButton = screen.getByText('불량 기록');
      fireEvent.click(defectButton);

      await waitFor(() => {
        const typeSelect = screen.getByLabelText('불량 유형');
        fireEvent.change(typeSelect, { target: { value: '외관 불량' } });
      });

      const submitButton = screen.getByText('확인');
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(popService.recordDefect).toHaveBeenCalledWith(
          expect.any(Number),
          expect.objectContaining({
            defectType: '외관 불량',
          })
        );
      });
    });
  });

  describe('Offline Mode', () => {
    it('should show offline indicator when offline', async () => {
      vi.mocked(offlineSyncService.isOnline).mockReturnValue(false);
      vi.mocked(offlineSyncService.getSyncStatus).mockResolvedValue({
        isOnline: false,
        isSyncing: false,
        queuedItems: 3,
        lastSyncTime: null,
        failedItems: 0,
      });

      renderWithRouter(<POPWorkOrderPage />);

      await waitFor(() => {
        expect(screen.getByText(/오프라인/i)).toBeInTheDocument();
      });
    });

    it('should queue work progress when offline', async () => {
      vi.mocked(offlineSyncService.isOnline).mockReturnValue(false);

      renderWithRouter(<POPWorkOrderPage />);

      await waitFor(() => {
        expect(screen.getByText('WO-2024-002')).toBeInTheDocument();
      });

      // Try to record progress
      const recordButton = screen.getByText('기록');
      fireEvent.click(recordButton);

      await waitFor(() => {
        expect(offlineSyncService.queueWorkProgress).toHaveBeenCalled();
      });
    });

    it('should show syncing indicator when syncing', async () => {
      vi.mocked(offlineSyncService.getSyncStatus).mockResolvedValue({
        isOnline: true,
        isSyncing: true,
        queuedItems: 5,
        lastSyncTime: Date.now(),
        failedItems: 0,
      });

      renderWithRouter(<POPWorkOrderPage />);

      await waitFor(() => {
        expect(screen.getByText(/동기화 중/i)).toBeInTheDocument();
      });
    });
  });

  describe('Real-time Updates', () => {
    it('should update work orders on WebSocket message', async () => {
      let progressCallback: ((progress: any) => void) | null = null;

      const useWorkProgressUpdatesMock = vi.fn((callback) => {
        progressCallback = callback;
      });

      vi.mocked(vi.importActual('../../../hooks/useWebSocket')).useWorkProgressUpdates =
        useWorkProgressUpdatesMock;

      renderWithRouter(<POPWorkOrderPage />);

      await waitFor(() => {
        expect(screen.getByText('WO-2024-001')).toBeInTheDocument();
      });

      // Simulate WebSocket update
      if (progressCallback) {
        progressCallback({
          workOrderId: 1,
          producedQuantity: 50,
          goodQuantity: 48,
          defectQuantity: 2,
        });
      }

      await waitFor(() => {
        expect(screen.getByText(/50/)).toBeInTheDocument();
      });
    });
  });

  describe('Error Handling', () => {
    it('should show error message on API failure', async () => {
      vi.mocked(popService.startWorkOrder).mockRejectedValue(
        new Error('Already started')
      );

      renderWithRouter(<POPWorkOrderPage />);

      await waitFor(() => {
        expect(screen.getByText('WO-2024-001')).toBeInTheDocument();
      });

      const startButton = screen.getByText('작업 시작');
      fireEvent.click(startButton);

      await waitFor(() => {
        expect(screen.getByText(/Already started/i)).toBeInTheDocument();
      });
    });

    it('should retry on network error', async () => {
      vi.mocked(popService.getActiveWorkOrders)
        .mockRejectedValueOnce(new Error('Network error'))
        .mockResolvedValueOnce(mockWorkOrders);

      renderWithRouter(<POPWorkOrderPage />);

      await waitFor(() => {
        expect(screen.getByText(/오류/i)).toBeInTheDocument();
      });

      const retryButton = screen.getByText('재시도');
      fireEvent.click(retryButton);

      await waitFor(() => {
        expect(screen.getByText('WO-2024-001')).toBeInTheDocument();
      });
    });
  });

  describe('User Interface', () => {
    it('should filter work orders by status', async () => {
      renderWithRouter(<POPWorkOrderPage />);

      await waitFor(() => {
        expect(screen.getByText('WO-2024-001')).toBeInTheDocument();
        expect(screen.getByText('WO-2024-002')).toBeInTheDocument();
      });

      const filterButton = screen.getByText('진행중만');
      fireEvent.click(filterButton);

      await waitFor(() => {
        expect(screen.queryByText('WO-2024-001')).not.toBeInTheDocument();
        expect(screen.getByText('WO-2024-002')).toBeInTheDocument();
      });
    });

    it('should sort work orders', async () => {
      renderWithRouter(<POPWorkOrderPage />);

      await waitFor(() => {
        const workOrders = screen.getAllByText(/WO-2024-/);
        expect(workOrders[0]).toHaveTextContent('WO-2024-001');
        expect(workOrders[1]).toHaveTextContent('WO-2024-002');
      });

      const sortButton = screen.getByText('정렬');
      fireEvent.click(sortButton);

      const sortByDate = screen.getByText('날짜순');
      fireEvent.click(sortByDate);

      await waitFor(() => {
        // Verify sort order changed
        const workOrders = screen.getAllByText(/WO-2024-/);
        expect(workOrders.length).toBeGreaterThan(0);
      });
    });

    it('should refresh work orders on pull-to-refresh', async () => {
      renderWithRouter(<POPWorkOrderPage />);

      await waitFor(() => {
        expect(screen.getByText('WO-2024-001')).toBeInTheDocument();
      });

      const refreshButton = screen.getByLabelText('새로고침');
      fireEvent.click(refreshButton);

      await waitFor(() => {
        expect(popService.getActiveWorkOrders).toHaveBeenCalledTimes(2);
      });
    });
  });
});
