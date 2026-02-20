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
import goodsReceiptService, { GoodsReceipt, GoodsReceiptRequest } from '../../services/goodsReceiptService';
import warehouseService, { Warehouse } from '../../services/warehouseService';
import purchaseOrderService, { PurchaseOrder } from '../../services/purchaseOrderService';

const ReceivingPage: React.FC = () => {
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
      setSnackbar({ open: true, message: 'Failed to load data', severity: 'error' });
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
      setSnackbar({ open: true, message: 'Goods receipt created successfully', severity: 'success' });
      handleCloseDialog();
      loadData();
    } catch (error) {
      setSnackbar({ open: true, message: 'Failed to create goods receipt', severity: 'error' });
    }
  };

  const handleDelete = async () => {
    if (!selectedReceipt) return;

    try {
      await goodsReceiptService.cancel(selectedReceipt.goodsReceiptId);
      setSnackbar({ open: true, message: 'Goods receipt deleted successfully', severity: 'success' });
      handleCloseDeleteDialog();
      loadData();
    } catch (error) {
      setSnackbar({ open: true, message: 'Failed to delete goods receipt', severity: 'error' });
    }
  };

  const handleComplete = async (id: number) => {
    try {
      await goodsReceiptService.complete(id);
      setSnackbar({ open: true, message: '입하가 완료되었습니다', severity: 'success' });
      loadData();
    } catch (error) {
      setSnackbar({ open: true, message: '입하 완료 실패', severity: 'error' });
    }
  };

  const handleCancel = async (id: number) => {
    try {
      await goodsReceiptService.cancel(id, '사용자 요청');
      setSnackbar({ open: true, message: '입하가 취소되었습니다', severity: 'success' });
      loadData();
    } catch (error) {
      setSnackbar({ open: true, message: '입하 취소 실패', severity: 'error' });
    }
  };

  const columns: GridColDef[] = [
    { field: 'receiptNo', headerName: '입하번호', width: 150 },
    {
      field: 'receiptDate',
      headerName: '입하일자',
      width: 180,
      valueFormatter: (params) => new Date(params.value).toLocaleString('ko-KR'),
    },
    { field: 'purchaseOrderNo', headerName: '구매주문번호', width: 150 },
    { field: 'supplierName', headerName: '공급업체', width: 150 },
    { field: 'warehouseName', headerName: '창고', width: 120 },
    {
      field: 'receiptType',
      headerName: '유형',
      width: 100,
      valueFormatter: (params) => {
        const types: { [key: string]: string } = {
          PURCHASE: '구매',
          RETURN: '반품',
          TRANSFER: '이동',
          OTHER: '기타',
        };
        return types[params.value] || params.value;
      },
    },
    {
      field: 'receiptStatus',
      headerName: '상태',
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
          PENDING: '대기',
          INSPECTING: '검사중',
          COMPLETED: '완료',
          REJECTED: '반려',
          CANCELLED: '취소',
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
    { field: 'receiverName', headerName: '입하담당자', width: 120 },
    {
      field: 'actions',
      type: 'actions',
      headerName: '작업',
      width: 150,
      getActions: (params) => [
        <GridActionsCellItem
          icon={<VisibilityIcon />}
          label="상세"
          onClick={() => handleOpenDialog(params.row, true)}
          showInMenu
        />,
        <GridActionsCellItem
          icon={<CheckCircleIcon />}
          label="완료"
          onClick={() => handleComplete(params.row.goodsReceiptId)}
          disabled={params.row.receiptStatus !== 'PENDING' && params.row.receiptStatus !== 'INSPECTING'}
          showInMenu
        />,
        <GridActionsCellItem
          icon={<CancelIcon />}
          label="취소"
          onClick={() => handleCancel(params.row.goodsReceiptId)}
          disabled={params.row.receiptStatus === 'COMPLETED' || params.row.receiptStatus === 'CANCELLED'}
          showInMenu
        />,
        <GridActionsCellItem
          icon={<DeleteIcon />}
          label="삭제"
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
          <Typography variant="h5">입하 관리</Typography>
          <Button variant="contained" startIcon={<AddIcon />} onClick={() => handleOpenDialog()}>
            신규 입하
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
        <DialogTitle>{viewMode ? '입하 상세' : '신규 입하'}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="입하번호"
                value={formData.receiptNo}
                onChange={(e) => setFormData({ ...formData, receiptNo: e.target.value })}
                required
                disabled={viewMode}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="입하일자"
                type="datetime-local"
                value={formData.receiptDate}
                onChange={(e) => setFormData({ ...formData, receiptDate: e.target.value })}
                required
                disabled={viewMode}
                InputLabelProps={{ shrink: true }}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth>
                <InputLabel>구매주문</InputLabel>
                <Select
                  value={formData.purchaseOrderId || ''}
                  onChange={(e) => setFormData({ ...formData, purchaseOrderId: e.target.value as number })}
                  label="구매주문"
                  disabled={viewMode}
                >
                  <MenuItem value="">
                    <em>선택 안 함</em>
                  </MenuItem>
                  {purchaseOrders.map((po) => (
                    <MenuItem key={po.purchaseOrderId} value={po.purchaseOrderId}>
                      {po.orderNo}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth required>
                <InputLabel>창고</InputLabel>
                <Select
                  value={formData.warehouseId || ''}
                  onChange={(e) => setFormData({ ...formData, warehouseId: e.target.value as number })}
                  label="창고"
                  disabled={viewMode}
                >
                  {warehouses.map((wh) => (
                    <MenuItem key={wh.warehouseId} value={wh.warehouseId}>
                      {wh.warehouseName}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth required>
                <InputLabel>입하유형</InputLabel>
                <Select
                  value={formData.receiptType}
                  onChange={(e) => setFormData({ ...formData, receiptType: e.target.value })}
                  label="입하유형"
                  disabled={viewMode}
                >
                  <MenuItem value="PURCHASE">구매</MenuItem>
                  <MenuItem value="RETURN">반품</MenuItem>
                  <MenuItem value="TRANSFER">이동</MenuItem>
                  <MenuItem value="OTHER">기타</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="입하담당자"
                value={formData.receiverName}
                onChange={(e) => setFormData({ ...formData, receiverName: e.target.value })}
                disabled={viewMode}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="비고"
                multiline
                rows={3}
                value={formData.remarks}
                onChange={(e) => setFormData({ ...formData, remarks: e.target.value })}
                disabled={viewMode}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>닫기</Button>
          {!viewMode && (
            <Button onClick={handleSubmit} variant="contained">
              생성
            </Button>
          )}
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={openDeleteDialog} onClose={handleCloseDeleteDialog}>
        <DialogTitle>입하 삭제</DialogTitle>
        <DialogContent>
          <Typography>정말 이 입하를 삭제하시겠습니까?</Typography>
          {selectedReceipt && (
            <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
              입하번호: {selectedReceipt.receiptNo}
            </Typography>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDeleteDialog}>취소</Button>
          <Button onClick={handleDelete} color="error" variant="contained">
            삭제
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

export default ReceivingPage;
