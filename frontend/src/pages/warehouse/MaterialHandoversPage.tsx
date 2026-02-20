import React, { useState, useEffect } from 'react';
import {
  Box,
  Paper,
  Typography,
  Button,
  Chip,
  Alert,
  Stack,
  CircularProgress,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
} from '@mui/material';
import { DataGrid, GridColDef, GridRenderCellParams, GridActionsCellItem } from '@mui/x-data-grid';
import {
  Refresh as RefreshIcon,
  CheckCircle as ConfirmIcon,
  Cancel as RejectIcon,
  Warning as PendingIcon,
} from '@mui/icons-material';
import axios from 'axios';
import { format } from 'date-fns';
import { useAuthStore } from '@/stores/authStore';

/**
 * 자재 인수인계 리스트 페이지
 * 창고에서 생산으로 자재 인수인계 관리
 *
 * @author Moon Myung-seop
 */

interface MaterialHandover {
  materialHandoverId: number;
  handoverNo: string;
  handoverDate: string;
  handoverStatus: string; // PENDING, CONFIRMED, REJECTED
  materialRequestNo: string;
  transactionNo: string;
  productCode: string;
  productName: string;
  lotNo: string;
  lotQualityStatus: string;
  quantity: number;
  unit: string;
  issuerName: string;
  issueLocation: string;
  receiverName: string;
  receiveLocation: string;
  receivedDate?: string;
  confirmationRemarks?: string;
  remarks?: string;
  createdAt: string;
  updatedAt: string;
}

