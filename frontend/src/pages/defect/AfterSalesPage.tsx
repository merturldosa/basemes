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
  PlayArrow as StartIcon,
  CheckCircle as CompleteIcon,
  Close as CloseIcon,
  Delete as DeleteIcon,
  Visibility as VisibilityIcon,
} from '@mui/icons-material';
import { useTranslation } from 'react-i18next';
import afterSalesService, { AfterSales, AfterSalesRequest } from '../../services/afterSalesService';
import customerService, { Customer } from '../../services/customerService';
import productService, { Product } from '../../services/productService';

const AfterSalesPage: React.FC = () => {
  const { t } = useTranslation();
  const [afterSales, setAfterSales] = useState<AfterSales[]>([]);
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(false);
  const [openDialog, setOpenDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [selectedAfterSales, setSelectedAfterSales] = useState<AfterSales | null>(null);
  const [viewMode, setViewMode] = useState(false);
  const [snackbar, setSnackbar] = useState<{
    open: boolean;
    message: string;
    severity: 'success' | 'error' | 'info';
  }>({ open: false, message: '', severity: 'success' });

  const [formData, setFormData] = useState<AfterSalesRequest>({
    asNo: '',
    receiptDate: new Date().toISOString().slice(0, 16),
    customerId: 0,
    productId: 0,
    issueDescription: '',
    serviceStatus: 'RECEIVED',
  });

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const [afterSalesData, customersData, productsData] = await Promise.all([
        afterSalesService.getAll(),
        customerService.getActive(),
        productService.getActiveProducts(),
      ]);
      setAfterSales(afterSalesData);
      setCustomers(customersData);
      setProducts(productsData);
    } catch (error) {
      setSnackbar({ open: true, message: t('pages.afterSales.errors.loadFailed'), severity: 'error' });
    } finally {
      setLoading(false);
    }
  };

  const handleOpenDialog = (item?: AfterSales, view = false) => {
    if (item) {
      setViewMode(view);
      setSelectedAfterSales(item);
      setFormData({
        asNo: item.asNo,
        receiptDate: item.receiptDate,
        customerId: item.customerId,
        productId: item.productId,
        issueDescription: item.issueDescription,
        serviceStatus: item.serviceStatus,
        contactPerson: item.contactPerson,
        contactPhone: item.contactPhone,
        issueCategory: item.issueCategory,
        priority: item.priority,
        remarks: item.remarks,
      });
    } else {
      setViewMode(false);
      setSelectedAfterSales(null);
      setFormData({
        asNo: '',
        receiptDate: new Date().toISOString().slice(0, 16),
        customerId: 0,
        productId: 0,
        issueDescription: '',
        serviceStatus: 'RECEIVED',
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedAfterSales(null);
    setViewMode(false);
  };

  const handleOpenDeleteDialog = (item: AfterSales) => {
    setSelectedAfterSales(item);
    setOpenDeleteDialog(true);
  };

  const handleCloseDeleteDialog = () => {
    setOpenDeleteDialog(false);
    setSelectedAfterSales(null);
  };

  const handleSubmit = async () => {
    try {
      await afterSalesService.create(formData);
      setSnackbar({ open: true, message: t('pages.afterSales.messages.createSuccess'), severity: 'success' });
      handleCloseDialog();
      loadData();
    } catch (error) {
      setSnackbar({ open: true, message: t('pages.afterSales.errors.createFailed'), severity: 'error' });
    }
  };

  const handleDelete = async () => {
    if (!selectedAfterSales) return;

    try {
      await afterSalesService.delete(selectedAfterSales.afterSalesId);
      setSnackbar({ open: true, message: t('pages.afterSales.messages.deleteSuccess'), severity: 'success' });
      handleCloseDeleteDialog();
      loadData();
    } catch (error) {
      setSnackbar({ open: true, message: t('pages.afterSales.errors.deleteFailed'), severity: 'error' });
    }
  };

  const handleStart = async (id: number) => {
    try {
      await afterSalesService.start(id);
      setSnackbar({ open: true, message: t('pages.afterSales.messages.startSuccess'), severity: 'success' });
      loadData();
    } catch (error) {
      setSnackbar({ open: true, message: t('pages.afterSales.errors.startFailed'), severity: 'error' });
    }
  };

  const handleComplete = async (id: number) => {
    try {
      await afterSalesService.complete(id);
      setSnackbar({ open: true, message: t('pages.afterSales.messages.completeSuccess'), severity: 'success' });
      loadData();
    } catch (error) {
      setSnackbar({ open: true, message: t('pages.afterSales.errors.completeFailed'), severity: 'error' });
    }
  };

  const handleClose = async (id: number) => {
    try {
      await afterSalesService.close(id);
      setSnackbar({ open: true, message: t('pages.afterSales.messages.closeSuccess'), severity: 'success' });
      loadData();
    } catch (error) {
      setSnackbar({ open: true, message: t('pages.afterSales.errors.closeFailed'), severity: 'error' });
    }
  };

  const columns: GridColDef[] = [
    { field: 'asNo', headerName: t('pages.afterSales.fields.asNo'), width: 150 },
    {
      field: 'receiptDate',
      headerName: t('pages.afterSales.fields.receiptDate'),
      width: 180,
      valueFormatter: (params) => new Date(params.value).toLocaleString('ko-KR'),
    },
    { field: 'customerName', headerName: t('pages.afterSales.fields.customer'), width: 150 },
    { field: 'productName', headerName: t('pages.afterSales.fields.product'), width: 150 },
    {
      field: 'priority',
      headerName: t('pages.afterSales.fields.priority'),
      width: 100,
      renderCell: (params) => {
        const priorityColors: { [key: string]: 'error' | 'warning' | 'info' | 'default' } = {
          URGENT: 'error',
          HIGH: 'warning',
          NORMAL: 'info',
          LOW: 'default',
        };
        const priorityLabels: { [key: string]: string } = {
          URGENT: t('pages.afterSales.priority.urgent'),
          HIGH: t('pages.afterSales.priority.high'),
          NORMAL: t('pages.afterSales.priority.normal'),
          LOW: t('pages.afterSales.priority.low'),
        };
        return (
          <Chip
            label={priorityLabels[params.value] || params.value || t('pages.afterSales.priority.normal')}
            color={priorityColors[params.value] || 'default'}
            size="small"
          />
        );
      },
    },
    {
      field: 'serviceStatus',
      headerName: t('common.labels.status'),
      width: 120,
      renderCell: (params) => {
        const statusColors: { [key: string]: 'default' | 'warning' | 'info' | 'success' | 'error' } = {
          RECEIVED: 'warning',
          IN_PROGRESS: 'info',
          COMPLETED: 'success',
          CLOSED: 'default',
          CANCELLED: 'error',
        };
        const statusLabels: { [key: string]: string } = {
          RECEIVED: t('pages.afterSales.status.received'),
          IN_PROGRESS: t('pages.afterSales.status.inProgress'),
          COMPLETED: t('pages.afterSales.status.completed'),
          CLOSED: t('pages.afterSales.status.closed'),
          CANCELLED: t('pages.afterSales.status.cancelled'),
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
    { field: 'assignedEngineerName', headerName: t('pages.afterSales.fields.assignedEngineer'), width: 120 },
    {
      field: 'actions',
      type: 'actions',
      headerName: t('common.labels.actions'),
      width: 200,
      getActions: (params) => [
        <GridActionsCellItem
          icon={<VisibilityIcon />}
          label={t('pages.afterSales.actions.view')}
          onClick={() => handleOpenDialog(params.row, true)}
        />,
        <GridActionsCellItem
          icon={<StartIcon />}
          label={t('pages.afterSales.actions.start')}
          onClick={() => handleStart(params.row.afterSalesId)}
          disabled={params.row.serviceStatus !== 'RECEIVED'}
        />,
        <GridActionsCellItem
          icon={<CompleteIcon />}
          label={t('pages.afterSales.actions.complete')}
          onClick={() => handleComplete(params.row.afterSalesId)}
          disabled={params.row.serviceStatus !== 'IN_PROGRESS'}
        />,
        <GridActionsCellItem
          icon={<CloseIcon />}
          label={t('pages.afterSales.actions.close')}
          onClick={() => handleClose(params.row.afterSalesId)}
          disabled={params.row.serviceStatus !== 'COMPLETED'}
        />,
        <GridActionsCellItem
          icon={<DeleteIcon />}
          label={t('common.buttons.delete')}
          onClick={() => handleOpenDeleteDialog(params.row)}
          disabled={params.row.serviceStatus === 'CLOSED'}
        />,
      ],
    },
  ];

  return (
    <Box sx={{ height: '100%', p: 3 }}>
      <Paper sx={{ p: 2, mb: 2 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="h5">{t('pages.afterSales.title')}</Typography>
          <Button variant="contained" startIcon={<AddIcon />} onClick={() => handleOpenDialog()}>
            {t('pages.afterSales.actions.create')}
          </Button>
        </Box>
      </Paper>

      <Paper sx={{ height: 'calc(100vh - 250px)' }}>
        <DataGrid
          rows={afterSales}
          columns={columns}
          loading={loading}
          getRowId={(row) => row.afterSalesId}
          pageSizeOptions={[10, 25, 50, 100]}
          initialState={{
            pagination: { paginationModel: { pageSize: 25 } },
          }}
        />
      </Paper>

      {/* Create/View Dialog */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="md" fullWidth>
        <DialogTitle>{viewMode ? t('pages.afterSales.dialog.viewTitle') : t('pages.afterSales.dialog.createTitle')}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label={t('pages.afterSales.fields.asNo')}
                value={formData.asNo}
                onChange={(e) => setFormData({ ...formData, asNo: e.target.value })}
                required
                disabled={viewMode}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label={t('pages.afterSales.fields.receiptDate')}
                type="datetime-local"
                value={formData.receiptDate}
                onChange={(e) => setFormData({ ...formData, receiptDate: e.target.value })}
                required
                disabled={viewMode}
                InputLabelProps={{ shrink: true }}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth required>
                <InputLabel>{t('pages.afterSales.fields.customer')}</InputLabel>
                <Select
                  value={formData.customerId || ''}
                  onChange={(e) => setFormData({ ...formData, customerId: e.target.value as number })}
                  label={t('pages.afterSales.fields.customer')}
                  disabled={viewMode}
                >
                  {customers.map((customer) => (
                    <MenuItem key={customer.customerId} value={customer.customerId}>
                      {customer.customerName}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth required>
                <InputLabel>{t('pages.afterSales.fields.product')}</InputLabel>
                <Select
                  value={formData.productId || ''}
                  onChange={(e) => setFormData({ ...formData, productId: e.target.value as number })}
                  label={t('pages.afterSales.fields.product')}
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
              <TextField
                fullWidth
                label={t('pages.afterSales.fields.contactPerson')}
                value={formData.contactPerson || ''}
                onChange={(e) => setFormData({ ...formData, contactPerson: e.target.value })}
                disabled={viewMode}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label={t('pages.afterSales.fields.contactPhone')}
                value={formData.contactPhone || ''}
                onChange={(e) => setFormData({ ...formData, contactPhone: e.target.value })}
                disabled={viewMode}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth>
                <InputLabel>{t('pages.afterSales.fields.issueCategory')}</InputLabel>
                <Select
                  value={formData.issueCategory || ''}
                  onChange={(e) => setFormData({ ...formData, issueCategory: e.target.value })}
                  label={t('pages.afterSales.fields.issueCategory')}
                  disabled={viewMode}
                >
                  <MenuItem value="DEFECT">{t('pages.afterSales.issueCategories.defect')}</MenuItem>
                  <MenuItem value="BREAKDOWN">{t('pages.afterSales.issueCategories.breakdown')}</MenuItem>
                  <MenuItem value="INSTALLATION">{t('pages.afterSales.issueCategories.installation')}</MenuItem>
                  <MenuItem value="USAGE">{t('pages.afterSales.issueCategories.usage')}</MenuItem>
                  <MenuItem value="OTHER">{t('pages.afterSales.issueCategories.other')}</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth>
                <InputLabel>{t('pages.afterSales.fields.priority')}</InputLabel>
                <Select
                  value={formData.priority || ''}
                  onChange={(e) => setFormData({ ...formData, priority: e.target.value })}
                  label={t('pages.afterSales.fields.priority')}
                  disabled={viewMode}
                >
                  <MenuItem value="URGENT">{t('pages.afterSales.priority.urgent')}</MenuItem>
                  <MenuItem value="HIGH">{t('pages.afterSales.priority.high')}</MenuItem>
                  <MenuItem value="NORMAL">{t('pages.afterSales.priority.normal')}</MenuItem>
                  <MenuItem value="LOW">{t('pages.afterSales.priority.low')}</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label={t('pages.afterSales.fields.issueDescription')}
                multiline
                rows={4}
                value={formData.issueDescription}
                onChange={(e) => setFormData({ ...formData, issueDescription: e.target.value })}
                required
                disabled={viewMode}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label={t('common.labels.remarks')}
                multiline
                rows={2}
                value={formData.remarks || ''}
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
              {t('pages.afterSales.actions.createBtn')}
            </Button>
          )}
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={openDeleteDialog} onClose={handleCloseDeleteDialog}>
        <DialogTitle>{t('pages.afterSales.dialog.deleteTitle')}</DialogTitle>
        <DialogContent>
          <Typography>{t('pages.afterSales.dialog.deleteConfirm')}</Typography>
          {selectedAfterSales && (
            <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
              {t('pages.afterSales.fields.asNo')}: {selectedAfterSales.asNo}
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

export default AfterSalesPage;
