/**
 * Error utility functions
 * Provides type-safe error message extraction from unknown error types
 * @author Moon Myung-seop
 */

import { AxiosError } from 'axios';
import { ErrorResponse } from '@/types';

/**
 * Extract error message from an unknown error.
 * Handles Axios errors with response data, standard Error objects, and unknown types.
 * @param error - The caught error (unknown type from catch block)
 * @param fallback - Default message if error cannot be parsed
 * @returns The extracted error message string
 */
export function getErrorMessage(error: unknown, fallback = 'An error occurred'): string {
  if (error instanceof AxiosError) {
    const responseMessage = (error.response?.data as ErrorResponse)?.message;
    if (responseMessage) return responseMessage;
    return error.message || fallback;
  }

  if (error instanceof Error) {
    return error.message || fallback;
  }

  if (typeof error === 'string') {
    return error;
  }

  return fallback;
}
