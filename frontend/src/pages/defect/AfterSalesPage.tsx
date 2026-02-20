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
  PlayArrow as StartIcon,
  CheckCircle as CompleteIcon,
  Close as CloseIcon,
  Delete as DeleteIcon,
  Visibility as VisibilityIcon,
} from '@mui/icons-material';
import afterSalesService, { AfterSales, AfterSalesRequest } from '../../services/afterSalesService';
import customerService, { Customer } from '../../services/customerService';
import productService, { Product } from '../../services/productService';

const AfterSalesPage: React.FC = () => {
  const [afterSales, setAfterSales] = useState<AfterSales[]>([]);
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(false);
  const [openDialog, setOpenDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [selectedAfterSales, setSelectedAfterSales] = useState<AfterSales | null>(null);
  const [viewMode, setViewMode] = useState(false);
  const [snackbar, setSnackbar] = useState<{
    open: boolean;
    message: string;
    severity: 'success' | 'error' | 'info';
  }>({ open: false, message: '', severity: 'success' });

  const [formData, setFormData] = useState<AfterSalesRequest>({
    asNo: '',
    receiptDate: new Date().toISOString().slice(0, 16),
    customerId: 0,
    productId: 0,
    issueDescription: '',
    serviceStatus: 'RECEIVED',
  });

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const [afterSalesData, customersData, productsData] = await Promise.all([
        afterSalesService.getAll(),
        customerService.getActive(),
        productService.getActiveProducts(),
      ]);
      setAfterSales(afterSalesData);
      setCustomers(customersData);
      setProducts(productsData);
    } catch (error) {
      setSnackbar({ open: true, message: 'Failed to load data', severity: 'error' });
    } finally {
      setLoading(false);
    }
  };

  const handleOpenDialog = (item?: AfterSales, view = false) => {
    if (item) {
      setViewMode(view);
      setSelectedAfterSales(item);
      setFormData({
        asNo: item.asNo,
        receiptDate: item.receiptDate,
        customerId: item.customerId,
        productId: item.productId,
        issueDescription: item.issueDescription,
        serviceStatus: item.serviceStatus,
        contactPerson: item.contactPerson,
        contactPhone: item.contactPhone,
        issueCategory: item.issueCategory,
        priority: item.priority,
        remarks: item.remarks,
      });
    } else {
      setViewMode(false);
      setSelectedAfterSales(null);
      setFormData({
        asNo: '',
        receiptDate: new Date().toISOString().slice(0, 16),
        customerId: 0,
        productId: 0,
        issueDescription: '',
        serviceStatus: 'RECEIVED',
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedAfterSales(null);
    setViewMode(false);
  };

  const handleOpenDeleteDialog = (item: AfterSales) => {
    setSelectedAfterSales(item);
    setOpenDeleteDialog(true);
  };

  const handleCloseDeleteDialog = () => {
    setOpenDeleteDialog(false);
    setSelectedAfterSales(null);
  };

  const handleSubmit = async () => {
    try {
      await afterSalesService.create(formData);
      setSnackbar({ open: true, message: 'A/S created successfully', severity: 'success' });
      handleCloseDialog();
      loadData();
    } catch (error) {
      setSnackbar({ open: true, message: 'Failed to create A/S', severity: 'error' });
    }
  };

  const handleDelete = async () => {
    if (!selectedAfterSales) return;

    try {
      await afterSalesService.delete(selectedAfterSales.afterSalesId);
      setSnackbar({ open: true, message: 'A/S deleted successfully', severity: 'success' });
      handleCloseDeleteDialog();
      loadData();
    } catch (error) {
      setSnackbar({ open: true, message: 'Failed to delete A/S', severity: 'error' });
    }
  };

  const handleStart = async (id: number) => {
    try {
      await afterSalesService.start(id);
      setSnackbar({ open: true, message: 'Service started successfully', severity: 'success' });
      loadData();
    } catch (error) {
      setSnackbar({ open: true, message: 'Failed to start service', severity: 'error' });
    }
  };

  const handleComplete = async (id: number) => {
    try {
      await afterSalesService.complete(id);
      setSnackbar({ open: true, message: 'Service completed successfully', severity: 'success' });
      loadData();
    } catch (error) {
      setSnackbar({ open: true, message: 'Failed to complete service', severity: 'error' });
    }
  };

  const handleClose = async (id: number) => {
    try {
      await afterSalesService.close(id);
      setSnackbar({ open: true, message: 'A/S closed successfully', severity: 'success' });
      loadData();
    } catch (error) {
      setSnackbar({ open: true, message: 'Failed to close A/S', severity: 'error' });
    }
  };

  const columns: GridColDef[] = [
    { field: 'asNo', headerName: 'A/S번호', width: 150 },
    {
      field: 'receiptDate',
      headerName: '접수일자',
      width: 180,
      valueFormatter: (params) => new Date(params.value).toLocaleString('ko-KR'),
    },
    { field: 'customerName', headerName: '고객', width: 150 },
    { field: 'productName', headerName: '제품', width: 150 },
    {
      field: 'priority',
      headerName: '우선순위',
      width: 100,
      renderCell: (params) => {
        const priorityColors: { [key: string]: 'error' | 'warning' | 'info' | 'default' } = {
          URGENT: 'error',
          HIGH: 'warning',
          NORMAL: 'info',
          LOW: 'default',
        };
        const priorityLabels: { [key: string]: string } = {
          URGENT: '긴급',
          HIGH: '높음',
          NORMAL: '보통',
          LOW: '낮음',
        };
        return (
          <Chip
            label={priorityLabels[params.value] || params.value || '보통'}
            color={priorityColors[params.value] || 'default'}
            size="small"
          />
        );
      },
    },
    {
      field: 'serviceStatus',
      headerName: '상태',
      width: 120,
      renderCell: (params) => {
        const statusColors: { [key: string]: 'default' | 'warning' | 'info' | 'success' | 'error' } = {
          RECEIVED: 'warning',
          IN_PROGRESS: 'info',
          COMPLETED: 'success',
          CLOSED: 'default',
          CANCELLED: 'error',
        };
        const statusLabels: { [key: string]: string } = {
          RECEIVED: '접수',
          IN_PROGRESS: '진행중',
          COMPLETED: '완료',
          CLOSED: '종료',
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
    { field: 'assignedEngineerName', headerName: '담당엔지니어', width: 120 },
    {
      field: 'actions',
      type: 'actions',
      headerName: '작업',
      width: 200,
      getActions: (params) => [
        <GridActionsCellItem
          icon={<VisibilityIcon />}
          label="View"
          onClick={() => handleOpenDialog(params.row, true)}
        />,
        <GridActionsCellItem
          icon={<StartIcon />}
          label="Start"
          onClick={() => handleStart(params.row.afterSalesId)}
          disabled={params.row.serviceStatus !== 'RECEIVED'}
        />,
        <GridActionsCellItem
          icon={<CompleteIcon />}
          label="Complete"
          onClick={() => handleComplete(params.row.afterSalesId)}
          disabled={params.row.serviceStatus !== 'IN_PROGRESS'}
        />,
        <GridActionsCellItem
          icon={<CloseIcon />}
          label="Close"
          onClick={() => handleClose(params.row.afterSalesId)}
          disabled={params.row.serviceStatus !== 'COMPLETED'}
        />,
        <GridActionsCellItem
          icon={<DeleteIcon />}
          label="Delete"
          onClick={() => handleOpenDeleteDialog(params.row)}
          disabled={params.row.serviceStatus === 'CLOSED'}
        />,
      ],
    },
  ];

  return (
    <Box sx={{ height: '100%', p: 3 }}>
      <Paper sx={{ p: 2, mb: 2 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="h5">A/S 관리</Typography>
          <Button variant="contained" startIcon={<AddIcon />} onClick={() => handleOpenDialog()}>
            신규 A/S
          </Button>
        </Box>
      </Paper>

      <Paper sx={{ height: 'calc(100vh - 250px)' }}>
        <DataGrid
          rows={afterSales}
          columns={columns}
          loading={loading}
          getRowId={(row) => row.afterSalesId}
          pageSizeOptions={[10, 25, 50, 100]}
          initialState={{
            pagination: { paginationModel: { pageSize: 25 } },
          }}
        />
      </Paper>

      {/* Create/View Dialog */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="md" fullWidth>
        <DialogTitle>{viewMode ? 'A/S 상세' : '신규 A/S'}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="A/S번호"
                value={formData.asNo}
                onChange={(e) => setFormData({ ...formData, asNo: e.target.value })}
                required
                disabled={viewMode}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="접수일자"
                type="datetime-local"
                value={formData.receiptDate}
                onChange={(e) => setFormData({ ...formData, receiptDate: e.target.value })}
                required
                disabled={viewMode}
                InputLabelProps={{ shrink: true }}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth required>
                <InputLabel>고객</InputLabel>
                <Select
                  value={formData.customerId || ''}
                  onChange={(e) => setFormData({ ...formData, customerId: e.target.value as number })}
                  label="고객"
                  disabled={viewMode}
                >
                  {customers.map((customer) => (
                    <MenuItem key={customer.customerId} value={customer.customerId}>
                      {customer.customerName}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth required>
                <InputLabel>제품</InputLabel>
                <Select
                  value={formData.productId || ''}
                  onChange={(e) => setFormData({ ...formData, productId: e.target.value as number })}
                  label="제품"
                  disabled={viewMode}
                >
                  {products.map((product) => (
                    <MenuItem key={product.productId} value={product.productId}>
                      {product.productName}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="연락처명"
                value={formData.contactPerson || ''}
                onChange={(e) => setFormData({ ...formData, contactPerson: e.target.value })}
                disabled={viewMode}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="연락전화"
                value={formData.contactPhone || ''}
                onChange={(e) => setFormData({ ...formData, contactPhone: e.target.value })}
                disabled={viewMode}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth>
                <InputLabel>문제분류</InputLabel>
                <Select
                  value={formData.issueCategory || ''}
                  onChange={(e) => setFormData({ ...formData, issueCategory: e.target.value })}
                  label="문제분류"
                  disabled={viewMode}
                >
                  <MenuItem value="DEFECT">불량</MenuItem>
                  <MenuItem value="BREAKDOWN">고장</MenuItem>
                  <MenuItem value="INSTALLATION">설치</MenuItem>
                  <MenuItem value="USAGE">사용법</MenuItem>
                  <MenuItem value="OTHER">기타</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth>
                <InputLabel>우선순위</InputLabel>
                <Select
                  value={formData.priority || ''}
                  onChange={(e) => setFormData({ ...formData, priority: e.target.value })}
                  label="우선순위"
                  disabled={viewMode}
                >
                  <MenuItem value="URGENT">긴급</MenuItem>
                  <MenuItem value="HIGH">높음</MenuItem>
                  <MenuItem value="NORMAL">보통</MenuItem>
                  <MenuItem value="LOW">낮음</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="문제설명"
                multiline
                rows={4}
                value={formData.issueDescription}
                onChange={(e) => setFormData({ ...formData, issueDescription: e.target.value })}
                required
                disabled={viewMode}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="비고"
                multiline
                rows={2}
                value={formData.remarks || ''}
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
        <DialogTitle>A/S 삭제</DialogTitle>
        <DialogContent>
          <Typography>정말 이 A/S를 삭제하시겠습니까?</Typography>
          {selectedAfterSales && (
            <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
              A/S번호: {selectedAfterSales.asNo}
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

export default AfterSalesPage;
