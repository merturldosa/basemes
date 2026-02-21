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
  Search as InvestigateIcon,
  CheckCircle as ResolveIcon,
  Close as CloseIcon,
  Delete as DeleteIcon,
  Visibility as VisibilityIcon,
} from '@mui/icons-material';
import { useTranslation } from 'react-i18next';
import claimService, { Claim, ClaimRequest } from '../../services/claimService';
import customerService, { Customer } from '../../services/customerService';
import productService, { Product } from '../../services/productService';

const ClaimsPage: React.FC = () => {
  const { t } = useTranslation();
  const [claims, setClaims] = useState<Claim[]>([]);
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(false);
  const [openDialog, setOpenDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [selectedClaim, setSelectedClaim] = useState<Claim | null>(null);
  const [viewMode, setViewMode] = useState(false);
  const [snackbar, setSnackbar] = useState<{
    open: boolean;
    message: string;
    severity: 'success' | 'error' | 'info';
  }>({ open: false, message: '', severity: 'success' });

  const [formData, setFormData] = useState<ClaimRequest>({
    claimNo: '',
    claimDate: new Date().toISOString().slice(0, 16),
    customerId: 0,
    claimDescription: '',
    status: 'RECEIVED',
  });

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const [claimsData, customersData, productsData] = await Promise.all([
        claimService.getAll(),
        customerService.getActive(),
        productService.getActiveProducts(),
      ]);
      setClaims(claimsData);
      setCustomers(customersData);
      setProducts(productsData);
    } catch (error) {
      setSnackbar({ open: true, message: t('pages.claims.errors.loadFailed'), severity: 'error' });
    } finally {
      setLoading(false);
    }
  };

  const handleOpenDialog = (claim?: Claim, view = false) => {
    if (claim) {
      setViewMode(view);
      setSelectedClaim(claim);
      setFormData({
        claimNo: claim.claimNo,
        claimDate: claim.claimDate,
        customerId: claim.customerId,
        claimDescription: claim.claimDescription,
        status: claim.status,
        contactPerson: claim.contactPerson,
        contactPhone: claim.contactPhone,
        productId: claim.productId,
        claimType: claim.claimType,
        severity: claim.severity,
        priority: claim.priority,
        remarks: claim.remarks,
      });
    } else {
      setViewMode(false);
      setSelectedClaim(null);
      setFormData({
        claimNo: '',
        claimDate: new Date().toISOString().slice(0, 16),
        customerId: 0,
        claimDescription: '',
        status: 'RECEIVED',
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedClaim(null);
    setViewMode(false);
  };

  const handleOpenDeleteDialog = (claim: Claim) => {
    setSelectedClaim(claim);
    setOpenDeleteDialog(true);
  };

  const handleCloseDeleteDialog = () => {
    setOpenDeleteDialog(false);
    setSelectedClaim(null);
  };

  const handleSubmit = async () => {
    try {
      await claimService.create(formData);
      setSnackbar({ open: true, message: t('pages.claims.messages.createSuccess'), severity: 'success' });
      handleCloseDialog();
      loadData();
    } catch (error) {
      setSnackbar({ open: true, message: t('pages.claims.errors.createFailed'), severity: 'error' });
    }
  };

  const handleDelete = async () => {
    if (!selectedClaim) return;

    try {
      await claimService.delete(selectedClaim.claimId);
      setSnackbar({ open: true, message: t('pages.claims.messages.deleteSuccess'), severity: 'success' });
      handleCloseDeleteDialog();
      loadData();
    } catch (error) {
      setSnackbar({ open: true, message: t('pages.claims.errors.deleteFailed'), severity: 'error' });
    }
  };

  const handleInvestigate = async (id: number) => {
    try {
      await claimService.investigate(id);
      setSnackbar({ open: true, message: t('pages.claims.messages.investigateSuccess'), severity: 'success' });
      loadData();
    } catch (error) {
      setSnackbar({ open: true, message: t('pages.claims.errors.investigateFailed'), severity: 'error' });
    }
  };

  const handleResolve = async (id: number) => {
    try {
      await claimService.resolve(id);
      setSnackbar({ open: true, message: t('pages.claims.messages.resolveSuccess'), severity: 'success' });
      loadData();
    } catch (error) {
      setSnackbar({ open: true, message: t('pages.claims.errors.resolveFailed'), severity: 'error' });
    }
  };

  const handleClose = async (id: number) => {
    try {
      await claimService.close(id);
      setSnackbar({ open: true, message: t('pages.claims.messages.closeSuccess'), severity: 'success' });
      loadData();
    } catch (error) {
      setSnackbar({ open: true, message: t('pages.claims.errors.closeFailed'), severity: 'error' });
    }
  };

  const columns: GridColDef[] = [
    { field: 'claimNo', headerName: t('pages.claims.fields.claimNo'), width: 150 },
    {
      field: 'claimDate',
      headerName: t('pages.claims.fields.claimDate'),
      width: 180,
      valueFormatter: (params) => new Date(params.value).toLocaleString('ko-KR'),
    },
    { field: 'customerName', headerName: t('pages.claims.fields.customer'), width: 150 },
    { field: 'productName', headerName: t('pages.claims.fields.product'), width: 150 },
    {
      field: 'claimType',
      headerName: t('pages.claims.fields.claimType'),
      width: 120,
      valueFormatter: (params) => {
        const types: { [key: string]: string } = {
          QUALITY: t('pages.claims.claimTypes.quality'),
          DELIVERY: t('pages.claims.claimTypes.delivery'),
          QUANTITY: t('pages.claims.claimTypes.quantity'),
          PACKAGING: t('pages.claims.claimTypes.packaging'),
          DOCUMENTATION: t('pages.claims.claimTypes.documentation'),
          SERVICE: t('pages.claims.claimTypes.service'),
          PRICE: t('pages.claims.claimTypes.price'),
          OTHER: t('pages.claims.claimTypes.other'),
        };
        return types[params.value] || params.value;
      },
    },
    {
      field: 'severity',
      headerName: t('pages.claims.fields.severity'),
      width: 100,
      renderCell: (params) => {
        const severityColors: { [key: string]: 'error' | 'warning' | 'default' } = {
          CRITICAL: 'error',
          MAJOR: 'warning',
          MINOR: 'default',
        };
        const severityLabels: { [key: string]: string } = {
          CRITICAL: t('pages.claims.severity.critical'),
          MAJOR: t('pages.claims.severity.major'),
          MINOR: t('pages.claims.severity.minor'),
        };
        return params.value ? (
          <Chip
            label={severityLabels[params.value] || params.value}
            color={severityColors[params.value] || 'default'}
            size="small"
          />
        ) : null;
      },
    },
    {
      field: 'status',
      headerName: t('common.labels.status'),
      width: 120,
      renderCell: (params) => {
        const statusColors: { [key: string]: 'default' | 'warning' | 'info' | 'success' | 'error' } = {
          RECEIVED: 'warning',
          INVESTIGATING: 'info',
          IN_PROGRESS: 'info',
          RESOLVED: 'success',
          CLOSED: 'default',
          REJECTED: 'error',
        };
        const statusLabels: { [key: string]: string } = {
          RECEIVED: t('pages.claims.status.received'),
          INVESTIGATING: t('pages.claims.status.investigating'),
          IN_PROGRESS: t('pages.claims.status.inProgress'),
          RESOLVED: t('pages.claims.status.resolved'),
          CLOSED: t('pages.claims.status.closed'),
          REJECTED: t('pages.claims.status.rejected'),
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
      width: 200,
      getActions: (params) => [
        <GridActionsCellItem
          icon={<VisibilityIcon />}
          label={t('pages.claims.actions.view')}
          onClick={() => handleOpenDialog(params.row, true)}
        />,
        <GridActionsCellItem
          icon={<InvestigateIcon />}
          label={t('pages.claims.actions.investigate')}
          onClick={() => handleInvestigate(params.row.claimId)}
          disabled={params.row.status !== 'RECEIVED'}
        />,
        <GridActionsCellItem
          icon={<ResolveIcon />}
          label={t('pages.claims.actions.resolve')}
          onClick={() => handleResolve(params.row.claimId)}
          disabled={!['INVESTIGATING', 'IN_PROGRESS'].includes(params.row.status)}
        />,
        <GridActionsCellItem
          icon={<CloseIcon />}
          label={t('pages.claims.actions.close')}
          onClick={() => handleClose(params.row.claimId)}
          disabled={params.row.status !== 'RESOLVED'}
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
          <Typography variant="h5">{t('pages.claims.title')}</Typography>
          <Button variant="contained" startIcon={<AddIcon />} onClick={() => handleOpenDialog()}>
            {t('pages.claims.actions.create')}
          </Button>
        </Box>
      </Paper>

      <Paper sx={{ height: 'calc(100vh - 250px)' }}>
        <DataGrid
          rows={claims}
          columns={columns}
          loading={loading}
          getRowId={(row) => row.claimId}
          pageSizeOptions={[10, 25, 50, 100]}
          initialState={{
            pagination: { paginationModel: { pageSize: 25 } },
          }}
        />
      </Paper>

      {/* Create/View Dialog */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="md" fullWidth>
        <DialogTitle>{viewMode ? t('pages.claims.dialog.viewTitle') : t('pages.claims.dialog.createTitle')}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label={t('pages.claims.fields.claimNo')}
                value={formData.claimNo}
                onChange={(e) => setFormData({ ...formData, claimNo: e.target.value })}
                required
                disabled={viewMode}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label={t('pages.claims.fields.claimDate')}
                type="datetime-local"
                value={formData.claimDate}
                onChange={(e) => setFormData({ ...formData, claimDate: e.target.value })}
                required
                disabled={viewMode}
                InputLabelProps={{ shrink: true }}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth required>
                <InputLabel>{t('pages.claims.fields.customer')}</InputLabel>
                <Select
                  value={formData.customerId || ''}
                  onChange={(e) => setFormData({ ...formData, customerId: e.target.value as number })}
                  label={t('pages.claims.fields.customer')}
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
              <FormControl fullWidth>
                <InputLabel>{t('pages.claims.fields.product')}</InputLabel>
                <Select
                  value={formData.productId || ''}
                  onChange={(e) => setFormData({ ...formData, productId: e.target.value as number })}
                  label={t('pages.claims.fields.product')}
                  disabled={viewMode}
                >
                  <MenuItem value="">
                    <em>{t('pages.claims.fields.noneSelected')}</em>
                  </MenuItem>
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
                label={t('pages.claims.fields.contactPerson')}
                value={formData.contactPerson || ''}
                onChange={(e) => setFormData({ ...formData, contactPerson: e.target.value })}
                disabled={viewMode}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label={t('pages.claims.fields.contactPhone')}
                value={formData.contactPhone || ''}
                onChange={(e) => setFormData({ ...formData, contactPhone: e.target.value })}
                disabled={viewMode}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth>
                <InputLabel>{t('pages.claims.fields.claimType')}</InputLabel>
                <Select
                  value={formData.claimType || ''}
                  onChange={(e) => setFormData({ ...formData, claimType: e.target.value })}
                  label={t('pages.claims.fields.claimType')}
                  disabled={viewMode}
                >
                  <MenuItem value="QUALITY">{t('pages.claims.claimTypes.quality')}</MenuItem>
                  <MenuItem value="DELIVERY">{t('pages.claims.claimTypes.delivery')}</MenuItem>
                  <MenuItem value="QUANTITY">{t('pages.claims.claimTypes.quantity')}</MenuItem>
                  <MenuItem value="PACKAGING">{t('pages.claims.claimTypes.packaging')}</MenuItem>
                  <MenuItem value="DOCUMENTATION">{t('pages.claims.claimTypes.documentation')}</MenuItem>
                  <MenuItem value="SERVICE">{t('pages.claims.claimTypes.service')}</MenuItem>
                  <MenuItem value="PRICE">{t('pages.claims.claimTypes.price')}</MenuItem>
                  <MenuItem value="OTHER">{t('pages.claims.claimTypes.other')}</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth>
                <InputLabel>{t('pages.claims.fields.severity')}</InputLabel>
                <Select
                  value={formData.severity || ''}
                  onChange={(e) => setFormData({ ...formData, severity: e.target.value })}
                  label={t('pages.claims.fields.severity')}
                  disabled={viewMode}
                >
                  <MenuItem value="CRITICAL">{t('pages.claims.severity.critical')}</MenuItem>
                  <MenuItem value="MAJOR">{t('pages.claims.severity.major')}</MenuItem>
                  <MenuItem value="MINOR">{t('pages.claims.severity.minor')}</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth>
                <InputLabel>{t('pages.claims.fields.priority')}</InputLabel>
                <Select
                  value={formData.priority || ''}
                  onChange={(e) => setFormData({ ...formData, priority: e.target.value })}
                  label={t('pages.claims.fields.priority')}
                  disabled={viewMode}
                >
                  <MenuItem value="URGENT">{t('pages.claims.priority.urgent')}</MenuItem>
                  <MenuItem value="HIGH">{t('pages.claims.priority.high')}</MenuItem>
                  <MenuItem value="NORMAL">{t('pages.claims.priority.normal')}</MenuItem>
                  <MenuItem value="LOW">{t('pages.claims.priority.low')}</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label={t('pages.claims.fields.claimDescription')}
                multiline
                rows={4}
                value={formData.claimDescription}
                onChange={(e) => setFormData({ ...formData, claimDescription: e.target.value })}
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
              {t('pages.claims.actions.createBtn')}
            </Button>
          )}
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={openDeleteDialog} onClose={handleCloseDeleteDialog}>
        <DialogTitle>{t('pages.claims.dialog.deleteTitle')}</DialogTitle>
        <DialogContent>
          <Typography>{t('pages.claims.dialog.deleteConfirm')}</Typography>
          {selectedClaim && (
            <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
              {t('pages.claims.fields.claimNo')}: {selectedClaim.claimNo}
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

export default ClaimsPage;
