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
import { useTranslation } from 'react-i18next';
import axios from 'axios';
import { format } from 'date-fns';
import { useAuthStore } from '@/stores/authStore';
import { getErrorMessage } from '@/utils/errorUtils';

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
  const { t } = useTranslation();
  const { user } = useAuthStore();
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

  const loadReturns = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await axios.get('/api/returns');
      setReturns(response.data.data || []);
    } catch (err) {
      setError(getErrorMessage(err, t('pages.returns.errors.loadFailed')));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadReturns();
  }, []);

  const renderStatus = (params: GridRenderCellParams) => {
    const status = params.value as string;
    const statusConfig: Record<string, { color: 'default' | 'primary' | 'success' | 'error' | 'warning' | 'info', label: string }> = {
      PENDING: { color: 'warning', label: t('pages.returns.status.pending') },
      APPROVED: { color: 'info', label: t('pages.returns.status.approved') },
      REJECTED: { color: 'error', label: t('pages.returns.status.rejected') },
      RECEIVED: { color: 'primary', label: t('pages.returns.status.received') },
      INSPECTING: { color: 'info', label: t('pages.returns.status.inspecting') },
      COMPLETED: { color: 'success', label: t('pages.returns.status.completed') },
      CANCELLED: { color: 'default', label: t('pages.returns.status.cancelled') },
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

  const renderType = (params: GridRenderCellParams) => {
    const type = params.value as string;
    const typeConfig: Record<string, { color: 'default' | 'error' | 'warning' | 'info', label: string }> = {
      DEFECTIVE: { color: 'error', label: t('pages.returns.types.defective') },
      EXCESS: { color: 'warning', label: t('pages.returns.types.excess') },
      WRONG_DELIVERY: { color: 'info', label: t('pages.returns.types.wrongDelivery') },
      OTHER: { color: 'default', label: t('pages.returns.types.other') },
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

  const renderQuantities = (params: GridRenderCellParams) => {
    const row = params.row as Return;
    return (
      <Stack direction="column" spacing={0.5}>
        <Typography variant="caption">{t('pages.returns.quantities.return')}: {row.totalReturnQuantity}</Typography>
        {row.totalReceivedQuantity > 0 && (
          <Typography variant="caption" color="primary.main">
            {t('pages.returns.quantities.received')}: {row.totalReceivedQuantity}
          </Typography>
        )}
        {row.totalPassedQuantity > 0 && (
          <Typography variant="caption" color="success.main">
            {t('pages.returns.quantities.passed')}: {row.totalPassedQuantity}
          </Typography>
        )}
        {row.totalFailedQuantity > 0 && (
          <Typography variant="caption" color="error.main">
            {t('pages.returns.quantities.failed')}: {row.totalFailedQuantity}
          </Typography>
        )}
      </Stack>
    );
  };

  const handleApprove = async (returnEntity: Return) => {
    if (!window.confirm(t('pages.returns.confirms.approve', { no: returnEntity.returnNo }))) {
      return;
    }

    try {
      const approverUserId = user?.userId ?? 0;
      await axios.post(`/api/returns/${returnEntity.returnId}/approve`, null, {
        params: { approverUserId }
      });
      alert(t('pages.returns.messages.approved'));
      loadReturns();
    } catch (err) {
      alert(getErrorMessage(err, t('pages.returns.messages.approveFailed')));
    }
  };

  const handleRejectDialogOpen = (returnEntity: Return) => {
    setSelectedReturn(returnEntity);
    setRejectionReason('');
    setRejectDialogOpen(true);
  };

  const handleReject = async () => {
    if (!selectedReturn || !rejectionReason.trim()) {
      alert(t('pages.returns.messages.enterRejectReason'));
      return;
    }

    try {
      const approverUserId = user?.userId ?? 0;
      await axios.post(`/api/returns/${selectedReturn.returnId}/reject`, null, {
        params: {
          approverUserId,
          reason: rejectionReason
        }
      });
      alert(t('pages.returns.messages.rejected'));
      setRejectDialogOpen(false);
      loadReturns();
    } catch (err) {
      alert(getErrorMessage(err, t('pages.returns.messages.rejectFailed')));
    }
  };

  const handleReceive = async (returnEntity: Return) => {
    if (!window.confirm(t('pages.returns.confirms.receive', { no: returnEntity.returnNo }))) {
      return;
    }

    try {
      const receiverUserId = user?.userId ?? 0;
      await axios.post(`/api/returns/${returnEntity.returnId}/receive`, null, {
        params: { receiverUserId }
      });
      alert(t('pages.returns.messages.received'));
      loadReturns();
    } catch (err) {
      alert(getErrorMessage(err, t('pages.returns.messages.receiveFailed')));
    }
  };

  const handleComplete = async (returnEntity: Return) => {
    if (!window.confirm(t('pages.returns.confirms.complete', { no: returnEntity.returnNo }))) {
      return;
    }

    try {
      await axios.post(`/api/returns/${returnEntity.returnId}/complete`);
      alert(t('pages.returns.messages.completed'));
      loadReturns();
    } catch (err) {
      alert(getErrorMessage(err, t('pages.returns.messages.completeFailed')));
    }
  };

  const handleCancelDialogOpen = (returnEntity: Return) => {
    setSelectedReturn(returnEntity);
    setCancellationReason('');
    setCancelDialogOpen(true);
  };

  const handleCancel = async () => {
    if (!selectedReturn) return;

    try {
      await axios.post(`/api/returns/${selectedReturn.returnId}/cancel`, null, {
        params: { reason: cancellationReason }
      });
      alert(t('pages.returns.messages.cancelled'));
      setCancelDialogOpen(false);
      loadReturns();
    } catch (err) {
      alert(getErrorMessage(err, t('pages.returns.messages.cancelFailed')));
    }
  };

  const handleView = (returnEntity: Return) => {
    navigate(`/warehouse/returns/${returnEntity.returnId}`);
  };

  const columns: GridColDef[] = [
    {
      field: 'returnNo',
      headerName: t('pages.returns.fields.returnNo'),
      width: 150,
      sortable: true,
    },
    {
      field: 'returnDate',
      headerName: t('pages.returns.fields.returnDate'),
      width: 160,
      renderCell: (params) => {
        return format(new Date(params.value as string), 'yyyy-MM-dd HH:mm');
      },
    },
    {
      field: 'returnStatus',
      headerName: t('common.labels.status'),
      width: 100,
      renderCell: renderStatus,
    },
    {
      field: 'returnType',
      headerName: t('pages.returns.fields.returnType'),
      width: 100,
      renderCell: renderType,
    },
    {
      field: 'materialRequestNo',
      headerName: t('pages.returns.fields.materialRequestNo'),
      width: 120,
    },
    {
      field: 'workOrderNo',
      headerName: t('pages.returns.fields.workOrderNo'),
      width: 120,
    },
    {
      field: 'requesterName',
      headerName: t('pages.returns.fields.requester'),
      width: 100,
    },
    {
      field: 'warehouseName',
      headerName: t('pages.returns.fields.warehouse'),
      width: 120,
    },
    {
      field: 'quantities',
      headerName: t('pages.returns.fields.quantities'),
      width: 120,
      renderCell: renderQuantities,
      sortable: false,
    },
    {
      field: 'approverName',
      headerName: t('pages.returns.fields.approver'),
      width: 100,
    },
    {
      field: 'actions',
      type: 'actions',
      headerName: t('common.labels.actions'),
      width: 200,
      getActions: (params) => {
        const row = params.row as Return;
        const actions = [
          <GridActionsCellItem
            icon={<ViewIcon />}
            label={t('pages.returns.actions.view')}
            onClick={() => handleView(row)}
            showInMenu
          />,
        ];

        if (row.returnStatus === 'PENDING') {
          actions.push(
            <GridActionsCellItem
              icon={<ApproveIcon />}
              label={t('pages.returns.actions.approve')}
              onClick={() => handleApprove(row)}
              showInMenu
            />,
            <GridActionsCellItem
              icon={<RejectIcon />}
              label={t('pages.returns.actions.reject')}
              onClick={() => handleRejectDialogOpen(row)}
              showInMenu
            />,
            <GridActionsCellItem
              icon={<CancelIcon />}
              label={t('pages.returns.actions.cancel')}
              onClick={() => handleCancelDialogOpen(row)}
              showInMenu
            />
          );
        }

        if (row.returnStatus === 'APPROVED') {
          actions.push(
            <GridActionsCellItem
              icon={<ReceiveIcon />}
              label={t('pages.returns.actions.receive')}
              onClick={() => handleReceive(row)}
              showInMenu
            />,
            <GridActionsCellItem
              icon={<CancelIcon />}
              label={t('pages.returns.actions.cancel')}
              onClick={() => handleCancelDialogOpen(row)}
              showInMenu
            />
          );
        }

        if (row.returnStatus === 'RECEIVED' || row.returnStatus === 'INSPECTING') {
          actions.push(
            <GridActionsCellItem
              icon={<CompleteIcon />}
              label={t('pages.returns.actions.complete')}
              onClick={() => handleComplete(row)}
              showInMenu
            />
          );
        }

        return actions;
      },
    },
  ];

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
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="h5" component="h1">
            {t('pages.returns.title')}
          </Typography>
          <Stack direction="row" spacing={1}>
            <Button
              variant="outlined"
              startIcon={<RefreshIcon />}
              onClick={loadReturns}
              disabled={loading}
            >
              {t('common.buttons.refresh')}
            </Button>
            <Button
              variant="contained"
              startIcon={<AddIcon />}
              onClick={() => navigate('/warehouse/returns/new')}
            >
              {t('pages.returns.actions.create')}
            </Button>
          </Stack>
        </Box>

        <Stack direction="row" spacing={2} sx={{ mb: 2 }}>
          <Chip label={`${t('pages.returns.stats.total')}: ${stats.total}`} />
          <Chip label={`${t('pages.returns.stats.pending')}: ${stats.pending}`} color="warning" variant="outlined" />
          <Chip label={`${t('pages.returns.stats.approved')}: ${stats.approved}`} color="info" variant="outlined" />
          <Chip label={`${t('pages.returns.stats.received')}: ${stats.received}`} color="primary" variant="outlined" />
          <Chip label={`${t('pages.returns.stats.inspecting')}: ${stats.inspecting}`} color="info" variant="outlined" />
          <Chip label={`${t('pages.returns.stats.completed')}: ${stats.completed}`} color="success" variant="outlined" />
          {stats.defective > 0 && (
            <Chip label={`${t('pages.returns.stats.defective')}: ${stats.defective}`} color="error" />
          )}
        </Stack>

        {error && (
          <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
            {error}
          </Alert>
        )}

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

      {/* Reject Dialog */}
      <Dialog open={rejectDialogOpen} onClose={() => setRejectDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{t('pages.returns.dialogs.rejectTitle')}</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin="dense"
            label={t('pages.returns.dialogs.rejectReason')}
            fullWidth
            multiline
            rows={4}
            value={rejectionReason}
            onChange={(e) => setRejectionReason(e.target.value)}
            required
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setRejectDialogOpen(false)}>{t('common.buttons.cancel')}</Button>
          <Button onClick={handleReject} variant="contained" color="error">
            {t('pages.returns.actions.reject')}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Cancel Dialog */}
      <Dialog open={cancelDialogOpen} onClose={() => setCancelDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{t('pages.returns.dialogs.cancelTitle')}</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin="dense"
            label={t('pages.returns.dialogs.cancelReason')}
            fullWidth
            multiline
            rows={4}
            value={cancellationReason}
            onChange={(e) => setCancellationReason(e.target.value)}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setCancelDialogOpen(false)}>{t('common.buttons.close')}</Button>
          <Button onClick={handleCancel} variant="contained" color="warning">
            {t('pages.returns.dialogs.cancelConfirm')}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default ReturnsPage;
