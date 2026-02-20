import React, { useState, useEffect } from 'react';
import {
  Box,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Snackbar,
  Alert,
  Chip,
} from '@mui/material';
import { DataGrid, GridColDef, GridRenderCellParams } from '@mui/x-data-grid';
import { Add as AddIcon } from '@mui/icons-material';
import inventoryTransactionService, {
  InventoryTransaction,
  InventoryTransactionCreateRequest,
} from '../../services/inventoryTransactionService';
import warehouseService, { Warehouse } from '../../services/warehouseService';
import productService, { Product } from '../../services/productService';
import lotService, { Lot } from '../../services/lotService';
import userService, { User } from '../../services/userService';

const InventoryTransactionsPage: React.FC = () => {
  const [transactions, setTransactions] = useState<InventoryTransaction[]>([]);
  const [warehouses, setWarehouses] = useState<Warehouse[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [lots, setLots] = useState<Lot[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(false);
  const [openDialog, setOpenDialog] = useState(false);
  const [formData, setFormData] = useState<Partial<InventoryTransactionCreateRequest>>({
    transactionType: 'IN_RECEIVE',
  });
  const [snackbar, setSnackbar] = useState<{ open: boolean; message: string; severity: 'success' | 'error' }>({
    open: false,
    message: '',
    severity: 'success',
  });

  useEffect(() => {
    loadTransactions();
    loadWarehouses();
    loadProducts();
    loadLots();
    loadUsers();
  }, []);

  const loadTransactions = async () => {
    try {
      setLoading(true);
      const data = await inventoryTransactionService.getAll();
      setTransactions(data || []);
    } catch (error) {
      setSnackbar({ open: true, message: '재고 이동 내역 조회 실패', severity: 'error' });
      setTransactions([]);
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

  const loadLots = async () => {
    try {
      const data = await lotService.getAll();
      setLots(data || []);
    } catch (error) {
      setLots([]);
    }
  };

  const loadUsers = async () => {
    try {
      const response = await userService.getUsers();
      setUsers(response?.content || []);
    } catch (error) {
      setUsers([]);
    }
  };

  const handleOpenDialog = () => {
    setFormData({
      transactionType: 'IN_RECEIVE',
      transactionDate: new Date().toISOString().slice(0, 16),
    });
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setFormData({
      transactionType: 'IN_RECEIVE',
    });
  };

  const handleSubmit = async () => {
    try {
      await inventoryTransactionService.create(formData as InventoryTransactionCreateRequest);
      setSnackbar({ open: true, message: '재고 이동이 생성되었습니다', severity: 'success' });
      handleCloseDialog();
      loadTransactions();
    } catch (error) {
      setSnackbar({ open: true, message: '재고 이동 생성 실패', severity: 'error' });
    }
  };

  const getTransactionTypeLabel = (type: string) => {
    const types: { [key: string]: string } = {
      IN_RECEIVE: '입고 (수령)',
      IN_PRODUCTION: '입고 (생산)',
      IN_RETURN: '입고 (반품)',
      OUT_ISSUE: '출고 (불출)',
      OUT_SCRAP: '출고 (폐기)',
      MOVE: '이동',
      ADJUST: '조정',
    };
    return types[type] || type;
  };

  const getTransactionTypeColor = (type: string) => {
    if (type.startsWith('IN_')) return 'success';
    if (type.startsWith('OUT_')) return 'error';
    if (type === 'MOVE') return 'info';
    if (type === 'ADJUST') return 'warning';
    return 'default';
  };

  const columns: GridColDef[] = [
    { field: 'transactionNo', headerName: '이동 번호', width: 150 },
    {
      field: 'transactionType',
      headerName: '이동 유형',
      width: 140,
      renderCell: (params: GridRenderCellParams) => (
        <Chip
          label={getTransactionTypeLabel(params.value as string)}
          color={getTransactionTypeColor(params.value as string)}
          size="small"
        />
      ),
    },
    {
      field: 'transactionDate',
      headerName: '이동 일시',
      width: 180,
      renderCell: (params: GridRenderCellParams) => new Date(params.value as string).toLocaleString('ko-KR'),
    },
    { field: 'warehouseCode', headerName: '창고', width: 100 },
    { field: 'productCode', headerName: '제품 코드', width: 120 },
    { field: 'productName', headerName: '제품명', width: 180 },
    { field: 'lotNo', headerName: 'LOT', width: 120 },
    {
      field: 'quantity',
      headerName: '수량',
      width: 120,
      renderCell: (params: GridRenderCellParams) => `${params.row.quantity} ${params.row.unit}`,
    },
    { field: 'fromWarehouseCode', headerName: '출발 창고', width: 110 },
    { field: 'toWarehouseCode', headerName: '도착 창고', width: 110 },
    { field: 'transactionUserName', headerName: '처리자', width: 120 },
    {
      field: 'approvalStatus',
      headerName: '승인 상태',
      width: 100,
      renderCell: (params: GridRenderCellParams) => (
        <Chip
          label={params.value === 'APPROVED' ? '승인' : '대기'}
          color={params.value === 'APPROVED' ? 'success' : 'default'}
          size="small"
        />
      ),
    },
  ];

  return (
    <Box sx={{ p: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
        <h2>재고 이동 내역</h2>
        <Button variant="contained" startIcon={<AddIcon />} onClick={handleOpenDialog}>
          재고 이동 생성
        </Button>
      </Box>

      <DataGrid
        rows={transactions}
        columns={columns}
        loading={loading}
        getRowId={(row) => row.transactionId}
        pageSizeOptions={[10, 25, 50, 100]}
        initialState={{
          pagination: { paginationModel: { pageSize: 25 } },
        }}
        sx={{ height: 600 }}
      />

      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
        <DialogTitle>재고 이동 생성</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 2 }}>
            <TextField
              label="이동 번호"
              value={formData.transactionNo || ''}
              onChange={(e) => setFormData({ ...formData, transactionNo: e.target.value })}
              required
              fullWidth
            />
            <FormControl fullWidth required>
              <InputLabel>이동 유형</InputLabel>
              <Select
                value={formData.transactionType || 'IN_RECEIVE'}
                onChange={(e) => setFormData({ ...formData, transactionType: e.target.value })}
                label="이동 유형"
              >
                <MenuItem value="IN_RECEIVE">입고 (수령)</MenuItem>
                <MenuItem value="IN_PRODUCTION">입고 (생산)</MenuItem>
                <MenuItem value="IN_RETURN">입고 (반품)</MenuItem>
                <MenuItem value="OUT_ISSUE">출고 (불출)</MenuItem>
                <MenuItem value="OUT_SCRAP">출고 (폐기)</MenuItem>
                <MenuItem value="MOVE">이동</MenuItem>
                <MenuItem value="ADJUST">조정</MenuItem>
              </Select>
            </FormControl>
            <TextField
              label="이동 일시"
              type="datetime-local"
              value={formData.transactionDate || ''}
              onChange={(e) => setFormData({ ...formData, transactionDate: e.target.value })}
              InputLabelProps={{ shrink: true }}
              required
              fullWidth
            />
            <FormControl fullWidth required>
              <InputLabel>창고</InputLabel>
              <Select
                value={formData.warehouseId || ''}
                onChange={(e) => setFormData({ ...formData, warehouseId: Number(e.target.value) })}
                label="창고"
              >
                {warehouses.map((warehouse) => (
                  <MenuItem key={warehouse.warehouseId} value={warehouse.warehouseId}>
                    {warehouse.warehouseCode} - {warehouse.warehouseName}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
            <FormControl fullWidth required>
              <InputLabel>제품</InputLabel>
              <Select
                value={formData.productId || ''}
                onChange={(e) => setFormData({ ...formData, productId: Number(e.target.value) })}
                label="제품"
              >
                {products.map((product) => (
                  <MenuItem key={product.productId} value={product.productId}>
                    {product.productCode} - {product.productName}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
            <FormControl fullWidth>
              <InputLabel>LOT</InputLabel>
              <Select
                value={formData.lotId || ''}
                onChange={(e) => setFormData({ ...formData, lotId: Number(e.target.value) || undefined })}
                label="LOT"
              >
                <MenuItem value="">없음</MenuItem>
                {lots.map((lot) => (
                  <MenuItem key={lot.lotId} value={lot.lotId}>
                    {lot.lotNo}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
            <Box sx={{ display: 'flex', gap: 2 }}>
              <TextField
                label="수량"
                type="number"
                value={formData.quantity || ''}
                onChange={(e) => setFormData({ ...formData, quantity: Number(e.target.value) })}
                required
                fullWidth
              />
              <TextField
                label="단위"
                value={formData.unit || ''}
                onChange={(e) => setFormData({ ...formData, unit: e.target.value })}
                required
                fullWidth
              />
            </Box>
            {formData.transactionType === 'MOVE' && (
              <>
                <FormControl fullWidth>
                  <InputLabel>출발 창고</InputLabel>
                  <Select
                    value={formData.fromWarehouseId || ''}
                    onChange={(e) =>
                      setFormData({ ...formData, fromWarehouseId: Number(e.target.value) || undefined })
                    }
                    label="출발 창고"
                  >
                    <MenuItem value="">없음</MenuItem>
                    {warehouses.map((warehouse) => (
                      <MenuItem key={warehouse.warehouseId} value={warehouse.warehouseId}>
                        {warehouse.warehouseCode} - {warehouse.warehouseName}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
                <FormControl fullWidth>
                  <InputLabel>도착 창고</InputLabel>
                  <Select
                    value={formData.toWarehouseId || ''}
                    onChange={(e) =>
                      setFormData({ ...formData, toWarehouseId: Number(e.target.value) || undefined })
                    }
                    label="도착 창고"
                  >
                    <MenuItem value="">없음</MenuItem>
                    {warehouses.map((warehouse) => (
                      <MenuItem key={warehouse.warehouseId} value={warehouse.warehouseId}>
                        {warehouse.warehouseCode} - {warehouse.warehouseName}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </>
            )}
            <FormControl fullWidth required>
              <InputLabel>처리자</InputLabel>
              <Select
                value={formData.transactionUserId || ''}
                onChange={(e) => setFormData({ ...formData, transactionUserId: Number(e.target.value) })}
                label="처리자"
              >
                {users.map((user) => (
                  <MenuItem key={user.userId} value={user.userId}>
                    {user.username}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
            <TextField
              label="참조 번호"
              value={formData.referenceNo || ''}
              onChange={(e) => setFormData({ ...formData, referenceNo: e.target.value })}
              fullWidth
            />
            <TextField
              label="비고"
              value={formData.remarks || ''}
              onChange={(e) => setFormData({ ...formData, remarks: e.target.value })}
              multiline
              rows={3}
              fullWidth
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>취소</Button>
          <Button onClick={handleSubmit} variant="contained">
            생성
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

export default InventoryTransactionsPage;
