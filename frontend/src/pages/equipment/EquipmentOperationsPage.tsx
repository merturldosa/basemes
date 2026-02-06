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
  CheckCircle as CompleteIcon,
} from '@mui/icons-material';
import equipmentOperationService, { EquipmentOperation, EquipmentOperationCreateRequest, EquipmentOperationUpdateRequest } from '../../services/equipmentOperationService';
import equipmentService, { Equipment } from '../../services/equipmentService';

const EquipmentOperationsPage: React.FC = () => {
  const [operations, setOperations] = useState<EquipmentOperation[]>([]);
  const [equipments, setEquipments] = useState<Equipment[]>([]);
  const [loading, setLoading] = useState(false);
  const [openDialog, setOpenDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [selectedOperation, setSelectedOperation] = useState<EquipmentOperation | null>(null);
  const [formData, setFormData] = useState<EquipmentOperationCreateRequest>({
    equipmentId: 0,
    operationDate: new Date().toISOString().split('T')[0],
    startTime: new Date().toISOString().slice(0, 16),
  });
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'success' as 'success' | 'error' | 'warning' | 'info',
  });

  useEffect(() => {
    loadOperations();
    loadEquipments();
  }, []);

  const loadOperations = async () => {
    try {
      setLoading(true);
      const data = await equipmentOperationService.getAll();
      setOperations(data || []);
    } catch (error) {
      setOperations([]);
      showSnackbar('가동 이력을 불러오는데 실패했습니다.', 'error');
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

  const handleOpenDialog = (operation?: EquipmentOperation) => {
    if (operation) {
      setSelectedOperation(operation);
      setFormData({
        equipmentId: operation.equipmentId,
        operationDate: operation.operationDate,
        startTime: operation.startTime,
        endTime: operation.endTime,
        productionQuantity: operation.productionQuantity,
        goodQuantity: operation.goodQuantity,
        defectQuantity: operation.defectQuantity,
        operationStatus: operation.operationStatus,
        stopReason: operation.stopReason,
        stopDurationMinutes: operation.stopDurationMinutes,
        cycleTime: operation.cycleTime,
        remarks: operation.remarks,
      });
    } else {
      setSelectedOperation(null);
      setFormData({
        equipmentId: 0,
        operationDate: new Date().toISOString().split('T')[0],
        startTime: new Date().toISOString().slice(0, 16),
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedOperation(null);
  };

  const handleSubmit = async () => {
    try {
      if (selectedOperation) {
        await equipmentOperationService.update(selectedOperation.operationId, formData as EquipmentOperationUpdateRequest);
        showSnackbar('가동 이력이 수정되었습니다.', 'success');
      } else {
        await equipmentOperationService.create(formData);
        showSnackbar('가동 이력이 등록되었습니다.', 'success');
      }
      handleCloseDialog();
      loadOperations();
    } catch (error) {
      showSnackbar('가동 이력 저장에 실패했습니다.', 'error');
    }
  };

  const handleComplete = async (operation: EquipmentOperation) => {
    try {
      await equipmentOperationService.complete(operation.operationId);
      showSnackbar('가동이 완료되었습니다. OEE가 계산되었습니다.', 'success');
      loadOperations();
    } catch (error) {
      showSnackbar('가동 완료 처리에 실패했습니다.', 'error');
    }
  };

  const handleDelete = async () => {
    if (selectedOperation) {
      try {
        await equipmentOperationService.delete(selectedOperation.operationId);
        showSnackbar('가동 이력이 삭제되었습니다.', 'success');
        setOpenDeleteDialog(false);
        setSelectedOperation(null);
        loadOperations();
      } catch (error) {
        showSnackbar('가동 이력 삭제에 실패했습니다.', 'error');
      }
    }
  };

  const showSnackbar = (message: string, severity: 'success' | 'error' | 'warning' | 'info') => {
    setSnackbar({ open: true, message, severity });
  };

  const getStatusChip = (status: string) => {
    const statusConfig: Record<string, { label: string; color: 'success' | 'error' | 'warning' | 'default' | 'info' }> = {
      RUNNING: { label: '가동중', color: 'success' },
      STOPPED: { label: '정지', color: 'error' },
      PAUSED: { label: '일시정지', color: 'warning' },
      COMPLETED: { label: '완료', color: 'info' },
    };
    const config = statusConfig[status] || { label: status, color: 'default' };
    return <Chip label={config.label} color={config.color} size="small" />;
  };

  const columns: GridColDef[] = [
    { field: 'operationDate', headerName: '가동 일자', width: 120 },
    { field: 'equipmentCode', headerName: '설비 코드', width: 130 },
    { field: 'equipmentName', headerName: '설비명', width: 180 },
    { field: 'startTime', headerName: '시작 시간', width: 160, valueFormatter: (params) => params.value ? new Date(params.value).toLocaleString('ko-KR') : '' },
    { field: 'endTime', headerName: '종료 시간', width: 160, valueFormatter: (params) => params.value ? new Date(params.value).toLocaleString('ko-KR') : '' },
    { field: 'operationHours', headerName: '가동 시간(h)', width: 120, valueFormatter: (params) => params.value ? params.value.toFixed(2) : '' },
    { field: 'productionQuantity', headerName: '생산량', width: 100, valueFormatter: (params) => params.value ? params.value.toFixed(0) : '' },
    { field: 'goodQuantity', headerName: '양품', width: 100, valueFormatter: (params) => params.value ? params.value.toFixed(0) : '' },
    { field: 'defectQuantity', headerName: '불량', width: 100, valueFormatter: (params) => params.value ? params.value.toFixed(0) : '' },
    {
      field: 'oee',
      headerName: 'OEE(%)',
      width: 100,
      renderCell: (params: GridRenderCellParams) => {
        const oee = params.value;
        if (!oee) return '-';
        const color = oee >= 85 ? 'success' : oee >= 70 ? 'warning' : 'error';
        return <Chip label={`${oee.toFixed(1)}%`} color={color} size="small" />;
      },
    },
    {
      field: 'operationStatus',
      headerName: '상태',
      width: 120,
      renderCell: (params: GridRenderCellParams) => getStatusChip(params.value),
    },
    {
      field: 'actions',
      headerName: '작업',
      width: 120,
      sortable: false,
      renderCell: (params: GridRenderCellParams) => (
        <Box>
          <IconButton
            size="small"
            onClick={() => handleOpenDialog(params.row as EquipmentOperation)}
            color="primary"
          >
            <EditIcon fontSize="small" />
          </IconButton>
          {params.row.operationStatus !== 'COMPLETED' && (
            <IconButton
              size="small"
              onClick={() => handleComplete(params.row as EquipmentOperation)}
              color="success"
            >
              <CompleteIcon fontSize="small" />
            </IconButton>
          )}
          <IconButton
            size="small"
            onClick={() => {
              setSelectedOperation(params.row as EquipmentOperation);
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
            설비 가동 이력
          </Typography>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => handleOpenDialog()}
          >
            가동 이력 등록
          </Button>
        </Box>

        <DataGrid
          rows={operations}
          columns={columns}
          loading={loading}
          getRowId={(row) => row.operationId}
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
        <DialogTitle>{selectedOperation ? '가동 이력 수정' : '가동 이력 등록'}</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
            <FormControl required disabled={!!selectedOperation}>
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
              label="가동 일자"
              type="date"
              value={formData.operationDate}
              onChange={(e) => setFormData({ ...formData, operationDate: e.target.value })}
              required
              InputLabelProps={{ shrink: true }}
            />
            <TextField
              label="시작 시간"
              type="datetime-local"
              value={formData.startTime}
              onChange={(e) => setFormData({ ...formData, startTime: e.target.value })}
              required
              InputLabelProps={{ shrink: true }}
            />
            <TextField
              label="종료 시간"
              type="datetime-local"
              value={formData.endTime || ''}
              onChange={(e) => setFormData({ ...formData, endTime: e.target.value })}
              InputLabelProps={{ shrink: true }}
            />
            <TextField
              label="생산 수량"
              type="number"
              value={formData.productionQuantity || ''}
              onChange={(e) => setFormData({ ...formData, productionQuantity: parseFloat(e.target.value) || undefined })}
            />
            <TextField
              label="양품 수량"
              type="number"
              value={formData.goodQuantity || ''}
              onChange={(e) => setFormData({ ...formData, goodQuantity: parseFloat(e.target.value) || undefined })}
            />
            <TextField
              label="불량 수량"
              type="number"
              value={formData.defectQuantity || ''}
              onChange={(e) => setFormData({ ...formData, defectQuantity: parseFloat(e.target.value) || undefined })}
            />
            <TextField
              label="사이클 타임 (초)"
              type="number"
              value={formData.cycleTime || ''}
              onChange={(e) => setFormData({ ...formData, cycleTime: parseFloat(e.target.value) || undefined })}
            />
            <TextField
              label="정지 사유"
              value={formData.stopReason || ''}
              onChange={(e) => setFormData({ ...formData, stopReason: e.target.value })}
            />
            <TextField
              label="정지 시간 (분)"
              type="number"
              value={formData.stopDurationMinutes || ''}
              onChange={(e) => setFormData({ ...formData, stopDurationMinutes: parseInt(e.target.value) || undefined })}
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
            {selectedOperation ? '수정' : '등록'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={openDeleteDialog} onClose={() => setOpenDeleteDialog(false)}>
        <DialogTitle>가동 이력 삭제</DialogTitle>
        <DialogContent>
          <Typography>
            이 가동 이력을 삭제하시겠습니까?
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

export default EquipmentOperationsPage;
