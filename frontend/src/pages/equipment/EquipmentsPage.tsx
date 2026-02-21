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
  CheckCircle as ActiveIcon,
  Cancel as InactiveIcon,
} from '@mui/icons-material';
import equipmentService, { Equipment, EquipmentCreateRequest, EquipmentUpdateRequest } from '../../services/equipmentService';

const EquipmentsPage: React.FC = () => {
  const [equipments, setEquipments] = useState<Equipment[]>([]);
  const [loading, setLoading] = useState(false);
  const [openDialog, setOpenDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [selectedEquipment, setSelectedEquipment] = useState<Equipment | null>(null);
  const [formData, setFormData] = useState<EquipmentCreateRequest>({
    equipmentCode: '',
    equipmentName: '',
    equipmentType: 'MACHINE',
    status: 'OPERATIONAL',
  });
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'success' as 'success' | 'error' | 'warning' | 'info',
  });

  useEffect(() => {
    loadEquipments();
  }, []);

  const loadEquipments = async () => {
    try {
      setLoading(true);
      const data = await equipmentService.getAll();
      setEquipments(data || []);
    } catch (error) {
      setEquipments([]);
      showSnackbar('설비 목록을 불러오는데 실패했습니다.', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleOpenDialog = (equipment?: Equipment) => {
    if (equipment) {
      setSelectedEquipment(equipment);
      setFormData({
        equipmentCode: equipment.equipmentCode,
        equipmentName: equipment.equipmentName,
        equipmentType: equipment.equipmentType,
        equipmentCategory: equipment.equipmentCategory,
        manufacturer: equipment.manufacturer,
        modelName: equipment.modelName,
        serialNo: equipment.serialNo,
        location: equipment.location,
        capacity: equipment.capacity,
        status: equipment.status,
        maintenanceCycleDays: equipment.maintenanceCycleDays,
        standardCycleTime: equipment.standardCycleTime,
        actualOeeTarget: equipment.actualOeeTarget,
        remarks: equipment.remarks,
      });
    } else {
      setSelectedEquipment(null);
      setFormData({
        equipmentCode: '',
        equipmentName: '',
        equipmentType: 'MACHINE',
        status: 'OPERATIONAL',
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedEquipment(null);
  };

  const handleSubmit = async () => {
    try {
      if (selectedEquipment) {
        await equipmentService.update(selectedEquipment.equipmentId, formData as EquipmentUpdateRequest);
        showSnackbar('설비가 수정되었습니다.', 'success');
      } else {
        await equipmentService.create(formData);
        showSnackbar('설비가 등록되었습니다.', 'success');
      }
      handleCloseDialog();
      loadEquipments();
    } catch (error) {
      showSnackbar('설비 저장에 실패했습니다.', 'error');
    }
  };

  const handleDelete = async () => {
    if (selectedEquipment) {
      try {
        await equipmentService.delete(selectedEquipment.equipmentId);
        showSnackbar('설비가 삭제되었습니다.', 'success');
        setOpenDeleteDialog(false);
        setSelectedEquipment(null);
        loadEquipments();
      } catch (error) {
        showSnackbar('설비 삭제에 실패했습니다.', 'error');
      }
    }
  };

  const handleToggleActive = async (equipment: Equipment) => {
    try {
      if (equipment.isActive) {
        await equipmentService.deactivate(equipment.equipmentId);
        showSnackbar('설비가 비활성화되었습니다.', 'success');
      } else {
        await equipmentService.activate(equipment.equipmentId);
        showSnackbar('설비가 활성화되었습니다.', 'success');
      }
      loadEquipments();
    } catch (error) {
      showSnackbar('설비 상태 변경에 실패했습니다.', 'error');
    }
  };

  const showSnackbar = (message: string, severity: 'success' | 'error' | 'warning' | 'info') => {
    setSnackbar({ open: true, message, severity });
  };

  const getStatusChip = (status: string) => {
    const statusConfig: Record<string, { label: string; color: 'success' | 'error' | 'warning' | 'default' | 'info' }> = {
      OPERATIONAL: { label: '가동중', color: 'success' },
      STOPPED: { label: '정지', color: 'default' },
      MAINTENANCE: { label: '점검중', color: 'warning' },
      BREAKDOWN: { label: '고장', color: 'error' },
      RETIRED: { label: '폐기', color: 'default' },
    };
    const config = statusConfig[status] || { label: status, color: 'default' };
    return <Chip label={config.label} color={config.color} size="small" />;
  };

  const columns: GridColDef[] = [
    { field: 'equipmentCode', headerName: '설비 코드', width: 150 },
    { field: 'equipmentName', headerName: '설비명', width: 200 },
    {
      field: 'equipmentType',
      headerName: '설비 유형',
      width: 120,
      renderCell: (params: GridRenderCellParams) => {
        const typeLabels: Record<string, string> = {
          MACHINE: '설비',
          MOLD: '금형',
          TOOL: '공구',
          FACILITY: '시설',
          VEHICLE: '차량',
          OTHER: '기타',
        };
        return typeLabels[params.value] || params.value;
      },
    },
    { field: 'manufacturer', headerName: '제조사', width: 150 },
    { field: 'modelName', headerName: '모델명', width: 150 },
    { field: 'location', headerName: '위치', width: 150 },
    {
      field: 'status',
      headerName: '상태',
      width: 120,
      renderCell: (params: GridRenderCellParams) => getStatusChip(params.value),
    },
    { field: 'nextMaintenanceDate', headerName: '다음 점검일', width: 130 },
    {
      field: 'isActive',
      headerName: '활성',
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
      width: 150,
      sortable: false,
      renderCell: (params: GridRenderCellParams) => (
        <Box>
          <IconButton
            size="small"
            onClick={() => handleOpenDialog(params.row as Equipment)}
            color="primary"
          >
            <EditIcon fontSize="small" />
          </IconButton>
          <IconButton
            size="small"
            onClick={() => handleToggleActive(params.row as Equipment)}
            color={params.row.isActive ? 'warning' : 'success'}
          >
            {params.row.isActive ? <InactiveIcon fontSize="small" /> : <ActiveIcon fontSize="small" />}
          </IconButton>
          <IconButton
            size="small"
            onClick={() => {
              setSelectedEquipment(params.row as Equipment);
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
            설비 관리
          </Typography>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => handleOpenDialog()}
          >
            설비 등록
          </Button>
        </Box>

        <DataGrid
          rows={equipments}
          columns={columns}
          loading={loading}
          getRowId={(row) => row.equipmentId}
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
        <DialogTitle>{selectedEquipment ? '설비 수정' : '설비 등록'}</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
            <TextField
              label="설비 코드"
              value={formData.equipmentCode}
              onChange={(e) => setFormData({ ...formData, equipmentCode: e.target.value })}
              required
              disabled={!!selectedEquipment}
            />
            <TextField
              label="설비명"
              value={formData.equipmentName}
              onChange={(e) => setFormData({ ...formData, equipmentName: e.target.value })}
              required
            />
            <FormControl>
              <InputLabel>설비 유형</InputLabel>
              <Select
                value={formData.equipmentType}
                onChange={(e) => setFormData({ ...formData, equipmentType: e.target.value })}
                label="설비 유형"
                required
              >
                <MenuItem value="MACHINE">설비</MenuItem>
                <MenuItem value="MOLD">금형</MenuItem>
                <MenuItem value="TOOL">공구</MenuItem>
                <MenuItem value="FACILITY">시설</MenuItem>
                <MenuItem value="VEHICLE">차량</MenuItem>
                <MenuItem value="OTHER">기타</MenuItem>
              </Select>
            </FormControl>
            <TextField
              label="분류"
              value={formData.equipmentCategory || ''}
              onChange={(e) => setFormData({ ...formData, equipmentCategory: e.target.value })}
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
              label="일련번호"
              value={formData.serialNo || ''}
              onChange={(e) => setFormData({ ...formData, serialNo: e.target.value })}
            />
            <TextField
              label="위치"
              value={formData.location || ''}
              onChange={(e) => setFormData({ ...formData, location: e.target.value })}
            />
            <TextField
              label="용량"
              value={formData.capacity || ''}
              onChange={(e) => setFormData({ ...formData, capacity: e.target.value })}
            />
            <FormControl>
              <InputLabel>상태</InputLabel>
              <Select
                value={formData.status}
                onChange={(e) => setFormData({ ...formData, status: e.target.value })}
                label="상태"
                required
              >
                <MenuItem value="OPERATIONAL">가동중</MenuItem>
                <MenuItem value="STOPPED">정지</MenuItem>
                <MenuItem value="MAINTENANCE">점검중</MenuItem>
                <MenuItem value="BREAKDOWN">고장</MenuItem>
                <MenuItem value="RETIRED">폐기</MenuItem>
              </Select>
            </FormControl>
            <TextField
              label="점검 주기 (일)"
              type="number"
              value={formData.maintenanceCycleDays || ''}
              onChange={(e) => setFormData({ ...formData, maintenanceCycleDays: parseInt(e.target.value) || undefined })}
            />
            <TextField
              label="표준 사이클 타임 (초)"
              type="number"
              value={formData.standardCycleTime || ''}
              onChange={(e) => setFormData({ ...formData, standardCycleTime: parseFloat(e.target.value) || undefined })}
            />
            <TextField
              label="OEE 목표 (%)"
              type="number"
              value={formData.actualOeeTarget || ''}
              onChange={(e) => setFormData({ ...formData, actualOeeTarget: parseFloat(e.target.value) || undefined })}
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
            {selectedEquipment ? '수정' : '등록'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={openDeleteDialog} onClose={() => setOpenDeleteDialog(false)}>
        <DialogTitle>설비 삭제</DialogTitle>
        <DialogContent>
          <Typography>
            {selectedEquipment?.equipmentName}을(를) 삭제하시겠습니까?
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

export default EquipmentsPage;
