/**
 * POP Scanner Page
 * Barcode scanning for work orders, LOTs, products, and locations
 * @author Moon Myung-seop
 */

import { useState } from 'react';
import { useTranslation } from 'react-i18next';
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
  const { t } = useTranslation();
  const [activeTab, setActiveTab] = useState(0);
  const [scanHistory, setScanHistory] = useState<ScanHistory[]>([]);
  const [snackbar, setSnackbar] = useState<{
    open: boolean;
    message: string;
    severity: 'success' | 'error';
  }>({ open: false, message: '', severity: 'success' });

  const scanTypes = [
    { label: t('pages.popScanner.scanTypes.workOrder'), value: 'work-order', icon: <WorkOrderIcon /> },
    { label: t('pages.popScanner.scanTypes.lot'), value: 'lot', icon: <LotIcon /> },
    { label: t('pages.popScanner.scanTypes.product'), value: 'product', icon: <ProductIcon /> },
    { label: t('pages.popScanner.scanTypes.location'), value: 'location', icon: <LocationIcon /> },
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
      message: t('pages.popScanner.processing', { type: currentScanType.label }),
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
                  || t('pages.popScanner.scanComplete', { type: currentScanType.label }),
              }
            : scan
        )
      );

      setSnackbar({
        open: true,
        message: t('pages.popScanner.scanSuccess', { type: currentScanType.label, data: result.data }),
        severity: 'success',
      });
    } catch (error: any) {
      const errorMessage =
        error?.response?.data?.message || error?.message || t('pages.popScanner.scanError');

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
        message: t('pages.popScanner.scanFailed', { message: errorMessage }),
        severity: 'error',
      });
    }
  };

  const getScanTypeInstructions = (type: string) => {
    const instructions: Record<string, string> = {
      'work-order': t('pages.popScanner.instructions.workOrder'),
      'lot': t('pages.popScanner.instructions.lot'),
      'product': t('pages.popScanner.instructions.product'),
      'location': t('pages.popScanner.instructions.location'),
    };
    return instructions[type] || '';
  };

  return (
    <Box sx={{ p: 3 }}>
      {/* Header */}
      <Typography variant="h4" gutterBottom fontWeight="bold">
        {t('pages.popScanner.title')}
      </Typography>
      <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
        {t('pages.popScanner.subtitle')}
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
        title={`${currentScanType.label} ${t('navigation.pop.scanner')}`}
        subtitle={getScanTypeInstructions(currentScanType.value)}
        showManualInput={true}
      />

      {/* Scan History */}
      {scanHistory.length > 0 && (
        <Card sx={{ mt: 4 }}>
          <CardContent>
            <Typography variant="h6" gutterBottom fontWeight="bold">
              {t('pages.popScanner.scanHistory')}
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
                            <Chip label={t('pages.popScanner.failed')} size="small" color="error" />
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
