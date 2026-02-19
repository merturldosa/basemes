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
  Build as ReplaceIcon,
} from '@mui/icons-material';
import equipmentPartService, { EquipmentPart, EquipmentPartCreateRequest, EquipmentPartUpdateRequest } from '../../services/equipmentPartService';
import equipmentService, { Equipment } from '../../services/equipmentService';

const EquipmentPartsPage: React.FC = () => {
  const [parts, setParts] = useState<EquipmentPart[]>([]);
  const [equipments, setEquipments] = useState<Equipment[]>([]);
  const [loading, setLoading] = useState(false);
  const [openDialog, setOpenDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [selectedPart, setSelectedPart] = useState<EquipmentPart | null>(null);
  const [formData, setFormData] = useState<EquipmentPartCreateRequest>({
    equipmentId: 0,
    partCode: '',
    partName: '',
    partType: '',
    manufacturer: '',
    modelName: '',
    serialNo: '',
    installationDate: '',
    expectedLifeDays: undefined,
    unitPrice: undefined,
    remarks: '',
  });
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'success' as 'success' | 'error' | 'warning' | 'info',
  });

  useEffect(() => {
    loadParts();
    loadEquipments();
  }, []);

  const loadParts = async () => {
    try {
      setLoading(true);
      const data = await equipmentPartService.getAll();
      setParts(data || []);
    } catch (error) {
      setParts([]);
      showSnackbar('부품을 불러오는데 실패했습니다.', 'error');
    } finally {
      setLoading(false);
    }
  };

  const loadEquipments = async () => {
    try {
      const data = await equipmentService.getActive();
      setEquipments(data || []);
    } catch (error) {
      console.error('Failed to load equipments:', error);
      setEquipments([]);
    }
  };

  const handleOpenDialog = (part?: EquipmentPart) => {
    if (part) {
      setSelectedPart(part);
      setFormData({
        equipmentId: part.equipmentId,
        partCode: part.partCode,
        partName: part.partName,
        partType: part.partType,
        manufacturer: part.manufacturer,
        modelName: part.modelName,
        serialNo: part.serialNo,
        installationDate: part.installationDate,
        expectedLifeDays: part.expectedLifeDays,
        unitPrice: part.unitPrice,
        remarks: part.remarks,
      });
    } else {
      setSelectedPart(null);
      setFormData({
        equipmentId: 0,
        partCode: '',
        partName: '',
        partType: '',
        manufacturer: '',
        modelName: '',
        serialNo: '',
        installationDate: '',
        expectedLifeDays: undefined,
        unitPrice: undefined,
        remarks: '',
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedPart(null);
  };

  const handleSubmit = async () => {
    try {
      if (selectedPart) {
        const updateData: EquipmentPartUpdateRequest = {
          partName: formData.partName,
          partType: formData.partType,
          manufacturer: formData.manufacturer,
          modelName: formData.modelName,
          serialNo: formData.serialNo,
          installationDate: formData.installationDate,
          expectedLifeDays: formData.expectedLifeDays,
          unitPrice: formData.unitPrice,
          remarks: formData.remarks,
        };
        await equipmentPartService.update(selectedPart.partId, updateData);
        showSnackbar('부품이 수정되었습니다.', 'success');
      } else {
        await equipmentPartService.create(formData);
        showSnackbar('부품이 등록되었습니다.', 'success');
      }
      handleCloseDialog();
      loadParts();
    } catch (error) {
      showSnackbar('부품 저장에 실패했습니다.', 'error');
    }
  };

  const handleDelete = async () => {
    if (selectedPart) {
      try {
        await equipmentPartService.delete(selectedPart.partId);
        showSnackbar('부품이 삭제되었습니다.', 'success');
        setOpenDeleteDialog(false);
        setSelectedPart(null);
        loadParts();
      } catch (error) {
        showSnackbar('부품 삭제에 실패했습니다.', 'error');
      }
    }
  };

  const handleRecordReplacement = async (part: EquipmentPart) => {
    try {
      const today = new Date().toISOString().slice(0, 10);
      await equipmentPartService.recordReplacement(part.partId, today);
      showSnackbar('교체 기록이 등록되었습니다.', 'success');
      loadParts();
    } catch (error) {
      showSnackbar('교체 기록 등록에 실패했습니다.', 'error');
    }
  };

  const showSnackbar = (message: string, severity: 'success' | 'error' | 'warning' | 'info') => {
    setSnackbar({ open: true, message, severity });
  };

  const columns: GridColDef[] = [
    { field: 'partCode', headerName: '부품 코드', width: 150 },
    { field: 'partName', headerName: '부품명', width: 200 },
    { field: 'equipmentName', headerName: '설비명', width: 180, valueFormatter: (params) => params.value || '-' },
    { field: 'partType', headerName: '부품 유형', width: 120, valueFormatter: (params) => params.value || '-' },
    { field: 'manufacturer', headerName: '제조사', width: 130, valueFormatter: (params) => params.value || '-' },
    { field: 'installationDate', headerName: '설치일', width: 120, valueFormatter: (params) => params.value || '-' },
    { field: 'replacementDate', headerName: '교체일', width: 120, valueFormatter: (params) => params.value || '-' },
    { field: 'nextReplacementDate', headerName: '다음 교체일', width: 130, valueFormatter: (params) => params.value || '-' },
    { field: 'replacementCount', headerName: '교체 횟수', width: 100, valueFormatter: (params) => params.value ?? 0 },
    {
      field: 'status',
      headerName: '상태',
      width: 100,
      renderCell: (params: GridRenderCellParams) => {
        const statusConfig: Record<string, { label: string; color: 'success' | 'warning' | 'info' | 'error' | 'default' }> = {
          ACTIVE: { label: '사용중', color: 'success' },
          WORN: { label: '마모', color: 'warning' },
          REPLACED: { label: '교체됨', color: 'info' },
          DISPOSED: { label: '폐기', color: 'error' },
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
            onClick={() => handleOpenDialog(params.row as EquipmentPart)}
            color="primary"
          >
            <EditIcon fontSize="small" />
          </IconButton>
          <IconButton
            size="small"
            onClick={() => handleRecordReplacement(params.row as EquipmentPart)}
            color="warning"
            title="교체 기록"
          >
            <ReplaceIcon fontSize="small" />
          </IconButton>
          <IconButton
            size="small"
            onClick={() => {
              setSelectedPart(params.row as EquipmentPart);
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
            설비 부품 관리
          </Typography>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => handleOpenDialog()}
          >
            부품 등록
          </Button>
        </Box>

        <DataGrid
          rows={parts}
          columns={columns}
          loading={loading}
          getRowId={(row) => row.partId}
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
        <DialogTitle>{selectedPart ? '부품 수정' : '부품 등록'}</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
            <FormControl required>
              <InputLabel>설비</InputLabel>
              <Select
                value={formData.equipmentId}
                onChange={(e) => setFormData({ ...formData, equipmentId: e.target.value as number })}
                label="설비"
              >
                {equipments.map((equipment) => (
                  <MenuItem key={equipment.equipmentId} value={equipment.equipmentId}>
                    {equipment.equipmentCode} - {equipment.equipmentName}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
            <TextField
              label="부품 코드"
              value={formData.partCode}
              onChange={(e) => setFormData({ ...formData, partCode: e.target.value })}
              required
              disabled={!!selectedPart}
            />
            <TextField
              label="부품명"
              value={formData.partName}
              onChange={(e) => setFormData({ ...formData, partName: e.target.value })}
              required
            />
            <TextField
              label="부품 유형"
              value={formData.partType || ''}
              onChange={(e) => setFormData({ ...formData, partType: e.target.value })}
            />
            <TextField
              label="제조사"
              value={formData.manufacturer || ''}
              onChange={(e) => setFormData({ ...formData, manufacturer: e.target.value })}
            />
            <TextField
              label="모델명"
              value={formData.modelName || ''}
              onChange={(e) => setFormData({ ...formData, modelName: e.target.value })}
            />
            <TextField
              label="시리얼 번호"
              value={formData.serialNo || ''}
              onChange={(e) => setFormData({ ...formData, serialNo: e.target.value })}
            />
            <TextField
              label="설치일"
              type="date"
              value={formData.installationDate || ''}
              onChange={(e) => setFormData({ ...formData, installationDate: e.target.value })}
              InputLabelProps={{ shrink: true }}
            />
            <TextField
              label="예상 수명 (일)"
              type="number"
              value={formData.expectedLifeDays || ''}
              onChange={(e) => setFormData({ ...formData, expectedLifeDays: parseInt(e.target.value) || undefined })}
            />
            <TextField
              label="단가 (원)"
              type="number"
              value={formData.unitPrice || ''}
              onChange={(e) => setFormData({ ...formData, unitPrice: parseFloat(e.target.value) || undefined })}
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
            {selectedPart ? '수정' : '등록'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={openDeleteDialog} onClose={() => setOpenDeleteDialog(false)}>
        <DialogTitle>부품 삭제</DialogTitle>
        <DialogContent>
          <Typography>
            {selectedPart?.partName}을(를) 삭제하시겠습니까?
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

export default EquipmentPartsPage;
