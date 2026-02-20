/**
 * POP Work Order Execution Page
 * Real-time work order execution and tracking
 * @author Moon Myung-seop
 */

import { useState, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  Grid,
  Chip,
  LinearProgress,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  List,
  ListItem,
  ListItemText,
  Alert,
  IconButton,
} from '@mui/material';
import {
  PlayArrow as StartIcon,
  Pause as PauseIcon,
  CheckCircle as CompleteIcon,
  Warning as DefectIcon,
  QrCodeScanner as ScanIcon,
  Timer as TimerIcon,
  Person as PersonIcon,
  Refresh as RefreshIcon,
} from '@mui/icons-material';

interface WorkOrder {
  workOrderId: number;
  workOrderNo: string;
  productCode: string;
  productName: string;
  targetQuantity: number;
  producedQuantity: number;
  defectQuantity: number;
  status: string;
  startTime?: string;
  endTime?: string;
  operatorName?: string;
}

const POPWorkOrderPage: React.FC = () => {
  const [selectedWorkOrder, setSelectedWorkOrder] = useState<WorkOrder | null>(null);
  const [workOrders, setWorkOrders] = useState<WorkOrder[]>([]);
  const [isWorking, setIsWorking] = useState(false);
  const [elapsedTime, setElapsedTime] = useState(0);
  const [openDefectDialog, setOpenDefectDialog] = useState(false);
  const [openCompleteDialog, setOpenCompleteDialog] = useState(false);
  const [defectQuantity, setDefectQuantity] = useState('');
  const [defectReason, setDefectReason] = useState('');

  // Mock work orders
  useEffect(() => {
    setWorkOrders([
      {
        workOrderId: 1,
        workOrderNo: 'WO-20260204-001',
        productCode: 'PROD-001',
        productName: '제품 A',
        targetQuantity: 1000,
        producedQuantity: 0,
        defectQuantity: 0,
        status: 'READY',
      },
      {
        workOrderId: 2,
        workOrderNo: 'WO-20260204-002',
        productCode: 'PROD-002',
        productName: '제품 B',
        targetQuantity: 500,
        producedQuantity: 0,
        defectQuantity: 0,
        status: 'READY',
      },
    ]);
  }, []);

  // Timer effect
  useEffect(() => {
    let interval: NodeJS.Timeout | null = null;
    if (isWorking) {
      interval = setInterval(() => {
        setElapsedTime((prev) => prev + 1);
      }, 1000);
    }
    return () => {
      if (interval) clearInterval(interval);
    };
  }, [isWorking]);

  const handleSelectWorkOrder = (workOrder: WorkOrder) => {
    setSelectedWorkOrder(workOrder);
    setElapsedTime(0);
  };

  const handleStartWork = () => {
    if (selectedWorkOrder) {
      setIsWorking(true);
      setSelectedWorkOrder({
        ...selectedWorkOrder,
        status: 'IN_PROGRESS',
        startTime: new Date().toISOString(),
        operatorName: '작업자1',
      });
    }
  };

  const handlePauseWork = () => {
    setIsWorking(false);
  };

  const handleResumeWork = () => {
    setIsWorking(true);
  };

  const handleRecordProduction = (quantity: number) => {
    if (selectedWorkOrder) {
      const newProducedQuantity = selectedWorkOrder.producedQuantity + quantity;
      setSelectedWorkOrder({
        ...selectedWorkOrder,
        producedQuantity: newProducedQuantity,
      });

      // Haptic feedback
      if (navigator.vibrate) {
        navigator.vibrate(100);
      }
    }
  };

  const handleRecordDefect = () => {
    if (selectedWorkOrder && defectQuantity) {
      const quantity = parseInt(defectQuantity);
      setSelectedWorkOrder({
        ...selectedWorkOrder,
        defectQuantity: selectedWorkOrder.defectQuantity + quantity,
      });
      setOpenDefectDialog(false);
      setDefectQuantity('');
      setDefectReason('');
    }
  };

  const handleCompleteWork = () => {
    if (selectedWorkOrder) {
      setSelectedWorkOrder({
        ...selectedWorkOrder,
        status: 'COMPLETED',
        endTime: new Date().toISOString(),
      });
      setIsWorking(false);
      setOpenCompleteDialog(false);
    }
  };

  const formatTime = (seconds: number) => {
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    const secs = seconds % 60;
    return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };

  const getProgress = () => {
    if (!selectedWorkOrder) return 0;
    return (selectedWorkOrder.producedQuantity / selectedWorkOrder.targetQuantity) * 100;
  };

  // No work order selected
  if (!selectedWorkOrder) {
    return (
      <Box sx={{ p: 3 }}>
        <Typography variant="h4" gutterBottom fontWeight="bold">
          작업 지시 선택
        </Typography>
        <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
          작업할 지시를 선택하거나 바코드를 스캔하세요
        </Typography>

        <Button
          variant="outlined"
          size="large"
          startIcon={<ScanIcon />}
          fullWidth
          sx={{ mb: 3, py: 2 }}
        >
          바코드 스캔으로 작업 지시 불러오기
        </Button>

        <Grid container spacing={2}>
          {workOrders.map((wo) => (
            <Grid item xs={12} key={wo.workOrderId}>
              <Card
                sx={{
                  cursor: 'pointer',
                  '&:hover': { boxShadow: 4 },
                  borderLeft: 4,
                  borderColor: 'primary.main',
                }}
                onClick={() => handleSelectWorkOrder(wo)}
              >
                <CardContent>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
                    <Typography variant="h6" fontWeight="bold">
                      {wo.workOrderNo}
                    </Typography>
                    <Chip label={wo.status} color="primary" size="small" />
                  </Box>
                  <Typography variant="body1" gutterBottom>
                    {wo.productCode} - {wo.productName}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    목표 수량: {wo.targetQuantity} EA
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      </Box>
    );
  }

  // Work order selected
  return (
    <Box sx={{ p: 3 }}>
      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box>
          <Typography variant="h4" fontWeight="bold">
            {selectedWorkOrder.workOrderNo}
          </Typography>
          <Typography variant="body1" color="text.secondary">
            {selectedWorkOrder.productCode} - {selectedWorkOrder.productName}
          </Typography>
        </Box>
        <IconButton onClick={() => setSelectedWorkOrder(null)}>
          <RefreshIcon />
        </IconButton>
      </Box>

      {/* Status Cards */}
      <Grid container spacing={2} sx={{ mb: 3 }}>
        {/* Timer */}
        <Grid item xs={12} sm={6}>
          <Card sx={{ bgcolor: 'primary.main', color: 'white' }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                <TimerIcon sx={{ mr: 1 }} />
                <Typography variant="subtitle2">작업 시간</Typography>
              </Box>
              <Typography variant="h4" fontWeight="bold">
                {formatTime(elapsedTime)}
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        {/* Operator */}
        <Grid item xs={12} sm={6}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                <PersonIcon sx={{ mr: 1, color: 'text.secondary' }} />
                <Typography variant="subtitle2" color="text.secondary">
                  작업자
                </Typography>
              </Box>
              <Typography variant="h6" fontWeight="bold">
                {selectedWorkOrder.operatorName || '미배정'}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Progress */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Typography variant="h6" gutterBottom fontWeight="bold">
            생산 진행률
          </Typography>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
            <Typography variant="h4" color="primary" fontWeight="bold">
              {selectedWorkOrder.producedQuantity}
            </Typography>
            <Typography variant="h6" color="text.secondary">
              / {selectedWorkOrder.targetQuantity} EA
            </Typography>
          </Box>
          <LinearProgress
            variant="determinate"
            value={getProgress()}
            sx={{ height: 10, borderRadius: 1, mb: 1 }}
          />
          <Typography variant="body2" color="text.secondary">
            {getProgress().toFixed(1)}% 완료
          </Typography>
        </CardContent>
      </Card>

      {/* Production Stats */}
      <Grid container spacing={2} sx={{ mb: 3 }}>
        <Grid item xs={6}>
          <Card sx={{ bgcolor: 'success.light' }}>
            <CardContent sx={{ textAlign: 'center' }}>
              <Typography variant="subtitle2" color="success.dark">
                양품
              </Typography>
              <Typography variant="h4" fontWeight="bold" color="success.dark">
                {selectedWorkOrder.producedQuantity - selectedWorkOrder.defectQuantity}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={6}>
          <Card sx={{ bgcolor: 'error.light' }}>
            <CardContent sx={{ textAlign: 'center' }}>
              <Typography variant="subtitle2" color="error.dark">
                불량
              </Typography>
              <Typography variant="h4" fontWeight="bold" color="error.dark">
                {selectedWorkOrder.defectQuantity}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Quick Production Buttons */}
      {isWorking && (
        <Card sx={{ mb: 3 }}>
          <CardContent>
            <Typography variant="h6" gutterBottom fontWeight="bold">
              빠른 생산 입력
            </Typography>
            <Grid container spacing={2}>
              {[1, 5, 10, 50, 100].map((qty) => (
                <Grid item xs={4} sm={2.4} key={qty}>
                  <Button
                    variant="contained"
                    size="large"
                    fullWidth
                    onClick={() => handleRecordProduction(qty)}
                    sx={{ py: 3, fontSize: '1.2rem', fontWeight: 'bold' }}
                  >
                    +{qty}
                  </Button>
                </Grid>
              ))}
            </Grid>
          </CardContent>
        </Card>
      )}

      {/* Control Buttons */}
      <Grid container spacing={2}>
        {!isWorking && selectedWorkOrder.status !== 'COMPLETED' && (
          <>
            {selectedWorkOrder.status === 'READY' ? (
              <Grid item xs={12}>
                <Button
                  variant="contained"
                  size="large"
                  fullWidth
                  startIcon={<StartIcon />}
                  onClick={handleStartWork}
                  sx={{ py: 3, fontSize: '1.2rem' }}
                >
                  작업 시작
                </Button>
              </Grid>
            ) : (
              <Grid item xs={12}>
                <Button
                  variant="contained"
                  size="large"
                  fullWidth
                  startIcon={<PlayArrow />}
                  onClick={handleResumeWork}
                  color="success"
                  sx={{ py: 3, fontSize: '1.2rem' }}
                >
                  작업 재개
                </Button>
              </Grid>
            )}
          </>
        )}

        {isWorking && (
          <>
            <Grid item xs={12} sm={6}>
              <Button
                variant="outlined"
                size="large"
                fullWidth
                startIcon={<PauseIcon />}
                onClick={handlePauseWork}
                sx={{ py: 3 }}
              >
                일시 정지
              </Button>
            </Grid>
            <Grid item xs={12} sm={6}>
              <Button
                variant="outlined"
                size="large"
                fullWidth
                color="error"
                startIcon={<DefectIcon />}
                onClick={() => setOpenDefectDialog(true)}
                sx={{ py: 3 }}
              >
                불량 등록
              </Button>
            </Grid>
          </>
        )}

        {selectedWorkOrder.status === 'IN_PROGRESS' && (
          <Grid item xs={12}>
            <Button
              variant="contained"
              size="large"
              fullWidth
              color="success"
              startIcon={<CompleteIcon />}
              onClick={() => setOpenCompleteDialog(true)}
              sx={{ py: 3, fontSize: '1.2rem' }}
            >
              작업 완료
            </Button>
          </Grid>
        )}
      </Grid>

      {/* Defect Dialog */}
      <Dialog open={openDefectDialog} onClose={() => setOpenDefectDialog(false)}>
        <DialogTitle>불량 등록</DialogTitle>
        <DialogContent>
          <TextField
            fullWidth
            label="불량 수량"
            type="number"
            value={defectQuantity}
            onChange={(e) => setDefectQuantity(e.target.value)}
            sx={{ mb: 2, mt: 1 }}
          />
          <TextField
            fullWidth
            label="불량 사유"
            multiline
            rows={3}
            value={defectReason}
            onChange={(e) => setDefectReason(e.target.value)}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenDefectDialog(false)}>취소</Button>
          <Button
            onClick={handleRecordDefect}
            variant="contained"
            color="error"
            disabled={!defectQuantity}
          >
            등록
          </Button>
        </DialogActions>
      </Dialog>

      {/* Complete Dialog */}
      <Dialog open={openCompleteDialog} onClose={() => setOpenCompleteDialog(false)}>
        <DialogTitle>작업 완료 확인</DialogTitle>
        <DialogContent>
          <Alert severity="info" sx={{ mb: 2 }}>
            작업을 완료하시겠습니까?
          </Alert>
          <List>
            <ListItem>
              <ListItemText
                primary="생산 수량"
                secondary={`${selectedWorkOrder.producedQuantity} / ${selectedWorkOrder.targetQuantity} EA`}
              />
            </ListItem>
            <ListItem>
              <ListItemText primary="불량 수량" secondary={`${selectedWorkOrder.defectQuantity} EA`} />
            </ListItem>
            <ListItem>
              <ListItemText primary="작업 시간" secondary={formatTime(elapsedTime)} />
            </ListItem>
          </List>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenCompleteDialog(false)}>취소</Button>
          <Button onClick={handleCompleteWork} variant="contained" color="success">
            완료
          </Button>
        </DialogActions>
      </Dialog>

      {selectedWorkOrder.status === 'COMPLETED' && (
        <Alert severity="success" sx={{ mt: 3 }}>
          작업이 완료되었습니다. 생산 실적: {selectedWorkOrder.producedQuantity} EA
        </Alert>
      )}
    </Box>
  );
};

export default POPWorkOrderPage;
