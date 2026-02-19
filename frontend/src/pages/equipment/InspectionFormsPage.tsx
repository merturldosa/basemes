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
  AddCircleOutline as AddFieldIcon,
  RemoveCircleOutline as RemoveFieldIcon,
} from '@mui/icons-material';
import inspectionFormService, { InspectionForm, InspectionFormCreateRequest, InspectionFormUpdateRequest, InspectionFormField } from '../../services/inspectionFormService';

const InspectionFormsPage: React.FC = () => {
  const [forms, setForms] = useState<InspectionForm[]>([]);
  const [loading, setLoading] = useState(false);
  const [openDialog, setOpenDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [selectedForm, setSelectedForm] = useState<InspectionForm | null>(null);
  const [formData, setFormData] = useState<InspectionFormCreateRequest>({
    formCode: '',
    formName: '',
    description: '',
    equipmentType: '',
    inspectionType: '',
    fields: [],
  });
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'success' as 'success' | 'error' | 'warning' | 'info',
  });

  useEffect(() => {
    loadForms();
  }, []);

  const loadForms = async () => {
    try {
      setLoading(true);
      const data = await inspectionFormService.getAll();
      setForms(data || []);
    } catch (error) {
      setForms([]);
      showSnackbar('점검 양식을 불러오는데 실패했습니다.', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleOpenDialog = (form?: InspectionForm) => {
    if (form) {
      setSelectedForm(form);
      setFormData({
        formCode: form.formCode,
        formName: form.formName,
        description: form.description,
        equipmentType: form.equipmentType,
        inspectionType: form.inspectionType,
        fields: form.fields ? [...form.fields] : [],
      });
    } else {
      setSelectedForm(null);
      setFormData({
        formCode: '',
        formName: '',
        description: '',
        equipmentType: '',
        inspectionType: '',
        fields: [],
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedForm(null);
  };

  const handleSubmit = async () => {
    try {
      if (selectedForm) {
        const updateData: InspectionFormUpdateRequest = {
          formName: formData.formName,
          description: formData.description,
          equipmentType: formData.equipmentType,
          inspectionType: formData.inspectionType,
          fields: formData.fields,
        };
        await inspectionFormService.update(selectedForm.formId, updateData);
        showSnackbar('점검 양식이 수정되었습니다.', 'success');
      } else {
        await inspectionFormService.create(formData);
        showSnackbar('점검 양식이 등록되었습니다.', 'success');
      }
      handleCloseDialog();
      loadForms();
    } catch (error) {
      showSnackbar('점검 양식 저장에 실패했습니다.', 'error');
    }
  };

  const handleDelete = async () => {
    if (selectedForm) {
      try {
        await inspectionFormService.delete(selectedForm.formId);
        showSnackbar('점검 양식이 삭제되었습니다.', 'success');
        setOpenDeleteDialog(false);
        setSelectedForm(null);
        loadForms();
      } catch (error) {
        showSnackbar('점검 양식 삭제에 실패했습니다.', 'error');
      }
    }
  };

  const showSnackbar = (message: string, severity: 'success' | 'error' | 'warning' | 'info') => {
    setSnackbar({ open: true, message, severity });
  };

  const handleAddField = () => {
    const fields = formData.fields || [];
    setFormData({
      ...formData,
      fields: [
        ...fields,
        {
          fieldName: '',
          fieldType: 'TEXT',
          fieldOrder: fields.length + 1,
          isRequired: false,
          unit: '',
          options: '',
        },
      ],
    });
  };

  const handleRemoveField = (index: number) => {
    const fields = [...(formData.fields || [])];
    fields.splice(index, 1);
    // Re-order
    fields.forEach((f, i) => (f.fieldOrder = i + 1));
    setFormData({ ...formData, fields });
  };

  const handleFieldChange = (index: number, key: keyof InspectionFormField, value: any) => {
    const fields = [...(formData.fields || [])];
    fields[index] = { ...fields[index], [key]: value };
    setFormData({ ...formData, fields });
  };

  const equipmentTypeLabels: Record<string, string> = {
    MACHINE: '기계',
    MOLD: '금형',
    TOOL: '공구',
    FACILITY: '시설',
    VEHICLE: '차량',
    OTHER: '기타',
  };

  const inspectionTypeLabels: Record<string, string> = {
    DAILY: '일상',
    PERIODIC: '정기',
    PREVENTIVE: '예방',
    CORRECTIVE: '시정',
  };

  const columns: GridColDef[] = [
    { field: 'formCode', headerName: '양식 코드', width: 150 },
    { field: 'formName', headerName: '양식명', width: 200 },
    {
      field: 'equipmentType',
      headerName: '설비 유형',
      width: 120,
      renderCell: (params: GridRenderCellParams) => equipmentTypeLabels[params.value] || params.value || '-',
    },
    {
      field: 'inspectionType',
      headerName: '점검 유형',
      width: 120,
      renderCell: (params: GridRenderCellParams) => inspectionTypeLabels[params.value] || params.value || '-',
    },
    {
      field: 'fields',
      headerName: '필드 수',
      width: 100,
      valueGetter: (params: any) => params.value ? params.value.length : 0,
    },
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
      width: 120,
      sortable: false,
      renderCell: (params: GridRenderCellParams) => (
        <Box>
          <IconButton
            size="small"
            onClick={() => handleOpenDialog(params.row as InspectionForm)}
            color="primary"
          >
            <EditIcon fontSize="small" />
          </IconButton>
          <IconButton
            size="small"
            onClick={() => {
              setSelectedForm(params.row as InspectionForm);
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
            점검 양식 관리
          </Typography>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => handleOpenDialog()}
          >
            양식 등록
          </Button>
        </Box>

        <DataGrid
          rows={forms}
          columns={columns}
          loading={loading}
          getRowId={(row) => row.formId}
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
        <DialogTitle>{selectedForm ? '양식 수정' : '양식 등록'}</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
            <TextField
              label="양식 코드"
              value={formData.formCode}
              onChange={(e) => setFormData({ ...formData, formCode: e.target.value })}
              required
              disabled={!!selectedForm}
            />
            <TextField
              label="양식명"
              value={formData.formName}
              onChange={(e) => setFormData({ ...formData, formName: e.target.value })}
              required
            />
            <TextField
              label="설명"
              value={formData.description || ''}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              multiline
              rows={3}
            />
            <FormControl>
              <InputLabel>설비 유형</InputLabel>
              <Select
                value={formData.equipmentType || ''}
                onChange={(e) => setFormData({ ...formData, equipmentType: e.target.value })}
                label="설비 유형"
              >
                <MenuItem value="MACHINE">기계</MenuItem>
                <MenuItem value="MOLD">금형</MenuItem>
                <MenuItem value="TOOL">공구</MenuItem>
                <MenuItem value="FACILITY">시설</MenuItem>
                <MenuItem value="VEHICLE">차량</MenuItem>
                <MenuItem value="OTHER">기타</MenuItem>
              </Select>
            </FormControl>
            <FormControl>
              <InputLabel>점검 유형</InputLabel>
              <Select
                value={formData.inspectionType || ''}
                onChange={(e) => setFormData({ ...formData, inspectionType: e.target.value })}
                label="점검 유형"
              >
                <MenuItem value="DAILY">일상 점검</MenuItem>
                <MenuItem value="PERIODIC">정기 점검</MenuItem>
                <MenuItem value="PREVENTIVE">예방 점검</MenuItem>
                <MenuItem value="CORRECTIVE">시정 점검</MenuItem>
              </Select>
            </FormControl>

            {/* Fields Section */}
            <Box sx={{ mt: 2 }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
                <Typography variant="subtitle1" fontWeight="bold">
                  점검 필드 목록
                </Typography>
                <Button
                  size="small"
                  startIcon={<AddFieldIcon />}
                  onClick={handleAddField}
                >
                  필드 추가
                </Button>
              </Box>
              {(formData.fields || []).map((field, index) => (
                <Paper key={index} variant="outlined" sx={{ p: 2, mb: 1 }}>
                  <Box sx={{ display: 'flex', gap: 1, alignItems: 'center', flexWrap: 'wrap' }}>
                    <TextField
                      label="필드명"
                      value={field.fieldName}
                      onChange={(e) => handleFieldChange(index, 'fieldName', e.target.value)}
                      size="small"
                      sx={{ flex: 1, minWidth: 120 }}
                    />
                    <FormControl size="small" sx={{ minWidth: 120 }}>
                      <InputLabel>유형</InputLabel>
                      <Select
                        value={field.fieldType}
                        onChange={(e) => handleFieldChange(index, 'fieldType', e.target.value)}
                        label="유형"
                      >
                        <MenuItem value="TEXT">텍스트</MenuItem>
                        <MenuItem value="NUMBER">숫자</MenuItem>
                        <MenuItem value="BOOLEAN">예/아니오</MenuItem>
                        <MenuItem value="SELECT">선택</MenuItem>
                      </Select>
                    </FormControl>
                    <TextField
                      label="순서"
                      type="number"
                      value={field.fieldOrder}
                      onChange={(e) => handleFieldChange(index, 'fieldOrder', parseInt(e.target.value) || 0)}
                      size="small"
                      sx={{ width: 80 }}
                    />
                    <FormControl size="small" sx={{ minWidth: 100 }}>
                      <InputLabel>필수</InputLabel>
                      <Select
                        value={field.isRequired ? 'true' : 'false'}
                        onChange={(e) => handleFieldChange(index, 'isRequired', e.target.value === 'true')}
                        label="필수"
                      >
                        <MenuItem value="true">예</MenuItem>
                        <MenuItem value="false">아니오</MenuItem>
                      </Select>
                    </FormControl>
                    {field.fieldType === 'NUMBER' && (
                      <TextField
                        label="단위"
                        value={field.unit || ''}
                        onChange={(e) => handleFieldChange(index, 'unit', e.target.value)}
                        size="small"
                        sx={{ width: 80 }}
                      />
                    )}
                    {field.fieldType === 'SELECT' && (
                      <TextField
                        label="옵션 (쉼표 구분)"
                        value={field.options || ''}
                        onChange={(e) => handleFieldChange(index, 'options', e.target.value)}
                        size="small"
                        sx={{ flex: 1, minWidth: 150 }}
                      />
                    )}
                    <IconButton
                      size="small"
                      onClick={() => handleRemoveField(index)}
                      color="error"
                    >
                      <RemoveFieldIcon fontSize="small" />
                    </IconButton>
                  </Box>
                </Paper>
              ))}
            </Box>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>취소</Button>
          <Button onClick={handleSubmit} variant="contained">
            {selectedForm ? '수정' : '등록'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={openDeleteDialog} onClose={() => setOpenDeleteDialog(false)}>
        <DialogTitle>양식 삭제</DialogTitle>
        <DialogContent>
          <Typography>
            {selectedForm?.formName}을(를) 삭제하시겠습니까?
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

export default InspectionFormsPage;
