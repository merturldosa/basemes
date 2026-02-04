/**
 * useInfiniteScroll Hook
 * Provides infinite scroll functionality using Intersection Observer
 * @author Moon Myung-seop
 */

import { useEffect, useRef, useCallback, useState } from 'react';

export interface UseInfiniteScrollOptions<T> {
  /**
   * Function to load more data
   * Should return the next page of data
   */
  loadMore: (page: number) => Promise<T[]>;

  /**
   * Initial page number (default: 0)
   */
  initialPage?: number;

  /**
   * Page size (default: 20)
   */
  pageSize?: number;

  /**
   * Threshold for triggering load (0-1, default: 0.8)
   * 0.8 means trigger when 80% scrolled
   */
  threshold?: number;

  /**
   * Whether infinite scroll is enabled (default: true)
   */
  enabled?: boolean;

  /**
   * Callback when error occurs
   */
  onError?: (error: Error) => void;
}

export interface UseInfiniteScrollResult<T> {
  /**
   * All loaded data items
   */
  items: T[];

  /**
   * Whether currently loading more data
   */
  isLoading: boolean;

  /**
   * Whether there's more data to load
   */
  hasMore: boolean;

  /**
   * Error if any occurred
   */
  error: Error | null;

  /**
   * Current page number
   */
  currentPage: number;

  /**
   * Ref to attach to the sentinel element (load trigger)
   */
  sentinelRef: React.RefObject<HTMLDivElement>;

  /**
   * Manually trigger load more
   */
  loadMore: () => Promise<void>;

  /**
   * Reset to initial state
   */
  reset: () => void;

  /**
   * Set items manually (useful for search/filter)
   */
  setItems: (items: T[]) => void;
}

/**
 * Custom hook for infinite scroll functionality
 *
 * @example
 * ```tsx
 * const { items, isLoading, hasMore, sentinelRef } = useInfiniteScroll({
 *   loadMore: async (page) => {
 *     const response = await api.getUsers({ page, size: 20 });
 *     return response.content;
 *   },
 *   pageSize: 20,
 *   threshold: 0.8,
 * });
 *
 * return (
 *   <>
 *     {items.map(item => <ItemCard key={item.id} data={item} />)}
 *     <div ref={sentinelRef} />
 *     {isLoading && <LoadingSpinner />}
 *   </>
 * );
 * ```
 */
export function useInfiniteScroll<T>({
  loadMore: loadMoreFn,
  initialPage = 0,
  pageSize = 20,
  threshold = 0.8,
  enabled = true,
  onError,
}: UseInfiniteScrollOptions<T>): UseInfiniteScrollResult<T> {
  const [items, setItems] = useState<T[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [hasMore, setHasMore] = useState(true);
  const [error, setError] = useState<Error | null>(null);
  const [currentPage, setCurrentPage] = useState(initialPage);

  const sentinelRef = useRef<HTMLDivElement>(null);
  const observerRef = useRef<IntersectionObserver | null>(null);
  const isLoadingRef = useRef(false); // Prevent duplicate loads

  /**
   * Load more data
   */
  const loadMore = useCallback(async () => {
    // Prevent duplicate loads
    if (isLoadingRef.current || !hasMore || !enabled) {
      return;
    }

    try {
      isLoadingRef.current = true;
      setIsLoading(true);
      setError(null);

      const newItems = await loadMoreFn(currentPage);

      // If we got less items than pageSize, we've reached the end
      const noMoreData = newItems.length < pageSize;

      setItems((prev) => [...prev, ...newItems]);
      setCurrentPage((prev) => prev + 1);
      setHasMore(!noMoreData);

    } catch (err) {
      const error = err instanceof Error ? err : new Error('Failed to load more data');
      setError(error);
      setHasMore(false);

      if (onError) {
        onError(error);
      }
    } finally {
      setIsLoading(false);
      isLoadingRef.current = false;
    }
  }, [currentPage, hasMore, enabled, loadMoreFn, pageSize, onError]);

  /**
   * Reset to initial state
   */
  const reset = useCallback(() => {
    setItems([]);
    setCurrentPage(initialPage);
    setHasMore(true);
    setError(null);
    setIsLoading(false);
    isLoadingRef.current = false;
  }, [initialPage]);

  /**
   * Set items manually (useful for search/filter)
   */
  const setItemsManually = useCallback((newItems: T[]) => {
    setItems(newItems);
    setCurrentPage(initialPage + 1);
    setHasMore(newItems.length >= pageSize);
  }, [initialPage, pageSize]);

  /**
   * Setup Intersection Observer
   */
  useEffect(() => {
    if (!enabled || !sentinelRef.current) {
      return;
    }

    // Create observer
    observerRef.current = new IntersectionObserver(
      (entries) => {
        const target = entries[0];

        // When sentinel becomes visible and we have more data, load more
        if (target.isIntersecting && hasMore && !isLoadingRef.current) {
          loadMore();
        }
      },
      {
        root: null, // viewport
        rootMargin: '0px',
        threshold: threshold,
      }
    );

    // Start observing
    observerRef.current.observe(sentinelRef.current);

    // Cleanup
    return () => {
      if (observerRef.current) {
        observerRef.current.disconnect();
      }
    };
  }, [enabled, hasMore, loadMore, threshold]);

  /**
   * Initial load
   */
  useEffect(() => {
    if (enabled && items.length === 0 && !isLoadingRef.current) {
      loadMore();
    }
  }, [enabled]); // Only run on mount or when enabled changes

  return {
    items,
    isLoading,
    hasMore,
    error,
    currentPage,
    sentinelRef,
    loadMore,
    reset,
    setItems: setItemsManually,
  };
}
