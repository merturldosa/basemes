/**
 * POP Performance Page
 * Production performance tracking and statistics
 * @author Moon Myung-seop
 */

import { useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Grid,
  Chip,
  List,
  ListItem,
  ListItemText,
  Divider,
  Tabs,
  Tab,
  LinearProgress,
} from '@mui/material';
import {
  TrendingUp as TrendingUpIcon,
  CheckCircle as SuccessIcon,
  Cancel as DefectIcon,
  Speed as EfficiencyIcon,
  Timer as TimeIcon,
} from '@mui/icons-material';

interface PerformanceData {
  shift: string;
  target: number;
  produced: number;
  defect: number;
  efficiency: number;
  workOrders: number;
}

interface WorkHistory {
  workOrderNo: string;
  productName: string;
  produced: number;
  target: number;
  defect: number;
  startTime: string;
  endTime: string;
  duration: string;
}

const POPPerformancePage: React.FC = () => {
  const [activeTab, setActiveTab] = useState(0);

  // Mock data
  const todayPerformance: PerformanceData = {
    shift: '주간',
    target: 1000,
    produced: 750,
    defect: 25,
    efficiency: 75,
    workOrders: 3,
  };

  const weekPerformance: PerformanceData[] = [
    { shift: '월요일', target: 1000, produced: 980, defect: 20, efficiency: 98, workOrders: 4 },
    { shift: '화요일', target: 1000, produced: 950, defect: 30, efficiency: 95, workOrders: 4 },
    { shift: '수요일', target: 1000, produced: 1020, defect: 15, efficiency: 102, workOrders: 5 },
    { shift: '목요일', target: 1000, produced: 900, defect: 25, efficiency: 90, workOrders: 3 },
    { shift: '금요일', target: 1000, produced: 750, defect: 25, efficiency: 75, workOrders: 3 },
  ];

  const workHistory: WorkHistory[] = [
    {
      workOrderNo: 'WO-20260204-001',
      productName: '제품 A',
      produced: 500,
      target: 500,
      defect: 10,
      startTime: '08:00',
      endTime: '12:30',
      duration: '4h 30m',
    },
    {
      workOrderNo: 'WO-20260204-002',
      productName: '제품 B',
      produced: 250,
      target: 300,
      defect: 15,
      startTime: '13:00',
      endTime: '16:00',
      duration: '3h 0m',
    },
  ];

  const getEfficiencyColor = (efficiency: number) => {
    if (efficiency >= 100) return 'success';
    if (efficiency >= 80) return 'info';
    if (efficiency >= 60) return 'warning';
    return 'error';
  };

  return (
    <Box sx={{ p: 3 }}>
      {/* Header */}
      <Typography variant="h4" gutterBottom fontWeight="bold">
        생산 실적
      </Typography>
      <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
        오늘의 생산 현황과 실적을 확인하세요
      </Typography>

      {/* Tabs */}
      <Tabs value={activeTab} onChange={(_, v) => setActiveTab(v)} sx={{ mb: 3 }}>
        <Tab label="오늘" />
        <Tab label="이번 주" />
        <Tab label="작업 이력" />
      </Tabs>

      {/* Today Tab */}
      {activeTab === 0 && (
        <>
          {/* Main Stats */}
          <Grid container spacing={2} sx={{ mb: 3 }}>
            <Grid item xs={6} sm={3}>
              <Card sx={{ bgcolor: 'primary.main', color: 'white' }}>
                <CardContent sx={{ textAlign: 'center' }}>
                  <TrendingUpIcon sx={{ fontSize: 40, mb: 1 }} />
                  <Typography variant="subtitle2">목표</Typography>
                  <Typography variant="h4" fontWeight="bold">
                    {todayPerformance.target}
                  </Typography>
                  <Typography variant="caption">EA</Typography>
                </CardContent>
              </Card>
            </Grid>

            <Grid item xs={6} sm={3}>
              <Card sx={{ bgcolor: 'success.main', color: 'white' }}>
                <CardContent sx={{ textAlign: 'center' }}>
                  <SuccessIcon sx={{ fontSize: 40, mb: 1 }} />
                  <Typography variant="subtitle2">생산</Typography>
                  <Typography variant="h4" fontWeight="bold">
                    {todayPerformance.produced}
                  </Typography>
                  <Typography variant="caption">EA</Typography>
                </CardContent>
              </Card>
            </Grid>

            <Grid item xs={6} sm={3}>
              <Card sx={{ bgcolor: 'error.main', color: 'white' }}>
                <CardContent sx={{ textAlign: 'center' }}>
                  <DefectIcon sx={{ fontSize: 40, mb: 1 }} />
                  <Typography variant="subtitle2">불량</Typography>
                  <Typography variant="h4" fontWeight="bold">
                    {todayPerformance.defect}
                  </Typography>
                  <Typography variant="caption">EA</Typography>
                </CardContent>
              </Card>
            </Grid>

            <Grid item xs={6} sm={3}>
              <Card sx={{ bgcolor: 'info.main', color: 'white' }}>
                <CardContent sx={{ textAlign: 'center' }}>
                  <EfficiencyIcon sx={{ fontSize: 40, mb: 1 }} />
                  <Typography variant="subtitle2">달성률</Typography>
                  <Typography variant="h4" fontWeight="bold">
                    {todayPerformance.efficiency}%
                  </Typography>
                  <Typography variant="caption">목표 대비</Typography>
                </CardContent>
              </Card>
            </Grid>
          </Grid>

          {/* Progress */}
          <Card sx={{ mb: 3 }}>
            <CardContent>
              <Typography variant="h6" gutterBottom fontWeight="bold">
                목표 달성률
              </Typography>
              <Box sx={{ mb: 2 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                  <Typography variant="body2">
                    {todayPerformance.produced} / {todayPerformance.target} EA
                  </Typography>
                  <Typography variant="body2" fontWeight="bold" color="primary">
                    {todayPerformance.efficiency}%
                  </Typography>
                </Box>
                <LinearProgress
                  variant="determinate"
                  value={todayPerformance.efficiency}
                  sx={{ height: 12, borderRadius: 1 }}
                  color={getEfficiencyColor(todayPerformance.efficiency)}
                />
              </Box>
            </CardContent>
          </Card>

          {/* Details */}
          <Grid container spacing={2}>
            <Grid item xs={12} sm={6}>
              <Card>
                <CardContent>
                  <Typography variant="h6" gutterBottom fontWeight="bold">
                    양품률
                  </Typography>
                  <Typography variant="h3" color="success.main" fontWeight="bold">
                    {(((todayPerformance.produced - todayPerformance.defect) / todayPerformance.produced) * 100).toFixed(1)}%
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    양품: {todayPerformance.produced - todayPerformance.defect} EA
                  </Typography>
                </CardContent>
              </Card>
            </Grid>

            <Grid item xs={12} sm={6}>
              <Card>
                <CardContent>
                  <Typography variant="h6" gutterBottom fontWeight="bold">
                    완료 작업
                  </Typography>
                  <Typography variant="h3" color="primary.main" fontWeight="bold">
                    {todayPerformance.workOrders}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    건
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
          </Grid>
        </>
      )}

      {/* Week Tab */}
      {activeTab === 1 && (
        <Card>
          <CardContent>
            <Typography variant="h6" gutterBottom fontWeight="bold">
              주간 실적
            </Typography>
            <List>
              {weekPerformance.map((day, index) => (
                <Box key={day.shift}>
                  {index > 0 && <Divider />}
                  <ListItem>
                    <ListItemText
                      primary={
                        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                          <Typography variant="body1" fontWeight="bold">
                            {day.shift}
                          </Typography>
                          <Chip
                            label={`${day.efficiency}%`}
                            color={getEfficiencyColor(day.efficiency)}
                            size="small"
                          />
                        </Box>
                      }
                      secondary={
                        <Box sx={{ mt: 1 }}>
                          <Typography variant="body2" color="text.secondary">
                            생산: {day.produced} / {day.target} EA • 불량: {day.defect} EA • 작업: {day.workOrders}건
                          </Typography>
                          <LinearProgress
                            variant="determinate"
                            value={day.efficiency}
                            sx={{ height: 6, borderRadius: 1, mt: 1 }}
                            color={getEfficiencyColor(day.efficiency)}
                          />
                        </Box>
                      }
                    />
                  </ListItem>
                </Box>
              ))}
            </List>
          </CardContent>
        </Card>
      )}

      {/* Work History Tab */}
      {activeTab === 2 && (
        <Box>
          {workHistory.map((work) => (
            <Card key={work.workOrderNo} sx={{ mb: 2 }}>
              <CardContent>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start', mb: 2 }}>
                  <Box>
                    <Typography variant="h6" fontWeight="bold">
                      {work.workOrderNo}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      {work.productName}
                    </Typography>
                  </Box>
                  <Chip
                    label="완료"
                    color="success"
                    size="small"
                  />
                </Box>

                <Grid container spacing={2} sx={{ mb: 2 }}>
                  <Grid item xs={6}>
                    <Typography variant="caption" color="text.secondary">
                      생산 수량
                    </Typography>
                    <Typography variant="h6" fontWeight="bold">
                      {work.produced} / {work.target} EA
                    </Typography>
                  </Grid>
                  <Grid item xs={6}>
                    <Typography variant="caption" color="text.secondary">
                      불량 수량
                    </Typography>
                    <Typography variant="h6" fontWeight="bold" color="error.main">
                      {work.defect} EA
                    </Typography>
                  </Grid>
                </Grid>

                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                  <Box sx={{ display: 'flex', alignItems: 'center' }}>
                    <TimeIcon sx={{ fontSize: 20, mr: 0.5, color: 'text.secondary' }} />
                    <Typography variant="body2" color="text.secondary">
                      {work.startTime} - {work.endTime}
                    </Typography>
                  </Box>
                  <Chip label={work.duration} size="small" variant="outlined" />
                </Box>

                <LinearProgress
                  variant="determinate"
                  value={(work.produced / work.target) * 100}
                  sx={{ height: 8, borderRadius: 1, mt: 2 }}
                  color="success"
                />
              </CardContent>
            </Card>
          ))}
        </Box>
      )}
    </Box>
  );
};

export default POPPerformancePage;
