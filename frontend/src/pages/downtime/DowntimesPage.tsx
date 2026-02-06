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
  Stop as StopIcon,
  CheckCircle as ResolveIcon,
  Cancel as InactiveIcon,
  PlayArrow as ActiveIcon,
} from '@mui/icons-material';
import downtimeService, { Downtime, DowntimeCreateRequest, DowntimeUpdateRequest } from '../../services/downtimeService';
import equipmentService, { Equipment } from '../../services/equipmentService';

const DowntimesPage: React.FC = () => {
  const [downtimes, setDowntimes] = useState<Downtime[]>([]);
  const [equipments, setEquipments] = useState<Equipment[]>([]);
  const [loading, setLoading] = useState(false);
  const [openDialog, setOpenDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [selectedDowntime, setSelectedDowntime] = useState<Downtime | null>(null);
  const [formData, setFormData] = useState<DowntimeCreateRequest>({
    equipmentId: 0,
    downtimeCode: '',
    downtimeType: 'BREAKDOWN',
    startTime: new Date().toISOString().slice(0, 16),
  });
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'success' as 'success' | 'error' | 'warning' | 'info',
  });

  useEffect(() => {
    loadDowntimes();
    loadEquipments();
  }, []);

  const loadDowntimes = async () => {
    try {
      setLoading(true);
      const data = await downtimeService.getAll();
      setDowntimes(data || []);
    } catch (error) {
      setDowntimes([]);
      showSnackbar('비가동 목록을 불러오는데 실패했습니다.', 'error');
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

  const handleOpenDialog = (downtime?: Downtime) => {
    if (downtime) {
      setSelectedDowntime(downtime);
      setFormData({
        equipmentId: downtime.equipmentId,
        downtimeCode: downtime.downtimeCode,
        downtimeType: downtime.downtimeType,
        downtimeCategory: downtime.downtimeCategory,
        startTime: downtime.startTime,
        endTime: downtime.endTime,
        cause: downtime.cause,
        countermeasure: downtime.countermeasure,
        preventiveAction: downtime.preventiveAction,
        remarks: downtime.remarks,
      });
    } else {
      setSelectedDowntime(null);
      setFormData({
        equipmentId: 0,
        downtimeCode: '',
        downtimeType: 'BREAKDOWN',
        startTime: new Date().toISOString().slice(0, 16),
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedDowntime(null);
  };

  const handleSubmit = async () => {
    try {
      if (selectedDowntime) {
        await downtimeService.update(selectedDowntime.downtimeId, formData as DowntimeUpdateRequest);
        showSnackbar('비가동이 수정되었습니다.', 'success');
      } else {
        await downtimeService.create(formData);
        showSnackbar('비가동이 등록되었습니다.', 'success');
      }
      handleCloseDialog();
      loadDowntimes();
    } catch (error) {
      showSnackbar('비가동 저장에 실패했습니다.', 'error');
    }
  };

  const handleEnd = async (downtime: Downtime) => {
    try {
      await downtimeService.end(downtime.downtimeId);
      showSnackbar('비가동이 종료되었습니다.', 'success');
      loadDowntimes();
    } catch (error) {
      showSnackbar('비가동 종료에 실패했습니다.', 'error');
    }
  };

  const handleResolve = async (downtime: Downtime) => {
    try {
      await downtimeService.resolve(downtime.downtimeId);
      showSnackbar('비가동이 해결되었습니다.', 'success');
      loadDowntimes();
    } catch (error) {
      showSnackbar('비가동 해결 처리에 실패했습니다.', 'error');
    }
  };

  const handleToggleActive = async (downtime: Downtime) => {
    try {
      if (downtime.isActive) {
        await downtimeService.deactivate(downtime.downtimeId);
        showSnackbar('비가동이 비활성화되었습니다.', 'success');
      } else {
        await downtimeService.activate(downtime.downtimeId);
        showSnackbar('비가동이 활성화되었습니다.', 'success');
      }
      loadDowntimes();
    } catch (error) {
      showSnackbar('비가동 상태 변경에 실패했습니다.', 'error');
    }
  };

  const handleDelete = async () => {
    if (selectedDowntime) {
      try {
        await downtimeService.delete(selectedDowntime.downtimeId);
        showSnackbar('비가동이 삭제되었습니다.', 'success');
        setOpenDeleteDialog(false);
        setSelectedDowntime(null);
        loadDowntimes();
      } catch (error) {
        showSnackbar('비가동 삭제에 실패했습니다.', 'error');
      }
    }
  };

  const showSnackbar = (message: string, severity: 'success' | 'error' | 'warning' | 'info') => {
    setSnackbar({ open: true, message, severity });
  };

  const getTypeChip = (type: string) => {
    const typeConfig: Record<string, { label: string; color: 'error' | 'warning' | 'info' | 'default' }> = {
      BREAKDOWN: { label: '고장', color: 'error' },
      SETUP_CHANGE: { label: '단취/준비', color: 'warning' },
      MATERIAL_SHORTAGE: { label: '자재부족', color: 'warning' },
      QUALITY_ISSUE: { label: '품질문제', color: 'error' },
      PLANNED_MAINTENANCE: { label: '계획보전', color: 'info' },
      UNPLANNED_MAINTENANCE: { label: '돌발보전', color: 'warning' },
      NO_ORDER: { label: '오더부족', color: 'default' },
      OTHER: { label: '기타', color: 'default' },
    };
    const config = typeConfig[type] || { label: type, color: 'default' };
    return <Chip label={config.label} color={config.color} size="small" />;
  };

  const columns: GridColDef[] = [
    { field: 'downtimeCode', headerName: '비가동 코드', width: 150 },
    { field: 'equipmentCode', headerName: '설비 코드', width: 130 },
    { field: 'equipmentName', headerName: '설비명', width: 180 },
    {
      field: 'downtimeType',
      headerName: '비가동 유형',
      width: 130,
      renderCell: (params: GridRenderCellParams) => getTypeChip(params.value),
    },
    { field: 'downtimeCategory', headerName: '분류', width: 120 },
    { field: 'startTime', headerName: '시작 시간', width: 160, valueFormatter: (params) => params.value ? new Date(params.value).toLocaleString('ko-KR') : '' },
    { field: 'endTime', headerName: '종료 시간', width: 160, valueFormatter: (params) => params.value ? new Date(params.value).toLocaleString('ko-KR') : '-' },
    {
      field: 'durationMinutes',
      headerName: '시간(분)',
      width: 100,
      valueFormatter: (params) => params.value ? `${params.value}분` : '-',
    },
    { field: 'responsibleName', headerName: '담당자', width: 120 },
    {
      field: 'isResolved',
      headerName: '해결',
      width: 80,
      renderCell: (params: GridRenderCellParams) => (
        params.value ? <Chip label="O" color="success" size="small" /> : <Chip label="X" color="error" size="small" />
      ),
    },
    {
      field: 'isActive',
      headerName: '활성',
      width: 80,
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
          <IconButton
            size="small"
            onClick={() => handleOpenDialog(params.row as Downtime)}
            color="primary"
          >
            <EditIcon fontSize="small" />
          </IconButton>
          {!params.row.endTime && (
            <IconButton
              size="small"
              onClick={() => handleEnd(params.row as Downtime)}
              color="warning"
            >
              <StopIcon fontSize="small" />
            </IconButton>
          )}
          {!params.row.isResolved && params.row.endTime && (
            <IconButton
              size="small"
              onClick={() => handleResolve(params.row as Downtime)}
              color="success"
            >
              <ResolveIcon fontSize="small" />
            </IconButton>
          )}
          <IconButton
            size="small"
            onClick={() => handleToggleActive(params.row as Downtime)}
            color={params.row.isActive ? 'warning' : 'success'}
          >
            {params.row.isActive ? <InactiveIcon fontSize="small" /> : <ActiveIcon fontSize="small" />}
          </IconButton>
          <IconButton
            size="small"
            onClick={() => {
              setSelectedDowntime(params.row as Downtime);
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
            비가동 관리
          </Typography>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => handleOpenDialog()}
          >
            비가동 등록
          </Button>
        </Box>

        <DataGrid
          rows={downtimes}
          columns={columns}
          loading={loading}
          getRowId={(row) => row.downtimeId}
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
        <DialogTitle>{selectedDowntime ? '비가동 수정' : '비가동 등록'}</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
            <FormControl required disabled={!!selectedDowntime}>
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
              label="비가동 코드"
              value={formData.downtimeCode}
              onChange={(e) => setFormData({ ...formData, downtimeCode: e.target.value })}
              required
              disabled={!!selectedDowntime}
            />
            <FormControl required>
              <InputLabel>비가동 유형</InputLabel>
              <Select
                value={formData.downtimeType}
                onChange={(e) => setFormData({ ...formData, downtimeType: e.target.value })}
                label="비가동 유형"
              >
                <MenuItem value="BREAKDOWN">고장</MenuItem>
                <MenuItem value="SETUP_CHANGE">단취/준비교체</MenuItem>
                <MenuItem value="MATERIAL_SHORTAGE">자재 부족</MenuItem>
                <MenuItem value="QUALITY_ISSUE">품질 문제</MenuItem>
                <MenuItem value="PLANNED_MAINTENANCE">계획 보전</MenuItem>
                <MenuItem value="UNPLANNED_MAINTENANCE">돌발 보전</MenuItem>
                <MenuItem value="NO_ORDER">오더 부족</MenuItem>
                <MenuItem value="OTHER">기타</MenuItem>
              </Select>
            </FormControl>
            <TextField
              label="비가동 분류"
              value={formData.downtimeCategory || ''}
              onChange={(e) => setFormData({ ...formData, downtimeCategory: e.target.value })}
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
              label="원인"
              value={formData.cause || ''}
              onChange={(e) => setFormData({ ...formData, cause: e.target.value })}
              multiline
              rows={3}
            />
            <TextField
              label="조치사항"
              value={formData.countermeasure || ''}
              onChange={(e) => setFormData({ ...formData, countermeasure: e.target.value })}
              multiline
              rows={2}
            />
            <TextField
              label="재발 방지 대책"
              value={formData.preventiveAction || ''}
              onChange={(e) => setFormData({ ...formData, preventiveAction: e.target.value })}
              multiline
              rows={2}
            />
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
            {selectedDowntime ? '수정' : '등록'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={openDeleteDialog} onClose={() => setOpenDeleteDialog(false)}>
        <DialogTitle>비가동 삭제</DialogTitle>
        <DialogContent>
          <Typography>
            {selectedDowntime?.downtimeCode}을(를) 삭제하시겠습니까?
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

export default DowntimesPage;
