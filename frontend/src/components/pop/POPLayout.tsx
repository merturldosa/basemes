/**
 * POP Layout Component
 * Point of Production - Mobile/Tablet optimized layout
 * @author Moon Myung-seop
 */

import { useState } from 'react';
import { Outlet, useNavigate } from 'react-router-dom';
import {
  Box,
  AppBar,
  Toolbar,
  Typography,
  IconButton,
  Drawer,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Chip,
  Badge,
  Divider,
  Avatar,
  useTheme,
  useMediaQuery,
} from '@mui/material';
import {
  Menu as MenuIcon,
  Home as HomeIcon,
  Assignment as WorkOrderIcon,
  QrCodeScanner as ScannerIcon,
  CheckCircle as CheckIcon,
  BarChart as StatsIcon,
  Logout as LogoutIcon,
  WifiOff as OfflineIcon,
  Wifi as OnlineIcon,
  Person as PersonIcon,
} from '@mui/icons-material';
import { useAuthStore } from '@/stores/authStore';

interface POPLayoutProps {
  children?: React.ReactNode;
}

const POPLayout: React.FC<POPLayoutProps> = () => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const navigate = useNavigate();
  const { user, logout } = useAuthStore();
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [isOnline, setIsOnline] = useState(navigator.onLine);
  const [currentWorkOrder, setCurrentWorkOrder] = useState<string | null>(null);

  // Monitor online/offline status
  useState(() => {
    const handleOnline = () => setIsOnline(true);
    const handleOffline = () => setIsOnline(false);

    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);

    return () => {
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
    };
  });

  const menuItems = [
    { text: 'POP 홈', icon: <HomeIcon />, path: '/pop' },
    { text: '작업 지시', icon: <WorkOrderIcon />, path: '/pop/work-orders' },
    { text: '바코드 스캔', icon: <ScannerIcon />, path: '/pop/scanner' },
    { text: 'SOP 체크리스트', icon: <CheckIcon />, path: '/pop/sop' },
    { text: '생산 실적', icon: <StatsIcon />, path: '/pop/performance' },
  ];

  const handleDrawerToggle = () => {
    setDrawerOpen(!drawerOpen);
  };

  const handleMenuClick = (path: string) => {
    navigate(path);
    setDrawerOpen(false);
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const drawer = (
    <Box sx={{ width: 280, height: '100%', bgcolor: 'background.default' }}>
      {/* User Info */}
      <Box sx={{ p: 3, bgcolor: 'primary.main', color: 'primary.contrastText' }}>
        <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
          <Avatar sx={{ width: 56, height: 56, mr: 2, bgcolor: 'primary.light' }}>
            <PersonIcon />
          </Avatar>
          <Box>
            <Typography variant="h6">{user?.fullName || user?.username}</Typography>
            <Typography variant="body2" sx={{ opacity: 0.9 }}>
              {user?.departmentName || '생산부'}
            </Typography>
          </Box>
        </Box>

        {/* Current Work Order */}
        {currentWorkOrder && (
          <Chip
            label={`작업 중: ${currentWorkOrder}`}
            color="secondary"
            size="small"
            sx={{ fontWeight: 'bold' }}
          />
        )}
      </Box>

      <Divider />

      {/* Menu Items */}
      <List sx={{ px: 1, py: 2 }}>
        {menuItems.map((item) => (
          <ListItem key={item.text} disablePadding sx={{ mb: 1 }}>
            <ListItemButton
              onClick={() => handleMenuClick(item.path)}
              sx={{
                borderRadius: 2,
                minHeight: 56,
                '&:hover': {
                  bgcolor: 'action.hover',
                },
              }}
            >
              <ListItemIcon sx={{ minWidth: 48, color: 'primary.main' }}>
                {item.icon}
              </ListItemIcon>
              <ListItemText
                primary={item.text}
                primaryTypographyProps={{
                  fontSize: '1rem',
                  fontWeight: 500,
                }}
              />
            </ListItemButton>
          </ListItem>
        ))}
      </List>

      <Divider />

      {/* Logout */}
      <List sx={{ px: 1, py: 2 }}>
        <ListItem disablePadding>
          <ListItemButton
            onClick={handleLogout}
            sx={{
              borderRadius: 2,
              minHeight: 56,
              color: 'error.main',
            }}
          >
            <ListItemIcon sx={{ minWidth: 48, color: 'error.main' }}>
              <LogoutIcon />
            </ListItemIcon>
            <ListItemText
              primary="로그아웃"
              primaryTypographyProps={{
                fontSize: '1rem',
                fontWeight: 500,
              }}
            />
          </ListItemButton>
        </ListItem>
      </List>
    </Box>
  );

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh', bgcolor: 'background.default' }}>
      {/* App Bar */}
      <AppBar
        position="fixed"
        sx={{
          zIndex: theme.zIndex.drawer + 1,
          bgcolor: 'primary.main',
        }}
      >
        <Toolbar sx={{ minHeight: { xs: 64, sm: 72 } }}>
          <IconButton
            color="inherit"
            edge="start"
            onClick={handleDrawerToggle}
            sx={{ mr: 2 }}
          >
            <MenuIcon fontSize="large" />
          </IconButton>

          <Typography variant="h5" component="div" sx={{ flexGrow: 1, fontWeight: 'bold' }}>
            POP 현장 시스템
          </Typography>

          {/* Online/Offline Status */}
          <Badge
            badgeContent={isOnline ? '온라인' : '오프라인'}
            color={isOnline ? 'success' : 'error'}
            sx={{ mr: 2 }}
          >
            <IconButton color="inherit">
              {isOnline ? <OnlineIcon /> : <OfflineIcon />}
            </IconButton>
          </Badge>

          {/* User Avatar */}
          <Avatar sx={{ bgcolor: 'primary.light' }}>
            {user?.username?.[0]?.toUpperCase()}
          </Avatar>
        </Toolbar>
      </AppBar>

      {/* Drawer */}
      <Drawer
        variant={isMobile ? 'temporary' : 'temporary'}
        open={drawerOpen}
        onClose={handleDrawerToggle}
        ModalProps={{
          keepMounted: true, // Better mobile performance
        }}
        sx={{
          '& .MuiDrawer-paper': {
            width: 280,
            boxSizing: 'border-box',
          },
        }}
      >
        {drawer}
      </Drawer>

      {/* Main Content */}
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          width: '100%',
          mt: { xs: '64px', sm: '72px' },
          minHeight: 'calc(100vh - 64px)',
          bgcolor: 'background.default',
        }}
      >
        <Outlet />
      </Box>
    </Box>
  );
};

export default POPLayout;
