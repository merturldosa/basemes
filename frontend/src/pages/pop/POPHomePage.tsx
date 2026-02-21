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
import { useTranslation } from 'react-i18next';

const POPHomePage: React.FC = () => {
  const navigate = useNavigate();
  const { t } = useTranslation();

  const quickActions = [
    {
      title: t('pages.popHome.actions.startWork'),
      description: t('pages.popHome.actions.startWorkDesc'),
      icon: <StartIcon sx={{ fontSize: 48 }} />,
      color: 'success.main',
      path: '/pop/work-orders',
    },
    {
      title: t('pages.popHome.actions.barcodeScan'),
      description: t('pages.popHome.actions.barcodeScanDesc'),
      icon: <ScannerIcon sx={{ fontSize: 48 }} />,
      color: 'primary.main',
      path: '/pop/scanner',
    },
    {
      title: t('pages.popHome.actions.sopCheck'),
      description: t('pages.popHome.actions.sopCheckDesc'),
      icon: <CheckIcon sx={{ fontSize: 48 }} />,
      color: 'warning.main',
      path: '/pop/sop',
    },
    {
      title: t('pages.popHome.actions.performance'),
      description: t('pages.popHome.actions.performanceDesc'),
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
          {t('pages.popHome.title')}
        </Typography>
        <Typography variant="body1" color="text.secondary">
          {t('pages.popHome.subtitle')}
        </Typography>
      </Box>

      {/* Current Status */}
      <Card sx={{ mb: 4, bgcolor: 'primary.main', color: 'primary.contrastText' }}>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            {t('pages.popHome.currentStatus')}
          </Typography>
          <Box sx={{ display: 'flex', gap: 2, mt: 2 }}>
            <Chip label={t('pages.popHome.waiting')} color="default" sx={{ bgcolor: 'white', color: 'text.primary' }} />
            <Chip label={t('pages.popHome.todayProduction', { count: 0 })} color="default" sx={{ bgcolor: 'white', color: 'text.primary' }} />
          </Box>
        </CardContent>
      </Card>

      {/* Quick Actions */}
      <Typography variant="h6" gutterBottom fontWeight="bold" sx={{ mb: 2 }}>
        {t('pages.popHome.quickActions')}
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
            {t('pages.popHome.help.title')}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            {t('pages.popHome.help.line1')}
            <br />
            {t('pages.popHome.help.line2')}
            <br />
            {t('pages.popHome.help.line3')}
            <br />
            {t('pages.popHome.help.line4')}
          </Typography>
        </CardContent>
      </Card>
    </Box>
  );
};

export default POPHomePage;
