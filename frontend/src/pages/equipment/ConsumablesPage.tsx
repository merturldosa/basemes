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
  IconButton,
  MenuItem,
  Select,
  FormControl,
  InputLabel,
  Snackbar,
  Alert,
} from '@mui/material';
import { DataGrid, GridColDef, GridRenderCellParams } from '@mui/x-data-grid';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  SwapVert as AdjustIcon,
} from '@mui/icons-material';
import consumableService, { Consumable, ConsumableCreateRequest, ConsumableUpdateRequest } from '../../services/consumableService';
import equipmentService, { Equipment } from '../../services/equipmentService';

const ConsumablesPage: React.FC = () => {
  const [consumables, setConsumables] = useState<Consumable[]>([]);
  const [equipments, setEquipments] = useState<Equipment[]>([]);
  const [loading, setLoading] = useState(false);
  const [openDialog, setOpenDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [openAdjustDialog, setOpenAdjustDialog] = useState(false);
  const [selectedConsumable, setSelectedConsumable] = useState<Consumable | null>(null);
  const [adjustQuantity, setAdjustQuantity] = useState<number>(0);
  const [formData, setFormData] = useState<ConsumableCreateRequest>({
    consumableCode: '',
    consumableName: '',
    category: '',
    equipmentId: undefined,
    unit: '',
    currentStock: 0,
    minimumStock: 0,
    maximumStock: 0,
    unitPrice: 0,
    supplier: '',
    leadTimeDays: undefined,
    remarks: '',
  });
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'success' as 'success' | 'error' | 'warning' | 'info',
  });

  useEffect(() => {
    loadConsumables();
    loadEquipments();
  }, []);

  const loadConsumables = async () => {
    try {
      setLoading(true);
      const data = await consumableService.getAll();
      setConsumables(data || []);
    } catch (error) {
      setConsumables([]);
      showSnackbar('소모품을 불러오는데 실패했습니다.', 'error');
    } finally {
      setLoading(false);
    }
  };

  const loadEquipments = async () => {
    try {
      const data = await equipmentService.getActive();
      setEquipments(data || []);
    } catch (error) {
      setEquipments([]);
    }
  };

  const handleOpenDialog = (consumable?: Consumable) => {
    if (consumable) {
      setSelectedConsumable(consumable);
      setFormData({
        consumableCode: consumable.consumableCode,
        consumableName: consumable.consumableName,
        category: consumable.category,
        equipmentId: consumable.equipmentId,
        unit: consumable.unit,
        currentStock: consumable.currentStock,
        minimumStock: consumable.minimumStock,
        maximumStock: consumable.maximumStock,
        unitPrice: consumable.unitPrice,
        supplier: consumable.supplier,
        leadTimeDays: consumable.leadTimeDays,
        remarks: consumable.remarks,
      });
    } else {
      setSelectedConsumable(null);
      setFormData({
        consumableCode: '',
        consumableName: '',
        category: '',
        equipmentId: undefined,
        unit: '',
        currentStock: 0,
        minimumStock: 0,
        maximumStock: 0,
        unitPrice: 0,
        supplier: '',
        leadTimeDays: undefined,
        remarks: '',
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedConsumable(null);
  };

  const handleSubmit = async () => {
    try {
      if (selectedConsumable) {
        const updateData: ConsumableUpdateRequest = {
          consumableName: formData.consumableName,
          category: formData.category,
          equipmentId: formData.equipmentId,
          unit: formData.unit,
          currentStock: formData.currentStock,
          minimumStock: formData.minimumStock,
          maximumStock: formData.maximumStock,
          unitPrice: formData.unitPrice,
          supplier: formData.supplier,
          leadTimeDays: formData.leadTimeDays,
          remarks: formData.remarks,
        };
        await consumableService.update(selectedConsumable.consumableId, updateData);
        showSnackbar('소모품이 수정되었습니다.', 'success');
      } else {
        await consumableService.create(formData);
        showSnackbar('소모품이 등록되었습니다.', 'success');
      }
      handleCloseDialog();
      loadConsumables();
    } catch (error) {
      showSnackbar('소모품 저장에 실패했습니다.', 'error');
    }
  };

  const handleDelete = async () => {
    if (selectedConsumable) {
      try {
        await consumableService.delete(selectedConsumable.consumableId);
        showSnackbar('소모품이 삭제되었습니다.', 'success');
        setOpenDeleteDialog(false);
        setSelectedConsumable(null);
        loadConsumables();
      } catch (error) {
        showSnackbar('소모품 삭제에 실패했습니다.', 'error');
      }
    }
  };

  const handleOpenAdjustDialog = (consumable: Consumable) => {
    setSelectedConsumable(consumable);
    setAdjustQuantity(0);
    setOpenAdjustDialog(true);
  };

  const handleAdjustStock = async () => {
    if (selectedConsumable && adjustQuantity !== 0) {
      try {
        await consumableService.adjustStock(selectedConsumable.consumableId, adjustQuantity);
        showSnackbar(
          adjustQuantity > 0
            ? `${adjustQuantity}개 입고 처리되었습니다.`
            : `${Math.abs(adjustQuantity)}개 출고 처리되었습니다.`,
          'success'
        );
        setOpenAdjustDialog(false);
        setSelectedConsumable(null);
        loadConsumables();
      } catch (error) {
        showSnackbar('재고 조정에 실패했습니다.', 'error');
      }
    }
  };

  const showSnackbar = (message: string, severity: 'success' | 'error' | 'warning' | 'info') => {
    setSnackbar({ open: true, message, severity });
  };

  const columns: GridColDef[] = [
    { field: 'consumableCode', headerName: '소모품 코드', width: 150 },
    { field: 'consumableName', headerName: '소모품명', width: 200 },
    { field: 'category', headerName: '분류', width: 120, valueFormatter: (params) => params.value || '-' },
    { field: 'equipmentName', headerName: '설비명', width: 180, valueFormatter: (params) => params.value || '-' },
    { field: 'unit', headerName: '단위', width: 80, valueFormatter: (params) => params.value || '-' },
    {
      field: 'currentStock',
      headerName: '현재 재고',
      width: 120,
      renderCell: (params: GridRenderCellParams) => {
        const currentStock = params.value ?? 0;
        const minimumStock = params.row.minimumStock ?? 0;
        const isLow = currentStock <= minimumStock;
        return (
          <Typography
            variant="body2"
            sx={{ color: isLow ? 'error.main' : 'inherit', fontWeight: isLow ? 'bold' : 'normal' }}
          >
            {currentStock}
          </Typography>
        );
      },
    },
    { field: 'minimumStock', headerName: '최소 재고', width: 100, valueFormatter: (params) => params.value ?? '-' },
    {
      field: 'unitPrice',
      headerName: '단가',
      width: 120,
      valueFormatter: (params) => params.value ? `\u20A9${params.value.toLocaleString()}` : '-',
    },
    { field: 'supplier', headerName: '공급업체', width: 150, valueFormatter: (params) => params.value || '-' },
    {
      field: 'status',
      headerName: '상태',
      width: 100,
      renderCell: (params: GridRenderCellParams) => {
        const statusConfig: Record<string, { label: string; color: 'success' | 'error' | 'warning' | 'default' }> = {
          ACTIVE: { label: '사용중', color: 'success' },
          INACTIVE: { label: '미사용', color: 'default' },
          DISCONTINUED: { label: '단종', color: 'error' },
        };
        const config = statusConfig[params.value] || { label: params.value, color: 'default' };
        return <Chip label={config.label} color={config.color} size="small" />;
      },
    },
    {
      field: 'actions',
      headerName: '작업',
      width: 150,
      sortable: false,
      renderCell: (params: GridRenderCellParams) => (
        <Box>
          <IconButton
            size="small"
            onClick={() => handleOpenDialog(params.row as Consumable)}
            color="primary"
          >
            <EditIcon fontSize="small" />
          </IconButton>
          <IconButton
            size="small"
            onClick={() => handleOpenAdjustDialog(params.row as Consumable)}
            color="info"
            title="재고 조정"
          >
            <AdjustIcon fontSize="small" />
          </IconButton>
          <IconButton
            size="small"
            onClick={() => {
              setSelectedConsumable(params.row as Consumable);
              setOpenDeleteDialog(true);
            }}
            color="error"
          >
            <DeleteIcon fontSize="small" />
          </IconButton>
        </Box>
      ),
    },
  ];

  return (
    <Box sx={{ p: 3 }}>
      <Paper sx={{ p: 2, mb: 2 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="h5" component="h1">
            소모품 관리
          </Typography>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => handleOpenDialog()}
          >
            소모품 등록
          </Button>
        </Box>

        <DataGrid
          rows={consumables}
          columns={columns}
          loading={loading}
          getRowId={(row) => row.consumableId}
          pageSizeOptions={[10, 25, 50, 100]}
          initialState={{
            pagination: { paginationModel: { pageSize: 25 } },
          }}
          autoHeight
          disableRowSelectionOnClick
        />
      </Paper>

      {/* Create/Edit Dialog */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="md" fullWidth>
        <DialogTitle>{selectedConsumable ? '소모품 수정' : '소모품 등록'}</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
            <TextField
              label="소모품 코드"
              value={formData.consumableCode}
              onChange={(e) => setFormData({ ...formData, consumableCode: e.target.value })}
              required
              disabled={!!selectedConsumable}
            />
            <TextField
              label="소모품명"
              value={formData.consumableName}
              onChange={(e) => setFormData({ ...formData, consumableName: e.target.value })}
              required
            />
            <TextField
              label="분류"
              value={formData.category || ''}
              onChange={(e) => setFormData({ ...formData, category: e.target.value })}
            />
            <FormControl>
              <InputLabel>설비</InputLabel>
              <Select
                value={formData.equipmentId || ''}
                onChange={(e) => setFormData({ ...formData, equipmentId: e.target.value as number || undefined })}
                label="설비"
              >
                <MenuItem value="">선택 안함</MenuItem>
                {equipments.map((equipment) => (
                  <MenuItem key={equipment.equipmentId} value={equipment.equipmentId}>
                    {equipment.equipmentCode} - {equipment.equipmentName}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
            <TextField
              label="단위"
              value={formData.unit || ''}
              onChange={(e) => setFormData({ ...formData, unit: e.target.value })}
            />
            <TextField
              label="현재 재고"
              type="number"
              value={formData.currentStock ?? ''}
              onChange={(e) => setFormData({ ...formData, currentStock: parseInt(e.target.value) || 0 })}
            />
            <TextField
              label="최소 재고"
              type="number"
              value={formData.minimumStock ?? ''}
              onChange={(e) => setFormData({ ...formData, minimumStock: parseInt(e.target.value) || 0 })}
            />
            <TextField
              label="최대 재고"
              type="number"
              value={formData.maximumStock ?? ''}
              onChange={(e) => setFormData({ ...formData, maximumStock: parseInt(e.target.value) || 0 })}
            />
            <TextField
              label="단가 (원)"
              type="number"
              value={formData.unitPrice ?? ''}
              onChange={(e) => setFormData({ ...formData, unitPrice: parseFloat(e.target.value) || 0 })}
            />
            <TextField
              label="공급업체"
              value={formData.supplier || ''}
              onChange={(e) => setFormData({ ...formData, supplier: e.target.value })}
            />
            <TextField
              label="리드타임 (일)"
              type="number"
              value={formData.leadTimeDays || ''}
              onChange={(e) => setFormData({ ...formData, leadTimeDays: parseInt(e.target.value) || undefined })}
            />
            <TextField
              label="비고"
              value={formData.remarks || ''}
              onChange={(e) => setFormData({ ...formData, remarks: e.target.value })}
              multiline
              rows={3}
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>취소</Button>
          <Button onClick={handleSubmit} variant="contained">
            {selectedConsumable ? '수정' : '등록'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Stock Adjust Dialog */}
      <Dialog open={openAdjustDialog} onClose={() => setOpenAdjustDialog(false)} maxWidth="xs" fullWidth>
        <DialogTitle>재고 조정</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
            <Typography variant="body2" color="text.secondary">
              {selectedConsumable?.consumableName} (현재 재고: {selectedConsumable?.currentStock ?? 0})
            </Typography>
            <TextField
              label="조정 수량"
              type="number"
              value={adjustQuantity}
              onChange={(e) => setAdjustQuantity(parseInt(e.target.value) || 0)}
              helperText="양수: 입고, 음수: 출고"
              fullWidth
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenAdjustDialog(false)}>취소</Button>
          <Button onClick={handleAdjustStock} variant="contained" disabled={adjustQuantity === 0}>
            조정
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={openDeleteDialog} onClose={() => setOpenDeleteDialog(false)}>
        <DialogTitle>소모품 삭제</DialogTitle>
        <DialogContent>
          <Typography>
            {selectedConsumable?.consumableName}을(를) 삭제하시겠습니까?
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenDeleteDialog(false)}>취소</Button>
          <Button onClick={handleDelete} color="error" variant="contained">
            삭제
          </Button>
        </DialogActions>
      </Dialog>

      {/* Snackbar */}
      <Snackbar
        open={snackbar.open}
        autoHideDuration={3000}
        onClose={() => setSnackbar({ ...snackbar, open: false })}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert severity={snackbar.severity} sx={{ width: '100%' }}>
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Box>
  );
};

export default ConsumablesPage;