const MaterialHandoversPage: React.FC = () => {
  const { user } = useAuthStore();
  const [handovers, setHandovers] = useState<MaterialHandover[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedHandover, setSelectedHandover] = useState<MaterialHandover | null>(null);

  // Dialog states
  const [confirmDialogOpen, setConfirmDialogOpen] = useState(false);
  const [rejectDialogOpen, setRejectDialogOpen] = useState(false);
  const [confirmRemarks, setConfirmRemarks] = useState('');
  const [rejectReason, setRejectReason] = useState('');

  // 데이터 로드
  const loadHandovers = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await axios.get('/api/material-handovers');
      setHandovers(response.data.data || []);
    } catch (err: any) {
      setError(err.response?.data?.message || '인수인계 목록을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadHandovers();
  }, []);

  // 상태 렌더링
  const renderStatus = (params: GridRenderCellParams) => {
    const status = params.value as string;
    const statusConfig: Record<string, { color: 'default' | 'primary' | 'success' | 'error' | 'warning', label: string, icon?: React.ReactElement }> = {
      PENDING: { color: 'warning', label: '대기', icon: <PendingIcon fontSize="small" /> },
      CONFIRMED: { color: 'success', label: '확인', icon: <ConfirmIcon fontSize="small" /> },
      REJECTED: { color: 'error', label: '거부', icon: <RejectIcon fontSize="small" /> },
    };

    const config = statusConfig[status] || { color: 'default' as const, label: status };

    return (
      <Chip
        icon={config.icon}
        label={config.label}
        color={config.color}
        size="small"
        variant="outlined"
      />
    );
  };

  // LOT 품질 상태 렌더링
  const renderLotQuality = (params: GridRenderCellParams) => {
    const quality = params.value as string;
    const qualityConfig: Record<string, { color: 'default' | 'success' | 'error' | 'warning' | 'info', label: string }> = {
      PASSED: { color: 'success', label: '합격' },
      FAILED: { color: 'error', label: '불합격' },
      PENDING: { color: 'warning', label: '검사대기' },
      CONDITIONAL: { color: 'info', label: '조건부' },
    };

    const config = qualityConfig[quality] || { color: 'default' as const, label: quality };

    return (
      <Chip
        label={config.label}
        color={config.color}
        size="small"
      />
    );
  };

  // 인수 확인 다이얼로그 열기
  const handleConfirmDialogOpen = (handover: MaterialHandover) => {
    setSelectedHandover(handover);
    setConfirmRemarks('');
    setConfirmDialogOpen(true);
  };

  // 인수 확인 실행
  const handleConfirm = async () => {
    if (!selectedHandover) return;

    try {
      const receiverId = user?.userId ?? 0;
      await axios.post(`/api/material-handovers/${selectedHandover.materialHandoverId}/confirm`, null, {
        params: {
          receiverId,
          remarks: confirmRemarks
        }
      });
      alert('인수 확인되었습니다.');
      setConfirmDialogOpen(false);
      loadHandovers();
    } catch (err: any) {
      alert(err.response?.data?.message || '인수 확인에 실패했습니다.');
    }
  };

  // 인수 거부 다이얼로그 열기
  const handleRejectDialogOpen = (handover: MaterialHandover) => {
    setSelectedHandover(handover);
    setRejectReason('');
    setRejectDialogOpen(true);
  };

  // 인수 거부 실행
  const handleReject = async () => {
    if (!selectedHandover || !rejectReason.trim()) {
      alert('거부 사유를 입력해주세요.');
      return;
    }

    try {
      const receiverId = user?.userId ?? 0;
      await axios.post(`/api/material-handovers/${selectedHandover.materialHandoverId}/reject`, null, {
        params: {
          receiverId,
          reason: rejectReason
        }
      });
      alert('인수 거부되었습니다.');
      setRejectDialogOpen(false);
      loadHandovers();
    } catch (err: any) {
      alert(err.response?.data?.message || '인수 거부에 실패했습니다.');
    }
  };

  // DataGrid 컬럼 정의
  const columns: GridColDef[] = [
    {
      field: 'handoverNo',
      headerName: '인수인계번호',
      width: 150,
      sortable: true,
    },
    {
      field: 'handoverDate',
      headerName: '인계일시',
      width: 160,
      renderCell: (params) => {
        return format(new Date(params.value as string), 'yyyy-MM-dd HH:mm');
      },
    },
    {
      field: 'handoverStatus',
      headerName: '상태',
      width: 100,
      renderCell: renderStatus,
    },
    {
      field: 'materialRequestNo',
      headerName: '불출신청번호',
      width: 150,
    },
    {
      field: 'productCode',
      headerName: '제품코드',
      width: 120,
    },
    {
      field: 'productName',
      headerName: '제품명',
      width: 200,
    },
    {
      field: 'lotNo',
      headerName: 'LOT번호',
      width: 150,
    },
    {
      field: 'lotQualityStatus',
      headerName: 'LOT품질',
      width: 100,
      renderCell: renderLotQuality,
    },
    {
      field: 'quantity',
      headerName: '수량',
      width: 100,
      renderCell: (params) => {
        const row = params.row as MaterialHandover;
        return `${params.value} ${row.unit}`;
      },
    },
    {
      field: 'issuerName',
      headerName: '출고자',
      width: 100,
    },
    {
      field: 'issueLocation',
      headerName: '출고위치',
      width: 120,
    },
    {
      field: 'receiverName',
      headerName: '인수자',
      width: 100,
    },
    {
      field: 'receiveLocation',
      headerName: '인수위치',
      width: 120,
    },
    {
      field: 'receivedDate',
      headerName: '인수일시',
      width: 160,
      renderCell: (params) => {
        if (!params.value) return '-';
        return format(new Date(params.value as string), 'yyyy-MM-dd HH:mm');
      },
    },
    {
      field: 'actions',
      type: 'actions',
      headerName: '작업',
      width: 120,
      getActions: (params) => {
        const row = params.row as MaterialHandover;
        const actions = [];

        // PENDING → 인수 확인/거부
        if (row.handoverStatus === 'PENDING') {
          actions.push(
            <GridActionsCellItem
              icon={<ConfirmIcon />}
              label="인수 확인"
              onClick={() => handleConfirmDialogOpen(row)}
              color="success"
              showInMenu
            />,
            <GridActionsCellItem
              icon={<RejectIcon />}
              label="인수 거부"
              onClick={() => handleRejectDialogOpen(row)}
              color="error"
              showInMenu
            />
          );
        }

        return actions;
      },
    },
  ];

  // 통계 계산
  const stats = {
    total: handovers.length,
    pending: handovers.filter(h => h.handoverStatus === 'PENDING').length,
    confirmed: handovers.filter(h => h.handoverStatus === 'CONFIRMED').length,
    rejected: handovers.filter(h => h.handoverStatus === 'REJECTED').length,
  };

  return (
    <Box sx={{ p: 3 }}>
      <Paper sx={{ p: 2 }}>
        {/* 헤더 */}
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="h5" component="h1">
            자재 인수인계 관리
          </Typography>
          <Button
            variant="outlined"
            startIcon={<RefreshIcon />}
            onClick={loadHandovers}
            disabled={loading}
          >
            새로고침
          </Button>
        </Box>

        {/* 통계 */}
        <Stack direction="row" spacing={2} sx={{ mb: 2 }}>
          <Chip label={`전체: ${stats.total}`} />
          <Chip label={`대기: ${stats.pending}`} color="warning" variant="outlined" />
          <Chip label={`확인: ${stats.confirmed}`} color="success" variant="outlined" />
          <Chip label={`거부: ${stats.rejected}`} color="error" variant="outlined" />
        </Stack>

        {/* 에러 메시지 */}
        {error && (
          <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
            {error}
          </Alert>
        )}

        {/* 데이터 그리드 */}
        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
            <CircularProgress />
          </Box>
        ) : (
          <DataGrid
            rows={handovers}
            columns={columns}
            getRowId={(row) => row.materialHandoverId}
            initialState={{
              pagination: {
                paginationModel: { page: 0, pageSize: 25 },
              },
              sorting: {
                sortModel: [{ field: 'handoverDate', sort: 'desc' }],
              },
            }}
            pageSizeOptions={[10, 25, 50, 100]}
            checkboxSelection={false}
            disableRowSelectionOnClick
            autoHeight
            density="compact"
            sx={{
              '& .MuiDataGrid-cell': {
                borderBottom: '1px solid #f0f0f0',
              },
              '& .MuiDataGrid-row:hover': {
                backgroundColor: '#f5f5f5',
              },
            }}
          />
        )}
      </Paper>

      {/* 인수 확인 다이얼로그 */}
      <Dialog open={confirmDialogOpen} onClose={() => setConfirmDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>자재 인수 확인</DialogTitle>
        <DialogContent>
          {selectedHandover && (
            <Box sx={{ mb: 2, mt: 1 }}>
              <Typography variant="body2" color="text.secondary">
                <strong>인수인계번호:</strong> {selectedHandover.handoverNo}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                <strong>제품:</strong> {selectedHandover.productName} ({selectedHandover.productCode})
              </Typography>
              <Typography variant="body2" color="text.secondary">
                <strong>LOT:</strong> {selectedHandover.lotNo}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                <strong>수량:</strong> {selectedHandover.quantity} {selectedHandover.unit}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                <strong>출고자:</strong> {selectedHandover.issuerName} ({selectedHandover.issueLocation})
              </Typography>
            </Box>
          )}
          <TextField
            autoFocus
            margin="dense"
            label="확인 메모 (선택)"
            fullWidth
            multiline
            rows={3}
            value={confirmRemarks}
            onChange={(e) => setConfirmRemarks(e.target.value)}
            placeholder="인수 확인 시 메모를 남길 수 있습니다."
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmDialogOpen(false)}>취소</Button>
          <Button onClick={handleConfirm} variant="contained" color="success">
            인수 확인
          </Button>
        </DialogActions>
      </Dialog>

      {/* 인수 거부 다이얼로그 */}
      <Dialog open={rejectDialogOpen} onClose={() => setRejectDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>자재 인수 거부</DialogTitle>
        <DialogContent>
          {selectedHandover && (
            <Box sx={{ mb: 2, mt: 1 }}>
              <Typography variant="body2" color="text.secondary">
                <strong>인수인계번호:</strong> {selectedHandover.handoverNo}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                <strong>제품:</strong> {selectedHandover.productName} ({selectedHandover.productCode})
              </Typography>
              <Typography variant="body2" color="text.secondary">
                <strong>LOT:</strong> {selectedHandover.lotNo}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                <strong>수량:</strong> {selectedHandover.quantity} {selectedHandover.unit}
              </Typography>
            </Box>
          )}
          <TextField
            autoFocus
            margin="dense"
            label="거부 사유"
            fullWidth
            multiline
            rows={4}
            value={rejectReason}
            onChange={(e) => setRejectReason(e.target.value)}
            required
            error={!rejectReason.trim()}
            helperText={!rejectReason.trim() ? '거부 사유를 입력해주세요.' : ''}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setRejectDialogOpen(false)}>취소</Button>
          <Button onClick={handleReject} variant="contained" color="error" disabled={!rejectReason.trim()}>
            인수 거부
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default MaterialHandoversPage;
