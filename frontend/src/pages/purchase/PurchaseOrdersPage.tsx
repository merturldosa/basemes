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
  Chip,
  IconButton,
  Typography,
} from '@mui/material';
import { DataGrid, GridColDef, GridRenderCellParams } from '@mui/x-data-grid';
import {
  Add as AddIcon,
  Visibility as ViewIcon,
  CheckCircle as CheckCircleIcon,
  Cancel as CancelIcon,
} from '@mui/icons-material';
import purchaseOrderService, { PurchaseOrder } from '../../services/purchaseOrderService';

const PurchaseOrdersPage: React.FC = () => {
  const [purchaseOrders, setPurchaseOrders] = useState<PurchaseOrder[]>([]);
  const [loading, setLoading] = useState(false);
  const [openViewDialog, setOpenViewDialog] = useState(false);
  const [selectedOrder, setSelectedOrder] = useState<PurchaseOrder | null>(null);
  const [snackbar, setSnackbar] = useState<{ open: boolean; message: string; severity: 'success' | 'error' }>({
    open: false,
    message: '',
    severity: 'success',
  });

  useEffect(() => {
    loadPurchaseOrders();
  }, []);

  const loadPurchaseOrders = async () => {
    try {
      setLoading(true);
      const data = await purchaseOrderService.getAll();
      setPurchaseOrders(data || []);
    } catch (error) {
      console.error('Failed to load purchase orders:', error);
      setPurchaseOrders([]);
      setSnackbar({ open: true, message: '구매 주문 목록 조회 실패', severity: 'error' });
    } finally {
      setLoading(false);
    }
  };

  const handleViewOrder = (order: PurchaseOrder) => {
    setSelectedOrder(order);
    setOpenViewDialog(true);
  };

  const handleConfirm = async (order: PurchaseOrder) => {
    try {
      await purchaseOrderService.confirm(order.purchaseOrderId);
      setSnackbar({ open: true, message: '구매 주문이 확정되었습니다', severity: 'success' });
      loadPurchaseOrders();
    } catch (error) {
      console.error('Failed to confirm purchase order:', error);
      setSnackbar({ open: true, message: '구매 주문 확정 실패', severity: 'error' });
    }
  };

  const handleCancel = async (order: PurchaseOrder) => {
    try {
      await purchaseOrderService.cancel(order.purchaseOrderId);
      setSnackbar({ open: true, message: '구매 주문이 취소되었습니다', severity: 'success' });
      loadPurchaseOrders();
    } catch (error) {
      console.error('Failed to cancel purchase order:', error);
      setSnackbar({ open: true, message: '구매 주문 취소 실패', severity: 'error' });
    }
  };

  const getStatusLabel = (status: string) => {
    const statuses: { [key: string]: string } = {
      DRAFT: '임시저장',
      CONFIRMED: '확정',
      PARTIALLY_RECEIVED: '부분입하',
      RECEIVED: '입하완료',
      CANCELLED: '취소',
    };
    return statuses[status] || status;
  };

  const getStatusColor = (status: string) => {
    const colors: { [key: string]: 'default' | 'primary' | 'secondary' | 'success' | 'warning' | 'error' } = {
      DRAFT: 'default',
      CONFIRMED: 'primary',
      PARTIALLY_RECEIVED: 'warning',
      RECEIVED: 'success',
      CANCELLED: 'error',
    };
    return colors[status] || 'default';
  };

  const columns: GridColDef[] = [
    { field: 'orderNo', headerName: '주문 번호', width: 150 },
    {
      field: 'orderDate',
      headerName: '주문 일자',
      width: 120,
      renderCell: (params: GridRenderCellParams) =>
        params.value ? new Date(params.value as string).toLocaleDateString() : '-'
    },
    { field: 'supplierName', headerName: '공급업체', width: 200 },
    { field: 'buyerFullName', headerName: '구매 담당자', width: 120 },
    {
      field: 'totalAmount',
      headerName: '총액',
      width: 120,
      renderCell: (params: GridRenderCellParams) =>
        params.value ? `${Number(params.value).toLocaleString()} ${params.row.currency || ''}` : '-'
    },
    {
      field: 'expectedDeliveryDate',
      headerName: '납기일',
      width: 120,
      renderCell: (params: GridRenderCellParams) =>
        params.value ? new Date(params.value as string).toLocaleDateString() : '-'
    },
    {
      field: 'status',
      headerName: '상태',
      width: 120,
      renderCell: (params: GridRenderCellParams) => (
        <Chip
          label={getStatusLabel(params.value as string)}
          color={getStatusColor(params.value as string)}
          size="small"
        />
      ),
    },
    {
      field: 'actions',
      headerName: '작업',
      width: 180,
      sortable: false,
      renderCell: (params: GridRenderCellParams) => (
        <Box>
          <IconButton size="small" onClick={() => handleViewOrder(params.row as PurchaseOrder)}>
            <ViewIcon fontSize="small" />
          </IconButton>
          {params.row.status === 'DRAFT' && (
            <IconButton
              size="small"
              onClick={() => handleConfirm(params.row as PurchaseOrder)}
              color="primary"
            >
              <CheckCircleIcon fontSize="small" />
            </IconButton>
          )}
          {(params.row.status === 'DRAFT' || params.row.status === 'CONFIRMED') && (
            <IconButton
              size="small"
              onClick={() => handleCancel(params.row as PurchaseOrder)}
              color="error"
            >
              <CancelIcon fontSize="small" />
            </IconButton>
          )}
        </Box>
      ),
    },
  ];

  return (
    <Box sx={{ p: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
        <h2>구매 주문 관리</h2>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => setSnackbar({ open: true, message: '구매 주문 생성 기능은 추후 구현 예정입니다', severity: 'error' })}>
          구매 주문 생성
        </Button>
      </Box>

      <DataGrid
        rows={purchaseOrders}
        columns={columns}
        loading={loading}
        getRowId={(row) => row.purchaseOrderId}
        pageSizeOptions={[10, 25, 50, 100]}
        initialState={{
          pagination: { paginationModel: { pageSize: 25 } },
        }}
        sx={{ height: 600 }}
      />

      {/* View Dialog */}
      <Dialog open={openViewDialog} onClose={() => setOpenViewDialog(false)} maxWidth="md" fullWidth>
        <DialogTitle>구매 주문 상세</DialogTitle>
        <DialogContent>
          {selectedOrder && (
            <Box sx={{ mt: 2 }}>
              <Typography variant="subtitle1"><strong>주문 번호:</strong> {selectedOrder.orderNo}</Typography>
              <Typography variant="subtitle1"><strong>공급업체:</strong> {selectedOrder.supplierName}</Typography>
              <Typography variant="subtitle1"><strong>구매 담당자:</strong> {selectedOrder.buyerFullName}</Typography>
              <Typography variant="subtitle1"><strong>상태:</strong> {getStatusLabel(selectedOrder.status)}</Typography>
              <Typography variant="subtitle1"><strong>총액:</strong> {selectedOrder.totalAmount?.toLocaleString()} {selectedOrder.currency}</Typography>
              <Typography variant="subtitle1" sx={{ mt: 2 }}><strong>주문 항목:</strong></Typography>
              {selectedOrder.items.map((item, index) => (
                <Box key={index} sx={{ ml: 2, mt: 1 }}>
                  <Typography variant="body2">
                    {item.lineNo}. {item.materialName} - 수량: {item.orderedQuantity} {item.unit} - 단가: {item.unitPrice?.toLocaleString()} - 금액: {item.amount?.toLocaleString()}
                  </Typography>
                </Box>
              ))}
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenViewDialog(false)}>닫기</Button>
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

export default PurchaseOrdersPage;
