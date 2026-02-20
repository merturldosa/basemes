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
  FormControlLabel,
  Checkbox,
} from '@mui/material';
import { DataGrid, GridColDef, GridRenderCellParams } from '@mui/x-data-grid';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  CheckCircle as CheckCircleIcon,
  Cancel as CancelIcon,
} from '@mui/icons-material';
import materialService, { Material, MaterialCreateRequest, MaterialUpdateRequest } from '../../services/materialService';
import supplierService, { Supplier } from '../../services/supplierService';

const MaterialsPage: React.FC = () => {
  const [materials, setMaterials] = useState<Material[]>([]);
  const [suppliers, setSuppliers] = useState<Supplier[]>([]);
  const [loading, setLoading] = useState(false);
  const [openDialog, setOpenDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [selectedMaterial, setSelectedMaterial] = useState<Material | null>(null);
  const [formData, setFormData] = useState<Partial<MaterialCreateRequest>>({
    materialType: 'RAW_MATERIAL',
    unit: 'EA',
    currency: 'KRW',
    lotManaged: false,
    isActive: true,
  });
  const [snackbar, setSnackbar] = useState<{ open: boolean; message: string; severity: 'success' | 'error' }>(
{
      open: false,
      message: '',
      severity: 'success',
    }
  );

  useEffect(() => {
    loadMaterials();
    loadSuppliers();
  }, []);

  const loadMaterials = async () => {
    try {
      setLoading(true);
      const data = await materialService.getAll();
      setMaterials(data || []);
    } catch (error) {
      setSnackbar({ open: true, message: '자재 목록 조회 실패', severity: 'error' });
      setMaterials([]);
    } finally {
      setLoading(false);
    }
  };

  const loadSuppliers = async () => {
    try {
      const data = await supplierService.getActive();
      setSuppliers(data || []);
    } catch (error) {
      setSuppliers([]);
    }
  };

  const handleOpenDialog = (material?: Material) => {
    if (material) {
      setSelectedMaterial(material);
      setFormData({
        materialCode: material.materialCode,
        materialName: material.materialName,
        materialType: material.materialType,
        specification: material.specification,
        model: material.model,
        unit: material.unit,
        standardPrice: material.standardPrice,
        currentPrice: material.currentPrice,
        currency: material.currency,
        supplierId: material.supplierId,
        leadTimeDays: material.leadTimeDays,
        minStockQuantity: material.minStockQuantity,
        maxStockQuantity: material.maxStockQuantity,
        safetyStockQuantity: material.safetyStockQuantity,
        reorderPoint: material.reorderPoint,
        storageLocation: material.storageLocation,
        lotManaged: material.lotManaged,
        shelfLifeDays: material.shelfLifeDays,
        isActive: material.isActive,
        remarks: material.remarks,
      });
    } else {
      setSelectedMaterial(null);
      setFormData({
        materialType: 'RAW_MATERIAL',
        unit: 'EA',
        currency: 'KRW',
        lotManaged: false,
        isActive: true,
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedMaterial(null);
    setFormData({
      materialType: 'RAW_MATERIAL',
      unit: 'EA',
      currency: 'KRW',
      lotManaged: false,
      isActive: true,
    });
  };

  const handleSubmit = async () => {
    try {
      if (selectedMaterial) {
        const updateRequest: MaterialUpdateRequest = {
          materialId: selectedMaterial.materialId,
          materialName: formData.materialName!,
          materialType: formData.materialType!,
          specification: formData.specification,
          model: formData.model,
          unit: formData.unit!,
          standardPrice: formData.standardPrice,
          currentPrice: formData.currentPrice,
          currency: formData.currency,
          supplierId: formData.supplierId,
          leadTimeDays: formData.leadTimeDays,
          minStockQuantity: formData.minStockQuantity,
          maxStockQuantity: formData.maxStockQuantity,
          safetyStockQuantity: formData.safetyStockQuantity,
          reorderPoint: formData.reorderPoint,
          storageLocation: formData.storageLocation,
          lotManaged: formData.lotManaged!,
          shelfLifeDays: formData.shelfLifeDays,
          isActive: formData.isActive!,
          remarks: formData.remarks,
        };
        await materialService.update(selectedMaterial.materialId, updateRequest);
        setSnackbar({ open: true, message: '자재가 수정되었습니다', severity: 'success' });
      } else {
        await materialService.create(formData as MaterialCreateRequest);
        setSnackbar({ open: true, message: '자재가 생성되었습니다', severity: 'success' });
      }
      handleCloseDialog();
      loadMaterials();
    } catch (error) {
      setSnackbar({ open: true, message: '자재 저장 실패', severity: 'error' });
    }
  };

  const handleDelete = async () => {
    if (!selectedMaterial) return;

    try {
      await materialService.delete(selectedMaterial.materialId);
      setSnackbar({ open: true, message: '자재가 삭제되었습니다', severity: 'success' });
      setOpenDeleteDialog(false);
      setSelectedMaterial(null);
      loadMaterials();
    } catch (error) {
      setSnackbar({ open: true, message: '자재 삭제 실패', severity: 'error' });
    }
  };

  const handleToggleActive = async (material: Material) => {
    try {
      await materialService.toggleActive(material.materialId);
      setSnackbar({
        open: true,
        message: material.isActive ? '자재가 비활성화되었습니다' : '자재가 활성화되었습니다',
        severity: 'success',
      });
      loadMaterials();
    } catch (error) {
      setSnackbar({ open: true, message: '자재 상태 변경 실패', severity: 'error' });
    }
  };

  const getMaterialTypeLabel = (type: string) => {
    const types: { [key: string]: string } = {
      RAW_MATERIAL: '원자재',
      SUB_MATERIAL: '부자재',
      SEMI_FINISHED: '반제품',
      FINISHED_PRODUCT: '완제품',
    };
    return types[type] || type;
  };

  const columns: GridColDef[] = [
    { field: 'materialCode', headerName: '자재 코드', width: 120 },
    { field: 'materialName', headerName: '자재명', width: 200 },
    {
      field: 'materialType',
      headerName: '유형',
      width: 100,
      renderCell: (params: GridRenderCellParams) => getMaterialTypeLabel(params.value as string),
    },
    { field: 'specification', headerName: '규격', width: 150 },
    { field: 'unit', headerName: '단위', width: 80 },
    {
      field: 'currentPrice',
      headerName: '현재 단가',
      width: 120,
      renderCell: (params: GridRenderCellParams) =>
        params.value ? `${Number(params.value).toLocaleString()} ${params.row.currency || ''}` : '-'
    },
    { field: 'supplierName', headerName: '공급업체', width: 150 },
    {
      field: 'lotManaged',
      headerName: 'LOT관리',
      width: 100,
      renderCell: (params: GridRenderCellParams) => (
        <Chip
          label={params.value ? 'Y' : 'N'}
          color={params.value ? 'primary' : 'default'}
          size="small"
        />
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
          <IconButton size="small" onClick={() => handleOpenDialog(params.row as Material)}>
            <EditIcon fontSize="small" />
          </IconButton>
          <IconButton
            size="small"
            onClick={() => handleToggleActive(params.row as Material)}
            color={params.row.isActive ? 'default' : 'success'}
          >
            {params.row.isActive ? <CancelIcon fontSize="small" /> : <CheckCircleIcon fontSize="small" />}
          </IconButton>
          <IconButton
            size="small"
            onClick={() => {
              setSelectedMaterial(params.row as Material);
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
        <h2>자재 관리</h2>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => handleOpenDialog()}>
          자재 생성
        </Button>
      </Box>

      <DataGrid
        rows={materials}
        columns={columns}
        loading={loading}
        getRowId={(row) => row.materialId}
        pageSizeOptions={[10, 25, 50, 100]}
        initialState={{
          pagination: { paginationModel: { pageSize: 25 } },
        }}
        sx={{ height: 600 }}
      />

      {/* Create/Edit Dialog */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="md" fullWidth>
        <DialogTitle>{selectedMaterial ? '자재 수정' : '자재 생성'}</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 2 }}>
            <Grid container spacing={2}>
              <Grid item xs={6}>
                <TextField
                  label="자재 코드"
                  value={formData.materialCode || ''}
                  onChange={(e) => setFormData({ ...formData, materialCode: e.target.value })}
                  disabled={!!selectedMaterial}
                  required
                  fullWidth
                />
              </Grid>
              <Grid item xs={6}>
                <TextField
                  label="자재명"
                  value={formData.materialName || ''}
                  onChange={(e) => setFormData({ ...formData, materialName: e.target.value })}
                  required
                  fullWidth
                />
              </Grid>
              <Grid item xs={6}>
                <FormControl fullWidth required>
                  <InputLabel>자재 유형</InputLabel>
                  <Select
                    value={formData.materialType || 'RAW_MATERIAL'}
                    onChange={(e) => setFormData({ ...formData, materialType: e.target.value })}
                    label="자재 유형"
                  >
                    <MenuItem value="RAW_MATERIAL">원자재</MenuItem>
                    <MenuItem value="SUB_MATERIAL">부자재</MenuItem>
                    <MenuItem value="SEMI_FINISHED">반제품</MenuItem>
                    <MenuItem value="FINISHED_PRODUCT">완제품</MenuItem>
                  </Select>
                </FormControl>
              </Grid>
              <Grid item xs={6}>
                <FormControl fullWidth>
                  <InputLabel>공급업체</InputLabel>
                  <Select
                    value={formData.supplierId || ''}
                    onChange={(e) => setFormData({ ...formData, supplierId: e.target.value as number })}
                    label="공급업체"
                  >
                    <MenuItem value="">선택 안함</MenuItem>
                    {suppliers.map((supplier) => (
                      <MenuItem key={supplier.supplierId} value={supplier.supplierId}>
                        {supplier.supplierName} ({supplier.supplierCode})
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Grid>
              <Grid item xs={6}>
                <TextField
                  label="규격"
                  value={formData.specification || ''}
                  onChange={(e) => setFormData({ ...formData, specification: e.target.value })}
                  fullWidth
                />
              </Grid>
              <Grid item xs={6}>
                <TextField
                  label="모델"
                  value={formData.model || ''}
                  onChange={(e) => setFormData({ ...formData, model: e.target.value })}
                  fullWidth
                />
              </Grid>
              <Grid item xs={4}>
                <FormControl fullWidth required>
                  <InputLabel>단위</InputLabel>
                  <Select
                    value={formData.unit || 'EA'}
                    onChange={(e) => setFormData({ ...formData, unit: e.target.value })}
                    label="단위"
                  >
                    <MenuItem value="EA">EA</MenuItem>
                    <MenuItem value="KG">KG</MenuItem>
                    <MenuItem value="L">L</MenuItem>
                    <MenuItem value="M">M</MenuItem>
                    <MenuItem value="BOX">BOX</MenuItem>
                    <MenuItem value="SET">SET</MenuItem>
                  </Select>
                </FormControl>
              </Grid>
              <Grid item xs={4}>
                <TextField
                  label="표준 단가"
                  type="number"
                  value={formData.standardPrice || ''}
                  onChange={(e) => setFormData({ ...formData, standardPrice: Number(e.target.value) })}
                  fullWidth
                />
              </Grid>
              <Grid item xs={4}>
                <TextField
                  label="현재 단가"
                  type="number"
                  value={formData.currentPrice || ''}
                  onChange={(e) => setFormData({ ...formData, currentPrice: Number(e.target.value) })}
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
              <Grid item xs={4}>
                <TextField
                  label="리드타임 (일)"
                  type="number"
                  value={formData.leadTimeDays || ''}
                  onChange={(e) => setFormData({ ...formData, leadTimeDays: Number(e.target.value) })}
                  fullWidth
                />
              </Grid>
              <Grid item xs={4}>
                <TextField
                  label="보관 위치"
                  value={formData.storageLocation || ''}
                  onChange={(e) => setFormData({ ...formData, storageLocation: e.target.value })}
                  fullWidth
                />
              </Grid>
              <Grid item xs={3}>
                <TextField
                  label="최소 재고량"
                  type="number"
                  value={formData.minStockQuantity || ''}
                  onChange={(e) => setFormData({ ...formData, minStockQuantity: Number(e.target.value) })}
                  fullWidth
                />
              </Grid>
              <Grid item xs={3}>
                <TextField
                  label="최대 재고량"
                  type="number"
                  value={formData.maxStockQuantity || ''}
                  onChange={(e) => setFormData({ ...formData, maxStockQuantity: Number(e.target.value) })}
                  fullWidth
                />
              </Grid>
              <Grid item xs={3}>
                <TextField
                  label="안전 재고량"
                  type="number"
                  value={formData.safetyStockQuantity || ''}
                  onChange={(e) => setFormData({ ...formData, safetyStockQuantity: Number(e.target.value) })}
                  fullWidth
                />
              </Grid>
              <Grid item xs={3}>
                <TextField
                  label="재주문점"
                  type="number"
                  value={formData.reorderPoint || ''}
                  onChange={(e) => setFormData({ ...formData, reorderPoint: Number(e.target.value) })}
                  fullWidth
                />
              </Grid>
              <Grid item xs={6}>
                <FormControlLabel
                  control={
                    <Checkbox
                      checked={formData.lotManaged || false}
                      onChange={(e) => setFormData({ ...formData, lotManaged: e.target.checked })}
                    />
                  }
                  label="LOT 관리"
                />
              </Grid>
              <Grid item xs={6}>
                <TextField
                  label="유통기한 (일)"
                  type="number"
                  value={formData.shelfLifeDays || ''}
                  onChange={(e) => setFormData({ ...formData, shelfLifeDays: Number(e.target.value) })}
                  fullWidth
                  disabled={!formData.lotManaged}
                />
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
            {selectedMaterial ? '수정' : '생성'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Dialog */}
      <Dialog open={openDeleteDialog} onClose={() => setOpenDeleteDialog(false)}>
        <DialogTitle>자재 삭제</DialogTitle>
        <DialogContent>정말로 이 자재를 삭제하시겠습니까?</DialogContent>
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

export default MaterialsPage;
