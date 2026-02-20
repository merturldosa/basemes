import React, { useEffect, useRef, useState } from 'react';
import {
  Box,
  Button,
  IconButton,
  Paper,
  Typography,
  Alert,
  CircularProgress,
  Snackbar
} from '@mui/material';
import {
  CameraAlt as CameraIcon,
  FlipCameraAndroid as FlipCameraIcon,
  Close as CloseIcon,
  FlashlightOn as FlashOnIcon,
  FlashlightOff as FlashOffIcon
} from '@mui/icons-material';
import { BrowserMultiFormatReader, NotFoundException } from '@zxing/library';

interface QRScannerProps {
  onScan: (data: string) => void;
  onClose?: () => void;
  continuous?: boolean;
}

/**
 * QR 코드 스캐너 컴포넌트
 * 웹 카메라를 사용한 QR 코드 스캔
 */
const QRScanner: React.FC<QRScannerProps> = ({
  onScan,
  onClose,
  continuous = false
}) => {
  const videoRef = useRef<HTMLVideoElement>(null);
  const [isScanning, setIsScanning] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [devices, setDevices] = useState<MediaDeviceInfo[]>([]);
  const [currentDeviceIndex, setCurrentDeviceIndex] = useState(0);
  const [torchEnabled, setTorchEnabled] = useState(false);
  const [lastScannedData, setLastScannedData] = useState<string>('');
  const [showSuccess, setShowSuccess] = useState(false);
  const codeReaderRef = useRef<BrowserMultiFormatReader | null>(null);
  const streamRef = useRef<MediaStream | null>(null);

  // 카메라 초기화
  useEffect(() => {
    initCamera();

    return () => {
      stopCamera();
    };
  }, [currentDeviceIndex]);

  // 카메라 장치 목록 가져오기
  const initCamera = async () => {
    try {
      setError(null);

      // 카메라 권한 요청
      const stream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: 'environment' }
      });

      // 권한 받은 후 스트림 중지 (장치 목록만 가져옴)
      stream.getTracks().forEach(track => track.stop());

      // 비디오 입력 장치 목록 가져오기
      const videoDevices = await navigator.mediaDevices.enumerateDevices();
      const cameras = videoDevices.filter(device => device.kind === 'videoinput');

      if (cameras.length === 0) {
        setError('카메라를 찾을 수 없습니다.');
        return;
      }

      setDevices(cameras);
      startScanning(cameras[currentDeviceIndex].deviceId);
    } catch (err: any) {
      if (err.name === 'NotAllowedError') {
        setError('카메라 권한이 거부되었습니다. 브라우저 설정에서 카메라 권한을 허용해주세요.');
      } else if (err.name === 'NotFoundError') {
        setError('카메라를 찾을 수 없습니다.');
      } else {
        setError('카메라 초기화 실패: ' + err.message);
      }
    }
  };

  // QR 스캔 시작
  const startScanning = async (deviceId: string) => {
    try {
      if (!videoRef.current) return;

      setIsScanning(true);

      // ZXing 코드 리더 생성
      const codeReader = new BrowserMultiFormatReader();
      codeReaderRef.current = codeReader;

      // 카메라 스트림 시작
      const constraints: MediaStreamConstraints = {
        video: {
          deviceId: deviceId ? { exact: deviceId } : undefined,
          facingMode: 'environment',
          width: { ideal: 1920 },
          height: { ideal: 1080 }
        }
      };

      const stream = await navigator.mediaDevices.getUserMedia(constraints);
      streamRef.current = stream;
      videoRef.current.srcObject = stream;

      // QR 코드 디코딩 시작
      codeReader.decodeFromVideoDevice(
        deviceId,
        videoRef.current,
        (result, error) => {
          if (result) {
            const scannedData = result.getText();

            // 중복 스캔 방지 (1초 내 같은 데이터)
            if (scannedData !== lastScannedData) {
              setLastScannedData(scannedData);
              setShowSuccess(true);

              // 진동 피드백 (모바일)
              if (navigator.vibrate) {
                navigator.vibrate(200);
              }

              // 콜백 호출
              onScan(scannedData);

              // 연속 스캔 모드가 아니면 중지
              if (!continuous) {
                stopCamera();
              }

              // 1초 후 중복 방지 해제
              setTimeout(() => {
                setLastScannedData('');
              }, 1000);
            }
          }

          // Non-NotFoundException decode errors are silently ignored
        }
      );
    } catch (err: any) {
      setError('스캔 시작 실패: ' + err.message);
      setIsScanning(false);
    }
  };

  // 카메라 중지
  const stopCamera = () => {
    if (codeReaderRef.current) {
      codeReaderRef.current.reset();
      codeReaderRef.current = null;
    }

    if (streamRef.current) {
      streamRef.current.getTracks().forEach(track => track.stop());
      streamRef.current = null;
    }

    if (videoRef.current) {
      videoRef.current.srcObject = null;
    }

    setIsScanning(false);
  };

  // 카메라 전환 (전면/후면)
  const switchCamera = () => {
    const nextIndex = (currentDeviceIndex + 1) % devices.length;
    setCurrentDeviceIndex(nextIndex);
  };

  // 플래시 토글
  const toggleTorch = async () => {
    if (!streamRef.current) return;

    const track = streamRef.current.getVideoTracks()[0];
    const capabilities = track.getCapabilities() as any;

    if (capabilities.torch) {
      try {
        await track.applyConstraints({
          advanced: [{ torch: !torchEnabled } as any]
        });
        setTorchEnabled(!torchEnabled);
      } catch (err) {
        // Torch toggle failed silently
      }
    }
  };

  // 성공 알림 닫기
  const handleCloseSuccess = () => {
    setShowSuccess(false);
  };

  return (
    <Box sx={{ position: 'relative', width: '100%', height: '100%' }}>
      {/* 비디오 뷰파인더 */}
      <Box
        sx={{
          position: 'relative',
          width: '100%',
          maxWidth: 600,
          margin: '0 auto',
          aspectRatio: '4 / 3',
          backgroundColor: '#000',
          borderRadius: 2,
          overflow: 'hidden'
        }}
      >
        <video
          ref={videoRef}
          autoPlay
          playsInline
          muted
          style={{
            width: '100%',
            height: '100%',
            objectFit: 'cover'
          }}
        />

        {/* 스캔 가이드 오버레이 */}
        {isScanning && (
          <Box
            sx={{
              position: 'absolute',
              top: '50%',
              left: '50%',
              transform: 'translate(-50%, -50%)',
              width: '70%',
              aspectRatio: '1',
              border: '3px solid #fff',
              borderRadius: 2,
              boxShadow: '0 0 0 9999px rgba(0, 0, 0, 0.5)',
              pointerEvents: 'none',
              '&::before, &::after': {
                content: '""',
                position: 'absolute',
                width: 30,
                height: 30,
                border: '4px solid #4caf50'
              },
              '&::before': {
                top: -4,
                left: -4,
                borderRight: 'none',
                borderBottom: 'none'
              },
              '&::after': {
                bottom: -4,
                right: -4,
                borderLeft: 'none',
                borderTop: 'none'
              }
            }}
          />
        )}

        {/* 로딩 표시 */}
        {!isScanning && !error && (
          <Box
            sx={{
              position: 'absolute',
              top: '50%',
              left: '50%',
              transform: 'translate(-50%, -50%)',
              textAlign: 'center'
            }}
          >
            <CircularProgress sx={{ color: '#fff', mb: 2 }} />
            <Typography variant="body2" sx={{ color: '#fff' }}>
              카메라 준비 중...
            </Typography>
          </Box>
        )}
      </Box>

      {/* 에러 메시지 */}
      {error && (
        <Alert severity="error" sx={{ mt: 2 }}>
          {error}
        </Alert>
      )}

      {/* 컨트롤 버튼 */}
      <Box
        sx={{
          display: 'flex',
          justifyContent: 'center',
          gap: 2,
          mt: 2
        }}
      >
        {/* 카메라 전환 */}
        {devices.length > 1 && (
          <IconButton
            onClick={switchCamera}
            sx={{
              backgroundColor: 'rgba(255, 255, 255, 0.9)',
              '&:hover': { backgroundColor: 'rgba(255, 255, 255, 1)' }
            }}
          >
            <FlipCameraIcon />
          </IconButton>
        )}

        {/* 플래시 */}
        <IconButton
          onClick={toggleTorch}
          sx={{
            backgroundColor: torchEnabled
              ? 'rgba(255, 235, 59, 0.9)'
              : 'rgba(255, 255, 255, 0.9)',
            '&:hover': {
              backgroundColor: torchEnabled
                ? 'rgba(255, 235, 59, 1)'
                : 'rgba(255, 255, 255, 1)'
            }
          }}
        >
          {torchEnabled ? <FlashOnIcon /> : <FlashOffIcon />}
        </IconButton>

        {/* 닫기 */}
        {onClose && (
          <IconButton
            onClick={() => {
              stopCamera();
              onClose();
            }}
            sx={{
              backgroundColor: 'rgba(244, 67, 54, 0.9)',
              color: '#fff',
              '&:hover': { backgroundColor: 'rgba(244, 67, 54, 1)' }
            }}
          >
            <CloseIcon />
          </IconButton>
        )}
      </Box>

      {/* 안내 문구 */}
      <Typography
        variant="body2"
        sx={{
          textAlign: 'center',
          color: 'text.secondary',
          mt: 2
        }}
      >
        QR 코드를 가이드 안에 맞춰주세요
      </Typography>

      {/* 성공 알림 */}
      <Snackbar
        open={showSuccess}
        autoHideDuration={2000}
        onClose={handleCloseSuccess}
        anchorOrigin={{ vertical: 'top', horizontal: 'center' }}
      >
        <Alert
          onClose={handleCloseSuccess}
          severity="success"
          sx={{ width: '100%' }}
        >
          QR 코드 스캔 완료!
        </Alert>
      </Snackbar>
    </Box>
  );
};

export default QRScanner;
