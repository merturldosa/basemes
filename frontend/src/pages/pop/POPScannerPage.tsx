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

  const scanTypes = [
    { label: '작업 지시', value: 'work-order', icon: <WorkOrderIcon /> },
    { label: 'LOT', value: 'lot', icon: <LotIcon /> },
    { label: '제품', value: 'product', icon: <ProductIcon /> },
    { label: '위치', value: 'location', icon: <LocationIcon /> },
  ];

  const currentScanType = scanTypes[activeTab];

  const handleScan = (result: BarcodeScanResult) => {
    console.log('Scanned:', result);

    // Validate scan based on type
    const newScan: ScanHistory = {
      id: `${Date.now()}-${Math.random()}`,
      type: currentScanType.value,
      data: result.data,
      timestamp: result.timestamp,
      status: 'success',
      message: `${currentScanType.label} 스캔 완료`,
    };

    // Add to history
    setScanHistory((prev) => [newScan, ...prev.slice(0, 9)]);

    // TODO: Process scan based on type
    // - Work Order: Load work order details
    // - LOT: Verify LOT and load details
    // - Product: Load product info
    // - Location: Verify location
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
          {scanTypes.map((type, index) => (
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
                        </Box>
                      }
                      secondary={
                        <>
                          {scan.message && (
                            <Typography variant="body2" color="text.secondary">
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
    </Box>
  );
};

export default POPScannerPage;
