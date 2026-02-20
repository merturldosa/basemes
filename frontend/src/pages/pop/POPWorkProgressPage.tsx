/**
 * POP Work Progress Page
 * Real-time monitoring of all work orders in progress
 * Displays current production status, operator performance, and line efficiency
 * @author Moon Myung-seop
 */

import React, { useEffect, useState, useCallback, useRef } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Grid,
  LinearProgress,
  Chip,
  Avatar,
  List,
  ListItem,
  ListItemAvatar,
  ListItemText,
  Divider,
  Alert,
  IconButton,
} from '@mui/material';
import {
  TrendingUp as TrendingUpIcon,
  CheckCircle as CheckIcon,
  Warning as WarningIcon,
  Person as PersonIcon,
  Refresh as RefreshIcon,
  Timer as TimerIcon,
} from '@mui/icons-material';
import axios from 'axios';

interface WorkProgress {
  workOrderId: number;
  workOrderNo: string;
  productName: string;
  productCode: string;
  operatorName: string;
  targetQuantity: number;
  producedQuantity: number;
  defectQuantity: number;
  progress: number;
  status: string;
  startTime: string;
  elapsedTime: string;
}

interface ProductionStatistics {
  date: string;
  totalProduced: number;
  totalDefects: number;
  completedOrders: number;
  defectRate: number;
}

