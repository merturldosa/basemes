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
import shippingService, { Shipping, ShippingRequest } from '../../services/shippingService';
import warehouseService, { Warehouse } from '../../services/warehouseService';
import salesOrderService, { SalesOrder } from '../../services/salesOrderService';

const ShippingPage: React.FC = () => {
  const { t } = useTranslation();
  const [shippings, setShippings] = useState<Shipping[]>([]);
  const [warehouses, setWarehouses] = useState<Warehouse[]>([]);
  const [salesOrders, setSalesOrders] = useState<SalesOrder[]>([]);
  const [loading, setLoading] = useState(false);
  const [openDialog, setOpenDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [selectedShipping, setSelectedShipping] = useState<Shipping | null>(null);
  const [viewMode, setViewMode] = useState(false);
  const [snackbar, setSnackbar] = useState<{
    open: boolean;
    message: string;
    severity: 'success' | 'error' | 'info';
  }>({ open: false, message: '', severity: 'success' });

  const [formData, setFormData] = useState<ShippingRequest>({
    shippingNo: '',
    shippingDate: new Date().toISOString().slice(0, 16),
    salesOrderId: undefined,
    warehouseId: 0,
    shippingType: 'SALES',
    shippingStatus: 'PENDING',
    shipperName: '',
    deliveryAddress: '',
    trackingNumber: '',
    carrierName: '',
    remarks: '',
    items: [],
  });

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const [shippingsData, warehousesData, salesOrdersData] = await Promise.all([
        shippingService.getAll(),
        warehouseService.getActive(),
        salesOrderService.getAll(),
      ]);
      setShippings(shippingsData || []);
      setWarehouses(warehousesData || []);
      setSalesOrders(salesOrdersData || []);
    } catch (error) {
      setSnackbar({ open: true, message: t('common.messages.error'), severity: 'error' });
      setShippings([]);
      setWarehouses([]);
      setSalesOrders([]);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenDialog = (shipping?: Shipping, view = false) => {
    if (shipping) {
      setViewMode(view);
      setSelectedShipping(shipping);
      setFormData({
        shippingNo: shipping.shippingNo,
        shippingDate: shipping.shippingDate,
        salesOrderId: shipping.salesOrderId,
        warehouseId: shipping.warehouseId,
        shippingType: shipping.shippingType,
        shippingStatus: shipping.shippingStatus,
        shipperName: shipping.shipperName || '',
        deliveryAddress: shipping.deliveryAddress || '',
        trackingNumber: shipping.trackingNumber || '',
        carrierName: shipping.carrierName || '',
        remarks: shipping.remarks || '',
        items: shipping.items || [],
      });
    } else {
      setViewMode(false);
      setSelectedShipping(null);
      setFormData({
        shippingNo: '',
        shippingDate: new Date().toISOString().slice(0, 16),
        salesOrderId: undefined,
        warehouseId: 0,
        shippingType: 'SALES',
        shippingStatus: 'PENDING',
        shipperName: '',
        deliveryAddress: '',
        trackingNumber: '',
        carrierName: '',
        remarks: '',
        items: [],
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => { setOpenDialog(false); setSelectedShipping(null); setViewMode(false); };
  const handleOpenDeleteDialog = (shipping: Shipping) => { setSelectedShipping(shipping); setOpenDeleteDialog(true); };
  const handleCloseDeleteDialog = () => { setOpenDeleteDialog(false); setSelectedShipping(null); };

  const handleSubmit = async () => {
    try {
      await shippingService.create(formData);
      setSnackbar({ open: true, message: t('common.messages.saveSuccess'), severity: 'success' });
      handleCloseDialog();
      loadData();
    } catch (error) {
      setSnackbar({ open: true, message: t('common.messages.error'), severity: 'error' });
    }
  };

  const handleDelete = async () => {
    if (!selectedShipping) return;
    try {
      await shippingService.delete(selectedShipping.shippingId);
      setSnackbar({ open: true, message: t('common.messages.success'), severity: 'success' });
      handleCloseDeleteDialog();
      loadData();
    } catch (error) {
      setSnackbar({ open: true, message: t('common.messages.error'), severity: 'error' });
    }
  };

  const handleComplete = async (id: number) => {
    try {
      await shippingService.complete(id);
      setSnackbar({ open: true, message: t('common.messages.success'), severity: 'success' });
      loadData();
    } catch (error) {
      setSnackbar({ open: true, message: t('common.messages.error'), severity: 'error' });
    }
  };

  const columns: GridColDef[] = [
    { field: 'shippingNo', headerName: t('pages.shipping.fields.shippingNo'), width: 150 },
    {
      field: 'shippingDate', headerName: t('pages.shipping.fields.shippingDate'), width: 180,
      valueFormatter: (params) => new Date(params.value).toLocaleString('ko-KR'),
    },
    { field: 'salesOrderNo', headerName: t('pages.shipping.fields.salesOrderNo'), width: 150 },
    { field: 'customerName', headerName: t('pages.shipping.fields.customer'), width: 150 },
    { field: 'warehouseName', headerName: t('pages.shipping.fields.warehouse'), width: 120 },
    {
      field: 'shippingType', headerName: t('pages.shipping.fields.shippingType'), width: 100,
      valueFormatter: (params) => {
        const types: { [key: string]: string } = {
          SALES: t('pages.shipping.types.sales'),
          RETURN: t('pages.shipping.types.return'),
          TRANSFER: t('pages.shipping.types.transfer'),
          OTHER: t('pages.shipping.types.other'),
        };
        return types[params.value] || params.value;
      },
    },
    {
      field: 'shippingStatus', headerName: t('pages.shipping.fields.shippingStatus'), width: 120,
      renderCell: (params) => {
        const statusColors: { [key: string]: 'default' | 'warning' | 'success' | 'error' } = {
          PENDING: 'warning', INSPECTING: 'default', SHIPPED: 'success', CANCELLED: 'default',
        };
        const statusLabels: { [key: string]: string } = {
          PENDING: t('pages.shipping.status.pending'), INSPECTING: t('pages.shipping.status.inspecting'),
          SHIPPED: t('pages.shipping.status.shipped'), CANCELLED: t('pages.shipping.status.cancelled'),
        };
        return <Chip label={statusLabels[params.value] || params.value} color={statusColors[params.value] || 'default'} size="small" />;
      },
    },
    { field: 'trackingNumber', headerName: t('pages.shipping.fields.trackingNumber'), width: 150 },
    { field: 'carrierName', headerName: t('pages.shipping.fields.carrierName'), width: 120 },
    {
      field: 'actions', type: 'actions', headerName: t('common.labels.actions'), width: 150,
      getActions: (params) => [
        <GridActionsCellItem icon={<VisibilityIcon />} label={t('common.buttons.edit')} onClick={() => handleOpenDialog(params.row, true)} />,
        <GridActionsCellItem icon={<CheckCircleIcon />} label={t('common.buttons.confirm')} onClick={() => handleComplete(params.row.shippingId)} disabled={params.row.shippingStatus !== 'PENDING'} />,
        <GridActionsCellItem icon={<DeleteIcon />} label={t('common.buttons.delete')} onClick={() => handleOpenDeleteDialog(params.row)} disabled={params.row.shippingStatus === 'SHIPPED'} />,
      ],
    },
  ];

  return (
    <Box sx={{ height: '100%', p: 3 }}>
      <Paper sx={{ p: 2, mb: 2 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="h5">{t('pages.shipping.title')}</Typography>
          <Button variant="contained" startIcon={<AddIcon />} onClick={() => handleOpenDialog()}>{t('pages.shipping.actions.create')}</Button>
        </Box>
      </Paper>

      <Paper sx={{ height: 'calc(100vh - 250px)' }}>
        <DataGrid rows={shippings} columns={columns} loading={loading} getRowId={(row) => row.shippingId} pageSizeOptions={[10, 25, 50, 100]} initialState={{ pagination: { paginationModel: { pageSize: 25 } } }} />
      </Paper>

      {/* Create/View Dialog */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="md" fullWidth>
        <DialogTitle>{viewMode ? t('pages.shipping.dialogs.detailTitle') : t('pages.shipping.dialogs.createTitle')}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12} sm={6}>
              <TextField fullWidth label={t('pages.shipping.fields.shippingNo')} value={formData.shippingNo} onChange={(e) => setFormData({ ...formData, shippingNo: e.target.value })} required disabled={viewMode} />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField fullWidth label={t('pages.shipping.fields.shippingDate')} type="datetime-local" value={formData.shippingDate} onChange={(e) => setFormData({ ...formData, shippingDate: e.target.value })} required disabled={viewMode} InputLabelProps={{ shrink: true }} />
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth>
                <InputLabel>{t('pages.shipping.fields.salesOrder')}</InputLabel>
                <Select value={formData.salesOrderId || ''} onChange={(e) => setFormData({ ...formData, salesOrderId: e.target.value as number })} label={t('pages.shipping.fields.salesOrder')} disabled={viewMode}>
                  <MenuItem value=""><em>{t('pages.shipping.fields.noneSelected')}</em></MenuItem>
                  {salesOrders.map((so) => (<MenuItem key={so.salesOrderId} value={so.salesOrderId}>{so.orderNo}</MenuItem>))}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth required>
                <InputLabel>{t('pages.shipping.fields.warehouse')}</InputLabel>
                <Select value={formData.warehouseId || ''} onChange={(e) => setFormData({ ...formData, warehouseId: e.target.value as number })} label={t('pages.shipping.fields.warehouse')} disabled={viewMode}>
                  {warehouses.map((wh) => (<MenuItem key={wh.warehouseId} value={wh.warehouseId}>{wh.warehouseName}</MenuItem>))}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth required>
                <InputLabel>{t('pages.shipping.fields.shippingTypeLabel')}</InputLabel>
                <Select value={formData.shippingType} onChange={(e) => setFormData({ ...formData, shippingType: e.target.value })} label={t('pages.shipping.fields.shippingTypeLabel')} disabled={viewMode}>
                  <MenuItem value="SALES">{t('pages.shipping.types.sales')}</MenuItem>
                  <MenuItem value="RETURN">{t('pages.shipping.types.return')}</MenuItem>
                  <MenuItem value="TRANSFER">{t('pages.shipping.types.transfer')}</MenuItem>
                  <MenuItem value="OTHER">{t('pages.shipping.types.other')}</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField fullWidth label={t('pages.shipping.fields.shipperName')} value={formData.shipperName} onChange={(e) => setFormData({ ...formData, shipperName: e.target.value })} disabled={viewMode} />
            </Grid>
            <Grid item xs={12}>
              <TextField fullWidth label={t('pages.shipping.fields.deliveryAddress')} value={formData.deliveryAddress} onChange={(e) => setFormData({ ...formData, deliveryAddress: e.target.value })} disabled={viewMode} />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField fullWidth label={t('pages.shipping.fields.trackingNumber')} value={formData.trackingNumber} onChange={(e) => setFormData({ ...formData, trackingNumber: e.target.value })} disabled={viewMode} />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField fullWidth label={t('pages.shipping.fields.carrierName')} value={formData.carrierName} onChange={(e) => setFormData({ ...formData, carrierName: e.target.value })} disabled={viewMode} />
            </Grid>
            <Grid item xs={12}>
              <TextField fullWidth label={t('common.labels.remarks')} multiline rows={3} value={formData.remarks} onChange={(e) => setFormData({ ...formData, remarks: e.target.value })} disabled={viewMode} />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>{t('common.buttons.close')}</Button>
          {!viewMode && (<Button onClick={handleSubmit} variant="contained">{t('common.buttons.add')}</Button>)}
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={openDeleteDialog} onClose={handleCloseDeleteDialog}>
        <DialogTitle>{t('pages.shipping.dialogs.deleteTitle')}</DialogTitle>
        <DialogContent>
          <Typography>{t('pages.shipping.dialogs.confirmDelete')}</Typography>
          {selectedShipping && (
            <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
              {t('pages.shipping.dialogs.shippingNoLabel')}: {selectedShipping.shippingNo}
            </Typography>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDeleteDialog}>{t('common.buttons.cancel')}</Button>
          <Button onClick={handleDelete} color="error" variant="contained">{t('common.buttons.delete')}</Button>
        </DialogActions>
      </Dialog>

      <Snackbar open={snackbar.open} autoHideDuration={6000} onClose={() => setSnackbar({ ...snackbar, open: false })}>
        <Alert severity={snackbar.severity} onClose={() => setSnackbar({ ...snackbar, open: false })}>{snackbar.message}</Alert>
      </Snackbar>
    </Box>
  );
};

export default ShippingPage;
