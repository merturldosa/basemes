/**
 * Alarm Management Page
 * 알람 관리 페이지
 */

import React, { useState, useEffect } from 'react';
import {
  Box,
  Paper,
  Typography,
  Tabs,
  Tab,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Grid,
  Card,
  CardContent,
  Chip,
  IconButton,
  Badge,
  Alert,
  Divider,
  List,
  ListItem,
  ListItemText,
  ListItemSecondaryAction,
} from '@mui/material';
import {
  DataGrid,
  GridColDef,
  GridRenderCellParams,
  GridToolbar,
} from '@mui/x-data-grid';
import {
  Notifications as NotificationsIcon,
  NotificationsActive as NotificationsActiveIcon,
  MarkEmailRead as MarkReadIcon,
  DoneAll as MarkAllReadIcon,
  Info as InfoIcon,
} from '@mui/icons-material';
import {
  alarmService,
  AlarmHistory,
  AlarmStatistics,
  getAlarmTypeLabel,
  getAlarmTypeColor,
  getPriorityLabel,
  getPriorityColor,
  formatDateTime,
  getRelativeTime,
} from '../../services/alarmService';
import { useTranslation } from 'react-i18next';
import { useAuthStore } from '@/stores/authStore';

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

function TabPanel(props: TabPanelProps) {
  const { children, value, index, ...other } = props;
  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`alarm-tabpanel-${index}`}
      aria-labelledby={`alarm-tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
    </div>
  );
}

const AlarmPage: React.FC = () => {
  const { t } = useTranslation();
  const { user } = useAuthStore();
  const tenantId = user?.tenantId ?? '';
  const userId = user?.userId ?? 0;

  const [tabValue, setTabValue] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  // Alarm data
  const [unreadAlarms, setUnreadAlarms] = useState<AlarmHistory[]>([]);
  const [allAlarms, setAllAlarms] = useState<AlarmHistory[]>([]);
  const [recentAlarms, setRecentAlarms] = useState<AlarmHistory[]>([]);
  const [statistics, setStatistics] = useState<AlarmStatistics | null>(null);
  const [unreadCount, setUnreadCount] = useState<number>(0);

  // Dialogs
  const [detailDialog, setDetailDialog] = useState(false);
  const [selectedAlarm, setSelectedAlarm] = useState<AlarmHistory | null>(null);

  // Load data
  useEffect(() => {
    loadData();
    // Auto-refresh every 30 seconds
    const interval = setInterval(loadData, 30000);
    return () => clearInterval(interval);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const [unread, all, recent, stats, count] = await Promise.all([
        alarmService.getUnreadAlarms(tenantId, userId),
        alarmService.getAlarms(tenantId, userId),
        alarmService.getRecentAlarms(tenantId, userId),
        alarmService.getStatistics(tenantId, userId),
        alarmService.countUnreadAlarms(tenantId, userId),
      ]);

      setUnreadAlarms(unread);
      setAllAlarms(all);
      setRecentAlarms(recent);
      setStatistics(stats);
      setUnreadCount(count);
      setError(null);
    } catch (err: any) {
      setError(err.message || t('pages.alarm.errors.loadFailed'));
    } finally {
      setLoading(false);
    }
  };

  // Handle mark as read
  const handleMarkAsRead = async (alarmId: number) => {
    try {
      await alarmService.markAsRead(alarmId);
      setSuccess(t('pages.alarm.messages.markedAsRead'));
      await loadData();
    } catch (err: any) {
      setError(err.message || t('pages.alarm.messages.markReadFailed'));
    }
  };

  // Handle mark all as read
  const handleMarkAllAsRead = async () => {
    try {
      setLoading(true);
      await alarmService.markAllAsRead(tenantId, userId);
      setSuccess(t('pages.alarm.messages.allMarkedAsRead'));
      await loadData();
    } catch (err: any) {
      setError(err.message || t('pages.alarm.messages.markAllReadFailed'));
    } finally {
      setLoading(false);
    }
  };

  // Handle view details
  const handleViewDetails = async (alarm: AlarmHistory) => {
    setSelectedAlarm(alarm);
    setDetailDialog(true);

    // Mark as read when viewing
    if (!alarm.isRead) {
      await handleMarkAsRead(alarm.alarmId);
    }
  };

  // DataGrid columns
  const columns: GridColDef[] = [
    {
      field: 'alarmType',
      headerName: t('pages.alarm.fields.alarmType'),
      width: 120,
      renderCell: (params: GridRenderCellParams) => (
        <Chip
          label={getAlarmTypeLabel(params.value)}
          color={getAlarmTypeColor(params.value)}
          size="small"
        />
      ),
    },
    {
      field: 'priority',
      headerName: t('pages.alarm.fields.priority'),
      width: 100,
      renderCell: (params: GridRenderCellParams) => (
        <Chip
          label={getPriorityLabel(params.value)}
          color={getPriorityColor(params.value)}
          size="small"
        />
      ),
    },
    {
      field: 'title',
      headerName: t('pages.alarm.fields.title'),
      flex: 1,
      minWidth: 300,
      renderCell: (params: GridRenderCellParams) => (
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          {!params.row.isRead && (
            <Badge color="primary" variant="dot" />
          )}
          <Typography
            variant="body2"
            sx={{
              fontWeight: params.row.isRead ? 'normal' : 'bold',
              overflow: 'hidden',
              textOverflow: 'ellipsis',
            }}
          >
            {params.value}
          </Typography>
        </Box>
      ),
    },
    {
      field: 'message',
      headerName: t('pages.alarm.fields.message'),
      flex: 1,
      minWidth: 250,
    },
    {
      field: 'createdAt',
      headerName: t('pages.alarm.fields.time'),
      width: 150,
      renderCell: (params: GridRenderCellParams) => (
        <Typography variant="body2" color="text.secondary">
          {getRelativeTime(params.value)}
        </Typography>
      ),
    },
    {
      field: 'isRead',
      headerName: t('pages.alarm.fields.isRead'),
      width: 80,
      renderCell: (params: GridRenderCellParams) => (
        params.value ? (
          <Chip label={t('pages.alarm.status.read')} size="small" color="default" />
        ) : (
          <Chip label={t('pages.alarm.status.unread')} size="small" color="primary" />
        )
      ),
    },
    {
      field: 'actions',
      headerName: t('common.labels.actions'),
      width: 120,
      sortable: false,
      renderCell: (params: GridRenderCellParams) => (
        <Box>
          <IconButton
            size="small"
            onClick={() => handleViewDetails(params.row)}
            title={t('pages.alarm.actions.viewDetail')}
          >
            <InfoIcon />
          </IconButton>
          {!params.row.isRead && (
            <IconButton
              size="small"
              onClick={() => handleMarkAsRead(params.row.alarmId)}
              title={t('pages.alarm.actions.markRead')}
            >
              <MarkReadIcon />
            </IconButton>
          )}
        </Box>
      ),
    },
  ];

  return (
    <Box sx={{ p: 3 }}>
      {/* Header */}
      <Box sx={{ mb: 3, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <NotificationsActiveIcon sx={{ fontSize: 40, color: 'primary.main' }} />
          <Box>
            <Typography variant="h4" gutterBottom>
              {t('pages.alarm.title')}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              {t('pages.alarm.subtitle')}
            </Typography>
          </Box>
        </Box>
        <Box sx={{ display: 'flex', gap: 1 }}>
          <Badge badgeContent={unreadCount} color="error">
            <NotificationsIcon />
          </Badge>
          {unreadCount > 0 && (
            <Button
              variant="outlined"
              startIcon={<MarkAllReadIcon />}
              onClick={handleMarkAllAsRead}
            >
              {t('pages.alarm.actions.markAllRead')}
            </Button>
          )}
        </Box>
      </Box>

      {/* Alert Messages */}
      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}
      {success && (
        <Alert severity="success" sx={{ mb: 2 }} onClose={() => setSuccess(null)}>
          {success}
        </Alert>
      )}

      {/* Statistics Cards */}
      {statistics && (
        <Grid container spacing={2} sx={{ mb: 3 }}>
          <Grid item xs={12} sm={6} md={2}>
            <Card>
              <CardContent>
                <Typography color="text.secondary" gutterBottom variant="body2">
                  {t('pages.alarm.statistics.unread')}
                </Typography>
                <Typography variant="h4" color="error.main">
                  {statistics.unreadCount}
                </Typography>
              </CardContent>
            </Card>
          </Grid>
          <Grid item xs={12} sm={6} md={2}>
            <Card>
              <CardContent>
                <Typography color="text.secondary" gutterBottom variant="body2">
                  {t('pages.alarm.statistics.total')}
                </Typography>
                <Typography variant="h4">
                  {statistics.totalCount}
                </Typography>
              </CardContent>
            </Card>
          </Grid>
          <Grid item xs={12} sm={6} md={2}>
            <Card>
              <CardContent>
                <Typography color="text.secondary" gutterBottom variant="body2">
                  {t('pages.alarm.statistics.approval')}
                </Typography>
                <Typography variant="h4" color="primary.main">
                  {statistics.approvalCount}
                </Typography>
              </CardContent>
            </Card>
          </Grid>
          <Grid item xs={12} sm={6} md={2}>
            <Card>
              <CardContent>
                <Typography color="text.secondary" gutterBottom variant="body2">
                  {t('pages.alarm.statistics.quality')}
                </Typography>
                <Typography variant="h4" color="warning.main">
                  {statistics.qualityCount}
                </Typography>
              </CardContent>
            </Card>
          </Grid>
          <Grid item xs={12} sm={6} md={2}>
            <Card>
              <CardContent>
                <Typography color="text.secondary" gutterBottom variant="body2">
                  {t('pages.alarm.statistics.production')}
                </Typography>
                <Typography variant="h4" color="success.main">
                  {statistics.productionCount}
                </Typography>
              </CardContent>
            </Card>
          </Grid>
          <Grid item xs={12} sm={6} md={2}>
            <Card>
              <CardContent>
                <Typography color="text.secondary" gutterBottom variant="body2">
                  {t('pages.alarm.statistics.inventory')}
                </Typography>
                <Typography variant="h4" color="error.main">
                  {statistics.inventoryCount}
                </Typography>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      )}

      {/* Tabs */}
      <Paper sx={{ mb: 2 }}>
        <Tabs value={tabValue} onChange={(e, newValue) => setTabValue(newValue)}>
          <Tab
            label={
              <Badge badgeContent={unreadCount} color="error">
                {t('pages.alarm.tabs.unread')}
              </Badge>
            }
          />
          <Tab label={t('pages.alarm.tabs.all')} />
          <Tab label={t('pages.alarm.tabs.recent')} />
          <Tab label={t('pages.alarm.tabs.statistics')} />
        </Tabs>
      </Paper>

      {/* Tab 1: Unread Alarms */}
      <TabPanel value={tabValue} index={0}>
        <Paper sx={{ width: '100%' }}>
          <DataGrid
            rows={unreadAlarms}
            columns={columns}
            getRowId={(row) => row.alarmId}
            loading={loading}
            autoHeight
            pageSizeOptions={[10, 25, 50]}
            initialState={{
              pagination: { paginationModel: { pageSize: 25 } },
            }}
            slots={{ toolbar: GridToolbar }}
            slotProps={{
              toolbar: {
                showQuickFilter: true,
                quickFilterProps: { debounceMs: 500 },
              },
            }}
            sx={{
              '& .MuiDataGrid-row:hover': {
                cursor: 'pointer',
              },
            }}
          />
        </Paper>
      </TabPanel>

      {/* Tab 2: All Alarms */}
      <TabPanel value={tabValue} index={1}>
        <Paper sx={{ width: '100%' }}>
          <DataGrid
            rows={allAlarms}
            columns={columns}
            getRowId={(row) => row.alarmId}
            loading={loading}
            autoHeight
            pageSizeOptions={[10, 25, 50]}
            initialState={{
              pagination: { paginationModel: { pageSize: 25 } },
            }}
            slots={{ toolbar: GridToolbar }}
            slotProps={{
              toolbar: {
                showQuickFilter: true,
                quickFilterProps: { debounceMs: 500 },
              },
            }}
          />
        </Paper>
      </TabPanel>

      {/* Tab 3: Recent Alarms (7 days) */}
      <TabPanel value={tabValue} index={2}>
        <Paper sx={{ width: '100%' }}>
          <DataGrid
            rows={recentAlarms}
            columns={columns}
            getRowId={(row) => row.alarmId}
            loading={loading}
            autoHeight
            pageSizeOptions={[10, 25, 50]}
            initialState={{
              pagination: { paginationModel: { pageSize: 25 } },
            }}
            slots={{ toolbar: GridToolbar }}
            slotProps={{
              toolbar: {
                showQuickFilter: true,
                quickFilterProps: { debounceMs: 500 },
              },
            }}
          />
        </Paper>
      </TabPanel>

      {/* Tab 4: Statistics */}
      <TabPanel value={tabValue} index={3}>
        {statistics && (
          <Grid container spacing={3}>
            <Grid item xs={12} md={6}>
              <Card>
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    {t('pages.alarm.statistics.overallStats')}
                  </Typography>
                  <Divider sx={{ my: 2 }} />
                  <List>
                    <ListItem>
                      <ListItemText primary={t('pages.alarm.statistics.totalAlarms')} />
                      <ListItemSecondaryAction>
                        <Typography variant="h6">{statistics.totalCount}</Typography>
                      </ListItemSecondaryAction>
                    </ListItem>
                    <ListItem>
                      <ListItemText primary={t('pages.alarm.statistics.unreadAlarms')} />
                      <ListItemSecondaryAction>
                        <Typography variant="h6" color="error.main">
                          {statistics.unreadCount}
                        </Typography>
                      </ListItemSecondaryAction>
                    </ListItem>
                    <ListItem>
                      <ListItemText primary={t('pages.alarm.statistics.readAlarms')} />
                      <ListItemSecondaryAction>
                        <Typography variant="h6">{statistics.readCount}</Typography>
                      </ListItemSecondaryAction>
                    </ListItem>
                    <ListItem>
                      <ListItemText primary={t('pages.alarm.statistics.readRate')} />
                      <ListItemSecondaryAction>
                        <Typography variant="h6" color="success.main">
                          {statistics.readRate.toFixed(1)}%
                        </Typography>
                      </ListItemSecondaryAction>
                    </ListItem>
                  </List>
                </CardContent>
              </Card>
            </Grid>

            <Grid item xs={12} md={6}>
              <Card>
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    {t('pages.alarm.statistics.typeStats')}
                  </Typography>
                  <Divider sx={{ my: 2 }} />
                  <List>
                    <ListItem>
                      <ListItemText primary={t('pages.alarm.statistics.approvalAlarm')} />
                      <ListItemSecondaryAction>
                        <Chip
                          label={statistics.approvalCount}
                          color="primary"
                          size="small"
                        />
                      </ListItemSecondaryAction>
                    </ListItem>
                    <ListItem>
                      <ListItemText primary={t('pages.alarm.statistics.qualityAlarm')} />
                      <ListItemSecondaryAction>
                        <Chip
                          label={statistics.qualityCount}
                          color="warning"
                          size="small"
                        />
                      </ListItemSecondaryAction>
                    </ListItem>
                    <ListItem>
                      <ListItemText primary={t('pages.alarm.statistics.productionAlarm')} />
                      <ListItemSecondaryAction>
                        <Chip
                          label={statistics.productionCount}
                          color="success"
                          size="small"
                        />
                      </ListItemSecondaryAction>
                    </ListItem>
                    <ListItem>
                      <ListItemText primary={t('pages.alarm.statistics.inventoryAlarm')} />
                      <ListItemSecondaryAction>
                        <Chip
                          label={statistics.inventoryCount}
                          color="error"
                          size="small"
                        />
                      </ListItemSecondaryAction>
                    </ListItem>
                  </List>
                </CardContent>
              </Card>
            </Grid>
          </Grid>
        )}
      </TabPanel>

      {/* Detail Dialog */}
      <Dialog
        open={detailDialog}
        onClose={() => setDetailDialog(false)}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <NotificationsIcon />
            {t('pages.alarm.detail.title')}
          </Box>
        </DialogTitle>
        <DialogContent dividers>
          {selectedAlarm && (
            <Grid container spacing={2}>
              <Grid item xs={12}>
                <Typography variant="subtitle2" color="text.secondary">
                  {t('pages.alarm.fields.alarmType')}
                </Typography>
                <Chip
                  label={getAlarmTypeLabel(selectedAlarm.alarmType)}
                  color={getAlarmTypeColor(selectedAlarm.alarmType)}
                  sx={{ mt: 1 }}
                />
              </Grid>
              <Grid item xs={12}>
                <Typography variant="subtitle2" color="text.secondary">
                  {t('pages.alarm.fields.priority')}
                </Typography>
                <Chip
                  label={getPriorityLabel(selectedAlarm.priority)}
                  color={getPriorityColor(selectedAlarm.priority)}
                  sx={{ mt: 1 }}
                />
              </Grid>
              <Grid item xs={12}>
                <Typography variant="subtitle2" color="text.secondary">
                  {t('pages.alarm.fields.title')}
                </Typography>
                <Typography variant="h6" sx={{ mt: 1 }}>
                  {selectedAlarm.title}
                </Typography>
              </Grid>
              <Grid item xs={12}>
                <Typography variant="subtitle2" color="text.secondary">
                  {t('pages.alarm.fields.message')}
                </Typography>
                <Typography variant="body1" sx={{ mt: 1 }}>
                  {selectedAlarm.message}
                </Typography>
              </Grid>
              {selectedAlarm.referenceNo && (
                <Grid item xs={12}>
                  <Typography variant="subtitle2" color="text.secondary">
                    {t('pages.alarm.fields.referenceNo')}
                  </Typography>
                  <Typography variant="body1" sx={{ mt: 1 }}>
                    {selectedAlarm.referenceType}: {selectedAlarm.referenceNo}
                  </Typography>
                </Grid>
              )}
              <Grid item xs={12} sm={6}>
                <Typography variant="subtitle2" color="text.secondary">
                  {t('pages.alarm.fields.sentTime')}
                </Typography>
                <Typography variant="body2" sx={{ mt: 1 }}>
                  {formatDateTime(selectedAlarm.createdAt)}
                </Typography>
              </Grid>
              {selectedAlarm.readAt && (
                <Grid item xs={12} sm={6}>
                  <Typography variant="subtitle2" color="text.secondary">
                    {t('pages.alarm.fields.readTime')}
                  </Typography>
                  <Typography variant="body2" sx={{ mt: 1 }}>
                    {formatDateTime(selectedAlarm.readAt)}
                  </Typography>
                </Grid>
              )}
              <Grid item xs={12}>
                <Typography variant="subtitle2" color="text.secondary">
                  {t('pages.alarm.fields.sentChannel')}
                </Typography>
                <Box sx={{ mt: 1, display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                  {selectedAlarm.sentViaEmail && <Chip label={t('pages.alarm.channels.email')} size="small" />}
                  {selectedAlarm.sentViaSms && <Chip label={t('pages.alarm.channels.sms')} size="small" />}
                  {selectedAlarm.sentViaPush && <Chip label={t('pages.alarm.channels.push')} size="small" />}
                  {selectedAlarm.sentViaSystem && <Chip label={t('pages.alarm.channels.system')} size="small" />}
                </Box>
              </Grid>
            </Grid>
          )}
        </DialogContent>
        <DialogActions>
          {selectedAlarm && !selectedAlarm.isRead && (
            <Button
              startIcon={<MarkReadIcon />}
              onClick={() => {
                handleMarkAsRead(selectedAlarm.alarmId);
                setDetailDialog(false);
              }}
            >
              {t('pages.alarm.actions.markRead')}
            </Button>
          )}
          <Button onClick={() => setDetailDialog(false)}>{t('common.buttons.close')}</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default AlarmPage;
