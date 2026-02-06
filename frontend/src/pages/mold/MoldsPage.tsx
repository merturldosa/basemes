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
  Chip,
  IconButton,
  MenuItem,
  Select,
  FormControl,
  InputLabel,
  Snackbar,
  Alert,
} from '@mui/material';
import { DataGrid, GridColDef, GridRenderCellParams } from '@mui/x-data-grid';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  CheckCircle as ActiveIcon,
  Cancel as InactiveIcon,
  Refresh as ResetIcon,
  Warning as WarningIcon,
} from '@mui/icons-material';
import moldService, { Mold, MoldCreateRequest, MoldUpdateRequest } from '../../services/moldService';

const MoldsPage: React.FC = () => {
  const [molds, setMolds] = useState<Mold[]>([]);
  const [loading, setLoading] = useState(false);
  const [openDialog, setOpenDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [selectedMold, setSelectedMold] = useState<Mold | null>(null);
  const [formData, setFormData] = useState<MoldCreateRequest>({
    moldCode: '',
    moldName: '',
    moldType: 'INJECTION',
    status: 'AVAILABLE',
  });
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'success' as 'success' | 'error' | 'warning' | 'info',
  });

  useEffect(() => {
    loadMolds();
  }, []);

  const loadMolds = async () => {
    try {
      setLoading(true);
      const data = await moldService.getAll();
      setMolds(data || []);
    } catch (error) {
      showSnackbar('금형 목록을 불러오는데 실패했습니다.', 'error');
      setMolds([]);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenDialog = (mold?: Mold) => {
    if (mold) {
      setSelectedMold(mold);
      setFormData({
        moldCode: mold.moldCode,
        moldName: mold.moldName,
        moldType: mold.moldType,
        moldGrade: mold.moldGrade,
        cavityCount: mold.cavityCount,
        maxShotCount: mold.maxShotCount,
        maintenanceShotInterval: mold.maintenanceShotInterval,
        manufacturer: mold.manufacturer,
        modelName: mold.modelName,
        serialNo: mold.serialNo,
        material: mold.material,
        weight: mold.weight,
        dimensions: mold.dimensions,
        status: mold.status,
        location: mold.location,
        remarks: mold.remarks,
      });
    } else {
      setSelectedMold(null);
      setFormData({
        moldCode: '',
        moldName: '',
        moldType: 'INJECTION',
        status: 'AVAILABLE',
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedMold(null);
  };

  const handleSubmit = async () => {
    try {
      if (selectedMold) {
        await moldService.update(selectedMold.moldId, formData as MoldUpdateRequest);
        showSnackbar('금형이 수정되었습니다.', 'success');
      } else {
        await moldService.create(formData);
        showSnackbar('금형이 등록되었습니다.', 'success');
      }
      handleCloseDialog();
      loadMolds();
    } catch (error) {
      showSnackbar('금형 저장에 실패했습니다.', 'error');
    }
  };

  const handleDelete = async () => {
    if (selectedMold) {
      try {
        await moldService.delete(selectedMold.moldId);
        showSnackbar('금형이 삭제되었습니다.', 'success');
        setOpenDeleteDialog(false);
        setSelectedMold(null);
        loadMolds();
      } catch (error) {
        showSnackbar('금형 삭제에 실패했습니다.', 'error');
      }
    }
  };

  const handleToggleActive = async (mold: Mold) => {
    try {
      if (mold.isActive) {
        await moldService.deactivate(mold.moldId);
        showSnackbar('금형이 비활성화되었습니다.', 'success');
      } else {
        await moldService.activate(mold.moldId);
        showSnackbar('금형이 활성화되었습니다.', 'success');
      }
      loadMolds();
    } catch (error) {
      showSnackbar('금형 상태 변경에 실패했습니다.', 'error');
    }
  };

  const handleResetShotCount = async (mold: Mold) => {
    if (window.confirm(`${mold.moldName}의 Shot 수를 초기화하시겠습니까?`)) {
      try {
        await moldService.resetShotCount(mold.moldId);
        showSnackbar('Shot 수가 초기화되었습니다.', 'success');
        loadMolds();
      } catch (error) {
        showSnackbar('Shot 수 초기화에 실패했습니다.', 'error');
      }
    }
  };

  const showSnackbar = (message: string, severity: 'success' | 'error' | 'warning' | 'info') => {
    setSnackbar({ open: true, message, severity });
  };

  const getStatusChip = (status: string) => {
    const statusConfig: Record<string, { label: string; color: 'success' | 'error' | 'warning' | 'default' | 'info' }> = {
      AVAILABLE: { label: '사용가능', color: 'success' },
      IN_USE: { label: '사용중', color: 'info' },
      MAINTENANCE: { label: '보전중', color: 'warning' },
      BREAKDOWN: { label: '고장', color: 'error' },
      RETIRED: { label: '폐기', color: 'default' },
    };
    const config = statusConfig[status] || { label: status, color: 'default' };
    return <Chip label={config.label} color={config.color} size="small" />;
  };

  const columns: GridColDef[] = [
    { field: 'moldCode', headerName: '금형 코드', width: 130 },
    { field: 'moldName', headerName: '금형명', width: 180 },
    {
      field: 'moldType',
      headerName: '금형 유형',
      width: 120,
      renderCell: (params: GridRenderCellParams) => {
        const typeLabels: Record<string, string> = {
          INJECTION: '사출',
          PRESS: '프레스',
          DIE_CASTING: '다이캐스팅',
          FORGING: '단조',
          OTHER: '기타',
        };
        return typeLabels[params.value] || params.value;
      },
    },
    { field: 'moldGrade', headerName: '등급', width: 80 },
    { field: 'cavityCount', headerName: 'Cavity', width: 90 },
    {
      field: 'currentShotCount',
      headerName: '현재 Shot',
      width: 110,
      renderCell: (params: GridRenderCellParams) => {
        return params.value?.toLocaleString() || '0';
      },
    },
    {
      field: 'maintenanceProgress',
      headerName: '보전 진행률',
      width: 140,
      renderCell: (params: GridRenderCellParams) => {
        const mold = params.row as Mold;
        if (!mold.maintenanceShotInterval) return '-';
        const shotsSinceMaintenance = mold.currentShotCount - (mold.lastMaintenanceShot || 0);
        const progress = Math.min(100, (shotsSinceMaintenance / mold.maintenanceShotInterval) * 100);
        const needsMaintenance = progress >= 100;
        return (
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Typography variant="body2" sx={{ color: needsMaintenance ? 'error.main' : 'inherit' }}>
              {progress.toFixed(0)}%
            </Typography>
            {needsMaintenance && <WarningIcon color="error" fontSize="small" />}
          </Box>
        );
      },
    },
    { field: 'manufacturer', headerName: '제조사', width: 130 },
    { field: 'location', headerName: '위치', width: 120 },
    {
      field: 'status',
      headerName: '상태',
      width: 110,
      renderCell: (params: GridRenderCellParams) => getStatusChip(params.value),
    },
    {
      field: 'isActive',
      headerName: '활성',
      width: 90,
      renderCell: (params: GridRenderCellParams) => (
        <Chip
          label={params.value ? '활성' : '비활성'}
          color={params.value ? 'success' : 'default'}
          size="small"
        />
      ),
    },
    {
      field: 'actions',
      headerName: '작업',
      width: 180,
      sortable: false,
      renderCell: (params: GridRenderCellParams) => (
        <Box>
          <IconButton
            size="small"
            onClick={() => handleOpenDialog(params.row as Mold)}
            color="primary"
          >
            <EditIcon fontSize="small" />
          </IconButton>
          <IconButton
            size="small"
            onClick={() => handleResetShotCount(params.row as Mold)}
            color="info"
            title="Shot 수 초기화"
          >
            <ResetIcon fontSize="small" />
          </IconButton>
          <IconButton
            size="small"
            onClick={() => handleToggleActive(params.row as Mold)}
            color={params.row.isActive ? 'warning' : 'success'}
          >
            {params.row.isActive ? <InactiveIcon fontSize="small" /> : <ActiveIcon fontSize="small" />}
          </IconButton>
          <IconButton
            size="small"
            onClick={() => {
              setSelectedMold(params.row as Mold);
              setOpenDeleteDialog(true);
            }}
            color="error"
          >
            <DeleteIcon fontSize="small" />
          </IconButton>
        </Box>
      ),
    },
  ];

  return (
    <Box sx={{ p: 3 }}>
      <Paper sx={{ p: 2, mb: 2 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="h5" component="h1">
            금형 관리
          </Typography>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => handleOpenDialog()}
          >
            금형 등록
          </Button>
        </Box>

        <DataGrid
          rows={molds}
          columns={columns}
          loading={loading}
          getRowId={(row) => row.moldId}
          pageSizeOptions={[10, 25, 50, 100]}
          initialState={{
            pagination: { paginationModel: { pageSize: 25 } },
          }}
          autoHeight
          disableRowSelectionOnClick
        />
      </Paper>

      {/* Create/Edit Dialog */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="md" fullWidth>
        <DialogTitle>{selectedMold ? '금형 수정' : '금형 등록'}</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
            <TextField
              label="금형 코드"
              value={formData.moldCode}
              onChange={(e) => setFormData({ ...formData, moldCode: e.target.value })}
              required
              disabled={!!selectedMold}
            />
            <TextField
              label="금형명"
              value={formData.moldName}
              onChange={(e) => setFormData({ ...formData, moldName: e.target.value })}
              required
            />
            <FormControl>
              <InputLabel>금형 유형</InputLabel>
              <Select
                value={formData.moldType}
                onChange={(e) => setFormData({ ...formData, moldType: e.target.value })}
                label="금형 유형"
                required
              >
                <MenuItem value="INJECTION">사출</MenuItem>
                <MenuItem value="PRESS">프레스</MenuItem>
                <MenuItem value="DIE_CASTING">다이캐스팅</MenuItem>
                <MenuItem value="FORGING">단조</MenuItem>
                <MenuItem value="OTHER">기타</MenuItem>
              </Select>
            </FormControl>
            <FormControl>
              <InputLabel>등급</InputLabel>
              <Select
                value={formData.moldGrade || ''}
                onChange={(e) => setFormData({ ...formData, moldGrade: e.target.value })}
                label="등급"
              >
                <MenuItem value="">없음</MenuItem>
                <MenuItem value="S">S급</MenuItem>
                <MenuItem value="A">A급</MenuItem>
                <MenuItem value="B">B급</MenuItem>
                <MenuItem value="C">C급</MenuItem>
              </Select>
            </FormControl>
            <TextField
              label="Cavity 수"
              type="number"
              value={formData.cavityCount || ''}
              onChange={(e) => setFormData({ ...formData, cavityCount: parseInt(e.target.value) || undefined })}
            />
            <TextField
              label="최대 Shot 수"
              type="number"
              value={formData.maxShotCount || ''}
              onChange={(e) => setFormData({ ...formData, maxShotCount: parseInt(e.target.value) || undefined })}
            />
            <TextField
              label="보전 Shot 주기"
              type="number"
              value={formData.maintenanceShotInterval || ''}
              onChange={(e) => setFormData({ ...formData, maintenanceShotInterval: parseInt(e.target.value) || undefined })}
            />
            <TextField
              label="제조사"
              value={formData.manufacturer || ''}
              onChange={(e) => setFormData({ ...formData, manufacturer: e.target.value })}
            />
            <TextField
              label="모델명"
              value={formData.modelName || ''}
              onChange={(e) => setFormData({ ...formData, modelName: e.target.value })}
            />
            <TextField
              label="일련번호"
              value={formData.serialNo || ''}
              onChange={(e) => setFormData({ ...formData, serialNo: e.target.value })}
            />
            <TextField
              label="재질"
              value={formData.material || ''}
              onChange={(e) => setFormData({ ...formData, material: e.target.value })}
            />
            <TextField
              label="중량 (kg)"
              type="number"
              value={formData.weight || ''}
              onChange={(e) => setFormData({ ...formData, weight: parseFloat(e.target.value) || undefined })}
            />
            <TextField
              label="치수"
              value={formData.dimensions || ''}
              onChange={(e) => setFormData({ ...formData, dimensions: e.target.value })}
            />
            <TextField
              label="위치"
              value={formData.location || ''}
              onChange={(e) => setFormData({ ...formData, location: e.target.value })}
            />
            <FormControl>
              <InputLabel>상태</InputLabel>
              <Select
                value={formData.status}
                onChange={(e) => setFormData({ ...formData, status: e.target.value })}
                label="상태"
                required
              >
                <MenuItem value="AVAILABLE">사용가능</MenuItem>
                <MenuItem value="IN_USE">사용중</MenuItem>
                <MenuItem value="MAINTENANCE">보전중</MenuItem>
                <MenuItem value="BREAKDOWN">고장</MenuItem>
                <MenuItem value="RETIRED">폐기</MenuItem>
              </Select>
            </FormControl>
            <TextField
              label="비고"
              value={formData.remarks || ''}
              onChange={(e) => setFormData({ ...formData, remarks: e.target.value })}
              multiline
              rows={3}
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>취소</Button>
          <Button onClick={handleSubmit} variant="contained">
            {selectedMold ? '수정' : '등록'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={openDeleteDialog} onClose={() => setOpenDeleteDialog(false)}>
        <DialogTitle>금형 삭제</DialogTitle>
        <DialogContent>
          <Typography>
            {selectedMold?.moldName}을(를) 삭제하시겠습니까?
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenDeleteDialog(false)}>취소</Button>
          <Button onClick={handleDelete} color="error" variant="contained">
            삭제
          </Button>
        </DialogActions>
      </Dialog>

      {/* Snackbar */}
      <Snackbar
        open={snackbar.open}
        autoHideDuration={3000}
        onClose={() => setSnackbar({ ...snackbar, open: false })}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert severity={snackbar.severity} sx={{ width: '100%' }}>
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Box>
  );
};

export default MoldsPage;
