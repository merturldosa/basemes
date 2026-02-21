/**
 * Audit Logs Page
 * 감사 로그 조회 페이지
 * @author Moon Myung-seop
 */

import { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Button,
  TextField,
  Paper,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Chip,
  Stack,
  InputAdornment,
  MenuItem,
  Grid,
} from '@mui/material';
import {
  Search as SearchIcon,
  Refresh as RefreshIcon,
  CheckCircle as SuccessIcon,
  Cancel as FailIcon,
  Info as InfoIcon,
} from '@mui/icons-material';
import { DataGrid, GridColDef, GridPaginationModel } from '@mui/x-data-grid';
import { AuditLog } from '@/types';
import auditLogService from '@/services/auditLogService';
import { useTranslation } from 'react-i18next';
import { format } from 'date-fns';

export default function AuditLogsPage() {
  const { t } = useTranslation();

  // State
  const [auditLogs, setAuditLogs] = useState<AuditLog[]>([]);
  const [loading, setLoading] = useState(false);
  const [totalElements, setTotalElements] = useState(0);
  const [paginationModel, setPaginationModel] = useState<GridPaginationModel>({
    page: 0,
    pageSize: 10,
  });

  // Filter State
  const [filters, setFilters] = useState({
    username: '',
    action: '',
    entityType: '',
    startDate: '',
    endDate: '',
    success: 'ALL',
  });

  // Detail Dialog State
  const [detailDialogOpen, setDetailDialogOpen] = useState(false);
  const [selectedLog, setSelectedLog] = useState<AuditLog | null>(null);

  // Load Audit Logs
  const loadAuditLogs = async () => {
    try {
      setLoading(true);
      const response = await auditLogService.getAuditLogs({
        page: paginationModel.page,
        size: paginationModel.pageSize,
        username: filters.username || undefined,
        action: filters.action || undefined,
        entityType: filters.entityType || undefined,
        startDate: filters.startDate || undefined,
        endDate: filters.endDate || undefined,
        success: filters.success === 'ALL' ? undefined : filters.success === 'SUCCESS',
      });

      setAuditLogs(response?.content || []);
      setTotalElements(response?.totalElements || 0);
    } catch (_error) {
      setAuditLogs([]);
      setTotalElements(0);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAuditLogs();
    // eslint-disable-next-line react-hooks/exhaustive-deps -- reload when pagination changes
  }, [paginationModel]);

  // Handle Search
  const handleSearch = () => {
    // Reset to first page when searching
    setPaginationModel({ ...paginationModel, page: 0 });
    loadAuditLogs();
  };

  // Handle Reset Filters
  const handleResetFilters = () => {
    setFilters({
      username: '',
      action: '',
      entityType: '',
      startDate: '',
      endDate: '',
      success: 'ALL',
    });
    setPaginationModel({ ...paginationModel, page: 0 });
  };

  // Open Detail Dialog
  const handleOpenDetailDialog = (log: AuditLog) => {
    setSelectedLog(log);
    setDetailDialogOpen(true);
  };

  // DataGrid Columns
  const columns: GridColDef[] = [
    {
      field: 'timestamp',
      headerName: t('pages.auditLogs.fields.timestamp'),
      width: 180,
      renderCell: (params) => {
        try {
          if (!params.value) return '-';
          const date = new Date(params.value);
          if (isNaN(date.getTime())) return '-';
          return format(date, 'yyyy-MM-dd HH:mm:ss');
        } catch {
          return '-';
        }
      },
    },
    {
      field: 'username',
      headerName: t('pages.auditLogs.fields.username'),
      width: 150,
      flex: 1,
    },
    {
      field: 'action',
      headerName: t('pages.auditLogs.fields.action'),
      width: 120,
      renderCell: (params) => (
        <Chip
          label={params.value}
          color={
            params.value === 'CREATE'
              ? 'success'
              : params.value === 'UPDATE'
              ? 'primary'
              : params.value === 'DELETE'
              ? 'error'
              : 'default'
          }
          size="small"
        />
      ),
    },
    {
      field: 'entityType',
      headerName: t('pages.auditLogs.fields.entityType'),
      width: 150,
      flex: 1,
    },
    {
      field: 'entityId',
      headerName: t('pages.auditLogs.fields.entityId'),
      width: 100,
    },
    {
      field: 'success',
      headerName: t('pages.auditLogs.fields.success'),
      width: 100,
      renderCell: (params) => (
        <Chip
          icon={params.value ? <SuccessIcon /> : <FailIcon />}
          label={params.value ? t('pages.auditLogs.filters.successLabel') : t('pages.auditLogs.filters.failLabel')}
          color={params.value ? 'success' : 'error'}
          size="small"
        />
      ),
    },
    {
      field: 'ipAddress',
      headerName: t('pages.auditLogs.fields.ipAddress'),
      width: 140,
    },
    {
      field: 'actions',
      headerName: t('pages.auditLogs.fields.detail'),
      width: 80,
      sortable: false,
      renderCell: (params) => (
        <Button
          size="small"
          startIcon={<InfoIcon />}
          onClick={() => handleOpenDetailDialog(params.row)}
        >
          {t('pages.auditLogs.actions.view')}
        </Button>
      ),
    },
  ];

  return (
    <Box>
      {/* Header */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" gutterBottom fontWeight="bold">
          {t('pages.auditLogs.title')}
        </Typography>
        <Typography variant="body1" color="text.secondary">
          {t('pages.auditLogs.subtitle')}
        </Typography>
      </Box>

      {/* Filters */}
      <Paper sx={{ p: 2, mb: 3 }}>
        <Grid container spacing={2}>
          <Grid item xs={12} md={3}>
            <TextField
              label={t('pages.auditLogs.filters.username')}
              value={filters.username}
              onChange={(e) => setFilters({ ...filters, username: e.target.value })}
              size="small"
              fullWidth
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon />
                  </InputAdornment>
                ),
              }}
            />
          </Grid>
          <Grid item xs={12} md={2}>
            <TextField
              select
              label={t('pages.auditLogs.filters.action')}
              value={filters.action}
              onChange={(e) => setFilters({ ...filters, action: e.target.value })}
              size="small"
              fullWidth
            >
              <MenuItem value="">{t('pages.auditLogs.filters.all')}</MenuItem>
              <MenuItem value="CREATE">{t('pages.auditLogs.actions.create')}</MenuItem>
              <MenuItem value="UPDATE">{t('pages.auditLogs.actions.update')}</MenuItem>
              <MenuItem value="DELETE">{t('pages.auditLogs.actions.delete')}</MenuItem>
              <MenuItem value="LOGIN">{t('pages.auditLogs.actions.login')}</MenuItem>
              <MenuItem value="LOGOUT">{t('pages.auditLogs.actions.logout')}</MenuItem>
            </TextField>
          </Grid>
          <Grid item xs={12} md={2}>
            <TextField
              label={t('pages.auditLogs.filters.entityType')}
              value={filters.entityType}
              onChange={(e) => setFilters({ ...filters, entityType: e.target.value })}
              size="small"
              fullWidth
            />
          </Grid>
          <Grid item xs={12} md={2}>
            <TextField
              label={t('common.labels.startDate')}
              type="date"
              value={filters.startDate}
              onChange={(e) => setFilters({ ...filters, startDate: e.target.value })}
              size="small"
              fullWidth
              InputLabelProps={{
                shrink: true,
              }}
            />
          </Grid>
          <Grid item xs={12} md={2}>
            <TextField
              label={t('common.labels.endDate')}
              type="date"
              value={filters.endDate}
              onChange={(e) => setFilters({ ...filters, endDate: e.target.value })}
              size="small"
              fullWidth
              InputLabelProps={{
                shrink: true,
              }}
            />
          </Grid>
          <Grid item xs={12} md={1}>
            <TextField
              select
              label={t('pages.auditLogs.filters.success')}
              value={filters.success}
              onChange={(e) => setFilters({ ...filters, success: e.target.value })}
              size="small"
              fullWidth
            >
              <MenuItem value="ALL">{t('pages.auditLogs.filters.all')}</MenuItem>
              <MenuItem value="SUCCESS">{t('pages.auditLogs.filters.successLabel')}</MenuItem>
              <MenuItem value="FAIL">{t('pages.auditLogs.filters.failLabel')}</MenuItem>
            </TextField>
          </Grid>
        </Grid>

        <Stack direction="row" spacing={2} sx={{ mt: 2 }} justifyContent="flex-end">
          <Button variant="outlined" onClick={handleResetFilters}>
            {t('common.buttons.reset')}
          </Button>
          <Button
            variant="outlined"
            startIcon={<RefreshIcon />}
            onClick={loadAuditLogs}
          >
            {t('common.buttons.refresh')}
          </Button>
          <Button variant="contained" startIcon={<SearchIcon />} onClick={handleSearch}>
            {t('common.buttons.search')}
          </Button>
        </Stack>
      </Paper>

      {/* Data Grid */}
      <Paper sx={{ height: 600 }}>
        <DataGrid
          rows={auditLogs}
          columns={columns}
          getRowId={(row) => row.auditId}
          loading={loading}
          paginationModel={paginationModel}
          onPaginationModelChange={setPaginationModel}
          pageSizeOptions={[10, 25, 50, 100]}
          rowCount={totalElements}
          paginationMode="server"
          disableRowSelectionOnClick
          sx={{
            border: 0,
            '& .MuiDataGrid-cell:focus': {
              outline: 'none',
            },
          }}
        />
      </Paper>

      {/* Detail Dialog */}
      <Dialog
        open={detailDialogOpen}
        onClose={() => setDetailDialogOpen(false)}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle>{t('pages.auditLogs.detail.title')}</DialogTitle>
        <DialogContent>
          {selectedLog && (
            <Stack spacing={2} sx={{ mt: 2 }}>
              <Grid container spacing={2}>
                <Grid item xs={6}>
                  <Typography variant="subtitle2" color="text.secondary">
                    {t('pages.auditLogs.fields.actionTime')}
                  </Typography>
                  <Typography variant="body1">
                    {(() => {
                      try {
                        if (!selectedLog.timestamp) return '-';
                        const date = new Date(selectedLog.timestamp);
                        if (isNaN(date.getTime())) return '-';
                        return format(date, 'yyyy-MM-dd HH:mm:ss');
                      } catch {
                        return '-';
                      }
                    })()}
                  </Typography>
                </Grid>
                <Grid item xs={6}>
                  <Typography variant="subtitle2" color="text.secondary">
                    {t('pages.auditLogs.fields.username')}
                  </Typography>
                  <Typography variant="body1">{selectedLog.username}</Typography>
                </Grid>

                <Grid item xs={6}>
                  <Typography variant="subtitle2" color="text.secondary">
                    {t('pages.auditLogs.fields.action')}
                  </Typography>
                  <Chip
                    label={selectedLog.action}
                    color={
                      selectedLog.action === 'CREATE'
                        ? 'success'
                        : selectedLog.action === 'UPDATE'
                        ? 'primary'
                        : selectedLog.action === 'DELETE'
                        ? 'error'
                        : 'default'
                    }
                    size="small"
                  />
                </Grid>
                <Grid item xs={6}>
                  <Typography variant="subtitle2" color="text.secondary">
                    {t('pages.auditLogs.fields.success')}
                  </Typography>
                  <Chip
                    icon={selectedLog.success ? <SuccessIcon /> : <FailIcon />}
                    label={selectedLog.success ? t('pages.auditLogs.filters.successLabel') : t('pages.auditLogs.filters.failLabel')}
                    color={selectedLog.success ? 'success' : 'error'}
                    size="small"
                  />
                </Grid>

                <Grid item xs={6}>
                  <Typography variant="subtitle2" color="text.secondary">
                    {t('pages.auditLogs.fields.entityType')}
                  </Typography>
                  <Typography variant="body1">{selectedLog.entityType}</Typography>
                </Grid>
                <Grid item xs={6}>
                  <Typography variant="subtitle2" color="text.secondary">
                    {t('pages.auditLogs.fields.entityId')}
                  </Typography>
                  <Typography variant="body1">{selectedLog.entityId}</Typography>
                </Grid>

                <Grid item xs={6}>
                  <Typography variant="subtitle2" color="text.secondary">
                    {t('pages.auditLogs.fields.ipAddress')}
                  </Typography>
                  <Typography variant="body1">{selectedLog.ipAddress || '-'}</Typography>
                </Grid>
                <Grid item xs={6}>
                  <Typography variant="subtitle2" color="text.secondary">
                    User Agent
                  </Typography>
                  <Typography variant="body2" sx={{ wordBreak: 'break-all' }}>
                    {selectedLog.userAgent || '-'}
                  </Typography>
                </Grid>

                {selectedLog.errorMessage && (
                  <Grid item xs={12}>
                    <Typography variant="subtitle2" color="error">
                      {t('pages.auditLogs.fields.errorMessage')}
                    </Typography>
                    <Paper sx={{ p: 2, bgcolor: 'error.lighter' }}>
                      <Typography variant="body2" color="error">
                        {selectedLog.errorMessage}
                      </Typography>
                    </Paper>
                  </Grid>
                )}

                {selectedLog.oldValue && (
                  <Grid item xs={12}>
                    <Typography variant="subtitle2" color="text.secondary">
                      {t('pages.auditLogs.fields.oldValue')}
                    </Typography>
                    <Paper sx={{ p: 2, bgcolor: 'grey.100' }}>
                      <pre style={{ margin: 0, fontSize: '0.875rem', overflow: 'auto' }}>
                        {JSON.stringify(JSON.parse(selectedLog.oldValue), null, 2)}
                      </pre>
                    </Paper>
                  </Grid>
                )}

                {selectedLog.newValue && (
                  <Grid item xs={12}>
                    <Typography variant="subtitle2" color="text.secondary">
                      {t('pages.auditLogs.fields.newValue')}
                    </Typography>
                    <Paper sx={{ p: 2, bgcolor: 'grey.100' }}>
                      <pre style={{ margin: 0, fontSize: '0.875rem', overflow: 'auto' }}>
                        {JSON.stringify(JSON.parse(selectedLog.newValue), null, 2)}
                      </pre>
                    </Paper>
                  </Grid>
                )}

                {selectedLog.metadata && (
                  <Grid item xs={12}>
                    <Typography variant="subtitle2" color="text.secondary">
                      {t('pages.auditLogs.fields.metadata')}
                    </Typography>
                    <Paper sx={{ p: 2, bgcolor: 'grey.100' }}>
                      <pre style={{ margin: 0, fontSize: '0.875rem', overflow: 'auto' }}>
                        {JSON.stringify(JSON.parse(selectedLog.metadata), null, 2)}
                      </pre>
                    </Paper>
                  </Grid>
                )}
              </Grid>
            </Stack>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDetailDialogOpen(false)}>{t('common.buttons.close')}</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
