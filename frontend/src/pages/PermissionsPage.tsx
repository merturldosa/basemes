/**
 * Permissions Management Page
 * 권한 관리 페이지
 * @author Moon Myung-seop
 */

import { useState, useEffect } from 'react';
import {
  Box,
  Button,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
  IconButton,
  Chip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Alert,
  CircularProgress,
  Tooltip,
  MenuItem,
} from '@mui/material';
import {
  Add,
  Edit,
  Delete,
  ToggleOn,
  ToggleOff,
} from '@mui/icons-material';
import permissionService, {
  Permission,
  PermissionCreateRequest,
  PermissionUpdateRequest,
} from '@/services/permissionService';
import { getErrorMessage } from '@/utils/errorUtils';

const MODULES = [
  '사용자관리',
  '역할관리',
  '권한관리',
  '생산관리',
  '품질관리',
  '재고관리',
  '설비관리',
  '시스템관리',
];

export default function PermissionsPage() {
  const [permissions, setPermissions] = useState<Permission[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Dialog states
  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);

  // Form states
  const [selectedPermission, setSelectedPermission] = useState<Permission | null>(null);
  const [formData, setFormData] = useState<PermissionCreateRequest>({
    permissionCode: '',
    permissionName: '',
    module: '',
    description: '',
  });

  // 데이터 로드
  useEffect(() => {
    loadPermissions();
  }, []);

  const loadPermissions = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await permissionService.getPermissions();
      setPermissions(data || []);
    } catch (err) {
      setPermissions([]);
      setError(getErrorMessage(err, '권한 목록 로드 실패'));
    } finally {
      setLoading(false);
    }
  };

  // 권한 생성
  const handleCreate = async () => {
    try {
      await permissionService.createPermission(formData);
      setCreateDialogOpen(false);
      setFormData({ permissionCode: '', permissionName: '', module: '', description: '' });
      loadPermissions();
    } catch (err) {
      setError(getErrorMessage(err, '권한 생성 실패'));
    }
  };

  // 권한 수정
  const handleEdit = async () => {
    if (!selectedPermission) return;

    try {
      const updateData: PermissionUpdateRequest = {
        permissionName: formData.permissionName,
        module: formData.module,
        description: formData.description,
      };
      await permissionService.updatePermission(selectedPermission.permissionId, updateData);
      setEditDialogOpen(false);
      setSelectedPermission(null);
      loadPermissions();
    } catch (err) {
      setError(getErrorMessage(err, '권한 수정 실패'));
    }
  };

  // 권한 삭제
  const handleDelete = async () => {
    if (!selectedPermission) return;

    try {
      await permissionService.deletePermission(selectedPermission.permissionId);
      setDeleteDialogOpen(false);
      setSelectedPermission(null);
      loadPermissions();
    } catch (err) {
      setError(getErrorMessage(err, '권한 삭제 실패'));
    }
  };

  // 권한 활성화/비활성화
  const handleToggleStatus = async (permission: Permission) => {
    try {
      if (permission.status === 'active') {
        await permissionService.deactivatePermission(permission.permissionId);
      } else {
        await permissionService.activatePermission(permission.permissionId);
      }
      loadPermissions();
    } catch (err) {
      setError(getErrorMessage(err, '권한 상태 변경 실패'));
    }
  };

  // 생성 다이얼로그 열기
  const openCreateDialog = () => {
    setFormData({ permissionCode: '', permissionName: '', module: '', description: '' });
    setCreateDialogOpen(true);
  };

  // 수정 다이얼로그 열기
  const openEditDialog = (permission: Permission) => {
    setSelectedPermission(permission);
    setFormData({
      permissionCode: permission.permissionCode,
      permissionName: permission.permissionName,
      module: permission.module,
      description: permission.description || '',
    });
    setEditDialogOpen(true);
  };

  // 삭제 다이얼로그 열기
  const openDeleteDialog = (permission: Permission) => {
    setSelectedPermission(permission);
    setDeleteDialogOpen(true);
  };

  if (loading && permissions.length === 0) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4" fontWeight="bold">
          권한 관리
        </Typography>
        <Button
          variant="contained"
          startIcon={<Add />}
          onClick={openCreateDialog}
        >
          권한 추가
        </Button>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>권한 코드</TableCell>
              <TableCell>권한 이름</TableCell>
              <TableCell>모듈</TableCell>
              <TableCell>설명</TableCell>
              <TableCell>상태</TableCell>
              <TableCell align="right">작업</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {permissions.map((permission) => (
              <TableRow key={permission.permissionId} hover>
                <TableCell>{permission.permissionCode}</TableCell>
                <TableCell>{permission.permissionName}</TableCell>
                <TableCell>
                  <Chip label={permission.module} size="small" variant="outlined" />
                </TableCell>
                <TableCell>{permission.description || '-'}</TableCell>
                <TableCell>
                  <Chip
                    label={permission.status === 'active' ? '활성' : '비활성'}
                    color={permission.status === 'active' ? 'success' : 'default'}
                    size="small"
                  />
                </TableCell>
                <TableCell align="right">
                  <Tooltip title={permission.status === 'active' ? '비활성화' : '활성화'}>
                    <IconButton
                      size="small"
                      color={permission.status === 'active' ? 'success' : 'default'}
                      onClick={() => handleToggleStatus(permission)}
                    >
                      {permission.status === 'active' ? <ToggleOn /> : <ToggleOff />}
                    </IconButton>
                  </Tooltip>
                  <Tooltip title="수정">
                    <IconButton
                      size="small"
                      color="primary"
                      onClick={() => openEditDialog(permission)}
                    >
                      <Edit />
                    </IconButton>
                  </Tooltip>
                  <Tooltip title="삭제">
                    <IconButton
                      size="small"
                      color="error"
                      onClick={() => openDeleteDialog(permission)}
                    >
                      <Delete />
                    </IconButton>
                  </Tooltip>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      {/* 권한 생성 다이얼로그 */}
      <Dialog open={createDialogOpen} onClose={() => setCreateDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>권한 추가</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin="dense"
            label="권한 코드"
            type="text"
            fullWidth
            required
            value={formData.permissionCode}
            onChange={(e) => setFormData({ ...formData, permissionCode: e.target.value })}
            helperText="영문 대문자, 숫자, 언더스코어만 사용 (예: USER_CREATE)"
          />
          <TextField
            margin="dense"
            label="권한 이름"
            type="text"
            fullWidth
            required
            value={formData.permissionName}
            onChange={(e) => setFormData({ ...formData, permissionName: e.target.value })}
          />
          <TextField
            margin="dense"
            label="모듈"
            select
            fullWidth
            required
            value={formData.module}
            onChange={(e) => setFormData({ ...formData, module: e.target.value })}
          >
            {MODULES.map((module) => (
              <MenuItem key={module} value={module}>
                {module}
              </MenuItem>
            ))}
          </TextField>
          <TextField
            margin="dense"
            label="설명"
            type="text"
            fullWidth
            multiline
            rows={3}
            value={formData.description}
            onChange={(e) => setFormData({ ...formData, description: e.target.value })}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setCreateDialogOpen(false)}>취소</Button>
          <Button
            onClick={handleCreate}
            variant="contained"
            disabled={!formData.permissionCode || !formData.permissionName || !formData.module}
          >
            추가
          </Button>
        </DialogActions>
      </Dialog>

      {/* 권한 수정 다이얼로그 */}
      <Dialog open={editDialogOpen} onClose={() => setEditDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>권한 수정</DialogTitle>
        <DialogContent>
          <TextField
            margin="dense"
            label="권한 코드"
            type="text"
            fullWidth
            disabled
            value={formData.permissionCode}
          />
          <TextField
            margin="dense"
            label="권한 이름"
            type="text"
            fullWidth
            required
            value={formData.permissionName}
            onChange={(e) => setFormData({ ...formData, permissionName: e.target.value })}
          />
          <TextField
            margin="dense"
            label="모듈"
            select
            fullWidth
            required
            value={formData.module}
            onChange={(e) => setFormData({ ...formData, module: e.target.value })}
          >
            {MODULES.map((module) => (
              <MenuItem key={module} value={module}>
                {module}
              </MenuItem>
            ))}
          </TextField>
          <TextField
            margin="dense"
            label="설명"
            type="text"
            fullWidth
            multiline
            rows={3}
            value={formData.description}
            onChange={(e) => setFormData({ ...formData, description: e.target.value })}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setEditDialogOpen(false)}>취소</Button>
          <Button
            onClick={handleEdit}
            variant="contained"
            disabled={!formData.permissionName || !formData.module}
          >
            저장
          </Button>
        </DialogActions>
      </Dialog>

      {/* 권한 삭제 확인 다이얼로그 */}
      <Dialog open={deleteDialogOpen} onClose={() => setDeleteDialogOpen(false)}>
        <DialogTitle>권한 삭제 확인</DialogTitle>
        <DialogContent>
          <Typography>
            정말로 <strong>{selectedPermission?.permissionName}</strong> 권한을 삭제하시겠습니까?
          </Typography>
          <Typography variant="body2" color="error" sx={{ mt: 1 }}>
            이 작업은 되돌릴 수 없으며, 해당 권한을 사용하는 역할에 영향을 미칠 수 있습니다.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialogOpen(false)}>취소</Button>
          <Button onClick={handleDelete} variant="contained" color="error">
            삭제
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
