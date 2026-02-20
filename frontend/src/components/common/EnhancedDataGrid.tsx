/**
 * EnhancedDataGrid Component
 * DataGrid with enhanced pagination and auto-load next page feature
 * Provides a smoother, more infinite-scroll-like experience
 * @author Moon Myung-seop
 */

import { useState, useEffect, useCallback, useRef } from 'react';
import {
  Box,
  LinearProgress,
  Alert,
  Snackbar,
  Fade,
  Typography,
} from '@mui/material';
import {
  DataGrid,
  GridPaginationModel,
  DataGridProps,
} from '@mui/x-data-grid';

export interface EnhancedDataGridProps<T> extends Omit<DataGridProps, 'rows' | 'loading'> {
  /**
   * Function to load data for a given page
   */
  loadData: (page: number, pageSize: number) => Promise<{
    content: T[];
    totalElements: number;
  }>;

  /**
   * Initial page size (default: 25)
   */
  initialPageSize?: number;

  /**
   * Auto-load next page when user scrolls near bottom (default: true)
   */
  autoLoadNext?: boolean;

  /**
   * Scroll threshold for auto-loading (0-1, default: 0.9)
   */
  scrollThreshold?: number;

  /**
   * Show loading progress bar (default: true)
   */
  showProgress?: boolean;

  /**
   * Callback when error occurs
   */
  onError?: (error: Error) => void;

  /**
   * Callback when data is loaded
   */
  onDataLoaded?: (data: T[], totalElements: number) => void;
}

/**
 * EnhancedDataGrid Component
 *
 * An enhanced DataGrid with server-side pagination and auto-load next page.
 * Provides a smoother user experience similar to infinite scroll.
 *
 * @example
 * ```tsx
 * <EnhancedDataGrid
 *   loadData={async (page, pageSize) => {
 *     return await userService.getUsers({ page, size: pageSize });
 *   }}
 *   columns={columns}
 *   getRowId={(row) => row.userId}
 *   autoLoadNext={true}
 *   onError={(error) => console.error(error)}
 * />
 * ```
 */
export default function EnhancedDataGrid<T extends Record<string, any>>({
  loadData,
  columns,
  initialPageSize = 25,
  autoLoadNext = true,
  scrollThreshold = 0.9,
  showProgress = true,
  onError,
  onDataLoaded,
  getRowId,
  ...otherProps
}: EnhancedDataGridProps<T>) {
  const [rows, setRows] = useState<T[]>([]);
  const [loading, setLoading] = useState(false);
  const [totalElements, setTotalElements] = useState(0);
  const [paginationModel, setPaginationModel] = useState<GridPaginationModel>({
    page: 0,
    pageSize: initialPageSize,
  });
  const [error, setError] = useState<string | null>(null);
  const [showScrollHint, setShowScrollHint] = useState(false);

  const gridRef = useRef<HTMLDivElement>(null);
  const lastScrollTop = useRef(0);
  const autoLoadTriggered = useRef(false);

  /**
   * Load data for current page
   */
  const loadCurrentPage = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      autoLoadTriggered.current = false;

      const response = await loadData(paginationModel.page, paginationModel.pageSize);

      setRows(response.content);
      setTotalElements(response.totalElements);

      if (onDataLoaded) {
        onDataLoaded(response.content, response.totalElements);
      }

      // Show hint for auto-load feature on first load
      if (paginationModel.page === 0 && autoLoadNext && response.totalElements > paginationModel.pageSize) {
        setShowScrollHint(true);
        setTimeout(() => setShowScrollHint(false), 5000);
      }

    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : '데이터를 불러오는 중 오류가 발생했습니다';
      setError(errorMessage);

      if (onError && err instanceof Error) {
        onError(err);
      }
    } finally {
      setLoading(false);
    }
  }, [loadData, paginationModel, autoLoadNext, onDataLoaded, onError]);

  /**
   * Load data when pagination changes
   */
  useEffect(() => {
    loadCurrentPage();
  }, [loadCurrentPage]);

  /**
   * Handle scroll events for auto-load next page
   */
  useEffect(() => {
    if (!autoLoadNext || !gridRef.current) {
      return;
    }

    const gridElement = gridRef.current.querySelector('.MuiDataGrid-virtualScroller');
    if (!gridElement) {
      return;
    }

    const handleScroll = () => {
      const { scrollTop, scrollHeight, clientHeight } = gridElement;

      // Calculate scroll percentage
      const scrollPercentage = (scrollTop + clientHeight) / scrollHeight;

      // Check if scrolling down
      const isScrollingDown = scrollTop > lastScrollTop.current;
      lastScrollTop.current = scrollTop;

      // If scrolled past threshold and scrolling down
      if (
        isScrollingDown &&
        scrollPercentage >= scrollThreshold &&
        !loading &&
        !autoLoadTriggered.current
      ) {
        const hasNextPage = (paginationModel.page + 1) * paginationModel.pageSize < totalElements;

        if (hasNextPage) {
          autoLoadTriggered.current = true;
          setPaginationModel((prev) => ({
            ...prev,
            page: prev.page + 1,
          }));
        }
      }
    };

    gridElement.addEventListener('scroll', handleScroll);

    return () => {
      gridElement.removeEventListener('scroll', handleScroll);
    };
  }, [autoLoadNext, scrollThreshold, loading, paginationModel, totalElements]);

  /**
   * Handle page change from pagination controls
   */
  const handlePaginationModelChange = (newModel: GridPaginationModel) => {
    setPaginationModel(newModel);
  };

  return (
    <Box ref={gridRef} sx={{ height: '100%', width: '100%', position: 'relative' }}>
      {/* Loading Progress Bar */}
      {showProgress && loading && (
        <Box
          sx={{
            position: 'absolute',
            top: 0,
            left: 0,
            right: 0,
            zIndex: 1000,
          }}
        >
          <LinearProgress />
        </Box>
      )}

      {/* Auto-Scroll Hint */}
      <Fade in={showScrollHint}>
        <Box
          sx={{
            position: 'absolute',
            top: 16,
            right: 16,
            zIndex: 999,
            maxWidth: 300,
          }}
        >
          <Alert severity="info" onClose={() => setShowScrollHint(false)}>
            <Typography variant="caption">
              스크롤을 아래로 내리면 자동으로 다음 페이지가 로드됩니다
            </Typography>
          </Alert>
        </Box>
      </Fade>

      {/* DataGrid */}
      <DataGrid
        rows={rows}
        columns={columns}
        loading={loading}
        paginationModel={paginationModel}
        onPaginationModelChange={handlePaginationModelChange}
        pageSizeOptions={[10, 25, 50, 100]}
        rowCount={totalElements}
        paginationMode="server"
        getRowId={getRowId}
        disableRowSelectionOnClick
        sx={{
          border: 0,
          '& .MuiDataGrid-cell:focus': {
            outline: 'none',
          },
          '& .MuiDataGrid-row:hover': {
            backgroundColor: 'action.hover',
          },
          ...otherProps.sx,
        }}
        {...otherProps}
      />

      {/* Error Snackbar */}
      <Snackbar
        open={!!error}
        autoHideDuration={6000}
        onClose={() => setError(null)}
        anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
      >
        <Alert
          onClose={() => setError(null)}
          severity="error"
          sx={{ width: '100%' }}
        >
          {error}
        </Alert>
      </Snackbar>
    </Box>
  );
}
