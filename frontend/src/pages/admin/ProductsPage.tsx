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
  IconButton,
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
import productService, { Product, ProductCreateRequest, ProductUpdateRequest } from '../../services/productService';

const ProductsPage: React.FC = () => {
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
  }, []);

  const loadProducts = async () => {
    try {
      setLoading(true);
      const data = await productService.getProducts();
      setProducts(data);
    } catch (error) {
      showSnackbar('제품 목록 조회 실패', 'error');
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
        showSnackbar('제품 수정 성공', 'success');
      } else {
        // Create
        await productService.createProduct(formData as ProductCreateRequest);
        showSnackbar('제품 생성 성공', 'success');
      }
      handleCloseDialog();
      loadProducts();
    } catch (error: any) {
      showSnackbar(error.response?.data?.message || '작업 실패', 'error');
    }
  };

  const handleToggleActive = async (product: Product) => {
    try {
      if (product.isActive) {
        await productService.deactivateProduct(product.productId);
        showSnackbar('제품 비활성화 성공', 'success');
      } else {
        await productService.activateProduct(product.productId);
        showSnackbar('제품 활성화 성공', 'success');
      }
      loadProducts();
    } catch (error: any) {
      showSnackbar(error.response?.data?.message || '상태 변경 실패', 'error');
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
      showSnackbar('제품 삭제 성공', 'success');
      handleCloseDeleteDialog();
      loadProducts();
    } catch (error: any) {
      showSnackbar(error.response?.data?.message || '삭제 실패', 'error');
    }
  };

  const columns: GridColDef[] = [
    { field: 'productCode', headerName: '제품 코드', width: 130 },
    { field: 'productName', headerName: '제품명', flex: 1, minWidth: 200 },
    { field: 'productType', headerName: '제품 유형', width: 120 },
    { field: 'specification', headerName: '규격', width: 200 },
    { field: 'unit', headerName: '단위', width: 80 },
    {
      field: 'standardCycleTime',
      headerName: '표준 사이클 타임(초)',
      width: 160,
      valueFormatter: (params) => params.value ? `${params.value}초` : '-',
    },
    {
      field: 'isActive',
      headerName: '상태',
      width: 100,
      renderCell: (params) => (
        <Chip
          label={params.value ? '활성' : '비활성'}
          color={params.value ? 'success' : 'default'}
          size="small"
        />
      ),
    },
    {
      field: 'createdAt',
      headerName: '생성일',
      width: 180,
      valueFormatter: (params) => new Date(params.value).toLocaleString('ko-KR'),
    },
    {
      field: 'actions',
      type: 'actions',
      headerName: '작업',
      width: 150,
      getActions: (params: GridRowParams<Product>) => [
        <GridActionsCellItem
          icon={<EditIcon />}
          label="수정"
          onClick={() => handleOpenDialog(params.row)}
        />,
        <GridActionsCellItem
          icon={params.row.isActive ? <ToggleOffIcon /> : <ToggleOnIcon />}
          label={params.row.isActive ? '비활성화' : '활성화'}
          onClick={() => handleToggleActive(params.row)}
        />,
        <GridActionsCellItem
          icon={<DeleteIcon />}
          label="삭제"
          onClick={() => handleOpenDeleteDialog(params.row)}
        />,
      ],
    },
  ];

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h5" component="h1">
          제품 마스터 관리
        </Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => handleOpenDialog()}
        >
          신규 등록
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
        <DialogTitle>{selectedProduct ? '제품 수정' : '신규 제품 등록'}</DialogTitle>
        <DialogContent>
          <Box display="flex" flexDirection="column" gap={2} mt={1}>
            {!selectedProduct && (
              <TextField
                name="productCode"
                label="제품 코드"
                value={(formData as ProductCreateRequest).productCode || ''}
                onChange={handleInputChange}
                required
                fullWidth
              />
            )}
            <TextField
              name="productName"
              label="제품명"
              value={formData.productName || ''}
              onChange={handleInputChange}
              required
              fullWidth
            />
            <TextField
              name="productType"
              label="제품 유형"
              value={formData.productType || ''}
              onChange={handleInputChange}
              placeholder="예: 완제품, 반제품, 원자재"
              fullWidth
            />
            <TextField
              name="specification"
              label="규격"
              value={formData.specification || ''}
              onChange={handleInputChange}
              multiline
              rows={2}
              fullWidth
            />
            <TextField
              name="unit"
              label="단위"
              value={formData.unit || 'EA'}
              onChange={handleInputChange}
              placeholder="예: EA, KG, L"
              required
              fullWidth
            />
            <TextField
              name="standardCycleTime"
              label="표준 사이클 타임(초)"
              type="number"
              value={formData.standardCycleTime || ''}
              onChange={handleInputChange}
              fullWidth
            />
            <TextField
              name="remarks"
              label="비고"
              value={formData.remarks || ''}
              onChange={handleInputChange}
              multiline
              rows={3}
              fullWidth
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>취소</Button>
          <Button onClick={handleSubmit} variant="contained">
            {selectedProduct ? '수정' : '등록'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={openDeleteDialog} onClose={handleCloseDeleteDialog}>
        <DialogTitle>제품 삭제 확인</DialogTitle>
        <DialogContent>
          <Alert severity="warning" sx={{ mb: 2 }}>
            이 작업은 되돌릴 수 없습니다.
          </Alert>
          <Typography>
            제품 <strong>{selectedProduct?.productName}</strong>을(를) 삭제하시겠습니까?
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDeleteDialog}>취소</Button>
          <Button onClick={handleDelete} color="error" variant="contained">
            삭제
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
