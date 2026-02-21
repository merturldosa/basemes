/**
 * Roles Management Page
 * 역할 관리 페이지
 * @author Moon Myung-seop
 */

import { useState, useEffect } from 'react';
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
  List,
  ListItem,
  ListItemText,
  Checkbox,
  ListItemIcon,
  Divider,
} from '@mui/material';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  CheckCircle as ActiveIcon,
  Cancel as InactiveIcon,
  Refresh as RefreshIcon,
  VpnKey as PermissionIcon,
} from '@mui/icons-material';
import { DataGrid, GridColDef, GridPaginationModel } from '@mui/x-data-grid';
import { Role, RoleCreateRequest, RoleUpdateRequest, Permission } from '@/types';
import roleService from '@/services/roleService';
import apiClient from '@/services/api';
import { format } from 'date-fns';

export default function RolesPage() {
  const { t } = useTranslation();

  // State
  const [roles, setRoles] = useState<Role[]>([]);
  const [loading, setLoading] = useState(false);
  const [paginationModel, setPaginationModel] = useState<GridPaginationModel>({
    page: 0,
    pageSize: 10,
  });
  const [statusFilter, setStatusFilter] = useState('ALL');

  // Dialog State
  const [openDialog, setOpenDialog] = useState(false);
  const [editingRole, setEditingRole] = useState<Role | null>(null);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [roleToDelete, setRoleToDelete] = useState<Role | null>(null);

  // Permission Dialog State
  const [permissionDialogOpen, setPermissionDialogOpen] = useState(false);
  const [selectedRole, setSelectedRole] = useState<Role | null>(null);
  const [allPermissions, setAllPermissions] = useState<Permission[]>([]);
  const [rolePermissions, setRolePermissions] = useState<Permission[]>([]);

  // Form State
  const [formData, setFormData] = useState<RoleCreateRequest | RoleUpdateRequest>({
    roleCode: '',
    roleName: '',
    description: '',
  });

  // Snackbar State
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'success' as 'success' | 'error',
  });

  // Load Roles
  const loadRoles = async () => {
    try {
      setLoading(true);
      const allRoles = await roleService.getRoles();

      // Client-side filtering by status
      const filteredRoles = statusFilter === 'ALL'
        ? allRoles
        : allRoles.filter(role =>
            statusFilter === 'ACTIVE' ? role.status === 'ACTIVE' : role.status !== 'ACTIVE'
          );

      setRoles(filteredRoles);
    } catch (error: any) {
      showSnackbar(error.response?.data?.message || t('pages.roles.messages.loadFailed'), 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadRoles();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [statusFilter]);

  // Load All Permissions
  const loadAllPermissions = async () => {
    try {
      const response = await apiClient.get<Permission[]>('/permissions', {
        page: 0,
        size: 1000,
      });
      setAllPermissions(response.data || []);
    } catch (error: any) {
      showSnackbar(t('pages.roles.messages.permissionsLoadFailed'), 'error');
    }
  };

  // Load Role Permissions
  const loadRolePermissions = async (roleId: number) => {
    try {
      const permissions = await roleService.getRolePermissions(roleId);
      setRolePermissions(permissions);
    } catch (error: any) {
      showSnackbar(t('pages.roles.messages.rolePermissionsLoadFailed'), 'error');
    }
  };

  // Show Snackbar
  const showSnackbar = (message: string, severity: 'success' | 'error') => {
    setSnackbar({ open: true, message, severity });
  };

  // Handle Create/Update Role
  const handleSaveRole = async () => {
    try {
      if (editingRole) {
        // Update
        await roleService.updateRole(editingRole.roleId, formData as RoleUpdateRequest);
        showSnackbar(t('pages.roles.messages.updated'), 'success');
      } else {
        // Create
        await roleService.createRole(formData as RoleCreateRequest);
        showSnackbar(t('pages.roles.messages.created'), 'success');
      }
      setOpenDialog(false);
      resetForm();
      loadRoles();
    } catch (error: any) {
      showSnackbar(error.response?.data?.message || t('pages.roles.messages.saveFailed'), 'error');
    }
  };

  // Handle Delete Role
  const handleDeleteRole = async () => {
    if (!roleToDelete) return;

    try {
      await roleService.deleteRole(roleToDelete.roleId);
      showSnackbar(t('pages.roles.messages.deleted'), 'success');
      setDeleteDialogOpen(false);
      setRoleToDelete(null);
      loadRoles();
    } catch (error: any) {
      showSnackbar(error.response?.data?.message || t('pages.roles.messages.deleteFailed'), 'error');
    }
  };

  // Handle Activate/Deactivate Role
  const handleToggleRoleStatus = async (role: Role) => {
    try {
      if (role.status === 'ACTIVE') {
        await roleService.deactivateRole(role.roleId);
        showSnackbar(t('pages.roles.messages.deactivated'), 'success');
      } else {
        await roleService.activateRole(role.roleId);
        showSnackbar(t('pages.roles.messages.activated'), 'success');
      }
      loadRoles();
    } catch (error: any) {
      showSnackbar(error.response?.data?.message || t('pages.roles.messages.statusChangeFailed'), 'error');
    }
  };

  // Open Permission Dialog
  const handleOpenPermissionDialog = async (role: Role) => {
    setSelectedRole(role);
    await loadAllPermissions();
    await loadRolePermissions(role.roleId);
    setPermissionDialogOpen(true);
  };

  // Handle Permission Toggle
  const handleTogglePermission = async (permission: Permission) => {
    if (!selectedRole) return;

    try {
      const hasPermission = rolePermissions.some(p => p.permissionId === permission.permissionId);

      if (hasPermission) {
        await roleService.removePermission(selectedRole.roleId, permission.permissionId);
        showSnackbar(t('pages.roles.messages.permissionRemoved'), 'success');
      } else {
        await roleService.assignPermission(selectedRole.roleId, permission.permissionId);
        showSnackbar(t('pages.roles.messages.permissionAssigned'), 'success');
      }

      await loadRolePermissions(selectedRole.roleId);
    } catch (error: any) {
      showSnackbar(error.response?.data?.message || t('pages.roles.messages.permissionChangeFailed'), 'error');
    }
  };

  // Open Create Dialog
  const handleOpenCreateDialog = () => {
    setEditingRole(null);
    resetForm();
    setOpenDialog(true);
  };

  // Open Edit Dialog
  const handleOpenEditDialog = (role: Role) => {
    setEditingRole(role);
    setFormData({
      roleName: role.roleName,
      description: role.description || '',
    });
    setOpenDialog(true);
  };

  // Open Delete Dialog
  const handleOpenDeleteDialog = (role: Role) => {
    setRoleToDelete(role);
    setDeleteDialogOpen(true);
  };

  // Reset Form
  const resetForm = () => {
    setFormData({
      roleCode: '',
      roleName: '',
      description: '',
    });
  };

  // DataGrid Columns
  const columns: GridColDef[] = [
    {
      field: 'roleCode',
      headerName: t('pages.roles.fields.roleCode'),
      width: 150,
      flex: 1,
    },
    {
      field: 'roleName',
      headerName: t('pages.roles.fields.roleName'),
      width: 200,
      flex: 1,
    },
    {
      field: 'description',
      headerName: t('common.labels.description'),
      width: 250,
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
      field: 'createdAt',
      headerName: t('common.labels.createdAt'),
      width: 180,
      renderCell: (params) => format(new Date(params.value), 'yyyy-MM-dd HH:mm:ss'),
    },
    {
      field: 'actions',
      headerName: t('common.labels.actions'),
      width: 220,
      sortable: false,
      renderCell: (params) => (
        <Stack direction="row" spacing={1}>
          <IconButton
            size="small"
            color="secondary"
            onClick={() => handleOpenPermissionDialog(params.row)}
            title={t('pages.roles.actions.managePermissions')}
          >
            <PermissionIcon fontSize="small" />
          </IconButton>
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
            onClick={() => handleToggleRoleStatus(params.row)}
            title={params.row.status === 'ACTIVE' ? t('pages.roles.actions.deactivate') : t('pages.roles.actions.activate')}
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

  // Group permissions by module
  const groupedPermissions = allPermissions.reduce((acc, permission) => {
    const module = permission.module || 'OTHER';
    if (!acc[module]) {
      acc[module] = [];
    }
    acc[module].push(permission);
    return acc;
  }, {} as Record<string, Permission[]>);

  return (
    <Box>
      {/* Header */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" gutterBottom fontWeight="bold">
          {t('pages.roles.title')}
        </Typography>
        <Typography variant="body1" color="text.secondary">
          {t('pages.roles.subtitle')}
        </Typography>
      </Box>

      {/* Toolbar */}
      <Paper sx={{ p: 2, mb: 3 }}>
        <Stack direction="row" spacing={2} alignItems="center" justifyContent="space-between">
          <TextField
            select
            label={t('common.labels.status')}
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            size="small"
            sx={{ width: 150 }}
          >
            <MenuItem value="ALL">{t('pages.roles.filters.all')}</MenuItem>
            <MenuItem value="ACTIVE">{t('common.status.active')}</MenuItem>
            <MenuItem value="INACTIVE">{t('common.status.inactive')}</MenuItem>
          </TextField>
          <Stack direction="row" spacing={2}>
            <Button
              variant="outlined"
              startIcon={<RefreshIcon />}
              onClick={loadRoles}
            >
              {t('common.buttons.refresh')}
            </Button>
            <Button
              variant="contained"
              startIcon={<AddIcon />}
              onClick={handleOpenCreateDialog}
            >
              {t('pages.roles.actions.addRole')}
            </Button>
          </Stack>
        </Stack>
      </Paper>

      {/* Data Grid */}
      <Paper sx={{ height: 600 }}>
        <DataGrid
          rows={roles}
          columns={columns}
          getRowId={(row) => row.roleId}
          loading={loading}
          paginationModel={paginationModel}
          onPaginationModelChange={setPaginationModel}
          pageSizeOptions={[10, 25, 50, 100]}
          disableRowSelectionOnClick
          sx={{
            border: 0,
            '& .MuiDataGrid-cell:focus': {
              outline: 'none',
            },
          }}
        />
      </Paper>

      {/* Create/Edit Dialog */}
      <Dialog open={openDialog} onClose={() => setOpenDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{editingRole ? t('pages.roles.dialogs.editTitle') : t('pages.roles.dialogs.createTitle')}</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 2 }}>
            {!editingRole && (
              <TextField
                label={t('pages.roles.fields.roleCode')}
                value={(formData as RoleCreateRequest).roleCode || ''}
                onChange={(e) =>
                  setFormData({ ...formData, roleCode: e.target.value } as RoleCreateRequest)
                }
                fullWidth
                required
                helperText={t('pages.roles.fields.roleCodeHelper')}
              />
            )}
            <TextField
              label={t('pages.roles.fields.roleName')}
              value={formData.roleName}
              onChange={(e) => setFormData({ ...formData, roleName: e.target.value })}
              fullWidth
              required
            />
            <TextField
              label={t('common.labels.description')}
              value={formData.description || ''}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              fullWidth
              multiline
              rows={3}
            />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenDialog(false)}>{t('common.buttons.cancel')}</Button>
          <Button onClick={handleSaveRole} variant="contained">
            {editingRole ? t('common.buttons.edit') : t('pages.roles.actions.create')}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Permission Management Dialog */}
      <Dialog
        open={permissionDialogOpen}
        onClose={() => setPermissionDialogOpen(false)}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle>
          {t('pages.roles.dialogs.permissionTitle')} - {selectedRole?.roleName}
        </DialogTitle>
        <DialogContent>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            {t('pages.roles.dialogs.permissionSubtitle')}
          </Typography>

          {Object.entries(groupedPermissions).map(([module, permissions]) => (
            <Box key={module} sx={{ mb: 3 }}>
              <Typography variant="h6" sx={{ mb: 1 }}>
                {module}
              </Typography>
              <Divider sx={{ mb: 1 }} />
              <List dense>
                {permissions.map((permission) => {
                  const isChecked = rolePermissions.some(
                    p => p.permissionId === permission.permissionId
                  );

                  return (
                    <ListItem
                      key={permission.permissionId}
                      button
                      onClick={() => handleTogglePermission(permission)}
                    >
                      <ListItemIcon>
                        <Checkbox
                          edge="start"
                          checked={isChecked}
                          tabIndex={-1}
                          disableRipple
                        />
                      </ListItemIcon>
                      <ListItemText
                        primary={permission.permissionName}
                        secondary={permission.description}
                      />
                    </ListItem>
                  );
                })}
              </List>
            </Box>
          ))}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setPermissionDialogOpen(false)}>{t('common.buttons.close')}</Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={deleteDialogOpen} onClose={() => setDeleteDialogOpen(false)}>
        <DialogTitle>{t('pages.roles.dialogs.deleteTitle')}</DialogTitle>
        <DialogContent>
          <Typography>
            {t('pages.roles.dialogs.deleteConfirm', { name: roleToDelete?.roleName })}
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
            {t('pages.roles.dialogs.deleteWarning')}
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialogOpen(false)}>{t('common.buttons.cancel')}</Button>
          <Button onClick={handleDeleteRole} color="error" variant="contained">
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
