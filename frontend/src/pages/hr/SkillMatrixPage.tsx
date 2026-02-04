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
  Alert,
  Snackbar,
  MenuItem,
  Select,
  FormControl,
  InputLabel,
  FormControlLabel,
  Checkbox,
} from '@mui/material';
import {
  DataGrid,
  GridColDef,
  GridActionsCellItem,
  GridRowParams,
} from '@mui/x-data-grid';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  ToggleOn as ToggleOnIcon,
  ToggleOff as ToggleOffIcon,
} from '@mui/icons-material';
import skillMatrixService, { SkillMatrix, SkillMatrixCreateRequest, SkillMatrixUpdateRequest } from '../../services/skillMatrixService';

const SKILL_CATEGORIES = [
  { value: 'TECHNICAL', label: '기술' },
  { value: 'OPERATIONAL', label: '운영' },
  { value: 'QUALITY', label: '품질' },
  { value: 'SAFETY', label: '안전' },
  { value: 'MANAGEMENT', label: '관리' },
];

const SkillMatrixPage: React.FC = () => {
  const [skills, setSkills] = useState<SkillMatrix[]>([]);
  const [loading, setLoading] = useState(true);
  const [openDialog, setOpenDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [selectedSkill, setSelectedSkill] = useState<SkillMatrix | null>(null);
  const [formData, setFormData] = useState<SkillMatrixCreateRequest | SkillMatrixUpdateRequest>({
    skillCode: '',
    skillName: '',
    skillCategory: 'TECHNICAL',
    skillLevelDefinition: '',
    description: '',
    certificationRequired: false,
    certificationName: '',
    validityPeriodMonths: undefined,
    remarks: '',
  });
  const [snackbar, setSnackbar] = useState<{ open: boolean; message: string; severity: 'success' | 'error' }>({
    open: false,
    message: '',
    severity: 'success',
  });

  useEffect(() => {
    loadSkills();
  }, []);

  const loadSkills = async () => {
    try {
      setLoading(true);
      const data = await skillMatrixService.getAll();
      setSkills(data);
    } catch (error) {
      showSnackbar('스킬 목록 조회 실패', 'error');
    } finally {
      setLoading(false);
    }
  };

  const showSnackbar = (message: string, severity: 'success' | 'error') => {
    setSnackbar({ open: true, message, severity });
  };

  const handleCloseSnackbar = () => {
    setSnackbar({ ...snackbar, open: false });
  };

  const handleOpenDialog = (skill?: SkillMatrix) => {
    if (skill) {
      setSelectedSkill(skill);
      setFormData({
        skillName: skill.skillName,
        skillCategory: skill.skillCategory,
        skillLevelDefinition: skill.skillLevelDefinition || '',
        description: skill.description || '',
        certificationRequired: skill.certificationRequired,
        certificationName: skill.certificationName || '',
        validityPeriodMonths: skill.validityPeriodMonths,
        remarks: skill.remarks || '',
      });
    } else {
      setSelectedSkill(null);
      setFormData({
        skillCode: '',
        skillName: '',
        skillCategory: 'TECHNICAL',
        skillLevelDefinition: '',
        description: '',
        certificationRequired: false,
        certificationName: '',
        validityPeriodMonths: undefined,
        remarks: '',
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedSkill(null);
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value, type, checked } = e.target;
    setFormData({
      ...formData,
      [name]: type === 'checkbox' ? checked : (name === 'validityPeriodMonths' && value ? Number(value) : value),
    });
  };

  const handleSelectChange = (name: string, value: string) => {
    setFormData({
      ...formData,
      [name]: value,
    });
  };

  const handleSubmit = async () => {
    try {
      if (selectedSkill) {
        // Update
        await skillMatrixService.update(selectedSkill.skillId, formData as SkillMatrixUpdateRequest);
        showSnackbar('스킬 수정 성공', 'success');
      } else {
        // Create
        await skillMatrixService.create(formData as SkillMatrixCreateRequest);
        showSnackbar('스킬 생성 성공', 'success');
      }
      handleCloseDialog();
      loadSkills();
    } catch (error: any) {
      showSnackbar(error.response?.data?.message || '작업 실패', 'error');
    }
  };

  const handleToggleActive = async (skill: SkillMatrix) => {
    try {
      if (skill.isActive) {
        await skillMatrixService.deactivate(skill.skillId);
        showSnackbar('스킬 비활성화 성공', 'success');
      } else {
        await skillMatrixService.activate(skill.skillId);
        showSnackbar('스킬 활성화 성공', 'success');
      }
      loadSkills();
    } catch (error: any) {
      showSnackbar(error.response?.data?.message || '상태 변경 실패', 'error');
    }
  };

  const handleOpenDeleteDialog = (skill: SkillMatrix) => {
    setSelectedSkill(skill);
    setOpenDeleteDialog(true);
  };

  const handleCloseDeleteDialog = () => {
    setOpenDeleteDialog(false);
    setSelectedSkill(null);
  };

  const handleDelete = async () => {
    if (!selectedSkill) return;

    try {
      await skillMatrixService.delete(selectedSkill.skillId);
      showSnackbar('스킬 삭제 성공', 'success');
      handleCloseDeleteDialog();
      loadSkills();
    } catch (error: any) {
      showSnackbar(error.response?.data?.message || '삭제 실패', 'error');
    }
  };

  const getCategoryLabel = (category: string) => {
    return SKILL_CATEGORIES.find(c => c.value === category)?.label || category;
  };

  const columns: GridColDef[] = [
    { field: 'skillCode', headerName: '스킬 코드', width: 130 },
    { field: 'skillName', headerName: '스킬명', flex: 1, minWidth: 200 },
    {
      field: 'skillCategory',
      headerName: '분류',
      width: 100,
      valueGetter: (params) => getCategoryLabel(params.value),
    },
    {
      field: 'certificationRequired',
      headerName: '자격증 필요',
      width: 120,
      renderCell: (params) => (
        <Chip
          label={params.value ? '필요' : '불필요'}
          color={params.value ? 'warning' : 'default'}
          size="small"
        />
      ),
    },
    { field: 'certificationName', headerName: '자격증명', width: 180 },
    {
      field: 'validityPeriodMonths',
      headerName: '유효기간(월)',
      width: 130,
      valueFormatter: (params) => params.value ? `${params.value}개월` : '-',
    },
    {
      field: 'isActive',
      headerName: '상태',
      width: 100,
      renderCell: (params) => (
        <Chip
          label={params.value ? '활성' : '비활성'}
          color={params.value ? 'success' : 'default'}
          size="small"
        />
      ),
    },
    {
      field: 'createdAt',
      headerName: '생성일',
      width: 180,
      valueFormatter: (params) => new Date(params.value).toLocaleString('ko-KR'),
    },
    {
      field: 'actions',
      type: 'actions',
      headerName: '작업',
      width: 150,
      getActions: (params: GridRowParams<SkillMatrix>) => [
        <GridActionsCellItem
          icon={<EditIcon />}
          label="수정"
          onClick={() => handleOpenDialog(params.row)}
        />,
        <GridActionsCellItem
          icon={params.row.isActive ? <ToggleOffIcon /> : <ToggleOnIcon />}
          label={params.row.isActive ? '비활성화' : '활성화'}
          onClick={() => handleToggleActive(params.row)}
        />,
        <GridActionsCellItem
          icon={<DeleteIcon />}
          label="삭제"
          onClick={() => handleOpenDeleteDialog(params.row)}
        />,
      ],
    },
  ];

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h5" component="h1">
          스킬 매트릭스 관리
        </Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => handleOpenDialog()}
        >
          신규 등록
        </Button>
      </Box>

      <Paper>
        <DataGrid
          rows={skills}
          columns={columns}
          getRowId={(row) => row.skillId}
          loading={loading}
          autoHeight
          pageSizeOptions={[10, 25, 50, 100]}
          initialState={{
            pagination: { paginationModel: { pageSize: 25 } },
          }}
          disableRowSelectionOnClick
          sx={{
            '& .MuiDataGrid-cell': {
              borderBottom: '1px solid rgba(224, 224, 224, 1)',
            },
          }}
        />
      </Paper>

      {/* Create/Edit Dialog */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="md" fullWidth>
        <DialogTitle>{selectedSkill ? '스킬 수정' : '신규 스킬 등록'}</DialogTitle>
        <DialogContent>
          <Box display="flex" flexDirection="column" gap={2} mt={1}>
            {!selectedSkill && (
              <TextField
                name="skillCode"
                label="스킬 코드"
                value={(formData as SkillMatrixCreateRequest).skillCode || ''}
                onChange={handleInputChange}
                required
                fullWidth
              />
            )}
            <TextField
              name="skillName"
              label="스킬명"
              value={formData.skillName || ''}
              onChange={handleInputChange}
              required
              fullWidth
            />
            <FormControl fullWidth required>
              <InputLabel>분류</InputLabel>
              <Select
                value={formData.skillCategory || 'TECHNICAL'}
                label="분류"
                onChange={(e) => handleSelectChange('skillCategory', e.target.value)}
              >
                {SKILL_CATEGORIES.map((category) => (
                  <MenuItem key={category.value} value={category.value}>
                    {category.label}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
            <TextField
              name="skillLevelDefinition"
              label="스킬 레벨 정의"
              value={formData.skillLevelDefinition || ''}
              onChange={handleInputChange}
              placeholder="예: 초급/중급/고급/전문가/마스터"
              fullWidth
            />
            <TextField
              name="description"
              label="설명"
              value={formData.description || ''}
              onChange={handleInputChange}
              multiline
              rows={3}
              fullWidth
            />
            <FormControlLabel
              control={
                <Checkbox
                  name="certificationRequired"
                  checked={formData.certificationRequired || false}
                  onChange={handleInputChange}
                />
              }
              label="자격증 필요"
            />
            {formData.certificationRequired && (
              <>
                <TextField
                  name="certificationName"
                  label="자격증명"
                  value={formData.certificationName || ''}
                  onChange={handleInputChange}
                  fullWidth
                />
                <TextField
                  name="validityPeriodMonths"
                  label="유효기간(월)"
                  type="number"
                  value={formData.validityPeriodMonths || ''}
                  onChange={handleInputChange}
                  placeholder="예: 24 (2년)"
                  fullWidth
                />
              </>
            )}
            <TextField
              name="remarks"
              label="비고"
              value={formData.remarks || ''}
              onChange={handleInputChange}
              multiline
              rows={2}
              fullWidth
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>취소</Button>
          <Button onClick={handleSubmit} variant="contained">
            {selectedSkill ? '수정' : '등록'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={openDeleteDialog} onClose={handleCloseDeleteDialog}>
        <DialogTitle>스킬 삭제 확인</DialogTitle>
        <DialogContent>
          <Alert severity="warning" sx={{ mb: 2 }}>
            이 작업은 되돌릴 수 없습니다.
          </Alert>
          <Typography>
            스킬 <strong>{selectedSkill?.skillName}</strong>을(를) 삭제하시겠습니까?
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDeleteDialog}>취소</Button>
          <Button onClick={handleDelete} color="error" variant="contained">
            삭제
          </Button>
        </DialogActions>
      </Dialog>

      {/* Snackbar for notifications */}
      <Snackbar
        open={snackbar.open}
        autoHideDuration={3000}
        onClose={handleCloseSnackbar}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
      >
        <Alert onClose={handleCloseSnackbar} severity={snackbar.severity} sx={{ width: '100%' }}>
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Box>
  );
};

export default SkillMatrixPage;
