import React, { useState, useEffect } from 'react';
import {
  Box,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Snackbar,
  Alert,
  Chip,
  IconButton,
  Card,
  CardContent,
  Typography,
  Grid,
  Autocomplete,
} from '@mui/material';
import { DataGrid, GridColDef, GridRenderCellParams } from '@mui/x-data-grid';
import {
  Add as AddIcon,
  PlayArrow as StartIcon,
  Stop as StopIcon,
  CheckCircle as CompleteIcon,
  Warning as WarningIcon,
  Refresh as RefreshIcon,
  Timeline as TimelineIcon,
} from '@mui/icons-material';
import { format } from 'date-fns';
import productionScheduleService, { ProductionSchedule } from '../../services/productionScheduleService';
import workOrderService, { WorkOrder } from '../../services/workOrderService';

const ProductionSchedulePage: React.FC = () => {
  const [schedules, setSchedules] = useState<ProductionSchedule[]>([]);
  const [workOrders, setWorkOrders] = useState<WorkOrder[]>([]);
  const [loading, setLoading] = useState(false);
  const [openGenerateDialog, setOpenGenerateDialog] = useState(false);
  const [selectedWorkOrder, setSelectedWorkOrder] = useState<WorkOrder | null>(null);
  const [startDate, setStartDate] = useState<string>(
    format(new Date(), 'yyyy-MM-dd')
  );
  const [endDate, setEndDate] = useState<string>(
    format(new Date(Date.now() + 30 * 24 * 60 * 60 * 1000), 'yyyy-MM-dd')
  );
  const [snackbar, setSnackbar] = useState<{ open: boolean; message: string; severity: 'success' | 'error' }>({
    open: false,
    message: '',
    severity: 'success',
  });

  useEffect(() => {
    loadSchedules();
    loadWorkOrders();
  }, [startDate, endDate]);

  const loadSchedules = async () => {
    try {
      setLoading(true);
      const data = await productionScheduleService.getByPeriod(startDate, endDate);
      setSchedules(data || []);
    } catch (error) {
      console.error('Failed to load schedules:', error);
      setSchedules([]);
      setSnackbar({ open: true, message: '일정 목록 조회 실패', severity: 'error' });
    } finally {
      setLoading(false);
    }
  };

  const loadWorkOrders = async () => {
    try {
      const data = await workOrderService.getWorkOrders();
      // routing이 있는 WorkOrder만 필터링
      setWorkOrders((data || []).filter((wo) => wo.status !== 'COMPLETED' && wo.status !== 'CANCELLED'));
    } catch (error) {
      console.error('Failed to load work orders:', error);
      setWorkOrders([]);
    }
  };

  const handleGenerateSchedule = async () => {
    if (!selectedWorkOrder) return;

    try {
      await productionScheduleService.generateFromWorkOrder(selectedWorkOrder.workOrderId);
      setSnackbar({ open: true, message: '일정이 생성되었습니다', severity: 'success' });
      setOpenGenerateDialog(false);
      setSelectedWorkOrder(null);
      loadSchedules();
    } catch (error: any) {
      console.error('Failed to generate schedule:', error);
      const message = error.response?.data?.message || '일정 생성 실패';
      setSnackbar({ open: true, message, severity: 'error' });
    }
  };

  const handleUpdateStatus = async (scheduleId: number, status: string) => {
    try {
      await productionScheduleService.updateStatus(scheduleId, status);
      setSnackbar({
        open: true,
        message: `상태가 ${getStatusLabel(status)}(으)로 변경되었습니다`,
        severity: 'success',
      });
      loadSchedules();
    } catch (error) {
      console.error('Failed to update status:', error);
      setSnackbar({ open: true, message: '상태 변경 실패', severity: 'error' });
    }
  };

  const getStatusLabel = (status: string): string => {
    const statusMap: { [key: string]: string } = {
      SCHEDULED: '예정',
      READY: '준비',
      IN_PROGRESS: '진행중',
      COMPLETED: '완료',
      DELAYED: '지연',
      CANCELLED: '취소',
    };
    return statusMap[status] || status;
  };

  const getStatusColor = (status: string): 'default' | 'primary' | 'secondary' | 'success' | 'error' | 'warning' => {
    const colorMap: { [key: string]: 'default' | 'primary' | 'secondary' | 'success' | 'error' | 'warning' } = {
      SCHEDULED: 'primary',
      READY: 'warning',
      IN_PROGRESS: 'secondary',
      COMPLETED: 'success',
      DELAYED: 'error',
      CANCELLED: 'default',
    };
    return colorMap[status] || 'default';
  };

  const formatDateTime = (dateStr: string | undefined): string => {
    if (!dateStr) return '-';
    try {
      return format(new Date(dateStr), 'MM/dd HH:mm');
    } catch {
      return dateStr;
    }
  };

  const columns: GridColDef[] = [
    { field: 'workOrderNo', headerName: '작업지시', width: 120 },
    { field: 'productName', headerName: '제품명', width: 150 },
    { field: 'sequenceOrder', headerName: '순서', width: 60 },
    { field: 'processName', headerName: '공정', width: 120 },
    {
      field: 'plannedStartTime',
      headerName: '계획 시작',
      width: 110,
      renderCell: (params: GridRenderCellParams) => formatDateTime(params.value),
    },
    {
      field: 'plannedEndTime',
      headerName: '계획 종료',
      width: 110,
      renderCell: (params: GridRenderCellParams) => formatDateTime(params.value),
    },
    {
      field: 'plannedDuration',
      headerName: '소요(분)',
      width: 80,
      renderCell: (params: GridRenderCellParams) => `${params.value}분`,
    },
    {
      field: 'assignedEquipmentName',
      headerName: '설비',
      width: 120,
      renderCell: (params: GridRenderCellParams) => params.value || '-',
    },
    {
      field: 'assignedWorkers',
      headerName: '인원',
      width: 60,
    },
    {
      field: 'status',
      headerName: '상태',
      width: 100,
      renderCell: (params: GridRenderCellParams) => (
        <Chip label={getStatusLabel(params.value)} color={getStatusColor(params.value)} size="small" />
      ),
    },
    {
      field: 'progressRate',
      headerName: '진행률',
      width: 80,
      renderCell: (params: GridRenderCellParams) => `${params.value || 0}%`,
    },
    {
      field: 'isDelayed',
      headerName: '지연',
      width: 60,
      renderCell: (params: GridRenderCellParams) =>
        params.value ? <WarningIcon color="error" fontSize="small" /> : null,
    },
    {
      field: 'actions',
      headerName: '작업',
      width: 150,
      sortable: false,
      renderCell: (params: GridRenderCellParams) => {
        const schedule = params.row as ProductionSchedule;
        return (
          <Box>
            {schedule.status === 'SCHEDULED' && (
              <IconButton
                size="small"
                onClick={() => handleUpdateStatus(schedule.scheduleId, 'IN_PROGRESS')}
                title="시작"
              >
                <StartIcon fontSize="small" />
              </IconButton>
            )}
            {schedule.status === 'IN_PROGRESS' && (
              <>
                <IconButton
                  size="small"
                  onClick={() => handleUpdateStatus(schedule.scheduleId, 'COMPLETED')}
                  title="완료"
                >
                  <CompleteIcon fontSize="small" color="success" />
                </IconButton>
                <IconButton
                  size="small"
                  onClick={() => handleUpdateStatus(schedule.scheduleId, 'SCHEDULED')}
                  title="중지"
                >
                  <StopIcon fontSize="small" />
                </IconButton>
              </>
            )}
          </Box>
        );
      },
    },
  ];

  // 통계 계산
  const stats = {
    total: schedules.length,
    scheduled: schedules.filter((s) => s.status === 'SCHEDULED').length,
    inProgress: schedules.filter((s) => s.status === 'IN_PROGRESS').length,
    completed: schedules.filter((s) => s.status === 'COMPLETED').length,
    delayed: schedules.filter((s) => s.isDelayed).length,
  };

  return (
    <Box sx={{ p: 3 }}>
      {/* 통계 카드 */}
      <Grid container spacing={2} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={6} md={2.4}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                전체 일정
              </Typography>
              <Typography variant="h4">{stats.total}</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={2.4}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                예정
              </Typography>
              <Typography variant="h4" color="primary">
                {stats.scheduled}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={2.4}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                진행중
              </Typography>
              <Typography variant="h4" color="secondary">
                {stats.inProgress}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={2.4}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                완료
              </Typography>
              <Typography variant="h4" color="success.main">
                {stats.completed}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={2.4}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                지연
              </Typography>
              <Typography variant="h4" color="error">
                {stats.delayed}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* 헤더 */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3, alignItems: 'center' }}>
        <h2>생산 일정 관리</h2>
        <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
          <TextField
            label="시작일"
            type="date"
            value={startDate}
            onChange={(e) => setStartDate(e.target.value)}
            InputLabelProps={{ shrink: true }}
            size="small"
          />
          <TextField
            label="종료일"
            type="date"
            value={endDate}
            onChange={(e) => setEndDate(e.target.value)}
            InputLabelProps={{ shrink: true }}
            size="small"
          />
          <Button variant="outlined" startIcon={<RefreshIcon />} onClick={loadSchedules}>
            새로고침
          </Button>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => setOpenGenerateDialog(true)}
          >
            일정 생성
          </Button>
        </Box>
      </Box>

      {/* DataGrid */}
      <DataGrid
        rows={schedules}
        columns={columns}
        loading={loading}
        getRowId={(row) => row.scheduleId}
        pageSizeOptions={[10, 25, 50, 100]}
        initialState={{
          pagination: { paginationModel: { pageSize: 25 } },
          sorting: { sortModel: [{ field: 'plannedStartTime', sort: 'asc' }] },
        }}
        sx={{ height: 600 }}
      />

      {/* 일정 생성 다이얼로그 */}
      <Dialog open={openGenerateDialog} onClose={() => setOpenGenerateDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>작업지시에서 일정 생성</DialogTitle>
        <DialogContent>
          <Box sx={{ mt: 2 }}>
            <Autocomplete
              options={workOrders}
              getOptionLabel={(option) => `${option.workOrderNo} - ${option.productName}`}
              value={selectedWorkOrder}
              onChange={(_, newValue) => setSelectedWorkOrder(newValue)}
              renderInput={(params) => <TextField {...params} label="작업지시 선택" required />}
            />
            {selectedWorkOrder && (
              <Box sx={{ mt: 2, p: 2, bgcolor: 'background.paper', borderRadius: 1 }}>
                <Typography variant="body2" color="textSecondary">
                  제품: {selectedWorkOrder.productName}
                </Typography>
                <Typography variant="body2" color="textSecondary">
                  계획 수량: {selectedWorkOrder.plannedQuantity}
                </Typography>
                <Typography variant="body2" color="textSecondary">
                  계획 기간: {formatDateTime(selectedWorkOrder.plannedStartDate)} ~{' '}
                  {formatDateTime(selectedWorkOrder.plannedEndDate)}
                </Typography>
              </Box>
            )}
            <Alert severity="info" sx={{ mt: 2 }}>
              선택한 작업지시의 공정 라우팅을 기반으로 각 공정별 상세 일정이 자동 생성됩니다.
            </Alert>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenGenerateDialog(false)}>취소</Button>
          <Button onClick={handleGenerateSchedule} variant="contained" disabled={!selectedWorkOrder}>
            생성
          </Button>
        </DialogActions>
      </Dialog>

      {/* Snackbar */}
      <Snackbar open={snackbar.open} autoHideDuration={3000} onClose={() => setSnackbar({ ...snackbar, open: false })}>
        <Alert severity={snackbar.severity}>{snackbar.message}</Alert>
      </Snackbar>
    </Box>
  );
};

export default ProductionSchedulePage;
