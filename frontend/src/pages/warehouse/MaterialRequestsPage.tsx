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
import { useNavigate } from 'react-router-dom';
import {
  Add as AddIcon,
  Refresh as RefreshIcon,
  CheckCircle as ApproveIcon,
  Cancel as RejectIcon,
  LocalShipping as IssueIcon,
  Check as CompleteIcon,
  Close as CancelIcon,
  Visibility as ViewIcon,
} from '@mui/icons-material';
import axios from 'axios';
import { format } from 'date-fns';
import { useAuthStore } from '@/stores/authStore';

/**
 * 불출 신청 리스트 페이지
 * 자재 불출 신청 목록 관리
 *
 * @author Moon Myung-seop
 */

interface MaterialRequest {
  materialRequestId: number;
  requestNo: string;
  requestDate: string;
  requestStatus: string; // PENDING, APPROVED, REJECTED, ISSUED, COMPLETED, CANCELLED
  priority: string; // URGENT, HIGH, NORMAL, LOW
  purpose: string;
  workOrderNo?: string;
  requesterName: string;
  warehouseCode: string;
  warehouseName: string;
  approverName?: string;
  approvedDate?: string;
  requiredDate: string;
  issuedDate?: string;
  completedDate?: string;
  totalRequestedQuantity: number;
  totalApprovedQuantity: number;
  totalIssuedQuantity: number;
  remarks?: string;
  rejectionReason?: string;
  cancellationReason?: string;
  createdAt: string;
  updatedAt: string;
}

