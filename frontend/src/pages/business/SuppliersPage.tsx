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
import supplierService, { Supplier, SupplierCreateRequest, SupplierUpdateRequest } from '../../services/supplierService';

const SuppliersPage: React.FC = () => {
  const [suppliers, setSuppliers] = useState<Supplier[]>([]);
  const [loading, setLoading] = useState(false);
  const [openDialog, setOpenDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [selectedSupplier, setSelectedSupplier] = useState<Supplier | null>(null);
  const [formData, setFormData] = useState<Partial<SupplierCreateRequest>>({
    supplierType: 'MATERIAL',
    currency: 'KRW',
    leadTimeDays: 0,
    isActive: true,
  });
  const [snackbar, setSnackbar] = useState<{ open: boolean; message: string; severity: 'success' | 'error' }>({
    open: false,
    message: '',
    severity: 'success',
  });

  useEffect(() => {
    loadSuppliers();
  }, []);

  const loadSuppliers = async () => {
    try {
      setLoading(true);
      const data = await supplierService.getAll();
      setSuppliers(data || []);
    } catch (error) {
      console.error('Failed to load suppliers:', error);
      setSnackbar({ open: true, message: '공급업체 목록 조회 실패', severity: 'error' });
      setSuppliers([]);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenDialog = (supplier?: Supplier) => {
    if (supplier) {
      setSelectedSupplier(supplier);
      setFormData({
        supplierCode: supplier.supplierCode,
        supplierName: supplier.supplierName,
        supplierType: supplier.supplierType,
        businessNumber: supplier.businessNumber,
        representativeName: supplier.representativeName,
        industry: supplier.industry,
        address: supplier.address,
        postalCode: supplier.postalCode,
        phoneNumber: supplier.phoneNumber,
        faxNumber: supplier.faxNumber,
        email: supplier.email,
        website: supplier.website,
        contactPerson: supplier.contactPerson,
        contactPhone: supplier.contactPhone,
        contactEmail: supplier.contactEmail,
        paymentTerms: supplier.paymentTerms,
        currency: supplier.currency,
        taxType: supplier.taxType,
        leadTimeDays: supplier.leadTimeDays,
        minOrderAmount: supplier.minOrderAmount,
        isActive: supplier.isActive,
        rating: supplier.rating,
        remarks: supplier.remarks,
      });
    } else {
      setSelectedSupplier(null);
      setFormData({
        supplierType: 'MATERIAL',
        currency: 'KRW',
        leadTimeDays: 0,
        isActive: true,
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedSupplier(null);
    setFormData({
      supplierType: 'MATERIAL',
      currency: 'KRW',
      leadTimeDays: 0,
      isActive: true,
    });
  };

  const handleSubmit = async () => {
    try {
      if (selectedSupplier) {
        const updateRequest: SupplierUpdateRequest = {
          supplierId: selectedSupplier.supplierId,
          supplierName: formData.supplierName!,
          supplierType: formData.supplierType!,
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
          currency: formData.currency,
          taxType: formData.taxType,
          leadTimeDays: formData.leadTimeDays,
          minOrderAmount: formData.minOrderAmount,
          isActive: formData.isActive!,
          rating: formData.rating,
          remarks: formData.remarks,
        };
        await supplierService.update(selectedSupplier.supplierId, updateRequest);
        setSnackbar({ open: true, message: '공급업체가 수정되었습니다', severity: 'success' });
      } else {
        await supplierService.create(formData as SupplierCreateRequest);
        setSnackbar({ open: true, message: '공급업체가 생성되었습니다', severity: 'success' });
      }
      handleCloseDialog();
      loadSuppliers();
    } catch (error) {
      console.error('Failed to save supplier:', error);
      setSnackbar({ open: true, message: '공급업체 저장 실패', severity: 'error' });
    }
  };

  const handleDelete = async () => {
    if (!selectedSupplier) return;

    try {
      await supplierService.delete(selectedSupplier.supplierId);
      setSnackbar({ open: true, message: '공급업체가 삭제되었습니다', severity: 'success' });
      setOpenDeleteDialog(false);
      setSelectedSupplier(null);
      loadSuppliers();
    } catch (error) {
      console.error('Failed to delete supplier:', error);
      setSnackbar({ open: true, message: '공급업체 삭제 실패', severity: 'error' });
    }
  };

  const handleToggleActive = async (supplier: Supplier) => {
    try {
      await supplierService.toggleActive(supplier.supplierId);
      setSnackbar({
        open: true,
        message: supplier.isActive ? '공급업체가 비활성화되었습니다' : '공급업체가 활성화되었습니다',
        severity: 'success',
      });
      loadSuppliers();
    } catch (error) {
      console.error('Failed to toggle supplier:', error);
      setSnackbar({ open: true, message: '공급업체 상태 변경 실패', severity: 'error' });
    }
  };

  const getSupplierTypeLabel = (type: string) => {
    const types: { [key: string]: string } = {
      MATERIAL: '자재',
      SERVICE: '서비스',
      EQUIPMENT: '설비',
      BOTH: '복합',
    };
    return types[type] || type;
  };

  const getRatingColor = (rating?: string) => {
    const colors: { [key: string]: 'success' | 'info' | 'warning' | 'error' | 'default' } = {
      EXCELLENT: 'success',
      GOOD: 'info',
      AVERAGE: 'warning',
      POOR: 'error',
    };
    return rating ? colors[rating] || 'default' : 'default';
  };

  const columns: GridColDef[] = [
    { field: 'supplierCode', headerName: '공급업체 코드', width: 130 },
    { field: 'supplierName', headerName: '공급업체명', width: 200 },
    {
      field: 'supplierType',
      headerName: '유형',
      width: 100,
      renderCell: (params: GridRenderCellParams) => getSupplierTypeLabel(params.value as string),
    },
    { field: 'contactPerson', headerName: '담당자', width: 120 },
    { field: 'phoneNumber', headerName: '전화번호', width: 150 },
    { field: 'leadTimeDays', headerName: '리드타임(일)', width: 110 },
    {
      field: 'rating',
      headerName: '평가',
      width: 100,
      renderCell: (params: GridRenderCellParams) =>
        params.value ? (
          <Chip label={params.value} color={getRatingColor(params.value as string)} size="small" />
        ) : (
          '-'
        ),
    },
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
          <IconButton size="small" onClick={() => handleOpenDialog(params.row as Supplier)}>
            <EditIcon fontSize="small" />
          </IconButton>
          <IconButton
            size="small"
            onClick={() => handleToggleActive(params.row as Supplier)}
            color={params.row.isActive ? 'default' : 'success'}
          >
            {params.row.isActive ? <CancelIcon fontSize="small" /> : <CheckCircleIcon fontSize="small" />}
          </IconButton>
          <IconButton
            size="small"
            onClick={() => {
              setSelectedSupplier(params.row as Supplier);
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
        <h2>공급업체 관리</h2>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => handleOpenDialog()}>
          공급업체 생성
        </Button>
      </Box>

      <DataGrid
        rows={suppliers}
        columns={columns}
        loading={loading}
        getRowId={(row) => row.supplierId}
        pageSizeOptions={[10, 25, 50, 100]}
        initialState={{
          pagination: { paginationModel: { pageSize: 25 } },
        }}
        sx={{ height: 600 }}
      />

      {/* Create/Edit Dialog */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="md" fullWidth>
        <DialogTitle>{selectedSupplier ? '공급업체 수정' : '공급업체 생성'}</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 2 }}>
            <Grid container spacing={2}>
              <Grid item xs={6}>
                <TextField
                  label="공급업체 코드"
                  value={formData.supplierCode || ''}
                  onChange={(e) => setFormData({ ...formData, supplierCode: e.target.value })}
                  disabled={!!selectedSupplier}
                  required
                  fullWidth
                />
              </Grid>
              <Grid item xs={6}>
                <TextField
                  label="공급업체명"
                  value={formData.supplierName || ''}
                  onChange={(e) => setFormData({ ...formData, supplierName: e.target.value })}
                  required
                  fullWidth
                />
              </Grid>
              <Grid item xs={6}>
                <FormControl fullWidth required>
                  <InputLabel>공급업체 유형</InputLabel>
                  <Select
                    value={formData.supplierType || 'MATERIAL'}
                    onChange={(e) => setFormData({ ...formData, supplierType: e.target.value })}
                    label="공급업체 유형"
                  >
                    <MenuItem value="MATERIAL">자재</MenuItem>
                    <MenuItem value="SERVICE">서비스</MenuItem>
                    <MenuItem value="EQUIPMENT">설비</MenuItem>
                    <MenuItem value="BOTH">복합</MenuItem>
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
              <Grid item xs={3}>
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
              <Grid item xs={3}>
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
              <Grid item xs={3}>
                <TextField
                  label="리드타임 (일)"
                  type="number"
                  value={formData.leadTimeDays || 0}
                  onChange={(e) => setFormData({ ...formData, leadTimeDays: Number(e.target.value) })}
                  fullWidth
                />
              </Grid>
              <Grid item xs={3}>
                <TextField
                  label="최소 주문금액"
                  type="number"
                  value={formData.minOrderAmount || ''}
                  onChange={(e) => setFormData({ ...formData, minOrderAmount: Number(e.target.value) })}
                  fullWidth
                />
              </Grid>
              <Grid item xs={12}>
                <FormControl fullWidth>
                  <InputLabel>평가 등급</InputLabel>
                  <Select
                    value={formData.rating || ''}
                    onChange={(e) => setFormData({ ...formData, rating: e.target.value })}
                    label="평가 등급"
                  >
                    <MenuItem value="">선택 안함</MenuItem>
                    <MenuItem value="EXCELLENT">우수</MenuItem>
                    <MenuItem value="GOOD">양호</MenuItem>
                    <MenuItem value="AVERAGE">보통</MenuItem>
                    <MenuItem value="POOR">미흡</MenuItem>
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
            {selectedSupplier ? '수정' : '생성'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Dialog */}
      <Dialog open={openDeleteDialog} onClose={() => setOpenDeleteDialog(false)}>
        <DialogTitle>공급업체 삭제</DialogTitle>
        <DialogContent>정말로 이 공급업체를 삭제하시겠습니까?</DialogContent>
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

export default SuppliersPage;
