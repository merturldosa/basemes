/**
 * POP Scanner Page
 * Barcode scanning for work orders, LOTs, products, and locations
 * @author Moon Myung-seop
 */

import { useState } from 'react';
import {
  Box,
  Typography,
  Tabs,
  Tab,
  Card,
  CardContent,
  Chip,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  Divider,
  Snackbar,
  Alert,
} from '@mui/material';
import {
  Assignment as WorkOrderIcon,
  Inventory2 as LotIcon,
  Category as ProductIcon,
  LocationOn as LocationIcon,
  CheckCircle as CheckIcon,
} from '@mui/icons-material';
import BarcodeScanner from '@/components/pop/BarcodeScanner';
import { BarcodeScanResult } from '@/hooks/useBarcodeScanner';
import popService, { ScanBarcodeRequest } from '@/services/popService';

interface ScanHistory {
  id: string;
  type: string;
  data: string;
  timestamp: number;
  status: 'success' | 'error';
  message?: string;
}

const POPScannerPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState(0);
  const [scanHistory, setScanHistory] = useState<ScanHistory[]>([]);
  const [snackbar, setSnackbar] = useState<{
    open: boolean;
    message: string;
    severity: 'success' | 'error';
  }>({ open: false, message: '', severity: 'success' });

  const scanTypes = [
    { label: '작업 지시', value: 'work-order', icon: <WorkOrderIcon /> },
    { label: 'LOT', value: 'lot', icon: <LotIcon /> },
    { label: '제품', value: 'product', icon: <ProductIcon /> },
    { label: '위치', value: 'location', icon: <LocationIcon /> },
  ];

  const scanTypeToApiType: Record<string, ScanBarcodeRequest['type']> = {
    'work-order': 'WORK_ORDER',
    'lot': 'LOT',
    'product': 'PRODUCT',
    'location': 'PRODUCT',
  };

  const currentScanType = scanTypes[activeTab];

  const handleScan = async (result: BarcodeScanResult) => {
    const scanId = `${Date.now()}-${Math.random()}`;
    const apiType = scanTypeToApiType[currentScanType.value] || 'PRODUCT';

    // Add initial scan entry to history (pending state shown as success until API responds)
    const newScan: ScanHistory = {
      id: scanId,
      type: currentScanType.value,
      data: result.data,
      timestamp: result.timestamp,
      status: 'success',
      message: `${currentScanType.label} 스캔 처리 중...`,
    };
    setScanHistory((prev) => [newScan, ...prev.slice(0, 9)]);

    try {
      const apiResponse = await popService.scanBarcode({
        barcode: result.data,
        type: apiType,
      });

      // Update scan history entry with API response details
      setScanHistory((prev) =>
        prev.map((scan) =>
          scan.id === scanId
            ? {
                ...scan,
                status: 'success' as const,
                message: apiResponse?.message
                  || apiResponse?.name
                  || apiResponse?.productName
                  || apiResponse?.workOrderNo
                  || `${currentScanType.label} 스캔 완료`,
              }
            : scan
        )
      );

      setSnackbar({
        open: true,
        message: `${currentScanType.label} 바코드 스캔 성공: ${result.data}`,
        severity: 'success',
      });
    } catch (error: any) {
      const errorMessage =
        error?.response?.data?.message || error?.message || '스캔 처리 중 오류가 발생했습니다.';

      // Mark the scan as failed with error message
      setScanHistory((prev) =>
        prev.map((scan) =>
          scan.id === scanId
            ? {
                ...scan,
                status: 'error' as const,
                message: errorMessage,
              }
            : scan
        )
      );

      setSnackbar({
        open: true,
        message: `스캔 실패: ${errorMessage}`,
        severity: 'error',
      });
    }
  };

  const getScanTypeInstructions = (type: string) => {
    const instructions: Record<string, string> = {
      'work-order': '작업 지시서의 QR 코드 또는 바코드를 스캔하세요',
      'lot': '자재 또는 제품의 LOT 번호 바코드를 스캔하세요',
      'product': '제품 바코드를 스캔하여 정보를 확인하세요',
      'location': '창고 위치의 QR 코드를 스캔하세요',
    };
    return instructions[type] || '';
  };

  return (
    <Box sx={{ p: 3 }}>
      {/* Header */}
      <Typography variant="h4" gutterBottom fontWeight="bold">
        바코드 스캐너
      </Typography>
      <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
        스캔할 항목 유형을 선택하세요
      </Typography>

      {/* Scan Type Tabs */}
      <Card sx={{ mb: 3 }}>
        <Tabs
          value={activeTab}
          onChange={(_, newValue) => setActiveTab(newValue)}
          variant="fullWidth"
          sx={{
            borderBottom: 1,
            borderColor: 'divider',
          }}
        >
          {scanTypes.map((type) => (
            <Tab
              key={type.value}
              icon={type.icon}
              label={type.label}
              sx={{
                minHeight: 80,
                fontSize: '1rem',
              }}
            />
          ))}
        </Tabs>
      </Card>

      {/* Scanner */}
      <BarcodeScanner
        onScan={handleScan}
        title={`${currentScanType.label} 스캔`}
        subtitle={getScanTypeInstructions(currentScanType.value)}
        showManualInput={true}
      />

      {/* Scan History */}
      {scanHistory.length > 0 && (
        <Card sx={{ mt: 4 }}>
          <CardContent>
            <Typography variant="h6" gutterBottom fontWeight="bold">
              스캔 기록
            </Typography>
            <List>
              {scanHistory.map((scan, index) => (
                <Box key={scan.id}>
                  {index > 0 && <Divider />}
                  <ListItem>
                    <ListItemIcon>
                      {scan.status === 'success' ? (
                        <CheckIcon color="success" />
                      ) : (
                        scanTypes.find((t) => t.value === scan.type)?.icon
                      )}
                    </ListItemIcon>
                    <ListItemText
                      primary={
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          <Typography variant="body1" fontWeight="medium">
                            {scan.data}
                          </Typography>
                          <Chip
                            label={scanTypes.find((t) => t.value === scan.type)?.label}
                            size="small"
                            color="primary"
                            variant="outlined"
                          />
                          {scan.status === 'error' && (
                            <Chip label="실패" size="small" color="error" />
                          )}
                        </Box>
                      }
                      secondary={
                        <>
                          {scan.message && (
                            <Typography
                              variant="body2"
                              color={scan.status === 'error' ? 'error' : 'text.secondary'}
                            >
                              {scan.message}
                            </Typography>
                          )}
                          <Typography variant="caption" color="text.secondary">
                            {new Date(scan.timestamp).toLocaleString('ko-KR')}
                          </Typography>
                        </>
                      }
                    />
                  </ListItem>
                </Box>
              ))}
            </List>
          </CardContent>
        </Card>
      )}

      {/* Snackbar for scan notifications */}
      <Snackbar
        open={snackbar.open}
        autoHideDuration={4000}
        onClose={() => setSnackbar((prev) => ({ ...prev, open: false }))}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert
          onClose={() => setSnackbar((prev) => ({ ...prev, open: false }))}
          severity={snackbar.severity}
          variant="filled"
          sx={{ width: '100%' }}
        >
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Box>
  );
};

export default POPScannerPage;
