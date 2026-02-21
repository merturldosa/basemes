/**
 * Production Dashboard Page
 * 생산 관리 대시보드
 * @author Moon Myung-seop
 */

import { useState, useEffect } from 'react';
import {
  Box,
  Grid,
  Paper,
  Typography,
  CircularProgress,
  Alert,
  Card,
  CardContent,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Chip,
} from '@mui/material';
import {
  Assignment,
  CheckCircle,
  HourglassEmpty,
  PlayArrow,
  Cancel,
  TrendingUp,
} from '@mui/icons-material';
import workOrderService, { WorkOrder } from '@/services/workOrderService';
import productService, { Product } from '@/services/productService';
import { getErrorMessage } from '@/utils/errorUtils';
import { format, subDays } from 'date-fns';
import ReactECharts from 'echarts-for-react';

interface WorkOrderStats {
  total: number;
  pending: number;
  ready: number;
  inProgress: number;
  completed: number;
  cancelled: number;
}

interface StatCardProps {
  title: string;
  value: number;
  icon: React.ReactNode;
  color: string;
  percentage?: number;
}

function StatCard({ title, value, icon, color, percentage }: StatCardProps) {
  return (
    <Card
      sx={{
        transition: 'transform 0.2s, box-shadow 0.2s',
        '&:hover': {
          transform: 'translateY(-4px)',
          boxShadow: 4,
        },
      }}
    >
      <CardContent>
        <Box display="flex" alignItems="center" gap={2}>
          <Box
            sx={{
              p: 2,
              borderRadius: 2,
              bgcolor: `${color}.light`,
              color: `${color}.main`,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            {icon}
          </Box>
          <Box flexGrow={1}>
            <Typography variant="body2" color="text.secondary">
              {title}
            </Typography>
            <Typography variant="h4" fontWeight="bold">
              {value}
            </Typography>
            {percentage !== undefined && (
              <Typography variant="caption" color="text.secondary">
                {percentage.toFixed(1)}%
              </Typography>
            )}
          </Box>
        </Box>
      </CardContent>
    </Card>
  );
}

export default function Dashboard() {
  const [workOrders, setWorkOrders] = useState<WorkOrder[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // 작업 지시 통계 계산
  const getWorkOrderStats = (): WorkOrderStats => {
    const stats: WorkOrderStats = {
      total: workOrders.length,
      pending: 0,
      ready: 0,
      inProgress: 0,
      completed: 0,
      cancelled: 0,
    };

    workOrders.forEach((wo) => {
      switch (wo.status) {
        case 'PENDING':
          stats.pending++;
          break;
        case 'READY':
          stats.ready++;
          break;
        case 'IN_PROGRESS':
          stats.inProgress++;
          break;
        case 'COMPLETED':
          stats.completed++;
          break;
        case 'CANCELLED':
          stats.cancelled++;
          break;
      }
    });

    return stats;
  };

  // 데이터 로드
  const loadDashboardData = async () => {
    try {
      setLoading(true);
      setError(null);

      const [workOrdersData, productsData] = await Promise.all([
        workOrderService.getWorkOrders(),
        productService.getProducts(),
      ]);

      setWorkOrders(workOrdersData);
      setProducts(productsData);
    } catch (err) {
      setError(getErrorMessage(err, '대시보드 데이터 로드 실패'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDashboardData();

    // 30초마다 자동 갱신
    const interval = setInterval(() => {
      loadDashboardData();
    }, 30000);

    return () => clearInterval(interval);
  }, []);

  // 상태별 칩 색상
  const getStatusColor = (status: string) => {
    switch (status) {
      case 'PENDING':
        return 'default';
      case 'READY':
        return 'info';
      case 'IN_PROGRESS':
        return 'primary';
      case 'COMPLETED':
        return 'success';
      case 'CANCELLED':
        return 'error';
      default:
        return 'default';
    }
  };

  // 상태 한글 변환
  const getStatusLabel = (status: string) => {
    switch (status) {
      case 'PENDING':
        return '대기';
      case 'READY':
        return '준비';
      case 'IN_PROGRESS':
        return '진행중';
      case 'COMPLETED':
        return '완료';
      case 'CANCELLED':
        return '취소';
      default:
        return status;
    }
  };

  // 상태별 작업 지시 도넛 차트
  const getWorkOrderStatusChart = () => {
    const stats = getWorkOrderStats();
    const colors = {
      pending: '#9e9e9e',
      ready: '#2196f3',
      inProgress: '#ff9800',
      completed: '#4caf50',
      cancelled: '#f44336',
    };

    return {
      title: {
        text: '작업 지시 상태별 분포',
        left: 'center',
        top: 10,
        textStyle: {
          fontSize: 16,
          fontWeight: 'bold',
        },
      },
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c}건 ({d}%)',
      },
      legend: {
        bottom: 10,
        left: 'center',
      },
      series: [
        {
          name: '작업 지시',
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2,
          },
          label: {
            show: false,
            position: 'center',
          },
          emphasis: {
            label: {
              show: true,
              fontSize: 20,
              fontWeight: 'bold',
            },
          },
          labelLine: {
            show: false,
          },
          data: [
            { value: stats.pending, name: '대기', itemStyle: { color: colors.pending } },
            { value: stats.ready, name: '준비', itemStyle: { color: colors.ready } },
            { value: stats.inProgress, name: '진행중', itemStyle: { color: colors.inProgress } },
            { value: stats.completed, name: '완료', itemStyle: { color: colors.completed } },
            { value: stats.cancelled, name: '취소', itemStyle: { color: colors.cancelled } },
          ].filter((item) => item.value > 0),
        },
      ],
    };
  };

  // 제품별 생산 수량 바 차트
  const getProductionByProductChart = () => {
    // 제품별 완료된 작업 지시의 실적 수량 합계
    const productStats = workOrders
      .filter((wo) => wo.status === 'COMPLETED')
      .reduce((acc: Record<string, number>, wo) => {
        const key = wo.productName;
        if (!acc[key]) {
          acc[key] = 0;
        }
        acc[key] += Number(wo.actualQuantity) || 0;
        return acc;
      }, {});

    const productNames = Object.keys(productStats);
    const productValues = Object.values(productStats) as number[];

    return {
      title: {
        text: '제품별 완료 수량',
        left: 'center',
        top: 10,
        textStyle: {
          fontSize: 16,
          fontWeight: 'bold',
        },
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow',
        },
        formatter: '{b}<br/>수량: {c}',
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true,
      },
      xAxis: {
        type: 'category',
        data: productNames,
        axisLabel: {
          interval: 0,
          rotate: productNames.length > 3 ? 30 : 0,
        },
      },
      yAxis: {
        type: 'value',
        name: '수량',
      },
      series: [
        {
          name: '생산 수량',
          type: 'bar',
          barWidth: '60%',
          itemStyle: {
            color: {
              type: 'linear',
              x: 0,
              y: 0,
              x2: 0,
              y2: 1,
              colorStops: [
                { offset: 0, color: '#42a5f5' },
                { offset: 1, color: '#1976d2' },
              ],
            },
            borderRadius: [8, 8, 0, 0],
          },
          label: {
            show: true,
            position: 'top',
            formatter: '{c}',
          },
          data: productValues,
        },
      ],
    };
  };

  // 일별 작업 지시 추이 (최근 7일)
  const getDailyWorkOrderTrendChart = () => {
    const days = 7;
    const dateLabels: string[] = [];
    const dateCounts: number[] = [];

    // 최근 7일 날짜 생성
    for (let i = days - 1; i >= 0; i--) {
      const date = subDays(new Date(), i);
      const dateStr = format(date, 'yyyy-MM-dd');
      const label = format(date, 'MM/dd');
      dateLabels.push(label);

      // 해당 날짜에 생성된 작업 지시 개수
      const count = workOrders.filter((wo) => {
        const createdDate = format(new Date(wo.createdAt), 'yyyy-MM-dd');
        return createdDate === dateStr;
      }).length;

      dateCounts.push(count);
    }

    return {
      title: {
        text: '최근 7일 작업 지시 생성 추이',
        left: 'center',
        top: 10,
        textStyle: {
          fontSize: 16,
          fontWeight: 'bold',
        },
      },
      tooltip: {
        trigger: 'axis',
        formatter: '{b}<br/>작업 지시: {c}건',
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true,
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: dateLabels,
      },
      yAxis: {
        type: 'value',
        minInterval: 1,
        name: '건수',
      },
      series: [
        {
          name: '작업 지시',
          type: 'line',
          smooth: true,
          symbol: 'circle',
          symbolSize: 8,
          lineStyle: {
            color: '#1976d2',
            width: 3,
          },
          itemStyle: {
            color: '#1976d2',
          },
          areaStyle: {
            color: {
              type: 'linear',
              x: 0,
              y: 0,
              x2: 0,
              y2: 1,
              colorStops: [
                { offset: 0, color: 'rgba(25, 118, 210, 0.3)' },
                { offset: 1, color: 'rgba(25, 118, 210, 0.05)' },
              ],
            },
          },
          data: dateCounts,
        },
      ],
    };
  };

  if (loading && workOrders.length === 0) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return (
      <Box>
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      </Box>
    );
  }

  const stats = getWorkOrderStats();
  const inProgressOrders = workOrders.filter((wo) => wo.status === 'IN_PROGRESS');
  const recentCompletedOrders = workOrders
    .filter((wo) => wo.status === 'COMPLETED')
    .sort((a, b) => new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime())
    .slice(0, 5);

  return (
    <Box>
      {/* Header */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" gutterBottom fontWeight="bold">
          생산 대시보드
        </Typography>
        <Typography variant="body1" color="text.secondary">
          실시간 생산 현황을 확인하세요 (30초마다 자동 갱신)
        </Typography>
      </Box>

      {/* 통계 카드 */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={6} md={4} lg={2}>
          <StatCard
            title="전체 작업 지시"
            value={stats.total}
            icon={<Assignment />}
            color="primary"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={4} lg={2}>
          <StatCard
            title="대기"
            value={stats.pending}
            icon={<HourglassEmpty />}
            color="info"
            percentage={stats.total > 0 ? (stats.pending / stats.total) * 100 : 0}
          />
        </Grid>
        <Grid item xs={12} sm={6} md={4} lg={2}>
          <StatCard
            title="진행중"
            value={stats.inProgress}
            icon={<PlayArrow />}
            color="warning"
            percentage={stats.total > 0 ? (stats.inProgress / stats.total) * 100 : 0}
          />
        </Grid>
        <Grid item xs={12} sm={6} md={4} lg={2}>
          <StatCard
            title="완료"
            value={stats.completed}
            icon={<CheckCircle />}
            color="success"
            percentage={stats.total > 0 ? (stats.completed / stats.total) * 100 : 0}
          />
        </Grid>
        <Grid item xs={12} sm={6} md={4} lg={2}>
          <StatCard
            title="취소"
            value={stats.cancelled}
            icon={<Cancel />}
            color="error"
            percentage={stats.total > 0 ? (stats.cancelled / stats.total) * 100 : 0}
          />
        </Grid>
        <Grid item xs={12} sm={6} md={4} lg={2}>
          <StatCard
            title="등록 제품"
            value={products.length}
            icon={<TrendingUp />}
            color="secondary"
          />
        </Grid>
      </Grid>

      {/* 차트 영역 */}
      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid item xs={12} md={4}>
          <Paper sx={{ p: 3, height: 400 }}>
            <ReactECharts
              option={getWorkOrderStatusChart()}
              style={{ height: '100%' }}
              opts={{ renderer: 'svg' }}
            />
          </Paper>
        </Grid>
        <Grid item xs={12} md={4}>
          <Paper sx={{ p: 3, height: 400 }}>
            <ReactECharts
              option={getProductionByProductChart()}
              style={{ height: '100%' }}
              opts={{ renderer: 'svg' }}
            />
          </Paper>
        </Grid>
        <Grid item xs={12} md={4}>
          <Paper sx={{ p: 3, height: 400 }}>
            <ReactECharts
              option={getDailyWorkOrderTrendChart()}
              style={{ height: '100%' }}
              opts={{ renderer: 'svg' }}
            />
          </Paper>
        </Grid>
      </Grid>

      {/* 진행 중인 작업 */}
      <Grid container spacing={3}>
        <Grid item xs={12} md={7}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom fontWeight="bold">
              진행 중인 작업
            </Typography>
            {inProgressOrders.length === 0 ? (
              <Typography variant="body2" color="text.secondary" sx={{ py: 4, textAlign: 'center' }}>
                진행 중인 작업이 없습니다
              </Typography>
            ) : (
              <TableContainer>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>작업 지시 번호</TableCell>
                      <TableCell>제품</TableCell>
                      <TableCell>공정</TableCell>
                      <TableCell align="right">계획 수량</TableCell>
                      <TableCell align="right">실적 수량</TableCell>
                      <TableCell align="right">진행률</TableCell>
                      <TableCell>시작 시간</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {inProgressOrders.map((order) => (
                      <TableRow key={order.workOrderId}>
                        <TableCell>
                          <Typography variant="body2" fontWeight="medium">
                            {order.workOrderNo}
                          </Typography>
                        </TableCell>
                        <TableCell>{order.productName}</TableCell>
                        <TableCell>{order.processName}</TableCell>
                        <TableCell align="right">
                          {order.plannedQuantity.toLocaleString()}
                        </TableCell>
                        <TableCell align="right">
                          {order.actualQuantity.toLocaleString()}
                        </TableCell>
                        <TableCell align="right">
                          <Chip
                            label={`${order.plannedQuantity > 0 ? ((order.actualQuantity / order.plannedQuantity) * 100).toFixed(0) : 0}%`}
                            size="small"
                            color={
                              order.actualQuantity >= order.plannedQuantity
                                ? 'success'
                                : 'warning'
                            }
                          />
                        </TableCell>
                        <TableCell>
                          {order.actualStartDate
                            ? format(new Date(order.actualStartDate), 'MM-dd HH:mm')
                            : '-'}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            )}
          </Paper>
        </Grid>

        {/* 최근 완료된 작업 */}
        <Grid item xs={12} md={5}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom fontWeight="bold">
              최근 완료된 작업
            </Typography>
            {recentCompletedOrders.length === 0 ? (
              <Typography variant="body2" color="text.secondary" sx={{ py: 4, textAlign: 'center' }}>
                완료된 작업이 없습니다
              </Typography>
            ) : (
              <TableContainer>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>작업 지시</TableCell>
                      <TableCell>제품</TableCell>
                      <TableCell align="right">수량</TableCell>
                      <TableCell>완료 시간</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {recentCompletedOrders.map((order) => (
                      <TableRow key={order.workOrderId}>
                        <TableCell>
                          <Typography variant="body2" fontWeight="medium">
                            {order.workOrderNo}
                          </Typography>
                        </TableCell>
                        <TableCell>
                          <Typography variant="body2" noWrap>
                            {order.productName}
                          </Typography>
                        </TableCell>
                        <TableCell align="right">
                          {order.actualQuantity.toLocaleString()}
                        </TableCell>
                        <TableCell>
                          {order.actualEndDate
                            ? format(new Date(order.actualEndDate), 'MM-dd HH:mm')
                            : '-'}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            )}
          </Paper>
        </Grid>
      </Grid>

      {/* 전체 작업 지시 목록 */}
      <Paper sx={{ p: 3, mt: 3 }}>
        <Typography variant="h6" gutterBottom fontWeight="bold">
          전체 작업 지시
        </Typography>
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>작업 지시 번호</TableCell>
                <TableCell>상태</TableCell>
                <TableCell>제품</TableCell>
                <TableCell>공정</TableCell>
                <TableCell align="right">계획 수량</TableCell>
                <TableCell align="right">실적 수량</TableCell>
                <TableCell align="right">양품</TableCell>
                <TableCell align="right">불량</TableCell>
                <TableCell>계획 시작일</TableCell>
                <TableCell>비고</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {workOrders.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={10} align="center" sx={{ py: 4 }}>
                    <Typography variant="body2" color="text.secondary">
                      작업 지시가 없습니다
                    </Typography>
                  </TableCell>
                </TableRow>
              ) : (
                workOrders.map((order) => (
                  <TableRow key={order.workOrderId}>
                    <TableCell>
                      <Typography variant="body2" fontWeight="medium">
                        {order.workOrderNo}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Chip
                        label={getStatusLabel(order.status)}
                        color={getStatusColor(order.status)}
                        size="small"
                      />
                    </TableCell>
                    <TableCell>{order.productName}</TableCell>
                    <TableCell>{order.processName}</TableCell>
                    <TableCell align="right">
                      {order.plannedQuantity.toLocaleString()}
                    </TableCell>
                    <TableCell align="right">
                      {order.actualQuantity.toLocaleString()}
                    </TableCell>
                    <TableCell align="right">
                      {order.goodQuantity.toLocaleString()}
                    </TableCell>
                    <TableCell align="right">
                      {order.defectQuantity.toLocaleString()}
                    </TableCell>
                    <TableCell>
                      {order.plannedStartDate
                        ? format(new Date(order.plannedStartDate), 'yyyy-MM-dd')
                        : '-'}
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2" noWrap sx={{ maxWidth: 150 }}>
                        {order.remarks || '-'}
                      </Typography>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>
      </Paper>
    </Box>
  );
}
