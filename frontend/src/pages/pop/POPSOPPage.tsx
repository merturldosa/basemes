/**
 * POP SOP (Standard Operating Procedure) Page
 * Step-by-step checklist with verification
 * @author Moon Myung-seop
 */

import { useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  Stepper,
  Step,
  StepLabel,
  StepContent,
  Checkbox,
  FormControlLabel,
  Alert,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Chip,
  LinearProgress,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
} from '@mui/material';
import {
  CheckCircle as CheckIcon,
  Warning as WarningIcon,
  Timer as TimerIcon,
  Edit as SignatureIcon,
  Image as ImageIcon,
  PlayCircle as VideoIcon,
} from '@mui/icons-material';

interface SOPStep {
  id: number;
  title: string;
  description: string;
  isRequired: boolean;
  hasTimer?: boolean;
  timerDuration?: number;
  hasImage?: boolean;
  hasVideo?: boolean;
  safetyWarning?: string;
  checkpoints: string[];
}

interface SOPTemplate {
  sopId: number;
  sopNo: string;
  title: string;
  version: string;
  category: string;
  steps: SOPStep[];
}

const POPSOPPage: React.FC = () => {
  const [selectedSOP, setSelectedSOP] = useState<SOPTemplate | null>(null);
  const [activeStep, setActiveStep] = useState(0);
  const [completedSteps, setCompletedSteps] = useState<Set<number>>(new Set());
  const [checkpointStatus, setCheckpointStatus] = useState<{ [key: string]: boolean }>({});
  const [openSignatureDialog, setOpenSignatureDialog] = useState(false);
  const [signature, setSignature] = useState('');
  const [timer, setTimer] = useState<number | null>(null);

  // Mock SOP data
  const sopTemplates: SOPTemplate[] = [
    {
      sopId: 1,
      sopNo: 'SOP-001',
      title: '생산 설비 가동 절차',
      version: 'v1.0',
      category: '설비 운영',
      steps: [
        {
          id: 1,
          title: '작업 전 안전 점검',
          description: '설비 가동 전 필수 안전 점검 항목을 확인합니다',
          isRequired: true,
          safetyWarning: '안전 장구를 반드시 착용하세요',
          checkpoints: [
            '보호구 착용 확인 (안전모, 안전화, 보호안경)',
            '설비 주변 정리정돈 상태 확인',
            '비상정지 버튼 작동 확인',
          ],
        },
        {
          id: 2,
          title: '설비 전원 투입',
          description: '설비의 전원을 순서대로 투입합니다',
          isRequired: true,
          hasImage: true,
          checkpoints: [
            '메인 전원 스위치 ON',
            '제어 패널 전원 확인',
            '경고등 정상 작동 확인',
          ],
        },
        {
          id: 3,
          title: '초기 가동 및 워밍업',
          description: '설비를 워밍업하고 정상 작동을 확인합니다',
          isRequired: true,
          hasTimer: true,
          timerDuration: 300, // 5 minutes
          checkpoints: [
            '공회전 5분 실시',
            '온도 상승 확인',
            '이상 소음 여부 확인',
            '압력 게이지 정상 범위 확인',
          ],
        },
        {
          id: 4,
          title: '생산 준비 완료',
          description: '생산을 시작할 준비가 완료되었습니다',
          isRequired: true,
          checkpoints: ['모든 점검 항목 이상 없음', '작업 지시서 확인 완료'],
        },
      ],
    },
    {
      sopId: 2,
      sopNo: 'SOP-002',
      title: '제품 품질 검사 절차',
      version: 'v1.0',
      category: '품질 관리',
      steps: [
        {
          id: 1,
          title: '외관 검사',
          description: '제품의 외관 상태를 육안으로 검사합니다',
          isRequired: true,
          hasImage: true,
          checkpoints: ['표면 손상 여부', '색상 균일성', '형상 정확도'],
        },
        {
          id: 2,
          title: '치수 측정',
          description: '지정된 치수를 측정하고 기록합니다',
          isRequired: true,
          checkpoints: ['길이 측정', '두께 측정', '중량 측정'],
        },
      ],
    },
  ];

  const handleSelectSOP = (sop: SOPTemplate) => {
    setSelectedSOP(sop);
    setActiveStep(0);
    setCompletedSteps(new Set());
    setCheckpointStatus({});
    setTimer(null);
  };

  const handleCheckpointChange = (stepId: number, checkpointIndex: number) => {
    const key = `${stepId}-${checkpointIndex}`;
    setCheckpointStatus((prev) => ({
      ...prev,
      [key]: !prev[key],
    }));
  };

  const isStepComplete = (stepId: number, checkpoints: string[]) => {
    return checkpoints.every((_, index) => checkpointStatus[`${stepId}-${index}`]);
  };

  const handleNext = () => {
    if (selectedSOP) {
      const currentStep = selectedSOP.steps[activeStep];
      if (currentStep.isRequired && !isStepComplete(currentStep.id, currentStep.checkpoints)) {
        return; // Cannot proceed if required checkpoints not completed
      }

      setCompletedSteps((prev) => new Set([...prev, currentStep.id]));
      setActiveStep((prev) => prev + 1);
    }
  };

  const handleBack = () => {
    setActiveStep((prev) => prev - 1);
  };

  const handleComplete = () => {
    setOpenSignatureDialog(true);
  };

  const handleSign = () => {
    if (signature.trim()) {
      // Submit SOP completion with signature
      console.log('SOP completed with signature:', signature);
      setOpenSignatureDialog(false);
      // Reset
      setSelectedSOP(null);
      setSignature('');
    }
  };

  const getProgressPercentage = () => {
    if (!selectedSOP) return 0;
    return (completedSteps.size / selectedSOP.steps.length) * 100;
  };

  // No SOP selected
  if (!selectedSOP) {
    return (
      <Box sx={{ p: 3 }}>
        <Typography variant="h4" gutterBottom fontWeight="bold">
          SOP 체크리스트
        </Typography>
        <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
          실행할 표준 작업 절차를 선택하세요
        </Typography>

        {sopTemplates.map((sop) => (
          <Card
            key={sop.sopId}
            sx={{
              mb: 2,
              cursor: 'pointer',
              '&:hover': { boxShadow: 4 },
              borderLeft: 4,
              borderColor: 'primary.main',
            }}
            onClick={() => handleSelectSOP(sop)}
          >
            <CardContent>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start', mb: 1 }}>
                <Box>
                  <Typography variant="h6" fontWeight="bold">
                    {sop.sopNo} - {sop.title}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    버전: {sop.version}
                  </Typography>
                </Box>
                <Chip label={sop.category} size="small" color="primary" />
              </Box>
              <Typography variant="body2" color="text.secondary">
                총 {sop.steps.length}단계
              </Typography>
            </CardContent>
          </Card>
        ))}
      </Box>
    );
  }

  // SOP selected - show checklist
  return (
    <Box sx={{ p: 3 }}>
      {/* Header */}
      <Box sx={{ mb: 3 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start', mb: 2 }}>
          <Box>
            <Typography variant="h5" fontWeight="bold">
              {selectedSOP.sopNo} - {selectedSOP.title}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              버전: {selectedSOP.version} • {selectedSOP.category}
            </Typography>
          </Box>
          <Button variant="outlined" onClick={() => setSelectedSOP(null)}>
            목록으로
          </Button>
        </Box>

        {/* Progress */}
        <Card sx={{ bgcolor: 'grey.50' }}>
          <CardContent>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
              <Typography variant="body2" fontWeight="medium">
                진행률
              </Typography>
              <Typography variant="body2" fontWeight="bold" color="primary">
                {completedSteps.size} / {selectedSOP.steps.length} 단계 완료
              </Typography>
            </Box>
            <LinearProgress
              variant="determinate"
              value={getProgressPercentage()}
              sx={{ height: 10, borderRadius: 1 }}
            />
          </CardContent>
        </Card>
      </Box>

      {/* Stepper */}
      <Stepper activeStep={activeStep} orientation="vertical">
        {selectedSOP.steps.map((step, index) => (
          <Step key={step.id} completed={completedSteps.has(step.id)}>
            <StepLabel
              optional={
                step.isRequired && (
                  <Chip label="필수" size="small" color="error" sx={{ mt: 0.5 }} />
                )
              }
            >
              <Typography variant="h6" fontWeight="bold">
                {step.title}
              </Typography>
            </StepLabel>
            <StepContent>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                {step.description}
              </Typography>

              {/* Safety Warning */}
              {step.safetyWarning && (
                <Alert severity="warning" icon={<WarningIcon />} sx={{ mb: 2 }}>
                  {step.safetyWarning}
                </Alert>
              )}

              {/* Media Badges */}
              <Box sx={{ display: 'flex', gap: 1, mb: 2 }}>
                {step.hasImage && (
                  <Chip icon={<ImageIcon />} label="이미지 가이드" size="small" variant="outlined" />
                )}
                {step.hasVideo && (
                  <Chip icon={<VideoIcon />} label="동영상 가이드" size="small" variant="outlined" />
                )}
                {step.hasTimer && (
                  <Chip
                    icon={<TimerIcon />}
                    label={`${step.timerDuration! / 60}분 소요`}
                    size="small"
                    variant="outlined"
                    color="primary"
                  />
                )}
              </Box>

              {/* Checkpoints */}
              <Card sx={{ mb: 2, bgcolor: 'background.default' }}>
                <CardContent>
                  <Typography variant="subtitle2" fontWeight="bold" gutterBottom>
                    체크 항목
                  </Typography>
                  <List dense>
                    {step.checkpoints.map((checkpoint, cpIndex) => (
                      <ListItem key={cpIndex} disablePadding>
                        <FormControlLabel
                          control={
                            <Checkbox
                              checked={checkpointStatus[`${step.id}-${cpIndex}`] || false}
                              onChange={() => handleCheckpointChange(step.id, cpIndex)}
                              color="success"
                            />
                          }
                          label={
                            <Typography variant="body2">{checkpoint}</Typography>
                          }
                        />
                      </ListItem>
                    ))}
                  </List>
                </CardContent>
              </Card>

              {/* Action Buttons */}
              <Box sx={{ display: 'flex', gap: 1 }}>
                <Button
                  disabled={index === 0}
                  onClick={handleBack}
                  variant="outlined"
                >
                  이전
                </Button>
                {index === selectedSOP.steps.length - 1 ? (
                  <Button
                    variant="contained"
                    onClick={handleComplete}
                    disabled={!isStepComplete(step.id, step.checkpoints)}
                    startIcon={<CheckIcon />}
                  >
                    완료
                  </Button>
                ) : (
                  <Button
                    variant="contained"
                    onClick={handleNext}
                    disabled={step.isRequired && !isStepComplete(step.id, step.checkpoints)}
                  >
                    다음
                  </Button>
                )}
              </Box>
            </StepContent>
          </Step>
        ))}
      </Stepper>

      {/* Completion Message */}
      {completedSteps.size === selectedSOP.steps.length && (
        <Alert severity="success" sx={{ mt: 3 }}>
          <Typography variant="body1" fontWeight="bold">
            모든 단계가 완료되었습니다!
          </Typography>
          <Typography variant="body2">
            서명하여 작업을 완료하세요
          </Typography>
        </Alert>
      )}

      {/* Signature Dialog */}
      <Dialog open={openSignatureDialog} onClose={() => setOpenSignatureDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>작업 완료 서명</DialogTitle>
        <DialogContent>
          <Alert severity="info" sx={{ mb: 2 }}>
            SOP 절차를 완료했습니다. 서명하여 확인해주세요.
          </Alert>
          <TextField
            fullWidth
            label="서명 (이름 입력)"
            value={signature}
            onChange={(e) => setSignature(e.target.value)}
            placeholder="홍길동"
            sx={{ mb: 2 }}
          />
          <Box
            sx={{
              border: '2px dashed',
              borderColor: 'grey.400',
              borderRadius: 1,
              p: 3,
              textAlign: 'center',
              bgcolor: 'grey.50',
            }}
          >
            <SignatureIcon sx={{ fontSize: 48, color: 'grey.400', mb: 1 }} />
            <Typography variant="body2" color="text.secondary">
              디지털 서명 패드
            </Typography>
            <Typography variant="caption" color="text.secondary">
              (실제 구현 시 서명 캔버스)
            </Typography>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenSignatureDialog(false)}>취소</Button>
          <Button onClick={handleSign} variant="contained" disabled={!signature.trim()}>
            서명 완료
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default POPSOPPage;
