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
  Inventory as ReceiveIcon,
  Check as CompleteIcon,
  Close as CancelIcon,
  Visibility as ViewIcon,
} from '@mui/icons-material';
import axios from 'axios';
import { format } from 'date-fns';

/**
 * 반품 관리 페이지
 * 생산/창고에서 반품 처리
 *
 * @author Moon Myung-seop
 */

interface Return {
  returnId: number;
  returnNo: string;
  returnDate: string;
  returnType: string; // DEFECTIVE, EXCESS, WRONG_DELIVERY, OTHER
  returnStatus: string; // PENDING, APPROVED, REJECTED, RECEIVED, INSPECTING, COMPLETED, CANCELLED
  materialRequestNo?: string;
  workOrderNo?: string;
  requesterName: string;
  warehouseCode: string;
  warehouseName: string;
  approverName?: string;
  approvedDate?: string;
  receivedDate?: string;
  completedDate?: string;
  totalReturnQuantity: number;
  totalReceivedQuantity: number;
  totalPassedQuantity: number;
  totalFailedQuantity: number;
  remarks?: string;
  rejectionReason?: string;
  cancellationReason?: string;
  createdAt: string;
  updatedAt: string;
}

const ReturnsPage: React.FC = () => {
  const navigate = useNavigate();
  const [returns, setReturns] = useState<Return[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedReturn, setSelectedReturn] = useState<Return | null>(null);

  // Dialog states
  const [rejectDialogOpen, setRejectDialogOpen] = useState(false);
  const [cancelDialogOpen, setCancelDialogOpen] = useState(false);
  const [rejectionReason, setRejectionReason] = useState('');
  const [cancellationReason, setCancellationReason] = useState('');

  // 데이터 로드
  const loadReturns = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await axios.get('/api/returns');
      setReturns(response.data.data || []);
    } catch (err: any) {
      console.error('Failed to load returns:', err);
      setError(err.response?.data?.message || '반품 목록을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadReturns();
  }, []);

  // 상태 렌더링
  const renderStatus = (params: GridRenderCellParams) => {
    const status = params.value as string;
    const statusConfig: Record<string, { color: 'default' | 'primary' | 'success' | 'error' | 'warning' | 'info', label: string }> = {
      PENDING: { color: 'warning', label: '대기' },
      APPROVED: { color: 'info', label: '승인' },
      REJECTED: { color: 'error', label: '거부' },
      RECEIVED: { color: 'primary', label: '입고' },
      INSPECTING: { color: 'info', label: '검사중' },
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

  // 반품 유형 렌더링
  const renderType = (params: GridRenderCellParams) => {
    const type = params.value as string;
    const typeConfig: Record<string, { color: 'default' | 'error' | 'warning' | 'info', label: string }> = {
      DEFECTIVE: { color: 'error', label: '불량품' },
      EXCESS: { color: 'warning', label: '과잉' },
      WRONG_DELIVERY: { color: 'info', label: '오배송' },
      OTHER: { color: 'default', label: '기타' },
    };

    const config = typeConfig[type] || { color: 'default' as const, label: type };

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
    const row = params.row as Return;
    return (
      <Stack direction="column" spacing={0.5}>
        <Typography variant="caption">반품: {row.totalReturnQuantity}</Typography>
        {row.totalReceivedQuantity > 0 && (
          <Typography variant="caption" color="primary.main">
            입고: {row.totalReceivedQuantity}
          </Typography>
        )}
        {row.totalPassedQuantity > 0 && (
          <Typography variant="caption" color="success.main">
            합격: {row.totalPassedQuantity}
          </Typography>
        )}
        {row.totalFailedQuantity > 0 && (
          <Typography variant="caption" color="error.main">
            불합격: {row.totalFailedQuantity}
          </Typography>
        )}
      </Stack>
    );
  };

  // 승인
  const handleApprove = async (returnEntity: Return) => {
    if (!window.confirm(`반품 ${returnEntity.returnNo}을(를) 승인하시겠습니까?`)) {
      return;
    }

    try {
      // TODO: Get actual approver user ID from authentication context
      const approverUserId = 1; // Placeholder
      await axios.post(`/api/returns/${returnEntity.returnId}/approve`, null, {
        params: { approverUserId }
      });
      alert('승인되었습니다.');
      loadReturns();
    } catch (err: any) {
      console.error('Failed to approve return:', err);
      alert(err.response?.data?.message || '승인에 실패했습니다.');
    }
  };

  // 거부 다이얼로그 열기
  const handleRejectDialogOpen = (returnEntity: Return) => {
    setSelectedReturn(returnEntity);
    setRejectionReason('');
    setRejectDialogOpen(true);
  };

  // 거부 실행
  const handleReject = async () => {
    if (!selectedReturn || !rejectionReason.trim()) {
      alert('거부 사유를 입력해주세요.');
      return;
    }

    try {
      // TODO: Get actual approver user ID from authentication context
      const approverUserId = 1; // Placeholder
      await axios.post(`/api/returns/${selectedReturn.returnId}/reject`, null, {
        params: {
          approverUserId,
          reason: rejectionReason
        }
      });
      alert('거부되었습니다.');
      setRejectDialogOpen(false);
      loadReturns();
    } catch (err: any) {
      console.error('Failed to reject return:', err);
      alert(err.response?.data?.message || '거부에 실패했습니다.');
    }
  };

  // 입고
  const handleReceive = async (returnEntity: Return) => {
    if (!window.confirm(`반품 ${returnEntity.returnNo}을(를) 입고 처리하시겠습니까?\n\n재고 트랜잭션이 생성되고 검사가 필요한 경우 품질 검사가 자동 요청됩니다.`)) {
      return;
    }

    try {
      // TODO: Get actual receiver user ID from authentication context
      const receiverUserId = 1; // Placeholder
      await axios.post(`/api/returns/${returnEntity.returnId}/receive`, null, {
        params: { receiverUserId }
      });
      alert('입고가 완료되었습니다.');
      loadReturns();
    } catch (err: any) {
      console.error('Failed to receive return:', err);
      alert(err.response?.data?.message || '입고 처리에 실패했습니다.');
    }
  };

  // 완료 (재고 복원)
  const handleComplete = async (returnEntity: Return) => {
    if (!window.confirm(`반품 ${returnEntity.returnNo}을(를) 완료 처리하시겠습니까?\n\n합격품은 재입고되고 불합격품은 격리창고로 이동됩니다.`)) {
      return;
    }

    try {
      await axios.post(`/api/returns/${returnEntity.returnId}/complete`);
      alert('완료되었습니다.');
      loadReturns();
    } catch (err: any) {
      console.error('Failed to complete return:', err);
      alert(err.response?.data?.message || '완료 처리에 실패했습니다.');
    }
  };

  // 취소 다이얼로그 열기
  const handleCancelDialogOpen = (returnEntity: Return) => {
    setSelectedReturn(returnEntity);
    setCancellationReason('');
    setCancelDialogOpen(true);
  };

  // 취소 실행
  const handleCancel = async () => {
    if (!selectedReturn) return;

    try {
      await axios.post(`/api/returns/${selectedReturn.returnId}/cancel`, null, {
        params: { reason: cancellationReason }
      });
      alert('취소되었습니다.');
      setCancelDialogOpen(false);
      loadReturns();
    } catch (err: any) {
      console.error('Failed to cancel return:', err);
      alert(err.response?.data?.message || '취소에 실패했습니다.');
    }
  };

  // 상세 보기
  const handleView = (returnEntity: Return) => {
    navigate(`/warehouse/returns/${returnEntity.returnId}`);
  };

  // DataGrid 컬럼 정의
  const columns: GridColDef[] = [
    {
      field: 'returnNo',
      headerName: '반품번호',
      width: 150,
      sortable: true,
    },
    {
      field: 'returnDate',
      headerName: '반품일시',
      width: 160,
      renderCell: (params) => {
        return format(new Date(params.value as string), 'yyyy-MM-dd HH:mm');
      },
    },
    {
      field: 'returnStatus',
      headerName: '상태',
      width: 100,
      renderCell: renderStatus,
    },
    {
      field: 'returnType',
      headerName: '유형',
      width: 100,
      renderCell: renderType,
    },
    {
      field: 'materialRequestNo',
      headerName: '불출신청',
      width: 120,
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
        const row = params.row as Return;
        const actions = [
          <GridActionsCellItem
            icon={<ViewIcon />}
            label="상세"
            onClick={() => handleView(row)}
            showInMenu
          />,
        ];

        // PENDING → 승인/거부
        if (row.returnStatus === 'PENDING') {
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

        // APPROVED → 입고/취소
        if (row.returnStatus === 'APPROVED') {
          actions.push(
            <GridActionsCellItem
              icon={<ReceiveIcon />}
              label="입고"
              onClick={() => handleReceive(row)}
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

        // RECEIVED/INSPECTING → 완료
        if (row.returnStatus === 'RECEIVED' || row.returnStatus === 'INSPECTING') {
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
    total: returns.length,
    pending: returns.filter(r => r.returnStatus === 'PENDING').length,
    approved: returns.filter(r => r.returnStatus === 'APPROVED').length,
    received: returns.filter(r => r.returnStatus === 'RECEIVED').length,
    inspecting: returns.filter(r => r.returnStatus === 'INSPECTING').length,
    completed: returns.filter(r => r.returnStatus === 'COMPLETED').length,
    defective: returns.filter(r => r.returnType === 'DEFECTIVE' && !['COMPLETED', 'CANCELLED', 'REJECTED'].includes(r.returnStatus)).length,
  };

  return (
    <Box sx={{ p: 3 }}>
      <Paper sx={{ p: 2 }}>
        {/* 헤더 */}
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="h5" component="h1">
            반품 관리
          </Typography>
          <Stack direction="row" spacing={1}>
            <Button
              variant="outlined"
              startIcon={<RefreshIcon />}
              onClick={loadReturns}
              disabled={loading}
            >
              새로고침
            </Button>
            <Button
              variant="contained"
              startIcon={<AddIcon />}
              onClick={() => navigate('/warehouse/returns/new')}
            >
              신규 반품
            </Button>
          </Stack>
        </Box>

        {/* 통계 */}
        <Stack direction="row" spacing={2} sx={{ mb: 2 }}>
          <Chip label={`전체: ${stats.total}`} />
          <Chip label={`대기: ${stats.pending}`} color="warning" variant="outlined" />
          <Chip label={`승인: ${stats.approved}`} color="info" variant="outlined" />
          <Chip label={`입고: ${stats.received}`} color="primary" variant="outlined" />
          <Chip label={`검사중: ${stats.inspecting}`} color="info" variant="outlined" />
          <Chip label={`완료: ${stats.completed}`} color="success" variant="outlined" />
          {stats.defective > 0 && (
            <Chip label={`불량품: ${stats.defective}`} color="error" />
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
            rows={returns}
            columns={columns}
            getRowId={(row) => row.returnId}
            initialState={{
              pagination: {
                paginationModel: { page: 0, pageSize: 25 },
              },
              sorting: {
                sortModel: [{ field: 'returnDate', sort: 'desc' }],
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
        <DialogTitle>반품 거부</DialogTitle>
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
        <DialogTitle>반품 취소</DialogTitle>
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

export default ReturnsPage;
