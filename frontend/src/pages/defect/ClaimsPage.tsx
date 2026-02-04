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
  Search as InvestigateIcon,
  CheckCircle as ResolveIcon,
  Close as CloseIcon,
  Delete as DeleteIcon,
  Visibility as VisibilityIcon,
} from '@mui/icons-material';
import claimService, { Claim, ClaimRequest } from '../../services/claimService';
import customerService, { Customer } from '../../services/customerService';
import productService, { Product } from '../../services/productService';

const ClaimsPage: React.FC = () => {
  const [claims, setClaims] = useState<Claim[]>([]);
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(false);
  const [openDialog, setOpenDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [selectedClaim, setSelectedClaim] = useState<Claim | null>(null);
  const [viewMode, setViewMode] = useState(false);
  const [snackbar, setSnackbar] = useState<{
    open: boolean;
    message: string;
    severity: 'success' | 'error' | 'info';
  }>({ open: false, message: '', severity: 'success' });

  const [formData, setFormData] = useState<ClaimRequest>({
    claimNo: '',
    claimDate: new Date().toISOString().slice(0, 16),
    customerId: 0,
    claimDescription: '',
    status: 'RECEIVED',
  });

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const [claimsData, customersData, productsData] = await Promise.all([
        claimService.getAll(),
        customerService.getActive(),
        productService.getActive(),
      ]);
      setClaims(claimsData);
      setCustomers(customersData);
      setProducts(productsData);
    } catch (error) {
      setSnackbar({ open: true, message: 'Failed to load data', severity: 'error' });
    } finally {
      setLoading(false);
    }
  };

  const handleOpenDialog = (claim?: Claim, view = false) => {
    if (claim) {
      setViewMode(view);
      setSelectedClaim(claim);
      setFormData({
        claimNo: claim.claimNo,
        claimDate: claim.claimDate,
        customerId: claim.customerId,
        claimDescription: claim.claimDescription,
        status: claim.status,
        contactPerson: claim.contactPerson,
        contactPhone: claim.contactPhone,
        productId: claim.productId,
        claimType: claim.claimType,
        severity: claim.severity,
        priority: claim.priority,
        remarks: claim.remarks,
      });
    } else {
      setViewMode(false);
      setSelectedClaim(null);
      setFormData({
        claimNo: '',
        claimDate: new Date().toISOString().slice(0, 16),
        customerId: 0,
        claimDescription: '',
        status: 'RECEIVED',
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedClaim(null);
    setViewMode(false);
  };

  const handleOpenDeleteDialog = (claim: Claim) => {
    setSelectedClaim(claim);
    setOpenDeleteDialog(true);
  };

  const handleCloseDeleteDialog = () => {
    setOpenDeleteDialog(false);
    setSelectedClaim(null);
  };

  const handleSubmit = async () => {
    try {
      await claimService.create(formData);
      setSnackbar({ open: true, message: 'Claim created successfully', severity: 'success' });
      handleCloseDialog();
      loadData();
    } catch (error) {
      setSnackbar({ open: true, message: 'Failed to create claim', severity: 'error' });
    }
  };

  const handleDelete = async () => {
    if (!selectedClaim) return;

    try {
      await claimService.delete(selectedClaim.claimId);
      setSnackbar({ open: true, message: 'Claim deleted successfully', severity: 'success' });
      handleCloseDeleteDialog();
      loadData();
    } catch (error) {
      setSnackbar({ open: true, message: 'Failed to delete claim', severity: 'error' });
    }
  };

  const handleInvestigate = async (id: number) => {
    try {
      await claimService.investigate(id);
      setSnackbar({ open: true, message: 'Investigation started successfully', severity: 'success' });
      loadData();
    } catch (error) {
      setSnackbar({ open: true, message: 'Failed to start investigation', severity: 'error' });
    }
  };

  const handleResolve = async (id: number) => {
    try {
      await claimService.resolve(id);
      setSnackbar({ open: true, message: 'Claim resolved successfully', severity: 'success' });
      loadData();
    } catch (error) {
      setSnackbar({ open: true, message: 'Failed to resolve claim', severity: 'error' });
    }
  };

  const handleClose = async (id: number) => {
    try {
      await claimService.close(id);
      setSnackbar({ open: true, message: 'Claim closed successfully', severity: 'success' });
      loadData();
    } catch (error) {
      setSnackbar({ open: true, message: 'Failed to close claim', severity: 'error' });
    }
  };

  const columns: GridColDef[] = [
    { field: 'claimNo', headerName: '클레임번호', width: 150 },
    {
      field: 'claimDate',
      headerName: '클레임일자',
      width: 180,
      valueFormatter: (params) => new Date(params).toLocaleString('ko-KR'),
    },
    { field: 'customerName', headerName: '고객', width: 150 },
    { field: 'productName', headerName: '제품', width: 150 },
    {
      field: 'claimType',
      headerName: '유형',
      width: 120,
      valueFormatter: (params) => {
        const types: { [key: string]: string } = {
          QUALITY: '품질',
          DELIVERY: '납기',
          QUANTITY: '수량',
          PACKAGING: '포장',
          DOCUMENTATION: '문서',
          SERVICE: '서비스',
          PRICE: '가격',
          OTHER: '기타',
        };
        return types[params] || params;
      },
    },
    {
      field: 'severity',
      headerName: '심각도',
      width: 100,
      renderCell: (params) => {
        const severityColors: { [key: string]: 'error' | 'warning' | 'default' } = {
          CRITICAL: 'error',
          MAJOR: 'warning',
          MINOR: 'default',
        };
        const severityLabels: { [key: string]: string } = {
          CRITICAL: '긴급',
          MAJOR: '중요',
          MINOR: '경미',
        };
        return params.value ? (
          <Chip
            label={severityLabels[params.value] || params.value}
            color={severityColors[params.value] || 'default'}
            size="small"
          />
        ) : null;
      },
    },
    {
      field: 'status',
      headerName: '상태',
      width: 120,
      renderCell: (params) => {
        const statusColors: { [key: string]: 'default' | 'warning' | 'info' | 'success' | 'error' } = {
          RECEIVED: 'warning',
          INVESTIGATING: 'info',
          IN_PROGRESS: 'info',
          RESOLVED: 'success',
          CLOSED: 'default',
          REJECTED: 'error',
        };
        const statusLabels: { [key: string]: string } = {
          RECEIVED: '접수',
          INVESTIGATING: '조사중',
          IN_PROGRESS: '처리중',
          RESOLVED: '해결',
          CLOSED: '종료',
          REJECTED: '거부',
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
          icon={<InvestigateIcon />}
          label="Investigate"
          onClick={() => handleInvestigate(params.row.claimId)}
          disabled={params.row.status !== 'RECEIVED'}
        />,
        <GridActionsCellItem
          icon={<ResolveIcon />}
          label="Resolve"
          onClick={() => handleResolve(params.row.claimId)}
          disabled={!['INVESTIGATING', 'IN_PROGRESS'].includes(params.row.status)}
        />,
        <GridActionsCellItem
          icon={<CloseIcon />}
          label="Close"
          onClick={() => handleClose(params.row.claimId)}
          disabled={params.row.status !== 'RESOLVED'}
        />,
        <GridActionsCellItem
          icon={<DeleteIcon />}
          label="Delete"
          onClick={() => handleOpenDeleteDialog(params.row)}
          disabled={params.row.status === 'CLOSED'}
        />,
      ],
    },
  ];

  return (
    <Box sx={{ height: '100%', p: 3 }}>
      <Paper sx={{ p: 2, mb: 2 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="h5">클레임 관리</Typography>
          <Button variant="contained" startIcon={<AddIcon />} onClick={() => handleOpenDialog()}>
            신규 클레임
          </Button>
        </Box>
      </Paper>

      <Paper sx={{ height: 'calc(100vh - 250px)' }}>
        <DataGrid
          rows={claims}
          columns={columns}
          loading={loading}
          getRowId={(row) => row.claimId}
          pageSizeOptions={[10, 25, 50, 100]}
          initialState={{
            pagination: { paginationModel: { pageSize: 25 } },
          }}
        />
      </Paper>

      {/* Create/View Dialog */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="md" fullWidth>
        <DialogTitle>{viewMode ? '클레임 상세' : '신규 클레임'}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="클레임번호"
                value={formData.claimNo}
                onChange={(e) => setFormData({ ...formData, claimNo: e.target.value })}
                required
                disabled={viewMode}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="클레임일자"
                type="datetime-local"
                value={formData.claimDate}
                onChange={(e) => setFormData({ ...formData, claimDate: e.target.value })}
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
              <FormControl fullWidth>
                <InputLabel>제품</InputLabel>
                <Select
                  value={formData.productId || ''}
                  onChange={(e) => setFormData({ ...formData, productId: e.target.value as number })}
                  label="제품"
                  disabled={viewMode}
                >
                  <MenuItem value="">
                    <em>선택 안 함</em>
                  </MenuItem>
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
                <InputLabel>클레임유형</InputLabel>
                <Select
                  value={formData.claimType || ''}
                  onChange={(e) => setFormData({ ...formData, claimType: e.target.value })}
                  label="클레임유형"
                  disabled={viewMode}
                >
                  <MenuItem value="QUALITY">품질</MenuItem>
                  <MenuItem value="DELIVERY">납기</MenuItem>
                  <MenuItem value="QUANTITY">수량</MenuItem>
                  <MenuItem value="PACKAGING">포장</MenuItem>
                  <MenuItem value="DOCUMENTATION">문서</MenuItem>
                  <MenuItem value="SERVICE">서비스</MenuItem>
                  <MenuItem value="PRICE">가격</MenuItem>
                  <MenuItem value="OTHER">기타</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth>
                <InputLabel>심각도</InputLabel>
                <Select
                  value={formData.severity || ''}
                  onChange={(e) => setFormData({ ...formData, severity: e.target.value })}
                  label="심각도"
                  disabled={viewMode}
                >
                  <MenuItem value="CRITICAL">긴급</MenuItem>
                  <MenuItem value="MAJOR">중요</MenuItem>
                  <MenuItem value="MINOR">경미</MenuItem>
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
                label="클레임설명"
                multiline
                rows={4}
                value={formData.claimDescription}
                onChange={(e) => setFormData({ ...formData, claimDescription: e.target.value })}
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
        <DialogTitle>클레임 삭제</DialogTitle>
        <DialogContent>
          <Typography>정말 이 클레임을 삭제하시겠습니까?</Typography>
          {selectedClaim && (
            <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
              클레임번호: {selectedClaim.claimNo}
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

export default ClaimsPage;
