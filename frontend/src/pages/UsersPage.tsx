/**
 * Users Management Page
 * 사용자 관리 페이지
 * @author Moon Myung-seop
 */

import { useState } from 'react';
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

export default function UsersPage() {
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
        showSnackbar('사용자가 수정되었습니다', 'success');
      } else {
        // Create
        await userService.createUser(formData as UserCreateRequest);
        showSnackbar('사용자가 생성되었습니다', 'success');
      }
      setOpenDialog(false);
      resetForm();
      setRefreshKey((prev) => prev + 1); // Trigger data reload
    } catch (error: any) {
      showSnackbar(error.response?.data?.message || '저장 실패', 'error');
    }
  };

  // Handle Delete User
  const handleDeleteUser = async () => {
    if (!userToDelete) return;

    try {
      await userService.deleteUser(userToDelete.userId);
      showSnackbar('사용자가 삭제되었습니다', 'success');
      setDeleteDialogOpen(false);
      setUserToDelete(null);
      setRefreshKey((prev) => prev + 1); // Trigger data reload
    } catch (error: any) {
      showSnackbar(error.response?.data?.message || '삭제 실패', 'error');
    }
  };

  // Handle Activate/Deactivate User
  const handleToggleUserStatus = async (user: User) => {
    try {
      if (user.status === 'ACTIVE') {
        await userService.deactivateUser(user.userId);
        showSnackbar('사용자가 비활성화되었습니다', 'success');
      } else {
        await userService.activateUser(user.userId);
        showSnackbar('사용자가 활성화되었습니다', 'success');
      }
      setRefreshKey((prev) => prev + 1); // Trigger data reload
    } catch (error: any) {
      showSnackbar(error.response?.data?.message || '상태 변경 실패', 'error');
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
      headerName: '사용자명',
      width: 150,
      flex: 1,
    },
    {
      field: 'fullName',
      headerName: '이름',
      width: 150,
      flex: 1,
    },
    {
      field: 'email',
      headerName: '이메일',
      width: 200,
      flex: 1,
    },
    {
      field: 'status',
      headerName: '상태',
      width: 100,
      renderCell: (params) => (
        <Chip
          label={params.value === 'ACTIVE' ? '활성' : '비활성'}
          color={params.value === 'ACTIVE' ? 'success' : 'default'}
          size="small"
        />
      ),
    },
    {
      field: 'lastLoginAt',
      headerName: '마지막 로그인',
      width: 180,
      renderCell: (params) =>
        params.value ? format(new Date(params.value), 'yyyy-MM-dd HH:mm:ss') : '-',
    },
    {
      field: 'createdAt',
      headerName: '생성일',
      width: 180,
      renderCell: (params) => format(new Date(params.value), 'yyyy-MM-dd HH:mm:ss'),
    },
    {
      field: 'actions',
      headerName: '작업',
      width: 180,
      sortable: false,
      renderCell: (params) => (
        <Stack direction="row" spacing={1}>
          <IconButton
            size="small"
            color="primary"
            onClick={() => handleOpenEditDialog(params.row)}
            title="수정"
          >
            <EditIcon fontSize="small" />
          </IconButton>
          <IconButton
            size="small"
            color={params.row.status === 'ACTIVE' ? 'warning' : 'success'}
            onClick={() => handleToggleUserStatus(params.row)}
            title={params.row.status === 'ACTIVE' ? '비활성화' : '활성화'}
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
            title="삭제"
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
          사용자 관리
        </Typography>
        <Typography variant="body1" color="text.secondary">
          시스템 사용자를 관리합니다
        </Typography>
      </Box>

      {/* Toolbar */}
      <Paper sx={{ p: 2, mb: 3 }}>
        <Stack direction="row" spacing={2} alignItems="center">
          <TextField
            placeholder="사용자명 또는 이메일 검색"
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
            label="상태"
            value={statusFilter}
            onChange={(e) => {
              setStatusFilter(e.target.value);
              setRefreshKey((prev) => prev + 1);
            }}
            size="small"
            sx={{ width: 150 }}
          >
            <MenuItem value="ALL">전체</MenuItem>
            <MenuItem value="ACTIVE">활성</MenuItem>
            <MenuItem value="INACTIVE">비활성</MenuItem>
          </TextField>
          <Button
            variant="outlined"
            startIcon={<SearchIcon />}
            onClick={() => setRefreshKey((prev) => prev + 1)}
          >
            검색
          </Button>
          <Button
            variant="outlined"
            startIcon={<RefreshIcon />}
            onClick={() => setRefreshKey((prev) => prev + 1)}
          >
            새로고침
          </Button>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={handleOpenCreateDialog}
          >
            사용자 추가
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
        <DialogTitle>{editingUser ? '사용자 수정' : '사용자 추가'}</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 2 }}>
            {!editingUser && (
              <TextField
                label="사용자명"
                value={(formData as UserCreateRequest).username || ''}
                onChange={(e) =>
                  setFormData({ ...formData, username: e.target.value } as UserCreateRequest)
                }
                fullWidth
                required
              />
            )}
            <TextField
              label="이메일"
              type="email"
              value={formData.email}
              onChange={(e) => setFormData({ ...formData, email: e.target.value })}
              fullWidth
              required
            />
            <TextField
              label="이름"
              value={formData.fullName}
              onChange={(e) => setFormData({ ...formData, fullName: e.target.value })}
              fullWidth
              required
            />
            {!editingUser && (
              <TextField
                label="비밀번호"
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
              label="선호 언어"
              value={formData.preferredLanguage || 'ko'}
              onChange={(e) => setFormData({ ...formData, preferredLanguage: e.target.value })}
              fullWidth
            >
              <MenuItem value="ko">한국어</MenuItem>
              <MenuItem value="en">English</MenuItem>
              <MenuItem value="zh">中文</MenuItem>
            </TextField>
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenDialog(false)}>취소</Button>
          <Button onClick={handleSaveUser} variant="contained">
            {editingUser ? '수정' : '생성'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={deleteDialogOpen} onClose={() => setDeleteDialogOpen(false)}>
        <DialogTitle>사용자 삭제</DialogTitle>
        <DialogContent>
          <Typography>
            정말로 <strong>{userToDelete?.fullName}</strong> 사용자를 삭제하시겠습니까?
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
            이 작업은 되돌릴 수 없습니다.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialogOpen(false)}>취소</Button>
          <Button onClick={handleDeleteUser} color="error" variant="contained">
            삭제
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
