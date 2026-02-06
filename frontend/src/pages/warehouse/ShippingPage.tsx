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
} from '@mui/icons-material';
import shippingService, { Shipping, ShippingRequest } from '../../services/shippingService';
import warehouseService, { Warehouse } from '../../services/warehouseService';
import salesOrderService, { SalesOrder } from '../../services/salesOrderService';

const ShippingPage: React.FC = () => {
  const [shippings, setShippings] = useState<Shipping[]>([]);
  const [warehouses, setWarehouses] = useState<Warehouse[]>([]);
  const [salesOrders, setSalesOrders] = useState<SalesOrder[]>([]);
  const [loading, setLoading] = useState(false);
  const [openDialog, setOpenDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [selectedShipping, setSelectedShipping] = useState<Shipping | null>(null);
  const [viewMode, setViewMode] = useState(false);
  const [snackbar, setSnackbar] = useState<{
    open: boolean;
    message: string;
    severity: 'success' | 'error' | 'info';
  }>({ open: false, message: '', severity: 'success' });

  const [formData, setFormData] = useState<ShippingRequest>({
    shippingNo: '',
    shippingDate: new Date().toISOString().slice(0, 16),
    salesOrderId: undefined,
    warehouseId: 0,
    shippingType: 'SALES',
    shippingStatus: 'PENDING',
    shipperName: '',
    deliveryAddress: '',
    trackingNumber: '',
    carrierName: '',
    remarks: '',
    items: [],
  });

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const [shippingsData, warehousesData, salesOrdersData] = await Promise.all([
        shippingService.getAll(),
        warehouseService.getActive(),
        salesOrderService.getAll(),
      ]);
      setShippings(shippingsData || []);
      setWarehouses(warehousesData || []);
      setSalesOrders(salesOrdersData || []);
    } catch (error) {
      setSnackbar({ open: true, message: 'Failed to load data', severity: 'error' });
      setShippings([]);
      setWarehouses([]);
      setSalesOrders([]);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenDialog = (shipping?: Shipping, view = false) => {
    if (shipping) {
      setViewMode(view);
      setSelectedShipping(shipping);
      setFormData({
        shippingNo: shipping.shippingNo,
        shippingDate: shipping.shippingDate,
        salesOrderId: shipping.salesOrderId,
        warehouseId: shipping.warehouseId,
        shippingType: shipping.shippingType,
        shippingStatus: shipping.shippingStatus,
        shipperName: shipping.shipperName || '',
        deliveryAddress: shipping.deliveryAddress || '',
        trackingNumber: shipping.trackingNumber || '',
        carrierName: shipping.carrierName || '',
        remarks: shipping.remarks || '',
        items: shipping.items || [],
      });
    } else {
      setViewMode(false);
      setSelectedShipping(null);
      setFormData({
        shippingNo: '',
        shippingDate: new Date().toISOString().slice(0, 16),
        salesOrderId: undefined,
        warehouseId: 0,
        shippingType: 'SALES',
        shippingStatus: 'PENDING',
        shipperName: '',
        deliveryAddress: '',
        trackingNumber: '',
        carrierName: '',
        remarks: '',
        items: [],
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedShipping(null);
    setViewMode(false);
  };

  const handleOpenDeleteDialog = (shipping: Shipping) => {
    setSelectedShipping(shipping);
    setOpenDeleteDialog(true);
  };

  const handleCloseDeleteDialog = () => {
    setOpenDeleteDialog(false);
    setSelectedShipping(null);
  };

  const handleSubmit = async () => {
    try {
      await shippingService.create(formData);
      setSnackbar({ open: true, message: 'Shipping created successfully', severity: 'success' });
      handleCloseDialog();
      loadData();
    } catch (error) {
      setSnackbar({ open: true, message: 'Failed to create shipping', severity: 'error' });
    }
  };

  const handleDelete = async () => {
    if (!selectedShipping) return;

    try {
      await shippingService.delete(selectedShipping.shippingId);
      setSnackbar({ open: true, message: 'Shipping deleted successfully', severity: 'success' });
      handleCloseDeleteDialog();
      loadData();
    } catch (error) {
      setSnackbar({ open: true, message: 'Failed to delete shipping', severity: 'error' });
    }
  };

  const handleComplete = async (id: number) => {
    try {
      await shippingService.complete(id);
      setSnackbar({ open: true, message: 'Shipping completed successfully', severity: 'success' });
      loadData();
    } catch (error) {
      setSnackbar({ open: true, message: 'Failed to complete shipping', severity: 'error' });
    }
  };

  const columns: GridColDef[] = [
    { field: 'shippingNo', headerName: '출하번호', width: 150 },
    {
      field: 'shippingDate',
      headerName: '출하일자',
      width: 180,
      valueFormatter: (params) => new Date(params).toLocaleString('ko-KR'),
    },
    { field: 'salesOrderNo', headerName: '판매주문번호', width: 150 },
    { field: 'customerName', headerName: '고객', width: 150 },
    { field: 'warehouseName', headerName: '창고', width: 120 },
    {
      field: 'shippingType',
      headerName: '유형',
      width: 100,
      valueFormatter: (params) => {
        const types: { [key: string]: string } = {
          SALES: '판매',
          RETURN: '반품',
          TRANSFER: '이동',
          OTHER: '기타',
        };
        return types[params] || params;
      },
    },
    {
      field: 'shippingStatus',
      headerName: '상태',
      width: 120,
      renderCell: (params) => {
        const statusColors: { [key: string]: 'default' | 'warning' | 'success' | 'error' } = {
          PENDING: 'warning',
          INSPECTING: 'info',
          SHIPPED: 'success',
          CANCELLED: 'default',
        };
        const statusLabels: { [key: string]: string } = {
          PENDING: '대기',
          INSPECTING: '검사중',
          SHIPPED: '출하완료',
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
    { field: 'trackingNumber', headerName: '송장번호', width: 150 },
    { field: 'carrierName', headerName: '택배사', width: 120 },
    {
      field: 'actions',
      type: 'actions',
      headerName: '작업',
      width: 150,
      getActions: (params) => [
        <GridActionsCellItem
          icon={<VisibilityIcon />}
          label="View"
          onClick={() => handleOpenDialog(params.row, true)}
        />,
        <GridActionsCellItem
          icon={<CheckCircleIcon />}
          label="Complete"
          onClick={() => handleComplete(params.row.shippingId)}
          disabled={params.row.shippingStatus !== 'PENDING'}
        />,
        <GridActionsCellItem
          icon={<DeleteIcon />}
          label="Delete"
          onClick={() => handleOpenDeleteDialog(params.row)}
          disabled={params.row.shippingStatus === 'SHIPPED'}
        />,
      ],
    },
  ];

  return (
    <Box sx={{ height: '100%', p: 3 }}>
      <Paper sx={{ p: 2, mb: 2 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="h5">출하 관리</Typography>
          <Button variant="contained" startIcon={<AddIcon />} onClick={() => handleOpenDialog()}>
            신규 출하
          </Button>
        </Box>
      </Paper>

      <Paper sx={{ height: 'calc(100vh - 250px)' }}>
        <DataGrid
          rows={shippings}
          columns={columns}
          loading={loading}
          getRowId={(row) => row.shippingId}
          pageSizeOptions={[10, 25, 50, 100]}
          initialState={{
            pagination: { paginationModel: { pageSize: 25 } },
          }}
        />
      </Paper>

      {/* Create/View Dialog */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="md" fullWidth>
        <DialogTitle>{viewMode ? '출하 상세' : '신규 출하'}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="출하번호"
                value={formData.shippingNo}
                onChange={(e) => setFormData({ ...formData, shippingNo: e.target.value })}
                required
                disabled={viewMode}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="출하일자"
                type="datetime-local"
                value={formData.shippingDate}
                onChange={(e) => setFormData({ ...formData, shippingDate: e.target.value })}
                required
                disabled={viewMode}
                InputLabelProps={{ shrink: true }}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth>
                <InputLabel>판매주문</InputLabel>
                <Select
                  value={formData.salesOrderId || ''}
                  onChange={(e) => setFormData({ ...formData, salesOrderId: e.target.value as number })}
                  label="판매주문"
                  disabled={viewMode}
                >
                  <MenuItem value="">
                    <em>선택 안 함</em>
                  </MenuItem>
                  {salesOrders.map((so) => (
                    <MenuItem key={so.salesOrderId} value={so.salesOrderId}>
                      {so.orderNo}
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
                <InputLabel>출하유형</InputLabel>
                <Select
                  value={formData.shippingType}
                  onChange={(e) => setFormData({ ...formData, shippingType: e.target.value })}
                  label="출하유형"
                  disabled={viewMode}
                >
                  <MenuItem value="SALES">판매</MenuItem>
                  <MenuItem value="RETURN">반품</MenuItem>
                  <MenuItem value="TRANSFER">이동</MenuItem>
                  <MenuItem value="OTHER">기타</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="출하담당자"
                value={formData.shipperName}
                onChange={(e) => setFormData({ ...formData, shipperName: e.target.value })}
                disabled={viewMode}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="배송지 주소"
                value={formData.deliveryAddress}
                onChange={(e) => setFormData({ ...formData, deliveryAddress: e.target.value })}
                disabled={viewMode}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="송장번호"
                value={formData.trackingNumber}
                onChange={(e) => setFormData({ ...formData, trackingNumber: e.target.value })}
                disabled={viewMode}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="택배사"
                value={formData.carrierName}
                onChange={(e) => setFormData({ ...formData, carrierName: e.target.value })}
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
        <DialogTitle>출하 삭제</DialogTitle>
        <DialogContent>
          <Typography>정말 이 출하를 삭제하시겠습니까?</Typography>
          {selectedShipping && (
            <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
              출하번호: {selectedShipping.shippingNo}
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

export default ShippingPage;
