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
  Grid,
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
} from '@mui/icons-material';
import employeeSkillService, { EmployeeSkill, EmployeeSkillCreateRequest, EmployeeSkillUpdateRequest } from '../../services/employeeSkillService';
import skillMatrixService, { SkillMatrix } from '../../services/skillMatrixService';

const SKILL_LEVELS = [
  { value: 'BEGINNER', label: '초급', numeric: 1 },
  { value: 'INTERMEDIATE', label: '중급', numeric: 2 },
  { value: 'ADVANCED', label: '고급', numeric: 3 },
  { value: 'EXPERT', label: '전문가', numeric: 4 },
  { value: 'MASTER', label: '마스터', numeric: 5 },
];

const ASSESSMENT_RESULTS = [
  { value: 'PASS', label: '합격' },
  { value: 'FAIL', label: '불합격' },
  { value: 'CONDITIONAL', label: '조건부' },
];

const EmployeeSkillsPage: React.FC = () => {
  const [employeeSkills, setEmployeeSkills] = useState<EmployeeSkill[]>([]);
  const [skills, setSkills] = useState<SkillMatrix[]>([]);
  const [loading, setLoading] = useState(true);
  const [openDialog, setOpenDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [selectedEmployeeSkill, setSelectedEmployeeSkill] = useState<EmployeeSkill | null>(null);
  const [formData, setFormData] = useState<EmployeeSkillCreateRequest | EmployeeSkillUpdateRequest>({
    employeeId: 0,
    skillId: 0,
    skillLevel: '',
    skillLevelNumeric: undefined,
    acquisitionDate: '',
    expiryDate: '',
    lastAssessmentDate: '',
    nextAssessmentDate: '',
    certificationNo: '',
    issuingAuthority: '',
    assessorName: '',
    assessmentScore: undefined,
    assessmentResult: '',
    remarks: '',
  });
  const [snackbar, setSnackbar] = useState<{ open: boolean; message: string; severity: 'success' | 'error' }>({
    open: false,
    message: '',
    severity: 'success',
  });

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const [employeeSkillsData, skillsData] = await Promise.all([
        employeeSkillService.getAll(),
        skillMatrixService.getActive(),
      ]);
      setEmployeeSkills(employeeSkillsData);
      setSkills(skillsData);
    } catch (error) {
      showSnackbar('데이터 조회 실패', 'error');
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

  const handleOpenDialog = (employeeSkill?: EmployeeSkill) => {
    if (employeeSkill) {
      setSelectedEmployeeSkill(employeeSkill);
      setFormData({
        skillLevel: employeeSkill.skillLevel || '',
        skillLevelNumeric: employeeSkill.skillLevelNumeric,
        acquisitionDate: employeeSkill.acquisitionDate || '',
        expiryDate: employeeSkill.expiryDate || '',
        lastAssessmentDate: employeeSkill.lastAssessmentDate || '',
        nextAssessmentDate: employeeSkill.nextAssessmentDate || '',
        certificationNo: employeeSkill.certificationNo || '',
        issuingAuthority: employeeSkill.issuingAuthority || '',
        assessorName: employeeSkill.assessorName || '',
        assessmentScore: employeeSkill.assessmentScore,
        assessmentResult: employeeSkill.assessmentResult || '',
        remarks: employeeSkill.remarks || '',
      });
    } else {
      setSelectedEmployeeSkill(null);
      setFormData({
        employeeId: 0,
        skillId: 0,
        skillLevel: '',
        skillLevelNumeric: undefined,
        acquisitionDate: '',
        expiryDate: '',
        lastAssessmentDate: '',
        nextAssessmentDate: '',
        certificationNo: '',
        issuingAuthority: '',
        assessorName: '',
        assessmentScore: undefined,
        assessmentResult: '',
        remarks: '',
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedEmployeeSkill(null);
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: (name === 'assessmentScore' && value) ? Number(value) : value,
    });
  };

  const handleSelectChange = (name: string, value: string | number) => {
    const updates: any = { [name]: value };

    // Auto-populate numeric level when text level is selected
    if (name === 'skillLevel') {
      const level = SKILL_LEVELS.find(l => l.value === value);
      if (level) {
        updates.skillLevelNumeric = level.numeric;
      }
    }

    setFormData({
      ...formData,
      ...updates,
    });
  };

  const handleSubmit = async () => {
    try {
      if (selectedEmployeeSkill) {
        // Update
        await employeeSkillService.update(selectedEmployeeSkill.employeeSkillId, formData as EmployeeSkillUpdateRequest);
        showSnackbar('사원 스킬 수정 성공', 'success');
      } else {
        // Create
        await employeeSkillService.create(formData as EmployeeSkillCreateRequest);
        showSnackbar('사원 스킬 등록 성공', 'success');
      }
      handleCloseDialog();
      loadData();
    } catch (error: any) {
      showSnackbar(error.response?.data?.message || '작업 실패', 'error');
    }
  };

  const handleOpenDeleteDialog = (employeeSkill: EmployeeSkill) => {
    setSelectedEmployeeSkill(employeeSkill);
    setOpenDeleteDialog(true);
  };

  const handleCloseDeleteDialog = () => {
    setOpenDeleteDialog(false);
    setSelectedEmployeeSkill(null);
  };

  const handleDelete = async () => {
    if (!selectedEmployeeSkill) return;

    try {
      await employeeSkillService.delete(selectedEmployeeSkill.employeeSkillId);
      showSnackbar('사원 스킬 삭제 성공', 'success');
      handleCloseDeleteDialog();
      loadData();
    } catch (error: any) {
      showSnackbar(error.response?.data?.message || '삭제 실패', 'error');
    }
  };

  const getSkillLevelLabel = (level: string) => {
    return SKILL_LEVELS.find(l => l.value === level)?.label || level;
  };

  const getAssessmentResultLabel = (result: string) => {
    return ASSESSMENT_RESULTS.find(r => r.value === result)?.label || result;
  };

  const columns: GridColDef[] = [
    { field: 'employeeNo', headerName: '사원번호', width: 110 },
    { field: 'employeeName', headerName: '사원명', width: 120 },
    { field: 'skillCode', headerName: '스킬 코드', width: 110 },
    { field: 'skillName', headerName: '스킬명', flex: 1, minWidth: 180 },
    { field: 'skillCategory', headerName: '분류', width: 90 },
    {
      field: 'skillLevel',
      headerName: '스킬 레벨',
      width: 110,
      valueGetter: (params) => params.value ? getSkillLevelLabel(params.value) : '-',
    },
    {
      field: 'skillLevelNumeric',
      headerName: '레벨(숫자)',
      width: 100,
      align: 'center',
    },
    {
      field: 'acquisitionDate',
      headerName: '취득일',
      width: 120,
      valueFormatter: (params) => params.value ? new Date(params.value).toLocaleDateString('ko-KR') : '-',
    },
    {
      field: 'expiryDate',
      headerName: '만료일',
      width: 120,
      valueFormatter: (params) => params.value ? new Date(params.value).toLocaleDateString('ko-KR') : '-',
    },
    { field: 'certificationNo', headerName: '자격증 번호', width: 140 },
    {
      field: 'assessmentResult',
      headerName: '평가 결과',
      width: 110,
      renderCell: (params) => {
        if (!params.value) return '-';
        const color = params.value === 'PASS' ? 'success' : params.value === 'FAIL' ? 'error' : 'warning';
        return (
          <Chip
            label={getAssessmentResultLabel(params.value)}
            color={color}
            size="small"
          />
        );
      },
    },
    {
      field: 'assessmentScore',
      headerName: '평가 점수',
      width: 100,
      valueFormatter: (params) => params.value ? `${params.value}점` : '-',
    },
    {
      field: 'actions',
      type: 'actions',
      headerName: '작업',
      width: 120,
      getActions: (params: GridRowParams<EmployeeSkill>) => [
        <GridActionsCellItem
          icon={<EditIcon />}
          label="수정"
          onClick={() => handleOpenDialog(params.row)}
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
          사원 스킬 관리
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
          rows={employeeSkills}
          columns={columns}
          getRowId={(row) => row.employeeSkillId}
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
        <DialogTitle>{selectedEmployeeSkill ? '사원 스킬 수정' : '신규 사원 스킬 등록'}</DialogTitle>
        <DialogContent>
          <Box display="flex" flexDirection="column" gap={2} mt={1}>
            {!selectedEmployeeSkill && (
              <>
                <TextField
                  name="employeeId"
                  label="사원 ID"
                  type="number"
                  value={(formData as EmployeeSkillCreateRequest).employeeId || ''}
                  onChange={handleInputChange}
                  required
                  fullWidth
                  helperText="사원 ID를 입력하세요 (추후 드롭다운으로 개선 예정)"
                />
                <FormControl fullWidth required>
                  <InputLabel>스킬</InputLabel>
                  <Select
                    value={(formData as EmployeeSkillCreateRequest).skillId || ''}
                    label="스킬"
                    onChange={(e) => handleSelectChange('skillId', e.target.value as number)}
                  >
                    {skills.map((skill) => (
                      <MenuItem key={skill.skillId} value={skill.skillId}>
                        {skill.skillCode} - {skill.skillName}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </>
            )}

            <Grid container spacing={2}>
              <Grid item xs={12} sm={6}>
                <FormControl fullWidth>
                  <InputLabel>스킬 레벨</InputLabel>
                  <Select
                    value={formData.skillLevel || ''}
                    label="스킬 레벨"
                    onChange={(e) => handleSelectChange('skillLevel', e.target.value)}
                  >
                    {SKILL_LEVELS.map((level) => (
                      <MenuItem key={level.value} value={level.value}>
                        {level.label} ({level.numeric})
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  name="skillLevelNumeric"
                  label="스킬 레벨(숫자)"
                  type="number"
                  value={formData.skillLevelNumeric || ''}
                  onChange={handleInputChange}
                  inputProps={{ min: 1, max: 5 }}
                  fullWidth
                />
              </Grid>
            </Grid>

            <Grid container spacing={2}>
              <Grid item xs={12} sm={6}>
                <TextField
                  name="acquisitionDate"
                  label="취득일"
                  type="date"
                  value={formData.acquisitionDate || ''}
                  onChange={handleInputChange}
                  InputLabelProps={{ shrink: true }}
                  fullWidth
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  name="expiryDate"
                  label="만료일"
                  type="date"
                  value={formData.expiryDate || ''}
                  onChange={handleInputChange}
                  InputLabelProps={{ shrink: true }}
                  fullWidth
                />
              </Grid>
            </Grid>

            <Grid container spacing={2}>
              <Grid item xs={12} sm={6}>
                <TextField
                  name="certificationNo"
                  label="자격증 번호"
                  value={formData.certificationNo || ''}
                  onChange={handleInputChange}
                  fullWidth
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  name="issuingAuthority"
                  label="발행 기관"
                  value={formData.issuingAuthority || ''}
                  onChange={handleInputChange}
                  fullWidth
                />
              </Grid>
            </Grid>

            <Grid container spacing={2}>
              <Grid item xs={12} sm={6}>
                <TextField
                  name="lastAssessmentDate"
                  label="최근 평가일"
                  type="date"
                  value={formData.lastAssessmentDate || ''}
                  onChange={handleInputChange}
                  InputLabelProps={{ shrink: true }}
                  fullWidth
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  name="nextAssessmentDate"
                  label="다음 평가일"
                  type="date"
                  value={formData.nextAssessmentDate || ''}
                  onChange={handleInputChange}
                  InputLabelProps={{ shrink: true }}
                  fullWidth
                />
              </Grid>
            </Grid>

            <Grid container spacing={2}>
              <Grid item xs={12} sm={4}>
                <TextField
                  name="assessorName"
                  label="평가자"
                  value={formData.assessorName || ''}
                  onChange={handleInputChange}
                  fullWidth
                />
              </Grid>
              <Grid item xs={12} sm={4}>
                <TextField
                  name="assessmentScore"
                  label="평가 점수"
                  type="number"
                  value={formData.assessmentScore || ''}
                  onChange={handleInputChange}
                  inputProps={{ min: 0, max: 100, step: 0.1 }}
                  fullWidth
                />
              </Grid>
              <Grid item xs={12} sm={4}>
                <FormControl fullWidth>
                  <InputLabel>평가 결과</InputLabel>
                  <Select
                    value={formData.assessmentResult || ''}
                    label="평가 결과"
                    onChange={(e) => handleSelectChange('assessmentResult', e.target.value)}
                  >
                    {ASSESSMENT_RESULTS.map((result) => (
                      <MenuItem key={result.value} value={result.value}>
                        {result.label}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Grid>
            </Grid>

            <TextField
              name="remarks"
              label="비고"
              value={formData.remarks || ''}
              onChange={handleInputChange}
              multiline
              rows={3}
              fullWidth
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>취소</Button>
          <Button onClick={handleSubmit} variant="contained">
            {selectedEmployeeSkill ? '수정' : '등록'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={openDeleteDialog} onClose={handleCloseDeleteDialog}>
        <DialogTitle>사원 스킬 삭제 확인</DialogTitle>
        <DialogContent>
          <Alert severity="warning" sx={{ mb: 2 }}>
            이 작업은 되돌릴 수 없습니다.
          </Alert>
          <Typography>
            사원 <strong>{selectedEmployeeSkill?.employeeName}</strong>의 스킬{' '}
            <strong>{selectedEmployeeSkill?.skillName}</strong>을(를) 삭제하시겠습니까?
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

export default EmployeeSkillsPage;
