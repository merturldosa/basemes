import React, { useState, useEffect } from 'react';
import {
  Box,
  Container,
  Typography,
  Card,
  CardContent,
  Button,
  TextField,
  List,
  ListItem,
  ListItemText,
  Chip,
  IconButton,
  Fab,
  Divider,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Alert,
  LinearProgress
} from '@mui/material';
import {
  QrCodeScanner as ScanIcon,
  CheckCircle as CheckIcon,
  Error as ErrorIcon,
  Send as SendIcon,
  List as ListIcon
} from '@mui/icons-material';
import QRScanner from '../../components/QRScanner';
import { barcodeService } from '../../services/barcodeService';
import { physicalInventoryService } from '../../services/physicalInventoryService';
import { useAuthStore } from '@/stores/authStore';

interface ScannedItem {
  lotNo: string;
  productCode: string;
  productName: string;
  systemQuantity: number;
  countedQuantity?: number;
  unit: string;
  status: 'pending' | 'counted' | 'error';
  itemId?: number;
}

/**
 * 모바일 실사 페이지
 * QR 스캔 기반 간편 실사
 */
const MobileInventoryCheckPage: React.FC = () => {
  const { user } = useAuthStore();
  const [physicalInventoryId, setPhysicalInventoryId] = useState<number | null>(null);
  const [showScanner, setShowScanner] = useState(false);
  const [scannedItems, setScannedItems] = useState<ScannedItem[]>([]);
  const [currentItem, setCurrentItem] = useState<ScannedItem | null>(null);
  const [countInput, setCountInput] = useState('');
  const [showCountDialog, setShowCountDialog] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  // QR 스캔 처리
  const handleScan = async (qrData: string) => {
    try {
      setLoading(true);
      setError(null);

      // QR 코드 스캔하여 LOT 정보 조회
      const response = await barcodeService.scan({
        qrData,
        scanLocation: '모바일 실사',
        scanPurpose: 'INVENTORY_CHECK'
      });

      const lotInfo = response.data;

      // 이미 스캔한 항목인지 확인
      const existingItem = scannedItems.find(item => item.lotNo === lotInfo.lotNo);

      if (existingItem) {
        setError('이미 스캔한 LOT입니다: ' + lotInfo.lotNo);
        return;
      }

      // 스캔된 항목 추가
      const newItem: ScannedItem = {
        lotNo: lotInfo.lotNo,
        productCode: lotInfo.productCode,
        productName: lotInfo.productName,
        systemQuantity: lotInfo.currentQuantity,
        unit: lotInfo.unit,
        status: 'pending',
        itemId: lotInfo.itemId,
      };

      setScannedItems(prev => [...prev, newItem]);
      setCurrentItem(newItem);
      setShowScanner(false);
      setShowCountDialog(true);
    } catch (err: any) {
      console.error('QR scan error:', err);
      setError('QR 스캔 실패: ' + (err.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  // 실사 수량 입력
  const handleCountSubmit = () => {
    if (!currentItem || !countInput) return;

    const countedQuantity = parseFloat(countInput);

    if (isNaN(countedQuantity) || countedQuantity < 0) {
      setError('유효한 수량을 입력해주세요');
      return;
    }

    // 항목 업데이트
    setScannedItems(prev =>
      prev.map(item =>
        item.lotNo === currentItem.lotNo
          ? { ...item, countedQuantity, status: 'counted' }
          : item
      )
    );

    // 다이얼로그 닫기
    setShowCountDialog(false);
    setCountInput('');
    setCurrentItem(null);

    // 자동으로 다음 스캔
    setShowScanner(true);
  };

  // 실사 완료 및 서버 전송
  const handleComplete = async () => {
    try {
      setLoading(true);
      setError(null);

      // 실사되지 않은 항목 확인
      const unCountedItems = scannedItems.filter(item => item.status === 'pending');

      if (unCountedItems.length > 0) {
        setError(`${unCountedItems.length}개 항목의 실사가 완료되지 않았습니다.`);
        return;
      }

      // 서버로 실사 결과 전송
      if (!physicalInventoryId) {
        setError('실사 ID가 설정되지 않았습니다.');
        return;
      }

      for (const item of scannedItems) {
        await physicalInventoryService.updateCount(physicalInventoryId, {
          itemId: item.itemId ?? 0,
          countedQuantity: item.countedQuantity!,
          countedByUserId: user?.userId ?? 0,
        });
      }

      alert('실사가 완료되었습니다!');

      // 목록 초기화
      setScannedItems([]);
    } catch (err: any) {
      console.error('Complete error:', err);
      setError('실사 완료 실패: ' + (err.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  // 진행률 계산
  const getProgress = () => {
    if (scannedItems.length === 0) return 0;
    const countedCount = scannedItems.filter(item => item.status === 'counted').length;
    return (countedCount / scannedItems.length) * 100;
  };

  return (
    <Container maxWidth="sm" sx={{ py: 2, pb: 10 }}>
      {/* 헤더 */}
      <Box sx={{ mb: 3 }}>
        <Typography variant="h5" gutterBottom>
          📦 모바일 실사
        </Typography>
        <Typography variant="body2" color="text.secondary">
          QR 코드를 스캔하여 재고를 확인하세요
        </Typography>
      </Box>

      {/* 진행률 */}
      {scannedItems.length > 0 && (
        <Card sx={{ mb: 2 }}>
          <CardContent>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
              <Typography variant="body2">진행률</Typography>
              <Typography variant="body2" fontWeight="bold">
                {scannedItems.filter(i => i.status === 'counted').length} /{' '}
                {scannedItems.length}
              </Typography>
            </Box>
            <LinearProgress
              variant="determinate"
              value={getProgress()}
              sx={{ height: 8, borderRadius: 4 }}
            />
          </CardContent>
        </Card>
      )}

      {/* 에러 메시지 */}
      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {/* 스캔된 항목 목록 */}
      {scannedItems.length > 0 && (
        <Card sx={{ mb: 2 }}>
          <CardContent>
            <Typography variant="subtitle2" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <ListIcon fontSize="small" />
              스캔 항목 ({scannedItems.length})
            </Typography>

            <List dense>
              {scannedItems.map((item, index) => (
                <React.Fragment key={item.lotNo}>
                  {index > 0 && <Divider />}
                  <ListItem
                    secondaryAction={
                      item.status === 'counted' ? (
                        <Chip
                          icon={<CheckIcon />}
                          label="완료"
                          color="success"
                          size="small"
                        />
                      ) : (
                        <Chip label="대기" size="small" />
                      )
                    }
                  >
                    <ListItemText
                      primary={
                        <Typography variant="body2" fontWeight="medium">
                          {item.productName}
                        </Typography>
                      }
                      secondary={
                        <Box component="span" sx={{ display: 'flex', flexDirection: 'column', gap: 0.5 }}>
                          <Typography variant="caption" color="text.secondary">
                            LOT: {item.lotNo}
                          </Typography>
                          <Typography variant="caption">
                            시스템: {item.systemQuantity.toLocaleString()} {item.unit}
                            {item.countedQuantity !== undefined && (
                              <>
                                {' → '}
                                실사: {item.countedQuantity.toLocaleString()} {item.unit}
                                {item.countedQuantity !== item.systemQuantity && (
                                  <Chip
                                    label={`차이: ${(item.countedQuantity - item.systemQuantity).toLocaleString()}`}
                                    size="small"
                                    color={item.countedQuantity > item.systemQuantity ? 'info' : 'warning'}
                                    sx={{ ml: 1 }}
                                  />
                                )}
                              </>
                            )}
                          </Typography>
                        </Box>
                      }
                    />
                  </ListItem>
                </React.Fragment>
              ))}
            </List>
          </CardContent>
        </Card>
      )}

      {/* 안내 메시지 (항목 없을 때) */}
      {scannedItems.length === 0 && !showScanner && (
        <Card sx={{ textAlign: 'center', py: 6 }}>
          <CardContent>
            <ScanIcon sx={{ fontSize: 80, color: 'text.secondary', mb: 2 }} />
            <Typography variant="h6" gutterBottom>
              QR 스캔 시작
            </Typography>
            <Typography variant="body2" color="text.secondary">
              아래 버튼을 눌러 QR 코드를 스캔하세요
            </Typography>
          </CardContent>
        </Card>
      )}

      {/* QR 스캐너 */}
      {showScanner && (
        <Card>
          <CardContent>
            <QRScanner
              onScan={handleScan}
              onClose={() => setShowScanner(false)}
              continuous={false}
            />
          </CardContent>
        </Card>
      )}

      {/* 실사 수량 입력 다이얼로그 */}
      <Dialog
        open={showCountDialog}
        onClose={() => setShowCountDialog(false)}
        fullWidth
        maxWidth="xs"
      >
        <DialogTitle>실사 수량 입력</DialogTitle>
        <DialogContent>
          {currentItem && (
            <>
              <Typography variant="body2" gutterBottom>
                제품: {currentItem.productName}
              </Typography>
              <Typography variant="body2" gutterBottom color="text.secondary">
                LOT: {currentItem.lotNo}
              </Typography>
              <Typography variant="body2" gutterBottom sx={{ mb: 2 }}>
                시스템 재고: {currentItem.systemQuantity.toLocaleString()} {currentItem.unit}
              </Typography>

              <TextField
                autoFocus
                fullWidth
                type="number"
                label="실사 수량"
                value={countInput}
                onChange={(e) => setCountInput(e.target.value)}
                inputProps={{ step: '0.001', min: '0' }}
                helperText={`단위: ${currentItem.unit}`}
              />
            </>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setShowCountDialog(false)}>취소</Button>
          <Button
            onClick={handleCountSubmit}
            variant="contained"
            disabled={!countInput}
          >
            확인
          </Button>
        </DialogActions>
      </Dialog>

      {/* 하단 고정 버튼 */}
      <Box
        sx={{
          position: 'fixed',
          bottom: 0,
          left: 0,
          right: 0,
          p: 2,
          backgroundColor: 'background.paper',
          borderTop: 1,
          borderColor: 'divider',
          display: 'flex',
          gap: 2
        }}
      >
        <Fab
          color="primary"
          variant="extended"
          onClick={() => setShowScanner(true)}
          disabled={showScanner || loading}
          sx={{ flex: 1 }}
        >
          <ScanIcon sx={{ mr: 1 }} />
          QR 스캔
        </Fab>

        {scannedItems.length > 0 && (
          <Fab
            color="success"
            variant="extended"
            onClick={handleComplete}
            disabled={getProgress() < 100 || loading}
            sx={{ flex: 1 }}
          >
            <SendIcon sx={{ mr: 1 }} />
            완료
          </Fab>
        )}
      </Box>
    </Container>
  );
};

export default MobileInventoryCheckPage;
