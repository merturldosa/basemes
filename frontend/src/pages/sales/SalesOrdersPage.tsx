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
  IconButton,
  Chip,
  MenuItem,
  Select,
  FormControl,
  InputLabel,
  Grid,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
} from '@mui/material';
import { DataGrid, GridColDef, GridActionsCellItem } from '@mui/x-data-grid';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Visibility as ViewIcon,
  Check as CheckIcon,
  Close as CloseIcon,
  RemoveCircleOutline as RemoveIcon,
} from '@mui/icons-material';
import salesOrderService, { SalesOrder, SalesOrderItem, SalesOrderCreateRequest, SalesOrderUpdateRequest } from '../../services/salesOrderService';
import customerService, { Customer } from '../../services/customerService';

const SalesOrdersPage: React.FC = () => {
  const [salesOrders, setSalesOrders] = useState<SalesOrder[]>([]);
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [loading, setLoading] = useState(false);
  const [openDialog, setOpenDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [openViewDialog, setOpenViewDialog] = useState(false);
  const [selectedSalesOrder, setSelectedSalesOrder] = useState<SalesOrder | null>(null);
  const [isEdit, setIsEdit] = useState(false);
  const [snackbar, setSnackbar] = useState<{
    open: boolean;
    message: string;
    severity: 'success' | 'error' | 'info';
  }>({ open: false, message: '', severity: 'success' });

  const [formData, setFormData] = useState<SalesOrderCreateRequest>({
    orderNo: '',
    orderDate: new Date().toISOString().slice(0, 16),
    customerId: 0,
    salesUserId: 1,
    requestedDeliveryDate: '',
    deliveryAddress: '',
    paymentTerms: 'NET30',
    currency: 'KRW',
    remarks: '',
    items: [],
  });

  useEffect(() => {
    loadData();
    loadCustomers();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const data = await salesOrderService.getAll();
      setSalesOrders(data || []);
    } catch (error) {
      setSalesOrders([]);
      setSnackbar({ open: true, message: 'Failed to load sales orders', severity: 'error' });
    } finally {
      setLoading(false);
    }
  };

  const loadCustomers = async () => {
    try {
      const data = await customerService.getActive();
      setCustomers(data || []);
    } catch (error) {
      setCustomers([]);
    }
  };

  const handleOpenDialog = (salesOrder?: SalesOrder) => {
    if (salesOrder) {
      setIsEdit(true);
      setSelectedSalesOrder(salesOrder);
      setFormData({
        orderNo: salesOrder.orderNo,
        orderDate: salesOrder.orderDate,
        customerId: salesOrder.customerId,
        salesUserId: salesOrder.salesUserId,
        requestedDeliveryDate: salesOrder.requestedDeliveryDate || '',
        deliveryAddress: salesOrder.deliveryAddress || '',
        paymentTerms: salesOrder.paymentTerms || 'NET30',
        currency: salesOrder.currency || 'KRW',
        remarks: salesOrder.remarks || '',
        items: salesOrder.items.map(item => ({
          lineNo: item.lineNo,
          productId: item.productId,
          materialId: item.materialId,
          orderedQuantity: item.orderedQuantity,
          unit: item.unit,
          unitPrice: item.unitPrice,
          requestedDate: item.requestedDate,
          remarks: item.remarks,
        })),
      });
    } else {
      setIsEdit(false);
      setSelectedSalesOrder(null);
      setFormData({
        orderNo: '',
        orderDate: new Date().toISOString().slice(0, 16),
        customerId: 0,
        salesUserId: 1,
        requestedDeliveryDate: '',
        deliveryAddress: '',
        paymentTerms: 'NET30',
        currency: 'KRW',
        remarks: '',
        items: [],
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedSalesOrder(null);
  };

  const handleOpenViewDialog = (salesOrder: SalesOrder) => {
    setSelectedSalesOrder(salesOrder);
    setOpenViewDialog(true);
  };

  const handleCloseViewDialog = () => {
    setOpenViewDialog(false);
    setSelectedSalesOrder(null);
  };

  const handleOpenDeleteDialog = (salesOrder: SalesOrder) => {
    setSelectedSalesOrder(salesOrder);
    setOpenDeleteDialog(true);
  };

  const handleCloseDeleteDialog = () => {
    setOpenDeleteDialog(false);
    setSelectedSalesOrder(null);
  };

  const handleSubmit = async () => {
    try {
      if (isEdit && selectedSalesOrder) {
        await salesOrderService.update(selectedSalesOrder.salesOrderId, formData as SalesOrderUpdateRequest);
        setSnackbar({ open: true, message: 'Sales order updated successfully', severity: 'success' });
      } else {
        await salesOrderService.create(formData);
        setSnackbar({ open: true, message: 'Sales order created successfully', severity: 'success' });
      }
      handleCloseDialog();
      loadData();
    } catch (error) {
      setSnackbar({ open: true, message: 'Failed to save sales order', severity: 'error' });
    }
  };

  const handleDelete = async () => {
    if (!selectedSalesOrder) return;

    try {
      await salesOrderService.delete(selectedSalesOrder.salesOrderId);
      setSnackbar({ open: true, message: 'Sales order deleted successfully', severity: 'success' });
      handleCloseDeleteDialog();
      loadData();
    } catch (error) {
      setSnackbar({ open: true, message: 'Failed to delete sales order', severity: 'error' });
    }
  };

  const handleConfirm = async (id: number) => {
    try {
      await salesOrderService.confirm(id);
      setSnackbar({ open: true, message: 'Sales order confirmed successfully', severity: 'success' });
      loadData();
    } catch (error) {
      setSnackbar({ open: true, message: 'Failed to confirm sales order', severity: 'error' });
    }
  };

  const handleCancel = async (id: number) => {
    try {
      await salesOrderService.cancel(id);
      setSnackbar({ open: true, message: 'Sales order cancelled successfully', severity: 'success' });
      loadData();
    } catch (error) {
      setSnackbar({ open: true, message: 'Failed to cancel sales order', severity: 'error' });
    }
  };

  const addItem = () => {
    setFormData({
      ...formData,
      items: [
        ...formData.items,
        {
          lineNo: formData.items.length + 1,
          orderedQuantity: 0,
          unit: 'EA',
        },
      ],
    });
  };

  const removeItem = (index: number) => {
    const newItems = formData.items.filter((_, i) => i !== index);
    setFormData({ ...formData, items: newItems });
  };

  const updateItem = (index: number, field: keyof SalesOrderItem, value: string | number) => {
    const newItems = [...formData.items];
    newItems[index] = { ...newItems[index], [field]: value };
    setFormData({ ...formData, items: newItems });
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'DRAFT':
        return 'default';
      case 'CONFIRMED':
        return 'primary';
      case 'PARTIALLY_DELIVERED':
        return 'warning';
      case 'DELIVERED':
        return 'success';
      case 'CANCELLED':
        return 'error';
      default:
        return 'default';
    }
  };

  const getStatusLabel = (status: string) => {
    const statuses: { [key: string]: string } = {
      DRAFT: '임시저장',
      CONFIRMED: '확정',
      PARTIALLY_DELIVERED: '부분출하',
      DELIVERED: '출하완료',
      CANCELLED: '취소',
    };
    return statuses[status] || status;
  };

  const columns: GridColDef[] = [
    { field: 'orderNo', headerName: '주문번호', width: 150 },
    {
      field: 'orderDate',
      headerName: '주문일자',
      width: 180,
      valueFormatter: (params) => new Date(params.value).toLocaleString('ko-KR'),
    },
    { field: 'customerName', headerName: '고객명', width: 200 },
    {
      field: 'totalAmount',
      headerName: '주문금액',
      width: 130,
      valueFormatter: (params) => params.value ? `₩${params.value.toLocaleString()}` : '',
    },
    {
      field: 'status',
      headerName: '상태',
      width: 120,
      renderCell: (params) => (
        <Chip label={getStatusLabel(params.value)} color={getStatusColor(params.value)} size="small" />
      ),
    },
    { field: 'salesUserName', headerName: '영업담당자', width: 120 },
    {
      field: 'requestedDeliveryDate',
      headerName: '요청납기일',
      width: 180,
      valueFormatter: (params) => params.value ? new Date(params.value).toLocaleString('ko-KR') : '-',
    },
    {
      field: 'actions',
      type: 'actions',
      headerName: '작업',
      width: 180,
      getActions: (params) => {
        const actions = [
          <GridActionsCellItem
            icon={<ViewIcon />}
            label="View"
            onClick={() => handleOpenViewDialog(params.row)}
          />,
        ];

        if (params.row.status === 'DRAFT') {
          actions.push(
            <GridActionsCellItem
              icon={<EditIcon />}
              label="Edit"
              onClick={() => handleOpenDialog(params.row)}
            />,
            <GridActionsCellItem
              icon={<CheckIcon />}
              label="Confirm"
              onClick={() => handleConfirm(params.row.salesOrderId)}
            />,
            <GridActionsCellItem
              icon={<DeleteIcon />}
              label="Delete"
              onClick={() => handleOpenDeleteDialog(params.row)}
            />
          );
        }

        if (params.row.status === 'CONFIRMED' || params.row.status === 'PARTIALLY_DELIVERED') {
          actions.push(
            <GridActionsCellItem
              icon={<CloseIcon />}
              label="Cancel"
              onClick={() => handleCancel(params.row.salesOrderId)}
            />
          );
        }

        return actions;
      },
    },
  ];

  return (
    <Box sx={{ height: '100%', p: 3 }}>
      <Paper sx={{ p: 2, mb: 2 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="h5">판매 주문 관리</Typography>
          <Button variant="contained" startIcon={<AddIcon />} onClick={() => handleOpenDialog()}>
            신규 주문
          </Button>
        </Box>
      </Paper>

      <Paper sx={{ height: 'calc(100vh - 250px)' }}>
        <DataGrid
          rows={salesOrders}
          columns={columns}
          loading={loading}
          getRowId={(row) => row.salesOrderId}
          pageSizeOptions={[10, 25, 50, 100]}
          initialState={{
            pagination: { paginationModel: { pageSize: 25 } },
          }}
        />
      </Paper>

      {/* Create/Edit Dialog */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="md" fullWidth>
        <DialogTitle>{isEdit ? '판매 주문 수정' : '신규 판매 주문'}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            {!isEdit && (
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="주문번호"
                  value={formData.orderNo}
                  onChange={(e) => setFormData({ ...formData, orderNo: e.target.value })}
                  required
                />
              </Grid>
            )}
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="주문일자"
                type="datetime-local"
                value={formData.orderDate}
                onChange={(e) => setFormData({ ...formData, orderDate: e.target.value })}
                required
                InputLabelProps={{ shrink: true }}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth required>
                <InputLabel>고객</InputLabel>
                <Select
                  value={formData.customerId || ''}
                  onChange={(e) => setFormData({ ...formData, customerId: Number(e.target.value) })}
                  label="고객"
                >
                  {customers.map((customer) => (
                    <MenuItem key={customer.customerId} value={customer.customerId}>
                      {customer.customerName} ({customer.customerCode})
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="요청납기일"
                type="datetime-local"
                value={formData.requestedDeliveryDate}
                onChange={(e) => setFormData({ ...formData, requestedDeliveryDate: e.target.value })}
                InputLabelProps={{ shrink: true }}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="납품주소"
                multiline
                rows={2}
                value={formData.deliveryAddress}
                onChange={(e) => setFormData({ ...formData, deliveryAddress: e.target.value })}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth>
                <InputLabel>결제조건</InputLabel>
                <Select
                  value={formData.paymentTerms}
                  onChange={(e) => setFormData({ ...formData, paymentTerms: e.target.value })}
                  label="결제조건"
                >
                  <MenuItem value="NET30">NET30</MenuItem>
                  <MenuItem value="NET60">NET60</MenuItem>
                  <MenuItem value="COD">현금</MenuItem>
                  <MenuItem value="ADVANCE">선불</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth>
                <InputLabel>통화</InputLabel>
                <Select
                  value={formData.currency}
                  onChange={(e) => setFormData({ ...formData, currency: e.target.value })}
                  label="통화"
                >
                  <MenuItem value="KRW">원화 (KRW)</MenuItem>
                  <MenuItem value="USD">달러 (USD)</MenuItem>
                  <MenuItem value="EUR">유로 (EUR)</MenuItem>
                  <MenuItem value="JPY">엔화 (JPY)</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="비고"
                multiline
                rows={2}
                value={formData.remarks}
                onChange={(e) => setFormData({ ...formData, remarks: e.target.value })}
              />
            </Grid>

            {/* Items Section */}
            <Grid item xs={12}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
                <Typography variant="h6">주문 상세</Typography>
                <Button variant="outlined" size="small" startIcon={<AddIcon />} onClick={addItem}>
                  품목 추가
                </Button>
              </Box>
              <TableContainer>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>번호</TableCell>
                      <TableCell>수량</TableCell>
                      <TableCell>단위</TableCell>
                      <TableCell>단가</TableCell>
                      <TableCell>작업</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {formData.items.map((item, index) => (
                      <TableRow key={index}>
                        <TableCell>{item.lineNo}</TableCell>
                        <TableCell>
                          <TextField
                            size="small"
                            type="number"
                            value={item.orderedQuantity}
                            onChange={(e) => updateItem(index, 'orderedQuantity', Number(e.target.value))}
                            sx={{ width: 100 }}
                          />
                        </TableCell>
                        <TableCell>
                          <TextField
                            size="small"
                            value={item.unit}
                            onChange={(e) => updateItem(index, 'unit', e.target.value)}
                            sx={{ width: 80 }}
                          />
                        </TableCell>
                        <TableCell>
                          <TextField
                            size="small"
                            type="number"
                            value={item.unitPrice || ''}
                            onChange={(e) => updateItem(index, 'unitPrice', Number(e.target.value))}
                            sx={{ width: 120 }}
                          />
                        </TableCell>
                        <TableCell>
                          <IconButton size="small" onClick={() => removeItem(index)}>
                            <RemoveIcon />
                          </IconButton>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>취소</Button>
          <Button onClick={handleSubmit} variant="contained">
            {isEdit ? '수정' : '생성'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* View Dialog */}
      <Dialog open={openViewDialog} onClose={handleCloseViewDialog} maxWidth="md" fullWidth>
        <DialogTitle>판매 주문 상세</DialogTitle>
        <DialogContent>
          {selectedSalesOrder && (
            <Grid container spacing={2} sx={{ mt: 1 }}>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="text.secondary">주문번호</Typography>
                <Typography variant="body1">{selectedSalesOrder.orderNo}</Typography>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="text.secondary">상태</Typography>
                <Chip label={getStatusLabel(selectedSalesOrder.status)} color={getStatusColor(selectedSalesOrder.status)} size="small" />
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="text.secondary">고객</Typography>
                <Typography variant="body1">{selectedSalesOrder.customerName}</Typography>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="text.secondary">주문금액</Typography>
                <Typography variant="body1">₩{selectedSalesOrder.totalAmount?.toLocaleString() || 0}</Typography>
              </Grid>
              <Grid item xs={12}>
                <Typography variant="h6" sx={{ mt: 2, mb: 1 }}>주문 상세</Typography>
                <TableContainer>
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>번호</TableCell>
                        <TableCell>품목</TableCell>
                        <TableCell>주문수량</TableCell>
                        <TableCell>출하수량</TableCell>
                        <TableCell>단위</TableCell>
                        <TableCell>단가</TableCell>
                        <TableCell>금액</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {selectedSalesOrder.items.map((item) => (
                        <TableRow key={item.salesOrderItemId}>
                          <TableCell>{item.lineNo}</TableCell>
                          <TableCell>{item.productName || item.materialName}</TableCell>
                          <TableCell>{item.orderedQuantity}</TableCell>
                          <TableCell>{item.deliveredQuantity || 0}</TableCell>
                          <TableCell>{item.unit}</TableCell>
                          <TableCell>₩{item.unitPrice?.toLocaleString() || 0}</TableCell>
                          <TableCell>₩{item.amount?.toLocaleString() || 0}</TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              </Grid>
            </Grid>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseViewDialog}>닫기</Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={openDeleteDialog} onClose={handleCloseDeleteDialog}>
        <DialogTitle>판매 주문 삭제</DialogTitle>
        <DialogContent>
          <Typography>정말 이 판매 주문을 삭제하시겠습니까?</Typography>
          {selectedSalesOrder && (
            <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
              주문번호: {selectedSalesOrder.orderNo}
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

export default SalesOrdersPage;
