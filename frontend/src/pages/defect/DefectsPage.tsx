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
  Snackbar,
  Alert,
  Chip,
  MenuItem,
  Select,
  FormControl,
  InputLabel,
  Grid,
} from '@mui/material';
import { DataGrid, GridColDef, GridActionsCellItem } from '@mui/x-data-grid';
import {
  Add as AddIcon,
  CheckCircle as CheckCircleIcon,
  Delete as DeleteIcon,
  Visibility as VisibilityIcon,
} from '@mui/icons-material';
import { useTranslation } from 'react-i18next';
import defectService, { Defect, DefectRequest } from '../../services/defectService';
import productService, { Product } from '../../services/productService';

const DefectsPage: React.FC = () => {
  const { t } = useTranslation();
  const [defects, setDefects] = useState<Defect[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(false);
  const [openDialog, setOpenDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [selectedDefect, setSelectedDefect] = useState<Defect | null>(null);
  const [viewMode, setViewMode] = useState(false);
  const [snackbar, setSnackbar] = useState<{
    open: boolean;
    message: string;
    severity: 'success' | 'error' | 'info';
  }>({ open: false, message: '', severity: 'success' });

  const [formData, setFormData] = useState<DefectRequest>({
    defectNo: '',
    defectDate: new Date().toISOString().slice(0, 16),
    sourceType: 'PRODUCTION',
    productId: 0,
    defectType: '',
    defectCategory: '',
    defectLocation: '',
    defectDescription: '',
    defectQuantity: 0,
    lotNo: '',
    severity: 'MINOR',
    status: 'REPORTED',
    defectCost: 0,
    remarks: '',
  });

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const [defectsData, productsData] = await Promise.all([
        defectService.getAll(),
        productService.getActiveProducts(),
      ]);
      setDefects(defectsData);
      setProducts(productsData);
    } catch (error) {
      setSnackbar({ open: true, message: t('pages.defects.errors.loadFailed'), severity: 'error' });
    } finally {
      setLoading(false);
    }
  };

  const handleOpenDialog = (defect?: Defect, view = false) => {
    if (defect) {
      setViewMode(view);
      setSelectedDefect(defect);
      setFormData({
        defectNo: defect.defectNo,
        defectDate: defect.defectDate,
        sourceType: defect.sourceType,
        productId: defect.productId,
        defectType: defect.defectType || '',
        defectCategory: defect.defectCategory || '',
        defectLocation: defect.defectLocation || '',
        defectDescription: defect.defectDescription || '',
        defectQuantity: defect.defectQuantity,
        lotNo: defect.lotNo || '',
        severity: defect.severity || 'MINOR',
        status: defect.status,
        defectCost: defect.defectCost,
        remarks: defect.remarks || '',
      });
    } else {
      setViewMode(false);
      setSelectedDefect(null);
      setFormData({
        defectNo: '',
        defectDate: new Date().toISOString().slice(0, 16),
        sourceType: 'PRODUCTION',
        productId: 0,
        defectType: '',
        defectCategory: '',
        defectLocation: '',
        defectDescription: '',
        defectQuantity: 0,
        lotNo: '',
        severity: 'MINOR',
        status: 'REPORTED',
        defectCost: 0,
        remarks: '',
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedDefect(null);
    setViewMode(false);
  };

  const handleOpenDeleteDialog = (defect: Defect) => {
    setSelectedDefect(defect);
    setOpenDeleteDialog(true);
  };

  const handleCloseDeleteDialog = () => {
    setOpenDeleteDialog(false);
    setSelectedDefect(null);
  };

  const handleSubmit = async () => {
    try {
      await defectService.create(formData);
      setSnackbar({ open: true, message: t('pages.defects.messages.createSuccess'), severity: 'success' });
      handleCloseDialog();
      loadData();
    } catch (error) {
      setSnackbar({ open: true, message: t('pages.defects.errors.createFailed'), severity: 'error' });
    }
  };

  const handleDelete = async () => {
    if (!selectedDefect) return;

    try {
      await defectService.delete(selectedDefect.defectId);
      setSnackbar({ open: true, message: t('pages.defects.messages.deleteSuccess'), severity: 'success' });
      handleCloseDeleteDialog();
      loadData();
    } catch (error) {
      setSnackbar({ open: true, message: t('pages.defects.errors.deleteFailed'), severity: 'error' });
    }
  };

  const handleClose = async (id: number) => {
    try {
      await defectService.close(id);
      setSnackbar({ open: true, message: t('pages.defects.messages.closeSuccess'), severity: 'success' });
      loadData();
    } catch (error) {
      setSnackbar({ open: true, message: t('pages.defects.errors.closeFailed'), severity: 'error' });
    }
  };

  const columns: GridColDef[] = [
    { field: 'defectNo', headerName: t('pages.defects.fields.defectNo'), width: 150 },
    {
      field: 'defectDate',
      headerName: t('pages.defects.fields.defectDate'),
      width: 180,
      valueFormatter: (params) => new Date(params.value).toLocaleString('ko-KR'),
    },
    {
      field: 'sourceType',
      headerName: t('pages.defects.fields.sourceType'),
      width: 120,
      valueFormatter: (params) => {
        const types: { [key: string]: string } = {
          PRODUCTION: t('pages.defects.sourceTypes.production'),
          RECEIVING: t('pages.defects.sourceTypes.receiving'),
          SHIPPING: t('pages.defects.sourceTypes.shipping'),
          INSPECTION: t('pages.defects.sourceTypes.inspection'),
          CUSTOMER: t('pages.defects.sourceTypes.customer'),
        };
        return types[params.value] || params.value;
      },
    },
    { field: 'productName', headerName: t('pages.defects.fields.product'), width: 150 },
    {
      field: 'defectType',
      headerName: t('pages.defects.fields.defectType'),
      width: 120,
      valueFormatter: (params) => {
        const types: { [key: string]: string } = {
          APPEARANCE: t('pages.defects.defectTypes.appearance'),
          DIMENSION: t('pages.defects.defectTypes.dimension'),
          FUNCTION: t('pages.defects.defectTypes.function'),
          MATERIAL: t('pages.defects.defectTypes.material'),
          ASSEMBLY: t('pages.defects.defectTypes.assembly'),
          OTHER: t('pages.defects.defectTypes.other'),
        };
        return types[params.value] || params.value;
      },
    },
    { field: 'defectQuantity', headerName: t('pages.defects.fields.defectQuantity'), width: 100 },
    {
      field: 'severity',
      headerName: t('pages.defects.fields.severity'),
      width: 100,
      renderCell: (params) => {
        const severityColors: { [key: string]: 'error' | 'warning' | 'default' } = {
          CRITICAL: 'error',
          MAJOR: 'warning',
          MINOR: 'default',
        };
        const severityLabels: { [key: string]: string } = {
          CRITICAL: t('pages.defects.severity.critical'),
          MAJOR: t('pages.defects.severity.major'),
          MINOR: t('pages.defects.severity.minor'),
        };
        return (
          <Chip
            label={severityLabels[params.value] || params.value}
            color={severityColors[params.value] || 'default'}
            size="small"
          />
        );
      },
    },
    {
      field: 'status',
      headerName: t('common.labels.status'),
      width: 120,
      renderCell: (params) => {
        const statusColors: { [key: string]: 'default' | 'warning' | 'info' | 'success' | 'error' } = {
          REPORTED: 'warning',
          IN_REVIEW: 'info',
          REWORK: 'info',
          SCRAP: 'error',
          CLOSED: 'success',
        };
        const statusLabels: { [key: string]: string } = {
          REPORTED: t('pages.defects.status.reported'),
          IN_REVIEW: t('pages.defects.status.inReview'),
          REWORK: t('pages.defects.status.rework'),
          SCRAP: t('pages.defects.status.scrap'),
          CLOSED: t('pages.defects.status.closed'),
        };
        return (
          <Chip
            label={statusLabels[params.value] || params.value}
            color={statusColors[params.value] || 'default'}
            size="small"
          />
        );
      },
    },
    {
      field: 'actions',
      type: 'actions',
      headerName: t('common.labels.actions'),
      width: 150,
      getActions: (params) => [
        <GridActionsCellItem
          icon={<VisibilityIcon />}
          label={t('pages.defects.actions.view')}
          onClick={() => handleOpenDialog(params.row, true)}
        />,
        <GridActionsCellItem
          icon={<CheckCircleIcon />}
          label={t('pages.defects.actions.close')}
          onClick={() => handleClose(params.row.defectId)}
          disabled={params.row.status === 'CLOSED'}
        />,
        <GridActionsCellItem
          icon={<DeleteIcon />}
          label={t('common.buttons.delete')}
          onClick={() => handleOpenDeleteDialog(params.row)}
          disabled={params.row.status === 'CLOSED'}
        />,
      ],
    },
  ];

  return (
    <Box sx={{ height: '100%', p: 3 }}>
      <Paper sx={{ p: 2, mb: 2 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="h5">{t('pages.defects.title')}</Typography>
          <Button variant="contained" startIcon={<AddIcon />} onClick={() => handleOpenDialog()}>
            {t('pages.defects.actions.create')}
          </Button>
        </Box>
      </Paper>

      <Paper sx={{ height: 'calc(100vh - 250px)' }}>
        <DataGrid
          rows={defects}
          columns={columns}
          loading={loading}
          getRowId={(row) => row.defectId}
          pageSizeOptions={[10, 25, 50, 100]}
          initialState={{
            pagination: { paginationModel: { pageSize: 25 } },
          }}
        />
      </Paper>

      {/* Create/View Dialog */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="md" fullWidth>
        <DialogTitle>{viewMode ? t('pages.defects.dialog.viewTitle') : t('pages.defects.dialog.createTitle')}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label={t('pages.defects.fields.defectNo')}
                value={formData.defectNo}
                onChange={(e) => setFormData({ ...formData, defectNo: e.target.value })}
                required
                disabled={viewMode}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label={t('pages.defects.fields.defectDate')}
                type="datetime-local"
                value={formData.defectDate}
                onChange={(e) => setFormData({ ...formData, defectDate: e.target.value })}
                required
                disabled={viewMode}
                InputLabelProps={{ shrink: true }}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth required>
                <InputLabel>{t('pages.defects.fields.sourceType')}</InputLabel>
                <Select
                  value={formData.sourceType}
                  onChange={(e) => setFormData({ ...formData, sourceType: e.target.value })}
                  label={t('pages.defects.fields.sourceType')}
                  disabled={viewMode}
                >
                  <MenuItem value="PRODUCTION">{t('pages.defects.sourceTypes.production')}</MenuItem>
                  <MenuItem value="RECEIVING">{t('pages.defects.sourceTypes.receiving')}</MenuItem>
                  <MenuItem value="SHIPPING">{t('pages.defects.sourceTypes.shipping')}</MenuItem>
                  <MenuItem value="INSPECTION">{t('pages.defects.sourceTypes.inspection')}</MenuItem>
                  <MenuItem value="CUSTOMER">{t('pages.defects.sourceTypes.customer')}</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth required>
                <InputLabel>{t('pages.defects.fields.product')}</InputLabel>
                <Select
                  value={formData.productId || ''}
                  onChange={(e) => setFormData({ ...formData, productId: e.target.value as number })}
                  label={t('pages.defects.fields.product')}
                  disabled={viewMode}
                >
                  {products.map((product) => (
                    <MenuItem key={product.productId} value={product.productId}>
                      {product.productName}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth>
                <InputLabel>{t('pages.defects.fields.defectType')}</InputLabel>
                <Select
                  value={formData.defectType}
                  onChange={(e) => setFormData({ ...formData, defectType: e.target.value })}
                  label={t('pages.defects.fields.defectType')}
                  disabled={viewMode}
                >
                  <MenuItem value="APPEARANCE">{t('pages.defects.defectTypes.appearance')}</MenuItem>
                  <MenuItem value="DIMENSION">{t('pages.defects.defectTypes.dimension')}</MenuItem>
                  <MenuItem value="FUNCTION">{t('pages.defects.defectTypes.function')}</MenuItem>
                  <MenuItem value="MATERIAL">{t('pages.defects.defectTypes.material')}</MenuItem>
                  <MenuItem value="ASSEMBLY">{t('pages.defects.defectTypes.assembly')}</MenuItem>
                  <MenuItem value="OTHER">{t('pages.defects.defectTypes.other')}</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth>
                <InputLabel>{t('pages.defects.fields.severity')}</InputLabel>
                <Select
                  value={formData.severity}
                  onChange={(e) => setFormData({ ...formData, severity: e.target.value })}
                  label={t('pages.defects.fields.severity')}
                  disabled={viewMode}
                >
                  <MenuItem value="CRITICAL">{t('pages.defects.severity.critical')}</MenuItem>
                  <MenuItem value="MAJOR">{t('pages.defects.severity.major')}</MenuItem>
                  <MenuItem value="MINOR">{t('pages.defects.severity.minor')}</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label={t('pages.defects.fields.defectQuantity')}
                type="number"
                value={formData.defectQuantity}
                onChange={(e) => setFormData({ ...formData, defectQuantity: parseFloat(e.target.value) })}
                disabled={viewMode}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label={t('pages.defects.fields.lotNo')}
                value={formData.lotNo}
                onChange={(e) => setFormData({ ...formData, lotNo: e.target.value })}
                disabled={viewMode}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label={t('pages.defects.fields.defectDescription')}
                multiline
                rows={3}
                value={formData.defectDescription}
                onChange={(e) => setFormData({ ...formData, defectDescription: e.target.value })}
                disabled={viewMode}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label={t('common.labels.remarks')}
                multiline
                rows={2}
                value={formData.remarks}
                onChange={(e) => setFormData({ ...formData, remarks: e.target.value })}
                disabled={viewMode}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>{t('common.buttons.close')}</Button>
          {!viewMode && (
            <Button onClick={handleSubmit} variant="contained">
              {t('pages.defects.actions.createBtn')}
            </Button>
          )}
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={openDeleteDialog} onClose={handleCloseDeleteDialog}>
        <DialogTitle>{t('pages.defects.dialog.deleteTitle')}</DialogTitle>
        <DialogContent>
          <Typography>{t('pages.defects.dialog.deleteConfirm')}</Typography>
          {selectedDefect && (
            <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
              {t('pages.defects.fields.defectNo')}: {selectedDefect.defectNo}
            </Typography>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDeleteDialog}>{t('common.buttons.cancel')}</Button>
          <Button onClick={handleDelete} color="error" variant="contained">
            {t('common.buttons.delete')}
          </Button>
        </DialogActions>
      </Dialog>

      <Snackbar
        open={snackbar.open}
        autoHideDuration={6000}
        onClose={() => setSnackbar({ ...snackbar, open: false })}
      >
        <Alert severity={snackbar.severity} onClose={() => setSnackbar({ ...snackbar, open: false })}>
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Box>
  );
};

export default DefectsPage;
