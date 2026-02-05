import React, { useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  IconButton,
  Collapse,
  TextField,
  Button,
  Chip,
} from '@mui/material';
import {
  ExpandMore as ExpandIcon,
  CheckCircle as PassIcon,
  Cancel as FailIcon,
  Notes as NotesIcon,
} from '@mui/icons-material';

/**
 * SOP Checklist Item Component
 * Simplified SOP step with Pass/Fail toggle
 * @author Moon Myung-seop
 */

interface SOPChecklistItemProps {
  step: {
    stepId: number;
    stepNumber: number;
    stepTitle: string;
    stepDescription?: string;
    isRequired: boolean;
    isCritical: boolean;
    executionStepId?: number;
    executionStatus?: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'SKIPPED' | 'FAILED';
    checkResult?: boolean;
    notes?: string;
  };
  onComplete?: (stepId: number, passed: boolean, notes?: string) => void;
  disabled?: boolean;
}

const SOPChecklistItem: React.FC<SOPChecklistItemProps> = ({
  step,
  onComplete,
  disabled = false,
}) => {
  const [expanded, setExpanded] = useState(false);
  const [showNotes, setShowNotes] = useState(false);
  const [notes, setNotes] = useState(step.notes || '');
  const [result, setResult] = useState<boolean | null>(
    step.checkResult !== undefined ? step.checkResult : null
  );

  const isCompleted = step.executionStatus === 'COMPLETED';

  const handleToggleComplete = (passed: boolean) => {
    if (disabled || isCompleted) return;

    setResult(passed);

    // Auto-submit if no notes required, otherwise show notes field
    if (passed) {
      onComplete?.(step.executionStepId || step.stepId, passed);
    } else {
      setShowNotes(true);
    }
  };

  const handleSubmitWithNotes = () => {
    if (result !== null) {
      onComplete?.(step.executionStepId || step.stepId, result, notes || undefined);
      setShowNotes(false);
    }
  };

  // Determine card border color based on status
  const getBorderColor = () => {
    if (isCompleted && result === true) return 'success.main';
    if (isCompleted && result === false) return 'error.main';
    if (step.isCritical) return 'warning.main';
    return 'divider';
  };

  return (
    <Card
      sx={{
        mb: 2,
        border: 2,
        borderColor: getBorderColor(),
        bgcolor: isCompleted ? 'action.hover' : 'background.paper',
      }}
    >
      <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
        {/* Header */}
        <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 2 }}>
          {/* Step Number */}
          <Box
            sx={{
              minWidth: 40,
              height: 40,
              borderRadius: '50%',
              bgcolor: isCompleted ? 'success.main' : 'primary.main',
              color: 'white',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontWeight: 'bold',
              fontSize: '1.1rem',
            }}
          >
            {step.stepNumber}
          </Box>

          {/* Content */}
          <Box sx={{ flex: 1 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 0.5 }}>
              <Typography variant="h6" fontWeight="bold">
                {step.stepTitle}
              </Typography>
              {step.isRequired && (
                <Chip label="필수" size="small" color="error" />
              )}
              {step.isCritical && (
                <Chip label="중요" size="small" color="warning" />
              )}
            </Box>

            {step.stepDescription && (
              <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                {step.stepDescription}
              </Typography>
            )}

            {/* Pass/Fail Buttons */}
            {!isCompleted && (
              <Box sx={{ display: 'flex', gap: 2, mt: 2 }}>
                <Button
                  variant={result === true ? 'contained' : 'outlined'}
                  color="success"
                  startIcon={<PassIcon />}
                  onClick={() => handleToggleComplete(true)}
                  disabled={disabled}
                  size="large"
                  sx={{ flex: 1, minHeight: 60 }}
                >
                  통과 (Pass)
                </Button>
                <Button
                  variant={result === false ? 'contained' : 'outlined'}
                  color="error"
                  startIcon={<FailIcon />}
                  onClick={() => handleToggleComplete(false)}
                  disabled={disabled}
                  size="large"
                  sx={{ flex: 1, minHeight: 60 }}
                >
                  실패 (Fail)
                </Button>
              </Box>
            )}

            {/* Completed Status */}
            {isCompleted && (
              <Box sx={{ mt: 2 }}>
                <Chip
                  icon={result ? <PassIcon /> : <FailIcon />}
                  label={result ? '통과 완료' : '실패 처리됨'}
                  color={result ? 'success' : 'error'}
                  variant="filled"
                />
                {step.notes && (
                  <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                    메모: {step.notes}
                  </Typography>
                )}
              </Box>
            )}

            {/* Notes Input (shown when Fail is selected) */}
            {showNotes && !isCompleted && (
              <Collapse in={showNotes}>
                <Box sx={{ mt: 2, p: 2, bgcolor: 'error.lighter', borderRadius: 1 }}>
                  <Typography variant="subtitle2" fontWeight="bold" sx={{ mb: 1 }}>
                    실패 사유 입력
                  </Typography>
                  <TextField
                    fullWidth
                    multiline
                    rows={3}
                    value={notes}
                    onChange={(e) => setNotes(e.target.value)}
                    placeholder="실패 사유 또는 조치 사항을 입력하세요"
                    sx={{ mb: 1 }}
                  />
                  <Box sx={{ display: 'flex', gap: 1 }}>
                    <Button
                      variant="outlined"
                      onClick={() => {
                        setShowNotes(false);
                        setResult(null);
                        setNotes('');
                      }}
                      fullWidth
                    >
                      취소
                    </Button>
                    <Button
                      variant="contained"
                      color="error"
                      onClick={handleSubmitWithNotes}
                      disabled={!notes.trim()}
                      fullWidth
                    >
                      확인
                    </Button>
                  </Box>
                </Box>
              </Collapse>
            )}

            {/* Add Notes Button (for Pass) */}
            {result === true && !isCompleted && !showNotes && (
              <Button
                startIcon={<NotesIcon />}
                onClick={() => setShowNotes(true)}
                size="small"
                sx={{ mt: 1 }}
              >
                메모 추가 (선택)
              </Button>
            )}
          </Box>

          {/* Expand Button (for detailed description) */}
          {step.stepDescription && step.stepDescription.length > 100 && (
            <IconButton
              onClick={() => setExpanded(!expanded)}
              sx={{
                transform: expanded ? 'rotate(180deg)' : 'rotate(0deg)',
                transition: 'transform 0.3s',
              }}
            >
              <ExpandIcon />
            </IconButton>
          )}
        </Box>

        {/* Expanded Description */}
        {step.stepDescription && step.stepDescription.length > 100 && (
          <Collapse in={expanded}>
            <Box sx={{ mt: 2, pl: 7 }}>
              <Typography variant="body2">
                {step.stepDescription}
              </Typography>
            </Box>
          </Collapse>
        )}
      </CardContent>
    </Card>
  );
};

export default SOPChecklistItem;
