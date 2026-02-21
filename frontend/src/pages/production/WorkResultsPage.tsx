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
import { useTranslation } from 'react-i18next';
import workResultService, { WorkResult, WorkResultCreateRequest, WorkResultUpdateRequest } from '../../services/workResultService';
import workOrderService, { WorkOrder } from '../../services/workOrderService';
import { getErrorMessage } from '@/utils/errorUtils';

const WorkResultsPage: React.FC = () => {
  const { t } = useTranslation();
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
      showSnackbar(t('pages.workResults.errors.loadFailed'), 'error');
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
        showSnackbar(t('pages.workResults.errors.quantityMismatch'), 'error');
        return;
      }

      if (selectedWorkResult) {
        await workResultService.updateWorkResult(selectedWorkResult.workResultId, formData as WorkResultUpdateRequest);
        showSnackbar(t('pages.workResults.messages.updateSuccess'), 'success');
      } else {
        await workResultService.createWorkResult(formData as WorkResultCreateRequest);
        showSnackbar(t('pages.workResults.messages.createSuccess'), 'success');
      }
      handleCloseDialog();
      loadData();
    } catch (error) {
      showSnackbar(getErrorMessage(error, t('pages.workResults.errors.operationFailed')), 'error');
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
      showSnackbar(t('pages.workResults.messages.deleteSuccess'), 'success');
      handleCloseDeleteDialog();
      loadData();
    } catch (error) {
      showSnackbar(getErrorMessage(error, t('pages.workResults.errors.deleteFailed')), 'error');
    }
  };

  const columns: GridColDef[] = [
    { field: 'workOrderNo', headerName: t('pages.workResults.fields.workOrderNo'), width: 150 },
    {
      field: 'resultDate',
      headerName: t('pages.workResults.fields.resultDate'),
      width: 160,
      valueFormatter: (params) => new Date(params.value).toLocaleString('ko-KR', {
        year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit'
      }),
    },
    {
      field: 'quantity',
      headerName: t('pages.workResults.fields.totalQuantity'),
      width: 100,
      type: 'number',
      valueFormatter: (params) => params.value.toLocaleString(),
    },
    {
      field: 'goodQuantity',
      headerName: t('pages.workResults.fields.goodQuantity'),
      width: 100,
      type: 'number',
      valueFormatter: (params) => params.value.toLocaleString(),
      cellClassName: 'text-success',
    },
    {
      field: 'defectQuantity',
      headerName: t('pages.workResults.fields.defectQuantity'),
      width: 100,
      type: 'number',
      valueFormatter: (params) => params.value.toLocaleString(),
      cellClassName: (params) => params.value > 0 ? 'text-error' : '',
    },
    {
      field: 'workDuration',
      headerName: t('pages.workResults.fields.workDuration'),
      width: 110,
      valueFormatter: (params) => params.value ? `${params.value}분` : '-',
    },
    { field: 'workerName', headerName: t('pages.workResults.fields.workerName'), width: 120 },
    { field: 'defectReason', headerName: t('pages.workResults.fields.defectReason'), width: 200 },
    {
      field: 'createdAt',
      headerName: t('common.labels.createdAt'),
      width: 160,
      valueFormatter: (params) => new Date(params.value).toLocaleString('ko-KR'),
    },
    {
      field: 'actions',
      type: 'actions',
      headerName: t('common.labels.actions'),
      width: 120,
      getActions: (params: GridRowParams<WorkResult>) => [
        <GridActionsCellItem
          icon={<EditIcon />}
          label={t('common.buttons.edit')}
          onClick={() => handleOpenDialog(params.row)}
        />,
        <GridActionsCellItem
          icon={<DeleteIcon />}
          label={t('common.buttons.delete')}
          onClick={() => handleOpenDeleteDialog(params.row)}
        />,
      ],
    },
  ];

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h5" component="h1">
          {t('pages.workResults.title')}
        </Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => handleOpenDialog()}
          disabled={workOrders.length === 0}
        >
          {t('pages.workResults.actions.create')}
        </Button>
      </Box>

      {workOrders.length === 0 && (
        <Alert severity="info" sx={{ mb: 2 }}>
          {t('pages.workResults.messages.noWorkOrders')}
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
        <DialogTitle>{selectedWorkResult ? t('pages.workResults.dialog.editTitle') : t('pages.workResults.dialog.createTitle')}</DialogTitle>
        <DialogContent>
          <Box display="flex" flexDirection="column" gap={2} mt={1}>
            {!selectedWorkResult && (
              <FormControl fullWidth required>
                <InputLabel>{t('pages.workResults.fields.workOrder')}</InputLabel>
                <Select
                  name="workOrderId"
                  value={(formData as WorkResultCreateRequest).workOrderId || ''}
                  onChange={handleSelectChange}
                  label={t('pages.workResults.fields.workOrder')}
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
              label={t('pages.workResults.fields.resultDate')}
              type="datetime-local"
              value={formData.resultDate || ''}
              onChange={handleInputChange}
              required
              fullWidth
              InputLabelProps={{ shrink: true }}
            />

            <Alert severity="info">
              {t('pages.workResults.messages.quantityValidation')}
            </Alert>

            <Grid container spacing={2}>
              <Grid item xs={4}>
                <TextField
                  name="quantity"
                  label={t('pages.workResults.fields.totalQuantity')}
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
                  label={t('pages.workResults.fields.goodQuantity')}
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
                  label={t('pages.workResults.fields.defectQuantity')}
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
                  label={t('pages.workResults.fields.workStartTime')}
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
                  label={t('pages.workResults.fields.workEndTime')}
                  type="datetime-local"
                  value={formData.workEndTime || ''}
                  onChange={handleInputChange}
                  required
                  fullWidth
                  InputLabelProps={{ shrink: true }}
                  helperText={t('pages.workResults.messages.autoCalculated')}
                />
              </Grid>
            </Grid>

            <TextField
              name="workerName"
              label={t('pages.workResults.fields.workerName')}
              value={formData.workerName || ''}
              onChange={handleInputChange}
              fullWidth
            />

            <TextField
              name="defectReason"
              label={t('pages.workResults.fields.defectReason')}
              value={formData.defectReason || ''}
              onChange={handleInputChange}
              multiline
              rows={2}
              fullWidth
              placeholder={t('pages.workResults.placeholders.defectReason')}
            />

            <TextField
              name="remarks"
              label={t('common.labels.remarks')}
              value={formData.remarks || ''}
              onChange={handleInputChange}
              multiline
              rows={2}
              fullWidth
            />

            <Alert severity="success">
              {t('pages.workResults.messages.autoAggregate')}
            </Alert>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>{t('common.buttons.cancel')}</Button>
          <Button onClick={handleSubmit} variant="contained">
            {selectedWorkResult ? t('common.buttons.edit') : t('pages.workResults.actions.register')}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={openDeleteDialog} onClose={handleCloseDeleteDialog}>
        <DialogTitle>{t('pages.workResults.dialog.deleteTitle')}</DialogTitle>
        <DialogContent>
          <Alert severity="warning" sx={{ mb: 2 }}>
            {t('pages.workResults.messages.deleteWarning')}
          </Alert>
          <Typography>
            {t('pages.workResults.messages.confirmDelete', { name: selectedWorkResult?.workOrderNo })}
          </Typography>
          <Typography variant="body2" color="text.secondary" mt={1}>
            {t('pages.workResults.fields.totalQuantity')}: {selectedWorkResult?.quantity} ({t('pages.workResults.fields.goodQuantity')}: {selectedWorkResult?.goodQuantity}, {t('pages.workResults.fields.defectQuantity')}: {selectedWorkResult?.defectQuantity})
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
