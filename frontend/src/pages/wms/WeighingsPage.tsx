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
  Grid,
  Typography,
  IconButton,
  Tooltip,
} from '@mui/material';
import { DataGrid, GridColDef, GridRenderCellParams } from '@mui/x-data-grid';
import {
  Add as AddIcon,
  CheckCircle as VerifyIcon,
  Cancel as RejectIcon,
  Warning as WarningIcon,
} from '@mui/icons-material';
import weighingService, {
  Weighing,
  WeighingCreateRequest,
  WeighingVerificationRequest,
} from '../../services/weighingService';
import productService, { Product } from '../../services/productService';
import lotService, { Lot } from '../../services/lotService';
import userService from '../../services/userService';
import { User } from '@/types';

const WeighingsPage: React.FC = () => {
  const [weighings, setWeighings] = useState<Weighing[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [lots, setLots] = useState<Lot[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(false);
  const [openDialog, setOpenDialog] = useState(false);
  const [openVerifyDialog, setOpenVerifyDialog] = useState(false);
  const [selectedWeighing, setSelectedWeighing] = useState<Weighing | null>(null);
  const [filterType, setFilterType] = useState<string>('ALL');
  const [filterStatus, setFilterStatus] = useState<string>('ALL');
  const [formData, setFormData] = useState<Partial<WeighingCreateRequest>>({
    weighingType: 'INCOMING',
    unit: 'kg',
  });
  const [verifyData, setVerifyData] = useState<WeighingVerificationRequest>({
    verifierUserId: 0,
    action: 'VERIFY',
    remarks: '',
  });
  const [snackbar, setSnackbar] = useState<{
    open: boolean;
    message: string;
    severity: 'success' | 'error' | 'info' | 'warning';
  }>({
    open: false,
    message: '',
    severity: 'success',
  });

  useEffect(() => {
    loadWeighings();
    loadProducts();
    loadLots();
    loadUsers();
  }, []);

  const loadWeighings = async () => {
    try {
      setLoading(true);
      const data = await weighingService.getAll();
      setWeighings(data || []);
    } catch (error) {
      setWeighings([]);
      setSnackbar({
        open: true,
        message: '칭량 기록 조회 실패',
        severity: 'error',
      });
    } finally {
      setLoading(false);
    }
  };

  const loadProducts = async () => {
    try {
      const data = await productService.getProducts();
      setProducts(data || []);
    } catch (error) {
      setProducts([]);
    }
  };

  const loadLots = async () => {
    try {
      const data = await lotService.getAll();
      setLots(data || []);
    } catch (error) {
      setLots([]);
    }
  };

  const loadUsers = async () => {
    try {
      const response = await userService.getUsers({ size: 1000 });
      setUsers(response?.content || []);
    } catch (error) {
      setUsers([]);
    }
  };

  const handleOpenDialog = () => {
    setFormData({
      weighingType: 'INCOMING',
      weighingDate: new Date().toISOString().slice(0, 16),
      unit: 'kg',
      tareWeight: 0,
      grossWeight: 0,
    });
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setFormData({
      weighingType: 'INCOMING',
      unit: 'kg',
    });
  };

  const handleOpenVerifyDialog = (weighing: Weighing) => {
    setSelectedWeighing(weighing);
    setVerifyData({
      verifierUserId: 0,
      action: 'VERIFY',
      remarks: '',
    });
    setOpenVerifyDialog(true);
  };

  const handleCloseVerifyDialog = () => {
    setOpenVerifyDialog(false);
    setSelectedWeighing(null);
    setVerifyData({ verifierUserId: 0, action: 'VERIFY', remarks: '' });
  };

  const handleInputChange = (field: string, value: any) => {
    const newFormData = { ...formData, [field]: value };

    // Auto-calculate net weight when tare or gross weight changes
    if (
      field === 'tareWeight' ||
      field === 'grossWeight'
    ) {
      const tare = parseFloat(newFormData.tareWeight as any) || 0;
      const gross = parseFloat(newFormData.grossWeight as any) || 0;
      const net = gross - tare;

      // Calculate variance if expected weight is provided
    }

    setFormData(newFormData);
  };

  const handleSubmit = async () => {
    try {
      await weighingService.create(formData as WeighingCreateRequest);
      setSnackbar({
        open: true,
        message: '칭량 기록이 생성되었습니다',
        severity: 'success',
      });
      handleCloseDialog();
      loadWeighings();
    } catch (error) {
      setSnackbar({
        open: true,
        message: '칭량 기록 생성 실패',
        severity: 'error',
      });
    }
  };

  const handleVerify = async () => {
    if (!selectedWeighing) return;

    try {
      await weighingService.verify(selectedWeighing.weighingId, {
        ...verifyData,
        action: 'VERIFY',
      });
      setSnackbar({
        open: true,
        message: '칭량 검증이 완료되었습니다',
        severity: 'success',
      });
      handleCloseVerifyDialog();
      loadWeighings();
    } catch (error: any) {
      setSnackbar({
        open: true,
        message: error.response?.data?.message || '칭량 검증 실패',
        severity: 'error',
      });
    }
  };

  const handleReject = async () => {
    if (!selectedWeighing) return;

    try {
      await weighingService.verify(selectedWeighing.weighingId, {
        ...verifyData,
        action: 'REJECT',
      });
      setSnackbar({
        open: true,
        message: '칭량이 반려되었습니다',
        severity: 'success',
      });
      handleCloseVerifyDialog();
      loadWeighings();
    } catch (error: any) {
      setSnackbar({
        open: true,
        message: error.response?.data?.message || '칭량 반려 실패',
        severity: 'error',
      });
    }
  };

  const getWeighingTypeLabel = (type: string) => {
    const types: { [key: string]: string } = {
      INCOMING: '입고 칭량',
      OUTGOING: '출고 칭량',
      PRODUCTION: '생산 칭량',
      SAMPLING: '샘플링 칭량',
    };
    return types[type] || type;
  };

  const getWeighingTypeColor = (type: string): 'default' | 'primary' | 'secondary' | 'success' | 'error' | 'info' | 'warning' => {
    const colors: { [key: string]: 'default' | 'primary' | 'secondary' | 'success' | 'error' | 'info' | 'warning' } = {
      INCOMING: 'success',
      OUTGOING: 'error',
      PRODUCTION: 'primary',
      SAMPLING: 'info',
    };
    return colors[type] || 'default';
  };

  const getVerificationStatusLabel = (status: string) => {
    const statuses: { [key: string]: string } = {
      PENDING: '검증 대기',
      VERIFIED: '검증 완료',
      REJECTED: '반려',
    };
    return statuses[status] || status;
  };

  const getVerificationStatusColor = (status: string): 'default' | 'primary' | 'secondary' | 'success' | 'error' | 'info' | 'warning' => {
    const colors: { [key: string]: 'default' | 'primary' | 'secondary' | 'success' | 'error' | 'info' | 'warning' } = {
      PENDING: 'warning',
      VERIFIED: 'success',
      REJECTED: 'error',
    };
    return colors[status] || 'default';
  };

  const filteredWeighings = weighings.filter((w) => {
    if (filterType !== 'ALL' && w.weighingType !== filterType) return false;
    if (filterStatus !== 'ALL' && w.verificationStatus !== filterStatus) return false;
    return true;
  });

  const columns: GridColDef[] = [
    {
      field: 'toleranceExceeded',
      headerName: '',
      width: 50,
      renderCell: (params: GridRenderCellParams) =>
        params.value ? (
          <Tooltip title="허용 오차 초과">
            <WarningIcon color="error" />
          </Tooltip>
        ) : null,
    },
    { field: 'weighingNo', headerName: '칭량 번호', width: 150 },
    {
      field: 'weighingType',
      headerName: '칭량 유형',
      width: 120,
      renderCell: (params: GridRenderCellParams) => (
        <Chip
          label={getWeighingTypeLabel(params.value as string)}
          color={getWeighingTypeColor(params.value as string)}
          size="small"
        />
      ),
    },
    {
      field: 'weighingDate',
      headerName: '칭량 일시',
      width: 180,
      renderCell: (params: GridRenderCellParams) =>
        new Date(params.value as string).toLocaleString('ko-KR'),
    },
    { field: 'productCode', headerName: '제품 코드', width: 120 },
    { field: 'productName', headerName: '제품명', width: 180 },
    { field: 'lotNo', headerName: 'LOT', width: 120 },
    {
      field: 'tareWeight',
      headerName: '용기 무게',
      width: 100,
      renderCell: (params: GridRenderCellParams) =>
        `${params.row.tareWeight} ${params.row.unit}`,
    },
    {
      field: 'grossWeight',
      headerName: '총 무게',
      width: 100,
      renderCell: (params: GridRenderCellParams) =>
        `${params.row.grossWeight} ${params.row.unit}`,
    },
    {
      field: 'netWeight',
      headerName: '순 무게',
      width: 100,
      renderCell: (params: GridRenderCellParams) => (
        <Typography
          sx={{
            fontWeight: 'bold',
            color: params.row.toleranceExceeded ? 'error.main' : 'inherit',
          }}
        >
          {params.row.netWeight} {params.row.unit}
        </Typography>
      ),
    },
    {
      field: 'expectedWeight',
      headerName: '예상 무게',
      width: 100,
      renderCell: (params: GridRenderCellParams) =>
        params.value ? `${params.value} ${params.row.unit}` : '-',
    },
    {
      field: 'variance',
      headerName: '편차',
      width: 100,
      renderCell: (params: GridRenderCellParams) =>
        params.value
          ? `${params.value > 0 ? '+' : ''}${params.value} ${params.row.unit}`
          : '-',
    },
    { field: 'operatorName', headerName: '작업자', width: 100 },
    { field: 'verifierName', headerName: '검증자', width: 100 },
    {
      field: 'verificationStatus',
      headerName: '검증 상태',
      width: 110,
      renderCell: (params: GridRenderCellParams) => (
        <Chip
          label={getVerificationStatusLabel(params.value as string)}
          color={getVerificationStatusColor(params.value as string)}
          size="small"
        />
      ),
    },
    {
      field: 'actions',
      headerName: '작업',
      width: 100,
      renderCell: (params: GridRenderCellParams) => (
        <Box>
          {params.row.verificationStatus === 'PENDING' && (
            <Tooltip title="검증/반려">
              <IconButton
                size="small"
                color="primary"
                onClick={() => handleOpenVerifyDialog(params.row as Weighing)}
              >
                <VerifyIcon />
              </IconButton>
            </Tooltip>
          )}
        </Box>
      ),
    },
  ];

  const calculateNetWeight = () => {
    const tare = parseFloat(formData.tareWeight as any) || 0;
    const gross = parseFloat(formData.grossWeight as any) || 0;
    return (gross - tare).toFixed(3);
  };

  const calculateVariance = () => {
    const net = parseFloat(calculateNetWeight());
    const expected = parseFloat(formData.expectedWeight as any) || 0;
    if (expected === 0) return '-';
    return ((net - expected)).toFixed(3);
  };

  return (
    <Box sx={{ p: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4">칭량 관리 (Weighing)</Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={handleOpenDialog}
        >
          칭량 기록 생성
        </Button>
      </Box>

      <Box sx={{ display: 'flex', gap: 2, mb: 2 }}>
        <FormControl sx={{ minWidth: 150 }}>
          <InputLabel>칭량 유형</InputLabel>
          <Select
            value={filterType}
            label="칭량 유형"
            onChange={(e) => setFilterType(e.target.value)}
          >
            <MenuItem value="ALL">전체</MenuItem>
            <MenuItem value="INCOMING">입고 칭량</MenuItem>
            <MenuItem value="OUTGOING">출고 칭량</MenuItem>
            <MenuItem value="PRODUCTION">생산 칭량</MenuItem>
            <MenuItem value="SAMPLING">샘플링 칭량</MenuItem>
          </Select>
        </FormControl>

        <FormControl sx={{ minWidth: 150 }}>
          <InputLabel>검증 상태</InputLabel>
          <Select
            value={filterStatus}
            label="검증 상태"
            onChange={(e) => setFilterStatus(e.target.value)}
          >
            <MenuItem value="ALL">전체</MenuItem>
            <MenuItem value="PENDING">검증 대기</MenuItem>
            <MenuItem value="VERIFIED">검증 완료</MenuItem>
            <MenuItem value="REJECTED">반려</MenuItem>
          </Select>
        </FormControl>

        <Button
          variant="outlined"
          onClick={async () => {
            try {
              const data = await weighingService.getToleranceExceeded();
              setWeighings(data);
              setSnackbar({
                open: true,
                message: `허용 오차 초과 항목: ${data.length}건`,
                severity: 'info',
              });
            } catch (error) {
              // Error silently handled - UI already shows previous data
            }
          }}
        >
          허용 오차 초과만 보기
        </Button>
      </Box>

      <DataGrid
        rows={filteredWeighings}
        columns={columns}
        loading={loading}
        getRowId={(row) => row.weighingId}
        pageSizeOptions={[10, 25, 50, 100]}
        initialState={{
          pagination: { paginationModel: { pageSize: 25 } },
        }}
        sx={{
          height: 600,
          '& .MuiDataGrid-row': {
            '&:hover': {
              backgroundColor: 'action.hover',
            },
          },
        }}
      />

      {/* Create/Edit Dialog */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="md" fullWidth>
        <DialogTitle>칭량 기록 생성</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth>
                <InputLabel>칭량 유형</InputLabel>
                <Select
                  value={formData.weighingType}
                  label="칭량 유형"
                  onChange={(e) => handleInputChange('weighingType', e.target.value)}
                >
                  <MenuItem value="INCOMING">입고 칭량</MenuItem>
                  <MenuItem value="OUTGOING">출고 칭량</MenuItem>
                  <MenuItem value="PRODUCTION">생산 칭량</MenuItem>
                  <MenuItem value="SAMPLING">샘플링 칭량</MenuItem>
                </Select>
              </FormControl>
            </Grid>

            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="칭량 일시"
                type="datetime-local"
                value={formData.weighingDate || ''}
                onChange={(e) => handleInputChange('weighingDate', e.target.value)}
                InputLabelProps={{ shrink: true }}
              />
            </Grid>

            <Grid item xs={12} sm={6}>
              <FormControl fullWidth>
                <InputLabel>제품</InputLabel>
                <Select
                  value={formData.productId || ''}
                  label="제품"
                  onChange={(e) => handleInputChange('productId', e.target.value)}
                >
                  {products.map((product) => (
                    <MenuItem key={product.productId} value={product.productId}>
                      {product.productCode} - {product.productName}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>

            <Grid item xs={12} sm={6}>
              <FormControl fullWidth>
                <InputLabel>LOT (선택)</InputLabel>
                <Select
                  value={formData.lotId || ''}
                  label="LOT (선택)"
                  onChange={(e) => handleInputChange('lotId', e.target.value)}
                >
                  <MenuItem value="">없음</MenuItem>
                  {lots.map((lot) => (
                    <MenuItem key={lot.lotId} value={lot.lotId}>
                      {lot.lotNo}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>

            <Grid item xs={12} sm={4}>
              <TextField
                fullWidth
                label="용기 무게 (Tare)"
                type="number"
                value={formData.tareWeight || ''}
                onChange={(e) => handleInputChange('tareWeight', e.target.value)}
                inputProps={{ step: '0.001', min: '0' }}
              />
            </Grid>

            <Grid item xs={12} sm={4}>
              <TextField
                fullWidth
                label="총 무게 (Gross)"
                type="number"
                value={formData.grossWeight || ''}
                onChange={(e) => handleInputChange('grossWeight', e.target.value)}
                inputProps={{ step: '0.001', min: '0' }}
              />
            </Grid>

            <Grid item xs={12} sm={4}>
              <TextField
                fullWidth
                label="순 무게 (Net)"
                value={calculateNetWeight()}
                InputProps={{ readOnly: true }}
                helperText="자동 계산됨"
              />
            </Grid>

            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="예상 무게 (선택)"
                type="number"
                value={formData.expectedWeight || ''}
                onChange={(e) => handleInputChange('expectedWeight', e.target.value)}
                inputProps={{ step: '0.001', min: '0' }}
              />
            </Grid>

            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="편차"
                value={calculateVariance()}
                InputProps={{ readOnly: true }}
                helperText="예상 무게와의 차이"
              />
            </Grid>

            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="단위"
                value={formData.unit}
                onChange={(e) => handleInputChange('unit', e.target.value)}
              />
            </Grid>

            <Grid item xs={12} sm={6}>
              <FormControl fullWidth>
                <InputLabel>작업자</InputLabel>
                <Select
                  value={formData.operatorUserId || ''}
                  label="작업자"
                  onChange={(e) => handleInputChange('operatorUserId', e.target.value)}
                >
                  {users.map((user) => (
                    <MenuItem key={user.userId} value={user.userId}>
                      {user.username} ({user.fullName})
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>

            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="온도 (°C, 선택)"
                type="number"
                value={formData.temperature || ''}
                onChange={(e) => handleInputChange('temperature', e.target.value)}
                inputProps={{ step: '0.1' }}
              />
            </Grid>

            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="습도 (%, 선택)"
                type="number"
                value={formData.humidity || ''}
                onChange={(e) => handleInputChange('humidity', e.target.value)}
                inputProps={{ step: '0.1', min: '0', max: '100' }}
              />
            </Grid>

            <Grid item xs={12}>
              <TextField
                fullWidth
                label="비고"
                multiline
                rows={2}
                value={formData.remarks || ''}
                onChange={(e) => handleInputChange('remarks', e.target.value)}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>취소</Button>
          <Button
            onClick={handleSubmit}
            variant="contained"
            disabled={!formData.productId || !formData.operatorUserId}
          >
            생성
          </Button>
        </DialogActions>
      </Dialog>

      {/* Verification Dialog */}
      <Dialog open={openVerifyDialog} onClose={handleCloseVerifyDialog} maxWidth="sm" fullWidth>
        <DialogTitle>칭량 검증 (GMP Dual Verification)</DialogTitle>
        <DialogContent>
          {selectedWeighing && (
            <Box sx={{ mt: 2 }}>
              <Typography variant="subtitle2" color="text.secondary">
                칭량 번호: {selectedWeighing.weighingNo}
              </Typography>
              <Typography variant="subtitle2" color="text.secondary">
                제품: {selectedWeighing.productName}
              </Typography>
              <Typography variant="subtitle2" color="text.secondary">
                순 무게: {selectedWeighing.netWeight} {selectedWeighing.unit}
              </Typography>
              <Typography variant="subtitle2" color="text.secondary" sx={{ mb: 2 }}>
                작업자: {selectedWeighing.operatorName}
              </Typography>

              <FormControl fullWidth sx={{ mb: 2 }}>
                <InputLabel>검증자</InputLabel>
                <Select
                  value={verifyData.verifierUserId}
                  label="검증자"
                  onChange={(e) =>
                    setVerifyData({ ...verifyData, verifierUserId: e.target.value as number })
                  }
                >
                  {users
                    .filter((user) => user.userId !== selectedWeighing.operatorUserId)
                    .map((user) => (
                      <MenuItem key={user.userId} value={user.userId}>
                        {user.username} ({user.fullName})
                      </MenuItem>
                    ))}
                </Select>
              </FormControl>

              <TextField
                fullWidth
                label="검증 의견"
                multiline
                rows={3}
                value={verifyData.remarks}
                onChange={(e) => setVerifyData({ ...verifyData, remarks: e.target.value })}
              />

              <Alert severity="info" sx={{ mt: 2 }}>
                GMP 규정에 따라 검증자는 작업자와 달라야 합니다.
              </Alert>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseVerifyDialog}>취소</Button>
          <Button
            onClick={handleReject}
            color="error"
            variant="outlined"
            startIcon={<RejectIcon />}
            disabled={!verifyData.verifierUserId}
          >
            반려
          </Button>
          <Button
            onClick={handleVerify}
            color="success"
            variant="contained"
            startIcon={<VerifyIcon />}
            disabled={!verifyData.verifierUserId}
          >
            검증 완료
          </Button>
        </DialogActions>
      </Dialog>

      <Snackbar
        open={snackbar.open}
        autoHideDuration={6000}
        onClose={() => setSnackbar({ ...snackbar, open: false })}
      >
        <Alert
          onClose={() => setSnackbar({ ...snackbar, open: false })}
          severity={snackbar.severity}
          sx={{ width: '100%' }}
        >
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Box>
  );
};

export default WeighingsPage;
