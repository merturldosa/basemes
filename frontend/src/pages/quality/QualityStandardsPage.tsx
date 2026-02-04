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
  Chip,
  Alert,
  Snackbar,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
} from '@mui/material';
import {
  DataGrid,
  GridColDef,
  GridActionsCellItem,
  GridRowParams,
} from '@mui/x-data-grid';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  ToggleOn as ToggleOnIcon,
  ToggleOff as ToggleOffIcon,
} from '@mui/icons-material';
import qualityStandardService, { QualityStandard, QualityStandardCreateRequest, QualityStandardUpdateRequest } from '../../services/qualityStandardService';
import productService, { Product } from '../../services/productService';

const QualityStandardsPage: React.FC = () => {
  const [qualityStandards, setQualityStandards] = useState<QualityStandard[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [openDialog, setOpenDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [selectedQualityStandard, setSelectedQualityStandard] = useState<QualityStandard | null>(null);
  const [formData, setFormData] = useState<QualityStandardCreateRequest | QualityStandardUpdateRequest>({
    productId: 0,
    standardCode: '',
    standardName: '',
    standardVersion: '1.0',
    inspectionType: 'INCOMING',
    effectiveDate: new Date().toISOString().split('T')[0],
  });
  const [snackbar, setSnackbar] = useState<{ open: boolean; message: string; severity: 'success' | 'error' }>({
    open: false,
    message: '',
    severity: 'success',
  });

  useEffect(() => {
    loadQualityStandards();
  }, []);

  const loadQualityStandards = async () => {
    try {
      setLoading(true);
      const [standardsData, productsData] = await Promise.all([
        qualityStandardService.getQualityStandards(),
        productService.getActiveProducts(),
      ]);
      setQualityStandards(standardsData);
      setProducts(productsData);
    } catch (error) {
      showSnackbar('품질 기준 목록 조회 실패', 'error');
    } finally {
      setLoading(false);
    }
  };

  const showSnackbar = (message: string, severity: 'success' | 'error') => {
    setSnackbar({ open: true, message, severity });
  };

  const handleCloseSnackbar = () => {
    setSnackbar({ ...snackbar, open: false });
  };

  const handleOpenDialog = (qualityStandard?: QualityStandard) => {
    if (qualityStandard) {
      setSelectedQualityStandard(qualityStandard);
      setFormData({
        qualityStandardId: qualityStandard.qualityStandardId,
        productId: qualityStandard.productId,
        standardName: qualityStandard.standardName,
        inspectionType: qualityStandard.inspectionType,
        inspectionMethod: qualityStandard.inspectionMethod,
        minValue: qualityStandard.minValue,
        maxValue: qualityStandard.maxValue,
        targetValue: qualityStandard.targetValue,
        toleranceValue: qualityStandard.toleranceValue,
        unit: qualityStandard.unit,
        measurementItem: qualityStandard.measurementItem,
        measurementEquipment: qualityStandard.measurementEquipment,
        samplingMethod: qualityStandard.samplingMethod,
        sampleSize: qualityStandard.sampleSize,
        isActive: qualityStandard.isActive,
        effectiveDate: qualityStandard.effectiveDate,
        expiryDate: qualityStandard.expiryDate,
        remarks: qualityStandard.remarks,
      });
    } else {
      setSelectedQualityStandard(null);
      setFormData({
        productId: products.length > 0 ? products[0].productId : 0,
        standardCode: '',
        standardName: '',
        standardVersion: '1.0',
        inspectionType: 'INCOMING',
        effectiveDate: new Date().toISOString().split('T')[0],
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedQualityStandard(null);
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: ['minValue', 'maxValue', 'targetValue', 'toleranceValue', 'sampleSize'].includes(name) && value
        ? Number(value)
        : value,
    });
  };

  const handleSubmit = async () => {
    try {
      if (selectedQualityStandard) {
        // Update
        await qualityStandardService.updateQualityStandard(selectedQualityStandard.qualityStandardId, formData as QualityStandardUpdateRequest);
        showSnackbar('품질 기준 수정 성공', 'success');
      } else {
        // Create
        await qualityStandardService.createQualityStandard(formData as QualityStandardCreateRequest);
        showSnackbar('품질 기준 생성 성공', 'success');
      }
      handleCloseDialog();
      loadQualityStandards();
    } catch (error: any) {
      showSnackbar(error.response?.data?.message || '작업 실패', 'error');
    }
  };

  const handleToggleActive = async (qualityStandard: QualityStandard) => {
    try {
      if (qualityStandard.isActive) {
        await qualityStandardService.deactivateQualityStandard(qualityStandard.qualityStandardId);
        showSnackbar('품질 기준 비활성화 성공', 'success');
      } else {
        await qualityStandardService.activateQualityStandard(qualityStandard.qualityStandardId);
        showSnackbar('품질 기준 활성화 성공', 'success');
      }
      loadQualityStandards();
    } catch (error: any) {
      showSnackbar(error.response?.data?.message || '상태 변경 실패', 'error');
    }
  };

  const handleOpenDeleteDialog = (qualityStandard: QualityStandard) => {
    setSelectedQualityStandard(qualityStandard);
    setOpenDeleteDialog(true);
  };

  const handleCloseDeleteDialog = () => {
    setOpenDeleteDialog(false);
    setSelectedQualityStandard(null);
  };

  const handleDelete = async () => {
    if (!selectedQualityStandard) return;

    try {
      await qualityStandardService.deleteQualityStandard(selectedQualityStandard.qualityStandardId);
      showSnackbar('품질 기준 삭제 성공', 'success');
      handleCloseDeleteDialog();
      loadQualityStandards();
    } catch (error: any) {
      showSnackbar(error.response?.data?.message || '삭제 실패', 'error');
    }
  };

  const inspectionTypeLabels: Record<string, string> = {
    INCOMING: '입고검사',
    IN_PROCESS: '공정검사',
    OUTGOING: '출하검사',
    FINAL: '최종검사',
  };

  const columns: GridColDef[] = [
    { field: 'standardCode', headerName: '기준 코드', width: 130 },
    { field: 'standardName', headerName: '기준명', flex: 1, minWidth: 200 },
    { field: 'standardVersion', headerName: '버전', width: 80 },
    { field: 'productCode', headerName: '제품 코드', width: 130 },
    { field: 'productName', headerName: '제품명', width: 150 },
    {
      field: 'inspectionType',
      headerName: '검사 유형',
      width: 120,
      renderCell: (params) => (
        <Chip
          label={inspectionTypeLabels[params.value] || params.value}
          color="primary"
          size="small"
          variant="outlined"
        />
      ),
    },
    {
      field: 'minValue',
      headerName: '최소값',
      width: 100,
      valueFormatter: (params) => params.value !== null && params.value !== undefined ? params.value : '-',
    },
    {
      field: 'maxValue',
      headerName: '최대값',
      width: 100,
      valueFormatter: (params) => params.value !== null && params.value !== undefined ? params.value : '-',
    },
    {
      field: 'targetValue',
      headerName: '목표값',
      width: 100,
      valueFormatter: (params) => params.value !== null && params.value !== undefined ? params.value : '-',
    },
    { field: 'unit', headerName: '단위', width: 80 },
    {
      field: 'isActive',
      headerName: '상태',
      width: 100,
      renderCell: (params) => (
        <Chip
          label={params.value ? '활성' : '비활성'}
          color={params.value ? 'success' : 'default'}
          size="small"
        />
      ),
    },
    {
      field: 'actions',
      type: 'actions',
      headerName: '작업',
      width: 150,
      getActions: (params: GridRowParams<QualityStandard>) => [
        <GridActionsCellItem
          icon={<EditIcon />}
          label="수정"
          onClick={() => handleOpenDialog(params.row)}
        />,
        <GridActionsCellItem
          icon={params.row.isActive ? <ToggleOffIcon /> : <ToggleOnIcon />}
          label={params.row.isActive ? '비활성화' : '활성화'}
          onClick={() => handleToggleActive(params.row)}
        />,
        <GridActionsCellItem
          icon={<DeleteIcon />}
          label="삭제"
          onClick={() => handleOpenDeleteDialog(params.row)}
        />,
      ],
    },
  ];

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h5" component="h1">
          품질 기준 관리
        </Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => handleOpenDialog()}
        >
          품질 기준 추가
        </Button>
      </Box>

      <Paper>
        <DataGrid
          rows={qualityStandards}
          columns={columns}
          getRowId={(row) => row.qualityStandardId}
          loading={loading}
          autoHeight
          pageSizeOptions={[10, 25, 50, 100]}
          initialState={{
            pagination: { paginationModel: { pageSize: 25 } },
          }}
          disableRowSelectionOnClick
          sx={{
            '& .MuiDataGrid-cell': {
              borderBottom: '1px solid rgba(224, 224, 224, 1)',
            },
          }}
        />
      </Paper>

      {/* Create/Edit Dialog */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="md" fullWidth>
        <DialogTitle>{selectedQualityStandard ? '품질 기준 수정' : '신규 품질 기준 등록'}</DialogTitle>
        <DialogContent>
          <Box display="flex" flexDirection="column" gap={2} mt={1}>
            <FormControl fullWidth required>
              <InputLabel>제품</InputLabel>
              <Select
                name="productId"
                value={formData.productId || ''}
                onChange={(e) => setFormData({ ...formData, productId: Number(e.target.value) })}
                label="제품"
              >
                {products.map((product) => (
                  <MenuItem key={product.productId} value={product.productId}>
                    {product.productName} ({product.productCode})
                  </MenuItem>
                ))}
              </Select>
            </FormControl>

            {!selectedQualityStandard && (
              <TextField
                name="standardCode"
                label="기준 코드"
                value={(formData as QualityStandardCreateRequest).standardCode || ''}
                onChange={handleInputChange}
                required
                fullWidth
              />
            )}

            <TextField
              name="standardName"
              label="기준명"
              value={formData.standardName || ''}
              onChange={handleInputChange}
              required
              fullWidth
            />

            {!selectedQualityStandard && (
              <TextField
                name="standardVersion"
                label="버전"
                value={(formData as QualityStandardCreateRequest).standardVersion || ''}
                onChange={handleInputChange}
                fullWidth
              />
            )}

            <FormControl fullWidth required>
              <InputLabel>검사 유형</InputLabel>
              <Select
                name="inspectionType"
                value={formData.inspectionType || 'INCOMING'}
                onChange={(e) => setFormData({ ...formData, inspectionType: e.target.value })}
                label="검사 유형"
              >
                <MenuItem value="INCOMING">입고검사</MenuItem>
                <MenuItem value="IN_PROCESS">공정검사</MenuItem>
                <MenuItem value="OUTGOING">출하검사</MenuItem>
                <MenuItem value="FINAL">최종검사</MenuItem>
              </Select>
            </FormControl>

            <TextField
              name="inspectionMethod"
              label="검사 방법"
              value={formData.inspectionMethod || ''}
              onChange={handleInputChange}
              fullWidth
            />

            <Box display="flex" gap={2}>
              <TextField
                name="minValue"
                label="최소값"
                type="number"
                value={formData.minValue || ''}
                onChange={handleInputChange}
                fullWidth
              />
              <TextField
                name="targetValue"
                label="목표값"
                type="number"
                value={formData.targetValue || ''}
                onChange={handleInputChange}
                fullWidth
              />
              <TextField
                name="maxValue"
                label="최대값"
                type="number"
                value={formData.maxValue || ''}
                onChange={handleInputChange}
                fullWidth
              />
            </Box>

            <Box display="flex" gap={2}>
              <TextField
                name="toleranceValue"
                label="허용 오차"
                type="number"
                value={formData.toleranceValue || ''}
                onChange={handleInputChange}
                fullWidth
              />
              <TextField
                name="unit"
                label="단위"
                value={formData.unit || ''}
                onChange={handleInputChange}
                fullWidth
              />
            </Box>

            <TextField
              name="measurementItem"
              label="측정 항목"
              value={formData.measurementItem || ''}
              onChange={handleInputChange}
              fullWidth
            />

            <TextField
              name="measurementEquipment"
              label="측정 장비"
              value={formData.measurementEquipment || ''}
              onChange={handleInputChange}
              fullWidth
            />

            <Box display="flex" gap={2}>
              <TextField
                name="samplingMethod"
                label="샘플링 방법"
                value={formData.samplingMethod || ''}
                onChange={handleInputChange}
                fullWidth
              />
              <TextField
                name="sampleSize"
                label="샘플 크기"
                type="number"
                value={formData.sampleSize || ''}
                onChange={handleInputChange}
                fullWidth
              />
            </Box>

            <Box display="flex" gap={2}>
              <TextField
                name="effectiveDate"
                label="유효 시작일"
                type="date"
                value={formData.effectiveDate || ''}
                onChange={handleInputChange}
                required
                fullWidth
                InputLabelProps={{ shrink: true }}
              />
              <TextField
                name="expiryDate"
                label="만료일"
                type="date"
                value={formData.expiryDate || ''}
                onChange={handleInputChange}
                fullWidth
                InputLabelProps={{ shrink: true }}
              />
            </Box>

            <TextField
              name="remarks"
              label="비고"
              value={formData.remarks || ''}
              onChange={handleInputChange}
              multiline
              rows={3}
              fullWidth
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>취소</Button>
          <Button onClick={handleSubmit} variant="contained">
            {selectedQualityStandard ? '수정' : '등록'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={openDeleteDialog} onClose={handleCloseDeleteDialog}>
        <DialogTitle>품질 기준 삭제 확인</DialogTitle>
        <DialogContent>
          <Alert severity="warning" sx={{ mb: 2 }}>
            이 작업은 되돌릴 수 없습니다.
          </Alert>
          <Typography>
            품질 기준 <strong>{selectedQualityStandard?.standardName}</strong>을(를) 삭제하시겠습니까?
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDeleteDialog}>취소</Button>
          <Button onClick={handleDelete} color="error" variant="contained">
            삭제
          </Button>
        </DialogActions>
      </Dialog>

      {/* Snackbar for notifications */}
      <Snackbar
        open={snackbar.open}
        autoHideDuration={3000}
        onClose={handleCloseSnackbar}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
      >
        <Alert onClose={handleCloseSnackbar} severity={snackbar.severity} sx={{ width: '100%' }}>
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Box>
  );
};

export default QualityStandardsPage;
