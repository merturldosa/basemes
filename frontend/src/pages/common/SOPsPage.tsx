import React, { useState, useEffect } from 'react';
import {
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  FormControlLabel,
  Grid,
  IconButton,
  InputLabel,
  MenuItem,
  Select,
  Switch,
  TextField,
  Typography,
  Alert,
  Tabs,
  Tab,
} from '@mui/material';
import {
  DataGrid,
  GridColDef,
  GridRenderCellParams,
  GridToolbar,
} from '@mui/x-data-grid';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  CheckCircle as ApproveIcon,
  Cancel as RejectIcon,
  Send as SubmitIcon,
  Archive as ObsoleteIcon,
} from '@mui/icons-material';
import sopService, {
  SOP,
  SOPStep,
  SOPCreateRequest,
  SOPStepCreateRequest,
} from '../../services/sopService';

const SOPsPage: React.FC = () => {
  const [sops, setSOPs] = useState<SOP[]>([]);
  const [selectedSOP, setSelectedSOP] = useState<SOP | null>(null);
  const [steps, setSteps] = useState<SOPStep[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Dialog states
  const [sopDialogOpen, setSopDialogOpen] = useState(false);
  const [stepDialogOpen, setStepDialogOpen] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [approvalDialogOpen, setApprovalDialogOpen] = useState(false);
  const [editingSOPId, setEditingSOPId] = useState<number | null>(null);
  const [editingStepId, setEditingStepId] = useState<number | null>(null);
  const [deletingId, setDeletingId] = useState<number | null>(null);
  const [deleteType, setDeleteType] = useState<'sop' | 'step'>('sop');

  // Tab state
  const [tabValue, setTabValue] = useState(0);

  // Form data
  const [sopForm, setSOPForm] = useState<SOPCreateRequest>({
    sopCode: '',
    sopName: '',
    description: '',
    sopType: 'PRODUCTION',
    category: '',
    targetProcess: '',
    version: '1.0',
    requiredRole: '',
    restricted: false,
    displayOrder: 0,
  });

  const [stepForm, setStepForm] = useState<SOPStepCreateRequest>({
    stepTitle: '',
    stepDescription: '',
    stepType: 'EXECUTION',
    estimatedDuration: 0,
    detailedInstruction: '',
    cautionNotes: '',
    qualityPoints: '',
    checklistItems: '',
    isCritical: false,
    isMandatory: true,
  });

  useEffect(() => {
    loadSOPs();
  }, []);

  useEffect(() => {
    if (selectedSOP) {
      loadSteps(selectedSOP.sopId);
    }
  }, [selectedSOP]);

  const loadSOPs = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await sopService.getSOPs();
      setSOPs(data || []);
    } catch (err: any) {
      setError(err.message || 'SOP 목록 조회 실패');
      setSOPs([]);
    } finally {
      setLoading(false);
    }
  };

  const loadSteps = async (sopId: number) => {
    try {
      const sop = await sopService.getSOPById(sopId);
      setSteps(sop?.steps || []);
    } catch (err: any) {
      setError(err.message || 'SOP 단계 조회 실패');
      setSteps([]);
    }
  };

  // SOP CRUD handlers
  const handleOpenSOPDialog = (sop?: SOP) => {
    if (sop) {
      setEditingSOPId(sop.sopId);
      setSOPForm({
        sopCode: sop.sopCode,
        sopName: sop.sopName,
        description: sop.description || '',
        sopType: sop.sopType,
        category: sop.category || '',
        targetProcess: sop.targetProcess || '',
        version: sop.version,
        requiredRole: sop.requiredRole || '',
        restricted: sop.restricted,
        displayOrder: sop.displayOrder,
      });
    } else {
      setEditingSOPId(null);
      setSOPForm({
        sopCode: '',
        sopName: '',
        description: '',
        sopType: 'PRODUCTION',
        category: '',
        targetProcess: '',
        version: '1.0',
        requiredRole: '',
        restricted: false,
        displayOrder: 0,
      });
    }
    setSopDialogOpen(true);
  };

  const handleCloseSOPDialog = () => {
    setSopDialogOpen(false);
    setEditingSOPId(null);
  };

  const handleSaveSOPDialog = async () => {
    try {
      if (editingSOPId) {
        await sopService.updateSOP(editingSOPId, sopForm);
      } else {
        await sopService.createSOP(sopForm);
      }
      handleCloseSOPDialog();
      loadSOPs();
    } catch (err: any) {
      setError(err.message || 'SOP 저장 실패');
    }
  };

  // Step CRUD handlers
  const handleOpenStepDialog = (step?: SOPStep) => {
    if (!selectedSOP) {
      setError('SOP를 먼저 선택하세요');
      return;
    }

    if (selectedSOP.approvalStatus !== 'DRAFT' && selectedSOP.approvalStatus !== 'REJECTED') {
      setError('DRAFT 또는 REJECTED 상태의 SOP만 편집 가능합니다');
      return;
    }

    if (step) {
      setEditingStepId(step.sopStepId);
      setStepForm({
        stepTitle: step.stepTitle,
        stepDescription: step.stepDescription || '',
        stepType: step.stepType || 'EXECUTION',
        estimatedDuration: step.estimatedDuration || 0,
        detailedInstruction: step.detailedInstruction || '',
        cautionNotes: step.cautionNotes || '',
        qualityPoints: step.qualityPoints || '',
        checklistItems: step.checklistItems || '',
        prerequisiteStepId: step.prerequisiteStepId,
        isCritical: step.isCritical,
        isMandatory: step.isMandatory,
      });
    } else {
      setEditingStepId(null);
      setStepForm({
        stepTitle: '',
        stepDescription: '',
        stepType: 'EXECUTION',
        estimatedDuration: 0,
        detailedInstruction: '',
        cautionNotes: '',
        qualityPoints: '',
        checklistItems: '',
        isCritical: false,
        isMandatory: true,
      });
    }
    setStepDialogOpen(true);
  };

  const handleCloseStepDialog = () => {
    setStepDialogOpen(false);
    setEditingStepId(null);
  };

  const handleSaveStepDialog = async () => {
    if (!selectedSOP) return;

    try {
      if (editingStepId) {
        await sopService.updateStep(editingStepId, stepForm);
      } else {
        await sopService.addStep(selectedSOP.sopId, stepForm);
      }
      handleCloseStepDialog();
      loadSteps(selectedSOP.sopId);
    } catch (err: any) {
      setError(err.message || 'SOP 단계 저장 실패');
    }
  };

  // Delete handlers
  const handleOpenDeleteDialog = (id: number, type: 'sop' | 'step') => {
    setDeletingId(id);
    setDeleteType(type);
    setDeleteDialogOpen(true);
  };

  const handleCloseDeleteDialog = () => {
    setDeleteDialogOpen(false);
    setDeletingId(null);
  };

  const handleConfirmDelete = async () => {
    if (!deletingId) return;

    try {
      if (deleteType === 'sop') {
        await sopService.deleteSOP(deletingId);
        loadSOPs();
        setSelectedSOP(null);
        setSteps([]);
      } else {
        await sopService.deleteStep(deletingId);
        if (selectedSOP) {
          loadSteps(selectedSOP.sopId);
        }
      }
      handleCloseDeleteDialog();
    } catch (err: any) {
      setError(err.message || '삭제 실패');
    }
  };

  // Approval workflow handlers
  const handleSubmitForApproval = async (sopId: number) => {
    try {
      await sopService.submitForApproval(sopId);
      loadSOPs();
      if (selectedSOP?.sopId === sopId) {
        const updated = await sopService.getSOPById(sopId);
        setSelectedSOP(updated);
      }
    } catch (err: any) {
      setError(err.message || '승인 요청 실패');
    }
  };

  const handleApproveSOP = async (sopId: number, approverId: number) => {
    try {
      await sopService.approveSOP(sopId, approverId);
      loadSOPs();
      if (selectedSOP?.sopId === sopId) {
        const updated = await sopService.getSOPById(sopId);
        setSelectedSOP(updated);
      }
    } catch (err: any) {
      setError(err.message || '승인 실패');
    }
  };

  const handleRejectSOP = async (sopId: number) => {
    try {
      await sopService.rejectSOP(sopId);
      loadSOPs();
      if (selectedSOP?.sopId === sopId) {
        const updated = await sopService.getSOPById(sopId);
        setSelectedSOP(updated);
      }
    } catch (err: any) {
      setError(err.message || '반려 실패');
    }
  };

  const handleMarkObsolete = async (sopId: number) => {
    try {
      await sopService.markObsolete(sopId);
      loadSOPs();
      if (selectedSOP?.sopId === sopId) {
        const updated = await sopService.getSOPById(sopId);
        setSelectedSOP(updated);
      }
    } catch (err: any) {
      setError(err.message || '폐기 실패');
    }
  };

  // SOP columns
  const sopColumns: GridColDef[] = [
    { field: 'sopCode', headerName: 'SOP 코드', width: 150 },
    { field: 'sopName', headerName: 'SOP 명칭', width: 250 },
    {
      field: 'sopType',
      headerName: '유형',
      width: 100,
      renderCell: (params: GridRenderCellParams) => (
        <Chip label={sopService.getTypeLabel(params.value)} size="small" />
      ),
    },
    { field: 'category', headerName: '카테고리', width: 120 },
    { field: 'targetProcess', headerName: '대상 공정', width: 150 },
    { field: 'version', headerName: '버전', width: 80 },
    {
      field: 'approvalStatus',
      headerName: '승인 상태',
      width: 120,
      renderCell: (params: GridRenderCellParams) => (
        <Chip
          label={sopService.getStatusLabel(params.value)}
          size="small"
          sx={{ bgcolor: sopService.getStatusColor(params.value), color: 'white' }}
        />
      ),
    },
    { field: 'effectiveDate', headerName: '시행일', width: 120 },
    {
      field: 'isActive',
      headerName: '활성',
      width: 80,
      renderCell: (params: GridRenderCellParams) => (
        <Chip
          label={params.value ? '활성' : '비활성'}
          size="small"
          color={params.value ? 'success' : 'default'}
        />
      ),
    },
    {
      field: 'actions',
      headerName: '작업',
      width: 250,
      sortable: false,
      renderCell: (params: GridRenderCellParams) => (
        <Box>
          <IconButton size="small" onClick={() => handleOpenSOPDialog(params.row)}>
            <EditIcon fontSize="small" />
          </IconButton>
          <IconButton
            size="small"
            onClick={() => handleOpenDeleteDialog(params.row.sopId, 'sop')}
          >
            <DeleteIcon fontSize="small" />
          </IconButton>
          {params.row.approvalStatus === 'DRAFT' && (
            <IconButton
              size="small"
              onClick={() => handleSubmitForApproval(params.row.sopId)}
              title="승인 요청"
            >
              <SubmitIcon fontSize="small" />
            </IconButton>
          )}
          {params.row.approvalStatus === 'PENDING' && (
            <>
              <IconButton
                size="small"
                onClick={() => handleApproveSOP(params.row.sopId, 1)}
                title="승인"
              >
                <ApproveIcon fontSize="small" color="success" />
              </IconButton>
              <IconButton
                size="small"
                onClick={() => handleRejectSOP(params.row.sopId)}
                title="반려"
              >
                <RejectIcon fontSize="small" color="error" />
              </IconButton>
            </>
          )}
          {params.row.approvalStatus === 'APPROVED' && (
            <IconButton
              size="small"
              onClick={() => handleMarkObsolete(params.row.sopId)}
              title="폐기"
            >
              <ObsoleteIcon fontSize="small" />
            </IconButton>
          )}
        </Box>
      ),
    },
  ];

  // Step columns
  const stepColumns: GridColDef[] = [
    { field: 'stepNumber', headerName: '순서', width: 80 },
    { field: 'stepTitle', headerName: '단계명', width: 250 },
    {
      field: 'stepType',
      headerName: '유형',
      width: 100,
      renderCell: (params: GridRenderCellParams) => (
        <Chip label={sopService.getTypeLabel(params.value || '')} size="small" />
      ),
    },
    { field: 'estimatedDuration', headerName: '예상 시간(분)', width: 120 },
    {
      field: 'isCritical',
      headerName: '중요',
      width: 80,
      renderCell: (params: GridRenderCellParams) =>
        params.value ? <Chip label="⚠️" size="small" color="warning" /> : null,
    },
    {
      field: 'isMandatory',
      headerName: '필수',
      width: 80,
      renderCell: (params: GridRenderCellParams) =>
        params.value ? <Chip label="✓" size="small" color="primary" /> : null,
    },
    {
      field: 'actions',
      headerName: '작업',
      width: 150,
      sortable: false,
      renderCell: (params: GridRenderCellParams) => (
        <Box>
          <IconButton size="small" onClick={() => handleOpenStepDialog(params.row)}>
            <EditIcon fontSize="small" />
          </IconButton>
          <IconButton
            size="small"
            onClick={() => handleOpenDeleteDialog(params.row.sopStepId, 'step')}
          >
            <DeleteIcon fontSize="small" />
          </IconButton>
        </Box>
      ),
    },
  ];

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        SOP (표준 작업 절차) 관리
      </Typography>

      {error && (
        <Alert severity="error" onClose={() => setError(null)} sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      <Grid container spacing={3}>
        {/* SOP List */}
        <Grid item xs={12} md={selectedSOP ? 6 : 12}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
                <Typography variant="h6">SOP 목록</Typography>
                <Button
                  variant="contained"
                  startIcon={<AddIcon />}
                  onClick={() => handleOpenSOPDialog()}
                >
                  SOP 생성
                </Button>
              </Box>

              <DataGrid
                rows={sops}
                columns={sopColumns}
                getRowId={(row) => row.sopId}
                loading={loading}
                autoHeight
                pageSizeOptions={[10, 25, 50]}
                initialState={{
                  pagination: { paginationModel: { pageSize: 10 } },
                }}
                components={{ Toolbar: GridToolbar }}
                onRowClick={(params) => setSelectedSOP(params.row)}
                sx={{
                  '& .MuiDataGrid-row': {
                    cursor: 'pointer',
                  },
                  '& .MuiDataGrid-row.Mui-selected': {
                    bgcolor: 'action.selected',
                  },
                }}
              />
            </CardContent>
          </Card>
        </Grid>

        {/* SOP Detail Panel */}
        {selectedSOP && (
          <Grid item xs={12} md={6}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  SOP 상세: {selectedSOP.sopName}
                </Typography>

                <Tabs value={tabValue} onChange={(e, v) => setTabValue(v)} sx={{ mb: 2 }}>
                  <Tab label="기본 정보" />
                  <Tab label="SOP 단계" />
                </Tabs>

                {tabValue === 0 && (
                  <Box>
                    <Grid container spacing={2}>
                      <Grid item xs={6}>
                        <Typography variant="body2" color="text.secondary">
                          SOP 코드
                        </Typography>
                        <Typography variant="body1">{selectedSOP.sopCode}</Typography>
                      </Grid>
                      <Grid item xs={6}>
                        <Typography variant="body2" color="text.secondary">
                          버전
                        </Typography>
                        <Typography variant="body1">{selectedSOP.version}</Typography>
                      </Grid>
                      <Grid item xs={6}>
                        <Typography variant="body2" color="text.secondary">
                          유형
                        </Typography>
                        <Typography variant="body1">
                          {sopService.getTypeLabel(selectedSOP.sopType)}
                        </Typography>
                      </Grid>
                      <Grid item xs={6}>
                        <Typography variant="body2" color="text.secondary">
                          승인 상태
                        </Typography>
                        <Chip
                          label={sopService.getStatusLabel(selectedSOP.approvalStatus)}
                          size="small"
                          sx={{
                            bgcolor: sopService.getStatusColor(selectedSOP.approvalStatus),
                            color: 'white',
                          }}
                        />
                      </Grid>
                      <Grid item xs={12}>
                        <Typography variant="body2" color="text.secondary">
                          설명
                        </Typography>
                        <Typography variant="body1">{selectedSOP.description}</Typography>
                      </Grid>
                      <Grid item xs={6}>
                        <Typography variant="body2" color="text.secondary">
                          시행일
                        </Typography>
                        <Typography variant="body1">{selectedSOP.effectiveDate}</Typography>
                      </Grid>
                      <Grid item xs={6}>
                        <Typography variant="body2" color="text.secondary">
                          다음 검토일
                        </Typography>
                        <Typography variant="body1">{selectedSOP.nextReviewDate}</Typography>
                      </Grid>
                    </Grid>
                  </Box>
                )}

                {tabValue === 1 && (
                  <Box>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
                      <Typography variant="subtitle1">SOP 단계 ({steps.length}개)</Typography>
                      <Button
                        variant="outlined"
                        size="small"
                        startIcon={<AddIcon />}
                        onClick={() => handleOpenStepDialog()}
                        disabled={
                          selectedSOP.approvalStatus !== 'DRAFT' &&
                          selectedSOP.approvalStatus !== 'REJECTED'
                        }
                      >
                        단계 추가
                      </Button>
                    </Box>

                    <DataGrid
                      rows={steps}
                      columns={stepColumns}
                      getRowId={(row) => row.sopStepId}
                      autoHeight
                      pageSizeOptions={[5, 10, 25]}
                      initialState={{
                        pagination: { paginationModel: { pageSize: 5 } },
                      }}
                      hideFooter={steps.length <= 5}
                    />
                  </Box>
                )}
              </CardContent>
            </Card>
          </Grid>
        )}
      </Grid>

      {/* SOP Dialog */}
      <Dialog open={sopDialogOpen} onClose={handleCloseSOPDialog} maxWidth="md" fullWidth>
        <DialogTitle>{editingSOPId ? 'SOP 수정' : 'SOP 생성'}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={6}>
              <TextField
                fullWidth
                label="SOP 코드"
                value={sopForm.sopCode}
                onChange={(e) => setSOPForm({ ...sopForm, sopCode: e.target.value })}
                required
              />
            </Grid>
            <Grid item xs={6}>
              <TextField
                fullWidth
                label="버전"
                value={sopForm.version}
                onChange={(e) => setSOPForm({ ...sopForm, version: e.target.value })}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="SOP 명칭"
                value={sopForm.sopName}
                onChange={(e) => setSOPForm({ ...sopForm, sopName: e.target.value })}
                required
              />
            </Grid>
            <Grid item xs={6}>
              <FormControl fullWidth>
                <InputLabel>유형</InputLabel>
                <Select
                  value={sopForm.sopType}
                  label="유형"
                  onChange={(e) => setSOPForm({ ...sopForm, sopType: e.target.value })}
                >
                  <MenuItem value="PRODUCTION">생산</MenuItem>
                  <MenuItem value="WAREHOUSE">창고</MenuItem>
                  <MenuItem value="QUALITY">품질</MenuItem>
                  <MenuItem value="FACILITY">설비</MenuItem>
                  <MenuItem value="SAFETY">안전</MenuItem>
                  <MenuItem value="MAINTENANCE">유지보수</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={6}>
              <TextField
                fullWidth
                label="카테고리"
                value={sopForm.category}
                onChange={(e) => setSOPForm({ ...sopForm, category: e.target.value })}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="대상 공정"
                value={sopForm.targetProcess}
                onChange={(e) => setSOPForm({ ...sopForm, targetProcess: e.target.value })}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                multiline
                rows={3}
                label="설명"
                value={sopForm.description}
                onChange={(e) => setSOPForm({ ...sopForm, description: e.target.value })}
              />
            </Grid>
            <Grid item xs={6}>
              <TextField
                fullWidth
                label="필요 권한"
                value={sopForm.requiredRole}
                onChange={(e) => setSOPForm({ ...sopForm, requiredRole: e.target.value })}
              />
            </Grid>
            <Grid item xs={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={sopForm.restricted}
                    onChange={(e) => setSOPForm({ ...sopForm, restricted: e.target.checked })}
                  />
                }
                label="제한 SOP"
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseSOPDialog}>취소</Button>
          <Button onClick={handleSaveSOPDialog} variant="contained">
            저장
          </Button>
        </DialogActions>
      </Dialog>

      {/* Step Dialog */}
      <Dialog open={stepDialogOpen} onClose={handleCloseStepDialog} maxWidth="md" fullWidth>
        <DialogTitle>{editingStepId ? 'SOP 단계 수정' : 'SOP 단계 추가'}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="단계명"
                value={stepForm.stepTitle}
                onChange={(e) => setStepForm({ ...stepForm, stepTitle: e.target.value })}
                required
              />
            </Grid>
            <Grid item xs={6}>
              <FormControl fullWidth>
                <InputLabel>단계 유형</InputLabel>
                <Select
                  value={stepForm.stepType}
                  label="단계 유형"
                  onChange={(e) => setStepForm({ ...stepForm, stepType: e.target.value })}
                >
                  <MenuItem value="PREPARATION">준비</MenuItem>
                  <MenuItem value="EXECUTION">실행</MenuItem>
                  <MenuItem value="INSPECTION">검사</MenuItem>
                  <MenuItem value="DOCUMENTATION">문서화</MenuItem>
                  <MenuItem value="SAFETY">안전</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={6}>
              <TextField
                fullWidth
                type="number"
                label="예상 시간(분)"
                value={stepForm.estimatedDuration}
                onChange={(e) =>
                  setStepForm({ ...stepForm, estimatedDuration: Number(e.target.value) })
                }
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                multiline
                rows={2}
                label="단계 설명"
                value={stepForm.stepDescription}
                onChange={(e) => setStepForm({ ...stepForm, stepDescription: e.target.value })}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                multiline
                rows={3}
                label="상세 작업 지침"
                value={stepForm.detailedInstruction}
                onChange={(e) =>
                  setStepForm({ ...stepForm, detailedInstruction: e.target.value })
                }
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                multiline
                rows={2}
                label="주의사항"
                value={stepForm.cautionNotes}
                onChange={(e) => setStepForm({ ...stepForm, cautionNotes: e.target.value })}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                multiline
                rows={2}
                label="품질 포인트"
                value={stepForm.qualityPoints}
                onChange={(e) => setStepForm({ ...stepForm, qualityPoints: e.target.value })}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                multiline
                rows={2}
                label="체크리스트 (JSON)"
                value={stepForm.checklistItems}
                onChange={(e) => setStepForm({ ...stepForm, checklistItems: e.target.value })}
                placeholder='[{"item": "항목1", "required": true}, {"item": "항목2", "required": false}]'
              />
            </Grid>
            <Grid item xs={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={stepForm.isCritical}
                    onChange={(e) => setStepForm({ ...stepForm, isCritical: e.target.checked })}
                  />
                }
                label="중요 단계"
              />
            </Grid>
            <Grid item xs={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={stepForm.isMandatory}
                    onChange={(e) => setStepForm({ ...stepForm, isMandatory: e.target.checked })}
                  />
                }
                label="필수 단계"
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseStepDialog}>취소</Button>
          <Button onClick={handleSaveStepDialog} variant="contained">
            저장
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={deleteDialogOpen} onClose={handleCloseDeleteDialog}>
        <DialogTitle>삭제 확인</DialogTitle>
        <DialogContent>
          <Typography>
            {deleteType === 'sop' ? 'SOP를' : 'SOP 단계를'} 삭제하시겠습니까?
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
            삭제된 항목은 복구할 수 없습니다.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDeleteDialog}>취소</Button>
          <Button onClick={handleConfirmDelete} color="error" variant="contained">
            삭제
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default SOPsPage;
