import React, { useState, useEffect } from 'react';
import {
  Box,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Snackbar,
  Alert,
  IconButton,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
} from '@mui/material';
import { DataGrid, GridColDef, GridRenderCellParams } from '@mui/x-data-grid';
import { Delete as DeleteIcon, Refresh as RefreshIcon } from '@mui/icons-material';
import inventoryService, { Inventory } from '../../services/inventoryService';
import warehouseService, { Warehouse } from '../../services/warehouseService';
import productService, { Product } from '../../services/productService';

const InventoryPage: React.FC = () => {
  const [inventory, setInventory] = useState<Inventory[]>([]);
  const [warehouses, setWarehouses] = useState<Warehouse[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [selectedInventory, setSelectedInventory] = useState<Inventory | null>(null);
  const [selectedWarehouse, setSelectedWarehouse] = useState<number | ''>('');
  const [selectedProduct, setSelectedProduct] = useState<number | ''>('');
  const [snackbar, setSnackbar] = useState<{ open: boolean; message: string; severity: 'success' | 'error' }>({
    open: false,
    message: '',
    severity: 'success',
  });

  useEffect(() => {
    loadInventory();
    loadWarehouses();
    loadProducts();
  }, []);

  const loadInventory = async () => {
    try {
      setLoading(true);
      const data = await inventoryService.getAll();
      setInventory(data || []);
    } catch (error) {
      setSnackbar({ open: true, message: '재고 목록 조회 실패', severity: 'error' });
      setInventory([]);
    } finally {
      setLoading(false);
    }
  };

  const loadWarehouses = async () => {
    try {
      const data = await warehouseService.getAll();
      setWarehouses(data || []);
    } catch (error) {
      setWarehouses([]);
    }
  };

  const loadProducts = async () => {
    try {
      const data = await productService.getProducts();
      setProducts(data || []);
    } catch (error) {
      setProducts([]);
    }
  };

  const handleFilterByWarehouse = async (warehouseId: number | '') => {
    setSelectedWarehouse(warehouseId);
    if (warehouseId === '') {
      loadInventory();
      return;
    }

    try {
      setLoading(true);
      const data = await inventoryService.getByWarehouse(warehouseId);
      setInventory(data || []);
    } catch (error) {
      setSnackbar({ open: true, message: '창고별 재고 조회 실패', severity: 'error' });
      setInventory([]);
    } finally {
      setLoading(false);
    }
  };

  const handleFilterByProduct = async (productId: number | '') => {
    setSelectedProduct(productId);
    if (productId === '') {
      loadInventory();
      return;
    }

    try {
      setLoading(true);
      const data = await inventoryService.getByProduct(productId);
      setInventory(data || []);
    } catch (error) {
      setSnackbar({ open: true, message: '제품별 재고 조회 실패', severity: 'error' });
      setInventory([]);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async () => {
    if (!selectedInventory) return;

    try {
      await inventoryService.delete(selectedInventory.inventoryId);
      setSnackbar({ open: true, message: '재고 레코드가 삭제되었습니다', severity: 'success' });
      setOpenDeleteDialog(false);
      setSelectedInventory(null);
      loadInventory();
    } catch (error) {
      setSnackbar({ open: true, message: '재고 삭제 실패', severity: 'error' });
    }
  };

  const columns: GridColDef[] = [
    { field: 'warehouseCode', headerName: '창고 코드', width: 120 },
    { field: 'warehouseName', headerName: '창고명', width: 150 },
    { field: 'productCode', headerName: '제품 코드', width: 120 },
    { field: 'productName', headerName: '제품명', width: 200 },
    { field: 'lotNo', headerName: 'LOT 번호', width: 130 },
    {
      field: 'availableQuantity',
      headerName: '가용 수량',
      width: 120,
      renderCell: (params: GridRenderCellParams) => `${params.row.availableQuantity} ${params.row.unit}`,
    },
    {
      field: 'reservedQuantity',
      headerName: '예약 수량',
      width: 120,
      renderCell: (params: GridRenderCellParams) => `${params.row.reservedQuantity} ${params.row.unit}`,
    },
    { field: 'location', headerName: '위치', width: 130 },
    { field: 'lastTransactionType', headerName: '최종 이동 유형', width: 140 },
    {
      field: 'lastTransactionDate',
      headerName: '최종 이동 일시',
      width: 180,
      renderCell: (params: GridRenderCellParams) =>
        params.value ? new Date(params.value as string).toLocaleString('ko-KR') : '-',
    },
    {
      field: 'actions',
      headerName: '작업',
      width: 80,
      sortable: false,
      renderCell: (params: GridRenderCellParams) => (
        <IconButton
          size="small"
          onClick={() => {
            setSelectedInventory(params.row as Inventory);
            setOpenDeleteDialog(true);
          }}
        >
          <DeleteIcon fontSize="small" />
        </IconButton>
      ),
    },
  ];

  return (
    <Box sx={{ p: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
        <h2>재고 현황</h2>
        <Button variant="contained" startIcon={<RefreshIcon />} onClick={loadInventory}>
          새로고침
        </Button>
      </Box>

      <Box sx={{ display: 'flex', gap: 2, mb: 3 }}>
        <FormControl sx={{ minWidth: 200 }}>
          <InputLabel>창고 필터</InputLabel>
          <Select
            value={selectedWarehouse}
            onChange={(e) => handleFilterByWarehouse(e.target.value as number | '')}
            label="창고 필터"
          >
            <MenuItem value="">전체</MenuItem>
            {warehouses.map((warehouse) => (
              <MenuItem key={warehouse.warehouseId} value={warehouse.warehouseId}>
                {warehouse.warehouseCode} - {warehouse.warehouseName}
              </MenuItem>
            ))}
          </Select>
        </FormControl>

        <FormControl sx={{ minWidth: 200 }}>
          <InputLabel>제품 필터</InputLabel>
          <Select
            value={selectedProduct}
            onChange={(e) => handleFilterByProduct(e.target.value as number | '')}
            label="제품 필터"
          >
            <MenuItem value="">전체</MenuItem>
            {products.map((product) => (
              <MenuItem key={product.productId} value={product.productId}>
                {product.productCode} - {product.productName}
              </MenuItem>
            ))}
          </Select>
        </FormControl>
      </Box>

      <DataGrid
        rows={inventory}
        columns={columns}
        loading={loading}
        getRowId={(row) => row.inventoryId}
        pageSizeOptions={[10, 25, 50, 100]}
        initialState={{
          pagination: { paginationModel: { pageSize: 25 } },
        }}
        sx={{ height: 600 }}
      />

      <Dialog open={openDeleteDialog} onClose={() => setOpenDeleteDialog(false)}>
        <DialogTitle>재고 레코드 삭제</DialogTitle>
        <DialogContent>정말로 이 재고 레코드를 삭제하시겠습니까?</DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenDeleteDialog(false)}>취소</Button>
          <Button onClick={handleDelete} color="error" variant="contained">
            삭제
          </Button>
        </DialogActions>
      </Dialog>

      <Snackbar
        open={snackbar.open}
        autoHideDuration={3000}
        onClose={() => setSnackbar({ ...snackbar, open: false })}
      >
        <Alert severity={snackbar.severity}>{snackbar.message}</Alert>
      </Snackbar>
    </Box>
  );
};

export default InventoryPage;
