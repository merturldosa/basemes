import React, { useState, useEffect } from 'react';
import {
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  TextField,
  Paper,
  Typography,
  Alert,
  Snackbar,
  MenuItem,
  Select,
  FormControl,
  InputLabel,
  SelectChangeEvent,
  Grid,
} from '@mui/material';
import {
  DataGrid,
  GridColDef,
  GridActionsCellItem,
  GridRowParams,
} from '@mui/x-data-grid';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
} from '@mui/icons-material';
import workResultService, { WorkResult, WorkResultCreateRequest, WorkResultUpdateRequest } from '../../services/workResultService';
import workOrderService, { WorkOrder } from '../../services/workOrderService';

const WorkResultsPage: React.FC = () => {
  const [workResults, setWorkResults] = useState<WorkResult[]>([]);
  const [workOrders, setWorkOrders] = useState<WorkOrder[]>([]);
  const [loading, setLoading] = useState(true);
  const [openDialog, setOpenDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [selectedWorkResult, setSelectedWorkResult] = useState<WorkResult | null>(null);
  const [formData, setFormData] = useState<WorkResultCreateRequest | WorkResultUpdateRequest>({
    workOrderId: 0,
    resultDate: '',
    quantity: 0,
    goodQuantity: 0,
    defectQuantity: 0,
    workStartTime: '',
    workEndTime: '',
    workerName: '',
    defectReason: '',
    remarks: '',
  });
  const [snackbar, setSnackbar] = useState<{ open: boolean; message: string; severity: 'success' | 'error' }>({
    open: false,
    message: '',
    severity: 'success',
  });

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const [resultsData, ordersData] = await Promise.all([
        workResultService.getWorkResults(),
        workOrderService.getWorkOrders(),
      ]);
      setWorkResults(resultsData);
      // Filter only IN_PROGRESS work orders for new results
      setWorkOrders(ordersData.filter(wo => wo.status === 'IN_PROGRESS' || wo.status === 'READY'));
    } catch (error) {
      showSnackbar('데이터 조회 실패', 'error');
    } finally {
      setLoading(false);
    }
  };

  const showSnackbar = (message: string, severity: 'success' | 'error') => {
    setSnackbar({ open: true, message, severity });
  };

  const handleCloseSnackbar = () => {
    setSnackbar({ ...snackbar, open: false });
  };

  const handleOpenDialog = (workResult?: WorkResult) => {
    if (workResult) {
      setSelectedWorkResult(workResult);
      setFormData({
        resultDate: workResult.resultDate.slice(0, 16),
        quantity: workResult.quantity,
        goodQuantity: workResult.goodQuantity,
        defectQuantity: workResult.defectQuantity,
        workStartTime: workResult.workStartTime.slice(0, 16),
        workEndTime: workResult.workEndTime.slice(0, 16),
        workerName: workResult.workerName || '',
        defectReason: workResult.defectReason || '',
        remarks: workResult.remarks || '',
      });
    } else {
      setSelectedWorkResult(null);
      const now = new Date();
      const oneHourAgo = new Date(now.getTime() - 60 * 60 * 1000);
      setFormData({
        workOrderId: workOrders[0]?.workOrderId || 0,
        resultDate: now.toISOString().slice(0, 16),
        quantity: 0,
        goodQuantity: 0,
        defectQuantity: 0,
        workStartTime: oneHourAgo.toISOString().slice(0, 16),
        workEndTime: now.toISOString().slice(0, 16),
        workerName: '',
        defectReason: '',
        remarks: '',
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedWorkResult(null);
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    const numericFields = ['quantity', 'goodQuantity', 'defectQuantity'];
    setFormData({
      ...formData,
      [name]: numericFields.includes(name) ? Number(value) : value,
    });
  };

  const handleSelectChange = (e: SelectChangeEvent<number>) => {
    setFormData({
      ...formData,
      workOrderId: Number(e.target.value),
    });
  };

  const handleSubmit = async () => {
    try {
      // Validate quantities
      const total = Number(formData.goodQuantity) + Number(formData.defectQuantity);
      if (total !== Number(formData.quantity)) {
        showSnackbar('총 수량은 양품 + 불량과 같아야 합니다', 'error');
        return;
      }

      if (selectedWorkResult) {
        await workResultService.updateWorkResult(selectedWorkResult.workResultId, formData as WorkResultUpdateRequest);
        showSnackbar('작업실적 수정 성공 (작업지시 집계 자동 업데이트)', 'success');
      } else {
        await workResultService.createWorkResult(formData as WorkResultCreateRequest);
        showSnackbar('작업실적 등록 성공 (작업지시 집계 자동 업데이트)', 'success');
      }
      handleCloseDialog();
      loadData();
    } catch (error: any) {
      showSnackbar(error.response?.data?.message || '작업 실패', 'error');
    }
  };

  const handleOpenDeleteDialog = (workResult: WorkResult) => {
    setSelectedWorkResult(workResult);
    setOpenDeleteDialog(true);
  };

  const handleCloseDeleteDialog = () => {
    setOpenDeleteDialog(false);
    setSelectedWorkResult(null);
  };

  const handleDelete = async () => {
    if (!selectedWorkResult) return;

    try {
      await workResultService.deleteWorkResult(selectedWorkResult.workResultId);
      showSnackbar('작업실적 삭제 성공 (작업지시 집계 자동 업데이트)', 'success');
      handleCloseDeleteDialog();
      loadData();
    } catch (error: any) {
      showSnackbar(error.response?.data?.message || '삭제 실패', 'error');
    }
  };

  const columns: GridColDef[] = [
    { field: 'workOrderNo', headerName: '작업지시 번호', width: 150 },
    {
      field: 'resultDate',
      headerName: '실적 일자',
      width: 160,
      valueFormatter: (params) => new Date(params.value).toLocaleString('ko-KR', {
        year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit'
      }),
    },
    {
      field: 'quantity',
      headerName: '총 수량',
      width: 100,
      type: 'number',
      valueFormatter: (params) => params.value.toLocaleString(),
    },
    {
      field: 'goodQuantity',
      headerName: '양품',
      width: 100,
      type: 'number',
      valueFormatter: (params) => params.value.toLocaleString(),
      cellClassName: 'text-success',
    },
    {
      field: 'defectQuantity',
      headerName: '불량',
      width: 100,
      type: 'number',
      valueFormatter: (params) => params.value.toLocaleString(),
      cellClassName: (params) => params.value > 0 ? 'text-error' : '',
    },
    {
      field: 'workDuration',
      headerName: '작업 시간',
      width: 110,
      valueFormatter: (params) => params.value ? `${params.value}분` : '-',
    },
    { field: 'workerName', headerName: '작업자', width: 120 },
    { field: 'defectReason', headerName: '불량 사유', width: 200 },
    {
      field: 'createdAt',
      headerName: '등록일',
      width: 160,
      valueFormatter: (params) => new Date(params.value).toLocaleString('ko-KR'),
    },
    {
      field: 'actions',
      type: 'actions',
      headerName: '작업',
      width: 120,
      getActions: (params: GridRowParams<WorkResult>) => [
        <GridActionsCellItem
          icon={<EditIcon />}
          label="수정"
          onClick={() => handleOpenDialog(params.row)}
        />,
        <GridActionsCellItem
          icon={<DeleteIcon />}
          label="삭제"
          onClick={() => handleOpenDeleteDialog(params.row)}
        />,
      ],
    },
  ];

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h5" component="h1">
          작업 실적 관리
        </Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => handleOpenDialog()}
          disabled={workOrders.length === 0}
        >
          신규 등록
        </Button>
      </Box>

      {workOrders.length === 0 && (
        <Alert severity="info" sx={{ mb: 2 }}>
          진행 중인 작업지시가 없습니다. 먼저 작업지시를 생성하고 시작해주세요.
        </Alert>
      )}

      <Paper>
        <DataGrid
          rows={workResults}
          columns={columns}
          getRowId={(row) => row.workResultId}
          loading={loading}
          autoHeight
          pageSizeOptions={[10, 25, 50, 100]}
          initialState={{
            pagination: { paginationModel: { pageSize: 25 } },
            sorting: {
              sortModel: [{ field: 'resultDate', sort: 'desc' }],
            },
          }}
          disableRowSelectionOnClick
          sx={{
            '& .MuiDataGrid-cell': {
              borderBottom: '1px solid rgba(224, 224, 224, 1)',
            },
            '& .text-error': {
              color: 'error.main',
            },
            '& .text-success': {
              color: 'success.main',
            },
          }}
        />
      </Paper>

      {/* Create/Edit Dialog */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="md" fullWidth>
        <DialogTitle>{selectedWorkResult ? '작업실적 수정' : '신규 작업실적 등록'}</DialogTitle>
        <DialogContent>
          <Box display="flex" flexDirection="column" gap={2} mt={1}>
            {!selectedWorkResult && (
              <FormControl fullWidth required>
                <InputLabel>작업지시</InputLabel>
                <Select
                  name="workOrderId"
                  value={(formData as WorkResultCreateRequest).workOrderId || ''}
                  onChange={handleSelectChange}
                  label="작업지시"
                >
                  {workOrders.map((wo) => (
                    <MenuItem key={wo.workOrderId} value={wo.workOrderId}>
                      {wo.workOrderNo} - {wo.productName} ({wo.processName})
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            )}

            <TextField
              name="resultDate"
              label="실적 일자"
              type="datetime-local"
              value={formData.resultDate || ''}
              onChange={handleInputChange}
              required
              fullWidth
              InputLabelProps={{ shrink: true }}
            />

            <Alert severity="info">
              총 수량 = 양품 + 불량 (자동 검증됩니다)
            </Alert>

            <Grid container spacing={2}>
              <Grid item xs={4}>
                <TextField
                  name="quantity"
                  label="총 수량"
                  type="number"
                  value={formData.quantity || 0}
                  onChange={handleInputChange}
                  required
                  fullWidth
                />
              </Grid>
              <Grid item xs={4}>
                <TextField
                  name="goodQuantity"
                  label="양품 수량"
                  type="number"
                  value={formData.goodQuantity || 0}
                  onChange={handleInputChange}
                  required
                  fullWidth
                />
              </Grid>
              <Grid item xs={4}>
                <TextField
                  name="defectQuantity"
                  label="불량 수량"
                  type="number"
                  value={formData.defectQuantity || 0}
                  onChange={handleInputChange}
                  required
                  fullWidth
                />
              </Grid>
            </Grid>

            <Grid container spacing={2}>
              <Grid item xs={6}>
                <TextField
                  name="workStartTime"
                  label="작업 시작 시간"
                  type="datetime-local"
                  value={formData.workStartTime || ''}
                  onChange={handleInputChange}
                  required
                  fullWidth
                  InputLabelProps={{ shrink: true }}
                />
              </Grid>
              <Grid item xs={6}>
                <TextField
                  name="workEndTime"
                  label="작업 종료 시간"
                  type="datetime-local"
                  value={formData.workEndTime || ''}
                  onChange={handleInputChange}
                  required
                  fullWidth
                  InputLabelProps={{ shrink: true }}
                  helperText="작업 시간은 자동 계산됩니다"
                />
              </Grid>
            </Grid>

            <TextField
              name="workerName"
              label="작업자명"
              value={formData.workerName || ''}
              onChange={handleInputChange}
              fullWidth
            />

            <TextField
              name="defectReason"
              label="불량 사유"
              value={formData.defectReason || ''}
              onChange={handleInputChange}
              multiline
              rows={2}
              fullWidth
              placeholder="불량이 발생한 경우 사유를 입력하세요"
            />

            <TextField
              name="remarks"
              label="비고"
              value={formData.remarks || ''}
              onChange={handleInputChange}
              multiline
              rows={2}
              fullWidth
            />

            <Alert severity="success">
              실적 등록/수정/삭제 시 해당 작업지시의 집계가 자동으로 업데이트됩니다.
            </Alert>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>취소</Button>
          <Button onClick={handleSubmit} variant="contained">
            {selectedWorkResult ? '수정' : '등록'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={openDeleteDialog} onClose={handleCloseDeleteDialog}>
        <DialogTitle>작업실적 삭제 확인</DialogTitle>
        <DialogContent>
          <Alert severity="warning" sx={{ mb: 2 }}>
            이 작업은 되돌릴 수 없습니다. 삭제 시 작업지시의 집계가 자동으로 재계산됩니다.
          </Alert>
          <Typography>
            작업지시 <strong>{selectedWorkResult?.workOrderNo}</strong>의 실적을 삭제하시겠습니까?
          </Typography>
          <Typography variant="body2" color="text.secondary" mt={1}>
            수량: {selectedWorkResult?.quantity} (양품: {selectedWorkResult?.goodQuantity}, 불량: {selectedWorkResult?.defectQuantity})
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDeleteDialog}>취소</Button>
          <Button onClick={handleDelete} color="error" variant="contained">
            삭제
          </Button>
        </DialogActions>
      </Dialog>

      {/* Snackbar for notifications */}
      <Snackbar
        open={snackbar.open}
        autoHideDuration={4000}
        onClose={handleCloseSnackbar}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
      >
        <Alert onClose={handleCloseSnackbar} severity={snackbar.severity} sx={{ width: '100%' }}>
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Box>
  );
};

export default WorkResultsPage;
