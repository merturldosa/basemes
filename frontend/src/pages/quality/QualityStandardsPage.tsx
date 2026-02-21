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
  Select,
  MenuItem,
  FormControl,
  InputLabel,
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
import qualityStandardService, { QualityStandard, QualityStandardCreateRequest, QualityStandardUpdateRequest } from '../../services/qualityStandardService';
import productService, { Product } from '../../services/productService';

const QualityStandardsPage: React.FC = () => {
  const { t } = useTranslation();
  const [qualityStandards, setQualityStandards] = useState<QualityStandard[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [openDialog, setOpenDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [selectedQualityStandard, setSelectedQualityStandard] = useState<QualityStandard | null>(null);
  const [formData, setFormData] = useState<QualityStandardCreateRequest | QualityStandardUpdateRequest>({
    productId: 0,
    standardCode: '',
    standardName: '',
    standardVersion: '1.0',
    inspectionType: 'INCOMING',
    effectiveDate: new Date().toISOString().split('T')[0],
  });
  const [snackbar, setSnackbar] = useState<{ open: boolean; message: string; severity: 'success' | 'error' }>({
    open: false,
    message: '',
    severity: 'success',
  });

  useEffect(() => {
    loadQualityStandards();
  }, []);

  const loadQualityStandards = async () => {
    try {
      setLoading(true);
      const [standardsData, productsData] = await Promise.all([
        qualityStandardService.getQualityStandards(),
        productService.getActiveProducts(),
      ]);
      setQualityStandards(standardsData || []);
      setProducts(productsData || []);
    } catch (error) {
      showSnackbar(t('pages.qualityStandards.errors.loadFailed'), 'error');
      setQualityStandards([]);
      setProducts([]);
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

  const handleOpenDialog = (qualityStandard?: QualityStandard) => {
    if (qualityStandard) {
      setSelectedQualityStandard(qualityStandard);
      setFormData({
        qualityStandardId: qualityStandard.qualityStandardId,
        productId: qualityStandard.productId,
        standardName: qualityStandard.standardName,
        inspectionType: qualityStandard.inspectionType,
        inspectionMethod: qualityStandard.inspectionMethod,
        minValue: qualityStandard.minValue,
        maxValue: qualityStandard.maxValue,
        targetValue: qualityStandard.targetValue,
        toleranceValue: qualityStandard.toleranceValue,
        unit: qualityStandard.unit,
        measurementItem: qualityStandard.measurementItem,
        measurementEquipment: qualityStandard.measurementEquipment,
        samplingMethod: qualityStandard.samplingMethod,
        sampleSize: qualityStandard.sampleSize,
        isActive: qualityStandard.isActive,
        effectiveDate: qualityStandard.effectiveDate,
        expiryDate: qualityStandard.expiryDate,
        remarks: qualityStandard.remarks,
      });
    } else {
      setSelectedQualityStandard(null);
      setFormData({
        productId: products.length > 0 ? products[0].productId : 0,
        standardCode: '',
        standardName: '',
        standardVersion: '1.0',
        inspectionType: 'INCOMING',
        effectiveDate: new Date().toISOString().split('T')[0],
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedQualityStandard(null);
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: ['minValue', 'maxValue', 'targetValue', 'toleranceValue', 'sampleSize'].includes(name) && value
        ? Number(value)
        : value,
    });
  };

  const handleSubmit = async () => {
    try {
      if (selectedQualityStandard) {
        await qualityStandardService.updateQualityStandard(selectedQualityStandard.qualityStandardId, formData as QualityStandardUpdateRequest);
        showSnackbar(t('pages.qualityStandards.messages.updateSuccess'), 'success');
      } else {
        await qualityStandardService.createQualityStandard(formData as QualityStandardCreateRequest);
        showSnackbar(t('pages.qualityStandards.messages.createSuccess'), 'success');
      }
      handleCloseDialog();
      loadQualityStandards();
    } catch (error: any) {
      showSnackbar(error.response?.data?.message || t('pages.qualityStandards.errors.saveFailed'), 'error');
    }
  };

  const handleToggleActive = async (qualityStandard: QualityStandard) => {
    try {
      if (qualityStandard.isActive) {
        await qualityStandardService.deactivateQualityStandard(qualityStandard.qualityStandardId);
        showSnackbar(t('pages.qualityStandards.messages.deactivateSuccess'), 'success');
      } else {
        await qualityStandardService.activateQualityStandard(qualityStandard.qualityStandardId);
        showSnackbar(t('pages.qualityStandards.messages.activateSuccess'), 'success');
      }
      loadQualityStandards();
    } catch (error: any) {
      showSnackbar(error.response?.data?.message || t('pages.qualityStandards.errors.statusChangeFailed'), 'error');
    }
  };

  const handleOpenDeleteDialog = (qualityStandard: QualityStandard) => {
    setSelectedQualityStandard(qualityStandard);
    setOpenDeleteDialog(true);
  };

  const handleCloseDeleteDialog = () => {
    setOpenDeleteDialog(false);
    setSelectedQualityStandard(null);
  };

  const handleDelete = async () => {
    if (!selectedQualityStandard) return;

    try {
      await qualityStandardService.deleteQualityStandard(selectedQualityStandard.qualityStandardId);
      showSnackbar(t('pages.qualityStandards.messages.deleteSuccess'), 'success');
      handleCloseDeleteDialog();
      loadQualityStandards();
    } catch (error: any) {
      showSnackbar(error.response?.data?.message || t('pages.qualityStandards.errors.deleteFailed'), 'error');
    }
  };

  const inspectionTypeLabels: Record<string, string> = {
    INCOMING: t('pages.qualityStandards.inspectionTypes.incoming'),
    IN_PROCESS: t('pages.qualityStandards.inspectionTypes.inProcess'),
    OUTGOING: t('pages.qualityStandards.inspectionTypes.outgoing'),
    FINAL: t('pages.qualityStandards.inspectionTypes.final'),
  };

  const columns: GridColDef[] = [
    { field: 'standardCode', headerName: t('pages.qualityStandards.fields.standardCode'), width: 130 },
    { field: 'standardName', headerName: t('pages.qualityStandards.fields.standardName'), flex: 1, minWidth: 200 },
    { field: 'standardVersion', headerName: t('pages.qualityStandards.fields.version'), width: 80 },
    { field: 'productCode', headerName: t('pages.qualityStandards.fields.productCode'), width: 130 },
    { field: 'productName', headerName: t('pages.qualityStandards.fields.productName'), width: 150 },
    {
      field: 'inspectionType',
      headerName: t('pages.qualityStandards.fields.inspectionType'),
      width: 120,
      renderCell: (params) => (
        <Chip
          label={inspectionTypeLabels[params.value] || params.value}
          color="primary"
          size="small"
          variant="outlined"
        />
      ),
    },
    {
      field: 'minValue',
      headerName: t('pages.qualityStandards.fields.minValue'),
      width: 100,
      valueFormatter: (params) => params.value !== null && params.value !== undefined ? params.value : '-',
    },
    {
      field: 'maxValue',
      headerName: t('pages.qualityStandards.fields.maxValue'),
      width: 100,
      valueFormatter: (params) => params.value !== null && params.value !== undefined ? params.value : '-',
    },
    {
      field: 'targetValue',
      headerName: t('pages.qualityStandards.fields.targetValue'),
      width: 100,
      valueFormatter: (params) => params.value !== null && params.value !== undefined ? params.value : '-',
    },
    { field: 'unit', headerName: t('pages.qualityStandards.fields.unit'), width: 80 },
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
      field: 'actions',
      type: 'actions',
      headerName: t('common.labels.actions'),
      width: 150,
      getActions: (params: GridRowParams<QualityStandard>) => [
        <GridActionsCellItem
          icon={<EditIcon />}
          label={t('common.buttons.edit')}
          onClick={() => handleOpenDialog(params.row)}
        />,
        <GridActionsCellItem
          icon={params.row.isActive ? <ToggleOffIcon /> : <ToggleOnIcon />}
          label={params.row.isActive ? t('common.status.inactive') : t('common.status.active')}
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
          {t('pages.qualityStandards.title')}
        </Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => handleOpenDialog()}
        >
          {t('pages.qualityStandards.actions.create')}
        </Button>
      </Box>

      <Paper>
        <DataGrid
          rows={qualityStandards}
          columns={columns}
          getRowId={(row) => row.qualityStandardId}
          loading={loading}
          autoHeight
          pageSizeOptions={[10, 25, 50, 100]}
          initialState={{
            pagination: { paginationModel: { pageSize: 25 } },
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
        <DialogTitle>{selectedQualityStandard ? t('pages.qualityStandards.dialog.editTitle') : t('pages.qualityStandards.dialog.createTitle')}</DialogTitle>
        <DialogContent>
          <Box display="flex" flexDirection="column" gap={2} mt={1}>
            <FormControl fullWidth required>
              <InputLabel>{t('pages.qualityStandards.fields.product')}</InputLabel>
              <Select
                name="productId"
                value={formData.productId || ''}
                onChange={(e) => setFormData({ ...formData, productId: Number(e.target.value) })}
                label={t('pages.qualityStandards.fields.product')}
              >
                {products.map((product) => (
                  <MenuItem key={product.productId} value={product.productId}>
                    {product.productName} ({product.productCode})
                  </MenuItem>
                ))}
              </Select>
            </FormControl>

            {!selectedQualityStandard && (
              <TextField
                name="standardCode"
                label={t('pages.qualityStandards.fields.standardCode')}
                value={(formData as QualityStandardCreateRequest).standardCode || ''}
                onChange={handleInputChange}
                required
                fullWidth
              />
            )}

            <TextField
              name="standardName"
              label={t('pages.qualityStandards.fields.standardName')}
              value={formData.standardName || ''}
              onChange={handleInputChange}
              required
              fullWidth
            />

            {!selectedQualityStandard && (
              <TextField
                name="standardVersion"
                label={t('pages.qualityStandards.fields.version')}
                value={(formData as QualityStandardCreateRequest).standardVersion || ''}
                onChange={handleInputChange}
                fullWidth
              />
            )}

            <FormControl fullWidth required>
              <InputLabel>{t('pages.qualityStandards.fields.inspectionType')}</InputLabel>
              <Select
                name="inspectionType"
                value={formData.inspectionType || 'INCOMING'}
                onChange={(e) => setFormData({ ...formData, inspectionType: e.target.value })}
                label={t('pages.qualityStandards.fields.inspectionType')}
              >
                <MenuItem value="INCOMING">{t('pages.qualityStandards.inspectionTypes.incoming')}</MenuItem>
                <MenuItem value="IN_PROCESS">{t('pages.qualityStandards.inspectionTypes.inProcess')}</MenuItem>
                <MenuItem value="OUTGOING">{t('pages.qualityStandards.inspectionTypes.outgoing')}</MenuItem>
                <MenuItem value="FINAL">{t('pages.qualityStandards.inspectionTypes.final')}</MenuItem>
              </Select>
            </FormControl>

            <TextField
              name="inspectionMethod"
              label={t('pages.qualityStandards.fields.inspectionMethod')}
              value={formData.inspectionMethod || ''}
              onChange={handleInputChange}
              fullWidth
            />

            <Box display="flex" gap={2}>
              <TextField
                name="minValue"
                label={t('pages.qualityStandards.fields.minValue')}
                type="number"
                value={formData.minValue || ''}
                onChange={handleInputChange}
                fullWidth
              />
              <TextField
                name="targetValue"
                label={t('pages.qualityStandards.fields.targetValue')}
                type="number"
                value={formData.targetValue || ''}
                onChange={handleInputChange}
                fullWidth
              />
              <TextField
                name="maxValue"
                label={t('pages.qualityStandards.fields.maxValue')}
                type="number"
                value={formData.maxValue || ''}
                onChange={handleInputChange}
                fullWidth
              />
            </Box>

            <Box display="flex" gap={2}>
              <TextField
                name="toleranceValue"
                label={t('pages.qualityStandards.fields.toleranceValue')}
                type="number"
                value={formData.toleranceValue || ''}
                onChange={handleInputChange}
                fullWidth
              />
              <TextField
                name="unit"
                label={t('pages.qualityStandards.fields.unit')}
                value={formData.unit || ''}
                onChange={handleInputChange}
                fullWidth
              />
            </Box>

            <TextField
              name="measurementItem"
              label={t('pages.qualityStandards.fields.measurementItem')}
              value={formData.measurementItem || ''}
              onChange={handleInputChange}
              fullWidth
            />

            <TextField
              name="measurementEquipment"
              label={t('pages.qualityStandards.fields.measurementEquipment')}
              value={formData.measurementEquipment || ''}
              onChange={handleInputChange}
              fullWidth
            />

            <Box display="flex" gap={2}>
              <TextField
                name="samplingMethod"
                label={t('pages.qualityStandards.fields.samplingMethod')}
                value={formData.samplingMethod || ''}
                onChange={handleInputChange}
                fullWidth
              />
              <TextField
                name="sampleSize"
                label={t('pages.qualityStandards.fields.sampleSize')}
                type="number"
                value={formData.sampleSize || ''}
                onChange={handleInputChange}
                fullWidth
              />
            </Box>

            <Box display="flex" gap={2}>
              <TextField
                name="effectiveDate"
                label={t('pages.qualityStandards.fields.effectiveDate')}
                type="date"
                value={formData.effectiveDate || ''}
                onChange={handleInputChange}
                required
                fullWidth
                InputLabelProps={{ shrink: true }}
              />
              <TextField
                name="expiryDate"
                label={t('pages.qualityStandards.fields.expiryDate')}
                type="date"
                value={formData.expiryDate || ''}
                onChange={handleInputChange}
                fullWidth
                InputLabelProps={{ shrink: true }}
              />
            </Box>

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
            {selectedQualityStandard ? t('common.buttons.edit') : t('pages.qualityStandards.actions.register')}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={openDeleteDialog} onClose={handleCloseDeleteDialog}>
        <DialogTitle>{t('pages.qualityStandards.dialog.deleteTitle')}</DialogTitle>
        <DialogContent>
          <Alert severity="warning" sx={{ mb: 2 }}>
            {t('pages.qualityStandards.dialog.deleteWarning')}
          </Alert>
          <Typography>
            {t('pages.qualityStandards.dialog.deleteConfirm', { name: selectedQualityStandard?.standardName })}
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

export default QualityStandardsPage;
