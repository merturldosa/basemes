/**
 * Barcode Scanner Hook
 * Provides barcode scanning functionality using device camera
 * @author Moon Myung-seop
 */

import { useState, useEffect, useRef, useCallback } from 'react';

export interface BarcodeScanResult {
  data: string;
  format: string;
  timestamp: number;
}

export interface BarcodeScannerOptions {
  onScan?: (result: BarcodeScanResult) => void;
  onError?: (error: Error) => void;
  continuous?: boolean;
  delay?: number;
}

export const useBarcodeScanner = (options: BarcodeScannerOptions = {}) => {
  const {
    onScan,
    onError,
    continuous = false,
    delay = 500,
  } = options;

  const [isScanning, setIsScanning] = useState(false);
  const [hasPermission, setHasPermission] = useState<boolean | null>(null);
  const [error, setError] = useState<string | null>(null);
  const videoRef = useRef<HTMLVideoElement | null>(null);
  const streamRef = useRef<MediaStream | null>(null);
  const scanTimeoutRef = useRef<NodeJS.Timeout | null>(null);

  // Request camera permission and start video stream
  const startScanning = useCallback(async () => {
    try {
      setError(null);

      if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
        throw new Error('Camera API is not supported in this browser');
      }

      const stream = await navigator.mediaDevices.getUserMedia({
        video: {
          facingMode: 'environment',
          width: { ideal: 1280 },
          height: { ideal: 720 },
        },
      });

      streamRef.current = stream;

      if (videoRef.current) {
        videoRef.current.srcObject = stream;
        await videoRef.current.play();
      }

      setHasPermission(true);
      setIsScanning(true);

      if (continuous) {
        startContinuousScanning();
      }
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Failed to access camera';
      setError(errorMessage);
      setHasPermission(false);
      if (onError) {
        onError(err instanceof Error ? err : new Error(errorMessage));
      }
    }
  }, [continuous, onError]);

  const stopScanning = useCallback(() => {
    if (streamRef.current) {
      streamRef.current.getTracks().forEach((track) => track.stop());
      streamRef.current = null;
    }

    if (videoRef.current) {
      videoRef.current.srcObject = null;
    }

    if (scanTimeoutRef.current) {
      clearTimeout(scanTimeoutRef.current);
      scanTimeoutRef.current = null;
    }

    setIsScanning(false);
  }, []);

  const startContinuousScanning = useCallback(() => {
    const scan = () => {
      if (!isScanning || !videoRef.current) return;
      scanTimeoutRef.current = setTimeout(scan, delay);
    };

    scan();
  }, [isScanning, delay]);

  const triggerScan = useCallback((mockData: string) => {
    if (onScan) {
      const result: BarcodeScanResult = {
        data: mockData,
        format: 'QR_CODE',
        timestamp: Date.now(),
      };
      onScan(result);

      if (navigator.vibrate) {
        navigator.vibrate(200);
      }

      if (!continuous) {
        stopScanning();
      }
    }
  }, [onScan, continuous, stopScanning]);

  useEffect(() => {
    return () => {
      stopScanning();
    };
  }, [stopScanning]);

  return {
    videoRef,
    isScanning,
    hasPermission,
    error,
    startScanning,
    stopScanning,
    triggerScan,
  };
};

export default useBarcodeScanner;
