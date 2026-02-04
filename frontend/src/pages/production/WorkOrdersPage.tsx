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
  Chip,
  Alert,
  Snackbar,
  MenuItem,
  Select,
  FormControl,
  InputLabel,
  SelectChangeEvent,
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
  PlayArrow as StartIcon,
  CheckCircle as CompleteIcon,
  Cancel as CancelIcon,
} from '@mui/icons-material';
import workOrderService, { WorkOrder, WorkOrderCreateRequest, WorkOrderUpdateRequest } from '../../services/workOrderService';
import productService, { Product } from '../../services/productService';
import processService, { Process } from '../../services/processService';

const WorkOrdersPage: React.FC = () => {
  const [workOrders, setWorkOrders] = useState<WorkOrder[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [processes, setProcesses] = useState<Process[]>([]);
  const [loading, setLoading] = useState(true);
  const [openDialog, setOpenDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [selectedWorkOrder, setSelectedWorkOrder] = useState<WorkOrder | null>(null);
  const [formData, setFormData] = useState<WorkOrderCreateRequest | WorkOrderUpdateRequest>({
    workOrderNo: '',
    productId: 0,
    processId: 0,
    plannedQuantity: 0,
    plannedStartDate: '',
    plannedEndDate: '',
    priority: '5',
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
      const [workOrdersData, productsData, processesData] = await Promise.all([
        workOrderService.getWorkOrders(),
        productService.getActiveProducts(),
        processService.getActiveProcesses(),
      ]);
      setWorkOrders(workOrdersData);
      setProducts(productsData);
      setProcesses(processesData);
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

  const handleOpenDialog = (workOrder?: WorkOrder) => {
    if (workOrder) {
      setSelectedWorkOrder(workOrder);
      setFormData({
        productId: workOrder.productId,
        processId: workOrder.processId,
        plannedQuantity: workOrder.plannedQuantity,
        plannedStartDate: workOrder.plannedStartDate.slice(0, 16),
        plannedEndDate: workOrder.plannedEndDate.slice(0, 16),
        priority: workOrder.priority || '5',
        remarks: workOrder.remarks || '',
      });
    } else {
      setSelectedWorkOrder(null);
      const now = new Date();
      const tomorrow = new Date(now.getTime() + 24 * 60 * 60 * 1000);
      setFormData({
        workOrderNo: '',
        productId: products[0]?.productId || 0,
        processId: processes[0]?.processId || 0,
        plannedQuantity: 0,
        plannedStartDate: now.toISOString().slice(0, 16),
        plannedEndDate: tomorrow.toISOString().slice(0, 16),
        priority: '5',
        remarks: '',
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedWorkOrder(null);
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: name === 'plannedQuantity' ? Number(value) : value,
    });
  };

  const handleSelectChange = (e: SelectChangeEvent<number | string>) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name as string]: name === 'productId' || name === 'processId' ? Number(value) : value,
    });
  };

  const handleSubmit = async () => {
    try {
      if (selectedWorkOrder) {
        await workOrderService.updateWorkOrder(selectedWorkOrder.workOrderId, formData as WorkOrderUpdateRequest);
        showSnackbar('작업지시 수정 성공', 'success');
      } else {
        await workOrderService.createWorkOrder(formData as WorkOrderCreateRequest);
        showSnackbar('작업지시 생성 성공', 'success');
      }
      handleCloseDialog();
      loadData();
    } catch (error: any) {
      showSnackbar(error.response?.data?.message || '작업 실패', 'error');
    }
  };

  const handleStartWorkOrder = async (workOrder: WorkOrder) => {
    try {
      await workOrderService.startWorkOrder(workOrder.workOrderId);
      showSnackbar('작업 시작 성공', 'success');
      loadData();
    } catch (error: any) {
      showSnackbar(error.response?.data?.message || '작업 시작 실패', 'error');
    }
  };

  const handleCompleteWorkOrder = async (workOrder: WorkOrder) => {
    try {
      await workOrderService.completeWorkOrder(workOrder.workOrderId);
      showSnackbar('작업 완료 성공', 'success');
      loadData();
    } catch (error: any) {
      showSnackbar(error.response?.data?.message || '작업 완료 실패', 'error');
    }
  };

  const handleCancelWorkOrder = async (workOrder: WorkOrder) => {
    try {
      await workOrderService.cancelWorkOrder(workOrder.workOrderId);
      showSnackbar('작업 취소 성공', 'success');
      loadData();
    } catch (error: any) {
      showSnackbar(error.response?.data?.message || '작업 취소 실패', 'error');
    }
  };

  const handleOpenDeleteDialog = (workOrder: WorkOrder) => {
    setSelectedWorkOrder(workOrder);
    setOpenDeleteDialog(true);
  };

  const handleCloseDeleteDialog = () => {
    setOpenDeleteDialog(false);
    setSelectedWorkOrder(null);
  };

  const handleDelete = async () => {
    if (!selectedWorkOrder) return;

    try {
      await workOrderService.deleteWorkOrder(selectedWorkOrder.workOrderId);
      showSnackbar('작업지시 삭제 성공', 'success');
      handleCloseDeleteDialog();
      loadData();
    } catch (error: any) {
      showSnackbar(error.response?.data?.message || '삭제 실패', 'error');
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'PENDING': return 'default';
      case 'READY': return 'info';
      case 'IN_PROGRESS': return 'primary';
      case 'COMPLETED': return 'success';
      case 'CANCELLED': return 'error';
      default: return 'default';
    }
  };

  const getStatusLabel = (status: string) => {
    switch (status) {
      case 'PENDING': return '대기';
      case 'READY': return '준비';
      case 'IN_PROGRESS': return '진행중';
      case 'COMPLETED': return '완료';
      case 'CANCELLED': return '취소';
      default: return status;
    }
  };

  const columns: GridColDef[] = [
    { field: 'workOrderNo', headerName: '작업지시 번호', width: 150 },
    {
      field: 'status',
      headerName: '상태',
      width: 100,
      renderCell: (params) => (
        <Chip
          label={getStatusLabel(params.value)}
          color={getStatusColor(params.value)}
          size="small"
        />
      ),
    },
    { field: 'productName', headerName: '제품명', width: 180 },
    { field: 'processName', headerName: '공정명', width: 150 },
    {
      field: 'plannedQuantity',
      headerName: '계획 수량',
      width: 110,
      type: 'number',
      valueFormatter: (params) => params.value.toLocaleString(),
    },
    {
      field: 'actualQuantity',
      headerName: '실적 수량',
      width: 110,
      type: 'number',
      valueFormatter: (params) => params.value.toLocaleString(),
    },
    {
      field: 'goodQuantity',
      headerName: '양품',
      width: 100,
      type: 'number',
      valueFormatter: (params) => params.value.toLocaleString(),
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
      field: 'plannedStartDate',
      headerName: '계획 시작일',
      width: 160,
      valueFormatter: (params) => new Date(params.value).toLocaleString('ko-KR', {
        year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit'
      }),
    },
    {
      field: 'actions',
      type: 'actions',
      headerName: '작업',
      width: 180,
      getActions: (params: GridRowParams<WorkOrder>) => {
        const actions = [];

        if (params.row.status === 'PENDING' || params.row.status === 'READY') {
          actions.push(
            <GridActionsCellItem
              icon={<StartIcon />}
              label="시작"
              onClick={() => handleStartWorkOrder(params.row)}
              showInMenu
            />
          );
        }

        if (params.row.status === 'IN_PROGRESS') {
          actions.push(
            <GridActionsCellItem
              icon={<CompleteIcon />}
              label="완료"
              onClick={() => handleCompleteWorkOrder(params.row)}
              showInMenu
            />
          );
        }

        if (params.row.status !== 'COMPLETED' && params.row.status !== 'CANCELLED') {
          actions.push(
            <GridActionsCellItem
              icon={<CancelIcon />}
              label="취소"
              onClick={() => handleCancelWorkOrder(params.row)}
              showInMenu
            />
          );
        }

        actions.push(
          <GridActionsCellItem
            icon={<EditIcon />}
            label="수정"
            onClick={() => handleOpenDialog(params.row)}
            showInMenu
          />,
          <GridActionsCellItem
            icon={<DeleteIcon />}
            label="삭제"
            onClick={() => handleOpenDeleteDialog(params.row)}
            showInMenu
          />
        );

        return actions;
      },
    },
  ];

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h5" component="h1">
          작업 지시 관리
        </Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => handleOpenDialog()}
        >
          신규 등록
        </Button>
      </Box>

      <Paper>
        <DataGrid
          rows={workOrders}
          columns={columns}
          getRowId={(row) => row.workOrderId}
          loading={loading}
          autoHeight
          pageSizeOptions={[10, 25, 50, 100]}
          initialState={{
            pagination: { paginationModel: { pageSize: 25 } },
            sorting: {
              sortModel: [{ field: 'plannedStartDate', sort: 'desc' }],
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
          }}
        />
      </Paper>

      {/* Create/Edit Dialog */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="md" fullWidth>
        <DialogTitle>{selectedWorkOrder ? '작업지시 수정' : '신규 작업지시 등록'}</DialogTitle>
        <DialogContent>
          <Box display="flex" flexDirection="column" gap={2} mt={1}>
            {!selectedWorkOrder && (
              <TextField
                name="workOrderNo"
                label="작업지시 번호"
                value={(formData as WorkOrderCreateRequest).workOrderNo || ''}
                onChange={handleInputChange}
                required
                fullWidth
              />
            )}

            <FormControl fullWidth required>
              <InputLabel>제품</InputLabel>
              <Select
                name="productId"
                value={formData.productId || ''}
                onChange={handleSelectChange}
                label="제품"
              >
                {products.map((product) => (
                  <MenuItem key={product.productId} value={product.productId}>
                    {product.productCode} - {product.productName}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>

            <FormControl fullWidth required>
              <InputLabel>공정</InputLabel>
              <Select
                name="processId"
                value={formData.processId || ''}
                onChange={handleSelectChange}
                label="공정"
              >
                {processes.map((process) => (
                  <MenuItem key={process.processId} value={process.processId}>
                    {process.processCode} - {process.processName}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>

            <TextField
              name="plannedQuantity"
              label="계획 수량"
              type="number"
              value={formData.plannedQuantity || 0}
              onChange={handleInputChange}
              required
              fullWidth
            />

            <TextField
              name="plannedStartDate"
              label="계획 시작일"
              type="datetime-local"
              value={formData.plannedStartDate || ''}
              onChange={handleInputChange}
              required
              fullWidth
              InputLabelProps={{ shrink: true }}
            />

            <TextField
              name="plannedEndDate"
              label="계획 종료일"
              type="datetime-local"
              value={formData.plannedEndDate || ''}
              onChange={handleInputChange}
              required
              fullWidth
              InputLabelProps={{ shrink: true }}
            />

            <FormControl fullWidth>
              <InputLabel>우선순위</InputLabel>
              <Select
                name="priority"
                value={formData.priority || '5'}
                onChange={handleSelectChange}
                label="우선순위"
              >
                <MenuItem value="1">높음 (1)</MenuItem>
                <MenuItem value="3">보통 높음 (3)</MenuItem>
                <MenuItem value="5">보통 (5)</MenuItem>
                <MenuItem value="7">보통 낮음 (7)</MenuItem>
                <MenuItem value="10">낮음 (10)</MenuItem>
              </Select>
            </FormControl>

            <TextField
              name="remarks"
              label="비고"
              value={formData.remarks || ''}
              onChange={handleInputChange}
              multiline
              rows={3}
              fullWidth
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>취소</Button>
          <Button onClick={handleSubmit} variant="contained">
            {selectedWorkOrder ? '수정' : '등록'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={openDeleteDialog} onClose={handleCloseDeleteDialog}>
        <DialogTitle>작업지시 삭제 확인</DialogTitle>
        <DialogContent>
          <Alert severity="warning" sx={{ mb: 2 }}>
            이 작업은 되돌릴 수 없습니다.
          </Alert>
          <Typography>
            작업지시 <strong>{selectedWorkOrder?.workOrderNo}</strong>을(를) 삭제하시겠습니까?
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
        autoHideDuration={3000}
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

export default WorkOrdersPage;
