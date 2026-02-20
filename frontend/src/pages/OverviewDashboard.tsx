/**
 * Overview Dashboard Page
 * 통합 대시보드 - 사용자, 역할, 로그인 통계
 * @author Claude Code (Sonnet 4.5)
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
  ToggleButtonGroup,
  ToggleButton,
  Chip,
} from '@mui/material';
import {
  People,
  SupervisedUserCircle,
  Lock,
  Login,
  TrendingUp,
  Groups,
} from '@mui/icons-material';
import dashboardService, {
  DashboardStats,
  UserStats,
  LoginTrend,
  RoleDistribution,
} from '@/services/dashboardService';
import ReactECharts from 'echarts-for-react';

interface StatCardProps {
  title: string;
  value: number | string;
  icon: React.ReactNode;
  color: string;
  subtitle?: string;
}

function StatCard({ title, value, icon, color, subtitle }: StatCardProps) {
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
            {subtitle && (
              <Typography variant="caption" color="text.secondary">
                {subtitle}
              </Typography>
            )}
          </Box>
        </Box>
      </CardContent>
    </Card>
  );
}

export default function OverviewDashboard() {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [userStats, setUserStats] = useState<UserStats[]>([]);
  const [loginTrend, setLoginTrend] = useState<LoginTrend[]>([]);
  const [roleDistribution, setRoleDistribution] = useState<RoleDistribution[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [trendDays, setTrendDays] = useState<number>(7);

  // 데이터 로드
  const loadDashboardData = async () => {
    try {
      setLoading(true);
      setError(null);

      const [statsData, userStatsData, loginTrendData, roleDistData] = await Promise.all([
        dashboardService.getStats(),
        dashboardService.getUserStats(),
        dashboardService.getLoginTrend(trendDays),
        dashboardService.getRoleDistribution(),
      ]);

      setStats(statsData);
      setUserStats(userStatsData);
      setLoginTrend(loginTrendData);
      setRoleDistribution(roleDistData);
    } catch (err: any) {
      setError(err.response?.data?.message || '대시보드 데이터 로드 실패');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDashboardData();

    // 60초마다 자동 갱신
    const interval = setInterval(() => {
      loadDashboardData();
    }, 60000);

    return () => clearInterval(interval);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [trendDays]);

  // 사용자 상태 도넛 차트
  const getUserStatsChart = () => {
    const colors = {
      활성: '#4caf50',
      비활성: '#9e9e9e',
      잠김: '#f44336',
    };

    return {
      title: {
        text: '사용자 상태 분포',
        left: 'center',
        top: 10,
        textStyle: {
          fontSize: 16,
          fontWeight: 'bold',
        },
      },
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c}명 ({d}%)',
      },
      legend: {
        bottom: 10,
        left: 'center',
      },
      series: [
        {
          name: '사용자 상태',
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
          data: userStats.map((stat) => ({
            value: stat.count,
            name: stat.displayName,
            itemStyle: { color: (colors as any)[stat.displayName] || '#2196f3' },
          })),
        },
      ],
    };
  };

  // 로그인 추이 라인 차트
  const getLoginTrendChart = () => {
    return {
      title: {
        text: `최근 ${trendDays}일 로그인 추이`,
        left: 'center',
        top: 10,
        textStyle: {
          fontSize: 16,
          fontWeight: 'bold',
        },
      },
      tooltip: {
        trigger: 'axis',
        formatter: '{b}<br/>로그인: {c}회',
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
        data: loginTrend.map((item) => item.dateLabel),
      },
      yAxis: {
        type: 'value',
        minInterval: 1,
        name: '로그인 횟수',
      },
      series: [
        {
          name: '로그인',
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
          data: loginTrend.map((item) => item.loginCount),
        },
      ],
    };
  };

  // 역할별 사용자 분포 바 차트
  const getRoleDistributionChart = () => {
    return {
      title: {
        text: '역할별 사용자 분포',
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
        formatter: '{b}<br/>사용자: {c}명',
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true,
      },
      xAxis: {
        type: 'category',
        data: roleDistribution.map((item) => item.roleName),
        axisLabel: {
          interval: 0,
          rotate: roleDistribution.length > 5 ? 30 : 0,
        },
      },
      yAxis: {
        type: 'value',
        name: '사용자 수',
        minInterval: 1,
      },
      series: [
        {
          name: '사용자 수',
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
            formatter: '{c}명',
          },
          data: roleDistribution.map((item) => item.userCount),
        },
      ],
    };
  };

  if (loading && !stats) {
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

  if (!stats) {
    return null;
  }

  return (
    <Box>
      {/* Header */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" gutterBottom fontWeight="bold">
          통합 대시보드
        </Typography>
        <Typography variant="body1" color="text.secondary">
          실시간 시스템 현황을 확인하세요 (60초마다 자동 갱신)
        </Typography>
      </Box>

      {/* 통계 카드 */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={6} md={4}>
          <StatCard
            title="전체 사용자"
            value={stats.totalUsers}
            icon={<People />}
            color="primary"
            subtitle={`활성: ${stats.activeUsers}명`}
          />
        </Grid>
        <Grid item xs={12} sm={6} md={4}>
          <StatCard
            title="전체 역할"
            value={stats.totalRoles}
            icon={<SupervisedUserCircle />}
            color="secondary"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={4}>
          <StatCard
            title="전체 권한"
            value={stats.totalPermissions}
            icon={<Lock />}
            color="info"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={4}>
          <StatCard
            title="오늘 로그인"
            value={stats.todayLogins}
            icon={<Login />}
            color="success"
            subtitle="자정 이후"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={4}>
          <StatCard
            title="활성 세션"
            value={stats.activeSessions}
            icon={<TrendingUp />}
            color="warning"
            subtitle="최근 30분"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={4}>
          <StatCard
            title="활성 사용자 비율"
            value={`${stats.totalUsers > 0 ? ((stats.activeUsers / stats.totalUsers) * 100).toFixed(1) : 0}%`}
            icon={<Groups />}
            color="success"
          />
        </Grid>
      </Grid>

      {/* 사용자 상태 도넛 차트 */}
      {userStats.length > 0 && (
        <Grid container spacing={3} sx={{ mb: 3 }}>
          <Grid item xs={12} md={4}>
            <Paper sx={{ p: 3, height: 400 }}>
              <ReactECharts
                option={getUserStatsChart()}
                style={{ height: '100%' }}
                opts={{ renderer: 'svg' }}
              />
            </Paper>
          </Grid>

          {/* 로그인 추이 차트 */}
          <Grid item xs={12} md={8}>
            <Paper sx={{ p: 3, height: 400 }}>
              <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                <ToggleButtonGroup
                  value={trendDays}
                  exclusive
                  onChange={(e, newValue) => {
                    if (newValue !== null) {
                      setTrendDays(newValue);
                    }
                  }}
                  size="small"
                >
                  <ToggleButton value={7}>7일</ToggleButton>
                  <ToggleButton value={30}>30일</ToggleButton>
                </ToggleButtonGroup>
              </Box>
              <ReactECharts
                option={getLoginTrendChart()}
                style={{ height: 'calc(100% - 50px)' }}
                opts={{ renderer: 'svg' }}
              />
            </Paper>
          </Grid>
        </Grid>
      )}

      {/* 역할 분포 바 차트 */}
      {roleDistribution.length > 0 && (
        <Grid container spacing={3}>
          <Grid item xs={12}>
            <Paper sx={{ p: 3, height: 400 }}>
              <ReactECharts
                option={getRoleDistributionChart()}
                style={{ height: '100%' }}
                opts={{ renderer: 'svg' }}
              />
            </Paper>
          </Grid>
        </Grid>
      )}

      {/* 사용자 상태 테이블 (상세) */}
      <Paper sx={{ p: 3, mt: 3 }}>
        <Typography variant="h6" gutterBottom fontWeight="bold">
          사용자 상태 상세
        </Typography>
        <Box display="flex" gap={2} mt={2}>
          {userStats.map((stat) => (
            <Chip
              key={stat.status}
              label={`${stat.displayName}: ${stat.count}명`}
              color={
                stat.displayName === '활성'
                  ? 'success'
                  : stat.displayName === '비활성'
                  ? 'default'
                  : 'error'
              }
              size="medium"
              sx={{ fontSize: '1rem', py: 2.5, px: 1 }}
            />
          ))}
        </Box>
      </Paper>
    </Box>
  );
}
