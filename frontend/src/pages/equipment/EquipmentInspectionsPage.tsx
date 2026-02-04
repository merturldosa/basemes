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
import equipmentInspectionService, { EquipmentInspection, EquipmentInspectionCreateRequest, EquipmentInspectionUpdateRequest } from '../../services/equipmentInspectionService';
import equipmentService, { Equipment } from '../../services/equipmentService';

const EquipmentInspectionsPage: React.FC = () => {
  const [inspections, setInspections] = useState<EquipmentInspection[]>([]);
  const [equipments, setEquipments] = useState<Equipment[]>([]);
  const [loading, setLoading] = useState(false);
  const [openDialog, setOpenDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [selectedInspection, setSelectedInspection] = useState<EquipmentInspection | null>(null);
  const [formData, setFormData] = useState<EquipmentInspectionCreateRequest>({
    equipmentId: 0,
    inspectionNo: '',
    inspectionType: 'PERIODIC',
    inspectionDate: new Date().toISOString().slice(0, 16),
    inspectionResult: 'PASS',
  });
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'success' as 'success' | 'error' | 'warning' | 'info',
  });

  useEffect(() => {
    loadInspections();
    loadEquipments();
  }, []);

  const loadInspections = async () => {
    try {
      setLoading(true);
      const data = await equipmentInspectionService.getAll();
      setInspections(data);
    } catch (error) {
      showSnackbar('점검 이력을 불러오는데 실패했습니다.', 'error');
    } finally {
      setLoading(false);
    }
  };

  const loadEquipments = async () => {
    try {
      const data = await equipmentService.getActive();
      setEquipments(data);
    } catch (error) {
      console.error('Failed to load equipments:', error);
    }
  };

  const handleOpenDialog = (inspection?: EquipmentInspection) => {
    if (inspection) {
      setSelectedInspection(inspection);
      setFormData({
        equipmentId: inspection.equipmentId,
        inspectionNo: inspection.inspectionNo,
        inspectionType: inspection.inspectionType,
        inspectionDate: inspection.inspectionDate,
        inspectionResult: inspection.inspectionResult,
        findings: inspection.findings,
        abnormalityDetected: inspection.abnormalityDetected,
        severity: inspection.severity,
        correctiveAction: inspection.correctiveAction,
        partsReplaced: inspection.partsReplaced,
        partsCost: inspection.partsCost,
        laborCost: inspection.laborCost,
        laborHours: inspection.laborHours,
        remarks: inspection.remarks,
      });
    } else {
      setSelectedInspection(null);
      setFormData({
        equipmentId: 0,
        inspectionNo: '',
        inspectionType: 'PERIODIC',
        inspectionDate: new Date().toISOString().slice(0, 16),
        inspectionResult: 'PASS',
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedInspection(null);
  };

  const handleSubmit = async () => {
    try {
      if (selectedInspection) {
        await equipmentInspectionService.update(selectedInspection.inspectionId, formData as EquipmentInspectionUpdateRequest);
        showSnackbar('점검이 수정되었습니다.', 'success');
      } else {
        await equipmentInspectionService.create(formData);
        showSnackbar('점검이 등록되었습니다.', 'success');
      }
      handleCloseDialog();
      loadInspections();
    } catch (error) {
      showSnackbar('점검 저장에 실패했습니다.', 'error');
    }
  };

  const handleComplete = async (inspection: EquipmentInspection) => {
    try {
      await equipmentInspectionService.complete(inspection.inspectionId);
      showSnackbar('점검이 완료되었습니다.', 'success');
      loadInspections();
    } catch (error) {
      showSnackbar('점검 완료 처리에 실패했습니다.', 'error');
    }
  };

  const handleDelete = async () => {
    if (selectedInspection) {
      try {
        await equipmentInspectionService.delete(selectedInspection.inspectionId);
        showSnackbar('점검이 삭제되었습니다.', 'success');
        setOpenDeleteDialog(false);
        setSelectedInspection(null);
        loadInspections();
      } catch (error) {
        showSnackbar('점검 삭제에 실패했습니다.', 'error');
      }
    }
  };

  const showSnackbar = (message: string, severity: 'success' | 'error' | 'warning' | 'info') => {
    setSnackbar({ open: true, message, severity });
  };

  const getResultChip = (result: string) => {
    const resultConfig: Record<string, { label: string; color: 'success' | 'error' | 'warning' }> = {
      PASS: { label: '합격', color: 'success' },
      FAIL: { label: '불합격', color: 'error' },
      CONDITIONAL: { label: '조건부', color: 'warning' },
    };
    const config = resultConfig[result] || { label: result, color: 'success' };
    return <Chip label={config.label} color={config.color} size="small" />;
  };

  const columns: GridColDef[] = [
    { field: 'inspectionNo', headerName: '점검 번호', width: 150 },
    { field: 'inspectionDate', headerName: '점검 일시', width: 160, valueFormatter: (params) => params.value ? new Date(params.value).toLocaleString('ko-KR') : '' },
    { field: 'equipmentCode', headerName: '설비 코드', width: 130 },
    { field: 'equipmentName', headerName: '설비명', width: 180 },
    {
      field: 'inspectionType',
      headerName: '점검 유형',
      width: 120,
      renderCell: (params: GridRenderCellParams) => {
        const typeLabels: Record<string, string> = {
          DAILY: '일상',
          PERIODIC: '정기',
          PREVENTIVE: '예방',
          CORRECTIVE: '시정',
          BREAKDOWN: '고장',
        };
        return typeLabels[params.value] || params.value;
      },
    },
    {
      field: 'inspectionResult',
      headerName: '결과',
      width: 100,
      renderCell: (params: GridRenderCellParams) => getResultChip(params.value),
    },
    { field: 'inspectorName', headerName: '점검자', width: 120 },
    {
      field: 'abnormalityDetected',
      headerName: '이상',
      width: 80,
      renderCell: (params: GridRenderCellParams) => (
        params.value ? <Chip label="O" color="error" size="small" /> : <Chip label="X" color="success" size="small" />
      ),
    },
    {
      field: 'severity',
      headerName: '심각도',
      width: 100,
      renderCell: (params: GridRenderCellParams) => {
        if (!params.value) return '-';
        const severityConfig: Record<string, { label: string; color: 'error' | 'warning' | 'info' | 'default' }> = {
          CRITICAL: { label: '심각', color: 'error' },
          HIGH: { label: '높음', color: 'error' },
          MEDIUM: { label: '중간', color: 'warning' },
          LOW: { label: '낮음', color: 'info' },
        };
        const config = severityConfig[params.value] || { label: params.value, color: 'default' };
        return <Chip label={config.label} color={config.color} size="small" />;
      },
    },
    { field: 'totalCost', headerName: '총 비용', width: 120, valueFormatter: (params) => params.value ? `₩${params.value.toLocaleString()}` : '-' },
    { field: 'nextInspectionDate', headerName: '다음 점검일', width: 130 },
    {
      field: 'actions',
      headerName: '작업',
      width: 120,
      sortable: false,
      renderCell: (params: GridRenderCellParams) => (
        <Box>
          <IconButton
            size="small"
            onClick={() => handleOpenDialog(params.row as EquipmentInspection)}
            color="primary"
          >
            <EditIcon fontSize="small" />
          </IconButton>
          {!params.row.correctiveActionDate && (
            <IconButton
              size="small"
              onClick={() => handleComplete(params.row as EquipmentInspection)}
              color="success"
            >
              <CompleteIcon fontSize="small" />
            </IconButton>
          )}
          <IconButton
            size="small"
            onClick={() => {
              setSelectedInspection(params.row as EquipmentInspection);
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
            설비 점검 관리
          </Typography>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => handleOpenDialog()}
          >
            점검 등록
          </Button>
        </Box>

        <DataGrid
          rows={inspections}
          columns={columns}
          loading={loading}
          getRowId={(row) => row.inspectionId}
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
        <DialogTitle>{selectedInspection ? '점검 수정' : '점검 등록'}</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
            <FormControl required disabled={!!selectedInspection}>
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
              label="점검 번호"
              value={formData.inspectionNo}
              onChange={(e) => setFormData({ ...formData, inspectionNo: e.target.value })}
              required
              disabled={!!selectedInspection}
            />
            <FormControl required>
              <InputLabel>점검 유형</InputLabel>
              <Select
                value={formData.inspectionType}
                onChange={(e) => setFormData({ ...formData, inspectionType: e.target.value })}
                label="점검 유형"
              >
                <MenuItem value="DAILY">일상 점검</MenuItem>
                <MenuItem value="PERIODIC">정기 점검</MenuItem>
                <MenuItem value="PREVENTIVE">예방 점검</MenuItem>
                <MenuItem value="CORRECTIVE">시정 점검</MenuItem>
                <MenuItem value="BREAKDOWN">고장 점검</MenuItem>
              </Select>
            </FormControl>
            <TextField
              label="점검 일시"
              type="datetime-local"
              value={formData.inspectionDate}
              onChange={(e) => setFormData({ ...formData, inspectionDate: e.target.value })}
              required
              InputLabelProps={{ shrink: true }}
            />
            <FormControl required>
              <InputLabel>점검 결과</InputLabel>
              <Select
                value={formData.inspectionResult}
                onChange={(e) => setFormData({ ...formData, inspectionResult: e.target.value })}
                label="점검 결과"
              >
                <MenuItem value="PASS">합격</MenuItem>
                <MenuItem value="FAIL">불합격</MenuItem>
                <MenuItem value="CONDITIONAL">조건부</MenuItem>
              </Select>
            </FormControl>
            <TextField
              label="점검 내용"
              value={formData.findings || ''}
              onChange={(e) => setFormData({ ...formData, findings: e.target.value })}
              multiline
              rows={3}
            />
            <FormControl>
              <InputLabel>이상 발견</InputLabel>
              <Select
                value={formData.abnormalityDetected !== undefined ? (formData.abnormalityDetected ? 'true' : 'false') : ''}
                onChange={(e) => setFormData({ ...formData, abnormalityDetected: e.target.value === 'true' })}
                label="이상 발견"
              >
                <MenuItem value="false">정상</MenuItem>
                <MenuItem value="true">이상 발견</MenuItem>
              </Select>
            </FormControl>
            {formData.abnormalityDetected && (
              <FormControl>
                <InputLabel>심각도</InputLabel>
                <Select
                  value={formData.severity || ''}
                  onChange={(e) => setFormData({ ...formData, severity: e.target.value })}
                  label="심각도"
                >
                  <MenuItem value="LOW">낮음</MenuItem>
                  <MenuItem value="MEDIUM">중간</MenuItem>
                  <MenuItem value="HIGH">높음</MenuItem>
                  <MenuItem value="CRITICAL">심각</MenuItem>
                </Select>
              </FormControl>
            )}
            <TextField
              label="시정 조치"
              value={formData.correctiveAction || ''}
              onChange={(e) => setFormData({ ...formData, correctiveAction: e.target.value })}
              multiline
              rows={2}
            />
            <TextField
              label="교체 부품"
              value={formData.partsReplaced || ''}
              onChange={(e) => setFormData({ ...formData, partsReplaced: e.target.value })}
            />
            <TextField
              label="부품 비용 (원)"
              type="number"
              value={formData.partsCost || ''}
              onChange={(e) => setFormData({ ...formData, partsCost: parseFloat(e.target.value) || undefined })}
            />
            <TextField
              label="작업 비용 (원)"
              type="number"
              value={formData.laborCost || ''}
              onChange={(e) => setFormData({ ...formData, laborCost: parseFloat(e.target.value) || undefined })}
            />
            <TextField
              label="작업 시간 (시간)"
              type="number"
              value={formData.laborHours || ''}
              onChange={(e) => setFormData({ ...formData, laborHours: parseInt(e.target.value) || undefined })}
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
            {selectedInspection ? '수정' : '등록'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={openDeleteDialog} onClose={() => setOpenDeleteDialog(false)}>
        <DialogTitle>점검 삭제</DialogTitle>
        <DialogContent>
          <Typography>
            {selectedInspection?.inspectionNo}을(를) 삭제하시겠습니까?
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

export default EquipmentInspectionsPage;
