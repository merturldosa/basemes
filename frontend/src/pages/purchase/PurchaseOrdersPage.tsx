import React, { useState, useEffect, useCallback } from 'react';
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
  Divider,
  Tooltip,
} from '@mui/material';
import { DataGrid, GridColDef, GridActionsCellItem } from '@mui/x-data-grid';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Visibility as ViewIcon,
  CheckCircle as CheckCircleIcon,
  Cancel as CancelIcon,
  RemoveCircleOutline as RemoveIcon,
} from '@mui/icons-material';
import purchaseOrderService, {
  PurchaseOrder,
  PurchaseOrderCreateRequest,
  PurchaseOrderUpdateRequest,
} from '../../services/purchaseOrderService';
import supplierService, { Supplier } from '../../services/supplierService';
import materialService, { Material } from '../../services/materialService';

const generateOrderNo = (): string => {
  const now = new Date();
  const dateStr = now.getFullYear().toString() +
    String(now.getMonth() + 1).padStart(2, '0') +
    String(now.getDate()).padStart(2, '0');
  const seq = String(Math.floor(Math.random() * 9999) + 1).padStart(4, '0');
  return `PO-${dateStr}-${seq}`;
};

const emptyCreateForm: PurchaseOrderCreateRequest = {
  orderNo: '',
  supplierId: 0,
  buyerUserId: 1,
  expectedDeliveryDate: '',
  deliveryAddress: '',
  paymentTerms: 'NET30',
  currency: 'KRW',
  remarks: '',
  items: [],
};