const POPWorkProgressPage: React.FC = () => {
  const [workProgressList, setWorkProgressList] = useState<WorkProgress[]>([]);
  const [statistics, setStatistics] = useState<ProductionStatistics | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null);

  const fetchWorkOrders = useCallback(async () => {
    try {
      const response = await axios.get('/api/pop/work-orders/active');
      const workOrdersData = response.data;
      if (workOrdersData?.data) {
        const progressList: WorkProgress[] = workOrdersData.data.map((wo: any) => ({
          workOrderId: wo.workOrderId,
          workOrderNo: wo.workOrderNo,
          productName: wo.productName || 'Unknown Product',
          productCode: wo.productCode || '',
          operatorName: wo.operatorName || '작업자',
          targetQuantity: wo.targetQuantity || 0,
          producedQuantity: wo.producedQuantity || 0,
          defectQuantity: wo.defectQuantity || 0,
          progress: wo.targetQuantity > 0 ? (wo.producedQuantity / wo.targetQuantity) * 100 : 0,
          status: wo.status || 'UNKNOWN',
          startTime: wo.actualStartDate || wo.plannedStartDate || '',
          elapsedTime: calculateElapsedTime(wo.actualStartDate),
        }));
        setWorkProgressList(progressList);
      }
    } catch {
      // API may not exist yet - show empty state
    } finally {
      setIsLoading(false);
    }
  }, []);

  const fetchStatistics = useCallback(async () => {
    try {
      const response = await axios.get('/api/pop/statistics/today');
      if (response.data?.data) {
        setStatistics(response.data.data);
      }
    } catch {
      // API may not exist yet
    }
  }, []);

  const refetch = useCallback(() => {
    fetchWorkOrders();
    fetchStatistics();
  }, [fetchWorkOrders, fetchStatistics]);

  useEffect(() => {
    fetchWorkOrders();
    fetchStatistics();
    intervalRef.current = setInterval(() => {
      fetchWorkOrders();
      fetchStatistics();
    }, 5000);
    return () => {
      if (intervalRef.current) clearInterval(intervalRef.current);
    };
  }, [fetchWorkOrders, fetchStatistics]);

  const calculateElapsedTime = (startTime: string | null): string => {
    if (!startTime) return '00:00:00';

    const start = new Date(startTime);
    const now = new Date();
    const diffMs = now.getTime() - start.getTime();

    const hours = Math.floor(diffMs / (1000 * 60 * 60));
    const minutes = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60));
    const seconds = Math.floor((diffMs % (1000 * 60)) / 1000);

    return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
  };

  const getProgressColor = (progress: number): 'success' | 'warning' | 'error' => {
    if (progress >= 80) return 'success';
    if (progress >= 50) return 'warning';
    return 'error';
  };

  const getStatusColor = (status: string): 'success' | 'warning' | 'error' | 'info' => {
    switch (status) {
      case 'IN_PROGRESS':
        return 'success';
      case 'PAUSED':
        return 'warning';
      case 'READY':
        return 'info';
      default:
        return 'error';
    }
  };

  const getStatusLabel = (status: string): string => {
    switch (status) {
      case 'IN_PROGRESS':
        return '진행중';
      case 'PAUSED':
        return '일시정지';
      case 'READY':
        return '대기';
      case 'COMPLETED':
        return '완료';
      default:
        return status;
    }
  };

  // Calculate summary statistics from current work progress
  const activeWorkOrders = workProgressList.filter((wp) => wp.status === 'IN_PROGRESS').length;
  const totalProduced = statistics?.totalProduced || 0;
  const totalDefects = statistics?.totalDefects || 0;
  const defectRate = totalProduced > 0 ? (totalDefects / totalProduced) * 100 : 0;

  return (
    <Box sx={{ p: 3 }}>
      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box>
          <Typography variant="h4" gutterBottom fontWeight="bold">
            작업 진행 현황
          </Typography>
          <Typography variant="body1" color="text.secondary">
            모든 작업 지시의 실시간 진행 상황
          </Typography>
        </Box>
        <IconButton onClick={() => refetch()} color="primary">
          <RefreshIcon />
        </IconButton>
      </Box>

      {/* Summary Cards */}
      <Grid container spacing={2} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={4}>
          <Card sx={{ bgcolor: 'primary.main', color: 'white' }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                <TrendingUpIcon sx={{ mr: 1 }} />
                <Typography variant="subtitle2">진행 중</Typography>
              </Box>
              <Typography variant="h3" fontWeight="bold">
                {activeWorkOrders}
              </Typography>
              <Typography variant="body2">작업 지시</Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={4}>
          <Card sx={{ bgcolor: 'success.main', color: 'white' }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                <CheckIcon sx={{ mr: 1 }} />
                <Typography variant="subtitle2">금일 생산</Typography>
              </Box>
              <Typography variant="h3" fontWeight="bold">
                {totalProduced.toLocaleString()}
              </Typography>
              <Typography variant="body2">EA</Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={4}>
          <Card sx={{ bgcolor: defectRate > 5 ? 'error.main' : 'warning.main', color: 'white' }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                <WarningIcon sx={{ mr: 1 }} />
                <Typography variant="subtitle2">불량</Typography>
              </Box>
              <Typography variant="h3" fontWeight="bold">
                {totalDefects.toLocaleString()}
              </Typography>
              <Typography variant="body2">({defectRate.toFixed(1)}%)</Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Work Progress List */}
      <Card>
        <CardContent>
          <Typography variant="h6" gutterBottom fontWeight="bold">
            작업 지시 목록 ({workProgressList.length})
          </Typography>

          {isLoading ? (
            <Box sx={{ py: 4, textAlign: 'center' }}>
              <Typography>로딩 중...</Typography>
            </Box>
          ) : workProgressList.length === 0 ? (
            <Alert severity="info" sx={{ mt: 2 }}>
              현재 진행 중인 작업이 없습니다
            </Alert>
          ) : (
            <List>
              {workProgressList.map((wp, index) => (
                <React.Fragment key={wp.workOrderId}>
                  {index > 0 && <Divider />}
                  <ListItem alignItems="flex-start" sx={{ py: 2 }}>
                    <ListItemAvatar>
                      <Avatar sx={{ bgcolor: 'primary.main', width: 56, height: 56 }}>
                        <PersonIcon fontSize="large" />
                      </Avatar>
                    </ListItemAvatar>
                    <ListItemText
                      primary={
                        <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                          <Box>
                            <Typography variant="h6" fontWeight="bold">
                              {wp.workOrderNo}
                            </Typography>
                            <Typography variant="body2" color="text.secondary">
                              {wp.productCode} - {wp.productName}
                            </Typography>
                          </Box>
                          <Chip
                            label={getStatusLabel(wp.status)}
                            color={getStatusColor(wp.status)}
                            size="small"
                          />
                        </Box>
                      }
                      secondary={
                        <Box>
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 1 }}>
                            <Box sx={{ display: 'flex', alignItems: 'center' }}>
                              <PersonIcon fontSize="small" sx={{ mr: 0.5, color: 'text.secondary' }} />
                              <Typography variant="body2" color="text.secondary">
                                {wp.operatorName}
                              </Typography>
                            </Box>
                            <Box sx={{ display: 'flex', alignItems: 'center' }}>
                              <TimerIcon fontSize="small" sx={{ mr: 0.5, color: 'text.secondary' }} />
                              <Typography variant="body2" color="text.secondary">
                                {wp.elapsedTime}
                              </Typography>
                            </Box>
                          </Box>

                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 1 }}>
                            <Typography variant="h6" color="primary" fontWeight="bold">
                              {wp.producedQuantity.toLocaleString()}
                            </Typography>
                            <Typography variant="body2" color="text.secondary">
                              / {wp.targetQuantity.toLocaleString()} EA
                            </Typography>
                            {wp.defectQuantity > 0 && (
                              <Typography variant="body2" color="error.main" fontWeight="bold">
                                (불량: {wp.defectQuantity})
                              </Typography>
                            )}
                          </Box>

                          <Box sx={{ mb: 0.5 }}>
                            <LinearProgress
                              variant="determinate"
                              value={Math.min(wp.progress, 100)}
                              color={getProgressColor(wp.progress)}
                              sx={{ height: 8, borderRadius: 1 }}
                            />
                          </Box>
                          <Typography variant="caption" color="text.secondary">
                            {wp.progress.toFixed(1)}% 완료
                          </Typography>
                        </Box>
                      }
                    />
                  </ListItem>
                </React.Fragment>
              ))}
            </List>
          )}
        </CardContent>
      </Card>

      {/* Auto-refresh indicator */}
      <Box sx={{ mt: 2, textAlign: 'center' }}>
        <Typography variant="caption" color="text.secondary">
          🔄 5초마다 자동 새로고침
        </Typography>
      </Box>
    </Box>
  );
};

export default POPWorkProgressPage;
