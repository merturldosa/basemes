/**
 * InfiniteScrollList Component
 * A reusable list component with infinite scroll functionality
 * Perfect for mobile views and card-based layouts
 * @author Moon Myung-seop
 */

import { ReactNode } from 'react';
import {
  Box,
  CircularProgress,
  Typography,
  Button,
  Paper,
  Alert,
  Skeleton,
  Stack,
} from '@mui/material';
import { Refresh as RefreshIcon, ArrowUpward as ArrowUpwardIcon } from '@mui/icons-material';
import { useInfiniteScroll, UseInfiniteScrollOptions } from '@/hooks/useInfiniteScroll';

export interface InfiniteScrollListProps<T> {
  /**
   * Function to load more data
   */
  loadMore: UseInfiniteScrollOptions<T>['loadMore'];

  /**
   * Render function for each item
   */
  renderItem: (item: T, index: number) => ReactNode;

  /**
   * Get unique key for each item
   */
  getItemKey: (item: T, index: number) => string | number;

  /**
   * Page size (default: 20)
   */
  pageSize?: number;

  /**
   * Threshold for triggering load (default: 0.8)
   */
  threshold?: number;

  /**
   * Whether infinite scroll is enabled (default: true)
   */
  enabled?: boolean;

  /**
   * Empty state message
   */
  emptyMessage?: string;

  /**
   * Loading skeleton count (default: 3)
   */
  skeletonCount?: number;

  /**
   * Show "scroll to top" button (default: true)
   */
  showScrollToTop?: boolean;

  /**
   * Container height (default: 'auto')
   */
  height?: string | number;

  /**
   * Spacing between items (default: 2)
   */
  spacing?: number;

  /**
   * Callback when error occurs
   */
  onError?: (error: Error) => void;

  /**
   * Custom loading component
   */
  loadingComponent?: ReactNode;

  /**
   * Custom end message
   */
  endMessage?: ReactNode;
}

/**
 * InfiniteScrollList Component
 *
 * A flexible list component with infinite scroll support.
 * Perfect for mobile views, card layouts, and any list-based UI.
 *
 * @example
 * ```tsx
 * <InfiniteScrollList
 *   loadMore={async (page) => {
 *     const response = await api.getUsers({ page, size: 20 });
 *     return response.content;
 *   }}
 *   renderItem={(user) => (
 *     <Card>
 *       <CardContent>
 *         <Typography>{user.fullName}</Typography>
 *       </CardContent>
 *     </Card>
 *   )}
 *   getItemKey={(user) => user.userId}
 *   emptyMessage="사용자가 없습니다"
 * />
 * ```
 */
export default function InfiniteScrollList<T>({
  loadMore,
  renderItem,
  getItemKey,
  pageSize = 20,
  threshold = 0.8,
  enabled = true,
  emptyMessage = '데이터가 없습니다',
  skeletonCount = 3,
  showScrollToTop = true,
  height = 'auto',
  spacing = 2,
  onError,
  loadingComponent,
  endMessage,
}: InfiniteScrollListProps<T>) {
  const {
    items,
    isLoading,
    hasMore,
    error,
    sentinelRef,
    reset,
  } = useInfiniteScroll<T>({
    loadMore,
    pageSize,
    threshold,
    enabled,
    onError,
  });

  /**
   * Scroll to top
   */
  const scrollToTop = () => {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  /**
   * Retry loading
   */
  const handleRetry = () => {
    reset();
  };

  // Show skeleton loading on initial load
  const showInitialLoading = isLoading && items.length === 0;

  // Show empty state
  const showEmpty = !isLoading && items.length === 0 && !error;

  return (
    <Box
      sx={{
        height,
        overflow: height !== 'auto' ? 'auto' : 'visible',
        position: 'relative',
      }}
    >
      {/* Items List */}
      {items.length > 0 && (
        <Stack spacing={spacing}>
          {items.map((item, index) => (
            <Box key={getItemKey(item, index)}>{renderItem(item, index)}</Box>
          ))}
        </Stack>
      )}

      {/* Initial Loading Skeletons */}
      {showInitialLoading && (
        <Stack spacing={spacing}>
          {Array.from({ length: skeletonCount }).map((_, index) => (
            <Paper key={index} sx={{ p: 2 }}>
              <Skeleton variant="text" width="60%" height={24} />
              <Skeleton variant="text" width="100%" height={20} />
              <Skeleton variant="text" width="80%" height={20} />
            </Paper>
          ))}
        </Stack>
      )}

      {/* Loading More Indicator */}
      {isLoading && items.length > 0 && (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 3 }}>
          {loadingComponent || (
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
              <CircularProgress size={24} />
              <Typography variant="body2" color="text.secondary">
                데이터를 불러오는 중...
              </Typography>
            </Box>
          )}
        </Box>
      )}

      {/* Sentinel Element (Intersection Observer Target) */}
      {hasMore && !isLoading && (
        <Box
          ref={sentinelRef}
          sx={{
            height: '20px',
            visibility: 'hidden',
          }}
        />
      )}

      {/* End of List Message */}
      {!hasMore && items.length > 0 && !error && (
        <Box sx={{ py: 3, textAlign: 'center' }}>
          {endMessage || (
            <Paper sx={{ p: 2, bgcolor: 'action.hover' }}>
              <Typography variant="body2" color="text.secondary">
                모든 데이터를 불러왔습니다 (총 {items.length}개)
              </Typography>
            </Paper>
          )}
        </Box>
      )}

      {/* Empty State */}
      {showEmpty && (
        <Paper
          sx={{
            p: 4,
            textAlign: 'center',
            bgcolor: 'action.hover',
          }}
        >
          <Typography variant="body1" color="text.secondary">
            {emptyMessage}
          </Typography>
        </Paper>
      )}

      {/* Error State */}
      {error && (
        <Box sx={{ py: 2 }}>
          <Alert
            severity="error"
            action={
              <Button
                color="inherit"
                size="small"
                startIcon={<RefreshIcon />}
                onClick={handleRetry}
              >
                다시 시도
              </Button>
            }
          >
            <Typography variant="body2">
              데이터를 불러오는 중 오류가 발생했습니다
            </Typography>
            <Typography variant="caption" color="text.secondary">
              {error.message}
            </Typography>
          </Alert>
        </Box>
      )}

      {/* Scroll to Top Button */}
      {showScrollToTop && items.length > 10 && (
        <Box
          sx={{
            position: 'fixed',
            bottom: 24,
            right: 24,
            zIndex: 1000,
          }}
        >
          <Button
            variant="contained"
            color="primary"
            onClick={scrollToTop}
            startIcon={<ArrowUpwardIcon />}
            sx={{
              borderRadius: '50px',
              boxShadow: 3,
              '&:hover': {
                boxShadow: 6,
              },
            }}
          >
            맨 위로
          </Button>
        </Box>
      )}
    </Box>
  );
}
