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
  PlayArrow as ExecuteIcon,
} from '@mui/icons-material';
import inspectionPlanService, { InspectionPlan, InspectionPlanCreateRequest, InspectionPlanUpdateRequest } from '../../services/inspectionPlanService';
import equipmentService, { Equipment } from '../../services/equipmentService';
import inspectionFormService, { InspectionForm } from '../../services/inspectionFormService';

const InspectionPlansPage: React.FC = () => {
  const [plans, setPlans] = useState<InspectionPlan[]>([]);
  const [equipments, setEquipments] = useState<Equipment[]>([]);
  const [forms, setForms] = useState<InspectionForm[]>([]);
  const [loading, setLoading] = useState(false);
  const [openDialog, setOpenDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [selectedPlan, setSelectedPlan] = useState<InspectionPlan | null>(null);
  const [formData, setFormData] = useState<InspectionPlanCreateRequest>({
    planCode: '',
    planName: '',
    equipmentId: 0,
    inspectionType: 'PERIODIC',
    cycleDays: 30,
  });
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'success' as 'success' | 'error' | 'warning' | 'info',
  });

  useEffect(() => {
    loadPlans();
    loadEquipments();
    loadForms();
  }, []);

  const loadPlans = async () => {
    try {
      setLoading(true);
      const data = await inspectionPlanService.getAll();
      setPlans(data || []);
    } catch (error) {
      setPlans([]);
      showSnackbar('점검 계획을 불러오는데 실패했습니다.', 'error');
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

  const loadForms = async () => {
    try {
      const data = await inspectionFormService.getActive();
      setForms(data || []);
    } catch (error) {
      setForms([]);
    }
  };

  const handleOpenDialog = (plan?: InspectionPlan) => {
    if (plan) {
      setSelectedPlan(plan);
      setFormData({
        planCode: plan.planCode,
        planName: plan.planName,
        equipmentId: plan.equipmentId,
        inspectionType: plan.inspectionType,
        cycleDays: plan.cycleDays,
        formId: plan.formId,
        assignedUserId: plan.assignedUserId,
        nextDueDate: plan.nextDueDate,
        remarks: plan.remarks,
      });
    } else {
      setSelectedPlan(null);
      setFormData({
        planCode: '',
        planName: '',
        equipmentId: 0,
        inspectionType: 'PERIODIC',
        cycleDays: 30,
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedPlan(null);
  };

  const handleSubmit = async () => {
    try {
      if (selectedPlan) {
        const updateData: InspectionPlanUpdateRequest = {
          planName: formData.planName,
          equipmentId: formData.equipmentId,
          inspectionType: formData.inspectionType,
          cycleDays: formData.cycleDays,
          formId: formData.formId,
          assignedUserId: formData.assignedUserId,
          nextDueDate: formData.nextDueDate,
          remarks: formData.remarks,
        };
        await inspectionPlanService.update(selectedPlan.planId, updateData);
        showSnackbar('점검 계획이 수정되었습니다.', 'success');
      } else {
        await inspectionPlanService.create(formData);
        showSnackbar('점검 계획이 등록되었습니다.', 'success');
      }
      handleCloseDialog();
      loadPlans();
    } catch (error) {
      showSnackbar('점검 계획 저장에 실패했습니다.', 'error');
    }
  };

  const handleDelete = async () => {
    if (selectedPlan) {
      try {
        await inspectionPlanService.delete(selectedPlan.planId);
        showSnackbar('점검 계획이 삭제되었습니다.', 'success');
        setOpenDeleteDialog(false);
        setSelectedPlan(null);
        loadPlans();
      } catch (error) {
        showSnackbar('점검 계획 삭제에 실패했습니다.', 'error');
      }
    }
  };

  const handleExecute = async (plan: InspectionPlan) => {
    try {
      const today = new Date().toISOString().slice(0, 10);
      await inspectionPlanService.execute(plan.planId, today);
      showSnackbar('점검 계획이 실행되었습니다.', 'success');
      loadPlans();
    } catch (error) {
      showSnackbar('점검 계획 실행에 실패했습니다.', 'error');
    }
  };

  const showSnackbar = (message: string, severity: 'success' | 'error' | 'warning' | 'info') => {
    setSnackbar({ open: true, message, severity });
  };

  const inspectionTypeLabels: Record<string, string> = {
    DAILY: '일상',
    PERIODIC: '정기',
    PREVENTIVE: '예방',
    CORRECTIVE: '시정',
  };

  const columns: GridColDef[] = [
    { field: 'planCode', headerName: '계획 코드', width: 150 },
    { field: 'planName', headerName: '계획명', width: 200 },
    { field: 'equipmentName', headerName: '설비명', width: 180 },
    { field: 'formName', headerName: '양식명', width: 150, valueFormatter: (params) => params.value || '-' },
    {
      field: 'inspectionType',
      headerName: '점검 유형',
      width: 120,
      renderCell: (params: GridRenderCellParams) => inspectionTypeLabels[params.value] || params.value,
    },
    {
      field: 'cycleDays',
      headerName: '주기',
      width: 100,
      valueFormatter: (params) => params.value ? `${params.value}일` : '-',
    },
    { field: 'nextDueDate', headerName: '다음 예정일', width: 130 },
    {
      field: 'status',
      headerName: '상태',
      width: 100,
      renderCell: (params: GridRenderCellParams) => {
        const statusConfig: Record<string, { label: string; color: 'success' | 'warning' | 'default' }> = {
          ACTIVE: { label: '활성', color: 'success' },
          PAUSED: { label: '일시정지', color: 'warning' },
          COMPLETED: { label: '완료', color: 'default' },
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
            onClick={() => handleOpenDialog(params.row as InspectionPlan)}
            color="primary"
          >
            <EditIcon fontSize="small" />
          </IconButton>
          <IconButton
            size="small"
            onClick={() => handleExecute(params.row as InspectionPlan)}
            color="success"
            title="실행"
          >
            <ExecuteIcon fontSize="small" />
          </IconButton>
          <IconButton
            size="small"
            onClick={() => {
              setSelectedPlan(params.row as InspectionPlan);
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
            점검 계획 관리
          </Typography>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => handleOpenDialog()}
          >
            계획 등록
          </Button>
        </Box>

        <DataGrid
          rows={plans}
          columns={columns}
          loading={loading}
          getRowId={(row) => row.planId}
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
        <DialogTitle>{selectedPlan ? '계획 수정' : '계획 등록'}</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
            <TextField
              label="계획 코드"
              value={formData.planCode}
              onChange={(e) => setFormData({ ...formData, planCode: e.target.value })}
              required
              disabled={!!selectedPlan}
            />
            <TextField
              label="계획명"
              value={formData.planName}
              onChange={(e) => setFormData({ ...formData, planName: e.target.value })}
              required
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
            <FormControl>
              <InputLabel>점검 양식</InputLabel>
              <Select
                value={formData.formId || ''}
                onChange={(e) => setFormData({ ...formData, formId: e.target.value as number || undefined })}
                label="점검 양식"
              >
                <MenuItem value="">선택 안함</MenuItem>
                {forms.map((form) => (
                  <MenuItem key={form.formId} value={form.formId}>
                    {form.formCode} - {form.formName}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
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
              </Select>
            </FormControl>
            <TextField
              label="주기 (일)"
              type="number"
              value={formData.cycleDays}
              onChange={(e) => setFormData({ ...formData, cycleDays: parseInt(e.target.value) || 0 })}
              required
            />
            <TextField
              label="담당자 ID"
              type="number"
              value={formData.assignedUserId || ''}
              onChange={(e) => setFormData({ ...formData, assignedUserId: parseInt(e.target.value) || undefined })}
            />
            <TextField
              label="다음 예정일"
              type="date"
              value={formData.nextDueDate || ''}
              onChange={(e) => setFormData({ ...formData, nextDueDate: e.target.value })}
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
            {selectedPlan ? '수정' : '등록'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={openDeleteDialog} onClose={() => setOpenDeleteDialog(false)}>
        <DialogTitle>계획 삭제</DialogTitle>
        <DialogContent>
          <Typography>
            {selectedPlan?.planName}을(를) 삭제하시겠습니까?
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

export default InspectionPlansPage;
