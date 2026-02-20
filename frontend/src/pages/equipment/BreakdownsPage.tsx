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
import breakdownService, { Breakdown, BreakdownCreateRequest, BreakdownUpdateRequest } from '../../services/breakdownService';
import equipmentService, { Equipment } from '../../services/equipmentService';

const BreakdownsPage: React.FC = () => {
  const [breakdowns, setBreakdowns] = useState<Breakdown[]>([]);
  const [equipments, setEquipments] = useState<Equipment[]>([]);
  const [loading, setLoading] = useState(false);
  const [openDialog, setOpenDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [selectedBreakdown, setSelectedBreakdown] = useState<Breakdown | null>(null);
  const [statusMenuAnchor, setStatusMenuAnchor] = useState<null | HTMLElement>(null);
  const [statusChangeTarget, setStatusChangeTarget] = useState<Breakdown | null>(null);
  const [formData, setFormData] = useState<BreakdownCreateRequest & Partial<BreakdownUpdateRequest>>({
    breakdownNo: '',
    equipmentId: 0,
    reportedAt: new Date().toISOString().slice(0, 16),
    description: '',
    failureType: 'MECHANICAL',
    severity: 'MINOR',
  });
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'success' as 'success' | 'error' | 'warning' | 'info',
  });

  useEffect(() => {
    loadBreakdowns();
    loadEquipments();
  }, []);

  const loadBreakdowns = async () => {
    try {
      setLoading(true);
      const data = await breakdownService.getAll();
      setBreakdowns(data || []);
    } catch (error) {
      setBreakdowns([]);
      showSnackbar('고장 목록을 불러오는데 실패했습니다.', 'error');
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

  const handleOpenDialog = (breakdown?: Breakdown) => {
    if (breakdown) {
      setSelectedBreakdown(breakdown);
      setFormData({
        breakdownNo: breakdown.breakdownNo,
        equipmentId: breakdown.equipmentId,
        reportedAt: breakdown.reportedAt,
        description: breakdown.description,
        failureType: breakdown.failureType,
        severity: breakdown.severity,
        repairDescription: breakdown.repairDescription,
        rootCause: breakdown.rootCause,
        preventiveAction: breakdown.preventiveAction,
        remarks: breakdown.remarks,
      });
    } else {
      setSelectedBreakdown(null);
      setFormData({
        breakdownNo: '',
        equipmentId: 0,
        reportedAt: new Date().toISOString().slice(0, 16),
        description: '',
        failureType: 'MECHANICAL',
        severity: 'MINOR',
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedBreakdown(null);
  };

  const handleSubmit = async () => {
    try {
      if (selectedBreakdown) {
        const updateData: BreakdownUpdateRequest = {
          failureType: formData.failureType,
          severity: formData.severity,
          description: formData.description,
          repairDescription: formData.repairDescription,
          rootCause: formData.rootCause,
          preventiveAction: formData.preventiveAction,
          remarks: formData.remarks,
        };
        await breakdownService.update(selectedBreakdown.breakdownId, updateData);
        showSnackbar('고장이 수정되었습니다.', 'success');
      } else {
        const createData: BreakdownCreateRequest = {
          breakdownNo: formData.breakdownNo,
          equipmentId: formData.equipmentId,
          reportedAt: formData.reportedAt,
          description: formData.description,
          failureType: formData.failureType,
          severity: formData.severity,
          remarks: formData.remarks,
        };
        await breakdownService.create(createData);
        showSnackbar('고장이 등록되었습니다.', 'success');
      }
      handleCloseDialog();
      loadBreakdowns();
    } catch (error) {
      showSnackbar('고장 저장에 실패했습니다.', 'error');
    }
  };

  const handleDelete = async () => {
    if (selectedBreakdown) {
      try {
        await breakdownService.delete(selectedBreakdown.breakdownId);
        showSnackbar('고장이 삭제되었습니다.', 'success');
        setOpenDeleteDialog(false);
        setSelectedBreakdown(null);
        loadBreakdowns();
      } catch (error) {
        showSnackbar('고장 삭제에 실패했습니다.', 'error');
      }
    }
  };

  const handleStatusMenuOpen = (event: React.MouseEvent<HTMLElement>, breakdown: Breakdown) => {
    setStatusMenuAnchor(event.currentTarget);
    setStatusChangeTarget(breakdown);
  };

  const handleStatusMenuClose = () => {
    setStatusMenuAnchor(null);
    setStatusChangeTarget(null);
  };

  const handleStatusChange = async (newStatus: string) => {
    if (statusChangeTarget) {
      try {
        await breakdownService.changeStatus(statusChangeTarget.breakdownId, newStatus);
        showSnackbar('상태가 변경되었습니다.', 'success');
        loadBreakdowns();
      } catch (error) {
        showSnackbar('상태 변경에 실패했습니다.', 'error');
      }
    }
    handleStatusMenuClose();
  };

  const showSnackbar = (message: string, severity: 'success' | 'error' | 'warning' | 'info') => {
    setSnackbar({ open: true, message, severity });
  };

  const getFailureTypeLabel = (type: string) => {
    const labels: Record<string, string> = {
      MECHANICAL: '기계',
      ELECTRICAL: '전기',
      SOFTWARE: '소프트웨어',
      PNEUMATIC: '공압',
      HYDRAULIC: '유압',
      OTHER: '기타',
    };
    return labels[type] || type;
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
    const config: Record<string, { label: string; color: 'default' | 'info' | 'warning' | 'success' }> = {
      REPORTED: { label: '접수', color: 'default' },
      ASSIGNED: { label: '배정', color: 'info' },
      IN_PROGRESS: { label: '수리중', color: 'warning' },
      COMPLETED: { label: '완료', color: 'success' },
      CLOSED: { label: '종결', color: 'default' },
    };
    const c = config[status] || { label: status, color: 'default' };
    return <Chip label={c.label} color={c.color} size="small" />;
  };

  const columns: GridColDef[] = [
    { field: 'breakdownNo', headerName: '고장 번호', width: 150 },
    { field: 'equipmentName', headerName: '설비명', width: 180 },
    {
      field: 'failureType',
      headerName: '고장 유형',
      width: 120,
      renderCell: (params: GridRenderCellParams) => getFailureTypeLabel(params.value),
    },
    {
      field: 'severity',
      headerName: '심각도',
      width: 100,
      renderCell: (params: GridRenderCellParams) => params.value ? getSeverityChip(params.value) : '-',
    },
    {
      field: 'reportedAt',
      headerName: '접수 일시',
      width: 160,
      valueFormatter: (params) => params.value ? new Date(params.value).toLocaleString('ko-KR') : '',
    },
    {
      field: 'status',
      headerName: '상태',
      width: 100,
      renderCell: (params: GridRenderCellParams) => getStatusChip(params.value),
    },
    {
      field: 'repairDurationMinutes',
      headerName: '수리 시간',
      width: 100,
      renderCell: (params: GridRenderCellParams) => params.value ? `${params.value}분` : '-',
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
            onClick={() => handleOpenDialog(params.row as Breakdown)}
            color="primary"
          >
            <EditIcon fontSize="small" />
          </IconButton>
          <IconButton
            size="small"
            onClick={(e) => handleStatusMenuOpen(e, params.row as Breakdown)}
            color="warning"
          >
            <StatusIcon fontSize="small" />
          </IconButton>
          <IconButton
            size="small"
            onClick={() => {
              setSelectedBreakdown(params.row as Breakdown);
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
            고장 관리
          </Typography>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => handleOpenDialog()}
          >
            고장 등록
          </Button>
        </Box>

        <DataGrid
          rows={breakdowns}
          columns={columns}
          loading={loading}
          getRowId={(row) => row.breakdownId}
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
        <MenuItem onClick={() => handleStatusChange('REPORTED')}>접수</MenuItem>
        <MenuItem onClick={() => handleStatusChange('ASSIGNED')}>배정</MenuItem>
        <MenuItem onClick={() => handleStatusChange('IN_PROGRESS')}>수리중</MenuItem>
        <MenuItem onClick={() => handleStatusChange('COMPLETED')}>완료</MenuItem>
        <MenuItem onClick={() => handleStatusChange('CLOSED')}>종결</MenuItem>
      </Menu>

      {/* Create/Edit Dialog */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="md" fullWidth>
        <DialogTitle>{selectedBreakdown ? '고장 수정' : '고장 등록'}</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
            <TextField
              label="고장 번호"
              value={formData.breakdownNo}
              onChange={(e) => setFormData({ ...formData, breakdownNo: e.target.value })}
              required
              disabled={!!selectedBreakdown}
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
              label="접수 일시"
              type="datetime-local"
              value={formData.reportedAt}
              onChange={(e) => setFormData({ ...formData, reportedAt: e.target.value })}
              required
              InputLabelProps={{ shrink: true }}
            />
            <FormControl>
              <InputLabel>고장 유형</InputLabel>
              <Select
                value={formData.failureType || ''}
                onChange={(e) => setFormData({ ...formData, failureType: e.target.value })}
                label="고장 유형"
              >
                <MenuItem value="MECHANICAL">기계</MenuItem>
                <MenuItem value="ELECTRICAL">전기</MenuItem>
                <MenuItem value="SOFTWARE">소프트웨어</MenuItem>
                <MenuItem value="PNEUMATIC">공압</MenuItem>
                <MenuItem value="HYDRAULIC">유압</MenuItem>
                <MenuItem value="OTHER">기타</MenuItem>
              </Select>
            </FormControl>
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
              label="고장 설명"
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              multiline
              rows={3}
              required
            />
            {selectedBreakdown && (
              <>
                <TextField
                  label="수리 내용"
                  value={formData.repairDescription || ''}
                  onChange={(e) => setFormData({ ...formData, repairDescription: e.target.value })}
                  multiline
                  rows={3}
                />
                <TextField
                  label="근본 원인"
                  value={formData.rootCause || ''}
                  onChange={(e) => setFormData({ ...formData, rootCause: e.target.value })}
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
            {selectedBreakdown ? '수정' : '등록'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={openDeleteDialog} onClose={() => setOpenDeleteDialog(false)}>
        <DialogTitle>고장 삭제</DialogTitle>
        <DialogContent>
          <Typography>
            {selectedBreakdown?.breakdownNo}을(를) 삭제하시겠습니까?
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

export default BreakdownsPage;
