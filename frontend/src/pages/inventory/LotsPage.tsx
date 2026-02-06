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
  IconButton,
} from '@mui/material';
import { DataGrid, GridColDef, GridRenderCellParams } from '@mui/x-data-grid';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
} from '@mui/icons-material';
import lotService, { Lot, LotCreateRequest, LotUpdateRequest } from '../../services/lotService';
import productService, { Product } from '../../services/productService';
import workOrderService, { WorkOrder } from '../../services/workOrderService';

const LotsPage: React.FC = () => {
  const [lots, setLots] = useState<Lot[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [workOrders, setWorkOrders] = useState<WorkOrder[]>([]);
  const [loading, setLoading] = useState(false);
  const [openDialog, setOpenDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [selectedLot, setSelectedLot] = useState<Lot | null>(null);
  const [formData, setFormData] = useState<Partial<LotCreateRequest>>({
    qualityStatus: 'PENDING',
  });
  const [snackbar, setSnackbar] = useState<{ open: boolean; message: string; severity: 'success' | 'error' }>({
    open: false,
    message: '',
    severity: 'success',
  });

  useEffect(() => {
    loadLots();
    loadProducts();
    loadWorkOrders();
  }, []);

  const loadLots = async () => {
    try {
      setLoading(true);
      const data = await lotService.getAll();
      setLots(data || []);
    } catch (error) {
      console.error('Failed to load lots:', error);
      setSnackbar({ open: true, message: 'LOT 목록 조회 실패', severity: 'error' });
      setLots([]);
    } finally {
      setLoading(false);
    }
  };

  const loadProducts = async () => {
    try {
      const data = await productService.getProducts();
      setProducts(data || []);
    } catch (error) {
      console.error('Failed to load products:', error);
      setProducts([]);
    }
  };

  const loadWorkOrders = async () => {
    try {
      const data = await workOrderService.getWorkOrders();
      setWorkOrders(data || []);
    } catch (error) {
      console.error('Failed to load work orders:', error);
      setWorkOrders([]);
    }
  };

  const handleOpenDialog = (lot?: Lot) => {
    if (lot) {
      setSelectedLot(lot);
      setFormData({
        lotNo: lot.lotNo,
        productId: lot.productId,
        workOrderId: lot.workOrderId,
        initialQuantity: lot.initialQuantity,
        currentQuantity: lot.currentQuantity,
        unit: lot.unit,
        manufactureDate: lot.manufactureDate,
        expiryDate: lot.expiryDate,
        qualityStatus: lot.qualityStatus,
        supplier: lot.supplier,
        supplierLotNo: lot.supplierLotNo,
        remarks: lot.remarks,
      });
    } else {
      setSelectedLot(null);
      setFormData({
        qualityStatus: 'PENDING',
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedLot(null);
    setFormData({
      qualityStatus: 'PENDING',
    });
  };

  const handleSubmit = async () => {
    try {
      if (selectedLot) {
        const updateRequest: LotUpdateRequest = {
          lotId: selectedLot.lotId,
          currentQuantity: formData.currentQuantity!,
          manufactureDate: formData.manufactureDate,
          expiryDate: formData.expiryDate,
          qualityStatus: formData.qualityStatus!,
          supplier: formData.supplier,
          supplierLotNo: formData.supplierLotNo,
          remarks: formData.remarks,
        };
        await lotService.update(selectedLot.lotId, updateRequest);
        setSnackbar({ open: true, message: 'LOT이 수정되었습니다', severity: 'success' });
      } else {
        await lotService.create(formData as LotCreateRequest);
        setSnackbar({ open: true, message: 'LOT이 생성되었습니다', severity: 'success' });
      }
      handleCloseDialog();
      loadLots();
    } catch (error) {
      console.error('Failed to save lot:', error);
      setSnackbar({ open: true, message: 'LOT 저장 실패', severity: 'error' });
    }
  };

  const handleDelete = async () => {
    if (!selectedLot) return;

    try {
      await lotService.delete(selectedLot.lotId);
      setSnackbar({ open: true, message: 'LOT이 삭제되었습니다', severity: 'success' });
      setOpenDeleteDialog(false);
      setSelectedLot(null);
      loadLots();
    } catch (error) {
      console.error('Failed to delete lot:', error);
      setSnackbar({ open: true, message: 'LOT 삭제 실패', severity: 'error' });
    }
  };

  const getQualityStatusLabel = (status: string) => {
    const statuses: { [key: string]: string } = {
      PENDING: '대기',
      PASSED: '합격',
      FAILED: '불합격',
      QUARANTINE: '격리',
    };
    return statuses[status] || status;
  };

  const getQualityStatusColor = (status: string) => {
    const colors: { [key: string]: 'default' | 'success' | 'error' | 'warning' } = {
      PENDING: 'default',
      PASSED: 'success',
      FAILED: 'error',
      QUARANTINE: 'warning',
    };
    return colors[status] || 'default';
  };

  const columns: GridColDef[] = [
    { field: 'lotNo', headerName: 'LOT 번호', width: 150 },
    { field: 'productCode', headerName: '제품 코드', width: 120 },
    { field: 'productName', headerName: '제품명', width: 200 },
    { field: 'workOrderNo', headerName: '작업 지시', width: 130 },
    {
      field: 'currentQuantity',
      headerName: '현재 수량',
      width: 120,
      renderCell: (params: GridRenderCellParams) => `${params.row.currentQuantity} ${params.row.unit}`,
    },
    { field: 'manufactureDate', headerName: '제조 일자', width: 120 },
    { field: 'expiryDate', headerName: '유효 기한', width: 120 },
    {
      field: 'qualityStatus',
      headerName: '품질 상태',
      width: 100,
      renderCell: (params: GridRenderCellParams) => (
        <Chip
          label={getQualityStatusLabel(params.value as string)}
          color={getQualityStatusColor(params.value as string)}
          size="small"
        />
      ),
    },
    { field: 'supplier', headerName: '공급업체', width: 150 },
    {
      field: 'actions',
      headerName: '작업',
      width: 120,
      sortable: false,
      renderCell: (params: GridRenderCellParams) => (
        <Box>
          <IconButton size="small" onClick={() => handleOpenDialog(params.row as Lot)}>
            <EditIcon fontSize="small" />
          </IconButton>
          <IconButton
            size="small"
            onClick={() => {
              setSelectedLot(params.row as Lot);
              setOpenDeleteDialog(true);
            }}
          >
            <DeleteIcon fontSize="small" />
          </IconButton>
        </Box>
      ),
    },
  ];

  return (
    <Box sx={{ p: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
        <h2>LOT 관리</h2>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => handleOpenDialog()}>
          LOT 생성
        </Button>
      </Box>

      <DataGrid
        rows={lots}
        columns={columns}
        loading={loading}
        getRowId={(row) => row.lotId}
        pageSizeOptions={[10, 25, 50, 100]}
        initialState={{
          pagination: { paginationModel: { pageSize: 25 } },
        }}
        sx={{ height: 600 }}
      />

      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
        <DialogTitle>{selectedLot ? 'LOT 수정' : 'LOT 생성'}</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 2 }}>
            <TextField
              label="LOT 번호"
              value={formData.lotNo || ''}
              onChange={(e) => setFormData({ ...formData, lotNo: e.target.value })}
              disabled={!!selectedLot}
              required
              fullWidth
            />
            <FormControl fullWidth required>
              <InputLabel>제품</InputLabel>
              <Select
                value={formData.productId || ''}
                onChange={(e) => setFormData({ ...formData, productId: Number(e.target.value) })}
                label="제품"
                disabled={!!selectedLot}
              >
                {products.map((product) => (
                  <MenuItem key={product.productId} value={product.productId}>
                    {product.productCode} - {product.productName}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
            <FormControl fullWidth>
              <InputLabel>작업 지시</InputLabel>
              <Select
                value={formData.workOrderId || ''}
                onChange={(e) => setFormData({ ...formData, workOrderId: Number(e.target.value) || undefined })}
                label="작업 지시"
                disabled={!!selectedLot}
              >
                <MenuItem value="">없음</MenuItem>
                {workOrders.map((wo) => (
                  <MenuItem key={wo.workOrderId} value={wo.workOrderId}>
                    {wo.workOrderNo}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
            {!selectedLot && (
              <TextField
                label="초기 수량"
                type="number"
                value={formData.initialQuantity || ''}
                onChange={(e) => setFormData({ ...formData, initialQuantity: Number(e.target.value) })}
                required
                fullWidth
              />
            )}
            <TextField
              label="현재 수량"
              type="number"
              value={formData.currentQuantity || ''}
              onChange={(e) => setFormData({ ...formData, currentQuantity: Number(e.target.value) })}
              required
              fullWidth
            />
            <TextField
              label="단위"
              value={formData.unit || ''}
              onChange={(e) => setFormData({ ...formData, unit: e.target.value })}
              required
              disabled={!!selectedLot}
              fullWidth
            />
            <TextField
              label="제조 일자"
              type="date"
              value={formData.manufactureDate || ''}
              onChange={(e) => setFormData({ ...formData, manufactureDate: e.target.value })}
              InputLabelProps={{ shrink: true }}
              fullWidth
            />
            <TextField
              label="유효 기한"
              type="date"
              value={formData.expiryDate || ''}
              onChange={(e) => setFormData({ ...formData, expiryDate: e.target.value })}
              InputLabelProps={{ shrink: true }}
              fullWidth
            />
            <FormControl fullWidth required>
              <InputLabel>품질 상태</InputLabel>
              <Select
                value={formData.qualityStatus || 'PENDING'}
                onChange={(e) => setFormData({ ...formData, qualityStatus: e.target.value })}
                label="품질 상태"
              >
                <MenuItem value="PENDING">대기</MenuItem>
                <MenuItem value="PASSED">합격</MenuItem>
                <MenuItem value="FAILED">불합격</MenuItem>
                <MenuItem value="QUARANTINE">격리</MenuItem>
              </Select>
            </FormControl>
            <TextField
              label="공급업체"
              value={formData.supplier || ''}
              onChange={(e) => setFormData({ ...formData, supplier: e.target.value })}
              fullWidth
            />
            <TextField
              label="공급업체 LOT 번호"
              value={formData.supplierLotNo || ''}
              onChange={(e) => setFormData({ ...formData, supplierLotNo: e.target.value })}
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
            {selectedLot ? '수정' : '생성'}
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={openDeleteDialog} onClose={() => setOpenDeleteDialog(false)}>
        <DialogTitle>LOT 삭제</DialogTitle>
        <DialogContent>정말로 이 LOT을 삭제하시겠습니까?</DialogContent>
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

export default LotsPage;
