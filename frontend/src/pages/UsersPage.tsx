/**
 * Users Management Page
 * 사용자 관리 페이지
 * @author Moon Myung-seop
 */

import { useState } from 'react';
import { useTranslation } from 'react-i18next';
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
  IconButton,
  Chip,
  Alert,
  Snackbar,
  MenuItem,
  Stack,
  InputAdornment,
} from '@mui/material';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Search as SearchIcon,
  CheckCircle as ActiveIcon,
  Cancel as InactiveIcon,
  Refresh as RefreshIcon,
} from '@mui/icons-material';
import { GridColDef } from '@mui/x-data-grid';
import { User, UserCreateRequest, UserUpdateRequest } from '@/types';
import userService from '@/services/userService';
import { format } from 'date-fns';
import EnhancedDataGrid from '@/components/common/EnhancedDataGrid';
import { getErrorMessage } from '@/utils/errorUtils';

export default function UsersPage() {
  const { t } = useTranslation();

  // State
  const [searchText, setSearchText] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [refreshKey, setRefreshKey] = useState(0); // Used to trigger data reload

  // Dialog State
  const [openDialog, setOpenDialog] = useState(false);
  const [editingUser, setEditingUser] = useState<User | null>(null);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [userToDelete, setUserToDelete] = useState<User | null>(null);

  // Form State
  const [formData, setFormData] = useState<UserCreateRequest | UserUpdateRequest>({
    username: '',
    email: '',
    password: '',
    fullName: '',
    preferredLanguage: 'ko',
  });

  // Snackbar State
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'success' as 'success' | 'error',
  });

  /**
   * Load users data with filters
   * Used by EnhancedDataGrid
   */
  const loadUsersData = async (page: number, pageSize: number) => {
    return await userService.getUsers({
      page,
      size: pageSize,
      status: statusFilter === 'ALL' ? undefined : statusFilter,
      search: searchText || undefined,
    });
  };

  // Show Snackbar
  const showSnackbar = (message: string, severity: 'success' | 'error') => {
    setSnackbar({ open: true, message, severity });
  };

  // Handle Create/Update User
  const handleSaveUser = async () => {
    try {
      if (editingUser) {
        // Update
        await userService.updateUser(editingUser.userId, formData as UserUpdateRequest);
        showSnackbar(t('pages.users.messages.updated'), 'success');
      } else {
        // Create
        await userService.createUser(formData as UserCreateRequest);
        showSnackbar(t('pages.users.messages.created'), 'success');
      }
      setOpenDialog(false);
      resetForm();
      setRefreshKey((prev) => prev + 1); // Trigger data reload
    } catch (error) {
      showSnackbar(getErrorMessage(error, t('pages.users.messages.saveFailed')), 'error');
    }
  };

  // Handle Delete User
  const handleDeleteUser = async () => {
    if (!userToDelete) return;

    try {
      await userService.deleteUser(userToDelete.userId);
      showSnackbar(t('pages.users.messages.deleted'), 'success');
      setDeleteDialogOpen(false);
      setUserToDelete(null);
      setRefreshKey((prev) => prev + 1); // Trigger data reload
    } catch (error) {
      showSnackbar(getErrorMessage(error, t('pages.users.messages.deleteFailed')), 'error');
    }
  };

  // Handle Activate/Deactivate User
  const handleToggleUserStatus = async (user: User) => {
    try {
      if (user.status === 'ACTIVE') {
        await userService.deactivateUser(user.userId);
        showSnackbar(t('pages.users.messages.deactivated'), 'success');
      } else {
        await userService.activateUser(user.userId);
        showSnackbar(t('pages.users.messages.activated'), 'success');
      }
      setRefreshKey((prev) => prev + 1); // Trigger data reload
    } catch (error) {
      showSnackbar(getErrorMessage(error, t('pages.users.messages.statusChangeFailed')), 'error');
    }
  };

  // Open Create Dialog
  const handleOpenCreateDialog = () => {
    setEditingUser(null);
    resetForm();
    setOpenDialog(true);
  };

  // Open Edit Dialog
  const handleOpenEditDialog = (user: User) => {
    setEditingUser(user);
    setFormData({
      email: user.email,
      fullName: user.fullName,
      preferredLanguage: user.preferredLanguage,
    });
    setOpenDialog(true);
  };

  // Open Delete Dialog
  const handleOpenDeleteDialog = (user: User) => {
    setUserToDelete(user);
    setDeleteDialogOpen(true);
  };

  // Reset Form
  const resetForm = () => {
    setFormData({
      username: '',
      email: '',
      password: '',
      fullName: '',
      preferredLanguage: 'ko',
    });
  };

  // DataGrid Columns
  const columns: GridColDef[] = [
    {
      field: 'username',
      headerName: t('pages.users.fields.username'),
      width: 150,
      flex: 1,
    },
    {
      field: 'fullName',
      headerName: t('pages.users.fields.fullName'),
      width: 150,
      flex: 1,
    },
    {
      field: 'email',
      headerName: t('pages.users.fields.email'),
      width: 200,
      flex: 1,
    },
    {
      field: 'status',
      headerName: t('common.labels.status'),
      width: 100,
      renderCell: (params) => (
        <Chip
          label={params.value === 'ACTIVE' ? t('common.status.active') : t('common.status.inactive')}
          color={params.value === 'ACTIVE' ? 'success' : 'default'}
          size="small"
        />
      ),
    },
    {
      field: 'lastLoginAt',
      headerName: t('pages.users.fields.lastLoginAt'),
      width: 180,
      renderCell: (params) =>
        params.value ? format(new Date(params.value), 'yyyy-MM-dd HH:mm:ss') : '-',
    },
    {
      field: 'createdAt',
      headerName: t('common.labels.createdAt'),
      width: 180,
      renderCell: (params) => format(new Date(params.value), 'yyyy-MM-dd HH:mm:ss'),
    },
    {
      field: 'actions',
      headerName: t('common.labels.actions'),
      width: 180,
      sortable: false,
      renderCell: (params) => (
        <Stack direction="row" spacing={1}>
          <IconButton
            size="small"
            color="primary"
            onClick={() => handleOpenEditDialog(params.row)}
            title={t('common.buttons.edit')}
          >
            <EditIcon fontSize="small" />
          </IconButton>
          <IconButton
            size="small"
            color={params.row.status === 'ACTIVE' ? 'warning' : 'success'}
            onClick={() => handleToggleUserStatus(params.row)}
            title={params.row.status === 'ACTIVE' ? t('pages.users.actions.deactivate') : t('pages.users.actions.activate')}
          >
            {params.row.status === 'ACTIVE' ? (
              <InactiveIcon fontSize="small" />
            ) : (
              <ActiveIcon fontSize="small" />
            )}
          </IconButton>
          <IconButton
            size="small"
            color="error"
            onClick={() => handleOpenDeleteDialog(params.row)}
            title={t('common.buttons.delete')}
          >
            <DeleteIcon fontSize="small" />
          </IconButton>
        </Stack>
      ),
    },
  ];

  return (
    <Box>
      {/* Header */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" gutterBottom fontWeight="bold">
          {t('pages.users.title')}
        </Typography>
        <Typography variant="body1" color="text.secondary">
          {t('pages.users.subtitle')}
        </Typography>
      </Box>

      {/* Toolbar */}
      <Paper sx={{ p: 2, mb: 3 }}>
        <Stack direction="row" spacing={2} alignItems="center">
          <TextField
            placeholder={t('pages.users.searchPlaceholder')}
            value={searchText}
            onChange={(e) => setSearchText(e.target.value)}
            onKeyPress={(e) => e.key === 'Enter' && setRefreshKey((prev) => prev + 1)}
            size="small"
            sx={{ flexGrow: 1 }}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon />
                </InputAdornment>
              ),
            }}
          />
          <TextField
            select
            label={t('common.labels.status')}
            value={statusFilter}
            onChange={(e) => {
              setStatusFilter(e.target.value);
              setRefreshKey((prev) => prev + 1);
            }}
            size="small"
            sx={{ width: 150 }}
          >
            <MenuItem value="ALL">{t('pages.users.filters.all')}</MenuItem>
            <MenuItem value="ACTIVE">{t('common.status.active')}</MenuItem>
            <MenuItem value="INACTIVE">{t('common.status.inactive')}</MenuItem>
          </TextField>
          <Button
            variant="outlined"
            startIcon={<SearchIcon />}
            onClick={() => setRefreshKey((prev) => prev + 1)}
          >
            {t('common.buttons.search')}
          </Button>
          <Button
            variant="outlined"
            startIcon={<RefreshIcon />}
            onClick={() => setRefreshKey((prev) => prev + 1)}
          >
            {t('common.buttons.refresh')}
          </Button>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={handleOpenCreateDialog}
          >
            {t('pages.users.actions.addUser')}
          </Button>
        </Stack>
      </Paper>

      {/* Enhanced Data Grid with Infinite Scroll */}
      <Paper sx={{ height: 600 }}>
        <EnhancedDataGrid
          key={refreshKey}
          loadData={loadUsersData}
          columns={columns}
          getRowId={(row) => row.userId}
          initialPageSize={25}
          autoLoadNext={true}
          onError={(error) => showSnackbar(error.message, 'error')}
        />
      </Paper>

      {/* Create/Edit Dialog */}
      <Dialog open={openDialog} onClose={() => setOpenDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{editingUser ? t('pages.users.dialogs.editTitle') : t('pages.users.dialogs.createTitle')}</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 2 }}>
            {!editingUser && (
              <TextField
                label={t('pages.users.fields.username')}
                value={(formData as UserCreateRequest).username || ''}
                onChange={(e) =>
                  setFormData({ ...formData, username: e.target.value } as UserCreateRequest)
                }
                fullWidth
                required
              />
            )}
            <TextField
              label={t('pages.users.fields.email')}
              type="email"
              value={formData.email}
              onChange={(e) => setFormData({ ...formData, email: e.target.value })}
              fullWidth
              required
            />
            <TextField
              label={t('pages.users.fields.fullName')}
              value={formData.fullName}
              onChange={(e) => setFormData({ ...formData, fullName: e.target.value })}
              fullWidth
              required
            />
            {!editingUser && (
              <TextField
                label={t('pages.users.fields.password')}
                type="password"
                value={(formData as UserCreateRequest).password || ''}
                onChange={(e) =>
                  setFormData({ ...formData, password: e.target.value } as UserCreateRequest)
                }
                fullWidth
                required
              />
            )}
            <TextField
              select
              label={t('pages.users.fields.preferredLanguage')}
              value={formData.preferredLanguage || 'ko'}
              onChange={(e) => setFormData({ ...formData, preferredLanguage: e.target.value })}
              fullWidth
            >
              <MenuItem value="ko">{t('pages.users.languages.ko')}</MenuItem>
              <MenuItem value="en">{t('pages.users.languages.en')}</MenuItem>
              <MenuItem value="zh">{t('pages.users.languages.zh')}</MenuItem>
            </TextField>
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenDialog(false)}>{t('common.buttons.cancel')}</Button>
          <Button onClick={handleSaveUser} variant="contained">
            {editingUser ? t('common.buttons.edit') : t('pages.users.actions.create')}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={deleteDialogOpen} onClose={() => setDeleteDialogOpen(false)}>
        <DialogTitle>{t('pages.users.dialogs.deleteTitle')}</DialogTitle>
        <DialogContent>
          <Typography>
            {t('pages.users.dialogs.deleteConfirm', { name: userToDelete?.fullName })}
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
            {t('pages.users.dialogs.deleteWarning')}
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialogOpen(false)}>{t('common.buttons.cancel')}</Button>
          <Button onClick={handleDeleteUser} color="error" variant="contained">
            {t('common.buttons.delete')}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Snackbar */}
      <Snackbar
        open={snackbar.open}
        autoHideDuration={6000}
        onClose={() => setSnackbar({ ...snackbar, open: false })}
        anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
      >
        <Alert
          onClose={() => setSnackbar({ ...snackbar, open: false })}
          severity={snackbar.severity}
          sx={{ width: '100%' }}
        >
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Box>
  );
}
