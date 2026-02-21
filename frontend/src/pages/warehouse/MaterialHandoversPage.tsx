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
import { useTranslation } from 'react-i18next';
import axios from 'axios';
import { format } from 'date-fns';
import { useAuthStore } from '@/stores/authStore';
import { getErrorMessage } from '@/utils/errorUtils';

interface MaterialHandover {
  materialHandoverId: number;
  handoverNo: string;
  handoverDate: string;
  handoverStatus: string;
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
  const { t } = useTranslation();
  const { user } = useAuthStore();
  const [handovers, setHandovers] = useState<MaterialHandover[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedHandover, setSelectedHandover] = useState<MaterialHandover | null>(null);

  const [confirmDialogOpen, setConfirmDialogOpen] = useState(false);
  const [rejectDialogOpen, setRejectDialogOpen] = useState(false);
  const [confirmRemarks, setConfirmRemarks] = useState('');
  const [rejectReason, setRejectReason] = useState('');

  const loadHandovers = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await axios.get('/api/material-handovers');
      setHandovers(response.data.data || []);
    } catch (err) {
      setError(getErrorMessage(err, t('pages.materialHandovers.errors.loadFailed')));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadHandovers();
  }, []);

  const renderStatus = (params: GridRenderCellParams) => {
    const status = params.value as string;
    const statusConfig: Record<string, { color: 'default' | 'primary' | 'success' | 'error' | 'warning', label: string, icon?: React.ReactElement }> = {
      PENDING: { color: 'warning', label: t('pages.materialHandovers.status.pending'), icon: <PendingIcon fontSize="small" /> },
      CONFIRMED: { color: 'success', label: t('pages.materialHandovers.status.confirmed'), icon: <ConfirmIcon fontSize="small" /> },
      REJECTED: { color: 'error', label: t('pages.materialHandovers.status.rejected'), icon: <RejectIcon fontSize="small" /> },
    };
    const config = statusConfig[status] || { color: 'default' as const, label: status };
    return <Chip icon={config.icon} label={config.label} color={config.color} size="small" variant="outlined" />;
  };

  const renderLotQuality = (params: GridRenderCellParams) => {
    const quality = params.value as string;
    const qualityConfig: Record<string, { color: 'default' | 'success' | 'error' | 'warning' | 'info', label: string }> = {
      PASSED: { color: 'success', label: t('pages.materialHandovers.lotQuality.passed') },
      FAILED: { color: 'error', label: t('pages.materialHandovers.lotQuality.failed') },
      PENDING: { color: 'warning', label: t('pages.materialHandovers.lotQuality.pending') },
      CONDITIONAL: { color: 'info', label: t('pages.materialHandovers.lotQuality.conditional') },
    };
    const config = qualityConfig[quality] || { color: 'default' as const, label: quality };
    return <Chip label={config.label} color={config.color} size="small" />;
  };

  const handleConfirmDialogOpen = (handover: MaterialHandover) => {
    setSelectedHandover(handover);
    setConfirmRemarks('');
    setConfirmDialogOpen(true);
  };

  const handleConfirm = async () => {
    if (!selectedHandover) return;
    try {
      const receiverId = user?.userId ?? 0;
      await axios.post(`/api/material-handovers/${selectedHandover.materialHandoverId}/confirm`, null, {
        params: { receiverId, remarks: confirmRemarks }
      });
      alert(t('pages.materialHandovers.messages.confirmed'));
      setConfirmDialogOpen(false);
      loadHandovers();
    } catch (err) {
      alert(getErrorMessage(err, t('pages.materialHandovers.messages.confirmFailed')));
    }
  };

  const handleRejectDialogOpen = (handover: MaterialHandover) => {
    setSelectedHandover(handover);
    setRejectReason('');
    setRejectDialogOpen(true);
  };

  const handleReject = async () => {
    if (!selectedHandover || !rejectReason.trim()) {
      alert(t('pages.materialHandovers.dialogs.rejectReasonRequired'));
      return;
    }
    try {
      const receiverId = user?.userId ?? 0;
      await axios.post(`/api/material-handovers/${selectedHandover.materialHandoverId}/reject`, null, {
        params: { receiverId, reason: rejectReason }
      });
      alert(t('pages.materialHandovers.messages.rejected'));
      setRejectDialogOpen(false);
      loadHandovers();
    } catch (err) {
      alert(getErrorMessage(err, t('pages.materialHandovers.messages.rejectFailed')));
    }
  };

  const columns: GridColDef[] = [
    { field: 'handoverNo', headerName: t('pages.materialHandovers.fields.handoverNo'), width: 150, sortable: true },
    {
      field: 'handoverDate', headerName: t('pages.materialHandovers.fields.handoverDate'), width: 160,
      renderCell: (params) => format(new Date(params.value as string), 'yyyy-MM-dd HH:mm'),
    },
    { field: 'handoverStatus', headerName: t('common.labels.status'), width: 100, renderCell: renderStatus },
    { field: 'materialRequestNo', headerName: t('pages.materialHandovers.fields.materialRequestNo'), width: 150 },
    { field: 'productCode', headerName: t('pages.materialHandovers.fields.productCode'), width: 120 },
    { field: 'productName', headerName: t('pages.materialHandovers.fields.productName'), width: 200 },
    { field: 'lotNo', headerName: t('pages.materialHandovers.fields.lotNo'), width: 150 },
    { field: 'lotQualityStatus', headerName: t('pages.materialHandovers.fields.lotQuality'), width: 100, renderCell: renderLotQuality },
    {
      field: 'quantity', headerName: t('pages.materialHandovers.fields.quantity'), width: 100,
      renderCell: (params) => { const row = params.row as MaterialHandover; return `${params.value} ${row.unit}`; },
    },
    { field: 'issuerName', headerName: t('pages.materialHandovers.fields.issuer'), width: 100 },
    { field: 'issueLocation', headerName: t('pages.materialHandovers.fields.issueLocation'), width: 120 },
    { field: 'receiverName', headerName: t('pages.materialHandovers.fields.receiver'), width: 100 },
    { field: 'receiveLocation', headerName: t('pages.materialHandovers.fields.receiveLocation'), width: 120 },
    {
      field: 'receivedDate', headerName: t('pages.materialHandovers.fields.receivedDate'), width: 160,
      renderCell: (params) => { if (!params.value) return '-'; return format(new Date(params.value as string), 'yyyy-MM-dd HH:mm'); },
    },
    {
      field: 'actions', type: 'actions', headerName: t('common.labels.actions'), width: 120,
      getActions: (params) => {
        const row = params.row as MaterialHandover;
        const actions = [];
        if (row.handoverStatus === 'PENDING') {
          actions.push(
            <GridActionsCellItem icon={<ConfirmIcon />} label={t('pages.materialHandovers.actions.confirm')} onClick={() => handleConfirmDialogOpen(row)} color="success" showInMenu />,
            <GridActionsCellItem icon={<RejectIcon />} label={t('pages.materialHandovers.actions.reject')} onClick={() => handleRejectDialogOpen(row)} color="error" showInMenu />
          );
        }
        return actions;
      },
    },
  ];

  const stats = {
    total: handovers.length,
    pending: handovers.filter(h => h.handoverStatus === 'PENDING').length,
    confirmed: handovers.filter(h => h.handoverStatus === 'CONFIRMED').length,
    rejected: handovers.filter(h => h.handoverStatus === 'REJECTED').length,
  };

  return (
    <Box sx={{ p: 3 }}>
      <Paper sx={{ p: 2 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="h5" component="h1">{t('pages.materialHandovers.title')}</Typography>
          <Button variant="outlined" startIcon={<RefreshIcon />} onClick={loadHandovers} disabled={loading}>{t('common.buttons.refresh')}</Button>
        </Box>

        <Stack direction="row" spacing={2} sx={{ mb: 2 }}>
          <Chip label={`${t('pages.materialHandovers.stats.total')}: ${stats.total}`} />
          <Chip label={`${t('pages.materialHandovers.stats.pending')}: ${stats.pending}`} color="warning" variant="outlined" />
          <Chip label={`${t('pages.materialHandovers.stats.confirmed')}: ${stats.confirmed}`} color="success" variant="outlined" />
          <Chip label={`${t('pages.materialHandovers.stats.rejected')}: ${stats.rejected}`} color="error" variant="outlined" />
        </Stack>

        {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>{error}</Alert>}

        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}><CircularProgress /></Box>
        ) : (
          <DataGrid
            rows={handovers} columns={columns} getRowId={(row) => row.materialHandoverId}
            initialState={{ pagination: { paginationModel: { page: 0, pageSize: 25 } }, sorting: { sortModel: [{ field: 'handoverDate', sort: 'desc' }] } }}
            pageSizeOptions={[10, 25, 50, 100]} checkboxSelection={false} disableRowSelectionOnClick autoHeight density="compact"
            sx={{ '& .MuiDataGrid-cell': { borderBottom: '1px solid #f0f0f0' }, '& .MuiDataGrid-row:hover': { backgroundColor: '#f5f5f5' } }}
          />
        )}
      </Paper>

      {/* Confirm Dialog */}
      <Dialog open={confirmDialogOpen} onClose={() => setConfirmDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{t('pages.materialHandovers.dialogs.confirmTitle')}</DialogTitle>
        <DialogContent>
          {selectedHandover && (
            <Box sx={{ mb: 2, mt: 1 }}>
              <Typography variant="body2" color="text.secondary">
                <strong>{t('pages.materialHandovers.dialogs.handoverNo')}:</strong> {selectedHandover.handoverNo}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                <strong>{t('pages.materialHandovers.dialogs.product')}:</strong> {selectedHandover.productName} ({selectedHandover.productCode})
              </Typography>
              <Typography variant="body2" color="text.secondary">
                <strong>{t('pages.materialHandovers.dialogs.lot')}:</strong> {selectedHandover.lotNo}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                <strong>{t('pages.materialHandovers.dialogs.quantity')}:</strong> {selectedHandover.quantity} {selectedHandover.unit}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                <strong>{t('pages.materialHandovers.dialogs.issuer')}:</strong> {selectedHandover.issuerName} ({selectedHandover.issueLocation})
              </Typography>
            </Box>
          )}
          <TextField
            autoFocus margin="dense" label={t('pages.materialHandovers.dialogs.confirmMemo')} fullWidth multiline rows={3}
            value={confirmRemarks} onChange={(e) => setConfirmRemarks(e.target.value)}
            placeholder={t('pages.materialHandovers.dialogs.confirmPlaceholder')}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmDialogOpen(false)}>{t('common.buttons.cancel')}</Button>
          <Button onClick={handleConfirm} variant="contained" color="success">{t('pages.materialHandovers.actions.confirm')}</Button>
        </DialogActions>
      </Dialog>

      {/* Reject Dialog */}
      <Dialog open={rejectDialogOpen} onClose={() => setRejectDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{t('pages.materialHandovers.dialogs.rejectTitle')}</DialogTitle>
        <DialogContent>
          {selectedHandover && (
            <Box sx={{ mb: 2, mt: 1 }}>
              <Typography variant="body2" color="text.secondary">
                <strong>{t('pages.materialHandovers.dialogs.handoverNo')}:</strong> {selectedHandover.handoverNo}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                <strong>{t('pages.materialHandovers.dialogs.product')}:</strong> {selectedHandover.productName} ({selectedHandover.productCode})
              </Typography>
              <Typography variant="body2" color="text.secondary">
                <strong>{t('pages.materialHandovers.dialogs.lot')}:</strong> {selectedHandover.lotNo}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                <strong>{t('pages.materialHandovers.dialogs.quantity')}:</strong> {selectedHandover.quantity} {selectedHandover.unit}
              </Typography>
            </Box>
          )}
          <TextField
            autoFocus margin="dense" label={t('pages.materialHandovers.dialogs.rejectReason')} fullWidth multiline rows={4}
            value={rejectReason} onChange={(e) => setRejectReason(e.target.value)} required
            error={!rejectReason.trim()} helperText={!rejectReason.trim() ? t('pages.materialHandovers.dialogs.rejectReasonRequired') : ''}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setRejectDialogOpen(false)}>{t('common.buttons.cancel')}</Button>
          <Button onClick={handleReject} variant="contained" color="error" disabled={!rejectReason.trim()}>{t('pages.materialHandovers.actions.reject')}</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default MaterialHandoversPage;
