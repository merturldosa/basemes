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
import { useTranslation } from 'react-i18next';
import workOrderService, { WorkOrder, WorkOrderCreateRequest, WorkOrderUpdateRequest } from '../../services/workOrderService';
import productService, { Product } from '../../services/productService';
import processService, { Process } from '../../services/processService';
import { getErrorMessage } from '@/utils/errorUtils';

const WorkOrdersPage: React.FC = () => {
  const { t } = useTranslation();
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
      setWorkOrders(workOrdersData || []);
      setProducts(productsData || []);
      setProcesses(processesData || []);
    } catch (error) {
      showSnackbar(t('pages.workOrders.errors.loadFailed'), 'error');
      setWorkOrders([]);
      setProducts([]);
      setProcesses([]);
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
        showSnackbar(t('pages.workOrders.messages.updateSuccess'), 'success');
      } else {
        await workOrderService.createWorkOrder(formData as WorkOrderCreateRequest);
        showSnackbar(t('pages.workOrders.messages.createSuccess'), 'success');
      }
      handleCloseDialog();
      loadData();
    } catch (error) {
      showSnackbar(getErrorMessage(error, t('pages.workOrders.errors.operationFailed')), 'error');
    }
  };

  const handleStartWorkOrder = async (workOrder: WorkOrder) => {
    try {
      await workOrderService.startWorkOrder(workOrder.workOrderId);
      showSnackbar(t('pages.workOrders.messages.startSuccess'), 'success');
      loadData();
    } catch (error) {
      showSnackbar(getErrorMessage(error, t('pages.workOrders.errors.startFailed')), 'error');
    }
  };

  const handleCompleteWorkOrder = async (workOrder: WorkOrder) => {
    try {
      await workOrderService.completeWorkOrder(workOrder.workOrderId);
      showSnackbar(t('pages.workOrders.messages.completeSuccess'), 'success');
      loadData();
    } catch (error) {
      showSnackbar(getErrorMessage(error, t('pages.workOrders.errors.completeFailed')), 'error');
    }
  };

  const handleCancelWorkOrder = async (workOrder: WorkOrder) => {
    try {
      await workOrderService.cancelWorkOrder(workOrder.workOrderId);
      showSnackbar(t('pages.workOrders.messages.cancelSuccess'), 'success');
      loadData();
    } catch (error) {
      showSnackbar(getErrorMessage(error, t('pages.workOrders.errors.cancelFailed')), 'error');
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
      showSnackbar(t('pages.workOrders.messages.deleteSuccess'), 'success');
      handleCloseDeleteDialog();
      loadData();
    } catch (error) {
      showSnackbar(getErrorMessage(error, t('pages.workOrders.errors.deleteFailed')), 'error');
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
      case 'PENDING': return t('workOrder.status.pending');
      case 'READY': return t('workOrder.status.ready');
      case 'IN_PROGRESS': return t('workOrder.status.inProgress');
      case 'COMPLETED': return t('workOrder.status.completed');
      case 'CANCELLED': return t('workOrder.status.cancelled');
      default: return status;
    }
  };

  const columns: GridColDef[] = [
    { field: 'workOrderNo', headerName: t('pages.workOrders.fields.workOrderNo'), width: 150 },
    {
      field: 'status',
      headerName: t('common.labels.status'),
      width: 100,
      renderCell: (params) => (
        <Chip
          label={getStatusLabel(params.value)}
          color={getStatusColor(params.value)}
          size="small"
        />
      ),
    },
    { field: 'productName', headerName: t('pages.workOrders.fields.productName'), width: 180 },
    { field: 'processName', headerName: t('pages.workOrders.fields.processName'), width: 150 },
    {
      field: 'plannedQuantity',
      headerName: t('pages.workOrders.fields.plannedQuantity'),
      width: 110,
      type: 'number',
      valueFormatter: (params) => params.value.toLocaleString(),
    },
    {
      field: 'actualQuantity',
      headerName: t('pages.workOrders.fields.actualQuantity'),
      width: 110,
      type: 'number',
      valueFormatter: (params) => params.value.toLocaleString(),
    },
    {
      field: 'goodQuantity',
      headerName: t('pages.workOrders.fields.goodQuantity'),
      width: 100,
      type: 'number',
      valueFormatter: (params) => params.value.toLocaleString(),
    },
    {
      field: 'defectQuantity',
      headerName: t('pages.workOrders.fields.defectQuantity'),
      width: 100,
      type: 'number',
      valueFormatter: (params) => params.value.toLocaleString(),
      cellClassName: (params) => params.value > 0 ? 'text-error' : '',
    },
    {
      field: 'plannedStartDate',
      headerName: t('pages.workOrders.fields.plannedStartDate'),
      width: 160,
      valueFormatter: (params) => new Date(params.value).toLocaleString('ko-KR', {
        year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit'
      }),
    },
    {
      field: 'actions',
      type: 'actions',
      headerName: t('common.labels.actions'),
      width: 180,
      getActions: (params: GridRowParams<WorkOrder>) => {
        const actions = [];

        if (params.row.status === 'PENDING' || params.row.status === 'READY') {
          actions.push(
            <GridActionsCellItem
              icon={<StartIcon />}
              label={t('pages.workOrders.actions.start')}
              onClick={() => handleStartWorkOrder(params.row)}
              showInMenu
            />
          );
        }

        if (params.row.status === 'IN_PROGRESS') {
          actions.push(
            <GridActionsCellItem
              icon={<CompleteIcon />}
              label={t('pages.workOrders.actions.complete')}
              onClick={() => handleCompleteWorkOrder(params.row)}
              showInMenu
            />
          );
        }

        if (params.row.status !== 'COMPLETED' && params.row.status !== 'CANCELLED') {
          actions.push(
            <GridActionsCellItem
              icon={<CancelIcon />}
              label={t('pages.workOrders.actions.cancel')}
              onClick={() => handleCancelWorkOrder(params.row)}
              showInMenu
            />
          );
        }

        actions.push(
          <GridActionsCellItem
            icon={<EditIcon />}
            label={t('common.buttons.edit')}
            onClick={() => handleOpenDialog(params.row)}
            showInMenu
          />,
          <GridActionsCellItem
            icon={<DeleteIcon />}
            label={t('common.buttons.delete')}
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
          {t('pages.workOrders.title')}
        </Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => handleOpenDialog()}
        >
          {t('pages.workOrders.actions.create')}
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
        <DialogTitle>{selectedWorkOrder ? t('pages.workOrders.dialog.editTitle') : t('pages.workOrders.dialog.createTitle')}</DialogTitle>
        <DialogContent>
          <Box display="flex" flexDirection="column" gap={2} mt={1}>
            {!selectedWorkOrder && (
              <TextField
                name="workOrderNo"
                label={t('pages.workOrders.fields.workOrderNo')}
                value={(formData as WorkOrderCreateRequest).workOrderNo || ''}
                onChange={handleInputChange}
                required
                fullWidth
              />
            )}

            <FormControl fullWidth required>
              <InputLabel>{t('pages.workOrders.fields.product')}</InputLabel>
              <Select
                name="productId"
                value={formData.productId || ''}
                onChange={handleSelectChange}
                label={t('pages.workOrders.fields.product')}
              >
                {products.map((product) => (
                  <MenuItem key={product.productId} value={product.productId}>
                    {product.productCode} - {product.productName}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>

            <FormControl fullWidth required>
              <InputLabel>{t('pages.workOrders.fields.process')}</InputLabel>
              <Select
                name="processId"
                value={formData.processId || ''}
                onChange={handleSelectChange}
                label={t('pages.workOrders.fields.process')}
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
              label={t('pages.workOrders.fields.plannedQuantity')}
              type="number"
              value={formData.plannedQuantity || 0}
              onChange={handleInputChange}
              required
              fullWidth
            />

            <TextField
              name="plannedStartDate"
              label={t('pages.workOrders.fields.plannedStartDate')}
              type="datetime-local"
              value={formData.plannedStartDate || ''}
              onChange={handleInputChange}
              required
              fullWidth
              InputLabelProps={{ shrink: true }}
            />

            <TextField
              name="plannedEndDate"
              label={t('pages.workOrders.fields.plannedEndDate')}
              type="datetime-local"
              value={formData.plannedEndDate || ''}
              onChange={handleInputChange}
              required
              fullWidth
              InputLabelProps={{ shrink: true }}
            />

            <FormControl fullWidth>
              <InputLabel>{t('pages.workOrders.fields.priority')}</InputLabel>
              <Select
                name="priority"
                value={formData.priority || '5'}
                onChange={handleSelectChange}
                label={t('pages.workOrders.fields.priority')}
              >
                <MenuItem value="1">{t('pages.workOrders.priority.high')} (1)</MenuItem>
                <MenuItem value="3">{t('pages.workOrders.priority.mediumHigh')} (3)</MenuItem>
                <MenuItem value="5">{t('pages.workOrders.priority.medium')} (5)</MenuItem>
                <MenuItem value="7">{t('pages.workOrders.priority.mediumLow')} (7)</MenuItem>
                <MenuItem value="10">{t('pages.workOrders.priority.low')} (10)</MenuItem>
              </Select>
            </FormControl>

            <TextField
              name="remarks"
              label={t('common.labels.remarks')}
              value={formData.remarks || ''}
              onChange={handleInputChange}
              multiline
              rows={3}
              fullWidth
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>{t('common.buttons.cancel')}</Button>
          <Button onClick={handleSubmit} variant="contained">
            {selectedWorkOrder ? t('common.buttons.edit') : t('pages.workOrders.actions.register')}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={openDeleteDialog} onClose={handleCloseDeleteDialog}>
        <DialogTitle>{t('pages.workOrders.dialog.deleteTitle')}</DialogTitle>
        <DialogContent>
          <Alert severity="warning" sx={{ mb: 2 }}>
            {t('pages.workOrders.messages.irreversible')}
          </Alert>
          <Typography>
            {t('pages.workOrders.messages.confirmDelete', { name: selectedWorkOrder?.workOrderNo })}
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDeleteDialog}>{t('common.buttons.cancel')}</Button>
          <Button onClick={handleDelete} color="error" variant="contained">
            {t('common.buttons.delete')}
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
