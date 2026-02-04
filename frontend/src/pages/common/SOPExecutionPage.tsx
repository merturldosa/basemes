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
  Grid,
  IconButton,
  InputLabel,
  LinearProgress,
  MenuItem,
  Select,
  Step,
  StepLabel,
  Stepper,
  TextField,
  Typography,
  Alert,
  List,
  ListItem,
  ListItemText,
  Checkbox,
  FormControlLabel,
  Divider,
} from '@mui/material';
import {
  PlayArrow as StartIcon,
  Check as CompleteIcon,
  Close as CancelIcon,
  SkipNext as SkipIcon,
  Error as FailIcon,
  Visibility as ViewIcon,
} from '@mui/icons-material';
import sopService, {
  SOP,
  SOPExecution,
  SOPExecutionStep,
  ExecutionStartRequest,
  StepCompleteRequest,
} from '../../services/sopService';

const SOPExecutionPage: React.FC = () => {
  const [approvedSOPs, setApprovedSOPs] = useState<SOP[]>([]);
  const [selectedSOP, setSelectedSOP] = useState<SOP | null>(null);
  const [execution, setExecution] = useState<SOPExecution | null>(null);
  const [currentStepIndex, setCurrentStepIndex] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Dialog states
  const [startDialogOpen, setStartDialogOpen] = useState(false);
  const [stepDialogOpen, setStepDialogOpen] = useState(false);
  const [cancelDialogOpen, setCancelDialogOpen] = useState(false);

  // Form data
  const [startForm, setStartForm] = useState<ExecutionStartRequest>({
    executorId: 1, // TODO: Get from auth context
    referenceType: '',
    referenceId: undefined,
    referenceNo: '',
  });

  const [stepResultForm, setStepResultForm] = useState<StepCompleteRequest>({
    resultValue: '',
    checklistResults: '',
  });

  const [checklist, setChecklist] = useState<Array<{ item: string; checked: boolean }>>([]);
  const [cancelReason, setCancelReason] = useState('');

  useEffect(() => {
    loadApprovedSOPs();
  }, []);

  const loadApprovedSOPs = async () => {
    try {
      setLoading(true);
      const data = await sopService.getApprovedSOPs();
      setApprovedSOPs(data);
    } catch (err: any) {
      setError(err.message || 'SOP 목록 조회 실패');
    } finally {
      setLoading(false);
    }
  };

  const handleOpenStartDialog = (sop: SOP) => {
    setSelectedSOP(sop);
    setStartForm({
      executorId: 1, // TODO: Get from auth context
      referenceType: '',
      referenceId: undefined,
      referenceNo: '',
    });
    setStartDialogOpen(true);
  };

  const handleCloseStartDialog = () => {
    setStartDialogOpen(false);
    setSelectedSOP(null);
  };

  const handleStartExecution = async () => {
    if (!selectedSOP) return;

    try {
      const newExecution = await sopService.startExecution(selectedSOP.sopId, startForm);
      setExecution(newExecution);
      setCurrentStepIndex(0);
      handleCloseStartDialog();

      // Load SOP details with steps
      const sopDetails = await sopService.getSOPById(selectedSOP.sopId);
      setSelectedSOP(sopDetails);
    } catch (err: any) {
      setError(err.message || 'SOP 실행 시작 실패');
    }
  };

  const handleOpenStepDialog = (stepIndex: number) => {
    if (!execution || !selectedSOP || !selectedSOP.steps) return;

    const step = selectedSOP.steps[stepIndex];
    setCurrentStepIndex(stepIndex);

    // Parse checklist items
    let checklistItems: Array<{ item: string; required?: boolean }> = [];
    if (step.checklistItems) {
      try {
        checklistItems = JSON.parse(step.checklistItems);
      } catch (e) {
        console.error('Failed to parse checklist items:', e);
      }
    }

    setChecklist(
      checklistItems.map((item) => ({
        item: item.item,
        checked: false,
      }))
    );

    setStepResultForm({
      resultValue: '',
      checklistResults: '',
    });

    setStepDialogOpen(true);
  };

  const handleCloseStepDialog = () => {
    setStepDialogOpen(false);
  };

  const handleStartStep = async () => {
    if (!execution || !selectedSOP || !selectedSOP.steps) return;

    const step = selectedSOP.steps[currentStepIndex];

    try {
      await sopService.startExecutionStep(execution.executionId, step.sopStepId);
      // Reload execution to get updated status
      // TODO: Implement execution detail fetch
      handleCloseStepDialog();
      handleOpenStepDialog(currentStepIndex);
    } catch (err: any) {
      setError(err.message || '단계 시작 실패');
    }
  };

  const handleCompleteStep = async () => {
    if (!execution || !selectedSOP || !selectedSOP.steps) return;

    const executionStep = execution.executionSteps?.[currentStepIndex];
    if (!executionStep) return;

    try {
      // Build checklist results JSON
      const checklistResults = JSON.stringify(checklist);

      await sopService.completeExecutionStep(executionStep.executionStepId, {
        resultValue: stepResultForm.resultValue,
        checklistResults,
      });

      // Move to next step
      if (currentStepIndex < selectedSOP.steps.length - 1) {
        setCurrentStepIndex(currentStepIndex + 1);
        handleCloseStepDialog();
      } else {
        // All steps completed, complete execution
        await handleCompleteExecution();
      }
    } catch (err: any) {
      setError(err.message || '단계 완료 실패');
    }
  };

  const handleCompleteExecution = async () => {
    if (!execution) return;

    try {
      await sopService.completeExecution(execution.executionId);
      setExecution(null);
      setSelectedSOP(null);
      setCurrentStepIndex(0);
      handleCloseStepDialog();
      alert('SOP 실행이 완료되었습니다.');
    } catch (err: any) {
      setError(err.message || 'SOP 실행 완료 실패');
    }
  };

  const handleOpenCancelDialog = () => {
    setCancelReason('');
    setCancelDialogOpen(true);
  };

  const handleCloseCancelDialog = () => {
    setCancelDialogOpen(false);
  };

  const handleCancelExecution = async () => {
    if (!execution) return;

    try {
      await sopService.cancelExecution(execution.executionId, cancelReason);
      setExecution(null);
      setSelectedSOP(null);
      setCurrentStepIndex(0);
      handleCloseCancelDialog();
    } catch (err: any) {
      setError(err.message || 'SOP 실행 취소 실패');
    }
  };

  const getCurrentStep = () => {
    if (!selectedSOP || !selectedSOP.steps || currentStepIndex >= selectedSOP.steps.length) {
      return null;
    }
    return selectedSOP.steps[currentStepIndex];
  };

  const getCurrentExecutionStep = () => {
    if (!execution || !execution.executionSteps || currentStepIndex >= execution.executionSteps.length) {
      return null;
    }
    return execution.executionSteps[currentStepIndex];
  };

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        SOP 실행
      </Typography>

      {error && (
        <Alert severity="error" onClose={() => setError(null)} sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {!execution ? (
        // SOP Selection View
        <Grid container spacing={3}>
          {approvedSOPs.map((sop) => (
            <Grid item xs={12} sm={6} md={4} key={sop.sopId}>
              <Card>
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    {sop.sopName}
                  </Typography>
                  <Typography variant="body2" color="text.secondary" gutterBottom>
                    {sop.sopCode} (v{sop.version})
                  </Typography>
                  <Box sx={{ mt: 2 }}>
                    <Chip
                      label={sopService.getTypeLabel(sop.sopType)}
                      size="small"
                      sx={{ mr: 1 }}
                    />
                    <Chip
                      label={`${sop.steps?.length || 0}개 단계`}
                      size="small"
                      color="primary"
                    />
                  </Box>
                  {sop.description && (
                    <Typography variant="body2" sx={{ mt: 2 }}>
                      {sop.description}
                    </Typography>
                  )}
                  <Button
                    fullWidth
                    variant="contained"
                    startIcon={<StartIcon />}
                    sx={{ mt: 2 }}
                    onClick={() => handleOpenStartDialog(sop)}
                  >
                    실행 시작
                  </Button>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      ) : (
        // Execution View
        <Card>
          <CardContent>
            <Box sx={{ mb: 3 }}>
              <Typography variant="h5" gutterBottom>
                {selectedSOP?.sopName}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                실행 번호: {execution.executionNo}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                작업자: {execution.executorName}
              </Typography>
              {execution.referenceNo && (
                <Typography variant="body2" color="text.secondary">
                  참조: {execution.referenceType} - {execution.referenceNo}
                </Typography>
              )}
            </Box>

            <Box sx={{ mb: 3 }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                <Typography variant="body2">
                  진행률: {execution.completionRate.toFixed(0)}%
                </Typography>
                <Typography variant="body2">
                  {execution.stepsCompleted} / {execution.stepsTotal} 단계 완료
                </Typography>
              </Box>
              <LinearProgress
                variant="determinate"
                value={execution.completionRate}
                sx={{ height: 10, borderRadius: 5 }}
              />
            </Box>

            <Stepper activeStep={currentStepIndex} alternativeLabel sx={{ mb: 3 }}>
              {selectedSOP?.steps?.map((step, index) => (
                <Step key={step.sopStepId}>
                  <StepLabel
                    error={execution.executionSteps?.[index]?.stepStatus === 'FAILED'}
                    optional={
                      step.isCritical ? (
                        <Typography variant="caption" color="warning.main">
                          ⚠️ 중요
                        </Typography>
                      ) : undefined
                    }
                  >
                    {step.stepTitle}
                  </StepLabel>
                </Step>
              ))}
            </Stepper>

            {getCurrentStep() && (
              <Card variant="outlined" sx={{ mb: 3 }}>
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    현재 단계: {getCurrentStep()?.stepTitle}
                  </Typography>

                  {getCurrentStep()?.stepDescription && (
                    <Typography variant="body2" paragraph>
                      {getCurrentStep()?.stepDescription}
                    </Typography>
                  )}

                  {getCurrentStep()?.detailedInstruction && (
                    <Box sx={{ mb: 2 }}>
                      <Typography variant="subtitle2" gutterBottom>
                        작업 지침:
                      </Typography>
                      <Typography
                        variant="body2"
                        sx={{ whiteSpace: 'pre-line', bgcolor: 'grey.50', p: 2, borderRadius: 1 }}
                      >
                        {getCurrentStep()?.detailedInstruction}
                      </Typography>
                    </Box>
                  )}

                  {getCurrentStep()?.cautionNotes && (
                    <Alert severity="warning" sx={{ mb: 2 }}>
                      <Typography variant="body2">
                        <strong>⚠️ 주의사항:</strong> {getCurrentStep()?.cautionNotes}
                      </Typography>
                    </Alert>
                  )}

                  {getCurrentStep()?.qualityPoints && (
                    <Alert severity="info" sx={{ mb: 2 }}>
                      <Typography variant="body2">
                        <strong>✓ 품질 포인트:</strong> {getCurrentStep()?.qualityPoints}
                      </Typography>
                    </Alert>
                  )}

                  <Box sx={{ display: 'flex', gap: 2, mt: 3 }}>
                    <Button
                      variant="contained"
                      startIcon={<StartIcon />}
                      onClick={() => handleOpenStepDialog(currentStepIndex)}
                    >
                      단계 시작
                    </Button>
                    <Button
                      variant="outlined"
                      color="error"
                      startIcon={<CancelIcon />}
                      onClick={handleOpenCancelDialog}
                    >
                      실행 취소
                    </Button>
                  </Box>
                </CardContent>
              </Card>
            )}
          </CardContent>
        </Card>
      )}

      {/* Start Execution Dialog */}
      <Dialog open={startDialogOpen} onClose={handleCloseStartDialog} maxWidth="sm" fullWidth>
        <DialogTitle>SOP 실행 시작</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12}>
              <Typography variant="body2" gutterBottom>
                SOP: {selectedSOP?.sopName}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                코드: {selectedSOP?.sopCode} (v{selectedSOP?.version})
              </Typography>
              <Typography variant="body2" color="text.secondary">
                단계 수: {selectedSOP?.steps?.length || 0}개
              </Typography>
            </Grid>
            <Grid item xs={12}>
              <Divider />
            </Grid>
            <Grid item xs={12}>
              <FormControl fullWidth>
                <InputLabel>참조 유형 (선택사항)</InputLabel>
                <Select
                  value={startForm.referenceType}
                  label="참조 유형 (선택사항)"
                  onChange={(e) => setStartForm({ ...startForm, referenceType: e.target.value })}
                >
                  <MenuItem value="">없음</MenuItem>
                  <MenuItem value="WORK_ORDER">작업 지시</MenuItem>
                  <MenuItem value="INSPECTION">품질 검사</MenuItem>
                  <MenuItem value="MAINTENANCE">설비 유지보수</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            {startForm.referenceType && (
              <>
                <Grid item xs={12}>
                  <TextField
                    fullWidth
                    label="참조 번호"
                    value={startForm.referenceNo}
                    onChange={(e) => setStartForm({ ...startForm, referenceNo: e.target.value })}
                    placeholder="예: WO-20260125-001"
                  />
                </Grid>
              </>
            )}
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseStartDialog}>취소</Button>
          <Button onClick={handleStartExecution} variant="contained" startIcon={<StartIcon />}>
            실행 시작
          </Button>
        </DialogActions>
      </Dialog>

      {/* Step Execution Dialog */}
      <Dialog open={stepDialogOpen} onClose={handleCloseStepDialog} maxWidth="md" fullWidth>
        <DialogTitle>
          단계 {currentStepIndex + 1}: {getCurrentStep()?.stepTitle}
        </DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            {getCurrentStep()?.detailedInstruction && (
              <Grid item xs={12}>
                <Typography variant="subtitle2" gutterBottom>
                  작업 지침:
                </Typography>
                <Typography
                  variant="body2"
                  sx={{ whiteSpace: 'pre-line', bgcolor: 'grey.50', p: 2, borderRadius: 1 }}
                >
                  {getCurrentStep()?.detailedInstruction}
                </Typography>
              </Grid>
            )}

            {checklist.length > 0 && (
              <Grid item xs={12}>
                <Typography variant="subtitle2" gutterBottom>
                  체크리스트:
                </Typography>
                <List>
                  {checklist.map((item, index) => (
                    <ListItem key={index} dense>
                      <FormControlLabel
                        control={
                          <Checkbox
                            checked={item.checked}
                            onChange={(e) => {
                              const newChecklist = [...checklist];
                              newChecklist[index].checked = e.target.checked;
                              setChecklist(newChecklist);
                            }}
                          />
                        }
                        label={item.item}
                      />
                    </ListItem>
                  ))}
                </List>
              </Grid>
            )}

            <Grid item xs={12}>
              <FormControl fullWidth>
                <InputLabel>결과</InputLabel>
                <Select
                  value={stepResultForm.resultValue}
                  label="결과"
                  onChange={(e) =>
                    setStepResultForm({ ...stepResultForm, resultValue: e.target.value })
                  }
                >
                  <MenuItem value="정상">정상</MenuItem>
                  <MenuItem value="이상">이상</MenuItem>
                  <MenuItem value="N/A">해당없음</MenuItem>
                </Select>
              </FormControl>
            </Grid>

            <Grid item xs={12}>
              <Typography variant="body2" color="text.secondary">
                예상 소요 시간: {getCurrentStep()?.estimatedDuration || 0}분
              </Typography>
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseStepDialog}>취소</Button>
          <Button onClick={handleCompleteStep} variant="contained" startIcon={<CompleteIcon />}>
            단계 완료
          </Button>
        </DialogActions>
      </Dialog>

      {/* Cancel Execution Dialog */}
      <Dialog open={cancelDialogOpen} onClose={handleCloseCancelDialog}>
        <DialogTitle>SOP 실행 취소</DialogTitle>
        <DialogContent>
          <Typography gutterBottom>SOP 실행을 취소하시겠습니까?</Typography>
          <TextField
            fullWidth
            multiline
            rows={3}
            label="취소 사유"
            value={cancelReason}
            onChange={(e) => setCancelReason(e.target.value)}
            sx={{ mt: 2 }}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseCancelDialog}>닫기</Button>
          <Button onClick={handleCancelExecution} color="error" variant="contained">
            취소 확인
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default SOPExecutionPage;
