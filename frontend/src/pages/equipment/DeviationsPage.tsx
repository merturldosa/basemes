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
  Menu,
} from '@mui/material';
import { DataGrid, GridColDef, GridRenderCellParams } from '@mui/x-data-grid';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Build as StatusIcon,
} from '@mui/icons-material';
import deviationService, { Deviation, DeviationCreateRequest, DeviationUpdateRequest } from '../../services/deviationService';
import equipmentService, { Equipment } from '../../services/equipmentService';

const DeviationsPage: React.FC = () => {
  const [deviations, setDeviations] = useState<Deviation[]>([]);
  const [equipments, setEquipments] = useState<Equipment[]>([]);
  const [loading, setLoading] = useState(false);
  const [openDialog, setOpenDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [selectedDeviation, setSelectedDeviation] = useState<Deviation | null>(null);
  const [statusMenuAnchor, setStatusMenuAnchor] = useState<null | HTMLElement>(null);
  const [statusChangeTarget, setStatusChangeTarget] = useState<Deviation | null>(null);
  const [formData, setFormData] = useState<DeviationCreateRequest & Partial<DeviationUpdateRequest>>({
    deviationNo: '',
    equipmentId: 0,
    parameterName: '',
    detectedAt: new Date().toISOString().slice(0, 16),
    severity: 'MINOR',
  });
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'success' as 'success' | 'error' | 'warning' | 'info',
  });

  useEffect(() => {
    loadDeviations();
    loadEquipments();
  }, []);

  const loadDeviations = async () => {
    try {
      setLoading(true);
      const data = await deviationService.getAll();
      setDeviations(data || []);
    } catch (error) {
      setDeviations([]);
      showSnackbar('이탈 목록을 불러오는데 실패했습니다.', 'error');
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

  const handleOpenDialog = (deviation?: Deviation) => {
    if (deviation) {
      setSelectedDeviation(deviation);
      setFormData({
        deviationNo: deviation.deviationNo,
        equipmentId: deviation.equipmentId,
        parameterName: deviation.parameterName,
        standardValue: deviation.standardValue,
        actualValue: deviation.actualValue,
        deviationValue: deviation.deviationValue,
        detectedAt: deviation.detectedAt,
        severity: deviation.severity,
        description: deviation.description,
        rootCause: deviation.rootCause,
        correctiveAction: deviation.correctiveAction,
        preventiveAction: deviation.preventiveAction,
        remarks: deviation.remarks,
      });
    } else {
      setSelectedDeviation(null);
      setFormData({
        deviationNo: '',
        equipmentId: 0,
        parameterName: '',
        detectedAt: new Date().toISOString().slice(0, 16),
        severity: 'MINOR',
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedDeviation(null);
  };

  const handleSubmit = async () => {
    try {
      if (selectedDeviation) {
        const updateData: DeviationUpdateRequest = {
          parameterName: formData.parameterName,
          standardValue: formData.standardValue,
          actualValue: formData.actualValue,
          deviationValue: formData.deviationValue,
          severity: formData.severity,
          description: formData.description,
          rootCause: formData.rootCause,
          correctiveAction: formData.correctiveAction,
          preventiveAction: formData.preventiveAction,
          remarks: formData.remarks,
        };
        await deviationService.update(selectedDeviation.deviationId, updateData);
        showSnackbar('이탈이 수정되었습니다.', 'success');
      } else {
        const createData: DeviationCreateRequest = {
          deviationNo: formData.deviationNo,
          equipmentId: formData.equipmentId,
          parameterName: formData.parameterName,
          standardValue: formData.standardValue,
          actualValue: formData.actualValue,
          deviationValue: formData.deviationValue,
          detectedAt: formData.detectedAt,
          severity: formData.severity,
          description: formData.description,
          remarks: formData.remarks,
        };
        await deviationService.create(createData);
        showSnackbar('이탈이 등록되었습니다.', 'success');
      }
      handleCloseDialog();
      loadDeviations();
    } catch (error) {
      showSnackbar('이탈 저장에 실패했습니다.', 'error');
    }
  };

  const handleDelete = async () => {
    if (selectedDeviation) {
      try {
        await deviationService.delete(selectedDeviation.deviationId);
        showSnackbar('이탈이 삭제되었습니다.', 'success');
        setOpenDeleteDialog(false);
        setSelectedDeviation(null);
        loadDeviations();
      } catch (error) {
        showSnackbar('이탈 삭제에 실패했습니다.', 'error');
      }
    }
  };

  const handleStatusMenuOpen = (event: React.MouseEvent<HTMLElement>, deviation: Deviation) => {
    setStatusMenuAnchor(event.currentTarget);
    setStatusChangeTarget(deviation);
  };

  const handleStatusMenuClose = () => {
    setStatusMenuAnchor(null);
    setStatusChangeTarget(null);
  };

  const handleStatusChange = async (newStatus: string) => {
    if (statusChangeTarget) {
      try {
        await deviationService.changeStatus(statusChangeTarget.deviationId, newStatus);
        showSnackbar('상태가 변경되었습니다.', 'success');
        loadDeviations();
      } catch (error) {
        showSnackbar('상태 변경에 실패했습니다.', 'error');
      }
    }
    handleStatusMenuClose();
  };

  const showSnackbar = (message: string, severity: 'success' | 'error' | 'warning' | 'info') => {
    setSnackbar({ open: true, message, severity });
  };

  const getSeverityChip = (severity: string) => {
    const config: Record<string, { label: string; color: 'error' | 'warning' | 'info' }> = {
      CRITICAL: { label: '심각', color: 'error' },
      MAJOR: { label: '중요', color: 'warning' },
      MINOR: { label: '경미', color: 'info' },
    };
    const c = config[severity] || { label: severity, color: 'info' };
    return <Chip label={c.label} color={c.color} size="small" />;
  };

  const getStatusChip = (status: string) => {
    const config: Record<string, { label: string; color: 'default' | 'info' | 'success' }> = {
      OPEN: { label: '접수', color: 'default' },
      INVESTIGATING: { label: '조사중', color: 'info' },
      RESOLVED: { label: '해결', color: 'success' },
      CLOSED: { label: '종결', color: 'default' },
    };
    const c = config[status] || { label: status, color: 'default' };
    return <Chip label={c.label} color={c.color} size="small" />;
  };

  const columns: GridColDef[] = [
    { field: 'deviationNo', headerName: '이탈 번호', width: 150 },
    { field: 'equipmentName', headerName: '설비명', width: 180 },
    { field: 'parameterName', headerName: '파라미터명', width: 140 },
    { field: 'standardValue', headerName: '기준값', width: 100 },
    { field: 'actualValue', headerName: '실측값', width: 100 },
    { field: 'deviationValue', headerName: '이탈값', width: 100 },
    {
      field: 'detectedAt',
      headerName: '감지 일시',
      width: 160,
      valueFormatter: (params) => params.value ? new Date(params.value).toLocaleString('ko-KR') : '',
    },
    {
      field: 'severity',
      headerName: '심각도',
      width: 100,
      renderCell: (params: GridRenderCellParams) => params.value ? getSeverityChip(params.value) : '-',
    },
    {
      field: 'status',
      headerName: '상태',
      width: 100,
      renderCell: (params: GridRenderCellParams) => getStatusChip(params.value),
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
            onClick={() => handleOpenDialog(params.row as Deviation)}
            color="primary"
          >
            <EditIcon fontSize="small" />
          </IconButton>
          <IconButton
            size="small"
            onClick={(e) => handleStatusMenuOpen(e, params.row as Deviation)}
            color="warning"
          >
            <StatusIcon fontSize="small" />
          </IconButton>
          <IconButton
            size="small"
            onClick={() => {
              setSelectedDeviation(params.row as Deviation);
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
            이탈 관리
          </Typography>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => handleOpenDialog()}
          >
            이탈 등록
          </Button>
        </Box>

        <DataGrid
          rows={deviations}
          columns={columns}
          loading={loading}
          getRowId={(row) => row.deviationId}
          pageSizeOptions={[10, 25, 50, 100]}
          initialState={{
            pagination: { paginationModel: { pageSize: 25 } },
          }}
          autoHeight
          disableRowSelectionOnClick
        />
      </Paper>

      {/* Status Change Menu */}
      <Menu
        anchorEl={statusMenuAnchor}
        open={Boolean(statusMenuAnchor)}
        onClose={handleStatusMenuClose}
      >
        <MenuItem onClick={() => handleStatusChange('OPEN')}>접수</MenuItem>
        <MenuItem onClick={() => handleStatusChange('INVESTIGATING')}>조사중</MenuItem>
        <MenuItem onClick={() => handleStatusChange('RESOLVED')}>해결</MenuItem>
        <MenuItem onClick={() => handleStatusChange('CLOSED')}>종결</MenuItem>
      </Menu>

      {/* Create/Edit Dialog */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="md" fullWidth>
        <DialogTitle>{selectedDeviation ? '이탈 수정' : '이탈 등록'}</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
            <TextField
              label="이탈 번호"
              value={formData.deviationNo}
              onChange={(e) => setFormData({ ...formData, deviationNo: e.target.value })}
              required
              disabled={!!selectedDeviation}
            />
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
              label="파라미터명"
              value={formData.parameterName}
              onChange={(e) => setFormData({ ...formData, parameterName: e.target.value })}
              required
            />
            <TextField
              label="기준값"
              type="number"
              value={formData.standardValue ?? ''}
              onChange={(e) => setFormData({ ...formData, standardValue: e.target.value ? parseFloat(e.target.value) : undefined })}
            />
            <TextField
              label="실측값"
              type="number"
              value={formData.actualValue ?? ''}
              onChange={(e) => setFormData({ ...formData, actualValue: e.target.value ? parseFloat(e.target.value) : undefined })}
            />
            <TextField
              label="이탈값"
              type="number"
              value={formData.deviationValue ?? ''}
              onChange={(e) => setFormData({ ...formData, deviationValue: e.target.value ? parseFloat(e.target.value) : undefined })}
            />
            <TextField
              label="감지 일시"
              type="datetime-local"
              value={formData.detectedAt}
              onChange={(e) => setFormData({ ...formData, detectedAt: e.target.value })}
              required
              InputLabelProps={{ shrink: true }}
            />
            <FormControl>
              <InputLabel>심각도</InputLabel>
              <Select
                value={formData.severity || ''}
                onChange={(e) => setFormData({ ...formData, severity: e.target.value })}
                label="심각도"
              >
                <MenuItem value="CRITICAL">심각</MenuItem>
                <MenuItem value="MAJOR">중요</MenuItem>
                <MenuItem value="MINOR">경미</MenuItem>
              </Select>
            </FormControl>
            <TextField
              label="설명"
              value={formData.description || ''}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              multiline
              rows={3}
            />
            {selectedDeviation && (
              <>
                <TextField
                  label="근본 원인"
                  value={formData.rootCause || ''}
                  onChange={(e) => setFormData({ ...formData, rootCause: e.target.value })}
                  multiline
                  rows={2}
                />
                <TextField
                  label="시정 조치"
                  value={formData.correctiveAction || ''}
                  onChange={(e) => setFormData({ ...formData, correctiveAction: e.target.value })}
                  multiline
                  rows={2}
                />
                <TextField
                  label="예방 조치"
                  value={formData.preventiveAction || ''}
                  onChange={(e) => setFormData({ ...formData, preventiveAction: e.target.value })}
                  multiline
                  rows={2}
                />
              </>
            )}
            <TextField
              label="비고"
              value={formData.remarks || ''}
              onChange={(e) => setFormData({ ...formData, remarks: e.target.value })}
              multiline
              rows={2}
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>취소</Button>
          <Button onClick={handleSubmit} variant="contained">
            {selectedDeviation ? '수정' : '등록'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={openDeleteDialog} onClose={() => setOpenDeleteDialog(false)}>
        <DialogTitle>이탈 삭제</DialogTitle>
        <DialogContent>
          <Typography>
            {selectedDeviation?.deviationNo}을(를) 삭제하시겠습니까?
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

export default DeviationsPage;
