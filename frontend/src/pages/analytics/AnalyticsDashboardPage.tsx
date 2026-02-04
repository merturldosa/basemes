/**
 * Analytics Dashboard Page
 * Comprehensive production analytics and KPIs
 * @author Moon Myung-seop
 */

import { useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Grid,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Chip,
  LinearProgress,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Paper,
} from '@mui/material';
import {
  TrendingUp as TrendingUpIcon,
  TrendingDown as TrendingDownIcon,
  Factory as FactoryIcon,
  CheckCircle as QualityIcon,
  Speed as EfficiencyIcon,
  Inventory as InventoryIcon,
  People as PeopleIcon,
  AccessTime as TimeIcon,
} from '@mui/icons-material';

interface KPIData {
  title: string;
  value: string;
  change: number;
  trend: 'up' | 'down';
  target?: string;
  icon: React.ReactNode;
  color: string;
}

interface ProductionData {
  date: string;
  target: number;
  actual: number;
  efficiency: number;
}

const AnalyticsDashboardPage: React.FC = () => {
  const [period, setPeriod] = useState('today');

  // Mock KPI data
  const kpiData: KPIData[] = [
    {
      title: '총 생산량',
      value: '12,450',
      change: 8.5,
      trend: 'up',
      target: '15,000',
      icon: <FactoryIcon />,
      color: '#1976d2',
    },
    {
      title: 'OEE (종합설비효율)',
      value: '84.2%',
      change: 3.2,
      trend: 'up',
      target: '85%',
      icon: <EfficiencyIcon />,
      color: '#2e7d32',
    },
    {
      title: '품질 달성률',
      value: '98.5%',
      change: -0.5,
      trend: 'down',
      target: '99%',
      icon: <QualityIcon />,
      color: '#ed6c02',
    },
    {
      title: '재고 회전율',
      value: '12.3',
      change: 1.8,
      trend: 'up',
      target: '12',
      icon: <InventoryIcon />,
      color: '#9c27b0',
    },
    {
      title: '평균 생산성',
      value: '145',
      change: 5.2,
      trend: 'up',
      target: '150',
      icon: <PeopleIcon />,
      color: '#0288d1',
    },
    {
      title: '평균 사이클 타임',
      value: '4.2분',
      change: -3.1,
      trend: 'up',
      target: '4.0분',
      icon: <TimeIcon />,
      color: '#d32f2f',
    },
  ];

  // Mock production trend data
  const productionTrend: ProductionData[] = [
    { date: '월', target: 2500, actual: 2450, efficiency: 98 },
    { date: '화', target: 2500, actual: 2380, efficiency: 95.2 },
    { date: '수', target: 2500, actual: 2520, efficiency: 100.8 },
    { date: '목', target: 2500, actual: 2400, efficiency: 96 },
    { date: '금', target: 2500, actual: 2700, efficiency: 108 },
  ];

  // Mock top products data
  const topProducts = [
    { name: '제품 A', quantity: 3500, efficiency: 105, quality: 99.2 },
    { name: '제품 B', quantity: 2800, efficiency: 98, quality: 98.5 },
    { name: '제품 C', quantity: 2400, efficiency: 102, quality: 99.8 },
    { name: '제품 D', quantity: 1950, efficiency: 88, quality: 97.5 },
    { name: '제품 E', quantity: 1800, efficiency: 95, quality: 98.9 },
  ];

  // Mock defect analysis
  const defectAnalysis = [
    { type: '치수 불량', count: 45, percentage: 35 },
    { type: '외관 불량', count: 38, percentage: 30 },
    { type: '기능 불량', count: 25, percentage: 20 },
    { type: '포장 불량', count: 19, percentage: 15 },
  ];

  const getEfficiencyColor = (efficiency: number) => {
    if (efficiency >= 100) return 'success';
    if (efficiency >= 90) return 'info';
    if (efficiency >= 80) return 'warning';
    return 'error';
  };

  return (
    <Box sx={{ p: 3 }}>
      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box>
          <Typography variant="h4" fontWeight="bold">
            분석 대시보드
          </Typography>
          <Typography variant="body2" color="text.secondary">
            실시간 생산 분석 및 KPI 모니터링
          </Typography>
        </Box>

        <FormControl sx={{ minWidth: 150 }}>
          <InputLabel>기간</InputLabel>
          <Select value={period} label="기간" onChange={(e) => setPeriod(e.target.value)}>
            <MenuItem value="today">오늘</MenuItem>
            <MenuItem value="week">이번 주</MenuItem>
            <MenuItem value="month">이번 달</MenuItem>
            <MenuItem value="quarter">분기</MenuItem>
          </Select>
        </FormControl>
      </Box>

      {/* KPI Cards */}
      <Grid container spacing={3} sx={{ mb: 3 }}>
        {kpiData.map((kpi) => (
          <Grid item xs={12} sm={6} md={4} key={kpi.title}>
            <Card>
              <CardContent>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start', mb: 2 }}>
                  <Box>
                    <Typography variant="body2" color="text.secondary" gutterBottom>
                      {kpi.title}
                    </Typography>
                    <Typography variant="h4" fontWeight="bold" sx={{ color: kpi.color }}>
                      {kpi.value}
                    </Typography>
                  </Box>
                  <Box
                    sx={{
                      bgcolor: kpi.color,
                      color: 'white',
                      p: 1,
                      borderRadius: 1,
                      display: 'flex',
                      alignItems: 'center',
                    }}
                  >
                    {kpi.icon}
                  </Box>
                </Box>

                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  {kpi.trend === 'up' ? (
                    <TrendingUpIcon color={kpi.change > 0 ? 'success' : 'error'} fontSize="small" />
                  ) : (
                    <TrendingDownIcon color="error" fontSize="small" />
                  )}
                  <Typography
                    variant="body2"
                    color={kpi.change > 0 ? 'success.main' : 'error.main'}
                    fontWeight="bold"
                  >
                    {kpi.change > 0 ? '+' : ''}
                    {kpi.change}%
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    vs 지난 기간
                  </Typography>
                </Box>

                {kpi.target && (
                  <Box sx={{ mt: 2 }}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5 }}>
                      <Typography variant="caption" color="text.secondary">
                        목표: {kpi.target}
                      </Typography>
                      <Typography variant="caption" fontWeight="bold">
                        {((parseFloat(kpi.value.replace(/[^0-9.]/g, '')) / parseFloat(kpi.target.replace(/[^0-9.]/g, ''))) * 100).toFixed(1)}%
                      </Typography>
                    </Box>
                    <LinearProgress
                      variant="determinate"
                      value={Math.min((parseFloat(kpi.value.replace(/[^0-9.]/g, '')) / parseFloat(kpi.target.replace(/[^0-9.]/g, ''))) * 100, 100)}
                      sx={{ height: 6, borderRadius: 1 }}
                    />
                  </Box>
                )}
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      {/* Charts Row */}
      <Grid container spacing={3} sx={{ mb: 3 }}>
        {/* Production Trend */}
        <Grid item xs={12} md={8}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom fontWeight="bold">
                주간 생산 추이
              </Typography>
              <Box sx={{ mt: 3 }}>
                {productionTrend.map((day) => (
                  <Box key={day.date} sx={{ mb: 2 }}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                      <Typography variant="body2" fontWeight="bold">
                        {day.date}
                      </Typography>
                      <Box sx={{ display: 'flex', gap: 2 }}>
                        <Typography variant="body2" color="text.secondary">
                          목표: {day.target}
                        </Typography>
                        <Typography variant="body2" fontWeight="bold" color="primary">
                          실적: {day.actual}
                        </Typography>
                        <Chip
                          label={`${day.efficiency}%`}
                          size="small"
                          color={getEfficiencyColor(day.efficiency)}
                        />
                      </Box>
                    </Box>
                    <LinearProgress
                      variant="determinate"
                      value={Math.min(day.efficiency, 100)}
                      sx={{ height: 12, borderRadius: 1 }}
                      color={getEfficiencyColor(day.efficiency)}
                    />
                  </Box>
                ))}
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* Defect Analysis */}
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom fontWeight="bold">
                불량 유형 분석
              </Typography>
              <Box sx={{ mt: 3 }}>
                {defectAnalysis.map((defect, index) => (
                  <Box key={defect.type} sx={{ mb: 2 }}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                      <Typography variant="body2">{defect.type}</Typography>
                      <Typography variant="body2" fontWeight="bold">
                        {defect.count}건 ({defect.percentage}%)
                      </Typography>
                    </Box>
                    <LinearProgress
                      variant="determinate"
                      value={defect.percentage}
                      sx={{ height: 8, borderRadius: 1 }}
                      color={index === 0 ? 'error' : index === 1 ? 'warning' : 'info'}
                    />
                  </Box>
                ))}
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Top Products Table */}
      <Card>
        <CardContent>
          <Typography variant="h6" gutterBottom fontWeight="bold">
            생산 실적 상위 제품
          </Typography>
          <Paper sx={{ mt: 2, overflow: 'hidden' }}>
            <Table>
              <TableHead>
                <TableRow sx={{ bgcolor: 'grey.100' }}>
                  <TableCell>
                    <Typography variant="subtitle2" fontWeight="bold">
                      순위
                    </Typography>
                  </TableCell>
                  <TableCell>
                    <Typography variant="subtitle2" fontWeight="bold">
                      제품명
                    </Typography>
                  </TableCell>
                  <TableCell align="right">
                    <Typography variant="subtitle2" fontWeight="bold">
                      생산량
                    </Typography>
                  </TableCell>
                  <TableCell align="right">
                    <Typography variant="subtitle2" fontWeight="bold">
                      효율
                    </Typography>
                  </TableCell>
                  <TableCell align="right">
                    <Typography variant="subtitle2" fontWeight="bold">
                      품질
                    </Typography>
                  </TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {topProducts.map((product, index) => (
                  <TableRow key={product.name} hover>
                    <TableCell>
                      <Chip
                        label={index + 1}
                        size="small"
                        color={index === 0 ? 'warning' : 'default'}
                        sx={{ fontWeight: 'bold' }}
                      />
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2" fontWeight="medium">
                        {product.name}
                      </Typography>
                    </TableCell>
                    <TableCell align="right">
                      <Typography variant="body2">{product.quantity.toLocaleString()} EA</Typography>
                    </TableCell>
                    <TableCell align="right">
                      <Chip
                        label={`${product.efficiency}%`}
                        size="small"
                        color={getEfficiencyColor(product.efficiency)}
                      />
                    </TableCell>
                    <TableCell align="right">
                      <Typography variant="body2" color="success.main" fontWeight="bold">
                        {product.quality}%
                      </Typography>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </Paper>
        </CardContent>
      </Card>
    </Box>
  );
};

export default AnalyticsDashboardPage;