const MaterialRequestsPage: React.FC = () => {
  const { user } = useAuthStore();
  const navigate = useNavigate();
  const [requests, setRequests] = useState<MaterialRequest[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedRequest, setSelectedRequest] = useState<MaterialRequest | null>(null);

  // Dialog states
  const [rejectDialogOpen, setRejectDialogOpen] = useState(false);
  const [cancelDialogOpen, setCancelDialogOpen] = useState(false);
  const [rejectionReason, setRejectionReason] = useState('');
  const [cancellationReason, setCancellationReason] = useState('');

  // 데이터 로드
  const loadRequests = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await axios.get('/api/material-requests');
      setRequests(response.data.data || []);
    } catch (err: any) {
      setError(err.response?.data?.message || '불출 신청 목록을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadRequests();
  }, []);

  // 상태 렌더링
  const renderStatus = (params: GridRenderCellParams) => {
    const status = params.value as string;
    const statusConfig: Record<string, { color: 'default' | 'primary' | 'success' | 'error' | 'warning' | 'info', label: string }> = {
      PENDING: { color: 'warning', label: '대기' },
      APPROVED: { color: 'info', label: '승인' },
      REJECTED: { color: 'error', label: '거부' },
      ISSUED: { color: 'primary', label: '불출' },
      COMPLETED: { color: 'success', label: '완료' },
      CANCELLED: { color: 'default', label: '취소' },
    };

    const config = statusConfig[status] || { color: 'default' as const, label: status };

    return (
      <Chip
        label={config.label}
        color={config.color}
        size="small"
        variant="outlined"
      />
    );
  };

  // 우선순위 렌더링
  const renderPriority = (params: GridRenderCellParams) => {
    const priority = params.value as string;
    const priorityConfig: Record<string, { color: 'default' | 'error' | 'warning' | 'info', label: string }> = {
      URGENT: { color: 'error', label: '긴급' },
      HIGH: { color: 'warning', label: '높음' },
      NORMAL: { color: 'info', label: '보통' },
      LOW: { color: 'default', label: '낮음' },
    };

    const config = priorityConfig[priority] || { color: 'default' as const, label: priority };

    return (
      <Chip
        label={config.label}
        color={config.color}
        size="small"
      />
    );
  };

  // 수량 렌더링
  const renderQuantities = (params: GridRenderCellParams) => {
    const row = params.row as MaterialRequest;
    return (
      <Stack direction="column" spacing={0.5}>
        <Typography variant="caption">요청: {row.totalRequestedQuantity}</Typography>
        {row.totalApprovedQuantity > 0 && (
          <Typography variant="caption" color="info.main">
            승인: {row.totalApprovedQuantity}
          </Typography>
        )}
        {row.totalIssuedQuantity > 0 && (
          <Typography variant="caption" color="success.main">
            불출: {row.totalIssuedQuantity}
          </Typography>
        )}
      </Stack>
    );
  };

  // 승인
  const handleApprove = async (request: MaterialRequest) => {
    if (!window.confirm(`불출 신청 ${request.requestNo}을(를) 승인하시겠습니까?`)) {
      return;
    }

    try {
      const approverUserId = user?.userId ?? 0;
      await axios.post(`/api/material-requests/${request.materialRequestId}/approve`, null, {
        params: { approverUserId }
      });
      alert('승인되었습니다.');
      loadRequests();
    } catch (err: any) {
      alert(err.response?.data?.message || '승인에 실패했습니다.');
    }
  };

  // 거부 다이얼로그 열기
  const handleRejectDialogOpen = (request: MaterialRequest) => {
    setSelectedRequest(request);
    setRejectionReason('');
    setRejectDialogOpen(true);
  };

  // 거부 실행
  const handleReject = async () => {
    if (!selectedRequest || !rejectionReason.trim()) {
      alert('거부 사유를 입력해주세요.');
      return;
    }

    try {
      const approverUserId = user?.userId ?? 0;
      await axios.post(`/api/material-requests/${selectedRequest.materialRequestId}/reject`, null, {
        params: {
          approverUserId,
          reason: rejectionReason
        }
      });
      alert('거부되었습니다.');
      setRejectDialogOpen(false);
      loadRequests();
    } catch (err: any) {
      alert(err.response?.data?.message || '거부에 실패했습니다.');
    }
  };

  // 불출 지시
  const handleIssue = async (request: MaterialRequest) => {
    if (!window.confirm(`불출 신청 ${request.requestNo}을(를) 불출 지시하시겠습니까?\n\n재고가 차감되고 인수인계 레코드가 생성됩니다.`)) {
      return;
    }

    try {
      const issuerUserId = user?.userId ?? 0;
      await axios.post(`/api/material-requests/${request.materialRequestId}/issue`, null, {
        params: { issuerUserId }
      });
      alert('불출 지시가 완료되었습니다.');
      loadRequests();
    } catch (err: any) {
      alert(err.response?.data?.message || '불출 지시에 실패했습니다.');
    }
  };

  // 완료
  const handleComplete = async (request: MaterialRequest) => {
    if (!window.confirm(`불출 신청 ${request.requestNo}을(를) 완료 처리하시겠습니까?`)) {
      return;
    }

    try {
      await axios.post(`/api/material-requests/${request.materialRequestId}/complete`);
      alert('완료되었습니다.');
      loadRequests();
    } catch (err: any) {
      alert(err.response?.data?.message || '완료 처리에 실패했습니다.');
    }
  };

  // 취소 다이얼로그 열기
  const handleCancelDialogOpen = (request: MaterialRequest) => {
    setSelectedRequest(request);
    setCancellationReason('');
    setCancelDialogOpen(true);
  };

  // 취소 실행
  const handleCancel = async () => {
    if (!selectedRequest) return;

    try {
      await axios.post(`/api/material-requests/${selectedRequest.materialRequestId}/cancel`, null, {
        params: { reason: cancellationReason }
      });
      alert('취소되었습니다.');
      setCancelDialogOpen(false);
      loadRequests();
    } catch (err: any) {
      alert(err.response?.data?.message || '취소에 실패했습니다.');
    }
  };

  // 상세 보기
  const handleView = (request: MaterialRequest) => {
    navigate(`/warehouse/material-requests/${request.materialRequestId}`);
  };

  // DataGrid 컬럼 정의
  const columns: GridColDef[] = [
    {
      field: 'requestNo',
      headerName: '신청번호',
      width: 150,
      sortable: true,
    },
    {
      field: 'requestDate',
      headerName: '신청일시',
      width: 160,
      renderCell: (params) => {
        return format(new Date(params.value as string), 'yyyy-MM-dd HH:mm');
      },
    },
    {
      field: 'requestStatus',
      headerName: '상태',
      width: 100,
      renderCell: renderStatus,
    },
    {
      field: 'priority',
      headerName: '우선순위',
      width: 100,
      renderCell: renderPriority,
    },
    {
      field: 'purpose',
      headerName: '용도',
      width: 120,
      renderCell: (params) => {
        const purposeMap: Record<string, string> = {
          PRODUCTION: '생산',
          MAINTENANCE: '보수',
          SAMPLE: '샘플',
          OTHER: '기타',
        };
        return purposeMap[params.value as string] || params.value;
      },
    },
    {
      field: 'workOrderNo',
      headerName: '작업지시',
      width: 120,
    },
    {
      field: 'requesterName',
      headerName: '신청자',
      width: 100,
    },
    {
      field: 'warehouseName',
      headerName: '창고',
      width: 120,
    },
    {
      field: 'requiredDate',
      headerName: '필요일자',
      width: 120,
      renderCell: (params) => {
        return format(new Date(params.value as string), 'yyyy-MM-dd');
      },
    },
    {
      field: 'quantities',
      headerName: '수량',
      width: 120,
      renderCell: renderQuantities,
      sortable: false,
    },
    {
      field: 'approverName',
      headerName: '승인자',
      width: 100,
    },
    {
      field: 'actions',
      type: 'actions',
      headerName: '작업',
      width: 200,
      getActions: (params) => {
        const row = params.row as MaterialRequest;
        const actions = [
          <GridActionsCellItem
            icon={<ViewIcon />}
            label="상세"
            onClick={() => handleView(row)}
            showInMenu
          />,
        ];

        // PENDING → 승인/거부
        if (row.requestStatus === 'PENDING') {
          actions.push(
            <GridActionsCellItem
              icon={<ApproveIcon />}
              label="승인"
              onClick={() => handleApprove(row)}
              showInMenu
            />,
            <GridActionsCellItem
              icon={<RejectIcon />}
              label="거부"
              onClick={() => handleRejectDialogOpen(row)}
              showInMenu
            />,
            <GridActionsCellItem
              icon={<CancelIcon />}
              label="취소"
              onClick={() => handleCancelDialogOpen(row)}
              showInMenu
            />
          );
        }

        // APPROVED → 불출 지시/취소
        if (row.requestStatus === 'APPROVED') {
          actions.push(
            <GridActionsCellItem
              icon={<IssueIcon />}
              label="불출 지시"
              onClick={() => handleIssue(row)}
              showInMenu
            />,
            <GridActionsCellItem
              icon={<CancelIcon />}
              label="취소"
              onClick={() => handleCancelDialogOpen(row)}
              showInMenu
            />
          );
        }

        // ISSUED → 완료
        if (row.requestStatus === 'ISSUED') {
          actions.push(
            <GridActionsCellItem
              icon={<CompleteIcon />}
              label="완료"
              onClick={() => handleComplete(row)}
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
    total: requests.length,
    pending: requests.filter(r => r.requestStatus === 'PENDING').length,
    approved: requests.filter(r => r.requestStatus === 'APPROVED').length,
    issued: requests.filter(r => r.requestStatus === 'ISSUED').length,
    completed: requests.filter(r => r.requestStatus === 'COMPLETED').length,
    urgent: requests.filter(r => r.priority === 'URGENT' && ['PENDING', 'APPROVED'].includes(r.requestStatus)).length,
  };

  return (
    <Box sx={{ p: 3 }}>
      <Paper sx={{ p: 2 }}>
        {/* 헤더 */}
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="h5" component="h1">
            불출 신청 관리
          </Typography>
          <Stack direction="row" spacing={1}>
            <Button
              variant="outlined"
              startIcon={<RefreshIcon />}
              onClick={loadRequests}
              disabled={loading}
            >
              새로고침
            </Button>
            <Button
              variant="contained"
              startIcon={<AddIcon />}
              onClick={() => navigate('/warehouse/material-requests/new')}
            >
              신규 신청
            </Button>
          </Stack>
        </Box>

        {/* 통계 */}
        <Stack direction="row" spacing={2} sx={{ mb: 2 }}>
          <Chip label={`전체: ${stats.total}`} />
          <Chip label={`대기: ${stats.pending}`} color="warning" variant="outlined" />
          <Chip label={`승인: ${stats.approved}`} color="info" variant="outlined" />
          <Chip label={`불출: ${stats.issued}`} color="primary" variant="outlined" />
          <Chip label={`완료: ${stats.completed}`} color="success" variant="outlined" />
          {stats.urgent > 0 && (
            <Chip label={`긴급: ${stats.urgent}`} color="error" />
          )}
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
            rows={requests}
            columns={columns}
            getRowId={(row) => row.materialRequestId}
            initialState={{
              pagination: {
                paginationModel: { page: 0, pageSize: 25 },
              },
              sorting: {
                sortModel: [{ field: 'requestDate', sort: 'desc' }],
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
            }}
          />
        )}
      </Paper>

      {/* 거부 다이얼로그 */}
      <Dialog open={rejectDialogOpen} onClose={() => setRejectDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>불출 신청 거부</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin="dense"
            label="거부 사유"
            fullWidth
            multiline
            rows={4}
            value={rejectionReason}
            onChange={(e) => setRejectionReason(e.target.value)}
            required
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setRejectDialogOpen(false)}>취소</Button>
          <Button onClick={handleReject} variant="contained" color="error">
            거부
          </Button>
        </DialogActions>
      </Dialog>

      {/* 취소 다이얼로그 */}
      <Dialog open={cancelDialogOpen} onClose={() => setCancelDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>불출 신청 취소</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin="dense"
            label="취소 사유 (선택)"
            fullWidth
            multiline
            rows={4}
            value={cancellationReason}
            onChange={(e) => setCancellationReason(e.target.value)}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setCancelDialogOpen(false)}>닫기</Button>
          <Button onClick={handleCancel} variant="contained" color="warning">
            취소 확정
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default MaterialRequestsPage;
