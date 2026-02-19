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
} from '@mui/icons-material';
import gaugeService, { Gauge, GaugeCreateRequest, GaugeUpdateRequest } from '../../services/gaugeService';
import equipmentService, { Equipment } from '../../services/equipmentService';

const GaugesPage: React.FC = () => {
  const [gauges, setGauges] = useState<Gauge[]>([]);
  const [equipments, setEquipments] = useState<Equipment[]>([]);
  const [loading, setLoading] = useState(false);
  const [openDialog, setOpenDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [selectedGauge, setSelectedGauge] = useState<Gauge | null>(null);
  const [formData, setFormData] = useState<GaugeCreateRequest>({
    gaugeCode: '',
    gaugeName: '',
    gaugeType: '',
    manufacturer: '',
    modelName: '',
    serialNo: '',
    equipmentId: undefined,
    location: '',
    measurementRange: '',
    accuracy: '',
    calibrationCycleDays: undefined,
    lastCalibrationDate: '',
    nextCalibrationDate: '',
    remarks: '',
  });
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'success' as 'success' | 'error' | 'warning' | 'info',
  });

  useEffect(() => {
    loadGauges();
    loadEquipments();
  }, []);

  const loadGauges = async () => {
    try {
      setLoading(true);
      const data = await gaugeService.getAll();
      setGauges(data || []);
    } catch (error) {
      setGauges([]);
      showSnackbar('계측기를 불러오는데 실패했습니다.', 'error');
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

  const handleOpenDialog = (gauge?: Gauge) => {
    if (gauge) {
      setSelectedGauge(gauge);
      setFormData({
        gaugeCode: gauge.gaugeCode,
        gaugeName: gauge.gaugeName,
        gaugeType: gauge.gaugeType,
        manufacturer: gauge.manufacturer,
        modelName: gauge.modelName,
        serialNo: gauge.serialNo,
        equipmentId: gauge.equipmentId,
        location: gauge.location,
        measurementRange: gauge.measurementRange,
        accuracy: gauge.accuracy,
        calibrationCycleDays: gauge.calibrationCycleDays,
        lastCalibrationDate: gauge.lastCalibrationDate,
        nextCalibrationDate: gauge.nextCalibrationDate,
        remarks: gauge.remarks,
      });
    } else {
      setSelectedGauge(null);
      setFormData({
        gaugeCode: '',
        gaugeName: '',
        gaugeType: '',
        manufacturer: '',
        modelName: '',
        serialNo: '',
        equipmentId: undefined,
        location: '',
        measurementRange: '',
        accuracy: '',
        calibrationCycleDays: undefined,
        lastCalibrationDate: '',
        nextCalibrationDate: '',
        remarks: '',
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedGauge(null);
  };

  const handleSubmit = async () => {
    try {
      if (selectedGauge) {
        const updateData: GaugeUpdateRequest = {
          gaugeName: formData.gaugeName,
          gaugeType: formData.gaugeType,
          manufacturer: formData.manufacturer,
          modelName: formData.modelName,
          serialNo: formData.serialNo,
          equipmentId: formData.equipmentId,
          location: formData.location,
          measurementRange: formData.measurementRange,
          accuracy: formData.accuracy,
          calibrationCycleDays: formData.calibrationCycleDays,
          lastCalibrationDate: formData.lastCalibrationDate,
          nextCalibrationDate: formData.nextCalibrationDate,
          remarks: formData.remarks,
        };
        await gaugeService.update(selectedGauge.gaugeId, updateData);
        showSnackbar('계측기가 수정되었습니다.', 'success');
      } else {
        await gaugeService.create(formData);
        showSnackbar('계측기가 등록되었습니다.', 'success');
      }
      handleCloseDialog();
      loadGauges();
    } catch (error) {
      showSnackbar('계측기 저장에 실패했습니다.', 'error');
    }
  };

  const handleDelete = async () => {
    if (selectedGauge) {
      try {
        await gaugeService.delete(selectedGauge.gaugeId);
        showSnackbar('계측기가 삭제되었습니다.', 'success');
        setOpenDeleteDialog(false);
        setSelectedGauge(null);
        loadGauges();
      } catch (error) {
        showSnackbar('계측기 삭제에 실패했습니다.', 'error');
      }
    }
  };

  const showSnackbar = (message: string, severity: 'success' | 'error' | 'warning' | 'info') => {
    setSnackbar({ open: true, message, severity });
  };

  const columns: GridColDef[] = [
    { field: 'gaugeCode', headerName: '계측기 코드', width: 150 },
    { field: 'gaugeName', headerName: '계측기명', width: 200 },
    { field: 'gaugeType', headerName: '유형', width: 120, valueFormatter: (params) => params.value || '-' },
    { field: 'manufacturer', headerName: '제조사', width: 130, valueFormatter: (params) => params.value || '-' },
    { field: 'equipmentName', headerName: '설비명', width: 180, valueFormatter: (params) => params.value || '-' },
    {
      field: 'calibrationStatus',
      headerName: '교정 상태',
      width: 120,
      renderCell: (params: GridRenderCellParams) => {
        const statusConfig: Record<string, { label: string; color: 'success' | 'error' | 'warning' | 'default' }> = {
          VALID: { label: '유효', color: 'success' },
          EXPIRED: { label: '만료', color: 'error' },
          IN_CALIBRATION: { label: '교정중', color: 'warning' },
        };
        const config = statusConfig[params.value] || { label: params.value || '-', color: 'default' };
        return <Chip label={config.label} color={config.color} size="small" />;
      },
    },
    { field: 'nextCalibrationDate', headerName: '다음 교정일', width: 130, valueFormatter: (params) => params.value || '-' },
    {
      field: 'status',
      headerName: '상태',
      width: 100,
      renderCell: (params: GridRenderCellParams) => {
        const statusConfig: Record<string, { label: string; color: 'success' | 'error' | 'warning' | 'default' }> = {
          ACTIVE: { label: '사용중', color: 'success' },
          INACTIVE: { label: '미사용', color: 'default' },
          DISPOSED: { label: '폐기', color: 'error' },
        };
        const config = statusConfig[params.value] || { label: params.value, color: 'default' };
        return <Chip label={config.label} color={config.color} size="small" />;
      },
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
            onClick={() => handleOpenDialog(params.row as Gauge)}
            color="primary"
          >
            <EditIcon fontSize="small" />
          </IconButton>
          <IconButton
            size="small"
            onClick={() => {
              setSelectedGauge(params.row as Gauge);
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
            계측기 관리
          </Typography>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => handleOpenDialog()}
          >
            계측기 등록
          </Button>
        </Box>

        <DataGrid
          rows={gauges}
          columns={columns}
          loading={loading}
          getRowId={(row) => row.gaugeId}
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
        <DialogTitle>{selectedGauge ? '계측기 수정' : '계측기 등록'}</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
            <TextField
              label="계측기 코드"
              value={formData.gaugeCode}
              onChange={(e) => setFormData({ ...formData, gaugeCode: e.target.value })}
              required
              disabled={!!selectedGauge}
            />
            <TextField
              label="계측기명"
              value={formData.gaugeName}
              onChange={(e) => setFormData({ ...formData, gaugeName: e.target.value })}
              required
            />
            <TextField
              label="유형"
              value={formData.gaugeType || ''}
              onChange={(e) => setFormData({ ...formData, gaugeType: e.target.value })}
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
              label="위치"
              value={formData.location || ''}
              onChange={(e) => setFormData({ ...formData, location: e.target.value })}
            />
            <TextField
              label="측정 범위"
              value={formData.measurementRange || ''}
              onChange={(e) => setFormData({ ...formData, measurementRange: e.target.value })}
            />
            <TextField
              label="정확도"
              value={formData.accuracy || ''}
              onChange={(e) => setFormData({ ...formData, accuracy: e.target.value })}
            />
            <TextField
              label="교정 주기 (일)"
              type="number"
              value={formData.calibrationCycleDays || ''}
              onChange={(e) => setFormData({ ...formData, calibrationCycleDays: parseInt(e.target.value) || undefined })}
            />
            <TextField
              label="최종 교정일"
              type="date"
              value={formData.lastCalibrationDate || ''}
              onChange={(e) => setFormData({ ...formData, lastCalibrationDate: e.target.value })}
              InputLabelProps={{ shrink: true }}
            />
            <TextField
              label="다음 교정일"
              type="date"
              value={formData.nextCalibrationDate || ''}
              onChange={(e) => setFormData({ ...formData, nextCalibrationDate: e.target.value })}
              InputLabelProps={{ shrink: true }}
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
            {selectedGauge ? '수정' : '등록'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={openDeleteDialog} onClose={() => setOpenDeleteDialog(false)}>
        <DialogTitle>계측기 삭제</DialogTitle>
        <DialogContent>
          <Typography>
            {selectedGauge?.gaugeName}을(를) 삭제하시겠습니까?
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

export default GaugesPage;
