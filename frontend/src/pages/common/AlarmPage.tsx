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
      setError(err.message || '데이터 로드 실패');
    } finally {
      setLoading(false);
    }
  };

  // Handle mark as read
  const handleMarkAsRead = async (alarmId: number) => {
    try {
      await alarmService.markAsRead(alarmId);
      setSuccess('알람을 읽음으로 표시했습니다.');
      await loadData();
    } catch (err: any) {
      setError(err.message || '알람 읽음 처리 실패');
    }
  };

  // Handle mark all as read
  const handleMarkAllAsRead = async () => {
    try {
      setLoading(true);
      await alarmService.markAllAsRead(tenantId, userId);
      setSuccess('모든 알람을 읽음으로 표시했습니다.');
      await loadData();
    } catch (err: any) {
      setError(err.message || '전체 알람 읽음 처리 실패');
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
      headerName: '알람 타입',
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
      headerName: '우선순위',
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
      headerName: '제목',
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
      headerName: '내용',
      flex: 1,
      minWidth: 250,
    },
    {
      field: 'createdAt',
      headerName: '시간',
      width: 150,
      renderCell: (params: GridRenderCellParams) => (
        <Typography variant="body2" color="text.secondary">
          {getRelativeTime(params.value)}
        </Typography>
      ),
    },
    {
      field: 'isRead',
      headerName: '읽음',
      width: 80,
      renderCell: (params: GridRenderCellParams) => (
        params.value ? (
          <Chip label="읽음" size="small" color="default" />
        ) : (
          <Chip label="읽지 않음" size="small" color="primary" />
        )
      ),
    },
    {
      field: 'actions',
      headerName: '작업',
      width: 120,
      sortable: false,
      renderCell: (params: GridRenderCellParams) => (
        <Box>
          <IconButton
            size="small"
            onClick={() => handleViewDetails(params.row)}
            title="상세 보기"
          >
            <InfoIcon />
          </IconButton>
          {!params.row.isRead && (
            <IconButton
              size="small"
              onClick={() => handleMarkAsRead(params.row.alarmId)}
              title="읽음 표시"
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
              알람 관리
            </Typography>
            <Typography variant="body2" color="text.secondary">
              시스템 알림 및 알람 설정을 관리합니다
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
              모두 읽음 표시
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
                  읽지 않음
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
                  전체
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
                  결재
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
                  품질
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
                  생산
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
                  재고
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
                읽지 않은 알람
              </Badge>
            }
          />
          <Tab label="전체 알람" />
          <Tab label="최근 알람 (7일)" />
          <Tab label="통계" />
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
                    전체 통계
                  </Typography>
                  <Divider sx={{ my: 2 }} />
                  <List>
                    <ListItem>
                      <ListItemText primary="전체 알람" />
                      <ListItemSecondaryAction>
                        <Typography variant="h6">{statistics.totalCount}</Typography>
                      </ListItemSecondaryAction>
                    </ListItem>
                    <ListItem>
                      <ListItemText primary="읽지 않은 알람" />
                      <ListItemSecondaryAction>
                        <Typography variant="h6" color="error.main">
                          {statistics.unreadCount}
                        </Typography>
                      </ListItemSecondaryAction>
                    </ListItem>
                    <ListItem>
                      <ListItemText primary="읽은 알람" />
                      <ListItemSecondaryAction>
                        <Typography variant="h6">{statistics.readCount}</Typography>
                      </ListItemSecondaryAction>
                    </ListItem>
                    <ListItem>
                      <ListItemText primary="읽음율" />
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
                    타입별 통계
                  </Typography>
                  <Divider sx={{ my: 2 }} />
                  <List>
                    <ListItem>
                      <ListItemText primary="결재 알람" />
                      <ListItemSecondaryAction>
                        <Chip
                          label={statistics.approvalCount}
                          color="primary"
                          size="small"
                        />
                      </ListItemSecondaryAction>
                    </ListItem>
                    <ListItem>
                      <ListItemText primary="품질 알람" />
                      <ListItemSecondaryAction>
                        <Chip
                          label={statistics.qualityCount}
                          color="warning"
                          size="small"
                        />
                      </ListItemSecondaryAction>
                    </ListItem>
                    <ListItem>
                      <ListItemText primary="생산 알람" />
                      <ListItemSecondaryAction>
                        <Chip
                          label={statistics.productionCount}
                          color="success"
                          size="small"
                        />
                      </ListItemSecondaryAction>
                    </ListItem>
                    <ListItem>
                      <ListItemText primary="재고 알람" />
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
            알람 상세
          </Box>
        </DialogTitle>
        <DialogContent dividers>
          {selectedAlarm && (
            <Grid container spacing={2}>
              <Grid item xs={12}>
                <Typography variant="subtitle2" color="text.secondary">
                  알람 타입
                </Typography>
                <Chip
                  label={getAlarmTypeLabel(selectedAlarm.alarmType)}
                  color={getAlarmTypeColor(selectedAlarm.alarmType)}
                  sx={{ mt: 1 }}
                />
              </Grid>
              <Grid item xs={12}>
                <Typography variant="subtitle2" color="text.secondary">
                  우선순위
                </Typography>
                <Chip
                  label={getPriorityLabel(selectedAlarm.priority)}
                  color={getPriorityColor(selectedAlarm.priority)}
                  sx={{ mt: 1 }}
                />
              </Grid>
              <Grid item xs={12}>
                <Typography variant="subtitle2" color="text.secondary">
                  제목
                </Typography>
                <Typography variant="h6" sx={{ mt: 1 }}>
                  {selectedAlarm.title}
                </Typography>
              </Grid>
              <Grid item xs={12}>
                <Typography variant="subtitle2" color="text.secondary">
                  내용
                </Typography>
                <Typography variant="body1" sx={{ mt: 1 }}>
                  {selectedAlarm.message}
                </Typography>
              </Grid>
              {selectedAlarm.referenceNo && (
                <Grid item xs={12}>
                  <Typography variant="subtitle2" color="text.secondary">
                    참조 번호
                  </Typography>
                  <Typography variant="body1" sx={{ mt: 1 }}>
                    {selectedAlarm.referenceType}: {selectedAlarm.referenceNo}
                  </Typography>
                </Grid>
              )}
              <Grid item xs={12} sm={6}>
                <Typography variant="subtitle2" color="text.secondary">
                  발송 시간
                </Typography>
                <Typography variant="body2" sx={{ mt: 1 }}>
                  {formatDateTime(selectedAlarm.createdAt)}
                </Typography>
              </Grid>
              {selectedAlarm.readAt && (
                <Grid item xs={12} sm={6}>
                  <Typography variant="subtitle2" color="text.secondary">
                    읽은 시간
                  </Typography>
                  <Typography variant="body2" sx={{ mt: 1 }}>
                    {formatDateTime(selectedAlarm.readAt)}
                  </Typography>
                </Grid>
              )}
              <Grid item xs={12}>
                <Typography variant="subtitle2" color="text.secondary">
                  발송 채널
                </Typography>
                <Box sx={{ mt: 1, display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                  {selectedAlarm.sentViaEmail && <Chip label="이메일" size="small" />}
                  {selectedAlarm.sentViaSms && <Chip label="SMS" size="small" />}
                  {selectedAlarm.sentViaPush && <Chip label="푸시" size="small" />}
                  {selectedAlarm.sentViaSystem && <Chip label="시스템" size="small" />}
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
              읽음 표시
            </Button>
          )}
          <Button onClick={() => setDetailDialog(false)}>닫기</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default AlarmPage;
