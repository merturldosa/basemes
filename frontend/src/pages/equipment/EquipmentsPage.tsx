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
} from '@mui/material';
import { DataGrid, GridColDef, GridRenderCellParams } from '@mui/x-data-grid';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  CheckCircle as ActiveIcon,
  Cancel as InactiveIcon,
} from '@mui/icons-material';
import equipmentService, { Equipment, EquipmentCreateRequest, EquipmentUpdateRequest } from '../../services/equipmentService';

const EquipmentsPage: React.FC = () => {
  const { t } = useTranslation();
  const [equipments, setEquipments] = useState<Equipment[]>([]);
  const [loading, setLoading] = useState(false);
  const [openDialog, setOpenDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [selectedEquipment, setSelectedEquipment] = useState<Equipment | null>(null);
  const [formData, setFormData] = useState<EquipmentCreateRequest>({
    equipmentCode: '',
    equipmentName: '',
    equipmentType: 'MACHINE',
    status: 'OPERATIONAL',
  });
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'success' as 'success' | 'error' | 'warning' | 'info',
  });

  useEffect(() => {
    loadEquipments();
  }, []);

  const loadEquipments = async () => {
    try {
      setLoading(true);
      const data = await equipmentService.getAll();
      setEquipments(data || []);
    } catch (error) {
      setEquipments([]);
      showSnackbar(t('pages.equipments.messages.loadFailed'), 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleOpenDialog = (equipment?: Equipment) => {
    if (equipment) {
      setSelectedEquipment(equipment);
      setFormData({
        equipmentCode: equipment.equipmentCode,
        equipmentName: equipment.equipmentName,
        equipmentType: equipment.equipmentType,
        equipmentCategory: equipment.equipmentCategory,
        manufacturer: equipment.manufacturer,
        modelName: equipment.modelName,
        serialNo: equipment.serialNo,
        location: equipment.location,
        capacity: equipment.capacity,
        status: equipment.status,
        maintenanceCycleDays: equipment.maintenanceCycleDays,
        standardCycleTime: equipment.standardCycleTime,
        actualOeeTarget: equipment.actualOeeTarget,
        remarks: equipment.remarks,
      });
    } else {
      setSelectedEquipment(null);
      setFormData({
        equipmentCode: '',
        equipmentName: '',
        equipmentType: 'MACHINE',
        status: 'OPERATIONAL',
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedEquipment(null);
  };

  const handleSubmit = async () => {
    try {
      if (selectedEquipment) {
        await equipmentService.update(selectedEquipment.equipmentId, formData as EquipmentUpdateRequest);
        showSnackbar(t('pages.equipments.messages.updated'), 'success');
      } else {
        await equipmentService.create(formData);
        showSnackbar(t('pages.equipments.messages.created'), 'success');
      }
      handleCloseDialog();
      loadEquipments();
    } catch (error) {
      showSnackbar(t('pages.equipments.messages.saveFailed'), 'error');
    }
  };

  const handleDelete = async () => {
    if (selectedEquipment) {
      try {
        await equipmentService.delete(selectedEquipment.equipmentId);
        showSnackbar(t('pages.equipments.messages.deleted'), 'success');
        setOpenDeleteDialog(false);
        setSelectedEquipment(null);
        loadEquipments();
      } catch (error) {
        showSnackbar(t('pages.equipments.messages.deleteFailed'), 'error');
      }
    }
  };

  const handleToggleActive = async (equipment: Equipment) => {
    try {
      if (equipment.isActive) {
        await equipmentService.deactivate(equipment.equipmentId);
        showSnackbar(t('pages.equipments.messages.deactivated'), 'success');
      } else {
        await equipmentService.activate(equipment.equipmentId);
        showSnackbar(t('pages.equipments.messages.activated'), 'success');
      }
      loadEquipments();
    } catch (error) {
      showSnackbar(t('pages.equipments.messages.statusChangeFailed'), 'error');
    }
  };

  const showSnackbar = (message: string, severity: 'success' | 'error' | 'warning' | 'info') => {
    setSnackbar({ open: true, message, severity });
  };

  const getStatusChip = (status: string) => {
    const statusConfig: Record<string, { label: string; color: 'success' | 'error' | 'warning' | 'default' | 'info' }> = {
      OPERATIONAL: { label: t('pages.equipments.status.operational'), color: 'success' },
      STOPPED: { label: t('pages.equipments.status.stopped'), color: 'default' },
      MAINTENANCE: { label: t('pages.equipments.status.maintenance'), color: 'warning' },
      BREAKDOWN: { label: t('pages.equipments.status.breakdown'), color: 'error' },
      RETIRED: { label: t('pages.equipments.status.retired'), color: 'default' },
    };
    const config = statusConfig[status] || { label: status, color: 'default' };
    return <Chip label={config.label} color={config.color} size="small" />;
  };

  const columns: GridColDef[] = [
    { field: 'equipmentCode', headerName: t('pages.equipments.fields.equipmentCode'), width: 150 },
    { field: 'equipmentName', headerName: t('pages.equipments.fields.equipmentName'), width: 200 },
    {
      field: 'equipmentType',
      headerName: t('pages.equipments.fields.equipmentType'),
      width: 120,
      renderCell: (params: GridRenderCellParams) => {
        const typeLabels: Record<string, string> = {
          MACHINE: t('pages.equipments.types.machine'),
          MOLD: t('pages.equipments.types.mold'),
          TOOL: t('pages.equipments.types.tool'),
          FACILITY: t('pages.equipments.types.facility'),
          VEHICLE: t('pages.equipments.types.vehicle'),
          OTHER: t('pages.equipments.types.other'),
        };
        return typeLabels[params.value] || params.value;
      },
    },
    { field: 'manufacturer', headerName: t('pages.equipments.fields.manufacturer'), width: 150 },
    { field: 'modelName', headerName: t('pages.equipments.fields.modelName'), width: 150 },
    { field: 'location', headerName: t('pages.equipments.fields.location'), width: 150 },
    {
      field: 'status',
      headerName: t('common.labels.status'),
      width: 120,
      renderCell: (params: GridRenderCellParams) => getStatusChip(params.value),
    },
    { field: 'nextMaintenanceDate', headerName: t('pages.equipments.fields.nextMaintenanceDate'), width: 130 },
    {
      field: 'isActive',
      headerName: t('pages.equipments.fields.isActive'),
      width: 100,
      renderCell: (params: GridRenderCellParams) => (
        <Chip
          label={params.value ? t('common.status.active') : t('common.status.inactive')}
          color={params.value ? 'success' : 'default'}
          size="small"
        />
      ),
    },
    {
      field: 'actions',
      headerName: t('common.labels.actions'),
      width: 150,
      sortable: false,
      renderCell: (params: GridRenderCellParams) => (
        <Box>
          <IconButton
            size="small"
            onClick={() => handleOpenDialog(params.row as Equipment)}
            color="primary"
          >
            <EditIcon fontSize="small" />
          </IconButton>
          <IconButton
            size="small"
            onClick={() => handleToggleActive(params.row as Equipment)}
            color={params.row.isActive ? 'warning' : 'success'}
          >
            {params.row.isActive ? <InactiveIcon fontSize="small" /> : <ActiveIcon fontSize="small" />}
          </IconButton>
          <IconButton
            size="small"
            onClick={() => {
              setSelectedEquipment(params.row as Equipment);
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
            {t('pages.equipments.title')}
          </Typography>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => handleOpenDialog()}
          >
            {t('pages.equipments.actions.register')}
          </Button>
        </Box>

        <DataGrid
          rows={equipments}
          columns={columns}
          loading={loading}
          getRowId={(row) => row.equipmentId}
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
        <DialogTitle>{selectedEquipment ? t('pages.equipments.dialogs.editTitle') : t('pages.equipments.dialogs.createTitle')}</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
            <TextField
              label={t('pages.equipments.fields.equipmentCode')}
              value={formData.equipmentCode}
              onChange={(e) => setFormData({ ...formData, equipmentCode: e.target.value })}
              required
              disabled={!!selectedEquipment}
            />
            <TextField
              label={t('pages.equipments.fields.equipmentName')}
              value={formData.equipmentName}
              onChange={(e) => setFormData({ ...formData, equipmentName: e.target.value })}
              required
            />
            <FormControl>
              <InputLabel>{t('pages.equipments.fields.equipmentType')}</InputLabel>
              <Select
                value={formData.equipmentType}
                onChange={(e) => setFormData({ ...formData, equipmentType: e.target.value })}
                label={t('pages.equipments.fields.equipmentType')}
                required
              >
                <MenuItem value="MACHINE">{t('pages.equipments.types.machine')}</MenuItem>
                <MenuItem value="MOLD">{t('pages.equipments.types.mold')}</MenuItem>
                <MenuItem value="TOOL">{t('pages.equipments.types.tool')}</MenuItem>
                <MenuItem value="FACILITY">{t('pages.equipments.types.facility')}</MenuItem>
                <MenuItem value="VEHICLE">{t('pages.equipments.types.vehicle')}</MenuItem>
                <MenuItem value="OTHER">{t('pages.equipments.types.other')}</MenuItem>
              </Select>
            </FormControl>
            <TextField
              label={t('pages.equipments.fields.category')}
              value={formData.equipmentCategory || ''}
              onChange={(e) => setFormData({ ...formData, equipmentCategory: e.target.value })}
            />
            <TextField
              label={t('pages.equipments.fields.manufacturer')}
              value={formData.manufacturer || ''}
              onChange={(e) => setFormData({ ...formData, manufacturer: e.target.value })}
            />
            <TextField
              label={t('pages.equipments.fields.modelName')}
              value={formData.modelName || ''}
              onChange={(e) => setFormData({ ...formData, modelName: e.target.value })}
            />
            <TextField
              label={t('pages.equipments.fields.serialNo')}
              value={formData.serialNo || ''}
              onChange={(e) => setFormData({ ...formData, serialNo: e.target.value })}
            />
            <TextField
              label={t('pages.equipments.fields.location')}
              value={formData.location || ''}
              onChange={(e) => setFormData({ ...formData, location: e.target.value })}
            />
            <TextField
              label={t('pages.equipments.fields.capacity')}
              value={formData.capacity || ''}
              onChange={(e) => setFormData({ ...formData, capacity: e.target.value })}
            />
            <FormControl>
              <InputLabel>{t('common.labels.status')}</InputLabel>
              <Select
                value={formData.status}
                onChange={(e) => setFormData({ ...formData, status: e.target.value })}
                label={t('common.labels.status')}
                required
              >
                <MenuItem value="OPERATIONAL">{t('pages.equipments.status.operational')}</MenuItem>
                <MenuItem value="STOPPED">{t('pages.equipments.status.stopped')}</MenuItem>
                <MenuItem value="MAINTENANCE">{t('pages.equipments.status.maintenance')}</MenuItem>
                <MenuItem value="BREAKDOWN">{t('pages.equipments.status.breakdown')}</MenuItem>
                <MenuItem value="RETIRED">{t('pages.equipments.status.retired')}</MenuItem>
              </Select>
            </FormControl>
            <TextField
              label={t('pages.equipments.fields.maintenanceCycleDays')}
              type="number"
              value={formData.maintenanceCycleDays || ''}
              onChange={(e) => setFormData({ ...formData, maintenanceCycleDays: parseInt(e.target.value) || undefined })}
            />
            <TextField
              label={t('pages.equipments.fields.standardCycleTime')}
              type="number"
              value={formData.standardCycleTime || ''}
              onChange={(e) => setFormData({ ...formData, standardCycleTime: parseFloat(e.target.value) || undefined })}
            />
            <TextField
              label={t('pages.equipments.fields.oeeTarget')}
              type="number"
              value={formData.actualOeeTarget || ''}
              onChange={(e) => setFormData({ ...formData, actualOeeTarget: parseFloat(e.target.value) || undefined })}
            />
            <TextField
              label={t('common.labels.remarks')}
              value={formData.remarks || ''}
              onChange={(e) => setFormData({ ...formData, remarks: e.target.value })}
              multiline
              rows={3}
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>{t('common.buttons.cancel')}</Button>
          <Button onClick={handleSubmit} variant="contained">
            {selectedEquipment ? t('common.buttons.edit') : t('pages.equipments.actions.register')}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={openDeleteDialog} onClose={() => setOpenDeleteDialog(false)}>
        <DialogTitle>{t('pages.equipments.dialogs.deleteTitle')}</DialogTitle>
        <DialogContent>
          <Typography>
            {t('pages.equipments.dialogs.deleteConfirm', { name: selectedEquipment?.equipmentName })}
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

export default EquipmentsPage;
