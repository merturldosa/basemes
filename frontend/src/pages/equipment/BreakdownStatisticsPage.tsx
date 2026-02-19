import React, { useState, useEffect } from 'react';
import {
  Box,
  Paper,
  Typography,
  TextField,
  Button,
  Card,
  CardContent,
  Grid,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Snackbar,
  Alert,
} from '@mui/material';
import breakdownService, { BreakdownStatistics, BreakdownTrend } from '../../services/breakdownService';

const BreakdownStatisticsPage: React.FC = () => {
  const [statistics, setStatistics] = useState<BreakdownStatistics | null>(null);
  const [trend, setTrend] = useState<BreakdownTrend[]>([]);
  const [loading, setLoading] = useState(false);
  const [startDate, setStartDate] = useState(() => {
    const d = new Date();
    d.setDate(d.getDate() - 30);
    return d.toISOString().slice(0, 10);
  });
  const [endDate, setEndDate] = useState(() => new Date().toISOString().slice(0, 10));
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'success' as 'success' | 'error' | 'warning' | 'info',
  });

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const [statsData, trendData] = await Promise.all([
        breakdownService.getStatistics(startDate, endDate),
        breakdownService.getMonthlyTrend(12),
      ]);
      setStatistics(statsData);
      setTrend(trendData || []);
    } catch (error) {
      showSnackbar('통계 데이터를 불러오는데 실패했습니다.', 'error');
    } finally {
      setLoading(false);
    }
  };

  const showSnackbar = (message: string, severity: 'success' | 'error' | 'warning' | 'info') => {
    setSnackbar({ open: true, message, severity });
  };

  const getStatusLabel = (status: string) => {
    const labels: Record<string, string> = {
      REPORTED: '접수',
      ASSIGNED: '배정',
      IN_PROGRESS: '수리중',
      COMPLETED: '완료',
      CLOSED: '종결',
    };
    return labels[status] || status;
  };

  const getFailureTypeLabel = (type: string) => {
    const labels: Record<string, string> = {
      MECHANICAL: '기계',
      ELECTRICAL: '전기',
      SOFTWARE: '소프트웨어',
      PNEUMATIC: '공압',
      HYDRAULIC: '유압',
      OTHER: '기타',
    };
    return labels[type] || type;
  };

  const getSeverityLabel = (severity: string) => {
    const labels: Record<string, string> = {
      CRITICAL: '심각',
      MAJOR: '중요',
      MINOR: '경미',
    };
    return labels[severity] || severity;
  };

  return (
    <Box sx={{ p: 3 }}>
      <Paper sx={{ p: 2, mb: 2 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="h5" component="h1">
            고장 통계
          </Typography>
        </Box>

        {/* Date Range Filter */}
        <Box sx={{ display: 'flex', gap: 2, mb: 3, alignItems: 'center' }}>
          <TextField
            label="시작일"
            type="date"
            value={startDate}
            onChange={(e) => setStartDate(e.target.value)}
            InputLabelProps={{ shrink: true }}
            size="small"
          />
          <TextField
            label="종료일"
            type="date"
            value={endDate}
            onChange={(e) => setEndDate(e.target.value)}
            InputLabelProps={{ shrink: true }}
            size="small"
          />
          <Button variant="contained" onClick={loadData} disabled={loading}>
            조회
          </Button>
        </Box>

        {/* Summary Cards */}
        {statistics && (
          <>
            <Grid container spacing={2} sx={{ mb: 3 }}>
              <Grid item xs={12} sm={6} md={3}>
                <Card>
                  <CardContent>
                    <Typography color="textSecondary" gutterBottom>
                      총 고장 건수
                    </Typography>
                    <Typography variant="h4">
                      {statistics.totalBreakdowns}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
              <Grid item xs={12} sm={6} md={3}>
                <Card>
                  <CardContent>
                    <Typography color="textSecondary" gutterBottom>
                      MTBF
                    </Typography>
                    <Typography variant="h4">
                      {statistics.mtbfHours != null ? `${statistics.mtbfHours}시간` : '-'}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
              <Grid item xs={12} sm={6} md={3}>
                <Card>
                  <CardContent>
                    <Typography color="textSecondary" gutterBottom>
                      MTTR
                    </Typography>
                    <Typography variant="h4">
                      {statistics.mttrMinutes != null ? `${statistics.mttrMinutes}분` : '-'}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
              <Grid item xs={12} sm={6} md={3}>
                <Card>
                  <CardContent>
                    <Typography color="textSecondary" gutterBottom>
                      고장률
                    </Typography>
                    <Typography variant="h4">
                      {statistics.failureRate != null ? `${statistics.failureRate}%` : '-'}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
            </Grid>

            {/* Detail Tables */}
            <Grid container spacing={2} sx={{ mb: 3 }}>
              {/* By Status */}
              <Grid item xs={12} md={4}>
                <TableContainer component={Paper} variant="outlined">
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell colSpan={2}>
                          <Typography variant="subtitle1" fontWeight="bold">상태별 현황</Typography>
                        </TableCell>
                      </TableRow>
                      <TableRow>
                        <TableCell>상태</TableCell>
                        <TableCell align="right">건수</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {Object.entries(statistics.byStatus).map(([status, count]) => (
                        <TableRow key={status}>
                          <TableCell>{getStatusLabel(status)}</TableCell>
                          <TableCell align="right">{count}</TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              </Grid>

              {/* By Failure Type */}
              <Grid item xs={12} md={4}>
                <TableContainer component={Paper} variant="outlined">
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell colSpan={2}>
                          <Typography variant="subtitle1" fontWeight="bold">고장유형별 현황</Typography>
                        </TableCell>
                      </TableRow>
                      <TableRow>
                        <TableCell>유형</TableCell>
                        <TableCell align="right">건수</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {Object.entries(statistics.byFailureType).map(([type, count]) => (
                        <TableRow key={type}>
                          <TableCell>{getFailureTypeLabel(type)}</TableCell>
                          <TableCell align="right">{count}</TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              </Grid>

              {/* By Severity */}
              <Grid item xs={12} md={4}>
                <TableContainer component={Paper} variant="outlined">
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell colSpan={2}>
                          <Typography variant="subtitle1" fontWeight="bold">심각도별 현황</Typography>
                        </TableCell>
                      </TableRow>
                      <TableRow>
                        <TableCell>심각도</TableCell>
                        <TableCell align="right">건수</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {Object.entries(statistics.bySeverity).map(([severity, count]) => (
                        <TableRow key={severity}>
                          <TableCell>{getSeverityLabel(severity)}</TableCell>
                          <TableCell align="right">{count}</TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              </Grid>
            </Grid>

            {/* Top 5 Equipments */}
            <Grid container spacing={2} sx={{ mb: 3 }}>
              <Grid item xs={12} md={6}>
                <TableContainer component={Paper} variant="outlined">
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell colSpan={3}>
                          <Typography variant="subtitle1" fontWeight="bold">Top 5 설비</Typography>
                        </TableCell>
                      </TableRow>
                      <TableRow>
                        <TableCell>설비 코드</TableCell>
                        <TableCell>설비명</TableCell>
                        <TableCell align="right">고장 건수</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {statistics.topEquipments.map((eq) => (
                        <TableRow key={eq.equipmentId}>
                          <TableCell>{eq.equipmentCode}</TableCell>
                          <TableCell>{eq.equipmentName}</TableCell>
                          <TableCell align="right">{eq.breakdownCount}</TableCell>
                        </TableRow>
                      ))}
                      {statistics.topEquipments.length === 0 && (
                        <TableRow>
                          <TableCell colSpan={3} align="center">데이터 없음</TableCell>
                        </TableRow>
                      )}
                    </TableBody>
                  </Table>
                </TableContainer>
              </Grid>

              {/* Monthly Trend */}
              <Grid item xs={12} md={6}>
                <TableContainer component={Paper} variant="outlined">
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell colSpan={3}>
                          <Typography variant="subtitle1" fontWeight="bold">월별 고장 추이</Typography>
                        </TableCell>
                      </TableRow>
                      <TableRow>
                        <TableCell>월</TableCell>
                        <TableCell align="right">건수</TableCell>
                        <TableCell align="right">평균 수리시간(분)</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {trend.map((item) => (
                        <TableRow key={item.month}>
                          <TableCell>{item.month}</TableCell>
                          <TableCell align="right">{item.breakdownCount}</TableCell>
                          <TableCell align="right">
                            {item.avgRepairMinutes != null ? `${item.avgRepairMinutes}` : '-'}
                          </TableCell>
                        </TableRow>
                      ))}
                      {trend.length === 0 && (
                        <TableRow>
                          <TableCell colSpan={3} align="center">데이터 없음</TableCell>
                        </TableRow>
                      )}
                    </TableBody>
                  </Table>
                </TableContainer>
              </Grid>
            </Grid>
          </>
        )}

        {!statistics && !loading && (
          <Box sx={{ textAlign: 'center', py: 4 }}>
            <Typography color="textSecondary">조회 버튼을 클릭하여 통계를 확인하세요.</Typography>
          </Box>
        )}
      </Paper>

      {/* Snackbar */}
      <Snackbar
        open={snackbar.open}
        autoHideDuration={3000}
        onClose={() => setSnackbar({ ...snackbar, open: false })}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert severity={snackbar.severity} sx={{ width: '100%' }}>
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Box>
  );
};

export default BreakdownStatisticsPage;
