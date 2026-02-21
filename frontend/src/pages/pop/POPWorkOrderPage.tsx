/**
 * POP Work Order Execution Page
 * Real-time work order execution and tracking
 * @author Moon Myung-seop
 */

import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
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
  const { t } = useTranslation();
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
    let interval: ReturnType<typeof setInterval> | null = null;
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
          {t('pages.popWorkOrder.title')}
        </Typography>
        <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
          {t('pages.popWorkOrder.subtitle')}
        </Typography>

        <Button
          variant="outlined"
          size="large"
          startIcon={<ScanIcon />}
          fullWidth
          sx={{ mb: 3, py: 2 }}
        >
          {t('pages.popWorkOrder.scanBarcode')}
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
                    {t('pages.popWorkOrder.targetQuantity')}: {wo.targetQuantity} EA
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
                <Typography variant="subtitle2">{t('pages.popWorkOrder.workTime')}</Typography>
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
                  {t('pages.popWorkOrder.operator')}
                </Typography>
              </Box>
              <Typography variant="h6" fontWeight="bold">
                {selectedWorkOrder.operatorName || t('pages.popWorkOrder.unassigned')}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Progress */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Typography variant="h6" gutterBottom fontWeight="bold">
            {t('pages.popWorkOrder.productionProgress')}
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
            {t('pages.popWorkOrder.percentComplete', { percent: getProgress().toFixed(1) })}
          </Typography>
        </CardContent>
      </Card>

      {/* Production Stats */}
      <Grid container spacing={2} sx={{ mb: 3 }}>
        <Grid item xs={6}>
          <Card sx={{ bgcolor: 'success.light' }}>
            <CardContent sx={{ textAlign: 'center' }}>
              <Typography variant="subtitle2" color="success.dark">
                {t('pages.popWorkOrder.goodQuantity')}
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
                {t('pages.popWorkOrder.defectQuantity')}
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
              {t('pages.popWorkOrder.quickInput')}
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
                  {t('pages.popWorkOrder.startWork')}
                </Button>
              </Grid>
            ) : (
              <Grid item xs={12}>
                <Button
                  variant="contained"
                  size="large"
                  fullWidth
                  startIcon={<StartIcon />}
                  onClick={handleResumeWork}
                  color="success"
                  sx={{ py: 3, fontSize: '1.2rem' }}
                >
                  {t('pages.popWorkOrder.resumeWork')}
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
                {t('pages.popWorkOrder.pauseWork')}
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
                {t('pages.popWorkOrder.registerDefect')}
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
              {t('pages.popWorkOrder.completeWork')}
            </Button>
          </Grid>
        )}
      </Grid>

      {/* Defect Dialog */}
      <Dialog open={openDefectDialog} onClose={() => setOpenDefectDialog(false)}>
        <DialogTitle>{t('pages.popWorkOrder.defectDialog.title')}</DialogTitle>
        <DialogContent>
          <TextField
            fullWidth
            label={t('pages.popWorkOrder.defectDialog.quantity')}
            type="number"
            value={defectQuantity}
            onChange={(e) => setDefectQuantity(e.target.value)}
            sx={{ mb: 2, mt: 1 }}
          />
          <TextField
            fullWidth
            label={t('pages.popWorkOrder.defectDialog.reason')}
            multiline
            rows={3}
            value={defectReason}
            onChange={(e) => setDefectReason(e.target.value)}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenDefectDialog(false)}>{t('common.buttons.cancel')}</Button>
          <Button
            onClick={handleRecordDefect}
            variant="contained"
            color="error"
            disabled={!defectQuantity}
          >
            {t('pages.popWorkOrder.defectDialog.register')}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Complete Dialog */}
      <Dialog open={openCompleteDialog} onClose={() => setOpenCompleteDialog(false)}>
        <DialogTitle>{t('pages.popWorkOrder.completeDialog.title')}</DialogTitle>
        <DialogContent>
          <Alert severity="info" sx={{ mb: 2 }}>
            {t('pages.popWorkOrder.completeDialog.confirmMessage')}
          </Alert>
          <List>
            <ListItem>
              <ListItemText
                primary={t('pages.popWorkOrder.completeDialog.producedQuantity')}
                secondary={`${selectedWorkOrder.producedQuantity} / ${selectedWorkOrder.targetQuantity} EA`}
              />
            </ListItem>
            <ListItem>
              <ListItemText primary={t('pages.popWorkOrder.completeDialog.defectQuantity')} secondary={`${selectedWorkOrder.defectQuantity} EA`} />
            </ListItem>
            <ListItem>
              <ListItemText primary={t('pages.popWorkOrder.completeDialog.workTime')} secondary={formatTime(elapsedTime)} />
            </ListItem>
          </List>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenCompleteDialog(false)}>{t('common.buttons.cancel')}</Button>
          <Button onClick={handleCompleteWork} variant="contained" color="success">
            {t('common.status.completed')}
          </Button>
        </DialogActions>
      </Dialog>

      {selectedWorkOrder.status === 'COMPLETED' && (
        <Alert severity="success" sx={{ mt: 3 }}>
          {t('pages.popWorkOrder.completedMessage', { quantity: selectedWorkOrder.producedQuantity })}
        </Alert>
      )}
    </Box>
  );
};

export default POPWorkOrderPage;
