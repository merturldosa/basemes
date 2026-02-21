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
import productService, { Product, ProductCreateRequest, ProductUpdateRequest } from '../../services/productService';
import { getErrorMessage } from '@/utils/errorUtils';

const ProductsPage: React.FC = () => {
  const { t } = useTranslation();
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [openDialog, setOpenDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null);
  const [formData, setFormData] = useState<ProductCreateRequest | ProductUpdateRequest>({
    productCode: '',
    productName: '',
    productType: '',
    specification: '',
    unit: 'EA',
    standardCycleTime: undefined,
    remarks: '',
  });
  const [snackbar, setSnackbar] = useState<{ open: boolean; message: string; severity: 'success' | 'error' }>({
    open: false,
    message: '',
    severity: 'success',
  });

  useEffect(() => {
    loadProducts();
    // eslint-disable-next-line react-hooks/exhaustive-deps -- load once on mount
  }, []);

  const loadProducts = async () => {
    try {
      setLoading(true);
      const data = await productService.getProducts();
      setProducts(data || []);
    } catch (error) {
      showSnackbar(t('pages.products.errors.loadFailed'), 'error');
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

  const handleOpenDialog = (product?: Product) => {
    if (product) {
      setSelectedProduct(product);
      setFormData({
        productName: product.productName,
        productType: product.productType || '',
        specification: product.specification || '',
        unit: product.unit,
        standardCycleTime: product.standardCycleTime,
        remarks: product.remarks || '',
      });
    } else {
      setSelectedProduct(null);
      setFormData({
        productCode: '',
        productName: '',
        productType: '',
        specification: '',
        unit: 'EA',
        standardCycleTime: undefined,
        remarks: '',
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedProduct(null);
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: name === 'standardCycleTime' && value ? Number(value) : value,
    });
  };

  const handleSubmit = async () => {
    try {
      if (selectedProduct) {
        // Update
        await productService.updateProduct(selectedProduct.productId, formData as ProductUpdateRequest);
        showSnackbar(t('pages.products.messages.updateSuccess'), 'success');
      } else {
        // Create
        await productService.createProduct(formData as ProductCreateRequest);
        showSnackbar(t('pages.products.messages.createSuccess'), 'success');
      }
      handleCloseDialog();
      loadProducts();
    } catch (error) {
      showSnackbar(getErrorMessage(error, t('pages.products.errors.saveFailed')), 'error');
    }
  };

  const handleToggleActive = async (product: Product) => {
    try {
      if (product.isActive) {
        await productService.deactivateProduct(product.productId);
        showSnackbar(t('pages.products.messages.deactivateSuccess'), 'success');
      } else {
        await productService.activateProduct(product.productId);
        showSnackbar(t('pages.products.messages.activateSuccess'), 'success');
      }
      loadProducts();
    } catch (error) {
      showSnackbar(getErrorMessage(error, t('pages.products.errors.statusChangeFailed')), 'error');
    }
  };

  const handleOpenDeleteDialog = (product: Product) => {
    setSelectedProduct(product);
    setOpenDeleteDialog(true);
  };

  const handleCloseDeleteDialog = () => {
    setOpenDeleteDialog(false);
    setSelectedProduct(null);
  };

  const handleDelete = async () => {
    if (!selectedProduct) return;

    try {
      await productService.deleteProduct(selectedProduct.productId);
      showSnackbar(t('pages.products.messages.deleteSuccess'), 'success');
      handleCloseDeleteDialog();
      loadProducts();
    } catch (error) {
      showSnackbar(getErrorMessage(error, t('pages.products.errors.deleteFailed')), 'error');
    }
  };

  const columns: GridColDef[] = [
    { field: 'productCode', headerName: t('pages.products.fields.productCode'), width: 130 },
    { field: 'productName', headerName: t('pages.products.fields.productName'), flex: 1, minWidth: 200 },
    { field: 'productType', headerName: t('pages.products.fields.productType'), width: 120 },
    { field: 'specification', headerName: t('pages.products.fields.specification'), width: 200 },
    { field: 'unit', headerName: t('pages.products.fields.unit'), width: 80 },
    {
      field: 'standardCycleTime',
      headerName: t('pages.products.fields.standardCycleTime'),
      width: 160,
      valueFormatter: (params) => params.value ? `${params.value}${t('pages.products.fields.seconds')}` : '-',
    },
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
      getActions: (params: GridRowParams<Product>) => [
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
          {t('pages.products.title')}
        </Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => handleOpenDialog()}
        >
          {t('pages.products.actions.create')}
        </Button>
      </Box>

      <Paper>
        <DataGrid
          rows={products}
          columns={columns}
          getRowId={(row) => row.productId}
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
        <DialogTitle>{selectedProduct ? t('pages.products.dialog.editTitle') : t('pages.products.dialog.createTitle')}</DialogTitle>
        <DialogContent>
          <Box display="flex" flexDirection="column" gap={2} mt={1}>
            {!selectedProduct && (
              <TextField
                name="productCode"
                label={t('pages.products.fields.productCode')}
                value={(formData as ProductCreateRequest).productCode || ''}
                onChange={handleInputChange}
                required
                fullWidth
              />
            )}
            <TextField
              name="productName"
              label={t('pages.products.fields.productName')}
              value={formData.productName || ''}
              onChange={handleInputChange}
              required
              fullWidth
            />
            <TextField
              name="productType"
              label={t('pages.products.fields.productType')}
              value={formData.productType || ''}
              onChange={handleInputChange}
              placeholder={t('pages.products.placeholders.productType')}
              fullWidth
            />
            <TextField
              name="specification"
              label={t('pages.products.fields.specification')}
              value={formData.specification || ''}
              onChange={handleInputChange}
              multiline
              rows={2}
              fullWidth
            />
            <TextField
              name="unit"
              label={t('pages.products.fields.unit')}
              value={formData.unit || 'EA'}
              onChange={handleInputChange}
              placeholder={t('pages.products.placeholders.unit')}
              required
              fullWidth
            />
            <TextField
              name="standardCycleTime"
              label={t('pages.products.fields.standardCycleTime')}
              type="number"
              value={formData.standardCycleTime || ''}
              onChange={handleInputChange}
              fullWidth
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
            {selectedProduct ? t('common.buttons.edit') : t('pages.products.actions.register')}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={openDeleteDialog} onClose={handleCloseDeleteDialog}>
        <DialogTitle>{t('pages.products.dialog.deleteTitle')}</DialogTitle>
        <DialogContent>
          <Alert severity="warning" sx={{ mb: 2 }}>
            {t('pages.products.dialog.deleteWarning')}
          </Alert>
          <Typography>
            {t('pages.products.dialog.deleteConfirm', { name: selectedProduct?.productName })}
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

export default ProductsPage;
