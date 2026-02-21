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
  Cancel as CancelIcon,
} from '@mui/icons-material';
import { useTranslation } from 'react-i18next';
import goodsReceiptService, { GoodsReceipt, GoodsReceiptRequest } from '../../services/goodsReceiptService';
import warehouseService, { Warehouse } from '../../services/warehouseService';
import purchaseOrderService, { PurchaseOrder } from '../../services/purchaseOrderService';

const ReceivingPage: React.FC = () => {
  const { t } = useTranslation();
  const [receipts, setReceipts] = useState<GoodsReceipt[]>([]);
  const [warehouses, setWarehouses] = useState<Warehouse[]>([]);
  const [purchaseOrders, setPurchaseOrders] = useState<PurchaseOrder[]>([]);
  const [loading, setLoading] = useState(false);
  const [openDialog, setOpenDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [selectedReceipt, setSelectedReceipt] = useState<GoodsReceipt | null>(null);
  const [viewMode, setViewMode] = useState(false);
  const [snackbar, setSnackbar] = useState<{
    open: boolean;
    message: string;
    severity: 'success' | 'error' | 'info';
  }>({ open: false, message: '', severity: 'success' });

  const [formData, setFormData] = useState<GoodsReceiptRequest>({
    receiptNo: '',
    receiptDate: new Date().toISOString().slice(0, 16),
    purchaseOrderId: undefined,
    warehouseId: 0,
    receiptType: 'PURCHASE',
    receiptStatus: 'PENDING',
    receiverName: '',
    remarks: '',
    items: [],
  });

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const [receiptsData, warehousesData, purchaseOrdersData] = await Promise.all([
        goodsReceiptService.getAll(),
        warehouseService.getActive(),
        purchaseOrderService.getAll(),
      ]);
      setReceipts(receiptsData || []);
      setWarehouses(warehousesData || []);
      setPurchaseOrders(purchaseOrdersData || []);
    } catch (error) {
      setSnackbar({ open: true, message: t('pages.receiving.messages.loadFailed') || 'Failed to load data', severity: 'error' });
      setReceipts([]);
      setWarehouses([]);
      setPurchaseOrders([]);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenDialog = (receipt?: GoodsReceipt, view = false) => {
    if (receipt) {
      setViewMode(view);
      setSelectedReceipt(receipt);
      setFormData({
        receiptNo: receipt.receiptNo,
        receiptDate: receipt.receiptDate,
        purchaseOrderId: receipt.purchaseOrderId,
        warehouseId: receipt.warehouseId,
        receiptType: receipt.receiptType,
        receiptStatus: receipt.receiptStatus,
        receiverName: receipt.receiverName || '',
        remarks: receipt.remarks || '',
        items: receipt.items || [],
      });
    } else {
      setViewMode(false);
      setSelectedReceipt(null);
      setFormData({
        receiptNo: '',
        receiptDate: new Date().toISOString().slice(0, 16),
        purchaseOrderId: undefined,
        warehouseId: 0,
        receiptType: 'PURCHASE',
        receiptStatus: 'PENDING',
        receiverName: '',
        remarks: '',
        items: [],
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedReceipt(null);
    setViewMode(false);
  };

  const handleOpenDeleteDialog = (receipt: GoodsReceipt) => {
    setSelectedReceipt(receipt);
    setOpenDeleteDialog(true);
  };

  const handleCloseDeleteDialog = () => {
    setOpenDeleteDialog(false);
    setSelectedReceipt(null);
  };

  const handleSubmit = async () => {
    try {
      await goodsReceiptService.create(formData);
      setSnackbar({ open: true, message: t('common.messages.saveSuccess'), severity: 'success' });
      handleCloseDialog();
      loadData();
    } catch (error) {
      setSnackbar({ open: true, message: t('common.messages.error'), severity: 'error' });
    }
  };

  const handleDelete = async () => {
    if (!selectedReceipt) return;
    try {
      await goodsReceiptService.cancel(selectedReceipt.goodsReceiptId);
      setSnackbar({ open: true, message: t('common.messages.success'), severity: 'success' });
      handleCloseDeleteDialog();
      loadData();
    } catch (error) {
      setSnackbar({ open: true, message: t('common.messages.error'), severity: 'error' });
    }
  };

  const handleComplete = async (id: number) => {
    try {
      await goodsReceiptService.complete(id);
      setSnackbar({ open: true, message: t('pages.receiving.messages.completed'), severity: 'success' });
      loadData();
    } catch (error) {
      setSnackbar({ open: true, message: t('pages.receiving.messages.completeFailed'), severity: 'error' });
    }
  };

  const handleCancel = async (id: number) => {
    try {
      await goodsReceiptService.cancel(id, t('common.messages.confirmDelete'));
      setSnackbar({ open: true, message: t('pages.receiving.messages.cancelled'), severity: 'success' });
      loadData();
    } catch (error) {
      setSnackbar({ open: true, message: t('pages.receiving.messages.cancelFailed'), severity: 'error' });
    }
  };

  const columns: GridColDef[] = [
    { field: 'receiptNo', headerName: t('pages.receiving.fields.receiptNo'), width: 150 },
    {
      field: 'receiptDate',
      headerName: t('pages.receiving.fields.receiptDate'),
      width: 180,
      valueFormatter: (params) => new Date(params.value).toLocaleString('ko-KR'),
    },
    { field: 'purchaseOrderNo', headerName: t('pages.receiving.fields.purchaseOrderNo'), width: 150 },
    { field: 'supplierName', headerName: t('pages.receiving.fields.supplier'), width: 150 },
    { field: 'warehouseName', headerName: t('pages.receiving.fields.warehouse'), width: 120 },
    {
      field: 'receiptType',
      headerName: t('pages.receiving.fields.receiptType'),
      width: 100,
      valueFormatter: (params) => {
        const types: { [key: string]: string } = {
          PURCHASE: t('pages.receiving.types.purchase'),
          RETURN: t('pages.receiving.types.return'),
          TRANSFER: t('pages.receiving.types.transfer'),
          OTHER: t('pages.receiving.types.other'),
        };
        return types[params.value] || params.value;
      },
    },
    {
      field: 'receiptStatus',
      headerName: t('pages.receiving.fields.receiptStatus'),
      width: 120,
      renderCell: (params) => {
        const statusColors: { [key: string]: 'default' | 'warning' | 'success' | 'error' } = {
          PENDING: 'warning',
          INSPECTING: 'default',
          COMPLETED: 'success',
          REJECTED: 'error',
          CANCELLED: 'default',
        };
        const statusLabels: { [key: string]: string } = {
          PENDING: t('pages.receiving.status.pending'),
          INSPECTING: t('pages.receiving.status.inspecting'),
          COMPLETED: t('pages.receiving.status.completed'),
          REJECTED: t('pages.receiving.status.rejected'),
          CANCELLED: t('pages.receiving.status.cancelled'),
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
    { field: 'receiverName', headerName: t('pages.receiving.fields.receiverName'), width: 120 },
    {
      field: 'actions',
      type: 'actions',
      headerName: t('common.labels.actions'),
      width: 150,
      getActions: (params) => [
        <GridActionsCellItem
          icon={<VisibilityIcon />}
          label={t('pages.receiving.actions.view')}
          onClick={() => handleOpenDialog(params.row, true)}
          showInMenu
        />,
        <GridActionsCellItem
          icon={<CheckCircleIcon />}
          label={t('pages.receiving.actions.complete')}
          onClick={() => handleComplete(params.row.goodsReceiptId)}
          disabled={params.row.receiptStatus !== 'PENDING' && params.row.receiptStatus !== 'INSPECTING'}
          showInMenu
        />,
        <GridActionsCellItem
          icon={<CancelIcon />}
          label={t('pages.receiving.actions.cancel')}
          onClick={() => handleCancel(params.row.goodsReceiptId)}
          disabled={params.row.receiptStatus === 'COMPLETED' || params.row.receiptStatus === 'CANCELLED'}
          showInMenu
        />,
        <GridActionsCellItem
          icon={<DeleteIcon />}
          label={t('pages.receiving.actions.delete')}
          onClick={() => handleOpenDeleteDialog(params.row)}
          disabled={params.row.receiptStatus === 'COMPLETED'}
          showInMenu
        />,
      ],
    },
  ];

  return (
    <Box sx={{ height: '100%', p: 3 }}>
      <Paper sx={{ p: 2, mb: 2 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="h5">{t('pages.receiving.title')}</Typography>
          <Button variant="contained" startIcon={<AddIcon />} onClick={() => handleOpenDialog()}>
            {t('pages.receiving.actions.create')}
          </Button>
        </Box>
      </Paper>

      <Paper sx={{ height: 'calc(100vh - 250px)' }}>
        <DataGrid
          rows={receipts}
          columns={columns}
          loading={loading}
          getRowId={(row) => row.goodsReceiptId}
          pageSizeOptions={[10, 25, 50, 100]}
          initialState={{
            pagination: { paginationModel: { pageSize: 25 } },
          }}
        />
      </Paper>

      {/* Create/View Dialog */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="md" fullWidth>
        <DialogTitle>{viewMode ? t('pages.receiving.dialogs.detailTitle') : t('pages.receiving.dialogs.createTitle')}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12} sm={6}>
              <TextField fullWidth label={t('pages.receiving.fields.receiptNo')} value={formData.receiptNo} onChange={(e) => setFormData({ ...formData, receiptNo: e.target.value })} required disabled={viewMode} />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField fullWidth label={t('pages.receiving.fields.receiptDate')} type="datetime-local" value={formData.receiptDate} onChange={(e) => setFormData({ ...formData, receiptDate: e.target.value })} required disabled={viewMode} InputLabelProps={{ shrink: true }} />
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth>
                <InputLabel>{t('pages.receiving.fields.purchaseOrder')}</InputLabel>
                <Select value={formData.purchaseOrderId || ''} onChange={(e) => setFormData({ ...formData, purchaseOrderId: e.target.value as number })} label={t('pages.receiving.fields.purchaseOrder')} disabled={viewMode}>
                  <MenuItem value=""><em>{t('pages.receiving.fields.noneSelected')}</em></MenuItem>
                  {purchaseOrders.map((po) => (<MenuItem key={po.purchaseOrderId} value={po.purchaseOrderId}>{po.orderNo}</MenuItem>))}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth required>
                <InputLabel>{t('pages.receiving.fields.warehouse')}</InputLabel>
                <Select value={formData.warehouseId || ''} onChange={(e) => setFormData({ ...formData, warehouseId: e.target.value as number })} label={t('pages.receiving.fields.warehouse')} disabled={viewMode}>
                  {warehouses.map((wh) => (<MenuItem key={wh.warehouseId} value={wh.warehouseId}>{wh.warehouseName}</MenuItem>))}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth required>
                <InputLabel>{t('pages.receiving.fields.receiptTypeLabel')}</InputLabel>
                <Select value={formData.receiptType} onChange={(e) => setFormData({ ...formData, receiptType: e.target.value })} label={t('pages.receiving.fields.receiptTypeLabel')} disabled={viewMode}>
                  <MenuItem value="PURCHASE">{t('pages.receiving.types.purchase')}</MenuItem>
                  <MenuItem value="RETURN">{t('pages.receiving.types.return')}</MenuItem>
                  <MenuItem value="TRANSFER">{t('pages.receiving.types.transfer')}</MenuItem>
                  <MenuItem value="OTHER">{t('pages.receiving.types.other')}</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField fullWidth label={t('pages.receiving.fields.receiverName')} value={formData.receiverName} onChange={(e) => setFormData({ ...formData, receiverName: e.target.value })} disabled={viewMode} />
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
        <DialogTitle>{t('pages.receiving.dialogs.deleteTitle')}</DialogTitle>
        <DialogContent>
          <Typography>{t('pages.receiving.dialogs.confirmDelete')}</Typography>
          {selectedReceipt && (
            <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
              {t('pages.receiving.dialogs.receiptNoLabel')}: {selectedReceipt.receiptNo}
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

export default ReceivingPage;
