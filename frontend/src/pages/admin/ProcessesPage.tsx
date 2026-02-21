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
  ToggleOn as ToggleOnIcon,
  ToggleOff as ToggleOffIcon,
} from '@mui/icons-material';
import { useTranslation } from 'react-i18next';
import processService, { Process, ProcessCreateRequest, ProcessUpdateRequest } from '../../services/processService';

const ProcessesPage: React.FC = () => {
  const { t } = useTranslation();
  const [processes, setProcesses] = useState<Process[]>([]);
  const [loading, setLoading] = useState(true);
  const [openDialog, setOpenDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [selectedProcess, setSelectedProcess] = useState<Process | null>(null);
  const [formData, setFormData] = useState<ProcessCreateRequest | ProcessUpdateRequest>({
    processCode: '',
    processName: '',
    processType: '',
    sequenceOrder: 1,
    remarks: '',
  });
  const [snackbar, setSnackbar] = useState<{ open: boolean; message: string; severity: 'success' | 'error' }>({
    open: false,
    message: '',
    severity: 'success',
  });

  useEffect(() => {
    loadProcesses();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const loadProcesses = async () => {
    try {
      setLoading(true);
      const data = await processService.getProcesses();
      setProcesses(data || []);
    } catch (error) {
      showSnackbar(t('pages.processes.errors.loadFailed'), 'error');
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

  const handleOpenDialog = (process?: Process) => {
    if (process) {
      setSelectedProcess(process);
      setFormData({
        processName: process.processName,
        processType: process.processType || '',
        sequenceOrder: process.sequenceOrder,
        remarks: process.remarks || '',
      });
    } else {
      setSelectedProcess(null);
      setFormData({
        processCode: '',
        processName: '',
        processType: '',
        sequenceOrder: 1,
        remarks: '',
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedProcess(null);
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: name === 'sequenceOrder' && value ? Number(value) : value,
    });
  };

  const handleSubmit = async () => {
    try {
      if (selectedProcess) {
        await processService.updateProcess(selectedProcess.processId, formData as ProcessUpdateRequest);
        showSnackbar(t('pages.processes.messages.updateSuccess'), 'success');
      } else {
        await processService.createProcess(formData as ProcessCreateRequest);
        showSnackbar(t('pages.processes.messages.createSuccess'), 'success');
      }
      handleCloseDialog();
      loadProcesses();
    } catch (error: any) {
      showSnackbar(error.response?.data?.message || t('pages.processes.errors.operationFailed'), 'error');
    }
  };

  const handleToggleActive = async (process: Process) => {
    try {
      if (process.isActive) {
        await processService.deactivateProcess(process.processId);
        showSnackbar(t('pages.processes.messages.deactivateSuccess'), 'success');
      } else {
        await processService.activateProcess(process.processId);
        showSnackbar(t('pages.processes.messages.activateSuccess'), 'success');
      }
      loadProcesses();
    } catch (error: any) {
      showSnackbar(error.response?.data?.message || t('pages.processes.errors.statusChangeFailed'), 'error');
    }
  };

  const handleOpenDeleteDialog = (process: Process) => {
    setSelectedProcess(process);
    setOpenDeleteDialog(true);
  };

  const handleCloseDeleteDialog = () => {
    setOpenDeleteDialog(false);
    setSelectedProcess(null);
  };

  const handleDelete = async () => {
    if (!selectedProcess) return;

    try {
      await processService.deleteProcess(selectedProcess.processId);
      showSnackbar(t('pages.processes.messages.deleteSuccess'), 'success');
      handleCloseDeleteDialog();
      loadProcesses();
    } catch (error: any) {
      showSnackbar(error.response?.data?.message || t('pages.processes.errors.deleteFailed'), 'error');
    }
  };

  const columns: GridColDef[] = [
    { field: 'sequenceOrder', headerName: t('pages.processes.fields.sequence'), width: 80 },
    { field: 'processCode', headerName: t('pages.processes.fields.processCode'), width: 130 },
    { field: 'processName', headerName: t('pages.processes.fields.processName'), flex: 1, minWidth: 200 },
    { field: 'processType', headerName: t('pages.processes.fields.processType'), width: 150 },
    {
      field: 'isActive',
      headerName: t('common.labels.status'),
      width: 100,
      renderCell: (params) => (
        <Chip
          label={params.value ? t('common.status.active') : t('common.status.inactive')}
          color={params.value ? 'success' : 'default'}
          size="small"
        />
      ),
    },
    {
      field: 'createdAt',
      headerName: t('common.labels.createdAt'),
      width: 180,
      valueFormatter: (params) => new Date(params.value).toLocaleString('ko-KR'),
    },
    {
      field: 'actions',
      type: 'actions',
      headerName: t('common.labels.actions'),
      width: 150,
      getActions: (params: GridRowParams<Process>) => [
        <GridActionsCellItem
          icon={<EditIcon />}
          label={t('common.buttons.edit')}
          onClick={() => handleOpenDialog(params.row)}
        />,
        <GridActionsCellItem
          icon={params.row.isActive ? <ToggleOffIcon /> : <ToggleOnIcon />}
          label={params.row.isActive ? t('pages.processes.actions.deactivate') : t('pages.processes.actions.activate')}
          onClick={() => handleToggleActive(params.row)}
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
          {t('pages.processes.title')}
        </Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => handleOpenDialog()}
        >
          {t('pages.processes.actions.create')}
        </Button>
      </Box>

      <Paper>
        <DataGrid
          rows={processes}
          columns={columns}
          getRowId={(row) => row.processId}
          loading={loading}
          autoHeight
          pageSizeOptions={[10, 25, 50, 100]}
          initialState={{
            pagination: { paginationModel: { pageSize: 25 } },
            sorting: {
              sortModel: [{ field: 'sequenceOrder', sort: 'asc' }],
            },
          }}
          disableRowSelectionOnClick
          sx={{
            '& .MuiDataGrid-cell': {
              borderBottom: '1px solid rgba(224, 224, 224, 1)',
            },
          }}
        />
      </Paper>

      {/* Create/Edit Dialog */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="md" fullWidth>
        <DialogTitle>{selectedProcess ? t('pages.processes.dialog.editTitle') : t('pages.processes.dialog.createTitle')}</DialogTitle>
        <DialogContent>
          <Box display="flex" flexDirection="column" gap={2} mt={1}>
            {!selectedProcess && (
              <TextField
                name="processCode"
                label={t('pages.processes.fields.processCode')}
                value={(formData as ProcessCreateRequest).processCode || ''}
                onChange={handleInputChange}
                required
                fullWidth
              />
            )}
            <TextField
              name="processName"
              label={t('pages.processes.fields.processName')}
              value={formData.processName || ''}
              onChange={handleInputChange}
              required
              fullWidth
            />
            <TextField
              name="processType"
              label={t('pages.processes.fields.processType')}
              value={formData.processType || ''}
              onChange={handleInputChange}
              placeholder={t('pages.processes.placeholders.processType')}
              fullWidth
            />
            <TextField
              name="sequenceOrder"
              label={t('pages.processes.fields.sequence')}
              type="number"
              value={formData.sequenceOrder || 1}
              onChange={handleInputChange}
              required
              fullWidth
              helperText={t('pages.processes.placeholders.sequenceHelp')}
            />
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
            {selectedProcess ? t('common.buttons.edit') : t('pages.processes.actions.register')}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={openDeleteDialog} onClose={handleCloseDeleteDialog}>
        <DialogTitle>{t('pages.processes.dialog.deleteTitle')}</DialogTitle>
        <DialogContent>
          <Alert severity="warning" sx={{ mb: 2 }}>
            {t('pages.processes.messages.irreversible')}
          </Alert>
          <Typography>
            {t('pages.processes.messages.confirmDelete', { name: selectedProcess?.processName })}
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

export default ProcessesPage;
