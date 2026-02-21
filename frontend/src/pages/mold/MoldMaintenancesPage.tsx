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
  Checkbox,
  FormControlLabel,
} from '@mui/material';
import { DataGrid, GridColDef, GridRenderCellParams } from '@mui/x-data-grid';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
} from '@mui/icons-material';
import moldMaintenanceService, { MoldMaintenance, MoldMaintenanceCreateRequest, MoldMaintenanceUpdateRequest } from '../../services/moldMaintenanceService';
import moldService, { Mold } from '../../services/moldService';

const MoldMaintenancesPage: React.FC = () => {
  const [maintenances, setMaintenances] = useState<MoldMaintenance[]>([]);
  const [molds, setMolds] = useState<Mold[]>([]);
  const [loading, setLoading] = useState(false);
  const [openDialog, setOpenDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [selectedMaintenance, setSelectedMaintenance] = useState<MoldMaintenance | null>(null);
  const [formData, setFormData] = useState<MoldMaintenanceCreateRequest>({
    moldId: 0,
    maintenanceNo: '',
    maintenanceType: 'PERIODIC',
    maintenanceDate: new Date().toISOString().slice(0, 16),
  });
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'success' as 'success' | 'error' | 'warning' | 'info',
  });

  useEffect(() => {
    loadMaintenances();
    loadMolds();
  }, []);

  const loadMaintenances = async () => {
    try {
      setLoading(true);
      const data = await moldMaintenanceService.getAll();
      setMaintenances(data || []);
    } catch (error) {
      showSnackbar('보전 이력을 불러오는데 실패했습니다.', 'error');
      setMaintenances([]);
    } finally {
      setLoading(false);
    }
  };

  const loadMolds = async () => {
    try {
      const data = await moldService.getActive();
      setMolds(data || []);
    } catch (error) {
      setMolds([]);
    }
  };

  const handleOpenDialog = (maintenance?: MoldMaintenance) => {
    if (maintenance) {
      setSelectedMaintenance(maintenance);
      setFormData({
        moldId: maintenance.moldId,
        maintenanceNo: maintenance.maintenanceNo,
        maintenanceType: maintenance.maintenanceType,
        maintenanceDate: maintenance.maintenanceDate.slice(0, 16),
        shotCountBefore: maintenance.shotCountBefore,
        shotCountReset: maintenance.shotCountReset,
        shotCountAfter: maintenance.shotCountAfter,
        maintenanceContent: maintenance.maintenanceContent,
        partsReplaced: maintenance.partsReplaced,
        findings: maintenance.findings,
        correctiveAction: maintenance.correctiveAction,
        partsCost: maintenance.partsCost,
        laborCost: maintenance.laborCost,
        laborHours: maintenance.laborHours,
        maintenanceResult: maintenance.maintenanceResult,
        technicianName: maintenance.technicianName,
        remarks: maintenance.remarks,
      });
    } else {
      setSelectedMaintenance(null);
      setFormData({
        moldId: 0,
        maintenanceNo: '',
        maintenanceType: 'PERIODIC',
        maintenanceDate: new Date().toISOString().slice(0, 16),
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedMaintenance(null);
  };

  const handleSubmit = async () => {
    try {
      if (selectedMaintenance) {
        await moldMaintenanceService.update(selectedMaintenance.maintenanceId, formData as MoldMaintenanceUpdateRequest);
        showSnackbar('보전 이력이 수정되었습니다.', 'success');
      } else {
        await moldMaintenanceService.create(formData);
        showSnackbar('보전 이력이 등록되었습니다.', 'success');
      }
      handleCloseDialog();
      loadMaintenances();
    } catch (error) {
      showSnackbar('보전 이력 저장에 실패했습니다.', 'error');
    }
  };

  const handleDelete = async () => {
    if (selectedMaintenance) {
      try {
        await moldMaintenanceService.delete(selectedMaintenance.maintenanceId);
        showSnackbar('보전 이력이 삭제되었습니다.', 'success');
        setOpenDeleteDialog(false);
        setSelectedMaintenance(null);
        loadMaintenances();
      } catch (error) {
        showSnackbar('보전 이력 삭제에 실패했습니다.', 'error');
      }
    }
  };

  const showSnackbar = (message: string, severity: 'success' | 'error' | 'warning' | 'info') => {
    setSnackbar({ open: true, message, severity });
  };

  const getMaintenanceTypeChip = (type: string) => {
    const typeConfig: Record<string, { label: string; color: 'success' | 'error' | 'warning' | 'default' | 'info' }> = {
      DAILY_CHECK: { label: '일상점검', color: 'info' },
      PERIODIC: { label: '정기보전', color: 'success' },
      SHOT_BASED: { label: 'Shot기준', color: 'warning' },
      EMERGENCY_REPAIR: { label: '긴급수리', color: 'error' },
      OVERHAUL: { label: '오버홀', color: 'warning' },
    };
    const config = typeConfig[type] || { label: type, color: 'default' };
    return <Chip label={config.label} color={config.color} size="small" />;
  };

  const getResultChip = (result?: string) => {
    if (!result) return null;
    const resultConfig: Record<string, { label: string; color: 'success' | 'error' | 'warning' }> = {
      COMPLETED: { label: '완료', color: 'success' },
      PARTIAL: { label: '부분완료', color: 'warning' },
      FAILED: { label: '실패', color: 'error' },
    };
    const config = resultConfig[result] || { label: result, color: 'warning' };
    return <Chip label={config.label} color={config.color} size="small" />;
  };

  const columns: GridColDef[] = [
    { field: 'maintenanceNo', headerName: '보전 번호', width: 130 },
    { field: 'moldCode', headerName: '금형 코드', width: 120 },
    { field: 'moldName', headerName: '금형명', width: 150 },
    {
      field: 'maintenanceType',
      headerName: '보전 유형',
      width: 120,
      renderCell: (params: GridRenderCellParams) => getMaintenanceTypeChip(params.value),
    },
    {
      field: 'maintenanceDate',
      headerName: '보전 일시',
      width: 160,
      renderCell: (params: GridRenderCellParams) => {
        return new Date(params.value).toLocaleString('ko-KR');
      },
    },
    {
      field: 'shotCountBefore',
      headerName: '보전 전 Shot',
      width: 120,
      renderCell: (params: GridRenderCellParams) => {
        return params.value?.toLocaleString() || '-';
      },
    },
    {
      field: 'shotCountReset',
      headerName: 'Shot 리셋',
      width: 100,
      renderCell: (params: GridRenderCellParams) => (
        <Chip
          label={params.value ? '리셋' : '유지'}
          color={params.value ? 'warning' : 'default'}
          size="small"
        />
      ),
    },
    {
      field: 'totalCost',
      headerName: '총 비용',
      width: 110,
      renderCell: (params: GridRenderCellParams) => {
        return params.value ? `₩${params.value.toLocaleString()}` : '-';
      },
    },
    {
      field: 'laborHours',
      headerName: '작업시간',
      width: 100,
      renderCell: (params: GridRenderCellParams) => {
        return params.value ? `${params.value}h` : '-';
      },
    },
    { field: 'technicianName', headerName: '작업자', width: 100 },
    {
      field: 'maintenanceResult',
      headerName: '결과',
      width: 100,
      renderCell: (params: GridRenderCellParams) => getResultChip(params.value),
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
            onClick={() => handleOpenDialog(params.row as MoldMaintenance)}
            color="primary"
          >
            <EditIcon fontSize="small" />
          </IconButton>
          <IconButton
            size="small"
            onClick={() => {
              setSelectedMaintenance(params.row as MoldMaintenance);
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
            금형 보전 관리
          </Typography>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => handleOpenDialog()}
          >
            보전 이력 등록
          </Button>
        </Box>

        <DataGrid
          rows={maintenances}
          columns={columns}
          loading={loading}
          getRowId={(row) => row.maintenanceId}
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
        <DialogTitle>{selectedMaintenance ? '보전 이력 수정' : '보전 이력 등록'}</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
            <FormControl required>
              <InputLabel>금형</InputLabel>
              <Select
                value={formData.moldId || ''}
                onChange={(e) => setFormData({ ...formData, moldId: Number(e.target.value) })}
                label="금형"
                disabled={!!selectedMaintenance}
              >
                {molds.map((mold) => (
                  <MenuItem key={mold.moldId} value={mold.moldId}>
                    {mold.moldCode} - {mold.moldName}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
            <TextField
              label="보전 번호"
              value={formData.maintenanceNo}
              onChange={(e) => setFormData({ ...formData, maintenanceNo: e.target.value })}
              required
              disabled={!!selectedMaintenance}
            />
            <FormControl required>
              <InputLabel>보전 유형</InputLabel>
              <Select
                value={formData.maintenanceType}
                onChange={(e) => setFormData({ ...formData, maintenanceType: e.target.value })}
                label="보전 유형"
              >
                <MenuItem value="DAILY_CHECK">일상점검</MenuItem>
                <MenuItem value="PERIODIC">정기보전</MenuItem>
                <MenuItem value="SHOT_BASED">Shot 기준 보전</MenuItem>
                <MenuItem value="EMERGENCY_REPAIR">긴급수리</MenuItem>
                <MenuItem value="OVERHAUL">오버홀</MenuItem>
              </Select>
            </FormControl>
            <TextField
              label="보전 일시"
              type="datetime-local"
              value={formData.maintenanceDate}
              onChange={(e) => setFormData({ ...formData, maintenanceDate: e.target.value })}
              required
              InputLabelProps={{ shrink: true }}
            />
            <TextField
              label="보전 전 Shot 수"
              type="number"
              value={formData.shotCountBefore || ''}
              onChange={(e) => setFormData({ ...formData, shotCountBefore: parseInt(e.target.value) || undefined })}
            />
            <FormControlLabel
              control={
                <Checkbox
                  checked={formData.shotCountReset || false}
                  onChange={(e) => setFormData({ ...formData, shotCountReset: e.target.checked })}
                />
              }
              label="Shot 수 리셋 (전체 오버홀 시)"
            />
            <TextField
              label="보전 내용"
              value={formData.maintenanceContent || ''}
              onChange={(e) => setFormData({ ...formData, maintenanceContent: e.target.value })}
              multiline
              rows={2}
            />
            <TextField
              label="교체 부품"
              value={formData.partsReplaced || ''}
              onChange={(e) => setFormData({ ...formData, partsReplaced: e.target.value })}
              multiline
              rows={2}
            />
            <TextField
              label="발견 사항"
              value={formData.findings || ''}
              onChange={(e) => setFormData({ ...formData, findings: e.target.value })}
              multiline
              rows={2}
            />
            <TextField
              label="조치 내용"
              value={formData.correctiveAction || ''}
              onChange={(e) => setFormData({ ...formData, correctiveAction: e.target.value })}
              multiline
              rows={2}
            />
            <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 2 }}>
              <TextField
                label="부품 비용"
                type="number"
                value={formData.partsCost || ''}
                onChange={(e) => setFormData({ ...formData, partsCost: parseFloat(e.target.value) || undefined })}
              />
              <TextField
                label="인건비"
                type="number"
                value={formData.laborCost || ''}
                onChange={(e) => setFormData({ ...formData, laborCost: parseFloat(e.target.value) || undefined })}
              />
            </Box>
            <TextField
              label="작업 시간 (시간)"
              type="number"
              value={formData.laborHours || ''}
              onChange={(e) => setFormData({ ...formData, laborHours: parseInt(e.target.value) || undefined })}
            />
            <TextField
              label="작업자명"
              value={formData.technicianName || ''}
              onChange={(e) => setFormData({ ...formData, technicianName: e.target.value })}
            />
            <FormControl>
              <InputLabel>보전 결과</InputLabel>
              <Select
                value={formData.maintenanceResult || ''}
                onChange={(e) => setFormData({ ...formData, maintenanceResult: e.target.value })}
                label="보전 결과"
              >
                <MenuItem value="">선택 안함</MenuItem>
                <MenuItem value="COMPLETED">완료</MenuItem>
                <MenuItem value="PARTIAL">부분 완료</MenuItem>
                <MenuItem value="FAILED">실패</MenuItem>
              </Select>
            </FormControl>
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
            {selectedMaintenance ? '수정' : '등록'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={openDeleteDialog} onClose={() => setOpenDeleteDialog(false)}>
        <DialogTitle>보전 이력 삭제</DialogTitle>
        <DialogContent>
          <Typography>
            {selectedMaintenance?.maintenanceNo}을(를) 삭제하시겠습니까?
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

export default MoldMaintenancesPage;
