import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import {
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  FormControlLabel,
  Grid,
  IconButton,
  InputLabel,
  MenuItem,
  Select,
  Switch,
  TextField,
  Typography,
  Alert,
  Tabs,
  Tab,
} from '@mui/material';
import {
  DataGrid,
  GridColDef,
  GridRenderCellParams,
  GridToolbar,
} from '@mui/x-data-grid';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  CheckCircle as ApproveIcon,
  Cancel as RejectIcon,
  Send as SubmitIcon,
  Archive as ObsoleteIcon,
} from '@mui/icons-material';
import sopService, {
  SOP,
  SOPStep,
  SOPCreateRequest,
  SOPStepCreateRequest,
} from '../../services/sopService';
import { getErrorMessage } from '@/utils/errorUtils';

const SOPsPage: React.FC = () => {
  const { t } = useTranslation();
  const [sops, setSOPs] = useState<SOP[]>([]);
  const [selectedSOP, setSelectedSOP] = useState<SOP | null>(null);
  const [steps, setSteps] = useState<SOPStep[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Dialog states
  const [sopDialogOpen, setSopDialogOpen] = useState(false);
  const [stepDialogOpen, setStepDialogOpen] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [approvalDialogOpen, setApprovalDialogOpen] = useState(false);
  const [editingSOPId, setEditingSOPId] = useState<number | null>(null);
  const [editingStepId, setEditingStepId] = useState<number | null>(null);
  const [deletingId, setDeletingId] = useState<number | null>(null);
  const [deleteType, setDeleteType] = useState<'sop' | 'step'>('sop');

  // Tab state
  const [tabValue, setTabValue] = useState(0);

  // Form data
  const [sopForm, setSOPForm] = useState<SOPCreateRequest>({
    sopCode: '',
    sopName: '',
    description: '',
    sopType: 'PRODUCTION',
    category: '',
    targetProcess: '',
    version: '1.0',
    requiredRole: '',
    restricted: false,
    displayOrder: 0,
  });

  const [stepForm, setStepForm] = useState<SOPStepCreateRequest>({
    stepTitle: '',
    stepDescription: '',
    stepType: 'EXECUTION',
    estimatedDuration: 0,
    detailedInstruction: '',
    cautionNotes: '',
    qualityPoints: '',
    checklistItems: '',
    isCritical: false,
    isMandatory: true,
  });

  useEffect(() => {
    loadSOPs();
  }, []);

  useEffect(() => {
    if (selectedSOP) {
      loadSteps(selectedSOP.sopId);
    }
  }, [selectedSOP]);

  const loadSOPs = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await sopService.getSOPs();
      setSOPs(data || []);
    } catch (err) {
      setError(getErrorMessage(err, t('pages.sops.messages.loadFailed')));
      setSOPs([]);
    } finally {
      setLoading(false);
    }
  };

  const loadSteps = async (sopId: number) => {
    try {
      const sop = await sopService.getSOPById(sopId);
      setSteps(sop?.steps || []);
    } catch (err) {
      setError(getErrorMessage(err, t('pages.sops.messages.stepsLoadFailed')));
      setSteps([]);
    }
  };

  // SOP CRUD handlers
  const handleOpenSOPDialog = (sop?: SOP) => {
    if (sop) {
      setEditingSOPId(sop.sopId);
      setSOPForm({
        sopCode: sop.sopCode,
        sopName: sop.sopName,
        description: sop.description || '',
        sopType: sop.sopType,
        category: sop.category || '',
        targetProcess: sop.targetProcess || '',
        version: sop.version,
        requiredRole: sop.requiredRole || '',
        restricted: sop.restricted,
        displayOrder: sop.displayOrder,
      });
    } else {
      setEditingSOPId(null);
      setSOPForm({
        sopCode: '',
        sopName: '',
        description: '',
        sopType: 'PRODUCTION',
        category: '',
        targetProcess: '',
        version: '1.0',
        requiredRole: '',
        restricted: false,
        displayOrder: 0,
      });
    }
    setSopDialogOpen(true);
  };

  const handleCloseSOPDialog = () => {
    setSopDialogOpen(false);
    setEditingSOPId(null);
  };

  const handleSaveSOPDialog = async () => {
    try {
      if (editingSOPId) {
        await sopService.updateSOP(editingSOPId, sopForm);
      } else {
        await sopService.createSOP(sopForm);
      }
      handleCloseSOPDialog();
      loadSOPs();
    } catch (err) {
      setError(getErrorMessage(err, t('pages.sops.messages.saveFailed')));
    }
  };

  // Step CRUD handlers
  const handleOpenStepDialog = (step?: SOPStep) => {
    if (!selectedSOP) {
      setError(t('pages.sops.messages.selectSOPFirst'));
      return;
    }

    if (selectedSOP.approvalStatus !== 'DRAFT' && selectedSOP.approvalStatus !== 'REJECTED') {
      setError(t('pages.sops.messages.onlyDraftEditable'));
      return;
    }

    if (step) {
      setEditingStepId(step.sopStepId);
      setStepForm({
        stepTitle: step.stepTitle,
        stepDescription: step.stepDescription || '',
        stepType: step.stepType || 'EXECUTION',
        estimatedDuration: step.estimatedDuration || 0,
        detailedInstruction: step.detailedInstruction || '',
        cautionNotes: step.cautionNotes || '',
        qualityPoints: step.qualityPoints || '',
        checklistItems: step.checklistItems || '',
        prerequisiteStepId: step.prerequisiteStepId,
        isCritical: step.isCritical,
        isMandatory: step.isMandatory,
      });
    } else {
      setEditingStepId(null);
      setStepForm({
        stepTitle: '',
        stepDescription: '',
        stepType: 'EXECUTION',
        estimatedDuration: 0,
        detailedInstruction: '',
        cautionNotes: '',
        qualityPoints: '',
        checklistItems: '',
        isCritical: false,
        isMandatory: true,
      });
    }
    setStepDialogOpen(true);
  };

  const handleCloseStepDialog = () => {
    setStepDialogOpen(false);
    setEditingStepId(null);
  };

  const handleSaveStepDialog = async () => {
    if (!selectedSOP) return;

    try {
      if (editingStepId) {
        await sopService.updateStep(editingStepId, stepForm);
      } else {
        await sopService.addStep(selectedSOP.sopId, stepForm);
      }
      handleCloseStepDialog();
      loadSteps(selectedSOP.sopId);
    } catch (err) {
      setError(getErrorMessage(err, t('pages.sops.messages.stepSaveFailed')));
    }
  };

  // Delete handlers
  const handleOpenDeleteDialog = (id: number, type: 'sop' | 'step') => {
    setDeletingId(id);
    setDeleteType(type);
    setDeleteDialogOpen(true);
  };

  const handleCloseDeleteDialog = () => {
    setDeleteDialogOpen(false);
    setDeletingId(null);
  };

  const handleConfirmDelete = async () => {
    if (!deletingId) return;

    try {
      if (deleteType === 'sop') {
        await sopService.deleteSOP(deletingId);
        loadSOPs();
        setSelectedSOP(null);
        setSteps([]);
      } else {
        await sopService.deleteStep(deletingId);
        if (selectedSOP) {
          loadSteps(selectedSOP.sopId);
        }
      }
      handleCloseDeleteDialog();
    } catch (err) {
      setError(getErrorMessage(err, t('pages.sops.messages.deleteFailed')));
    }
  };

  // Approval workflow handlers
  const handleSubmitForApproval = async (sopId: number) => {
    try {
      await sopService.submitForApproval(sopId);
      loadSOPs();
      if (selectedSOP?.sopId === sopId) {
        const updated = await sopService.getSOPById(sopId);
        setSelectedSOP(updated);
      }
    } catch (err) {
      setError(getErrorMessage(err, t('pages.sops.messages.submitFailed')));
    }
  };

  const handleApproveSOP = async (sopId: number, approverId: number) => {
    try {
      await sopService.approveSOP(sopId, approverId);
      loadSOPs();
      if (selectedSOP?.sopId === sopId) {
        const updated = await sopService.getSOPById(sopId);
        setSelectedSOP(updated);
      }
    } catch (err) {
      setError(getErrorMessage(err, t('pages.sops.messages.approveFailed')));
    }
  };

  const handleRejectSOP = async (sopId: number) => {
    try {
      await sopService.rejectSOP(sopId);
      loadSOPs();
      if (selectedSOP?.sopId === sopId) {
        const updated = await sopService.getSOPById(sopId);
        setSelectedSOP(updated);
      }
    } catch (err) {
      setError(getErrorMessage(err, t('pages.sops.messages.rejectFailed')));
    }
  };

  const handleMarkObsolete = async (sopId: number) => {
    try {
      await sopService.markObsolete(sopId);
      loadSOPs();
      if (selectedSOP?.sopId === sopId) {
        const updated = await sopService.getSOPById(sopId);
        setSelectedSOP(updated);
      }
    } catch (err) {
      setError(getErrorMessage(err, t('pages.sops.messages.obsoleteFailed')));
    }
  };

  // SOP columns
  const sopColumns: GridColDef[] = [
    { field: 'sopCode', headerName: t('pages.sops.fields.sopCode'), width: 150 },
    { field: 'sopName', headerName: t('pages.sops.fields.sopName'), width: 250 },
    {
      field: 'sopType',
      headerName: t('common.labels.type'),
      width: 100,
      renderCell: (params: GridRenderCellParams) => (
        <Chip label={sopService.getTypeLabel(params.value)} size="small" />
      ),
    },
    { field: 'category', headerName: t('common.labels.category'), width: 120 },
    { field: 'targetProcess', headerName: t('pages.sops.fields.targetProcess'), width: 150 },
    { field: 'version', headerName: t('pages.sops.fields.version'), width: 80 },
    {
      field: 'approvalStatus',
      headerName: t('pages.sops.fields.approvalStatus'),
      width: 120,
      renderCell: (params: GridRenderCellParams) => (
        <Chip
          label={sopService.getStatusLabel(params.value)}
          size="small"
          sx={{ bgcolor: sopService.getStatusColor(params.value), color: 'white' }}
        />
      ),
    },
    { field: 'effectiveDate', headerName: t('pages.sops.fields.effectiveDate'), width: 120 },
    {
      field: 'isActive',
      headerName: t('pages.sops.fields.isActive'),
      width: 80,
      renderCell: (params: GridRenderCellParams) => (
        <Chip
          label={params.value ? t('common.status.active') : t('common.status.inactive')}
          size="small"
          color={params.value ? 'success' : 'default'}
        />
      ),
    },
    {
      field: 'actions',
      headerName: t('common.labels.actions'),
      width: 250,
      sortable: false,
      renderCell: (params: GridRenderCellParams) => (
        <Box>
          <IconButton size="small" onClick={() => handleOpenSOPDialog(params.row)}>
            <EditIcon fontSize="small" />
          </IconButton>
          <IconButton
            size="small"
            onClick={() => handleOpenDeleteDialog(params.row.sopId, 'sop')}
          >
            <DeleteIcon fontSize="small" />
          </IconButton>
          {params.row.approvalStatus === 'DRAFT' && (
            <IconButton
              size="small"
              onClick={() => handleSubmitForApproval(params.row.sopId)}
              title={t('pages.sops.actions.submitForApproval')}
            >
              <SubmitIcon fontSize="small" />
            </IconButton>
          )}
          {params.row.approvalStatus === 'PENDING' && (
            <>
              <IconButton
                size="small"
                onClick={() => handleApproveSOP(params.row.sopId, 1)}
                title={t('pages.sops.actions.approve')}
              >
                <ApproveIcon fontSize="small" color="success" />
              </IconButton>
              <IconButton
                size="small"
                onClick={() => handleRejectSOP(params.row.sopId)}
                title={t('pages.sops.actions.reject')}
              >
                <RejectIcon fontSize="small" color="error" />
              </IconButton>
            </>
          )}
          {params.row.approvalStatus === 'APPROVED' && (
            <IconButton
              size="small"
              onClick={() => handleMarkObsolete(params.row.sopId)}
              title={t('pages.sops.actions.obsolete')}
            >
              <ObsoleteIcon fontSize="small" />
            </IconButton>
          )}
        </Box>
      ),
    },
  ];

  // Step columns
  const stepColumns: GridColDef[] = [
    { field: 'stepNumber', headerName: t('pages.sops.stepFields.stepNumber'), width: 80 },
    { field: 'stepTitle', headerName: t('pages.sops.stepFields.stepTitle'), width: 250 },
    {
      field: 'stepType',
      headerName: t('common.labels.type'),
      width: 100,
      renderCell: (params: GridRenderCellParams) => (
        <Chip label={sopService.getTypeLabel(params.value || '')} size="small" />
      ),
    },
    { field: 'estimatedDuration', headerName: t('pages.sops.stepFields.estimatedDuration'), width: 120 },
    {
      field: 'isCritical',
      headerName: t('pages.sops.stepFields.critical'),
      width: 80,
      renderCell: (params: GridRenderCellParams) =>
        params.value ? <Chip label="⚠️" size="small" color="warning" /> : null,
    },
    {
      field: 'isMandatory',
      headerName: t('pages.sops.stepFields.mandatory'),
      width: 80,
      renderCell: (params: GridRenderCellParams) =>
        params.value ? <Chip label="✓" size="small" color="primary" /> : null,
    },
    {
      field: 'actions',
      headerName: t('common.labels.actions'),
      width: 150,
      sortable: false,
      renderCell: (params: GridRenderCellParams) => (
        <Box>
          <IconButton size="small" onClick={() => handleOpenStepDialog(params.row)}>
            <EditIcon fontSize="small" />
          </IconButton>
          <IconButton
            size="small"
            onClick={() => handleOpenDeleteDialog(params.row.sopStepId, 'step')}
          >
            <DeleteIcon fontSize="small" />
          </IconButton>
        </Box>
      ),
    },
  ];

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        {t('pages.sops.title')}
      </Typography>

      {error && (
        <Alert severity="error" onClose={() => setError(null)} sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      <Grid container spacing={3}>
        {/* SOP List */}
        <Grid item xs={12} md={selectedSOP ? 6 : 12}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
                <Typography variant="h6">{t('pages.sops.sopList')}</Typography>
                <Button
                  variant="contained"
                  startIcon={<AddIcon />}
                  onClick={() => handleOpenSOPDialog()}
                >
                  {t('pages.sops.actions.createSOP')}
                </Button>
              </Box>

              <DataGrid
                rows={sops}
                columns={sopColumns}
                getRowId={(row) => row.sopId}
                loading={loading}
                autoHeight
                pageSizeOptions={[10, 25, 50]}
                initialState={{
                  pagination: { paginationModel: { pageSize: 10 } },
                }}
                components={{ Toolbar: GridToolbar }}
                onRowClick={(params) => setSelectedSOP(params.row)}
                sx={{
                  '& .MuiDataGrid-row': {
                    cursor: 'pointer',
                  },
                  '& .MuiDataGrid-row.Mui-selected': {
                    bgcolor: 'action.selected',
                  },
                }}
              />
            </CardContent>
          </Card>
        </Grid>

        {/* SOP Detail Panel */}
        {selectedSOP && (
          <Grid item xs={12} md={6}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  {t('pages.sops.detail.title')}: {selectedSOP.sopName}
                </Typography>

                <Tabs value={tabValue} onChange={(e, v) => setTabValue(v)} sx={{ mb: 2 }}>
                  <Tab label={t('pages.sops.detail.basicInfo')} />
                  <Tab label={t('pages.sops.detail.sopSteps')} />
                </Tabs>

                {tabValue === 0 && (
                  <Box>
                    <Grid container spacing={2}>
                      <Grid item xs={6}>
                        <Typography variant="body2" color="text.secondary">
                          {t('pages.sops.fields.sopCode')}
                        </Typography>
                        <Typography variant="body1">{selectedSOP.sopCode}</Typography>
                      </Grid>
                      <Grid item xs={6}>
                        <Typography variant="body2" color="text.secondary">
                          {t('pages.sops.fields.version')}
                        </Typography>
                        <Typography variant="body1">{selectedSOP.version}</Typography>
                      </Grid>
                      <Grid item xs={6}>
                        <Typography variant="body2" color="text.secondary">
                          {t('common.labels.type')}
                        </Typography>
                        <Typography variant="body1">
                          {sopService.getTypeLabel(selectedSOP.sopType)}
                        </Typography>
                      </Grid>
                      <Grid item xs={6}>
                        <Typography variant="body2" color="text.secondary">
                          {t('pages.sops.fields.approvalStatus')}
                        </Typography>
                        <Chip
                          label={sopService.getStatusLabel(selectedSOP.approvalStatus)}
                          size="small"
                          sx={{
                            bgcolor: sopService.getStatusColor(selectedSOP.approvalStatus),
                            color: 'white',
                          }}
                        />
                      </Grid>
                      <Grid item xs={12}>
                        <Typography variant="body2" color="text.secondary">
                          {t('common.labels.description')}
                        </Typography>
                        <Typography variant="body1">{selectedSOP.description}</Typography>
                      </Grid>
                      <Grid item xs={6}>
                        <Typography variant="body2" color="text.secondary">
                          {t('pages.sops.fields.effectiveDate')}
                        </Typography>
                        <Typography variant="body1">{selectedSOP.effectiveDate}</Typography>
                      </Grid>
                      <Grid item xs={6}>
                        <Typography variant="body2" color="text.secondary">
                          {t('pages.sops.fields.nextReviewDate')}
                        </Typography>
                        <Typography variant="body1">{selectedSOP.nextReviewDate}</Typography>
                      </Grid>
                    </Grid>
                  </Box>
                )}

                {tabValue === 1 && (
                  <Box>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
                      <Typography variant="subtitle1">{t('pages.sops.detail.sopStepsCount', { count: steps.length })}</Typography>
                      <Button
                        variant="outlined"
                        size="small"
                        startIcon={<AddIcon />}
                        onClick={() => handleOpenStepDialog()}
                        disabled={
                          selectedSOP.approvalStatus !== 'DRAFT' &&
                          selectedSOP.approvalStatus !== 'REJECTED'
                        }
                      >
                        {t('pages.sops.actions.addStep')}
                      </Button>
                    </Box>

                    <DataGrid
                      rows={steps}
                      columns={stepColumns}
                      getRowId={(row) => row.sopStepId}
                      autoHeight
                      pageSizeOptions={[5, 10, 25]}
                      initialState={{
                        pagination: { paginationModel: { pageSize: 5 } },
                      }}
                      hideFooter={steps.length <= 5}
                    />
                  </Box>
                )}
              </CardContent>
            </Card>
          </Grid>
        )}
      </Grid>

      {/* SOP Dialog */}
      <Dialog open={sopDialogOpen} onClose={handleCloseSOPDialog} maxWidth="md" fullWidth>
        <DialogTitle>{editingSOPId ? t('pages.sops.dialogs.editSOP') : t('pages.sops.dialogs.createSOP')}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={6}>
              <TextField
                fullWidth
                label={t('pages.sops.fields.sopCode')}
                value={sopForm.sopCode}
                onChange={(e) => setSOPForm({ ...sopForm, sopCode: e.target.value })}
                required
              />
            </Grid>
            <Grid item xs={6}>
              <TextField
                fullWidth
                label={t('pages.sops.fields.version')}
                value={sopForm.version}
                onChange={(e) => setSOPForm({ ...sopForm, version: e.target.value })}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label={t('pages.sops.fields.sopName')}
                value={sopForm.sopName}
                onChange={(e) => setSOPForm({ ...sopForm, sopName: e.target.value })}
                required
              />
            </Grid>
            <Grid item xs={6}>
              <FormControl fullWidth>
                <InputLabel>{t('common.labels.type')}</InputLabel>
                <Select
                  value={sopForm.sopType}
                  label={t('common.labels.type')}
                  onChange={(e) => setSOPForm({ ...sopForm, sopType: e.target.value })}
                >
                  <MenuItem value="PRODUCTION">{t('pages.sops.sopTypes.production')}</MenuItem>
                  <MenuItem value="WAREHOUSE">{t('pages.sops.sopTypes.warehouse')}</MenuItem>
                  <MenuItem value="QUALITY">{t('pages.sops.sopTypes.quality')}</MenuItem>
                  <MenuItem value="FACILITY">{t('pages.sops.sopTypes.facility')}</MenuItem>
                  <MenuItem value="SAFETY">{t('pages.sops.sopTypes.safety')}</MenuItem>
                  <MenuItem value="MAINTENANCE">{t('pages.sops.sopTypes.maintenance')}</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={6}>
              <TextField
                fullWidth
                label={t('common.labels.category')}
                value={sopForm.category}
                onChange={(e) => setSOPForm({ ...sopForm, category: e.target.value })}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label={t('pages.sops.fields.targetProcess')}
                value={sopForm.targetProcess}
                onChange={(e) => setSOPForm({ ...sopForm, targetProcess: e.target.value })}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                multiline
                rows={3}
                label={t('common.labels.description')}
                value={sopForm.description}
                onChange={(e) => setSOPForm({ ...sopForm, description: e.target.value })}
              />
            </Grid>
            <Grid item xs={6}>
              <TextField
                fullWidth
                label={t('pages.sops.fields.requiredRole')}
                value={sopForm.requiredRole}
                onChange={(e) => setSOPForm({ ...sopForm, requiredRole: e.target.value })}
              />
            </Grid>
            <Grid item xs={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={sopForm.restricted}
                    onChange={(e) => setSOPForm({ ...sopForm, restricted: e.target.checked })}
                  />
                }
                label={t('pages.sops.fields.restrictedSOP')}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseSOPDialog}>{t('common.buttons.cancel')}</Button>
          <Button onClick={handleSaveSOPDialog} variant="contained">
            {t('common.buttons.save')}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Step Dialog */}
      <Dialog open={stepDialogOpen} onClose={handleCloseStepDialog} maxWidth="md" fullWidth>
        <DialogTitle>{editingStepId ? t('pages.sops.dialogs.editStep') : t('pages.sops.dialogs.addStep')}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label={t('pages.sops.stepFields.stepTitle')}
                value={stepForm.stepTitle}
                onChange={(e) => setStepForm({ ...stepForm, stepTitle: e.target.value })}
                required
              />
            </Grid>
            <Grid item xs={6}>
              <FormControl fullWidth>
                <InputLabel>{t('pages.sops.stepFields.stepType')}</InputLabel>
                <Select
                  value={stepForm.stepType}
                  label={t('pages.sops.stepFields.stepType')}
                  onChange={(e) => setStepForm({ ...stepForm, stepType: e.target.value })}
                >
                  <MenuItem value="PREPARATION">{t('pages.sops.stepTypes.preparation')}</MenuItem>
                  <MenuItem value="EXECUTION">{t('pages.sops.stepTypes.execution')}</MenuItem>
                  <MenuItem value="INSPECTION">{t('pages.sops.stepTypes.inspection')}</MenuItem>
                  <MenuItem value="DOCUMENTATION">{t('pages.sops.stepTypes.documentation')}</MenuItem>
                  <MenuItem value="SAFETY">{t('pages.sops.stepTypes.safety')}</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={6}>
              <TextField
                fullWidth
                type="number"
                label={t('pages.sops.stepFields.estimatedDuration')}
                value={stepForm.estimatedDuration}
                onChange={(e) =>
                  setStepForm({ ...stepForm, estimatedDuration: Number(e.target.value) })
                }
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                multiline
                rows={2}
                label={t('pages.sops.stepFields.stepDescription')}
                value={stepForm.stepDescription}
                onChange={(e) => setStepForm({ ...stepForm, stepDescription: e.target.value })}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                multiline
                rows={3}
                label={t('pages.sops.stepFields.detailedInstruction')}
                value={stepForm.detailedInstruction}
                onChange={(e) =>
                  setStepForm({ ...stepForm, detailedInstruction: e.target.value })
                }
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                multiline
                rows={2}
                label={t('pages.sops.stepFields.cautionNotes')}
                value={stepForm.cautionNotes}
                onChange={(e) => setStepForm({ ...stepForm, cautionNotes: e.target.value })}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                multiline
                rows={2}
                label={t('pages.sops.stepFields.qualityPoints')}
                value={stepForm.qualityPoints}
                onChange={(e) => setStepForm({ ...stepForm, qualityPoints: e.target.value })}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                multiline
                rows={2}
                label={t('pages.sops.stepFields.checklistItems')}
                value={stepForm.checklistItems}
                onChange={(e) => setStepForm({ ...stepForm, checklistItems: e.target.value })}
                placeholder='[{"item": "항목1", "required": true}, {"item": "항목2", "required": false}]'
              />
            </Grid>
            <Grid item xs={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={stepForm.isCritical}
                    onChange={(e) => setStepForm({ ...stepForm, isCritical: e.target.checked })}
                  />
                }
                label={t('pages.sops.stepFields.criticalStep')}
              />
            </Grid>
            <Grid item xs={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={stepForm.isMandatory}
                    onChange={(e) => setStepForm({ ...stepForm, isMandatory: e.target.checked })}
                  />
                }
                label={t('pages.sops.stepFields.mandatoryStep')}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseStepDialog}>{t('common.buttons.cancel')}</Button>
          <Button onClick={handleSaveStepDialog} variant="contained">
            {t('common.buttons.save')}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={deleteDialogOpen} onClose={handleCloseDeleteDialog}>
        <DialogTitle>{t('pages.sops.dialogs.deleteConfirmTitle')}</DialogTitle>
        <DialogContent>
          <Typography>
            {deleteType === 'sop' ? t('pages.sops.dialogs.deleteSOPConfirm') : t('pages.sops.dialogs.deleteStepConfirm')}
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
            {t('pages.sops.dialogs.deleteWarning')}
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDeleteDialog}>{t('common.buttons.cancel')}</Button>
          <Button onClick={handleConfirmDelete} color="error" variant="contained">
            {t('common.buttons.delete')}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default SOPsPage;
