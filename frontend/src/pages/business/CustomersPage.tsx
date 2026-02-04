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
  Grid,
} from '@mui/material';
import { DataGrid, GridColDef, GridRenderCellParams } from '@mui/x-data-grid';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  CheckCircle as CheckCircleIcon,
  Cancel as CancelIcon,
} from '@mui/icons-material';
import customerService, { Customer, CustomerCreateRequest, CustomerUpdateRequest } from '../../services/customerService';

const CustomersPage: React.FC = () => {
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [loading, setLoading] = useState(false);
  const [openDialog, setOpenDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [selectedCustomer, setSelectedCustomer] = useState<Customer | null>(null);
  const [formData, setFormData] = useState<Partial<CustomerCreateRequest>>({
    customerType: 'DOMESTIC',
    currency: 'KRW',
    isActive: true,
  });
  const [snackbar, setSnackbar] = useState<{ open: boolean; message: string; severity: 'success' | 'error' }>({
    open: false,
    message: '',
    severity: 'success',
  });

  useEffect(() => {
    loadCustomers();
  }, []);

  const loadCustomers = async () => {
    try {
      setLoading(true);
      const data = await customerService.getAll();
      setCustomers(data);
    } catch (error) {
      console.error('Failed to load customers:', error);
      setSnackbar({ open: true, message: '고객 목록 조회 실패', severity: 'error' });
    } finally {
      setLoading(false);
    }
  };

  const handleOpenDialog = (customer?: Customer) => {
    if (customer) {
      setSelectedCustomer(customer);
      setFormData({
        customerCode: customer.customerCode,
        customerName: customer.customerName,
        customerType: customer.customerType,
        businessNumber: customer.businessNumber,
        representativeName: customer.representativeName,
        industry: customer.industry,
        address: customer.address,
        postalCode: customer.postalCode,
        phoneNumber: customer.phoneNumber,
        faxNumber: customer.faxNumber,
        email: customer.email,
        website: customer.website,
        contactPerson: customer.contactPerson,
        contactPhone: customer.contactPhone,
        contactEmail: customer.contactEmail,
        paymentTerms: customer.paymentTerms,
        creditLimit: customer.creditLimit,
        currency: customer.currency,
        taxType: customer.taxType,
        isActive: customer.isActive,
        remarks: customer.remarks,
      });
    } else {
      setSelectedCustomer(null);
      setFormData({
        customerType: 'DOMESTIC',
        currency: 'KRW',
        isActive: true,
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedCustomer(null);
    setFormData({
      customerType: 'DOMESTIC',
      currency: 'KRW',
      isActive: true,
    });
  };

  const handleSubmit = async () => {
    try {
      if (selectedCustomer) {
        const updateRequest: CustomerUpdateRequest = {
          customerId: selectedCustomer.customerId,
          customerName: formData.customerName!,
          customerType: formData.customerType!,
          businessNumber: formData.businessNumber,
          representativeName: formData.representativeName,
          industry: formData.industry,
          address: formData.address,
          postalCode: formData.postalCode,
          phoneNumber: formData.phoneNumber,
          faxNumber: formData.faxNumber,
          email: formData.email,
          website: formData.website,
          contactPerson: formData.contactPerson,
          contactPhone: formData.contactPhone,
          contactEmail: formData.contactEmail,
          paymentTerms: formData.paymentTerms,
          creditLimit: formData.creditLimit,
          currency: formData.currency,
          taxType: formData.taxType,
          isActive: formData.isActive!,
          remarks: formData.remarks,
        };
        await customerService.update(selectedCustomer.customerId, updateRequest);
        setSnackbar({ open: true, message: '고객이 수정되었습니다', severity: 'success' });
      } else {
        await customerService.create(formData as CustomerCreateRequest);
        setSnackbar({ open: true, message: '고객이 생성되었습니다', severity: 'success' });
      }
      handleCloseDialog();
      loadCustomers();
    } catch (error) {
      console.error('Failed to save customer:', error);
      setSnackbar({ open: true, message: '고객 저장 실패', severity: 'error' });
    }
  };

  const handleDelete = async () => {
    if (!selectedCustomer) return;

    try {
      await customerService.delete(selectedCustomer.customerId);
      setSnackbar({ open: true, message: '고객이 삭제되었습니다', severity: 'success' });
      setOpenDeleteDialog(false);
      setSelectedCustomer(null);
      loadCustomers();
    } catch (error) {
      console.error('Failed to delete customer:', error);
      setSnackbar({ open: true, message: '고객 삭제 실패', severity: 'error' });
    }
  };

  const handleToggleActive = async (customer: Customer) => {
    try {
      await customerService.toggleActive(customer.customerId);
      setSnackbar({
        open: true,
        message: customer.isActive ? '고객이 비활성화되었습니다' : '고객이 활성화되었습니다',
        severity: 'success',
      });
      loadCustomers();
    } catch (error) {
      console.error('Failed to toggle customer:', error);
      setSnackbar({ open: true, message: '고객 상태 변경 실패', severity: 'error' });
    }
  };

  const getCustomerTypeLabel = (type: string) => {
    const types: { [key: string]: string } = {
      DOMESTIC: '국내',
      OVERSEAS: '해외',
      BOTH: '국내/해외',
    };
    return types[type] || type;
  };

  const columns: GridColDef[] = [
    { field: 'customerCode', headerName: '고객 코드', width: 120 },
    { field: 'customerName', headerName: '고객명', width: 200 },
    {
      field: 'customerType',
      headerName: '유형',
      width: 100,
      renderCell: (params: GridRenderCellParams) => getCustomerTypeLabel(params.value as string),
    },
    { field: 'representativeName', headerName: '대표자', width: 120 },
    { field: 'contactPerson', headerName: '담당자', width: 120 },
    { field: 'phoneNumber', headerName: '전화번호', width: 150 },
    { field: 'email', headerName: '이메일', width: 180 },
    {
      field: 'isActive',
      headerName: '상태',
      width: 100,
      renderCell: (params: GridRenderCellParams) => (
        <Chip
          label={params.value ? '활성' : '비활성'}
          color={params.value ? 'success' : 'default'}
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
          <IconButton size="small" onClick={() => handleOpenDialog(params.row as Customer)}>
            <EditIcon fontSize="small" />
          </IconButton>
          <IconButton
            size="small"
            onClick={() => handleToggleActive(params.row as Customer)}
            color={params.row.isActive ? 'default' : 'success'}
          >
            {params.row.isActive ? <CancelIcon fontSize="small" /> : <CheckCircleIcon fontSize="small" />}
          </IconButton>
          <IconButton
            size="small"
            onClick={() => {
              setSelectedCustomer(params.row as Customer);
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
        <h2>고객 관리</h2>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => handleOpenDialog()}>
          고객 생성
        </Button>
      </Box>

      <DataGrid
        rows={customers}
        columns={columns}
        loading={loading}
        getRowId={(row) => row.customerId}
        pageSizeOptions={[10, 25, 50, 100]}
        initialState={{
          pagination: { paginationModel: { pageSize: 25 } },
        }}
        sx={{ height: 600 }}
      />

      {/* Create/Edit Dialog */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="md" fullWidth>
        <DialogTitle>{selectedCustomer ? '고객 수정' : '고객 생성'}</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 2 }}>
            <Grid container spacing={2}>
              <Grid item xs={6}>
                <TextField
                  label="고객 코드"
                  value={formData.customerCode || ''}
                  onChange={(e) => setFormData({ ...formData, customerCode: e.target.value })}
                  disabled={!!selectedCustomer}
                  required
                  fullWidth
                />
              </Grid>
              <Grid item xs={6}>
                <TextField
                  label="고객명"
                  value={formData.customerName || ''}
                  onChange={(e) => setFormData({ ...formData, customerName: e.target.value })}
                  required
                  fullWidth
                />
              </Grid>
              <Grid item xs={6}>
                <FormControl fullWidth required>
                  <InputLabel>고객 유형</InputLabel>
                  <Select
                    value={formData.customerType || 'DOMESTIC'}
                    onChange={(e) => setFormData({ ...formData, customerType: e.target.value })}
                    label="고객 유형"
                  >
                    <MenuItem value="DOMESTIC">국내</MenuItem>
                    <MenuItem value="OVERSEAS">해외</MenuItem>
                    <MenuItem value="BOTH">국내/해외</MenuItem>
                  </Select>
                </FormControl>
              </Grid>
              <Grid item xs={6}>
                <TextField
                  label="사업자번호"
                  value={formData.businessNumber || ''}
                  onChange={(e) => setFormData({ ...formData, businessNumber: e.target.value })}
                  fullWidth
                />
              </Grid>
              <Grid item xs={6}>
                <TextField
                  label="대표자명"
                  value={formData.representativeName || ''}
                  onChange={(e) => setFormData({ ...formData, representativeName: e.target.value })}
                  fullWidth
                />
              </Grid>
              <Grid item xs={6}>
                <TextField
                  label="업종"
                  value={formData.industry || ''}
                  onChange={(e) => setFormData({ ...formData, industry: e.target.value })}
                  fullWidth
                />
              </Grid>
              <Grid item xs={12}>
                <TextField
                  label="주소"
                  value={formData.address || ''}
                  onChange={(e) => setFormData({ ...formData, address: e.target.value })}
                  fullWidth
                />
              </Grid>
              <Grid item xs={6}>
                <TextField
                  label="우편번호"
                  value={formData.postalCode || ''}
                  onChange={(e) => setFormData({ ...formData, postalCode: e.target.value })}
                  fullWidth
                />
              </Grid>
              <Grid item xs={6}>
                <TextField
                  label="전화번호"
                  value={formData.phoneNumber || ''}
                  onChange={(e) => setFormData({ ...formData, phoneNumber: e.target.value })}
                  fullWidth
                />
              </Grid>
              <Grid item xs={6}>
                <TextField
                  label="팩스번호"
                  value={formData.faxNumber || ''}
                  onChange={(e) => setFormData({ ...formData, faxNumber: e.target.value })}
                  fullWidth
                />
              </Grid>
              <Grid item xs={6}>
                <TextField
                  label="이메일"
                  value={formData.email || ''}
                  onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                  fullWidth
                />
              </Grid>
              <Grid item xs={12}>
                <TextField
                  label="웹사이트"
                  value={formData.website || ''}
                  onChange={(e) => setFormData({ ...formData, website: e.target.value })}
                  fullWidth
                />
              </Grid>
              <Grid item xs={4}>
                <TextField
                  label="담당자명"
                  value={formData.contactPerson || ''}
                  onChange={(e) => setFormData({ ...formData, contactPerson: e.target.value })}
                  fullWidth
                />
              </Grid>
              <Grid item xs={4}>
                <TextField
                  label="담당자 전화"
                  value={formData.contactPhone || ''}
                  onChange={(e) => setFormData({ ...formData, contactPhone: e.target.value })}
                  fullWidth
                />
              </Grid>
              <Grid item xs={4}>
                <TextField
                  label="담당자 이메일"
                  value={formData.contactEmail || ''}
                  onChange={(e) => setFormData({ ...formData, contactEmail: e.target.value })}
                  fullWidth
                />
              </Grid>
              <Grid item xs={4}>
                <FormControl fullWidth>
                  <InputLabel>결제 조건</InputLabel>
                  <Select
                    value={formData.paymentTerms || ''}
                    onChange={(e) => setFormData({ ...formData, paymentTerms: e.target.value })}
                    label="결제 조건"
                  >
                    <MenuItem value="">선택 안함</MenuItem>
                    <MenuItem value="NET30">NET30</MenuItem>
                    <MenuItem value="NET60">NET60</MenuItem>
                    <MenuItem value="COD">현금</MenuItem>
                    <MenuItem value="ADVANCE">선불</MenuItem>
                  </Select>
                </FormControl>
              </Grid>
              <Grid item xs={4}>
                <TextField
                  label="여신 한도"
                  type="number"
                  value={formData.creditLimit || ''}
                  onChange={(e) => setFormData({ ...formData, creditLimit: Number(e.target.value) })}
                  fullWidth
                />
              </Grid>
              <Grid item xs={4}>
                <FormControl fullWidth>
                  <InputLabel>통화</InputLabel>
                  <Select
                    value={formData.currency || 'KRW'}
                    onChange={(e) => setFormData({ ...formData, currency: e.target.value })}
                    label="통화"
                  >
                    <MenuItem value="KRW">KRW</MenuItem>
                    <MenuItem value="USD">USD</MenuItem>
                    <MenuItem value="EUR">EUR</MenuItem>
                    <MenuItem value="JPY">JPY</MenuItem>
                    <MenuItem value="CNY">CNY</MenuItem>
                  </Select>
                </FormControl>
              </Grid>
              <Grid item xs={12}>
                <TextField
                  label="비고"
                  value={formData.remarks || ''}
                  onChange={(e) => setFormData({ ...formData, remarks: e.target.value })}
                  multiline
                  rows={3}
                  fullWidth
                />
              </Grid>
            </Grid>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>취소</Button>
          <Button onClick={handleSubmit} variant="contained">
            {selectedCustomer ? '수정' : '생성'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Dialog */}
      <Dialog open={openDeleteDialog} onClose={() => setOpenDeleteDialog(false)}>
        <DialogTitle>고객 삭제</DialogTitle>
        <DialogContent>정말로 이 고객을 삭제하시겠습니까?</DialogContent>
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

export default CustomersPage;
