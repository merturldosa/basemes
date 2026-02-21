import React from 'react';
import {
  Card,
  CardContent,
  CardActions,
  Typography,
  Box,
  Chip,
  LinearProgress,
  Button,
  IconButton,
  Divider,
} from '@mui/material';
import {
  PlayArrow as StartIcon,
  Pause as PauseIcon,
  CheckCircle as CompleteIcon,
  MoreVert as MoreIcon,
  Person as PersonIcon,
  Category as ProductIcon,
} from '@mui/icons-material';
import { useTranslation } from 'react-i18next';

/**
 * Work Order Card Component
 * Display work order information with actions
 * @author Moon Myung-seop
 */

interface WorkOrderCardProps {
  workOrder: {
    workOrderId: number;
    workOrderNo: string;
    productCode: string;
    productName: string;
    processName?: string;
    plannedQuantity: number;
    actualQuantity: number;
    goodQuantity: number;
    defectQuantity: number;
    status: 'PENDING' | 'READY' | 'IN_PROGRESS' | 'PAUSED' | 'COMPLETED' | 'CANCELLED';
    assignedUserName?: string;
    plannedStartDate?: string;
    actualStartDate?: string;
  };
  onStart?: (id: number) => void;
  onResume?: (id: number) => void;
  onPause?: (id: number) => void;
  onComplete?: (id: number) => void;
  onClick?: (id: number) => void;
}

