import React, { useState, useEffect } from 'react';
import {
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  TextField,
  Paper,
  Typography,
  IconButton,
  Chip,
  Alert,
  Snackbar,
  Grid,
  FormControlLabel,
  Switch,
  Divider,
} from '@mui/material';
import {
  DataGrid,
  GridColDef,
  GridActionsCellItem,
  GridRowParams,
  GridRowSelectionModel,
} from '@mui/x-data-grid';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Refresh as RefreshIcon,
} from '@mui/icons-material';
import { useTranslation } from 'react-i18next';
import commonCodeService, {
  CommonCodeGroup,
  CommonCodeDetail,
  CommonCodeGroupCreateRequest,
  CommonCodeDetailCreateRequest,
} from '../../services/commonCodeService';

const CommonCodesPage: React.FC = () => {
  const { t } = useTranslation();

  // State for code groups
  const [codeGroups, setCodeGroups] = useState<CommonCodeGroup[]>([]);
  const [selectedGroupId, setSelectedGroupId] = useState<number | null>(null);
  const [loadingGroups, setLoadingGroups] = useState(true);

  // State for code details
  const [codeDetails, setCodeDetails] = useState<CommonCodeDetail[]>([]);
  const [loadingDetails, setLoadingDetails] = useState(false);

  // Dialogs
  const [openGroupDialog, setOpenGroupDialog] = useState(false);
  const [openDetailDialog, setOpenDetailDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState<{ type: 'group' | 'detail'; id: number } | null>(null);

  // Form data
  const [selectedGroup, setSelectedGroup] = useState<CommonCodeGroup | null>(null);
  const [selectedDetail, setSelectedDetail] = useState<CommonCodeDetail | null>(null);
  const [groupFormData, setGroupFormData] = useState<CommonCodeGroupCreateRequest>({
    codeGroup: '',
    codeGroupName: '',
    description: '',
    isSystem: false,
    displayOrder: 0,
    isActive: true,
  });
  const [detailFormData, setDetailFormData] = useState<CommonCodeDetailCreateRequest>({
    code: '',
    codeName: '',
    description: '',
    displayOrder: 0,
    isDefault: false,
    isActive: true,
    colorCode: '',
    iconName: '',
    value1: '',
    value2: '',
    value3: '',
  });

  // Snackbar
  const [snackbar, setSnackbar] = useState<{ open: boolean; message: string; severity: 'success' | 'error' }>({
    open: false,
    message: '',
    severity: 'success',
  });

  useEffect(() => {
    loadCodeGroups();
    // eslint-disable-next-line react-hooks/exhaustive-deps -- load once on mount
  }, []);

  useEffect(() => {
    if (selectedGroupId) {
      loadCodeDetails(selectedGroupId);
    } else {
      setCodeDetails([]);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps -- reload details when selected group changes
  }, [selectedGroupId]);

  const loadCodeGroups = async () => {
    try {
      setLoadingGroups(true);
      const data = await commonCodeService.getCodeGroups();
      setCodeGroups(data || []);
    } catch (error) {
      showSnackbar(t('pages.commonCodes.messages.groupLoadFailed'), 'error');
      setCodeGroups([]);
    } finally {
      setLoadingGroups(false);
    }
  };

  const loadCodeDetails = async (groupId: number) => {
    try {
      setLoadingDetails(true);
      const data = await commonCodeService.getCodeDetails(groupId);
      setCodeDetails(data || []);
    } catch (error) {
      showSnackbar(t('pages.commonCodes.messages.detailLoadFailed'), 'error');
      setCodeDetails([]);
    } finally {
      setLoadingDetails(false);
    }
  };

  const showSnackbar = (message: string, severity: 'success' | 'error') => {
    setSnackbar({ open: true, message, severity });
  };

  const handleCloseSnackbar = () => {
    setSnackbar({ ...snackbar, open: false });
  };

  // ==================== Code Group Handlers ====================

  const handleOpenGroupDialog = (group?: CommonCodeGroup) => {
    if (group) {
      setSelectedGroup(group);
      setGroupFormData({
        codeGroup: group.codeGroup,
        codeGroupName: group.codeGroupName,
        description: group.description || '',
        isSystem: group.isSystem,
        displayOrder: group.displayOrder,
        isActive: group.isActive,
      });
    } else {
      setSelectedGroup(null);
      setGroupFormData({
        codeGroup: '',
        codeGroupName: '',
        description: '',
        isSystem: false,
        displayOrder: 0,
        isActive: true,
      });
    }
    setOpenGroupDialog(true);
  };

  const handleCloseGroupDialog = () => {
    setOpenGroupDialog(false);
    setSelectedGroup(null);
  };

  const handleSaveGroup = async () => {
    try {
      if (selectedGroup) {
        await commonCodeService.updateCodeGroup(selectedGroup.codeGroupId, groupFormData);
        showSnackbar(t('pages.commonCodes.messages.groupUpdateSuccess'), 'success');
      } else {
        await commonCodeService.createCodeGroup(groupFormData);
        showSnackbar(t('pages.commonCodes.messages.groupCreateSuccess'), 'success');
      }
      handleCloseGroupDialog();
      loadCodeGroups();
    } catch (error) {
      showSnackbar(selectedGroup ? t('pages.commonCodes.messages.groupUpdateFailed') : t('pages.commonCodes.messages.groupCreateFailed'), 'error');
    }
  };

  const handleDeleteGroup = async () => {
    if (!deleteTarget || deleteTarget.type !== 'group') return;

    try {
      await commonCodeService.deleteCodeGroup(deleteTarget.id);
      showSnackbar(t('pages.commonCodes.messages.groupDeleteSuccess'), 'success');
      setOpenDeleteDialog(false);
      setDeleteTarget(null);
      setSelectedGroupId(null);
      loadCodeGroups();
    } catch (error) {
      showSnackbar(t('pages.commonCodes.messages.groupDeleteFailed'), 'error');
    }
  };

  // ==================== Code Detail Handlers ====================

  const handleOpenDetailDialog = (detail?: CommonCodeDetail) => {
    if (!selectedGroupId) {
      showSnackbar(t('pages.commonCodes.messages.selectGroupFirst'), 'error');
      return;
    }

    if (detail) {
      setSelectedDetail(detail);
      setDetailFormData({
        code: detail.code,
        codeName: detail.codeName,
        description: detail.description || '',
        displayOrder: detail.displayOrder,
        isDefault: detail.isDefault,
        isActive: detail.isActive,
        colorCode: detail.colorCode || '',
        iconName: detail.iconName || '',
        value1: detail.value1 || '',
        value2: detail.value2 || '',
        value3: detail.value3 || '',
      });
    } else {
      setSelectedDetail(null);
      setDetailFormData({
        code: '',
        codeName: '',
        description: '',
        displayOrder: 0,
        isDefault: false,
        isActive: true,
        colorCode: '',
        iconName: '',
        value1: '',
        value2: '',
        value3: '',
      });
    }
    setOpenDetailDialog(true);
  };

  const handleCloseDetailDialog = () => {
    setOpenDetailDialog(false);
    setSelectedDetail(null);
  };

  const handleSaveDetail = async () => {
    if (!selectedGroupId) return;

    try {
      if (selectedDetail) {
        await commonCodeService.updateCodeDetail(selectedDetail.codeDetailId, detailFormData);
        showSnackbar(t('pages.commonCodes.messages.detailUpdateSuccess'), 'success');
      } else {
        await commonCodeService.createCodeDetail(selectedGroupId, detailFormData);
        showSnackbar(t('pages.commonCodes.messages.detailCreateSuccess'), 'success');
      }
      handleCloseDetailDialog();
      loadCodeDetails(selectedGroupId);
    } catch (error) {
      showSnackbar(selectedDetail ? t('pages.commonCodes.messages.detailUpdateFailed') : t('pages.commonCodes.messages.detailCreateFailed'), 'error');
    }
  };

  const handleDeleteDetail = async () => {
    if (!deleteTarget || deleteTarget.type !== 'detail') return;

    try {
      await commonCodeService.deleteCodeDetail(deleteTarget.id);
      showSnackbar(t('pages.commonCodes.messages.detailDeleteSuccess'), 'success');
      setOpenDeleteDialog(false);
      setDeleteTarget(null);
      if (selectedGroupId) {
        loadCodeDetails(selectedGroupId);
      }
    } catch (error) {
      showSnackbar(t('pages.commonCodes.messages.detailDeleteFailed'), 'error');
    }
  };

  const handleOpenDeleteDialog = (type: 'group' | 'detail', id: number) => {
    setDeleteTarget({ type, id });
    setOpenDeleteDialog(true);
  };

  const handleCloseDeleteDialog = () => {
    setOpenDeleteDialog(false);
    setDeleteTarget(null);
  };

  // ==================== Data Grid Columns ====================

  const groupColumns: GridColDef[] = [
    { field: 'codeGroup', headerName: t('pages.commonCodes.codeGroup.code'), width: 150 },
    { field: 'codeGroupName', headerName: t('pages.commonCodes.codeGroup.name'), width: 200, flex: 1 },
    {
      field: 'isSystem',
      headerName: t('pages.commonCodes.codeGroup.system'),
      width: 100,
      renderCell: (params) => (
        <Chip
          label={params.value ? t('pages.commonCodes.codeGroup.system') : t('pages.commonCodes.codeGroup.user')}
          color={params.value ? 'warning' : 'default'}
          size="small"
        />
      ),
    },
    {
      field: 'isActive',
      headerName: t('common.status.active'),
      width: 100,
      renderCell: (params) => (
        <Chip
          label={params.value ? t('common.status.active') : t('common.status.inactive')}
          color={params.value ? 'success' : 'default'}
          size="small"
        />
      ),
    },
    { field: 'displayOrder', headerName: t('pages.commonCodes.codeGroup.displayOrder'), width: 80 },
    {
      field: 'actions',
      type: 'actions',
      headerName: t('common.labels.actions'),
      width: 100,
      getActions: (params: GridRowParams) => [
        <GridActionsCellItem
          icon={<EditIcon />}
          label={t('common.buttons.edit')}
          onClick={() => handleOpenGroupDialog(params.row as CommonCodeGroup)}
        />,
        <GridActionsCellItem
          icon={<DeleteIcon />}
          label={t('common.buttons.delete')}
          onClick={() => handleOpenDeleteDialog('group', params.row.codeGroupId)}
          disabled={params.row.isSystem}
        />,
      ],
    },
  ];

  const detailColumns: GridColDef[] = [
    { field: 'code', headerName: t('pages.commonCodes.codeDetail.code'), width: 120 },
    { field: 'codeName', headerName: t('pages.commonCodes.codeDetail.name'), width: 150, flex: 1 },
    {
      field: 'colorCode',
      headerName: t('pages.commonCodes.codeDetail.color'),
      width: 100,
      renderCell: (params) =>
        params.value ? (
          <Box
            sx={{
              width: 40,
              height: 20,
              backgroundColor: params.value,
              border: '1px solid #ccc',
              borderRadius: 1,
            }}
          />
        ) : null,
    },
    {
      field: 'isDefault',
      headerName: t('pages.commonCodes.codeDetail.defaultValue'),
      width: 90,
      renderCell: (params) => (
        params.value ? <Chip label={t('common.labels.default')} color="primary" size="small" /> : null
      ),
    },
    {
      field: 'isActive',
      headerName: t('common.status.active'),
      width: 90,
      renderCell: (params) => (
        <Chip
          label={params.value ? t('common.status.active') : t('common.status.inactive')}
          color={params.value ? 'success' : 'default'}
          size="small"
        />
      ),
    },
    { field: 'displayOrder', headerName: t('pages.commonCodes.codeDetail.displayOrder'), width: 80 },
    {
      field: 'actions',
      type: 'actions',
      headerName: t('common.labels.actions'),
      width: 100,
      getActions: (params: GridRowParams) => [
        <GridActionsCellItem
          icon={<EditIcon />}
          label={t('common.buttons.edit')}
          onClick={() => handleOpenDetailDialog(params.row as CommonCodeDetail)}
        />,
        <GridActionsCellItem
          icon={<DeleteIcon />}
          label={t('common.buttons.delete')}
          onClick={() => handleOpenDeleteDialog('detail', params.row.codeDetailId)}
        />,
      ],
    },
  ];

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        {t('pages.commonCodes.title')}
      </Typography>

      <Grid container spacing={2}>
        {/* Left Panel: Code Groups */}
        <Grid item xs={12} md={5}>
          <Paper sx={{ p: 2, height: 'calc(100vh - 200px)' }}>
            <Box sx={{ mb: 2, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <Typography variant="h6">{t('pages.commonCodes.codeGroup.title')}</Typography>
              <Box>
                <IconButton onClick={loadCodeGroups} size="small">
                  <RefreshIcon />
                </IconButton>
                <Button
                  variant="contained"
                  startIcon={<AddIcon />}
                  onClick={() => handleOpenGroupDialog()}
                  size="small"
                  sx={{ ml: 1 }}
                >
                  {t('pages.commonCodes.codeGroup.add')}
                </Button>
              </Box>
            </Box>

            <DataGrid
              rows={codeGroups}
              columns={groupColumns}
              loading={loadingGroups}
              getRowId={(row) => row.codeGroupId}
              pageSizeOptions={[10, 25, 50]}
              initialState={{
                pagination: { paginationModel: { pageSize: 10 } },
              }}
              onRowSelectionModelChange={(selection: GridRowSelectionModel) => {
                const id = selection[0] as number;
                setSelectedGroupId(id || null);
              }}
              sx={{ height: 'calc(100% - 60px)' }}
            />
          </Paper>
        </Grid>

        {/* Right Panel: Code Details */}
        <Grid item xs={12} md={7}>
          <Paper sx={{ p: 2, height: 'calc(100vh - 200px)' }}>
            <Box sx={{ mb: 2, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <Typography variant="h6">
                {t('pages.commonCodes.codeDetail.title')} {selectedGroupId && `(${codeGroups.find(g => g.codeGroupId === selectedGroupId)?.codeGroupName})`}
              </Typography>
              <Button
                variant="contained"
                startIcon={<AddIcon />}
                onClick={() => handleOpenDetailDialog()}
                disabled={!selectedGroupId}
                size="small"
              >
                {t('pages.commonCodes.codeDetail.add')}
              </Button>
            </Box>

            {selectedGroupId ? (
              <DataGrid
                rows={codeDetails}
                columns={detailColumns}
                loading={loadingDetails}
                getRowId={(row) => row.codeDetailId}
                pageSizeOptions={[10, 25, 50]}
                initialState={{
                  pagination: { paginationModel: { pageSize: 10 } },
                }}
                sx={{ height: 'calc(100% - 60px)' }}
              />
            ) : (
              <Box sx={{ height: 'calc(100% - 60px)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                <Typography color="text.secondary">
                  {t('pages.commonCodes.codeDetail.selectGroup')}
                </Typography>
              </Box>
            )}
          </Paper>
        </Grid>
      </Grid>

      {/* Code Group Dialog */}
      <Dialog open={openGroupDialog} onClose={handleCloseGroupDialog} maxWidth="sm" fullWidth>
        <DialogTitle>{selectedGroup ? t('pages.commonCodes.codeGroup.edit') : t('pages.commonCodes.codeGroup.create')}</DialogTitle>
        <DialogContent>
          <TextField
            fullWidth
            label={t('pages.commonCodes.codeGroup.code')}
            value={groupFormData.codeGroup}
            onChange={(e) => setGroupFormData({ ...groupFormData, codeGroup: e.target.value.toUpperCase() })}
            margin="normal"
            required
            disabled={!!selectedGroup}
            helperText={t('pages.commonCodes.codeGroup.helperText')}
          />
          <TextField
            fullWidth
            label={t('pages.commonCodes.codeGroup.name')}
            value={groupFormData.codeGroupName}
            onChange={(e) => setGroupFormData({ ...groupFormData, codeGroupName: e.target.value })}
            margin="normal"
            required
          />
          <TextField
            fullWidth
            label={t('common.labels.description')}
            value={groupFormData.description}
            onChange={(e) => setGroupFormData({ ...groupFormData, description: e.target.value })}
            margin="normal"
            multiline
            rows={3}
          />
          <TextField
            fullWidth
            type="number"
            label={t('pages.commonCodes.codeDetail.displayOrder')}
            value={groupFormData.displayOrder}
            onChange={(e) => setGroupFormData({ ...groupFormData, displayOrder: parseInt(e.target.value) })}
            margin="normal"
          />
          <FormControlLabel
            control={
              <Switch
                checked={groupFormData.isActive}
                onChange={(e) => setGroupFormData({ ...groupFormData, isActive: e.target.checked })}
              />
            }
            label={t('common.status.active')}
            sx={{ mt: 2 }}
          />
          {!selectedGroup && (
            <FormControlLabel
              control={
                <Switch
                  checked={groupFormData.isSystem}
                  onChange={(e) => setGroupFormData({ ...groupFormData, isSystem: e.target.checked })}
                />
              }
              label={t('pages.commonCodes.codeGroup.systemCode')}
              sx={{ mt: 1 }}
            />
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseGroupDialog}>{t('common.buttons.cancel')}</Button>
          <Button onClick={handleSaveGroup} variant="contained">
            {selectedGroup ? t('common.buttons.edit') : t('common.buttons.add')}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Code Detail Dialog */}
      <Dialog open={openDetailDialog} onClose={handleCloseDetailDialog} maxWidth="sm" fullWidth>
        <DialogTitle>{selectedDetail ? t('pages.commonCodes.codeDetail.edit') : t('pages.commonCodes.codeDetail.create')}</DialogTitle>
        <DialogContent>
          <TextField
            fullWidth
            label={t('pages.commonCodes.codeDetail.code')}
            value={detailFormData.code}
            onChange={(e) => setDetailFormData({ ...detailFormData, code: e.target.value.toUpperCase() })}
            margin="normal"
            required
            disabled={!!selectedDetail}
            helperText={t('pages.commonCodes.codeGroup.helperText')}
          />
          <TextField
            fullWidth
            label={t('pages.commonCodes.codeDetail.name')}
            value={detailFormData.codeName}
            onChange={(e) => setDetailFormData({ ...detailFormData, codeName: e.target.value })}
            margin="normal"
            required
          />
          <TextField
            fullWidth
            label={t('common.labels.description')}
            value={detailFormData.description}
            onChange={(e) => setDetailFormData({ ...detailFormData, description: e.target.value })}
            margin="normal"
            multiline
            rows={2}
          />
          <Grid container spacing={2}>
            <Grid item xs={6}>
              <TextField
                fullWidth
                type="number"
                label={t('pages.commonCodes.codeDetail.displayOrder')}
                value={detailFormData.displayOrder}
                onChange={(e) => setDetailFormData({ ...detailFormData, displayOrder: parseInt(e.target.value) })}
                margin="normal"
              />
            </Grid>
            <Grid item xs={6}>
              <TextField
                fullWidth
                label={t('pages.commonCodes.codeDetail.colorCode')}
                value={detailFormData.colorCode}
                onChange={(e) => setDetailFormData({ ...detailFormData, colorCode: e.target.value })}
                margin="normal"
                placeholder="#4CAF50"
              />
            </Grid>
          </Grid>
          <TextField
            fullWidth
            label={t('pages.commonCodes.codeDetail.iconName')}
            value={detailFormData.iconName}
            onChange={(e) => setDetailFormData({ ...detailFormData, iconName: e.target.value })}
            margin="normal"
            placeholder="CheckCircleIcon"
          />

          <Divider sx={{ my: 2 }} />
          <Typography variant="subtitle2" gutterBottom>{t('pages.commonCodes.codeDetail.extensionFields')}</Typography>

          <TextField
            fullWidth
            label={t('pages.commonCodes.codeDetail.extensionField1')}
            value={detailFormData.value1}
            onChange={(e) => setDetailFormData({ ...detailFormData, value1: e.target.value })}
            margin="normal"
            size="small"
          />
          <TextField
            fullWidth
            label={t('pages.commonCodes.codeDetail.extensionField2')}
            value={detailFormData.value2}
            onChange={(e) => setDetailFormData({ ...detailFormData, value2: e.target.value })}
            margin="normal"
            size="small"
          />
          <TextField
            fullWidth
            label={t('pages.commonCodes.codeDetail.extensionField3')}
            value={detailFormData.value3}
            onChange={(e) => setDetailFormData({ ...detailFormData, value3: e.target.value })}
            margin="normal"
            size="small"
          />

          <FormControlLabel
            control={
              <Switch
                checked={detailFormData.isDefault}
                onChange={(e) => setDetailFormData({ ...detailFormData, isDefault: e.target.checked })}
              />
            }
            label={t('pages.commonCodes.codeDetail.defaultValue')}
            sx={{ mt: 2 }}
          />
          <FormControlLabel
            control={
              <Switch
                checked={detailFormData.isActive}
                onChange={(e) => setDetailFormData({ ...detailFormData, isActive: e.target.checked })}
              />
            }
            label={t('common.status.active')}
            sx={{ mt: 1 }}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDetailDialog}>{t('common.buttons.cancel')}</Button>
          <Button onClick={handleSaveDetail} variant="contained">
            {selectedDetail ? t('common.buttons.edit') : t('common.buttons.add')}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={openDeleteDialog} onClose={handleCloseDeleteDialog}>
        <DialogTitle>{t('pages.commonCodes.deleteConfirm.title')}</DialogTitle>
        <DialogContent>
          <Typography>
            {deleteTarget?.type === 'group' ? t('pages.commonCodes.deleteConfirm.groupMessage') : t('pages.commonCodes.deleteConfirm.detailMessage')}
          </Typography>
          {deleteTarget?.type === 'group' && (
            <Alert severity="warning" sx={{ mt: 2 }}>
              {t('pages.commonCodes.deleteConfirm.groupWarning')}
            </Alert>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDeleteDialog}>{t('common.buttons.cancel')}</Button>
          <Button
            onClick={deleteTarget?.type === 'group' ? handleDeleteGroup : handleDeleteDetail}
            color="error"
            variant="contained"
          >
            {t('common.buttons.delete')}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Snackbar */}
      <Snackbar
        open={snackbar.open}
        autoHideDuration={3000}
        onClose={handleCloseSnackbar}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert onClose={handleCloseSnackbar} severity={snackbar.severity} sx={{ width: '100%' }}>
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Box>
  );
};

export default CommonCodesPage;
