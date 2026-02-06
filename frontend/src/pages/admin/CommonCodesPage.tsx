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
import commonCodeService, {
  CommonCodeGroup,
  CommonCodeDetail,
  CommonCodeGroupCreateRequest,
  CommonCodeDetailCreateRequest,
} from '../../services/commonCodeService';

const CommonCodesPage: React.FC = () => {
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
  }, []);

  useEffect(() => {
    if (selectedGroupId) {
      loadCodeDetails(selectedGroupId);
    } else {
      setCodeDetails([]);
    }
  }, [selectedGroupId]);

  const loadCodeGroups = async () => {
    try {
      setLoadingGroups(true);
      const data = await commonCodeService.getCodeGroups();
      setCodeGroups(data || []);
    } catch (error) {
      showSnackbar('코드 그룹 목록 조회 실패', 'error');
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
      showSnackbar('코드 상세 목록 조회 실패', 'error');
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
        showSnackbar('코드 그룹 수정 성공', 'success');
      } else {
        await commonCodeService.createCodeGroup(groupFormData);
        showSnackbar('코드 그룹 생성 성공', 'success');
      }
      handleCloseGroupDialog();
      loadCodeGroups();
    } catch (error) {
      showSnackbar(selectedGroup ? '코드 그룹 수정 실패' : '코드 그룹 생성 실패', 'error');
    }
  };

  const handleDeleteGroup = async () => {
    if (!deleteTarget || deleteTarget.type !== 'group') return;

    try {
      await commonCodeService.deleteCodeGroup(deleteTarget.id);
      showSnackbar('코드 그룹 삭제 성공', 'success');
      setOpenDeleteDialog(false);
      setDeleteTarget(null);
      setSelectedGroupId(null);
      loadCodeGroups();
    } catch (error) {
      showSnackbar('코드 그룹 삭제 실패 (시스템 코드는 삭제 불가)', 'error');
    }
  };

  // ==================== Code Detail Handlers ====================

  const handleOpenDetailDialog = (detail?: CommonCodeDetail) => {
    if (!selectedGroupId) {
      showSnackbar('코드 그룹을 먼저 선택하세요', 'error');
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
        showSnackbar('코드 상세 수정 성공', 'success');
      } else {
        await commonCodeService.createCodeDetail(selectedGroupId, detailFormData);
        showSnackbar('코드 상세 생성 성공', 'success');
      }
      handleCloseDetailDialog();
      loadCodeDetails(selectedGroupId);
    } catch (error) {
      showSnackbar(selectedDetail ? '코드 상세 수정 실패' : '코드 상세 생성 실패', 'error');
    }
  };

  const handleDeleteDetail = async () => {
    if (!deleteTarget || deleteTarget.type !== 'detail') return;

    try {
      await commonCodeService.deleteCodeDetail(deleteTarget.id);
      showSnackbar('코드 상세 삭제 성공', 'success');
      setOpenDeleteDialog(false);
      setDeleteTarget(null);
      if (selectedGroupId) {
        loadCodeDetails(selectedGroupId);
      }
    } catch (error) {
      showSnackbar('코드 상세 삭제 실패', 'error');
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
    { field: 'codeGroup', headerName: '코드 그룹', width: 150 },
    { field: 'codeGroupName', headerName: '코드 그룹명', width: 200, flex: 1 },
    {
      field: 'isSystem',
      headerName: '시스템',
      width: 100,
      renderCell: (params) => (
        <Chip
          label={params.value ? '시스템' : '사용자'}
          color={params.value ? 'warning' : 'default'}
          size="small"
        />
      ),
    },
    {
      field: 'isActive',
      headerName: '활성',
      width: 100,
      renderCell: (params) => (
        <Chip
          label={params.value ? '활성' : '비활성'}
          color={params.value ? 'success' : 'default'}
          size="small"
        />
      ),
    },
    { field: 'displayOrder', headerName: '순서', width: 80 },
    {
      field: 'actions',
      type: 'actions',
      headerName: '작업',
      width: 100,
      getActions: (params: GridRowParams) => [
        <GridActionsCellItem
          icon={<EditIcon />}
          label="수정"
          onClick={() => handleOpenGroupDialog(params.row as CommonCodeGroup)}
        />,
        <GridActionsCellItem
          icon={<DeleteIcon />}
          label="삭제"
          onClick={() => handleOpenDeleteDialog('group', params.row.codeGroupId)}
          disabled={params.row.isSystem}
        />,
      ],
    },
  ];

  const detailColumns: GridColDef[] = [
    { field: 'code', headerName: '코드', width: 120 },
    { field: 'codeName', headerName: '코드명', width: 150, flex: 1 },
    {
      field: 'colorCode',
      headerName: '색상',
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
      headerName: '기본값',
      width: 90,
      renderCell: (params) => (
        params.value ? <Chip label="기본" color="primary" size="small" /> : null
      ),
    },
    {
      field: 'isActive',
      headerName: '활성',
      width: 90,
      renderCell: (params) => (
        <Chip
          label={params.value ? '활성' : '비활성'}
          color={params.value ? 'success' : 'default'}
          size="small"
        />
      ),
    },
    { field: 'displayOrder', headerName: '순서', width: 80 },
    {
      field: 'actions',
      type: 'actions',
      headerName: '작업',
      width: 100,
      getActions: (params: GridRowParams) => [
        <GridActionsCellItem
          icon={<EditIcon />}
          label="수정"
          onClick={() => handleOpenDetailDialog(params.row as CommonCodeDetail)}
        />,
        <GridActionsCellItem
          icon={<DeleteIcon />}
          label="삭제"
          onClick={() => handleOpenDeleteDialog('detail', params.row.codeDetailId)}
        />,
      ],
    },
  ];

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        공통 코드 관리
      </Typography>

      <Grid container spacing={2}>
        {/* Left Panel: Code Groups */}
        <Grid item xs={12} md={5}>
          <Paper sx={{ p: 2, height: 'calc(100vh - 200px)' }}>
            <Box sx={{ mb: 2, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <Typography variant="h6">코드 그룹</Typography>
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
                  그룹 추가
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
                코드 상세 {selectedGroupId && `(${codeGroups.find(g => g.codeGroupId === selectedGroupId)?.codeGroupName})`}
              </Typography>
              <Button
                variant="contained"
                startIcon={<AddIcon />}
                onClick={() => handleOpenDetailDialog()}
                disabled={!selectedGroupId}
                size="small"
              >
                코드 추가
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
                  코드 그룹을 선택하세요
                </Typography>
              </Box>
            )}
          </Paper>
        </Grid>
      </Grid>

      {/* Code Group Dialog */}
      <Dialog open={openGroupDialog} onClose={handleCloseGroupDialog} maxWidth="sm" fullWidth>
        <DialogTitle>{selectedGroup ? '코드 그룹 수정' : '코드 그룹 생성'}</DialogTitle>
        <DialogContent>
          <TextField
            fullWidth
            label="코드 그룹"
            value={groupFormData.codeGroup}
            onChange={(e) => setGroupFormData({ ...groupFormData, codeGroup: e.target.value.toUpperCase() })}
            margin="normal"
            required
            disabled={!!selectedGroup}
            helperText="영문 대문자, 숫자, 언더스코어(_) 사용"
          />
          <TextField
            fullWidth
            label="코드 그룹명"
            value={groupFormData.codeGroupName}
            onChange={(e) => setGroupFormData({ ...groupFormData, codeGroupName: e.target.value })}
            margin="normal"
            required
          />
          <TextField
            fullWidth
            label="설명"
            value={groupFormData.description}
            onChange={(e) => setGroupFormData({ ...groupFormData, description: e.target.value })}
            margin="normal"
            multiline
            rows={3}
          />
          <TextField
            fullWidth
            type="number"
            label="표시 순서"
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
            label="활성"
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
              label="시스템 코드 (삭제 불가)"
              sx={{ mt: 1 }}
            />
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseGroupDialog}>취소</Button>
          <Button onClick={handleSaveGroup} variant="contained">
            {selectedGroup ? '수정' : '생성'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Code Detail Dialog */}
      <Dialog open={openDetailDialog} onClose={handleCloseDetailDialog} maxWidth="sm" fullWidth>
        <DialogTitle>{selectedDetail ? '코드 상세 수정' : '코드 상세 생성'}</DialogTitle>
        <DialogContent>
          <TextField
            fullWidth
            label="코드"
            value={detailFormData.code}
            onChange={(e) => setDetailFormData({ ...detailFormData, code: e.target.value.toUpperCase() })}
            margin="normal"
            required
            disabled={!!selectedDetail}
            helperText="영문 대문자, 숫자, 언더스코어(_) 사용"
          />
          <TextField
            fullWidth
            label="코드명"
            value={detailFormData.codeName}
            onChange={(e) => setDetailFormData({ ...detailFormData, codeName: e.target.value })}
            margin="normal"
            required
          />
          <TextField
            fullWidth
            label="설명"
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
                label="표시 순서"
                value={detailFormData.displayOrder}
                onChange={(e) => setDetailFormData({ ...detailFormData, displayOrder: parseInt(e.target.value) })}
                margin="normal"
              />
            </Grid>
            <Grid item xs={6}>
              <TextField
                fullWidth
                label="색상 코드"
                value={detailFormData.colorCode}
                onChange={(e) => setDetailFormData({ ...detailFormData, colorCode: e.target.value })}
                margin="normal"
                placeholder="#4CAF50"
              />
            </Grid>
          </Grid>
          <TextField
            fullWidth
            label="아이콘명"
            value={detailFormData.iconName}
            onChange={(e) => setDetailFormData({ ...detailFormData, iconName: e.target.value })}
            margin="normal"
            placeholder="CheckCircleIcon"
          />

          <Divider sx={{ my: 2 }} />
          <Typography variant="subtitle2" gutterBottom>확장 필드</Typography>

          <TextField
            fullWidth
            label="확장 필드 1"
            value={detailFormData.value1}
            onChange={(e) => setDetailFormData({ ...detailFormData, value1: e.target.value })}
            margin="normal"
            size="small"
          />
          <TextField
            fullWidth
            label="확장 필드 2"
            value={detailFormData.value2}
            onChange={(e) => setDetailFormData({ ...detailFormData, value2: e.target.value })}
            margin="normal"
            size="small"
          />
          <TextField
            fullWidth
            label="확장 필드 3"
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
            label="기본값"
            sx={{ mt: 2 }}
          />
          <FormControlLabel
            control={
              <Switch
                checked={detailFormData.isActive}
                onChange={(e) => setDetailFormData({ ...detailFormData, isActive: e.target.checked })}
              />
            }
            label="활성"
            sx={{ mt: 1 }}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDetailDialog}>취소</Button>
          <Button onClick={handleSaveDetail} variant="contained">
            {selectedDetail ? '수정' : '생성'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={openDeleteDialog} onClose={handleCloseDeleteDialog}>
        <DialogTitle>삭제 확인</DialogTitle>
        <DialogContent>
          <Typography>
            {deleteTarget?.type === 'group' ? '코드 그룹을' : '코드 상세를'} 삭제하시겠습니까?
          </Typography>
          {deleteTarget?.type === 'group' && (
            <Alert severity="warning" sx={{ mt: 2 }}>
              코드 그룹을 삭제하면 하위 코드 상세도 모두 삭제됩니다.
            </Alert>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDeleteDialog}>취소</Button>
          <Button
            onClick={deleteTarget?.type === 'group' ? handleDeleteGroup : handleDeleteDetail}
            color="error"
            variant="contained"
          >
            삭제
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
