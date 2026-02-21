/**
 * Approval Page
 * 결재 관리 페이지
 */

import React, { useState, useEffect } from 'react';
import {
  Box,
  Paper,
  Typography,
  Button,
  Chip,
  Grid,
  Card,
  CardContent,
  Tabs,
  Tab,
  Alert,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  IconButton,
  Stepper,
  Step,
  StepLabel,
  LinearProgress
} from '@mui/material';
import { DataGrid, GridColDef, GridRenderCellParams } from '@mui/x-data-grid';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import CancelIcon from '@mui/icons-material/Cancel';
import VisibilityIcon from '@mui/icons-material/Visibility';
import ApprovalIcon from '@mui/icons-material/Approval';
import {
  approvalService,
  ApprovalInstance,
  ApprovalDelegation,
  ApprovalStatistics,
  ApprovalLineTemplate,
  getApprovalStatusLabel,
  getApprovalStatusColor,
  getDocumentTypeLabel,
  formatDateTime
} from '../../services/approvalService';
import { useTranslation } from 'react-i18next';
import { useAuthStore } from '@/stores/authStore';
import { getErrorMessage } from '@/utils/errorUtils';

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

function TabPanel(props: TabPanelProps) {
  const { children, value, index, ...other } = props;
  return (
    <div role="tabpanel" hidden={value !== index} {...other}>
      {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
    </div>
  );
}

export default function ApprovalPage() {
  const { t } = useTranslation();
  const { user } = useAuthStore();
  const [currentTab, setCurrentTab] = useState(0);
  const tenantId = user?.tenantId ?? '';
  const userId = user?.userId ?? 0;

  // ==================== State ====================
  const [pendingApprovals, setPendingApprovals] = useState<ApprovalInstance[]>([]);
  const [templates, setTemplates] = useState<ApprovalLineTemplate[]>([]);
  const [delegations, setDelegations] = useState<ApprovalDelegation[]>([]);
  const [statistics, setStatistics] = useState<ApprovalStatistics | null>(null);

  const [selectedInstance, setSelectedInstance] = useState<ApprovalInstance | null>(null);
  const [detailDialogOpen, setDetailDialogOpen] = useState(false);
  const [actionDialogOpen, setActionDialogOpen] = useState(false);
  const [actionType, setActionType] = useState<'approve' | 'reject'>('approve');
  const [actionComment, setActionComment] = useState('');

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // ==================== Effects ====================

  useEffect(() => {
    loadData();
    // eslint-disable-next-line react-hooks/exhaustive-deps -- reload when tab changes
  }, [currentTab]);

  // ==================== Data Loading ====================

  const loadData = async () => {
    try {
      setLoading(true);
      setError(null);

      if (currentTab === 0) {
        // Load pending approvals
        const data = await approvalService.getPendingApprovals(tenantId, userId);
        setPendingApprovals(data || []);
      } else if (currentTab === 1) {
        // Load templates
        const data = await approvalService.getAllTemplates(tenantId);
        setTemplates(data || []);
      } else if (currentTab === 2) {
        // Load delegations
        const data = await approvalService.getCurrentDelegations(tenantId);
        setDelegations(data || []);
      } else if (currentTab === 3) {
        // Load statistics
        const stats = await approvalService.getStatistics(tenantId);
        setStatistics(stats);
      }
    } catch (err) {
      setError(getErrorMessage(err, t('pages.approval.errors.loadFailed')));
      if (currentTab === 0) {
        setPendingApprovals([]);
      } else if (currentTab === 1) {
        setTemplates([]);
      } else if (currentTab === 2) {
        setDelegations([]);
      }
    } finally {
      setLoading(false);
    }
  };

  // ==================== Handlers ====================

  const handleViewDetail = (instance: ApprovalInstance) => {
    setSelectedInstance(instance);
    setDetailDialogOpen(true);
  };

  const handleApproveClick = (instance: ApprovalInstance) => {
    setSelectedInstance(instance);
    setActionType('approve');
    setActionComment('');
    setActionDialogOpen(true);
  };

  const handleRejectClick = (instance: ApprovalInstance) => {
    setSelectedInstance(instance);
    setActionType('reject');
    setActionComment('');
    setActionDialogOpen(true);
  };

  const handleConfirmAction = async () => {
    if (!selectedInstance) return;

    try {
      const currentStep = selectedInstance.stepInstances?.find(
        s => s.stepOrder === selectedInstance.currentStepOrder
      );
      if (!currentStep) return;

      if (actionType === 'approve') {
        await approvalService.approveStep(
          selectedInstance.instanceId,
          currentStep.stepInstanceId,
          { approverId: userId, comment: actionComment }
        );
      } else {
        await approvalService.rejectStep(
          selectedInstance.instanceId,
          currentStep.stepInstanceId,
          { approverId: userId, reason: actionComment }
        );
      }

      setActionDialogOpen(false);
      loadData();
    } catch (err) {
      setError(getErrorMessage(err, t('pages.approval.errors.processFailed')));
    }
  };

  // ==================== Column Definitions ====================

  const pendingColumns: GridColDef[] = [
    {
      field: 'documentNo',
      headerName: t('pages.approval.fields.documentNo'),
      width: 150
    },
    {
      field: 'documentType',
      headerName: t('pages.approval.fields.documentType'),
      width: 120,
      renderCell: (params: GridRenderCellParams) => (
        <Chip label={getDocumentTypeLabel(params.value)} size="small" />
      )
    },
    { field: 'documentTitle', headerName: t('pages.approval.fields.title'), width: 250 },
    {
      field: 'requesterName',
      headerName: t('pages.approval.fields.requester'),
      width: 100
    },
    {
      field: 'requestDate',
      headerName: t('pages.approval.fields.requestDate'),
      width: 150,
      valueFormatter: (params) => formatDateTime(params.value)
    },
    {
      field: 'approvalStatus',
      headerName: t('common.labels.status'),
      width: 100,
      renderCell: (params: GridRenderCellParams) => (
        <Chip
          label={getApprovalStatusLabel(params.value)}
          color={getApprovalStatusColor(params.value)}
          size="small"
        />
      )
    },
    {
      field: 'actions',
      headerName: t('common.labels.actions'),
      width: 200,
      sortable: false,
      renderCell: (params: GridRenderCellParams) => (
        <Box>
          <IconButton size="small" onClick={() => handleViewDetail(params.row)}>
            <VisibilityIcon fontSize="small" />
          </IconButton>
          <Button
            size="small"
            startIcon={<CheckCircleIcon />}
            onClick={() => handleApproveClick(params.row)}
            sx={{ ml: 1 }}
          >
            {t('pages.approval.actions.approve')}
          </Button>
          <Button
            size="small"
            startIcon={<CancelIcon />}
            onClick={() => handleRejectClick(params.row)}
            color="error"
            sx={{ ml: 1 }}
          >
            {t('pages.approval.actions.reject')}
          </Button>
        </Box>
      )
    }
  ];

  const templateColumns: GridColDef[] = [
    { field: 'templateName', headerName: t('pages.approval.fields.templateName'), width: 200 },
    { field: 'templateCode', headerName: t('pages.approval.fields.templateCode'), width: 150 },
    {
      field: 'documentType',
      headerName: t('pages.approval.fields.documentType'),
      width: 150,
      renderCell: (params: GridRenderCellParams) => (
        <Chip label={getDocumentTypeLabel(params.value)} size="small" />
      )
    },
    {
      field: 'isDefault',
      headerName: t('common.labels.default'),
      width: 80,
      renderCell: (params: GridRenderCellParams) => (
        params.value ? <Chip label={t('common.labels.default')} color="primary" size="small" /> : null
      )
    },
    {
      field: 'steps',
      headerName: t('pages.approval.fields.stepCount'),
      width: 100,
      valueGetter: (params) => params.row.steps?.length || 0
    },
    { field: 'description', headerName: t('common.labels.description'), width: 300 }
  ];

  const delegationColumns: GridColDef[] = [
    { field: 'delegatorName', headerName: t('pages.approval.fields.delegator'), width: 120 },
    { field: 'delegateName', headerName: t('pages.approval.fields.delegate'), width: 120 },
    {
      field: 'delegationType',
      headerName: t('pages.approval.fields.delegationType'),
      width: 100,
      renderCell: (params: GridRenderCellParams) => (
        <Chip
          label={params.value === 'FULL' ? t('pages.approval.status.full') : t('pages.approval.status.partial')}
          color={params.value === 'FULL' ? 'primary' : 'default'}
          size="small"
        />
      )
    },
    { field: 'startDate', headerName: t('common.labels.startDate'), width: 120 },
    { field: 'endDate', headerName: t('common.labels.endDate'), width: 120 },
    { field: 'delegationReason', headerName: t('pages.approval.fields.delegationReason'), width: 250 }
  ];

  // ==================== Render ====================

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        <ApprovalIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
        {t('pages.approval.title')}
      </Typography>

      {error && (
        <Alert severity="error" onClose={() => setError(null)} sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      <Paper sx={{ width: '100%' }}>
        <Tabs value={currentTab} onChange={(e, v) => setCurrentTab(v)}>
          <Tab label={t('pages.approval.tabs.pending')} />
          <Tab label={t('pages.approval.tabs.templates')} />
          <Tab label={t('pages.approval.tabs.delegations')} />
          <Tab label={t('pages.approval.tabs.statistics')} />
        </Tabs>

        {/* Tab 1: Pending Approvals */}
        <TabPanel value={currentTab} index={0}>
          <DataGrid
            rows={pendingApprovals}
            columns={pendingColumns}
            getRowId={(row) => row.instanceId}
            loading={loading}
            autoHeight
            initialState={{
              pagination: { paginationModel: { pageSize: 10 } }
            }}
            pageSizeOptions={[10, 20, 50]}
          />
        </TabPanel>

        {/* Tab 2: Templates */}
        <TabPanel value={currentTab} index={1}>
          <DataGrid
            rows={templates}
            columns={templateColumns}
            getRowId={(row) => row.templateId}
            loading={loading}
            autoHeight
            initialState={{
              pagination: { paginationModel: { pageSize: 10 } }
            }}
            pageSizeOptions={[10, 20]}
          />
        </TabPanel>

        {/* Tab 3: Delegations */}
        <TabPanel value={currentTab} index={2}>
          <DataGrid
            rows={delegations}
            columns={delegationColumns}
            getRowId={(row) => row.delegationId}
            loading={loading}
            autoHeight
            initialState={{
              pagination: { paginationModel: { pageSize: 10 } }
            }}
            pageSizeOptions={[10, 20]}
          />
        </TabPanel>

        {/* Tab 4: Statistics */}
        <TabPanel value={currentTab} index={3}>
          {statistics && (
            <Grid container spacing={3}>
              <Grid item xs={12} md={3}>
                <Card>
                  <CardContent>
                    <Typography color="text.secondary" gutterBottom>
                      {t('pages.approval.status.pending')}
                    </Typography>
                    <Typography variant="h4">{statistics.pending}</Typography>
                  </CardContent>
                </Card>
              </Grid>
              <Grid item xs={12} md={3}>
                <Card>
                  <CardContent>
                    <Typography color="text.secondary" gutterBottom>
                      {t('pages.approval.status.inProgress')}
                    </Typography>
                    <Typography variant="h4" color="primary">
                      {statistics.inProgress}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
              <Grid item xs={12} md={3}>
                <Card>
                  <CardContent>
                    <Typography color="text.secondary" gutterBottom>
                      {t('pages.approval.status.approved')}
                    </Typography>
                    <Typography variant="h4" color="success.main">
                      {statistics.approved}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
              <Grid item xs={12} md={3}>
                <Card>
                  <CardContent>
                    <Typography color="text.secondary" gutterBottom>
                      {t('pages.approval.status.rejected')}
                    </Typography>
                    <Typography variant="h4" color="error.main">
                      {statistics.rejected}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
              <Grid item xs={12}>
                <Card>
                  <CardContent>
                    <Typography variant="h6" gutterBottom>
                      {t('pages.approval.statistics.approvalRate')}
                    </Typography>
                    <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                      <Box sx={{ width: '100%', mr: 1 }}>
                        <LinearProgress
                          variant="determinate"
                          value={statistics.approvalRate}
                          sx={{ height: 10, borderRadius: 5 }}
                        />
                      </Box>
                      <Box sx={{ minWidth: 35 }}>
                        <Typography variant="body2" color="text.secondary">
                          {statistics.approvalRate.toFixed(1)}%
                        </Typography>
                      </Box>
                    </Box>
                    <Typography variant="body2" color="text.secondary">
                      {t('pages.approval.statistics.totalApprovedOf', { total: statistics.total, approved: statistics.approved })}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
            </Grid>
          )}
        </TabPanel>
      </Paper>

      {/* Detail Dialog */}
      <Dialog
        open={detailDialogOpen}
        onClose={() => setDetailDialogOpen(false)}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle>{t('pages.approval.detail.title')}</DialogTitle>
        <DialogContent>
          {selectedInstance && (
            <Box>
              <Grid container spacing={2} sx={{ mb: 3 }}>
                <Grid item xs={6}>
                  <Typography variant="body2" color="text.secondary">
                    {t('pages.approval.fields.documentNo')}
                  </Typography>
                  <Typography variant="body1">{selectedInstance.documentNo}</Typography>
                </Grid>
                <Grid item xs={6}>
                  <Typography variant="body2" color="text.secondary">
                    {t('pages.approval.fields.documentType')}
                  </Typography>
                  <Typography variant="body1">
                    {getDocumentTypeLabel(selectedInstance.documentType)}
                  </Typography>
                </Grid>
                <Grid item xs={12}>
                  <Typography variant="body2" color="text.secondary">
                    {t('pages.approval.fields.title')}
                  </Typography>
                  <Typography variant="body1">{selectedInstance.documentTitle}</Typography>
                </Grid>
                <Grid item xs={6}>
                  <Typography variant="body2" color="text.secondary">
                    {t('pages.approval.fields.requester')}
                  </Typography>
                  <Typography variant="body1">
                    {selectedInstance.requesterName} ({selectedInstance.requesterDepartment})
                  </Typography>
                </Grid>
                <Grid item xs={6}>
                  <Typography variant="body2" color="text.secondary">
                    {t('pages.approval.fields.requestDate')}
                  </Typography>
                  <Typography variant="body1">
                    {formatDateTime(selectedInstance.requestDate)}
                  </Typography>
                </Grid>
              </Grid>

              <Typography variant="h6" gutterBottom sx={{ mt: 3 }}>
                {t('pages.approval.detail.approvalProgress')}
              </Typography>
              <Stepper activeStep={selectedInstance.currentStepOrder || 0} alternativeLabel>
                {selectedInstance.stepInstances?.map((step) => (
                  <Step key={step.stepInstanceId} completed={step.stepStatus === 'APPROVED'}>
                    <StepLabel
                      error={step.stepStatus === 'REJECTED'}
                      optional={
                        <Box sx={{ textAlign: 'center' }}>
                          <Typography variant="caption">
                            {step.approverName}
                          </Typography>
                          <br />
                          <Chip
                            label={getApprovalStatusLabel(step.stepStatus)}
                            color={getApprovalStatusColor(step.stepStatus)}
                            size="small"
                          />
                        </Box>
                      }
                    >
                      {step.stepName}
                    </StepLabel>
                  </Step>
                ))}
              </Stepper>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDetailDialogOpen(false)}>{t('common.buttons.close')}</Button>
        </DialogActions>
      </Dialog>

      {/* Action Dialog */}
      <Dialog open={actionDialogOpen} onClose={() => setActionDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>
          {actionType === 'approve' ? t('pages.approval.detail.approveTitle') : t('pages.approval.detail.rejectTitle')}
        </DialogTitle>
        <DialogContent>
          <TextField
            fullWidth
            multiline
            rows={4}
            label={actionType === 'approve' ? t('pages.approval.fields.approvalComment') : t('pages.approval.fields.rejectionReason')}
            value={actionComment}
            onChange={(e) => setActionComment(e.target.value)}
            sx={{ mt: 2 }}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setActionDialogOpen(false)}>{t('common.buttons.cancel')}</Button>
          <Button
            variant="contained"
            color={actionType === 'approve' ? 'primary' : 'error'}
            onClick={handleConfirmAction}
          >
            {actionType === 'approve' ? t('pages.approval.actions.approve') : t('pages.approval.actions.reject')}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