const PurchaseOrdersPage: React.FC = () => {
  const [purchaseOrders, setPurchaseOrders] = useState<PurchaseOrder[]>([]);
  const [suppliers, setSuppliers] = useState<Supplier[]>([]);
  const [materials, setMaterials] = useState<Material[]>([]);
  const [loading, setLoading] = useState(false);

  // Dialog states
  const [openDialog, setOpenDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [openViewDialog, setOpenViewDialog] = useState(false);
  const [isEdit, setIsEdit] = useState(false);
  const [selectedOrder, setSelectedOrder] = useState<PurchaseOrder | null>(null);

  // Form state
  const [formData, setFormData] = useState<PurchaseOrderCreateRequest>({ ...emptyCreateForm });

  // Snackbar
  const [snackbar, setSnackbar] = useState<{
    open: boolean;
    message: string;
    severity: 'success' | 'error' | 'info';
  }>({ open: false, message: '', severity: 'success' });

  // ------- Data Loading -------
  const loadPurchaseOrders = useCallback(async () => {
    try {
      setLoading(true);
      const data = await purchaseOrderService.getAll();
      setPurchaseOrders(data || []);
    } catch (error) {
      setPurchaseOrders([]);
      setSnackbar({ open: true, message: '구매 주문 목록 조회 실패', severity: 'error' });
    } finally {
      setLoading(false);
    }
  }, []);

  const loadSuppliers = useCallback(async () => {
    try {
      const data = await supplierService.getActive();
      setSuppliers(data || []);
    } catch (error) {
      setSuppliers([]);
    }
  }, []);

  const loadMaterials = useCallback(async () => {
    try {
      const data = await materialService.getActive();
      setMaterials(data || []);
    } catch (error) {
      setMaterials([]);
    }
  }, []);

  useEffect(() => {
    loadPurchaseOrders();
    loadSuppliers();
    loadMaterials();
  }, [loadPurchaseOrders, loadSuppliers, loadMaterials]);

  // ------- Status Helpers -------
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

  const getStatusColor = (status: string): 'default' | 'primary' | 'warning' | 'success' | 'error' => {
    const colors: { [key: string]: 'default' | 'primary' | 'warning' | 'success' | 'error' } = {
      DRAFT: 'default',
      CONFIRMED: 'primary',
      PARTIALLY_RECEIVED: 'warning',
      RECEIVED: 'success',
      CANCELLED: 'error',
    };
    return colors[status] || 'default';
  };

  // ------- Dialog Handlers -------
  const handleOpenCreateDialog = () => {
    setIsEdit(false);
    setSelectedOrder(null);
    setFormData({
      ...emptyCreateForm,
      orderNo: generateOrderNo(),
    });
    setOpenDialog(true);
  };

  const handleOpenEditDialog = (order: PurchaseOrder) => {
    setIsEdit(true);
    setSelectedOrder(order);
    setFormData({
      orderNo: order.orderNo,
      supplierId: order.supplierId,
      buyerUserId: order.buyerUserId,
      expectedDeliveryDate: order.expectedDeliveryDate || '',
      deliveryAddress: order.deliveryAddress || '',
      paymentTerms: order.paymentTerms || 'NET30',
      currency: order.currency || 'KRW',
      remarks: order.remarks || '',
      items: order.items.map((item) => ({
        purchaseOrderItemId: item.purchaseOrderItemId,
        lineNo: item.lineNo,
        materialId: item.materialId,
        materialCode: item.materialCode,
        materialName: item.materialName,
        orderedQuantity: item.orderedQuantity,
        receivedQuantity: item.receivedQuantity,
        unit: item.unit,
        unitPrice: item.unitPrice,
        amount: item.amount,
        requiredDate: item.requiredDate,
        purchaseRequestId: item.purchaseRequestId,
        purchaseRequestNo: item.purchaseRequestNo,
        remarks: item.remarks,
      })),
    });
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedOrder(null);
  };

  const handleOpenViewDialog = (order: PurchaseOrder) => {
    setSelectedOrder(order);
    setOpenViewDialog(true);
  };

  const handleCloseViewDialog = () => {
    setOpenViewDialog(false);
    setSelectedOrder(null);
  };

  const handleOpenDeleteDialog = (order: PurchaseOrder) => {
    setSelectedOrder(order);
    setOpenDeleteDialog(true);
  };

  const handleCloseDeleteDialog = () => {
    setOpenDeleteDialog(false);
    setSelectedOrder(null);
  };

  // ------- Item Manipulation -------
  const addItem = () => {
    setFormData({
      ...formData,
      items: [
        ...formData.items,
        {
          lineNo: formData.items.length + 1,
          materialId: 0,
          orderedQuantity: 0,
          unit: 'EA',
          unitPrice: 0,
          remarks: '',
        },
      ],
    });
  };

  const removeItem = (index: number) => {
    const newItems = formData.items
      .filter((_, i) => i !== index)
      .map((item, i) => ({ ...item, lineNo: i + 1 }));
    setFormData({ ...formData, items: newItems });
  };

  const updateItem = (index: number, field: string, value: any) => {
    const newItems = [...formData.items];
    const updatedItem = { ...newItems[index], [field]: value };

    // Auto-fill material info when materialId changes
    if (field === 'materialId') {
      const mat = materials.find((m) => m.materialId === Number(value));
      if (mat) {
        updatedItem.materialCode = mat.materialCode;
        updatedItem.materialName = mat.materialName;
        updatedItem.unit = mat.unit;
        updatedItem.unitPrice = mat.currentPrice || mat.standardPrice || 0;
      }
    }

    // Recalculate amount
    if (field === 'orderedQuantity' || field === 'unitPrice' || field === 'materialId') {
      updatedItem.amount = (updatedItem.orderedQuantity || 0) * (updatedItem.unitPrice || 0);
    }

    newItems[index] = updatedItem;
    setFormData({ ...formData, items: newItems });
  };

  // ------- CRUD Operations -------
  const handleSubmit = async () => {
    // Validation
    if (!formData.orderNo) {
      setSnackbar({ open: true, message: '주문번호를 입력해 주세요', severity: 'error' });
      return;
    }
    if (!formData.supplierId || formData.supplierId === 0) {
      setSnackbar({ open: true, message: '공급업체를 선택해 주세요', severity: 'error' });
      return;
    }
    if (formData.items.length === 0) {
      setSnackbar({ open: true, message: '주문 품목을 1개 이상 추가해 주세요', severity: 'error' });
      return;
    }
    for (const item of formData.items) {
      if (!item.materialId || item.materialId === 0) {
        setSnackbar({ open: true, message: '모든 품목에 자재를 선택해 주세요', severity: 'error' });
        return;
      }
      if (!item.orderedQuantity || item.orderedQuantity <= 0) {
        setSnackbar({ open: true, message: '주문 수량은 0보다 커야 합니다', severity: 'error' });
        return;
      }
    }

    try {
      if (isEdit && selectedOrder) {
        const updateRequest: PurchaseOrderUpdateRequest = {
          expectedDeliveryDate: formData.expectedDeliveryDate || undefined,
          deliveryAddress: formData.deliveryAddress || undefined,
          paymentTerms: formData.paymentTerms || undefined,
          currency: formData.currency || undefined,
          remarks: formData.remarks || undefined,
          items: formData.items,
        };
        await purchaseOrderService.update(selectedOrder.purchaseOrderId, updateRequest);
        setSnackbar({ open: true, message: '구매 주문이 수정되었습니다', severity: 'success' });
      } else {
        await purchaseOrderService.create(formData);
        setSnackbar({ open: true, message: '구매 주문이 생성되었습니다', severity: 'success' });
      }
      handleCloseDialog();
      loadPurchaseOrders();
    } catch (error) {
      setSnackbar({ open: true, message: '구매 주문 저장 실패', severity: 'error' });
    }
  };

  const handleDelete = async () => {
    if (!selectedOrder) return;
    try {
      await purchaseOrderService.delete(selectedOrder.purchaseOrderId);
      setSnackbar({ open: true, message: '구매 주문이 삭제되었습니다', severity: 'success' });
      handleCloseDeleteDialog();
      loadPurchaseOrders();
    } catch (error) {
      setSnackbar({ open: true, message: '구매 주문 삭제 실패', severity: 'error' });
    }
  };

  const handleConfirm = async (id: number) => {
    try {
      await purchaseOrderService.confirm(id);
      setSnackbar({ open: true, message: '구매 주문이 확정되었습니다', severity: 'success' });
      loadPurchaseOrders();
    } catch (error) {
      setSnackbar({ open: true, message: '구매 주문 확정 실패', severity: 'error' });
    }
  };

  const handleCancel = async (id: number) => {
    try {
      await purchaseOrderService.cancel(id);
      setSnackbar({ open: true, message: '구매 주문이 취소되었습니다', severity: 'success' });
      loadPurchaseOrders();
    } catch (error) {
      setSnackbar({ open: true, message: '구매 주문 취소 실패', severity: 'error' });
    }
  };

  // ------- Computed Values -------
  const calculateTotalAmount = (): number => {
    return formData.items.reduce((sum, item) => sum + (item.orderedQuantity || 0) * (item.unitPrice || 0), 0);
  };

  // ------- DataGrid Columns -------
  const columns: GridColDef[] = [
    { field: 'orderNo', headerName: '주문번호', width: 160 },
    {
      field: 'orderDate',
      headerName: '주문일자',
      width: 120,
      valueFormatter: (params) => params.value ? new Date(params.value as string).toLocaleDateString('ko-KR') : '-',
    },
    { field: 'supplierName', headerName: '공급업체', width: 200 },
    { field: 'buyerFullName', headerName: '구매담당자', width: 120 },
    {
      field: 'totalAmount',
      headerName: '총액',
      width: 140,
      renderCell: (params) =>
        params.value != null
          ? `${Number(params.value).toLocaleString()} ${params.row.currency || 'KRW'}`
          : '-',
    },
    {
      field: 'expectedDeliveryDate',
      headerName: '납기예정일',
      width: 120,
      valueFormatter: (params) => params.value ? new Date(params.value as string).toLocaleDateString('ko-KR') : '-',
    },
    {
      field: 'status',
      headerName: '상태',
      width: 110,
      renderCell: (params) => (
        <Chip
          label={getStatusLabel(params.value as string)}
          color={getStatusColor(params.value as string)}
          size="small"
        />
      ),
    },
    {
      field: 'actions',
      type: 'actions',
      headerName: '작업',
      width: 200,
      getActions: (params) => {
        const row = params.row as PurchaseOrder;
        const actions = [
          <GridActionsCellItem
            key="view"
            icon={<Tooltip title="상세보기"><ViewIcon /></Tooltip>}
            label="상세보기"
            onClick={() => handleOpenViewDialog(row)}
          />,
        ];

        if (row.status === 'DRAFT') {
          actions.push(
            <GridActionsCellItem
              key="edit"
              icon={<Tooltip title="수정"><EditIcon /></Tooltip>}
              label="수정"
              onClick={() => handleOpenEditDialog(row)}
            />,
            <GridActionsCellItem
              key="confirm"
              icon={<Tooltip title="확정"><CheckCircleIcon color="primary" /></Tooltip>}
              label="확정"
              onClick={() => handleConfirm(row.purchaseOrderId)}
            />,
            <GridActionsCellItem
              key="delete"
              icon={<Tooltip title="삭제"><DeleteIcon color="error" /></Tooltip>}
              label="삭제"
              onClick={() => handleOpenDeleteDialog(row)}
            />,
          );
        }

        if (row.status === 'CONFIRMED' || row.status === 'PARTIALLY_RECEIVED') {
          actions.push(
            <GridActionsCellItem
              key="cancel"
              icon={<Tooltip title="취소"><CancelIcon color="error" /></Tooltip>}
              label="취소"
              onClick={() => handleCancel(row.purchaseOrderId)}
            />,
          );
        }

        return actions;
      },
    },
  ];

  // ------- Render -------
  return (
    <Box sx={{ height: '100%', p: 3 }}>
      {/* Header */}
      <Paper sx={{ p: 2, mb: 2 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Typography variant="h5">구매 주문 관리</Typography>
          <Button variant="contained" startIcon={<AddIcon />} onClick={handleOpenCreateDialog}>
            구매 주문 생성
          </Button>
        </Box>
      </Paper>

      {/* Data Grid */}
      <Paper sx={{ height: 'calc(100vh - 250px)' }}>
        <DataGrid
          rows={purchaseOrders}
          columns={columns}
          loading={loading}
          getRowId={(row) => row.purchaseOrderId}
          pageSizeOptions={[10, 25, 50, 100]}
          initialState={{
            pagination: { paginationModel: { pageSize: 25 } },
          }}
        />
      </Paper>

      {/* ====== Create / Edit Dialog ====== */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="lg" fullWidth>
        <DialogTitle>{isEdit ? '구매 주문 수정' : '신규 구매 주문'}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            {/* Order No */}
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="주문번호"
                value={formData.orderNo}
                onChange={(e) => setFormData({ ...formData, orderNo: e.target.value })}
                required
                disabled={isEdit}
                helperText={!isEdit ? '자동 생성됨 (변경 가능)' : undefined}
              />
            </Grid>

            {/* Supplier */}
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth required>
                <InputLabel>공급업체</InputLabel>
                <Select
                  value={formData.supplierId || ''}
                  onChange={(e) => {
                    const supplierId = Number(e.target.value);
                    const supplier = suppliers.find((s) => s.supplierId === supplierId);
                    setFormData({
                      ...formData,
                      supplierId,
                      paymentTerms: supplier?.paymentTerms || formData.paymentTerms,
                      currency: supplier?.currency || formData.currency,
                    });
                  }}
                  label="공급업체"
                  disabled={isEdit}
                >
                  {suppliers.map((supplier) => (
                    <MenuItem key={supplier.supplierId} value={supplier.supplierId}>
                      {supplier.supplierName} ({supplier.supplierCode})
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>

            {/* Expected Delivery Date */}
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="납기예정일"
                type="date"
                value={formData.expectedDeliveryDate}
                onChange={(e) => setFormData({ ...formData, expectedDeliveryDate: e.target.value })}
                InputLabelProps={{ shrink: true }}
              />
            </Grid>

            {/* Payment Terms */}
            <Grid item xs={12} sm={3}>
              <FormControl fullWidth>
                <InputLabel>결제조건</InputLabel>
                <Select
                  value={formData.paymentTerms || 'NET30'}
                  onChange={(e) => setFormData({ ...formData, paymentTerms: e.target.value })}
                  label="결제조건"
                >
                  <MenuItem value="NET30">NET30</MenuItem>
                  <MenuItem value="NET60">NET60</MenuItem>
                  <MenuItem value="NET90">NET90</MenuItem>
                  <MenuItem value="COD">현금 (COD)</MenuItem>
                  <MenuItem value="ADVANCE">선불</MenuItem>
                </Select>
              </FormControl>
            </Grid>

            {/* Currency */}
            <Grid item xs={12} sm={3}>
              <FormControl fullWidth>
                <InputLabel>통화</InputLabel>
                <Select
                  value={formData.currency || 'KRW'}
                  onChange={(e) => setFormData({ ...formData, currency: e.target.value })}
                  label="통화"
                >
                  <MenuItem value="KRW">원화 (KRW)</MenuItem>
                  <MenuItem value="USD">달러 (USD)</MenuItem>
                  <MenuItem value="EUR">유로 (EUR)</MenuItem>
                  <MenuItem value="JPY">엔화 (JPY)</MenuItem>
                  <MenuItem value="CNY">위안 (CNY)</MenuItem>
                </Select>
              </FormControl>
            </Grid>

            {/* Delivery Address */}
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="납품주소"
                value={formData.deliveryAddress}
                onChange={(e) => setFormData({ ...formData, deliveryAddress: e.target.value })}
              />
            </Grid>

            {/* Remarks */}
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
              <Divider sx={{ mb: 2 }} />
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
                <Typography variant="h6">주문 품목</Typography>
                <Button variant="outlined" size="small" startIcon={<AddIcon />} onClick={addItem}>
                  품목 추가
                </Button>
              </Box>

              {formData.items.length === 0 ? (
                <Typography variant="body2" color="text.secondary" sx={{ textAlign: 'center', py: 3 }}>
                  품목을 추가해 주세요
                </Typography>
              ) : (
                <TableContainer>
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell width={50}>No</TableCell>
                        <TableCell width={250}>자재</TableCell>
                        <TableCell width={100}>수량</TableCell>
                        <TableCell width={80}>단위</TableCell>
                        <TableCell width={120}>단가</TableCell>
                        <TableCell width={130}>금액</TableCell>
                        <TableCell width={130}>요청납기일</TableCell>
                        <TableCell width={150}>비고</TableCell>
                        <TableCell width={50}>삭제</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {formData.items.map((item, index) => (
                        <TableRow key={index}>
                          <TableCell>{item.lineNo}</TableCell>
                          <TableCell>
                            <FormControl fullWidth size="small">
                              <Select
                                value={item.materialId || ''}
                                onChange={(e) => updateItem(index, 'materialId', Number(e.target.value))}
                                displayEmpty
                              >
                                <MenuItem value="" disabled>
                                  자재 선택
                                </MenuItem>
                                {materials.map((mat) => (
                                  <MenuItem key={mat.materialId} value={mat.materialId}>
                                    {mat.materialName} ({mat.materialCode})
                                  </MenuItem>
                                ))}
                              </Select>
                            </FormControl>
                          </TableCell>
                          <TableCell>
                            <TextField
                              size="small"
                              type="number"
                              value={item.orderedQuantity || ''}
                              onChange={(e) => updateItem(index, 'orderedQuantity', Number(e.target.value))}
                              inputProps={{ min: 0 }}
                              sx={{ width: 100 }}
                            />
                          </TableCell>
                          <TableCell>
                            <TextField
                              size="small"
                              value={item.unit || 'EA'}
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
                              inputProps={{ min: 0 }}
                              sx={{ width: 120 }}
                            />
                          </TableCell>
                          <TableCell>
                            <Typography variant="body2">
                              {((item.orderedQuantity || 0) * (item.unitPrice || 0)).toLocaleString()}
                            </Typography>
                          </TableCell>
                          <TableCell>
                            <TextField
                              size="small"
                              type="date"
                              value={item.requiredDate || ''}
                              onChange={(e) => updateItem(index, 'requiredDate', e.target.value)}
                              InputLabelProps={{ shrink: true }}
                              sx={{ width: 130 }}
                            />
                          </TableCell>
                          <TableCell>
                            <TextField
                              size="small"
                              value={item.remarks || ''}
                              onChange={(e) => updateItem(index, 'remarks', e.target.value)}
                              sx={{ width: 150 }}
                            />
                          </TableCell>
                          <TableCell>
                            <IconButton size="small" onClick={() => removeItem(index)} color="error">
                              <RemoveIcon />
                            </IconButton>
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              )}

              {formData.items.length > 0 && (
                <Box sx={{ display: 'flex', justifyContent: 'flex-end', mt: 2, pr: 2 }}>
                  <Typography variant="subtitle1">
                    <strong>합계: {calculateTotalAmount().toLocaleString()} {formData.currency || 'KRW'}</strong>
                  </Typography>
                </Box>
              )}
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

      {/* ====== View Dialog ====== */}
      <Dialog open={openViewDialog} onClose={handleCloseViewDialog} maxWidth="md" fullWidth>
        <DialogTitle>구매 주문 상세</DialogTitle>
        <DialogContent>
          {selectedOrder && (
            <Grid container spacing={2} sx={{ mt: 1 }}>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="text.secondary">주문번호</Typography>
                <Typography variant="body1">{selectedOrder.orderNo}</Typography>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="text.secondary">상태</Typography>
                <Chip
                  label={getStatusLabel(selectedOrder.status)}
                  color={getStatusColor(selectedOrder.status)}
                  size="small"
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="text.secondary">공급업체</Typography>
                <Typography variant="body1">{selectedOrder.supplierName}</Typography>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="text.secondary">구매담당자</Typography>
                <Typography variant="body1">{selectedOrder.buyerFullName}</Typography>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="text.secondary">주문일자</Typography>
                <Typography variant="body1">
                  {selectedOrder.orderDate ? new Date(selectedOrder.orderDate).toLocaleDateString('ko-KR') : '-'}
                </Typography>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="text.secondary">납기예정일</Typography>
                <Typography variant="body1">
                  {selectedOrder.expectedDeliveryDate
                    ? new Date(selectedOrder.expectedDeliveryDate).toLocaleDateString('ko-KR')
                    : '-'}
                </Typography>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="text.secondary">결제조건</Typography>
                <Typography variant="body1">{selectedOrder.paymentTerms || '-'}</Typography>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="text.secondary">총액</Typography>
                <Typography variant="body1">
                  {selectedOrder.totalAmount != null
                    ? `${selectedOrder.totalAmount.toLocaleString()} ${selectedOrder.currency || 'KRW'}`
                    : '-'}
                </Typography>
              </Grid>
              {selectedOrder.deliveryAddress && (
                <Grid item xs={12}>
                  <Typography variant="body2" color="text.secondary">납품주소</Typography>
                  <Typography variant="body1">{selectedOrder.deliveryAddress}</Typography>
                </Grid>
              )}
              {selectedOrder.remarks && (
                <Grid item xs={12}>
                  <Typography variant="body2" color="text.secondary">비고</Typography>
                  <Typography variant="body1">{selectedOrder.remarks}</Typography>
                </Grid>
              )}

              {/* Items */}
              <Grid item xs={12}>
                <Divider sx={{ my: 1 }} />
                <Typography variant="h6" sx={{ mt: 1, mb: 1 }}>주문 품목</Typography>
                <TableContainer>
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>No</TableCell>
                        <TableCell>자재코드</TableCell>
                        <TableCell>자재명</TableCell>
                        <TableCell align="right">주문수량</TableCell>
                        <TableCell align="right">입하수량</TableCell>
                        <TableCell>단위</TableCell>
                        <TableCell align="right">단가</TableCell>
                        <TableCell align="right">금액</TableCell>
                        <TableCell>비고</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {selectedOrder.items.map((item) => (
                        <TableRow key={item.purchaseOrderItemId || item.lineNo}>
                          <TableCell>{item.lineNo}</TableCell>
                          <TableCell>{item.materialCode || '-'}</TableCell>
                          <TableCell>{item.materialName || '-'}</TableCell>
                          <TableCell align="right">{item.orderedQuantity}</TableCell>
                          <TableCell align="right">{item.receivedQuantity || 0}</TableCell>
                          <TableCell>{item.unit}</TableCell>
                          <TableCell align="right">{item.unitPrice?.toLocaleString() || 0}</TableCell>
                          <TableCell align="right">{item.amount?.toLocaleString() || 0}</TableCell>
                          <TableCell>{item.remarks || '-'}</TableCell>
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

      {/* ====== Delete Confirmation Dialog ====== */}
      <Dialog open={openDeleteDialog} onClose={handleCloseDeleteDialog}>
        <DialogTitle>구매 주문 삭제</DialogTitle>
        <DialogContent>
          <Typography>정말 이 구매 주문을 삭제하시겠습니까?</Typography>
          {selectedOrder && (
            <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
              주문번호: {selectedOrder.orderNo} | 공급업체: {selectedOrder.supplierName}
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

      {/* Snackbar */}
      <Snackbar
        open={snackbar.open}
        autoHideDuration={4000}
        onClose={() => setSnackbar({ ...snackbar, open: false })}
      >
        <Alert severity={snackbar.severity} onClose={() => setSnackbar({ ...snackbar, open: false })}>
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Box>
  );
};

export default PurchaseOrdersPage;
