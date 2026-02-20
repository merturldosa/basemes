/**
 * Barcode Scanner Component
 * Camera-based barcode scanning UI
 * @author Moon Myung-seop
 */

import { Box, Button, Typography, Paper, Alert, CircularProgress, TextField } from '@mui/material';
import {
  QrCodeScanner as ScannerIcon,
  CameraAlt as CameraIcon,
  Stop as StopIcon,
} from '@mui/icons-material';
import { useBarcodeScanner, BarcodeScanResult } from '@/hooks/useBarcodeScanner';
import { useState } from 'react';

interface BarcodeScannerProps {
  onScan: (result: BarcodeScanResult) => void;
  title?: string;
  subtitle?: string;
  continuous?: boolean;
  showManualInput?: boolean;
}

const BarcodeScanner: React.FC<BarcodeScannerProps> = ({
  onScan,
  title = '바코드 스캔',
  subtitle = '카메라를 바코드에 맞춰주세요',
  continuous = false,
  showManualInput = true,
}) => {
  const [manualInput, setManualInput] = useState('');

  const {
    videoRef,
    isScanning,
    hasPermission,
    error,
    startScanning,
    stopScanning,
    triggerScan,
  } = useBarcodeScanner({
    onScan,
    continuous,
    onError: () => {},
  });

  const handleManualSubmit = () => {
    if (manualInput.trim()) {
      triggerScan(manualInput.trim());
      setManualInput('');
    }
  };

  return (
    <Box sx={{ maxWidth: 600, mx: 'auto' }}>
      {/* Header */}
      <Box sx={{ textAlign: 'center', mb: 3 }}>
        <ScannerIcon sx={{ fontSize: 48, color: 'primary.main', mb: 1 }} />
        <Typography variant="h5" gutterBottom fontWeight="bold">
          {title}
        </Typography>
        <Typography variant="body2" color="text.secondary">
          {subtitle}
        </Typography>
      </Box>

      {/* Camera View */}
      <Paper
        elevation={3}
        sx={{
          position: 'relative',
          width: '100%',
          height: 400,
          bgcolor: 'black',
          borderRadius: 2,
          overflow: 'hidden',
          mb: 3,
        }}
      >
        {!isScanning && hasPermission === null && (
          <Box
            sx={{
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              justifyContent: 'center',
              height: '100%',
              color: 'white',
            }}
          >
            <CameraIcon sx={{ fontSize: 64, mb: 2, opacity: 0.5 }} />
            <Typography variant="body1">카메라 준비 중...</Typography>
          </Box>
        )}

        {hasPermission === false && (
          <Box
            sx={{
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              justifyContent: 'center',
              height: '100%',
              color: 'white',
              p: 3,
            }}
          >
            <Alert severity="error" sx={{ mb: 2 }}>
              카메라 접근 권한이 필요합니다
            </Alert>
            <Typography variant="body2" align="center">
              브라우저 설정에서 카메라 권한을 허용해주세요
            </Typography>
          </Box>
        )}

        {error && (
          <Box
            sx={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              height: '100%',
              p: 3,
            }}
          >
            <Alert severity="error">{error}</Alert>
          </Box>
        )}

        <video
          ref={videoRef}
          style={{
            width: '100%',
            height: '100%',
            objectFit: 'cover',
            display: isScanning ? 'block' : 'none',
          }}
          playsInline
          muted
        />

        {/* Scanning Overlay */}
        {isScanning && (
          <Box
            sx={{
              position: 'absolute',
              top: 0,
              left: 0,
              right: 0,
              bottom: 0,
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              justifyContent: 'center',
              pointerEvents: 'none',
            }}
          >
            {/* Scanning Frame */}
            <Box
              sx={{
                width: '80%',
                maxWidth: 300,
                height: 200,
                border: '3px solid',
                borderColor: 'success.main',
                borderRadius: 2,
                position: 'relative',
                '&::before, &::after': {
                  content: '""',
                  position: 'absolute',
                  width: 20,
                  height: 20,
                  border: '4px solid',
                  borderColor: 'success.main',
                },
                '&::before': {
                  top: -4,
                  left: -4,
                  borderRight: 'none',
                  borderBottom: 'none',
                },
                '&::after': {
                  bottom: -4,
                  right: -4,
                  borderLeft: 'none',
                  borderTop: 'none',
                },
              }}
            />

            {/* Scanning Line Animation */}
            <Box
              sx={{
                position: 'absolute',
                width: '60%',
                maxWidth: 240,
                height: 2,
                bgcolor: 'success.main',
                animation: 'scan 2s ease-in-out infinite',
                '@keyframes scan': {
                  '0%, 100%': { transform: 'translateY(-100px)' },
                  '50%': { transform: 'translateY(100px)' },
                },
              }}
            />
          </Box>
        )}
      </Paper>

      {/* Controls */}
      <Box sx={{ display: 'flex', gap: 2, mb: 3 }}>
        {!isScanning ? (
          <Button
            variant="contained"
            size="large"
            fullWidth
            startIcon={<CameraIcon />}
            onClick={startScanning}
            sx={{ py: 2 }}
          >
            스캔 시작
          </Button>
        ) : (
          <Button
            variant="outlined"
            size="large"
            fullWidth
            color="error"
            startIcon={<StopIcon />}
            onClick={stopScanning}
            sx={{ py: 2 }}
          >
            스캔 중지
          </Button>
        )}
      </Box>

      {/* Manual Input */}
      {showManualInput && (
        <Paper sx={{ p: 3, bgcolor: 'grey.50' }}>
          <Typography variant="subtitle2" gutterBottom fontWeight="bold">
            수동 입력
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            바코드를 직접 입력하거나 붙여넣기할 수 있습니다
          </Typography>
          <Box sx={{ display: 'flex', gap: 1 }}>
            <TextField
              fullWidth
              size="small"
              placeholder="바코드 번호 입력"
              value={manualInput}
              onChange={(e) => setManualInput(e.target.value)}
              onKeyPress={(e) => {
                if (e.key === 'Enter') {
                  handleManualSubmit();
                }
              }}
            />
            <Button
              variant="contained"
              onClick={handleManualSubmit}
              disabled={!manualInput.trim()}
            >
              확인
            </Button>
          </Box>
        </Paper>
      )}

      {/* Instructions */}
      <Alert severity="info" sx={{ mt: 3 }}>
        <Typography variant="body2" fontWeight="bold" gutterBottom>
          📱 스캔 팁
        </Typography>
        <Typography variant="caption" component="div">
          • 바코드가 화면 중앙의 프레임 안에 들어오도록 맞춰주세요
          <br />
          • 조명이 충분한 곳에서 스캔하세요
          <br />
          • 바코드가 흔들리지 않도록 고정해주세요
          <br />• 자동으로 인식되지 않으면 수동 입력을 사용하세요
        </Typography>
      </Alert>
    </Box>
  );
};

export default BarcodeScanner;
