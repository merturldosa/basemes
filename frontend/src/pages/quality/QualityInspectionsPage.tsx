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
} from '@mui/icons-material';
import qualityInspectionService, { QualityInspection, QualityInspectionCreateRequest, QualityInspectionUpdateRequest } from '../../services/qualityInspectionService';
import qualityStandardService, { QualityStandard } from '../../services/qualityStandardService';
import productService, { Product } from '../../services/productService';
import workOrderService, { WorkOrder } from '../../services/workOrderService';
import userService, { User } from '../../services/userService';

const QualityInspectionsPage: React.FC = () => {
  const [qualityInspections, setQualityInspections] = useState<QualityInspection[]>([]);
  const [qualityStandards, setQualityStandards] = useState<QualityStandard[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [workOrders, setWorkOrders] = useState<WorkOrder[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [openDialog, setOpenDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [selectedQualityInspection, setSelectedQualityInspection] = useState<QualityInspection | null>(null);
  const [formData, setFormData] = useState<QualityInspectionCreateRequest | QualityInspectionUpdateRequest>({
    qualityStandardId: 0,
    productId: 0,
    inspectionNo: '',
    inspectionDate: new Date().toISOString(),
    inspectionType: 'INCOMING',
    inspectorUserId: 0,
    inspectedQuantity: 0,
    inspectionResult: 'PASS',
  });
  const [snackbar, setSnackbar] = useState<{ open: boolean; message: string; severity: 'success' | 'error' }>({
    open: false,
    message: '',
    severity: 'success',
  });

  useEffect(() => {
    loadQualityInspections();
  }, []);

  const loadQualityInspections = async () => {
    try {
      setLoading(true);
      const [inspectionsData, standardsData, productsData, workOrdersData, usersResponse] = await Promise.all([
        qualityInspectionService.getQualityInspections(),
        qualityStandardService.getActiveQualityStandards(),
        productService.getActiveProducts(),
        workOrderService.getWorkOrders(),
        userService.getUsers(),
      ]);
      setQualityInspections(inspectionsData || []);
      setQualityStandards(standardsData || []);
      setProducts(productsData || []);
      setWorkOrders(workOrdersData || []);
      setUsers(usersResponse?.content || []);
    } catch (error) {
      showSnackbar('품질 검사 목록 조회 실패', 'error');
      setQualityInspections([]);
      setQualityStandards([]);
      setProducts([]);
      setWorkOrders([]);
      setUsers([]);
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

  // Auto-calculate pass/fail quantities based on inspection result
  const calculateQuantities = (result: string, inspectedQty: number) => {
    if (result === 'PASS') {
      return { passedQuantity: inspectedQty, failedQuantity: 0 };
    } else if (result === 'FAIL') {
      return { passedQuantity: 0, failedQuantity: inspectedQty };
    }
    // CONDITIONAL: return current values
    return {};
  };

  const handleOpenDialog = (qualityInspection?: QualityInspection) => {
    if (qualityInspection) {
      setSelectedQualityInspection(qualityInspection);
      setFormData({
        qualityInspectionId: qualityInspection.qualityInspectionId,
        qualityStandardId: qualityInspection.qualityStandardId,
        workOrderId: qualityInspection.workOrderId,
        workResultId: qualityInspection.workResultId,
        productId: qualityInspection.productId,
        inspectionDate: qualityInspection.inspectionDate,
        inspectionType: qualityInspection.inspectionType,
        inspectorUserId: qualityInspection.inspectorUserId,
        inspectedQuantity: qualityInspection.inspectedQuantity,
        passedQuantity: qualityInspection.passedQuantity,
        failedQuantity: qualityInspection.failedQuantity,
        measuredValue: qualityInspection.measuredValue,
        measurementUnit: qualityInspection.measurementUnit,
        inspectionResult: qualityInspection.inspectionResult,
        defectType: qualityInspection.defectType,
        defectReason: qualityInspection.defectReason,
        defectLocation: qualityInspection.defectLocation,
        correctiveAction: qualityInspection.correctiveAction,
        correctiveActionDate: qualityInspection.correctiveActionDate,
        remarks: qualityInspection.remarks,
      });
    } else {
      setSelectedQualityInspection(null);
      setFormData({
        qualityStandardId: qualityStandards.length > 0 ? qualityStandards[0].qualityStandardId : 0,
        productId: products.length > 0 ? products[0].productId : 0,
        inspectionNo: '',
        inspectionDate: new Date().toISOString(),
        inspectionType: 'INCOMING',
        inspectorUserId: users.length > 0 ? users[0].userId : 0,
        inspectedQuantity: 0,
        inspectionResult: 'PASS',
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedQualityInspection(null);
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    const newFormData = {
      ...formData,
      [name]: ['inspectedQuantity', 'passedQuantity', 'failedQuantity', 'measuredValue'].includes(name) && value
        ? Number(value)
        : value,
    };

    // Auto-calculate when inspected quantity changes
    if (name === 'inspectedQuantity') {
      const calculated = calculateQuantities(formData.inspectionResult || 'PASS', Number(value));
      Object.assign(newFormData, calculated);
    }

    setFormData(newFormData);
  };

  const handleResultChange = (result: string) => {
    const calculated = calculateQuantities(result, formData.inspectedQuantity || 0);
    setFormData({
      ...formData,
      inspectionResult: result,
      ...calculated,
    });
  };

  const handleSubmit = async () => {
    try {
      if (selectedQualityInspection) {
        // Update
        await qualityInspectionService.updateQualityInspection(selectedQualityInspection.qualityInspectionId, formData as QualityInspectionUpdateRequest);
        showSnackbar('품질 검사 수정 성공', 'success');
      } else {
        // Create
        await qualityInspectionService.createQualityInspection(formData as QualityInspectionCreateRequest);
        showSnackbar('품질 검사 생성 성공', 'success');
      }
      handleCloseDialog();
      loadQualityInspections();
    } catch (error: any) {
      showSnackbar(error.response?.data?.message || '작업 실패', 'error');
    }
  };

  const handleOpenDeleteDialog = (qualityInspection: QualityInspection) => {
    setSelectedQualityInspection(qualityInspection);
    setOpenDeleteDialog(true);
  };

  const handleCloseDeleteDialog = () => {
    setOpenDeleteDialog(false);
    setSelectedQualityInspection(null);
  };

  const handleDelete = async () => {
    if (!selectedQualityInspection) return;

    try {
      await qualityInspectionService.deleteQualityInspection(selectedQualityInspection.qualityInspectionId);
      showSnackbar('품질 검사 삭제 성공', 'success');
      handleCloseDeleteDialog();
      loadQualityInspections();
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

  const resultLabels: Record<string, string> = {
    PASS: '합격',
    FAIL: '불합격',
    CONDITIONAL: '조건부',
  };

  const columns: GridColDef[] = [
    { field: 'inspectionNo', headerName: '검사 번호', width: 130 },
    {
      field: 'inspectionDate',
      headerName: '검사일시',
      width: 180,
      valueFormatter: (params) => new Date(params.value).toLocaleString('ko-KR'),
    },
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
    { field: 'standardCode', headerName: '기준 코드', width: 130 },
    { field: 'productCode', headerName: '제품 코드', width: 130 },
    { field: 'productName', headerName: '제품명', flex: 1, minWidth: 150 },
    { field: 'inspectorName', headerName: '검사자', width: 100 },
    { field: 'inspectedQuantity', headerName: '검사 수량', width: 100 },
    { field: 'passedQuantity', headerName: '합격 수량', width: 100 },
    { field: 'failedQuantity', headerName: '불량 수량', width: 100 },
    {
      field: 'measuredValue',
      headerName: '측정값',
      width: 100,
      valueFormatter: (params) => params.value !== null && params.value !== undefined ? params.value : '-',
    },
    {
      field: 'inspectionResult',
      headerName: '검사 결과',
      width: 100,
      renderCell: (params) => (
        <Chip
          label={resultLabels[params.value] || params.value}
          color={params.value === 'PASS' ? 'success' : params.value === 'FAIL' ? 'error' : 'warning'}
          size="small"
        />
      ),
    },
    {
      field: 'actions',
      type: 'actions',
      headerName: '작업',
      width: 120,
      getActions: (params: GridRowParams<QualityInspection>) => [
        <GridActionsCellItem
          icon={<EditIcon />}
          label="수정"
          onClick={() => handleOpenDialog(params.row)}
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
          품질 검사 관리
        </Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => handleOpenDialog()}
        >
          품질 검사 추가
        </Button>
      </Box>

      <Paper>
        <DataGrid
          rows={qualityInspections}
          columns={columns}
          getRowId={(row) => row.qualityInspectionId}
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
        <DialogTitle>{selectedQualityInspection ? '품질 검사 수정' : '신규 품질 검사 등록'}</DialogTitle>
        <DialogContent>
          <Box display="flex" flexDirection="column" gap={2} mt={1}>
            <FormControl fullWidth required>
              <InputLabel>품질 기준</InputLabel>
              <Select
                name="qualityStandardId"
                value={formData.qualityStandardId || ''}
                onChange={(e) => setFormData({ ...formData, qualityStandardId: Number(e.target.value) })}
                label="품질 기준"
              >
                {qualityStandards.map((standard) => (
                  <MenuItem key={standard.qualityStandardId} value={standard.qualityStandardId}>
                    {standard.standardName} ({standard.standardCode})
                  </MenuItem>
                ))}
              </Select>
            </FormControl>

            <FormControl fullWidth>
              <InputLabel>작업 지시 (선택사항)</InputLabel>
              <Select
                name="workOrderId"
                value={formData.workOrderId || ''}
                onChange={(e) => setFormData({ ...formData, workOrderId: e.target.value ? Number(e.target.value) : undefined })}
                label="작업 지시 (선택사항)"
              >
                <MenuItem value="">
                  <em>없음</em>
                </MenuItem>
                {workOrders.map((workOrder) => (
                  <MenuItem key={workOrder.workOrderId} value={workOrder.workOrderId}>
                    {workOrder.workOrderNo} - {workOrder.productName}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>

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

            {!selectedQualityInspection && (
              <TextField
                name="inspectionNo"
                label="검사 번호"
                value={(formData as QualityInspectionCreateRequest).inspectionNo || ''}
                onChange={handleInputChange}
                required
                fullWidth
              />
            )}

            <TextField
              name="inspectionDate"
              label="검사 일시"
              type="datetime-local"
              value={formData.inspectionDate ? formData.inspectionDate.slice(0, 16) : ''}
              onChange={handleInputChange}
              required
              fullWidth
              InputLabelProps={{ shrink: true }}
            />

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

            <FormControl fullWidth required>
              <InputLabel>검사자</InputLabel>
              <Select
                name="inspectorUserId"
                value={formData.inspectorUserId || ''}
                onChange={(e) => setFormData({ ...formData, inspectorUserId: Number(e.target.value) })}
                label="검사자"
              >
                {users.map((user) => (
                  <MenuItem key={user.userId} value={user.userId}>
                    {user.fullName} ({user.username})
                  </MenuItem>
                ))}
              </Select>
            </FormControl>

            <TextField
              name="inspectedQuantity"
              label="검사 수량"
              type="number"
              value={formData.inspectedQuantity || ''}
              onChange={handleInputChange}
              required
              fullWidth
            />

            <FormControl fullWidth required>
              <InputLabel>검사 결과</InputLabel>
              <Select
                name="inspectionResult"
                value={formData.inspectionResult || 'PASS'}
                onChange={(e) => handleResultChange(e.target.value)}
                label="검사 결과"
              >
                <MenuItem value="PASS">합격</MenuItem>
                <MenuItem value="FAIL">불합격</MenuItem>
                <MenuItem value="CONDITIONAL">조건부</MenuItem>
              </Select>
            </FormControl>

            {formData.inspectionResult === 'CONDITIONAL' && (
              <Box display="flex" gap={2}>
                <TextField
                  name="passedQuantity"
                  label="합격 수량"
                  type="number"
                  value={formData.passedQuantity || ''}
                  onChange={handleInputChange}
                  fullWidth
                />
                <TextField
                  name="failedQuantity"
                  label="불량 수량"
                  type="number"
                  value={formData.failedQuantity || ''}
                  onChange={handleInputChange}
                  fullWidth
                />
              </Box>
            )}

            <Box display="flex" gap={2}>
              <TextField
                name="measuredValue"
                label="측정값"
                type="number"
                value={formData.measuredValue || ''}
                onChange={handleInputChange}
                fullWidth
              />
              <TextField
                name="measurementUnit"
                label="측정 단위"
                value={formData.measurementUnit || ''}
                onChange={handleInputChange}
                fullWidth
              />
            </Box>

            {formData.inspectionResult !== 'PASS' && (
              <>
                <TextField
                  name="defectType"
                  label="불량 유형"
                  value={formData.defectType || ''}
                  onChange={handleInputChange}
                  fullWidth
                />
                <TextField
                  name="defectReason"
                  label="불량 사유"
                  value={formData.defectReason || ''}
                  onChange={handleInputChange}
                  multiline
                  rows={2}
                  fullWidth
                />
                <TextField
                  name="defectLocation"
                  label="불량 위치"
                  value={formData.defectLocation || ''}
                  onChange={handleInputChange}
                  fullWidth
                />
                <TextField
                  name="correctiveAction"
                  label="시정 조치"
                  value={formData.correctiveAction || ''}
                  onChange={handleInputChange}
                  multiline
                  rows={2}
                  fullWidth
                />
                <TextField
                  name="correctiveActionDate"
                  label="시정 조치 일자"
                  type="date"
                  value={formData.correctiveActionDate || ''}
                  onChange={handleInputChange}
                  fullWidth
                  InputLabelProps={{ shrink: true }}
                />
              </>
            )}

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
            {selectedQualityInspection ? '수정' : '등록'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={openDeleteDialog} onClose={handleCloseDeleteDialog}>
        <DialogTitle>품질 검사 삭제 확인</DialogTitle>
        <DialogContent>
          <Alert severity="warning" sx={{ mb: 2 }}>
            이 작업은 되돌릴 수 없습니다.
          </Alert>
          <Typography>
            품질 검사 <strong>{selectedQualityInspection?.inspectionNo}</strong>을(를) 삭제하시겠습니까?
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

export default QualityInspectionsPage;
