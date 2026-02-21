import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
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
  IconButton,
  MenuItem,
  Select,
  FormControl,
  InputLabel,
  Snackbar,
  Alert,
  Checkbox,
  FormControlLabel,
} from '@mui/material';
import { DataGrid, GridColDef, GridRenderCellParams } from '@mui/x-data-grid';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
} from '@mui/icons-material';
import moldMaintenanceService, { MoldMaintenance, MoldMaintenanceCreateRequest, MoldMaintenanceUpdateRequest } from '../../services/moldMaintenanceService';
import moldService, { Mold } from '../../services/moldService';

const MoldMaintenancesPage: React.FC = () => {
  const { t } = useTranslation();
  const [maintenances, setMaintenances] = useState<MoldMaintenance[]>([]);
  const [molds, setMolds] = useState<Mold[]>([]);
  const [loading, setLoading] = useState(false);
  const [openDialog, setOpenDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [selectedMaintenance, setSelectedMaintenance] = useState<MoldMaintenance | null>(null);
  const [formData, setFormData] = useState<MoldMaintenanceCreateRequest>({
    moldId: 0,
    maintenanceNo: '',
    maintenanceType: 'PERIODIC',
    maintenanceDate: new Date().toISOString().slice(0, 16),
  });
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'success' as 'success' | 'error' | 'warning' | 'info',
  });

  useEffect(() => {
    loadMaintenances();
    loadMolds();
  }, []);

  const loadMaintenances = async () => {
    try {
      setLoading(true);
      const data = await moldMaintenanceService.getAll();
      setMaintenances(data || []);
    } catch (error) {
      showSnackbar(t('pages.moldMaintenances.messages.loadFailed'), 'error');
      setMaintenances([]);
    } finally {
      setLoading(false);
    }
  };

  const loadMolds = async () => {
    try {
      const data = await moldService.getActive();
      setMolds(data || []);
    } catch (error) {
      setMolds([]);
    }
  };

  const handleOpenDialog = (maintenance?: MoldMaintenance) => {
    if (maintenance) {
      setSelectedMaintenance(maintenance);
      setFormData({
        moldId: maintenance.moldId,
        maintenanceNo: maintenance.maintenanceNo,
        maintenanceType: maintenance.maintenanceType,
        maintenanceDate: maintenance.maintenanceDate.slice(0, 16),
        shotCountBefore: maintenance.shotCountBefore,
        shotCountReset: maintenance.shotCountReset,
        shotCountAfter: maintenance.shotCountAfter,
        maintenanceContent: maintenance.maintenanceContent,
        partsReplaced: maintenance.partsReplaced,
        findings: maintenance.findings,
        correctiveAction: maintenance.correctiveAction,
        partsCost: maintenance.partsCost,
        laborCost: maintenance.laborCost,
        laborHours: maintenance.laborHours,
        maintenanceResult: maintenance.maintenanceResult,
        technicianName: maintenance.technicianName,
        remarks: maintenance.remarks,
      });
    } else {
      setSelectedMaintenance(null);
      setFormData({
        moldId: 0,
        maintenanceNo: '',
        maintenanceType: 'PERIODIC',
        maintenanceDate: new Date().toISOString().slice(0, 16),
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedMaintenance(null);
  };

  const handleSubmit = async () => {
    try {
      if (selectedMaintenance) {
        await moldMaintenanceService.update(selectedMaintenance.maintenanceId, formData as MoldMaintenanceUpdateRequest);
        showSnackbar(t('pages.moldMaintenances.messages.updated'), 'success');
      } else {
        await moldMaintenanceService.create(formData);
        showSnackbar(t('pages.moldMaintenances.messages.created'), 'success');
      }
      handleCloseDialog();
      loadMaintenances();
    } catch (error) {
      showSnackbar(t('pages.moldMaintenances.messages.saveFailed'), 'error');
    }
  };

  const handleDelete = async () => {
    if (selectedMaintenance) {
      try {
        await moldMaintenanceService.delete(selectedMaintenance.maintenanceId);
        showSnackbar(t('pages.moldMaintenances.messages.deleted'), 'success');
        setOpenDeleteDialog(false);
        setSelectedMaintenance(null);
        loadMaintenances();
      } catch (error) {
        showSnackbar(t('pages.moldMaintenances.messages.deleteFailed'), 'error');
      }
    }
  };

  const showSnackbar = (message: string, severity: 'success' | 'error' | 'warning' | 'info') => {
    setSnackbar({ open: true, message, severity });
  };

  const getMaintenanceTypeChip = (type: string) => {
    const typeConfig: Record<string, { label: string; color: 'success' | 'error' | 'warning' | 'default' | 'info' }> = {
      DAILY_CHECK: { label: t('pages.moldMaintenances.types.dailyCheck'), color: 'info' },
      PERIODIC: { label: t('pages.moldMaintenances.types.periodic'), color: 'success' },
      SHOT_BASED: { label: t('pages.moldMaintenances.types.shotBased'), color: 'warning' },
      EMERGENCY_REPAIR: { label: t('pages.moldMaintenances.types.emergencyRepair'), color: 'error' },
      OVERHAUL: { label: t('pages.moldMaintenances.types.overhaul'), color: 'warning' },
    };
    const config = typeConfig[type] || { label: type, color: 'default' };
    return <Chip label={config.label} color={config.color} size="small" />;
  };

  const getResultChip = (result?: string) => {
    if (!result) return null;
    const resultConfig: Record<string, { label: string; color: 'success' | 'error' | 'warning' }> = {
      COMPLETED: { label: t('pages.moldMaintenances.results.completed'), color: 'success' },
      PARTIAL: { label: t('pages.moldMaintenances.results.partial'), color: 'warning' },
      FAILED: { label: t('pages.moldMaintenances.results.failed'), color: 'error' },
    };
    const config = resultConfig[result] || { label: result, color: 'warning' };
    return <Chip label={config.label} color={config.color} size="small" />;
  };

  const columns: GridColDef[] = [
    { field: 'maintenanceNo', headerName: t('pages.moldMaintenances.fields.maintenanceNo'), width: 130 },
    { field: 'moldCode', headerName: t('pages.moldMaintenances.fields.moldCode'), width: 120 },
    { field: 'moldName', headerName: t('pages.moldMaintenances.fields.moldName'), width: 150 },
    {
      field: 'maintenanceType',
      headerName: t('pages.moldMaintenances.fields.maintenanceType'),
      width: 120,
      renderCell: (params: GridRenderCellParams) => getMaintenanceTypeChip(params.value),
    },
    {
      field: 'maintenanceDate',
      headerName: t('pages.moldMaintenances.fields.maintenanceDate'),
      width: 160,
      renderCell: (params: GridRenderCellParams) => {
        return new Date(params.value).toLocaleString('ko-KR');
      },
    },
    {
      field: 'shotCountBefore',
      headerName: t('pages.moldMaintenances.fields.shotCountBefore'),
      width: 120,
      renderCell: (params: GridRenderCellParams) => {
        return params.value?.toLocaleString() || '-';
      },
    },
    {
      field: 'shotCountReset',
      headerName: t('pages.moldMaintenances.fields.shotCountReset'),
      width: 100,
      renderCell: (params: GridRenderCellParams) => (
        <Chip
          label={params.value ? t('pages.moldMaintenances.shotReset.reset') : t('pages.moldMaintenances.shotReset.keep')}
          color={params.value ? 'warning' : 'default'}
          size="small"
        />
      ),
    },
    {
      field: 'totalCost',
      headerName: t('pages.moldMaintenances.fields.totalCost'),
      width: 110,
      renderCell: (params: GridRenderCellParams) => {
        return params.value ? `₩${params.value.toLocaleString()}` : '-';
      },
    },
    {
      field: 'laborHours',
      headerName: t('pages.moldMaintenances.fields.laborHours'),
      width: 100,
      renderCell: (params: GridRenderCellParams) => {
        return params.value ? `${params.value}h` : '-';
      },
    },
    { field: 'technicianName', headerName: t('pages.moldMaintenances.fields.technicianName'), width: 100 },
    {
      field: 'maintenanceResult',
      headerName: t('pages.moldMaintenances.fields.result'),
      width: 100,
      renderCell: (params: GridRenderCellParams) => getResultChip(params.value),
    },
    {
      field: 'actions',
      headerName: t('common.labels.actions'),
      width: 120,
      sortable: false,
      renderCell: (params: GridRenderCellParams) => (
        <Box>
          <IconButton
            size="small"
            onClick={() => handleOpenDialog(params.row as MoldMaintenance)}
            color="primary"
          >
            <EditIcon fontSize="small" />
          </IconButton>
          <IconButton
            size="small"
            onClick={() => {
              setSelectedMaintenance(params.row as MoldMaintenance);
              setOpenDeleteDialog(true);
            }}
            color="error"
          >
            <DeleteIcon fontSize="small" />
          </IconButton>
        </Box>
      ),
    },
  ];

  return (
    <Box sx={{ p: 3 }}>
      <Paper sx={{ p: 2, mb: 2 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="h5" component="h1">
            {t('pages.moldMaintenances.title')}
          </Typography>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => handleOpenDialog()}
          >
            {t('pages.moldMaintenances.actions.register')}
          </Button>
        </Box>

        <DataGrid
          rows={maintenances}
          columns={columns}
          loading={loading}
          getRowId={(row) => row.maintenanceId}
          pageSizeOptions={[10, 25, 50, 100]}
          initialState={{
            pagination: { paginationModel: { pageSize: 25 } },
          }}
          autoHeight
          disableRowSelectionOnClick
        />
      </Paper>

      {/* Create/Edit Dialog */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="md" fullWidth>
        <DialogTitle>{selectedMaintenance ? t('pages.moldMaintenances.dialogs.editTitle') : t('pages.moldMaintenances.dialogs.createTitle')}</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
            <FormControl required>
              <InputLabel>{t('pages.moldMaintenances.fields.mold')}</InputLabel>
              <Select
                value={formData.moldId || ''}
                onChange={(e) => setFormData({ ...formData, moldId: Number(e.target.value) })}
                label={t('pages.moldMaintenances.fields.mold')}
                disabled={!!selectedMaintenance}
              >
                {molds.map((mold) => (
                  <MenuItem key={mold.moldId} value={mold.moldId}>
                    {mold.moldCode} - {mold.moldName}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
            <TextField
              label={t('pages.moldMaintenances.fields.maintenanceNo')}
              value={formData.maintenanceNo}
              onChange={(e) => setFormData({ ...formData, maintenanceNo: e.target.value })}
              required
              disabled={!!selectedMaintenance}
            />
            <FormControl required>
              <InputLabel>{t('pages.moldMaintenances.fields.maintenanceType')}</InputLabel>
              <Select
                value={formData.maintenanceType}
                onChange={(e) => setFormData({ ...formData, maintenanceType: e.target.value })}
                label={t('pages.moldMaintenances.fields.maintenanceType')}
              >
                <MenuItem value="DAILY_CHECK">{t('pages.moldMaintenances.types.dailyCheck')}</MenuItem>
                <MenuItem value="PERIODIC">{t('pages.moldMaintenances.types.periodic')}</MenuItem>
                <MenuItem value="SHOT_BASED">{t('pages.moldMaintenances.types.shotBasedMaint')}</MenuItem>
                <MenuItem value="EMERGENCY_REPAIR">{t('pages.moldMaintenances.types.emergencyRepair')}</MenuItem>
                <MenuItem value="OVERHAUL">{t('pages.moldMaintenances.types.overhaul')}</MenuItem>
              </Select>
            </FormControl>
            <TextField
              label={t('pages.moldMaintenances.fields.maintenanceDate')}
              type="datetime-local"
              value={formData.maintenanceDate}
              onChange={(e) => setFormData({ ...formData, maintenanceDate: e.target.value })}
              required
              InputLabelProps={{ shrink: true }}
            />
            <TextField
              label={t('pages.moldMaintenances.fields.shotCountBeforeLabel')}
              type="number"
              value={formData.shotCountBefore || ''}
              onChange={(e) => setFormData({ ...formData, shotCountBefore: parseInt(e.target.value) || undefined })}
            />
            <FormControlLabel
              control={
                <Checkbox
                  checked={formData.shotCountReset || false}
                  onChange={(e) => setFormData({ ...formData, shotCountReset: e.target.checked })}
                />
              }
              label={t('pages.moldMaintenances.fields.shotCountResetLabel')}
            />
            <TextField
              label={t('pages.moldMaintenances.fields.maintenanceContent')}
              value={formData.maintenanceContent || ''}
              onChange={(e) => setFormData({ ...formData, maintenanceContent: e.target.value })}
              multiline
              rows={2}
            />
            <TextField
              label={t('pages.moldMaintenances.fields.partsReplaced')}
              value={formData.partsReplaced || ''}
              onChange={(e) => setFormData({ ...formData, partsReplaced: e.target.value })}
              multiline
              rows={2}
            />
            <TextField
              label={t('pages.moldMaintenances.fields.findings')}
              value={formData.findings || ''}
              onChange={(e) => setFormData({ ...formData, findings: e.target.value })}
              multiline
              rows={2}
            />
            <TextField
              label={t('pages.moldMaintenances.fields.correctiveAction')}
              value={formData.correctiveAction || ''}
              onChange={(e) => setFormData({ ...formData, correctiveAction: e.target.value })}
              multiline
              rows={2}
            />
            <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 2 }}>
              <TextField
                label={t('pages.moldMaintenances.fields.partsCost')}
                type="number"
                value={formData.partsCost || ''}
                onChange={(e) => setFormData({ ...formData, partsCost: parseFloat(e.target.value) || undefined })}
              />
              <TextField
                label={t('pages.moldMaintenances.fields.laborCost')}
                type="number"
                value={formData.laborCost || ''}
                onChange={(e) => setFormData({ ...formData, laborCost: parseFloat(e.target.value) || undefined })}
              />
            </Box>
            <TextField
              label={t('pages.moldMaintenances.fields.laborHoursLabel')}
              type="number"
              value={formData.laborHours || ''}
              onChange={(e) => setFormData({ ...formData, laborHours: parseInt(e.target.value) || undefined })}
            />
            <TextField
              label={t('pages.moldMaintenances.fields.technicianName')}
              value={formData.technicianName || ''}
              onChange={(e) => setFormData({ ...formData, technicianName: e.target.value })}
            />
            <FormControl>
              <InputLabel>{t('pages.moldMaintenances.fields.maintenanceResult')}</InputLabel>
              <Select
                value={formData.maintenanceResult || ''}
                onChange={(e) => setFormData({ ...formData, maintenanceResult: e.target.value })}
                label={t('pages.moldMaintenances.fields.maintenanceResult')}
              >
                <MenuItem value="">{t('pages.moldMaintenances.results.none')}</MenuItem>
                <MenuItem value="COMPLETED">{t('pages.moldMaintenances.results.completed')}</MenuItem>
                <MenuItem value="PARTIAL">{t('pages.moldMaintenances.results.partialComplete')}</MenuItem>
                <MenuItem value="FAILED">{t('pages.moldMaintenances.results.failed')}</MenuItem>
              </Select>
            </FormControl>
            <TextField
              label={t('common.labels.remarks')}
              value={formData.remarks || ''}
              onChange={(e) => setFormData({ ...formData, remarks: e.target.value })}
              multiline
              rows={2}
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>{t('common.buttons.cancel')}</Button>
          <Button onClick={handleSubmit} variant="contained">
            {selectedMaintenance ? t('common.buttons.edit') : t('pages.moldMaintenances.actions.register')}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={openDeleteDialog} onClose={() => setOpenDeleteDialog(false)}>
        <DialogTitle>{t('pages.moldMaintenances.dialogs.deleteTitle')}</DialogTitle>
        <DialogContent>
          <Typography>
            {t('pages.moldMaintenances.dialogs.deleteConfirm', { no: selectedMaintenance?.maintenanceNo })}
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenDeleteDialog(false)}>{t('common.buttons.cancel')}</Button>
          <Button onClick={handleDelete} color="error" variant="contained">
            {t('common.buttons.delete')}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Snackbar */}
      <Snackbar
        open={snackbar.open}
        autoHideDuration={3000}
        onClose={() => setSnackbar({ ...snackbar, open: false })}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert severity={snackbar.severity} sx={{ width: '100%' }}>
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Box>
  );
};

export default MoldMaintenancesPage;