const WorkOrderCard: React.FC<WorkOrderCardProps> = ({
  workOrder,
  onStart,
  onResume,
  onPause,
  onComplete,
  onClick,
}) => {
  const { t } = useTranslation();
  const {
    workOrderId,
    workOrderNo,
    productCode,
    productName,
    processName,
    plannedQuantity,
    actualQuantity,
    goodQuantity,
    defectQuantity,
    status,
    assignedUserName,
  } = workOrder;

  // Calculate progress
  const progressRate = plannedQuantity > 0
    ? Math.round((actualQuantity / plannedQuantity) * 100)
    : 0;

  const defectRate = actualQuantity > 0
    ? ((defectQuantity / actualQuantity) * 100).toFixed(1)
    : '0.0';

  // Status colors and labels
  const statusConfig = {
    PENDING: { color: 'default', label: t('workOrder.status.pending'), bgcolor: '#9e9e9e' },
    READY: { color: 'info', label: t('workOrder.status.ready'), bgcolor: '#2196f3' },
    IN_PROGRESS: { color: 'success', label: t('workOrder.status.inProgress'), bgcolor: '#4caf50' },
    PAUSED: { color: 'warning', label: t('workOrder.status.paused'), bgcolor: '#ff9800' },
    COMPLETED: { color: 'success', label: t('workOrder.status.completed'), bgcolor: '#66bb6a' },
    CANCELLED: { color: 'error', label: t('workOrder.status.cancelled'), bgcolor: '#f44336' },
  } as const;

  const currentStatus = statusConfig[status] || statusConfig.PENDING;

  // Determine which action button to show
  const getActionButton = () => {
    if (status === 'READY' || status === 'PENDING') {
      return (
        <Button
          variant="contained"
          color="success"
          startIcon={<StartIcon />}
          onClick={(e) => {
            e.stopPropagation();
            onStart?.(workOrderId);
          }}
          size="large"
          fullWidth
        >
          {t('workOrder.actions.start')}
        </Button>
      );
    }

    if (status === 'PAUSED') {
      return (
        <Button
          variant="contained"
          color="primary"
          startIcon={<StartIcon />}
          onClick={(e) => {
            e.stopPropagation();
            onResume?.(workOrderId);
          }}
          size="large"
          fullWidth
        >
          {t('workOrder.actions.resume')}
        </Button>
      );
    }

    if (status === 'IN_PROGRESS') {
      return (
        <Box sx={{ display: 'flex', gap: 1, width: '100%' }}>
          <Button
            variant="outlined"
            color="warning"
            startIcon={<PauseIcon />}
            onClick={(e) => {
              e.stopPropagation();
              onPause?.(workOrderId);
            }}
            size="large"
            sx={{ flex: 1 }}
          >
            {t('workOrder.actions.pause')}
          </Button>
          <Button
            variant="contained"
            color="success"
            startIcon={<CompleteIcon />}
            onClick={(e) => {
              e.stopPropagation();
              onComplete?.(workOrderId);
            }}
            size="large"
            sx={{ flex: 1 }}
          >
            {t('workOrder.actions.complete')}
          </Button>
        </Box>
      );
    }

    return null;
  };

  return (
    <Card
      sx={{
        cursor: onClick ? 'pointer' : 'default',
        transition: 'all 0.2s',
        '&:hover': onClick ? {
          boxShadow: 4,
          transform: 'translateY(-2px)',
        } : {},
        border: status === 'IN_PROGRESS' ? '2px solid #4caf50' : 'none',
      }}
      onClick={() => onClick?.(workOrderId)}
    >
      <CardContent sx={{ pb: 1 }}>
        {/* Header */}
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 2 }}>
          <Box>
            <Typography variant="h6" fontWeight="bold" sx={{ mb: 0.5 }}>
              {workOrderNo}
            </Typography>
            <Chip
              label={currentStatus.label}
              size="small"
              sx={{
                bgcolor: currentStatus.bgcolor,
                color: 'white',
                fontWeight: 600,
              }}
            />
          </Box>
          <IconButton size="small">
            <MoreIcon />
          </IconButton>
        </Box>

        {/* Product Info */}
        <Box sx={{ mb: 2 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 0.5 }}>
            <ProductIcon fontSize="small" color="action" />
            <Typography variant="body2" color="text.secondary">
              {productCode}
            </Typography>
          </Box>
          <Typography variant="body1" fontWeight={600}>
            {productName}
          </Typography>
          {processName && (
            <Typography variant="caption" color="text.secondary">
              {t('workOrder.labels.process')}: {processName}
            </Typography>
          )}
        </Box>

        <Divider sx={{ my: 2 }} />

        {/* Progress */}
        <Box sx={{ mb: 2 }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
            <Typography variant="body2" color="text.secondary">
              {t('workOrder.labels.progressRate')}
            </Typography>
            <Typography variant="body2" fontWeight="bold">
              {actualQuantity} / {plannedQuantity} ({progressRate}%)
            </Typography>
          </Box>
          <LinearProgress
            variant="determinate"
            value={progressRate}
            sx={{
              height: 8,
              borderRadius: 4,
              bgcolor: 'grey.200',
              '& .MuiLinearProgress-bar': {
                bgcolor: progressRate >= 100 ? 'success.main' : 'primary.main',
              },
            }}
          />
        </Box>

        {/* Quality Info */}
        <Box sx={{ display: 'flex', gap: 2, mb: 2 }}>
          <Box sx={{ flex: 1 }}>
            <Typography variant="caption" color="text.secondary">
              {t('workOrder.labels.goodQuantity')}
            </Typography>
            <Typography variant="h6" color="success.main" fontWeight="bold">
              {goodQuantity}
            </Typography>
          </Box>
          <Box sx={{ flex: 1 }}>
            <Typography variant="caption" color="text.secondary">
              {t('workOrder.labels.defectQuantity')}
            </Typography>
            <Typography variant="h6" color="error.main" fontWeight="bold">
              {defectQuantity}
            </Typography>
          </Box>
          <Box sx={{ flex: 1 }}>
            <Typography variant="caption" color="text.secondary">
              {t('workOrder.labels.defectRate')}
            </Typography>
            <Typography variant="h6" fontWeight="bold">
              {defectRate}%
            </Typography>
          </Box>
        </Box>

        {/* Operator Info */}
        {assignedUserName && (
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <PersonIcon fontSize="small" color="action" />
            <Typography variant="caption" color="text.secondary">
              {t('workOrder.labels.operator')}: {assignedUserName}
            </Typography>
          </Box>
        )}
      </CardContent>

      {/* Actions */}
      {getActionButton() && (
        <CardActions sx={{ p: 2, pt: 0 }}>
          {getActionButton()}
        </CardActions>
      )}
    </Card>
  );
};

export default WorkOrderCard;
