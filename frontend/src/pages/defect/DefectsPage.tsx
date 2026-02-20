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
import defectService, { Defect, DefectRequest } from '../../services/defectService';
import productService, { Product } from '../../services/productService';

const DefectsPage: React.FC = () => {
  const [defects, setDefects] = useState<Defect[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(false);
  const [openDialog, setOpenDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [selectedDefect, setSelectedDefect] = useState<Defect | null>(null);
  const [viewMode, setViewMode] = useState(false);
  const [snackbar, setSnackbar] = useState<{
    open: boolean;
    message: string;
    severity: 'success' | 'error' | 'info';
  }>({ open: false, message: '', severity: 'success' });

  const [formData, setFormData] = useState<DefectRequest>({
    defectNo: '',
    defectDate: new Date().toISOString().slice(0, 16),
    sourceType: 'PRODUCTION',
    productId: 0,
    defectType: '',
    defectCategory: '',
    defectLocation: '',
    defectDescription: '',
    defectQuantity: 0,
    lotNo: '',
    severity: 'MINOR',
    status: 'REPORTED',
    defectCost: 0,
    remarks: '',
  });

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const [defectsData, productsData] = await Promise.all([
        defectService.getAll(),
        productService.getActiveProducts(),
      ]);
      setDefects(defectsData);
      setProducts(productsData);
    } catch (error) {
      setSnackbar({ open: true, message: 'Failed to load data', severity: 'error' });
    } finally {
      setLoading(false);
    }
  };

  const handleOpenDialog = (defect?: Defect, view = false) => {
    if (defect) {
      setViewMode(view);
      setSelectedDefect(defect);
      setFormData({
        defectNo: defect.defectNo,
        defectDate: defect.defectDate,
        sourceType: defect.sourceType,
        productId: defect.productId,
        defectType: defect.defectType || '',
        defectCategory: defect.defectCategory || '',
        defectLocation: defect.defectLocation || '',
        defectDescription: defect.defectDescription || '',
        defectQuantity: defect.defectQuantity,
        lotNo: defect.lotNo || '',
        severity: defect.severity || 'MINOR',
        status: defect.status,
        defectCost: defect.defectCost,
        remarks: defect.remarks || '',
      });
    } else {
      setViewMode(false);
      setSelectedDefect(null);
      setFormData({
        defectNo: '',
        defectDate: new Date().toISOString().slice(0, 16),
        sourceType: 'PRODUCTION',
        productId: 0,
        defectType: '',
        defectCategory: '',
        defectLocation: '',
        defectDescription: '',
        defectQuantity: 0,
        lotNo: '',
        severity: 'MINOR',
        status: 'REPORTED',
        defectCost: 0,
        remarks: '',
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedDefect(null);
    setViewMode(false);
  };

  const handleOpenDeleteDialog = (defect: Defect) => {
    setSelectedDefect(defect);
    setOpenDeleteDialog(true);
  };

  const handleCloseDeleteDialog = () => {
    setOpenDeleteDialog(false);
    setSelectedDefect(null);
  };

  const handleSubmit = async () => {
    try {
      await defectService.create(formData);
      setSnackbar({ open: true, message: 'Defect created successfully', severity: 'success' });
      handleCloseDialog();
      loadData();
    } catch (error) {
      setSnackbar({ open: true, message: 'Failed to create defect', severity: 'error' });
    }
  };

  const handleDelete = async () => {
    if (!selectedDefect) return;

    try {
      await defectService.delete(selectedDefect.defectId);
      setSnackbar({ open: true, message: 'Defect deleted successfully', severity: 'success' });
      handleCloseDeleteDialog();
      loadData();
    } catch (error) {
      setSnackbar({ open: true, message: 'Failed to delete defect', severity: 'error' });
    }
  };

  const handleClose = async (id: number) => {
    try {
      await defectService.close(id);
      setSnackbar({ open: true, message: 'Defect closed successfully', severity: 'success' });
      loadData();
    } catch (error) {
      setSnackbar({ open: true, message: 'Failed to close defect', severity: 'error' });
    }
  };

  const columns: GridColDef[] = [
    { field: 'defectNo', headerName: '불량번호', width: 150 },
    {
      field: 'defectDate',
      headerName: '불량일자',
      width: 180,
      valueFormatter: (params) => new Date(params.value).toLocaleString('ko-KR'),
    },
    {
      field: 'sourceType',
      headerName: '발생원천',
      width: 120,
      valueFormatter: (params) => {
        const types: { [key: string]: string } = {
          PRODUCTION: '생산',
          RECEIVING: '입하',
          SHIPPING: '출하',
          INSPECTION: '검사',
          CUSTOMER: '고객',
        };
        return types[params.value] || params.value;
      },
    },
    { field: 'productName', headerName: '제품', width: 150 },
    {
      field: 'defectType',
      headerName: '불량유형',
      width: 120,
      valueFormatter: (params) => {
        const types: { [key: string]: string } = {
          APPEARANCE: '외관',
          DIMENSION: '치수',
          FUNCTION: '기능',
          MATERIAL: '재질',
          ASSEMBLY: '조립',
          OTHER: '기타',
        };
        return types[params.value] || params.value;
      },
    },
    { field: 'defectQuantity', headerName: '불량수량', width: 100 },
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
        return (
          <Chip
            label={severityLabels[params.value] || params.value}
            color={severityColors[params.value] || 'default'}
            size="small"
          />
        );
      },
    },
    {
      field: 'status',
      headerName: '상태',
      width: 120,
      renderCell: (params) => {
        const statusColors: { [key: string]: 'default' | 'warning' | 'info' | 'success' | 'error' } = {
          REPORTED: 'warning',
          IN_REVIEW: 'info',
          REWORK: 'info',
          SCRAP: 'error',
          CLOSED: 'success',
        };
        const statusLabels: { [key: string]: string } = {
          REPORTED: '보고',
          IN_REVIEW: '검토중',
          REWORK: '재작업',
          SCRAP: '폐기',
          CLOSED: '종료',
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
      width: 150,
      getActions: (params) => [
        <GridActionsCellItem
          icon={<VisibilityIcon />}
          label="View"
          onClick={() => handleOpenDialog(params.row, true)}
        />,
        <GridActionsCellItem
          icon={<CheckCircleIcon />}
          label="Close"
          onClick={() => handleClose(params.row.defectId)}
          disabled={params.row.status === 'CLOSED'}
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
          <Typography variant="h5">불량 관리</Typography>
          <Button variant="contained" startIcon={<AddIcon />} onClick={() => handleOpenDialog()}>
            신규 불량
          </Button>
        </Box>
      </Paper>

      <Paper sx={{ height: 'calc(100vh - 250px)' }}>
        <DataGrid
          rows={defects}
          columns={columns}
          loading={loading}
          getRowId={(row) => row.defectId}
          pageSizeOptions={[10, 25, 50, 100]}
          initialState={{
            pagination: { paginationModel: { pageSize: 25 } },
          }}
        />
      </Paper>

      {/* Create/View Dialog */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="md" fullWidth>
        <DialogTitle>{viewMode ? '불량 상세' : '신규 불량'}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="불량번호"
                value={formData.defectNo}
                onChange={(e) => setFormData({ ...formData, defectNo: e.target.value })}
                required
                disabled={viewMode}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="불량일자"
                type="datetime-local"
                value={formData.defectDate}
                onChange={(e) => setFormData({ ...formData, defectDate: e.target.value })}
                required
                disabled={viewMode}
                InputLabelProps={{ shrink: true }}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth required>
                <InputLabel>발생원천</InputLabel>
                <Select
                  value={formData.sourceType}
                  onChange={(e) => setFormData({ ...formData, sourceType: e.target.value })}
                  label="발생원천"
                  disabled={viewMode}
                >
                  <MenuItem value="PRODUCTION">생산</MenuItem>
                  <MenuItem value="RECEIVING">입하</MenuItem>
                  <MenuItem value="SHIPPING">출하</MenuItem>
                  <MenuItem value="INSPECTION">검사</MenuItem>
                  <MenuItem value="CUSTOMER">고객</MenuItem>
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
              <FormControl fullWidth>
                <InputLabel>불량유형</InputLabel>
                <Select
                  value={formData.defectType}
                  onChange={(e) => setFormData({ ...formData, defectType: e.target.value })}
                  label="불량유형"
                  disabled={viewMode}
                >
                  <MenuItem value="APPEARANCE">외관</MenuItem>
                  <MenuItem value="DIMENSION">치수</MenuItem>
                  <MenuItem value="FUNCTION">기능</MenuItem>
                  <MenuItem value="MATERIAL">재질</MenuItem>
                  <MenuItem value="ASSEMBLY">조립</MenuItem>
                  <MenuItem value="OTHER">기타</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth>
                <InputLabel>심각도</InputLabel>
                <Select
                  value={formData.severity}
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
              <TextField
                fullWidth
                label="불량수량"
                type="number"
                value={formData.defectQuantity}
                onChange={(e) => setFormData({ ...formData, defectQuantity: parseFloat(e.target.value) })}
                disabled={viewMode}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="LOT 번호"
                value={formData.lotNo}
                onChange={(e) => setFormData({ ...formData, lotNo: e.target.value })}
                disabled={viewMode}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="불량설명"
                multiline
                rows={3}
                value={formData.defectDescription}
                onChange={(e) => setFormData({ ...formData, defectDescription: e.target.value })}
                disabled={viewMode}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="비고"
                multiline
                rows={2}
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
        <DialogTitle>불량 삭제</DialogTitle>
        <DialogContent>
          <Typography>정말 이 불량을 삭제하시겠습니까?</Typography>
          {selectedDefect && (
            <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
              불량번호: {selectedDefect.defectNo}
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

export default DefectsPage;
