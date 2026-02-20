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
import externalCalibrationService, { ExternalCalibration, ExternalCalibrationCreateRequest, ExternalCalibrationUpdateRequest } from '../../services/externalCalibrationService';
import gaugeService, { Gauge } from '../../services/gaugeService';

const ExternalCalibrationsPage: React.FC = () => {
  const [calibrations, setCalibrations] = useState<ExternalCalibration[]>([]);
  const [gauges, setGauges] = useState<Gauge[]>([]);
  const [loading, setLoading] = useState(false);
  const [openDialog, setOpenDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [openCompleteDialog, setOpenCompleteDialog] = useState(false);
  const [selectedCalibration, setSelectedCalibration] = useState<ExternalCalibration | null>(null);
  const [completeTarget, setCompleteTarget] = useState<ExternalCalibration | null>(null);
  const [completeData, setCompleteData] = useState({
    calibrationResult: 'PASS',
    certificateNo: '',
    nextCalibrationDate: '',
  });
  const [formData, setFormData] = useState<ExternalCalibrationCreateRequest & Partial<ExternalCalibrationUpdateRequest>>({
    calibrationNo: '',
    gaugeId: 0,
    requestedDate: new Date().toISOString().slice(0, 10),
    calibrationVendor: '',
    cost: undefined,
    remarks: '',
  });
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'success' as 'success' | 'error' | 'warning' | 'info',
  });

  useEffect(() => {
    loadCalibrations();
    loadGauges();
  }, []);

  const loadCalibrations = async () => {
    try {
      setLoading(true);
      const data = await externalCalibrationService.getAll();
      setCalibrations(data || []);
    } catch (error) {
      setCalibrations([]);
      showSnackbar('외부 검교정 목록을 불러오는데 실패했습니다.', 'error');
    } finally {
      setLoading(false);
    }
  };

  const loadGauges = async () => {
    try {
      const data = await gaugeService.getAll();
      setGauges(data || []);
    } catch (error) {
      setGauges([]);
    }
  };

  const handleOpenDialog = (calibration?: ExternalCalibration) => {
    if (calibration) {
      setSelectedCalibration(calibration);
      setFormData({
        calibrationNo: calibration.calibrationNo,
        gaugeId: calibration.gaugeId,
        requestedDate: calibration.requestedDate,
        calibrationVendor: calibration.calibrationVendor || '',
        sentDate: calibration.sentDate,
        cost: calibration.cost,
        remarks: calibration.remarks || '',
      });
    } else {
      setSelectedCalibration(null);
      setFormData({
        calibrationNo: '',
        gaugeId: 0,
        requestedDate: new Date().toISOString().slice(0, 10),
        calibrationVendor: '',
        cost: undefined,
        remarks: '',
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedCalibration(null);
  };

  const handleSubmit = async () => {
    try {
      if (selectedCalibration) {
        const updateData: ExternalCalibrationUpdateRequest = {
          calibrationVendor: formData.calibrationVendor,
          sentDate: formData.sentDate,
          cost: formData.cost,
          remarks: formData.remarks,
        };
        await externalCalibrationService.update(selectedCalibration.calibrationId, updateData);
        showSnackbar('외부 검교정이 수정되었습니다.', 'success');
      } else {
        const createData: ExternalCalibrationCreateRequest = {
          calibrationNo: formData.calibrationNo,
          gaugeId: formData.gaugeId,
          requestedDate: formData.requestedDate,
          calibrationVendor: formData.calibrationVendor,
          cost: formData.cost,
          remarks: formData.remarks,
        };
        await externalCalibrationService.create(createData);
        showSnackbar('외부 검교정이 등록되었습니다.', 'success');
      }
      handleCloseDialog();
      loadCalibrations();
    } catch (error) {
      showSnackbar('외부 검교정 저장에 실패했습니다.', 'error');
    }
  };

  const handleDelete = async () => {
    if (selectedCalibration) {
      try {
        await externalCalibrationService.delete(selectedCalibration.calibrationId);
        showSnackbar('외부 검교정이 삭제되었습니다.', 'success');
        setOpenDeleteDialog(false);
        setSelectedCalibration(null);
        loadCalibrations();
      } catch (error) {
        showSnackbar('외부 검교정 삭제에 실패했습니다.', 'error');
      }
    }
  };

  const handleOpenCompleteDialog = (calibration: ExternalCalibration) => {
    setCompleteTarget(calibration);
    setCompleteData({
      calibrationResult: 'PASS',
      certificateNo: '',
      nextCalibrationDate: '',
    });
    setOpenCompleteDialog(true);
  };

  const handleComplete = async () => {
    if (completeTarget) {
      try {
        await externalCalibrationService.complete(
          completeTarget.calibrationId,
          completeData.calibrationResult,
          completeData.certificateNo || undefined,
          completeData.nextCalibrationDate || undefined,
        );
        showSnackbar('완료 처리되었습니다.', 'success');
        setOpenCompleteDialog(false);
        setCompleteTarget(null);
        loadCalibrations();
      } catch (error) {
        showSnackbar('완료 처리에 실패했습니다.', 'error');
      }
    }
  };

  const showSnackbar = (message: string, severity: 'success' | 'error' | 'warning' | 'info') => {
    setSnackbar({ open: true, message, severity });
  };

  const getResultChip = (result: string) => {
    const config: Record<string, { label: string; color: 'success' | 'error' | 'warning' }> = {
      PASS: { label: '합격', color: 'success' },
      FAIL: { label: '불합격', color: 'error' },
      CONDITIONAL: { label: '조건부', color: 'warning' },
    };
    const c = config[result] || { label: result, color: 'success' };
    return <Chip label={c.label} color={c.color} size="small" />;
  };

  const getStatusChip = (status: string) => {
    const config: Record<string, { label: string; color: 'default' | 'info' | 'warning' | 'success' }> = {
      REQUESTED: { label: '의뢰', color: 'default' },
      SENT: { label: '발송', color: 'info' },
      IN_PROGRESS: { label: '진행중', color: 'warning' },
      COMPLETED: { label: '완료', color: 'success' },
    };
    const c = config[status] || { label: status, color: 'default' };
    return <Chip label={c.label} color={c.color} size="small" />;
  };

  const columns: GridColDef[] = [
    { field: 'calibrationNo', headerName: '검교정 번호', width: 150 },
    { field: 'gaugeName', headerName: '게이지명', width: 180 },
    { field: 'calibrationVendor', headerName: '검교정 업체', width: 160 },
    { field: 'requestedDate', headerName: '의뢰일', width: 120 },
    { field: 'sentDate', headerName: '발송일', width: 120, valueFormatter: (params) => params.value || '-' },
    { field: 'completedDate', headerName: '완료일', width: 120, valueFormatter: (params) => params.value || '-' },
    { field: 'certificateNo', headerName: '성적서 번호', width: 140, valueFormatter: (params) => params.value || '-' },
    {
      field: 'calibrationResult',
      headerName: '결과',
      width: 100,
      renderCell: (params: GridRenderCellParams) => params.value ? getResultChip(params.value) : '-',
    },
    {
      field: 'cost',
      headerName: '비용',
      width: 120,
      valueFormatter: (params) => params.value != null ? `\u20A9${params.value.toLocaleString()}` : '-',
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
            onClick={() => handleOpenDialog(params.row as ExternalCalibration)}
            color="primary"
          >
            <EditIcon fontSize="small" />
          </IconButton>
          {params.row.status !== 'COMPLETED' && (
            <IconButton
              size="small"
              onClick={() => handleOpenCompleteDialog(params.row as ExternalCalibration)}
              color="success"
            >
              <CompleteIcon fontSize="small" />
            </IconButton>
          )}
          <IconButton
            size="small"
            onClick={() => {
              setSelectedCalibration(params.row as ExternalCalibration);
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
            외부 검교정 관리
          </Typography>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => handleOpenDialog()}
          >
            검교정 등록
          </Button>
        </Box>

        <DataGrid
          rows={calibrations}
          columns={columns}
          loading={loading}
          getRowId={(row) => row.calibrationId}
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
        <DialogTitle>{selectedCalibration ? '검교정 수정' : '검교정 등록'}</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
            <TextField
              label="검교정 번호"
              value={formData.calibrationNo}
              onChange={(e) => setFormData({ ...formData, calibrationNo: e.target.value })}
              required
              disabled={!!selectedCalibration}
            />
            <FormControl required>
              <InputLabel>게이지</InputLabel>
              <Select
                value={formData.gaugeId}
                onChange={(e) => setFormData({ ...formData, gaugeId: e.target.value as number })}
                label="게이지"
              >
                {gauges.map((gauge) => (
                  <MenuItem key={gauge.gaugeId} value={gauge.gaugeId}>
                    {gauge.gaugeCode} - {gauge.gaugeName}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
            <TextField
              label="의뢰일"
              type="date"
              value={formData.requestedDate}
              onChange={(e) => setFormData({ ...formData, requestedDate: e.target.value })}
              required
              InputLabelProps={{ shrink: true }}
            />
            <TextField
              label="검교정 업체"
              value={formData.calibrationVendor || ''}
              onChange={(e) => setFormData({ ...formData, calibrationVendor: e.target.value })}
            />
            {selectedCalibration && (
              <TextField
                label="발송일"
                type="date"
                value={formData.sentDate || ''}
                onChange={(e) => setFormData({ ...formData, sentDate: e.target.value })}
                InputLabelProps={{ shrink: true }}
              />
            )}
            <TextField
              label="비용 (원)"
              type="number"
              value={formData.cost ?? ''}
              onChange={(e) => setFormData({ ...formData, cost: e.target.value ? parseFloat(e.target.value) : undefined })}
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
            {selectedCalibration ? '수정' : '등록'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Complete Dialog */}
      <Dialog open={openCompleteDialog} onClose={() => setOpenCompleteDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>완료 처리</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
            <FormControl required>
              <InputLabel>검교정 결과</InputLabel>
              <Select
                value={completeData.calibrationResult}
                onChange={(e) => setCompleteData({ ...completeData, calibrationResult: e.target.value })}
                label="검교정 결과"
              >
                <MenuItem value="PASS">합격</MenuItem>
                <MenuItem value="FAIL">불합격</MenuItem>
                <MenuItem value="CONDITIONAL">조건부</MenuItem>
              </Select>
            </FormControl>
            <TextField
              label="성적서 번호"
              value={completeData.certificateNo}
              onChange={(e) => setCompleteData({ ...completeData, certificateNo: e.target.value })}
            />
            <TextField
              label="다음 검교정일"
              type="date"
              value={completeData.nextCalibrationDate}
              onChange={(e) => setCompleteData({ ...completeData, nextCalibrationDate: e.target.value })}
              InputLabelProps={{ shrink: true }}
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenCompleteDialog(false)}>취소</Button>
          <Button onClick={handleComplete} variant="contained" color="success">
            완료 처리
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={openDeleteDialog} onClose={() => setOpenDeleteDialog(false)}>
        <DialogTitle>검교정 삭제</DialogTitle>
        <DialogContent>
          <Typography>
            {selectedCalibration?.calibrationNo}을(를) 삭제하시겠습니까?
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

export default ExternalCalibrationsPage;
