/**
 * POP Home Page
 * Point of Production - Main dashboard for field workers
 * @author Moon Myung-seop
 */

import { Box, Card, CardContent, Typography, Grid, Chip } from '@mui/material';
import {
  QrCodeScanner as ScannerIcon,
  CheckCircle as CheckIcon,
  BarChart as StatsIcon,
  PlayArrow as StartIcon,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';

const POPHomePage: React.FC = () => {
  const navigate = useNavigate();

  const quickActions = [
    {
      title: '작업 시작',
      description: '새로운 작업 지시를 시작합니다',
      icon: <StartIcon sx={{ fontSize: 48 }} />,
      color: 'success.main',
      path: '/pop/work-orders',
    },
    {
      title: '바코드 스캔',
      description: '작업 지시서, LOT, 제품 스캔',
      icon: <ScannerIcon sx={{ fontSize: 48 }} />,
      color: 'primary.main',
      path: '/pop/scanner',
    },
    {
      title: 'SOP 확인',
      description: '작업 표준서 체크리스트',
      icon: <CheckIcon sx={{ fontSize: 48 }} />,
      color: 'warning.main',
      path: '/pop/sop',
    },
    {
      title: '생산 실적',
      description: '오늘의 생산 현황 확인',
      icon: <StatsIcon sx={{ fontSize: 48 }} />,
      color: 'info.main',
      path: '/pop/performance',
    },
  ];

  return (
    <Box sx={{ p: 3 }}>
      {/* Header */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" gutterBottom fontWeight="bold">
          POP 현장 시스템
        </Typography>
        <Typography variant="body1" color="text.secondary">
          생산 현장 작업을 시작하세요
        </Typography>
      </Box>

      {/* Current Status */}
      <Card sx={{ mb: 4, bgcolor: 'primary.main', color: 'primary.contrastText' }}>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            현재 작업 상태
          </Typography>
          <Box sx={{ display: 'flex', gap: 2, mt: 2 }}>
            <Chip label="작업 대기 중" color="default" sx={{ bgcolor: 'white', color: 'text.primary' }} />
            <Chip label="금일 생산: 0 EA" color="default" sx={{ bgcolor: 'white', color: 'text.primary' }} />
          </Box>
        </CardContent>
      </Card>

      {/* Quick Actions */}
      <Typography variant="h6" gutterBottom fontWeight="bold" sx={{ mb: 2 }}>
        빠른 작업
      </Typography>

      <Grid container spacing={3}>
        {quickActions.map((action) => (
          <Grid item xs={12} sm={6} key={action.title}>
            <Card
              sx={{
                cursor: 'pointer',
                transition: 'transform 0.2s, box-shadow 0.2s',
                '&:hover': {
                  transform: 'translateY(-4px)',
                  boxShadow: 4,
                },
              }}
              onClick={() => navigate(action.path)}
            >
              <CardContent sx={{ textAlign: 'center', py: 4 }}>
                <Box
                  sx={{
                    display: 'flex',
                    justifyContent: 'center',
                    alignItems: 'center',
                    width: 80,
                    height: 80,
                    borderRadius: '50%',
                    bgcolor: action.color,
                    color: 'white',
                    mx: 'auto',
                    mb: 2,
                  }}
                >
                  {action.icon}
                </Box>
                <Typography variant="h6" gutterBottom fontWeight="bold">
                  {action.title}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  {action.description}
                </Typography>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      {/* Help Section */}
      <Card sx={{ mt: 4, bgcolor: 'grey.100' }}>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            💡 도움말
          </Typography>
          <Typography variant="body2" color="text.secondary">
            • 작업을 시작하려면 <strong>"작업 시작"</strong> 버튼을 클릭하세요
            <br />
            • 바코드 스캔으로 빠르게 작업 지시를 불러올 수 있습니다
            <br />
            • 인터넷 연결이 끊어져도 작업을 계속할 수 있습니다 (오프라인 모드)
            <br />• 문제가 발생하면 현장 관리자에게 문의하세요
          </Typography>
        </CardContent>
      </Card>
    </Box>
  );
};

export default POPHomePage;
