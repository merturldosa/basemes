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
} from '@mui/icons-material';
import moldProductionHistoryService, { MoldProductionHistory, MoldProductionHistoryCreateRequest, MoldProductionHistoryUpdateRequest } from '../../services/moldProductionHistoryService';
import moldService, { Mold } from '../../services/moldService';

const MoldProductionHistoriesPage: React.FC = () => {
  const [histories, setHistories] = useState<MoldProductionHistory[]>([]);
  const [molds, setMolds] = useState<Mold[]>([]);
  const [loading, setLoading] = useState(false);
  const [openDialog, setOpenDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [selectedHistory, setSelectedHistory] = useState<MoldProductionHistory | null>(null);
  const [formData, setFormData] = useState<MoldProductionHistoryCreateRequest>({
    moldId: 0,
    productionDate: new Date().toISOString().split('T')[0],
    shotCount: 0,
  });
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'success' as 'success' | 'error' | 'warning' | 'info',
  });

  useEffect(() => {
    loadHistories();
    loadMolds();
  }, []);

  const loadHistories = async () => {
    try {
      setLoading(true);
      const data = await moldProductionHistoryService.getAll();
      setHistories(data || []);
    } catch (error) {
      showSnackbar('생산 이력을 불러오는데 실패했습니다.', 'error');
      setHistories([]);
    } finally {
      setLoading(false);
    }
  };

  const loadMolds = async () => {
    try {
      const data = await moldService.getActive();
      setMolds(data || []);
    } catch (error) {
      console.error('Failed to load molds:', error);
      setMolds([]);
    }
  };

  const handleOpenDialog = (history?: MoldProductionHistory) => {
    if (history) {
      setSelectedHistory(history);
      setFormData({
        moldId: history.moldId,
        workOrderId: history.workOrderId,
        workResultId: history.workResultId,
        productionDate: history.productionDate,
        shotCount: history.shotCount,
        productionQuantity: history.productionQuantity,
        goodQuantity: history.goodQuantity,
        defectQuantity: history.defectQuantity,
        operatorName: history.operatorName,
        remarks: history.remarks,
      });
    } else {
      setSelectedHistory(null);
      setFormData({
        moldId: 0,
        productionDate: new Date().toISOString().split('T')[0],
        shotCount: 0,
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedHistory(null);
  };

  const handleSubmit = async () => {
    try {
      if (selectedHistory) {
        await moldProductionHistoryService.update(selectedHistory.historyId, formData as MoldProductionHistoryUpdateRequest);
        showSnackbar('생산 이력이 수정되었습니다.', 'success');
      } else {
        await moldProductionHistoryService.create(formData);
        showSnackbar('생산 이력이 등록되었습니다.', 'success');
      }
      handleCloseDialog();
      loadHistories();
    } catch (error) {
      showSnackbar('생산 이력 저장에 실패했습니다.', 'error');
    }
  };

  const handleDelete = async () => {
    if (selectedHistory) {
      try {
        await moldProductionHistoryService.delete(selectedHistory.historyId);
        showSnackbar('생산 이력이 삭제되었습니다.', 'success');
        setOpenDeleteDialog(false);
        setSelectedHistory(null);
        loadHistories();
      } catch (error) {
        showSnackbar('생산 이력 삭제에 실패했습니다.', 'error');
      }
    }
  };

  const showSnackbar = (message: string, severity: 'success' | 'error' | 'warning' | 'info') => {
    setSnackbar({ open: true, message, severity });
  };

  const columns: GridColDef[] = [
    { field: 'moldCode', headerName: '금형 코드', width: 120 },
    { field: 'moldName', headerName: '금형명', width: 150 },
    {
      field: 'productionDate',
      headerName: '생산 일자',
      width: 120,
      renderCell: (params: GridRenderCellParams) => {
        return new Date(params.value).toLocaleDateString('ko-KR');
      },
    },
    { field: 'workOrderNo', headerName: '작업지시번호', width: 140 },
    {
      field: 'shotCount',
      headerName: 'Shot 수',
      width: 100,
      renderCell: (params: GridRenderCellParams) => {
        return params.value.toLocaleString();
      },
    },
    {
      field: 'cumulativeShotCount',
      headerName: '누적 Shot',
      width: 110,
      renderCell: (params: GridRenderCellParams) => {
        return params.value ? params.value.toLocaleString() : '-';
      },
    },
    {
      field: 'productionQuantity',
      headerName: '생산 수량',
      width: 110,
      renderCell: (params: GridRenderCellParams) => {
        return params.value ? params.value.toLocaleString() : '-';
      },
    },
    {
      field: 'goodQuantity',
      headerName: '양품 수량',
      width: 110,
      renderCell: (params: GridRenderCellParams) => {
        return params.value ? params.value.toLocaleString() : '-';
      },
    },
    {
      field: 'defectQuantity',
      headerName: '불량 수량',
      width: 110,
      renderCell: (params: GridRenderCellParams) => {
        return params.value ? params.value.toLocaleString() : '-';
      },
    },
    { field: 'operatorName', headerName: '작업자', width: 100 },
    {
      field: 'actions',
      headerName: '작업',
      width: 120,
      sortable: false,
      renderCell: (params: GridRenderCellParams) => (
        <Box>
          <IconButton
            size="small"
            onClick={() => handleOpenDialog(params.row as MoldProductionHistory)}
            color="primary"
          >
            <EditIcon fontSize="small" />
          </IconButton>
          <IconButton
            size="small"
            onClick={() => {
              setSelectedHistory(params.row as MoldProductionHistory);
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
            금형 생산 이력
          </Typography>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => handleOpenDialog()}
          >
            생산 이력 등록
          </Button>
        </Box>

        <DataGrid
          rows={histories}
          columns={columns}
          loading={loading}
          getRowId={(row) => row.historyId}
          pageSizeOptions={[10, 25, 50, 100]}
          initialState={{
            pagination: { paginationModel: { pageSize: 25 } },
          }}
          autoHeight
          disableRowSelectionOnClick
        />
      </Paper>

      {/* Create/Edit Dialog */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
        <DialogTitle>{selectedHistory ? '생산 이력 수정' : '생산 이력 등록'}</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
            <FormControl required>
              <InputLabel>금형</InputLabel>
              <Select
                value={formData.moldId || ''}
                onChange={(e) => setFormData({ ...formData, moldId: Number(e.target.value) })}
                label="금형"
                disabled={!!selectedHistory}
              >
                {molds.map((mold) => (
                  <MenuItem key={mold.moldId} value={mold.moldId}>
                    {mold.moldCode} - {mold.moldName}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
            <TextField
              label="생산 일자"
              type="date"
              value={formData.productionDate}
              onChange={(e) => setFormData({ ...formData, productionDate: e.target.value })}
              required
              InputLabelProps={{ shrink: true }}
            />
            <TextField
              label="Shot 수"
              type="number"
              value={formData.shotCount}
              onChange={(e) => setFormData({ ...formData, shotCount: parseInt(e.target.value) || 0 })}
              required
            />
            <TextField
              label="생산 수량"
              type="number"
              value={formData.productionQuantity || ''}
              onChange={(e) => setFormData({ ...formData, productionQuantity: parseFloat(e.target.value) || undefined })}
            />
            <TextField
              label="양품 수량"
              type="number"
              value={formData.goodQuantity || ''}
              onChange={(e) => setFormData({ ...formData, goodQuantity: parseFloat(e.target.value) || undefined })}
            />
            <TextField
              label="불량 수량"
              type="number"
              value={formData.defectQuantity || ''}
              onChange={(e) => setFormData({ ...formData, defectQuantity: parseFloat(e.target.value) || undefined })}
            />
            <TextField
              label="작업자명"
              value={formData.operatorName || ''}
              onChange={(e) => setFormData({ ...formData, operatorName: e.target.value })}
            />
            <TextField
              label="비고"
              value={formData.remarks || ''}
              onChange={(e) => setFormData({ ...formData, remarks: e.target.value })}
              multiline
              rows={2}
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>취소</Button>
          <Button onClick={handleSubmit} variant="contained">
            {selectedHistory ? '수정' : '등록'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={openDeleteDialog} onClose={() => setOpenDeleteDialog(false)}>
        <DialogTitle>생산 이력 삭제</DialogTitle>
        <DialogContent>
          <Typography>
            선택한 생산 이력을 삭제하시겠습니까?
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

export default MoldProductionHistoriesPage;
