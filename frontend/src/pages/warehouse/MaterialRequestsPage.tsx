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
import { useTranslation } from 'react-i18next';
import axios from 'axios';
import { format } from 'date-fns';
import { useAuthStore } from '@/stores/authStore';

interface MaterialRequest {
  materialRequestId: number;
  requestNo: string;
  requestDate: string;
  requestStatus: string;
  priority: string;
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
  const { t } = useTranslation();
  const { user } = useAuthStore();
  const navigate = useNavigate();
  const [requests, setRequests] = useState<MaterialRequest[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedRequest, setSelectedRequest] = useState<MaterialRequest | null>(null);

  const [rejectDialogOpen, setRejectDialogOpen] = useState(false);
  const [cancelDialogOpen, setCancelDialogOpen] = useState(false);
  const [rejectionReason, setRejectionReason] = useState('');
  const [cancellationReason, setCancellationReason] = useState('');

  const loadRequests = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await axios.get('/api/material-requests');
      setRequests(response.data.data || []);
    } catch (err: any) {
      setError(err.response?.data?.message || t('pages.materialRequests.errors.loadFailed'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadRequests();
  }, []);

  const renderStatus = (params: GridRenderCellParams) => {
    const status = params.value as string;
    const statusConfig: Record<string, { color: 'default' | 'primary' | 'success' | 'error' | 'warning' | 'info', label: string }> = {
      PENDING: { color: 'warning', label: t('pages.materialRequests.status.pending') },
      APPROVED: { color: 'info', label: t('pages.materialRequests.status.approved') },
      REJECTED: { color: 'error', label: t('pages.materialRequests.status.rejected') },
      ISSUED: { color: 'primary', label: t('pages.materialRequests.status.issued') },
      COMPLETED: { color: 'success', label: t('pages.materialRequests.status.completed') },
      CANCELLED: { color: 'default', label: t('pages.materialRequests.status.cancelled') },
    };
    const config = statusConfig[status] || { color: 'default' as const, label: status };
    return <Chip label={config.label} color={config.color} size="small" variant="outlined" />;
  };

  const renderPriority = (params: GridRenderCellParams) => {
    const priority = params.value as string;
    const priorityConfig: Record<string, { color: 'default' | 'error' | 'warning' | 'info', label: string }> = {
      URGENT: { color: 'error', label: t('pages.materialRequests.priority.urgent') },
      HIGH: { color: 'warning', label: t('pages.materialRequests.priority.high') },
      NORMAL: { color: 'info', label: t('pages.materialRequests.priority.normal') },
      LOW: { color: 'default', label: t('pages.materialRequests.priority.low') },
    };
    const config = priorityConfig[priority] || { color: 'default' as const, label: priority };
    return <Chip label={config.label} color={config.color} size="small" />;
  };

  const renderQuantities = (params: GridRenderCellParams) => {
    const row = params.row as MaterialRequest;
    return (
      <Stack direction="column" spacing={0.5}>
        <Typography variant="caption">{t('pages.materialRequests.quantities.requested')}: {row.totalRequestedQuantity}</Typography>
        {row.totalApprovedQuantity > 0 && (
          <Typography variant="caption" color="info.main">
            {t('pages.materialRequests.quantities.approved')}: {row.totalApprovedQuantity}
          </Typography>
        )}
        {row.totalIssuedQuantity > 0 && (
          <Typography variant="caption" color="success.main">
            {t('pages.materialRequests.quantities.issued')}: {row.totalIssuedQuantity}
          </Typography>
        )}
      </Stack>
    );
  };

  const handleApprove = async (request: MaterialRequest) => {
    if (!window.confirm(t('pages.materialRequests.confirms.approve', { no: request.requestNo }))) return;
    try {
      const approverUserId = user?.userId ?? 0;
      await axios.post(`/api/material-requests/${request.materialRequestId}/approve`, null, { params: { approverUserId } });
      alert(t('pages.materialRequests.messages.approved'));
      loadRequests();
    } catch (err: any) {
      alert(err.response?.data?.message || t('pages.materialRequests.messages.approveFailed'));
    }
  };

  const handleRejectDialogOpen = (request: MaterialRequest) => {
    setSelectedRequest(request);
    setRejectionReason('');
    setRejectDialogOpen(true);
  };

  const handleReject = async () => {
    if (!selectedRequest || !rejectionReason.trim()) {
      alert(t('pages.materialRequests.messages.enterRejectReason'));
      return;
    }
    try {
      const approverUserId = user?.userId ?? 0;
      await axios.post(`/api/material-requests/${selectedRequest.materialRequestId}/reject`, null, {
        params: { approverUserId, reason: rejectionReason }
      });
      alert(t('pages.materialRequests.messages.rejected'));
      setRejectDialogOpen(false);
      loadRequests();
    } catch (err: any) {
      alert(err.response?.data?.message || t('pages.materialRequests.messages.rejectFailed'));
    }
  };

  const handleIssue = async (request: MaterialRequest) => {
    if (!window.confirm(t('pages.materialRequests.confirms.issue', { no: request.requestNo }))) return;
    try {
      const issuerUserId = user?.userId ?? 0;
      await axios.post(`/api/material-requests/${request.materialRequestId}/issue`, null, { params: { issuerUserId } });
      alert(t('pages.materialRequests.messages.issued'));
      loadRequests();
    } catch (err: any) {
      alert(err.response?.data?.message || t('pages.materialRequests.messages.issueFailed'));
    }
  };

  const handleComplete = async (request: MaterialRequest) => {
    if (!window.confirm(t('pages.materialRequests.confirms.complete', { no: request.requestNo }))) return;
    try {
      await axios.post(`/api/material-requests/${request.materialRequestId}/complete`);
      alert(t('pages.materialRequests.messages.completed'));
      loadRequests();
    } catch (err: any) {
      alert(err.response?.data?.message || t('pages.materialRequests.messages.completeFailed'));
    }
  };

  const handleCancelDialogOpen = (request: MaterialRequest) => {
    setSelectedRequest(request);
    setCancellationReason('');
    setCancelDialogOpen(true);
  };

  const handleCancel = async () => {
    if (!selectedRequest) return;
    try {
      await axios.post(`/api/material-requests/${selectedRequest.materialRequestId}/cancel`, null, {
        params: { reason: cancellationReason }
      });
      alert(t('pages.materialRequests.messages.cancelled'));
      setCancelDialogOpen(false);
      loadRequests();
    } catch (err: any) {
      alert(err.response?.data?.message || t('pages.materialRequests.messages.cancelFailed'));
    }
  };

  const handleView = (request: MaterialRequest) => {
    navigate(`/warehouse/material-requests/${request.materialRequestId}`);
  };

  const columns: GridColDef[] = [
    { field: 'requestNo', headerName: t('pages.materialRequests.fields.requestNo'), width: 150, sortable: true },
    {
      field: 'requestDate', headerName: t('pages.materialRequests.fields.requestDate'), width: 160,
      renderCell: (params) => format(new Date(params.value as string), 'yyyy-MM-dd HH:mm'),
    },
    { field: 'requestStatus', headerName: t('common.labels.status'), width: 100, renderCell: renderStatus },
    { field: 'priority', headerName: t('pages.materialRequests.fields.priority'), width: 100, renderCell: renderPriority },
    {
      field: 'purpose', headerName: t('pages.materialRequests.fields.purpose'), width: 120,
      renderCell: (params) => {
        const purposeMap: Record<string, string> = {
          PRODUCTION: t('pages.materialRequests.purpose.production'),
          MAINTENANCE: t('pages.materialRequests.purpose.maintenance'),
          SAMPLE: t('pages.materialRequests.purpose.sample'),
          OTHER: t('pages.materialRequests.purpose.other'),
        };
        return purposeMap[params.value as string] || params.value;
      },
    },
    { field: 'workOrderNo', headerName: t('pages.materialRequests.fields.workOrderNo'), width: 120 },
    { field: 'requesterName', headerName: t('pages.materialRequests.fields.requester'), width: 100 },
    { field: 'warehouseName', headerName: t('pages.materialRequests.fields.warehouse'), width: 120 },
    {
      field: 'requiredDate', headerName: t('pages.materialRequests.fields.requiredDate'), width: 120,
      renderCell: (params) => format(new Date(params.value as string), 'yyyy-MM-dd'),
    },
    { field: 'quantities', headerName: t('pages.materialRequests.fields.quantities'), width: 120, renderCell: renderQuantities, sortable: false },
    { field: 'approverName', headerName: t('pages.materialRequests.fields.approver'), width: 100 },
    {
      field: 'actions', type: 'actions', headerName: t('common.labels.actions'), width: 200,
      getActions: (params) => {
        const row = params.row as MaterialRequest;
        const actions = [
          <GridActionsCellItem icon={<ViewIcon />} label={t('pages.materialRequests.actions.view')} onClick={() => handleView(row)} showInMenu />,
        ];
        if (row.requestStatus === 'PENDING') {
          actions.push(
            <GridActionsCellItem icon={<ApproveIcon />} label={t('pages.materialRequests.actions.approve')} onClick={() => handleApprove(row)} showInMenu />,
            <GridActionsCellItem icon={<RejectIcon />} label={t('pages.materialRequests.actions.reject')} onClick={() => handleRejectDialogOpen(row)} showInMenu />,
            <GridActionsCellItem icon={<CancelIcon />} label={t('pages.materialRequests.actions.cancel')} onClick={() => handleCancelDialogOpen(row)} showInMenu />
          );
        }
        if (row.requestStatus === 'APPROVED') {
          actions.push(
            <GridActionsCellItem icon={<IssueIcon />} label={t('pages.materialRequests.actions.issue')} onClick={() => handleIssue(row)} showInMenu />,
            <GridActionsCellItem icon={<CancelIcon />} label={t('pages.materialRequests.actions.cancel')} onClick={() => handleCancelDialogOpen(row)} showInMenu />
          );
        }
        if (row.requestStatus === 'ISSUED') {
          actions.push(
            <GridActionsCellItem icon={<CompleteIcon />} label={t('pages.materialRequests.actions.complete')} onClick={() => handleComplete(row)} showInMenu />
          );
        }
        return actions;
      },
    },
  ];

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
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="h5" component="h1">{t('pages.materialRequests.title')}</Typography>
          <Stack direction="row" spacing={1}>
            <Button variant="outlined" startIcon={<RefreshIcon />} onClick={loadRequests} disabled={loading}>{t('common.buttons.refresh')}</Button>
            <Button variant="contained" startIcon={<AddIcon />} onClick={() => navigate('/warehouse/material-requests/new')}>{t('pages.materialRequests.actions.create')}</Button>
          </Stack>
        </Box>

        <Stack direction="row" spacing={2} sx={{ mb: 2 }}>
          <Chip label={`${t('pages.materialRequests.stats.total')}: ${stats.total}`} />
          <Chip label={`${t('pages.materialRequests.stats.pending')}: ${stats.pending}`} color="warning" variant="outlined" />
          <Chip label={`${t('pages.materialRequests.stats.approved')}: ${stats.approved}`} color="info" variant="outlined" />
          <Chip label={`${t('pages.materialRequests.stats.issued')}: ${stats.issued}`} color="primary" variant="outlined" />
          <Chip label={`${t('pages.materialRequests.stats.completed')}: ${stats.completed}`} color="success" variant="outlined" />
          {stats.urgent > 0 && <Chip label={`${t('pages.materialRequests.stats.urgent')}: ${stats.urgent}`} color="error" />}
        </Stack>

        {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>{error}</Alert>}

        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}><CircularProgress /></Box>
        ) : (
          <DataGrid
            rows={requests} columns={columns} getRowId={(row) => row.materialRequestId}
            initialState={{ pagination: { paginationModel: { page: 0, pageSize: 25 } }, sorting: { sortModel: [{ field: 'requestDate', sort: 'desc' }] } }}
            pageSizeOptions={[10, 25, 50, 100]} checkboxSelection={false} disableRowSelectionOnClick autoHeight density="compact"
            sx={{ '& .MuiDataGrid-cell': { borderBottom: '1px solid #f0f0f0' } }}
          />
        )}
      </Paper>

      <Dialog open={rejectDialogOpen} onClose={() => setRejectDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{t('pages.materialRequests.dialogs.rejectTitle')}</DialogTitle>
        <DialogContent>
          <TextField autoFocus margin="dense" label={t('pages.materialRequests.dialogs.rejectReason')} fullWidth multiline rows={4} value={rejectionReason} onChange={(e) => setRejectionReason(e.target.value)} required />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setRejectDialogOpen(false)}>{t('common.buttons.cancel')}</Button>
          <Button onClick={handleReject} variant="contained" color="error">{t('pages.materialRequests.actions.reject')}</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={cancelDialogOpen} onClose={() => setCancelDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{t('pages.materialRequests.dialogs.cancelTitle')}</DialogTitle>
        <DialogContent>
          <TextField autoFocus margin="dense" label={t('pages.materialRequests.dialogs.cancelReason')} fullWidth multiline rows={4} value={cancellationReason} onChange={(e) => setCancellationReason(e.target.value)} />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setCancelDialogOpen(false)}>{t('common.buttons.close')}</Button>
          <Button onClick={handleCancel} variant="contained" color="warning">{t('pages.materialRequests.dialogs.cancelConfirm')}</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default MaterialRequestsPage;
