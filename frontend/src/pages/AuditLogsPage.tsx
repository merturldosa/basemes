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
import { format } from 'date-fns';

export default function AuditLogsPage() {
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

      setAuditLogs(response.content);
      setTotalElements(response.totalElements);
    } catch (error: any) {
      console.error('Failed to load audit logs:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAuditLogs();
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
      headerName: '시간',
      width: 180,
      renderCell: (params) => format(new Date(params.value), 'yyyy-MM-dd HH:mm:ss'),
    },
    {
      field: 'username',
      headerName: '사용자',
      width: 150,
      flex: 1,
    },
    {
      field: 'action',
      headerName: '작업',
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
      headerName: '대상 유형',
      width: 150,
      flex: 1,
    },
    {
      field: 'entityId',
      headerName: '대상 ID',
      width: 100,
    },
    {
      field: 'success',
      headerName: '성공 여부',
      width: 100,
      renderCell: (params) => (
        <Chip
          icon={params.value ? <SuccessIcon /> : <FailIcon />}
          label={params.value ? '성공' : '실패'}
          color={params.value ? 'success' : 'error'}
          size="small"
        />
      ),
    },
    {
      field: 'ipAddress',
      headerName: 'IP 주소',
      width: 140,
    },
    {
      field: 'actions',
      headerName: '상세',
      width: 80,
      sortable: false,
      renderCell: (params) => (
        <Button
          size="small"
          startIcon={<InfoIcon />}
          onClick={() => handleOpenDetailDialog(params.row)}
        >
          보기
        </Button>
      ),
    },
  ];

  return (
    <Box>
      {/* Header */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" gutterBottom fontWeight="bold">
          감사 로그
        </Typography>
        <Typography variant="body1" color="text.secondary">
          시스템 작업 이력을 조회합니다
        </Typography>
      </Box>

      {/* Filters */}
      <Paper sx={{ p: 2, mb: 3 }}>
        <Grid container spacing={2}>
          <Grid item xs={12} md={3}>
            <TextField
              label="사용자명"
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
              label="작업"
              value={filters.action}
              onChange={(e) => setFilters({ ...filters, action: e.target.value })}
              size="small"
              fullWidth
            >
              <MenuItem value="">전체</MenuItem>
              <MenuItem value="CREATE">생성</MenuItem>
              <MenuItem value="UPDATE">수정</MenuItem>
              <MenuItem value="DELETE">삭제</MenuItem>
              <MenuItem value="LOGIN">로그인</MenuItem>
              <MenuItem value="LOGOUT">로그아웃</MenuItem>
            </TextField>
          </Grid>
          <Grid item xs={12} md={2}>
            <TextField
              label="대상 유형"
              value={filters.entityType}
              onChange={(e) => setFilters({ ...filters, entityType: e.target.value })}
              size="small"
              fullWidth
            />
          </Grid>
          <Grid item xs={12} md={2}>
            <TextField
              label="시작일"
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
              label="종료일"
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
              label="성공"
              value={filters.success}
              onChange={(e) => setFilters({ ...filters, success: e.target.value })}
              size="small"
              fullWidth
            >
              <MenuItem value="ALL">전체</MenuItem>
              <MenuItem value="SUCCESS">성공</MenuItem>
              <MenuItem value="FAIL">실패</MenuItem>
            </TextField>
          </Grid>
        </Grid>

        <Stack direction="row" spacing={2} sx={{ mt: 2 }} justifyContent="flex-end">
          <Button variant="outlined" onClick={handleResetFilters}>
            초기화
          </Button>
          <Button
            variant="outlined"
            startIcon={<RefreshIcon />}
            onClick={loadAuditLogs}
          >
            새로고침
          </Button>
          <Button variant="contained" startIcon={<SearchIcon />} onClick={handleSearch}>
            검색
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
        <DialogTitle>감사 로그 상세</DialogTitle>
        <DialogContent>
          {selectedLog && (
            <Stack spacing={2} sx={{ mt: 2 }}>
              <Grid container spacing={2}>
                <Grid item xs={6}>
                  <Typography variant="subtitle2" color="text.secondary">
                    작업 시간
                  </Typography>
                  <Typography variant="body1">
                    {format(new Date(selectedLog.timestamp), 'yyyy-MM-dd HH:mm:ss')}
                  </Typography>
                </Grid>
                <Grid item xs={6}>
                  <Typography variant="subtitle2" color="text.secondary">
                    사용자
                  </Typography>
                  <Typography variant="body1">{selectedLog.username}</Typography>
                </Grid>

                <Grid item xs={6}>
                  <Typography variant="subtitle2" color="text.secondary">
                    작업
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
                    성공 여부
                  </Typography>
                  <Chip
                    icon={selectedLog.success ? <SuccessIcon /> : <FailIcon />}
                    label={selectedLog.success ? '성공' : '실패'}
                    color={selectedLog.success ? 'success' : 'error'}
                    size="small"
                  />
                </Grid>

                <Grid item xs={6}>
                  <Typography variant="subtitle2" color="text.secondary">
                    대상 유형
                  </Typography>
                  <Typography variant="body1">{selectedLog.entityType}</Typography>
                </Grid>
                <Grid item xs={6}>
                  <Typography variant="subtitle2" color="text.secondary">
                    대상 ID
                  </Typography>
                  <Typography variant="body1">{selectedLog.entityId}</Typography>
                </Grid>

                <Grid item xs={6}>
                  <Typography variant="subtitle2" color="text.secondary">
                    IP 주소
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
                      오류 메시지
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
                      이전 값
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
                      새로운 값
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
                      메타데이터
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
          <Button onClick={() => setDetailDialogOpen(false)}>닫기</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
