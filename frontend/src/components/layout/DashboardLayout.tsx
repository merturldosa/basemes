/**
 * Dashboard Layout
 * 메인 대시보드 레이아웃 (Sidebar + AppBar + Content)
 * @author Moon Myung-seop
 */

import { useState } from 'react';
import { Outlet, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import {
  Box,
  Drawer,
  AppBar,
  Toolbar,
  List,
  Typography,
  Divider,
  IconButton,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Avatar,
  Menu,
  MenuItem,
} from '@mui/material';
import {
  Menu as MenuIcon,
  Dashboard as DashboardIcon,
  People,
  Security,
  VpnKey,
  History,
  Palette,
  AccountCircle,
  Logout,
  Factory,
  Engineering,
  Assignment,
  Assessment,
  Rule as RuleIcon,
  FactCheck as FactCheckIcon,
  Warehouse,
  Inventory2,
  Inventory,
  SwapHoriz,
  AccountTree,
  Business,
  LocalShipping,
  Category,
  ShoppingCart,
  ShoppingBag,
  LocalShippingOutlined,
  LocationCity,
  CorporateFare,
  Unarchive,
  LocalShipping as LocalShippingIcon,
  BugReport,
  Build,
  Report,
  PrecisionManufacturing,
  Speed,
  ErrorOutline,
  Archive,
  Construction,
  Timeline,
  School,
  AssignmentInd,
  Scale,
  Analytics,
  BarChart,
  Checklist,
  EventNote,
  Straighten,
  Settings as SettingsIcon,
  Memory,
  Warning,
  TrendingUp,
  TrackChanges,
  VerifiedUser,
  Code,
  CalendarMonth,
  Approval,
  NotificationsActive,
  Description,
} from '@mui/icons-material';
import { useAuthStore } from '@/stores/authStore';
import LanguageSelector from '@/components/common/LanguageSelector';
import ThemeSelector from '@/components/common/ThemeSelector';

const DRAWER_WIDTH = 260;

const getAllMenuItems = (tenantId: string | undefined, t: (key: string) => string) => {
  const baseMenuItems = [
    { text: t('navigation.menu.dashboard'), icon: <DashboardIcon />, path: '/', divider: false },
    { text: t('navigation.menu.products'), icon: <Factory />, path: '/production/products', divider: false },
    { text: t('navigation.menu.processes'), icon: <Engineering />, path: '/production/processes', divider: false },
    { text: t('navigation.menu.workOrders'), icon: <Assignment />, path: '/production/work-orders', divider: false },
    { text: t('navigation.menu.workResults'), icon: <Assessment />, path: '/production/work-results', divider: false },
    { text: t('navigation.menu.productionSchedules'), icon: <Timeline />, path: '/production/schedules', divider: true },
    { text: t('navigation.menu.qualityStandards'), icon: <RuleIcon />, path: '/quality/standards', divider: false },
    { text: t('navigation.menu.qualityInspections'), icon: <FactCheckIcon />, path: '/quality/inspections', divider: true },
    { text: t('navigation.menu.warehouses'), icon: <Warehouse />, path: '/inventory/warehouses', divider: false },
    { text: t('navigation.menu.lots'), icon: <Inventory2 />, path: '/inventory/lots', divider: false },
    { text: t('navigation.menu.inventoryStatus'), icon: <Inventory />, path: '/inventory/status', divider: false },
    { text: t('navigation.menu.inventoryTransactions'), icon: <SwapHoriz />, path: '/inventory/transactions', divider: true },
    { text: t('navigation.menu.boms'), icon: <AccountTree />, path: '/bom/boms', divider: false },
    { text: t('navigation.menu.processRoutings'), icon: <Timeline />, path: '/routing/routings', divider: true },
    { text: t('navigation.menu.materials'), icon: <Category />, path: '/material/materials', divider: true },
    { text: t('navigation.menu.purchaseOrders'), icon: <ShoppingCart />, path: '/purchase/orders', divider: true },
    { text: t('navigation.menu.salesOrders'), icon: <ShoppingBag />, path: '/sales/orders', divider: false },
    { text: t('navigation.menu.deliveries'), icon: <LocalShippingOutlined />, path: '/sales/deliveries', divider: true },
    { text: t('navigation.menu.receiving'), icon: <Unarchive />, path: '/warehouse/receiving', divider: false },
    { text: t('navigation.menu.shipping'), icon: <LocalShippingIcon />, path: '/warehouse/shipping', divider: false },
    { text: t('navigation.menu.weighings'), icon: <Scale />, path: '/warehouse/weighings', divider: true },
    { text: t('navigation.menu.defects'), icon: <BugReport />, path: '/defect/defects', divider: false },
    { text: t('navigation.menu.afterSales'), icon: <Build />, path: '/defect/after-sales', divider: false },
    { text: t('navigation.menu.claims'), icon: <Report />, path: '/defect/claims', divider: true },
    { text: t('navigation.menu.equipments'), icon: <PrecisionManufacturing />, path: '/equipment/equipments', divider: false },
    { text: t('navigation.menu.equipmentOperations'), icon: <Speed />, path: '/equipment/operations', divider: false },
    { text: t('navigation.menu.equipmentInspections'), icon: <Build />, path: '/equipment/inspections', divider: false },
    { text: t('navigation.menu.inspectionForms'), icon: <Checklist />, path: '/equipment/inspection-forms', divider: false },
    { text: t('navigation.menu.inspectionPlans'), icon: <EventNote />, path: '/equipment/inspection-plans', divider: false },
    { text: t('navigation.menu.gauges'), icon: <Straighten />, path: '/equipment/gauges', divider: false },
    { text: t('navigation.menu.consumables'), icon: <SettingsIcon />, path: '/equipment/consumables', divider: false },
    { text: t('navigation.menu.equipmentParts'), icon: <Memory />, path: '/equipment/parts', divider: false },
    { text: t('navigation.menu.breakdowns'), icon: <Warning />, path: '/equipment/breakdowns', divider: false },
    { text: t('navigation.menu.breakdownStatistics'), icon: <TrendingUp />, path: '/equipment/breakdown-statistics', divider: false },
    { text: t('navigation.menu.deviations'), icon: <TrackChanges />, path: '/equipment/deviations', divider: false },
    { text: t('navigation.menu.externalCalibrations'), icon: <VerifiedUser />, path: '/equipment/external-calibrations', divider: false },
    { text: t('navigation.menu.downtimes'), icon: <ErrorOutline />, path: '/downtime/downtimes', divider: false },
    { text: t('navigation.menu.molds'), icon: <Archive />, path: '/mold/molds', divider: false },
    { text: t('navigation.menu.moldMaintenances'), icon: <Construction />, path: '/mold/maintenances', divider: false },
    { text: t('navigation.menu.moldProductionHistories'), icon: <Timeline />, path: '/mold/production-histories', divider: true },
    { text: t('navigation.menu.skillMatrix'), icon: <School />, path: '/hr/skill-matrix', divider: false },
    { text: t('navigation.menu.employeeSkills'), icon: <AssignmentInd />, path: '/hr/employee-skills', divider: true },
    { text: t('navigation.menu.analyticsDashboard'), icon: <Analytics />, path: '/analytics/dashboard', divider: false },
    { text: t('navigation.menu.statisticalReports'), icon: <BarChart />, path: '/analytics/reports', divider: true },
    { text: t('navigation.menu.customers'), icon: <Business />, path: '/business/customers', divider: false },
    { text: t('navigation.menu.suppliers'), icon: <LocalShipping />, path: '/business/suppliers', divider: true },
    { text: t('navigation.menu.sites'), icon: <LocationCity />, path: '/common/sites', divider: false },
    { text: t('navigation.menu.departments'), icon: <CorporateFare />, path: '/common/departments', divider: false },
    { text: t('navigation.menu.employees'), icon: <People />, path: '/common/employees', divider: false },
    { text: t('navigation.menu.commonCodes'), icon: <Code />, path: '/common/common-codes', divider: false },
    { text: t('navigation.menu.holidays'), icon: <CalendarMonth />, path: '/common/holidays', divider: false },
    { text: t('navigation.menu.approvals'), icon: <Approval />, path: '/common/approvals', divider: false },
    { text: t('navigation.menu.alarms'), icon: <NotificationsActive />, path: '/common/alarms', divider: false },
    { text: t('navigation.menu.sops'), icon: <Description />, path: '/common/sops', divider: true },
    { text: t('navigation.menu.users'), icon: <People />, path: '/users', divider: false },
    { text: t('navigation.menu.roles'), icon: <Security />, path: '/roles', divider: false },
    { text: t('navigation.menu.permissions'), icon: <VpnKey />, path: '/permissions', divider: false },
    { text: t('navigation.menu.auditLogs'), icon: <History />, path: '/audit-logs', divider: false },
  ];

  // 테마 설정은 개발회사(softice)에서만 접근 가능
  if (tenantId === 'softice') {
    baseMenuItems.push({ text: t('navigation.menu.themes'), icon: <Palette />, path: '/themes', divider: false });
  }

  return baseMenuItems;
};

export default function DashboardLayout() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { user, logout } = useAuthStore();
  const menuItems = getAllMenuItems(user?.tenantId, t);
  const [mobileOpen, setMobileOpen] = useState(false);
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);

  const handleDrawerToggle = () => {
    setMobileOpen(!mobileOpen);
  };

  const handleMenuClick = (path: string) => {
    navigate(path);
    setMobileOpen(false);
  };

  const handleProfileMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleProfileMenuClose = () => {
    setAnchorEl(null);
  };

  const handleLogout = async () => {
    handleProfileMenuClose();
    await logout();
    navigate('/login');
  };

  const drawer = (
    <Box>
      <Toolbar>
        <Typography variant="h6" noWrap component="div" fontWeight="bold">
          {t('common.appName')}
        </Typography>
      </Toolbar>
      <Divider />
      <List>
        {menuItems.map((item) => (
          <Box key={item.text}>
            <ListItem disablePadding>
              <ListItemButton onClick={() => handleMenuClick(item.path)}>
                <ListItemIcon>{item.icon}</ListItemIcon>
                <ListItemText primary={item.text} />
              </ListItemButton>
            </ListItem>
            {item.divider && <Divider sx={{ my: 1 }} />}
          </Box>
        ))}
      </List>
    </Box>
  );

  return (
    <Box sx={{ display: 'flex' }}>
      {/* App Bar */}
      <AppBar
        position="fixed"
        sx={{
          width: { sm: `calc(100% - ${DRAWER_WIDTH}px)` },
          ml: { sm: `${DRAWER_WIDTH}px` },
        }}
      >
        <Toolbar>
          <IconButton
            color="inherit"
            edge="start"
            onClick={handleDrawerToggle}
            sx={{ mr: 2, display: { sm: 'none' } }}
          >
            <MenuIcon />
          </IconButton>

          <Typography variant="h6" noWrap component="div" sx={{ flexGrow: 1 }}>
            {t('common.appFullName')}
          </Typography>

          {/* User Info & Language/Theme Selector */}
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            <Typography variant="body2">
              {user?.fullName} ({user?.tenantName})
            </Typography>
            <LanguageSelector />
            <ThemeSelector />
            <IconButton
              size="large"
              edge="end"
              onClick={handleProfileMenuOpen}
              color="inherit"
            >
              <Avatar sx={{ width: 32, height: 32 }}>
                {user?.fullName?.charAt(0)}
              </Avatar>
            </IconButton>
          </Box>

          {/* Profile Menu */}
          <Menu
            anchorEl={anchorEl}
            open={Boolean(anchorEl)}
            onClose={handleProfileMenuClose}
            anchorOrigin={{
              vertical: 'bottom',
              horizontal: 'right',
            }}
            transformOrigin={{
              vertical: 'top',
              horizontal: 'right',
            }}
          >
            <MenuItem onClick={handleProfileMenuClose}>
              <ListItemIcon>
                <AccountCircle fontSize="small" />
              </ListItemIcon>
              <ListItemText>{t('navigation.menu.profile')}</ListItemText>
            </MenuItem>
            <Divider />
            <MenuItem onClick={handleLogout}>
              <ListItemIcon>
                <Logout fontSize="small" />
              </ListItemIcon>
              <ListItemText>{t('auth.logout.button')}</ListItemText>
            </MenuItem>
          </Menu>
        </Toolbar>
      </AppBar>

      {/* Sidebar Drawer */}
      <Box
        component="nav"
        sx={{ width: { sm: DRAWER_WIDTH }, flexShrink: { sm: 0 } }}
      >
        {/* Mobile Drawer */}
        <Drawer
          variant="temporary"
          open={mobileOpen}
          onClose={handleDrawerToggle}
          ModalProps={{
            keepMounted: true,
          }}
          sx={{
            display: { xs: 'block', sm: 'none' },
            '& .MuiDrawer-paper': {
              boxSizing: 'border-box',
              width: DRAWER_WIDTH,
            },
          }}
        >
          {drawer}
        </Drawer>

        {/* Desktop Drawer */}
        <Drawer
          variant="permanent"
          sx={{
            display: { xs: 'none', sm: 'block' },
            '& .MuiDrawer-paper': {
              boxSizing: 'border-box',
              width: DRAWER_WIDTH,
            },
          }}
          open
        >
          {drawer}
        </Drawer>
      </Box>

      {/* Main Content */}
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          p: 3,
          width: { sm: `calc(100% - ${DRAWER_WIDTH}px)` },
        }}
      >
        <Toolbar />
        <Outlet />
      </Box>
    </Box>
  );
}
