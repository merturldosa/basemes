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
  Alert,
  Snackbar,
} from '@mui/material';
import {
  DataGrid,
  GridColDef,
  GridActionsCellItem,
  GridRowParams,
} from '@mui/x-data-grid';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  ToggleOn as ToggleOnIcon,
  ToggleOff as ToggleOffIcon,
} from '@mui/icons-material';
import processService, { Process, ProcessCreateRequest, ProcessUpdateRequest } from '../../services/processService';

const ProcessesPage: React.FC = () => {
  const [processes, setProcesses] = useState<Process[]>([]);
  const [loading, setLoading] = useState(true);
  const [openDialog, setOpenDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [selectedProcess, setSelectedProcess] = useState<Process | null>(null);
  const [formData, setFormData] = useState<ProcessCreateRequest | ProcessUpdateRequest>({
    processCode: '',
    processName: '',
    processType: '',
    sequenceOrder: 1,
    remarks: '',
  });
  const [snackbar, setSnackbar] = useState<{ open: boolean; message: string; severity: 'success' | 'error' }>({
    open: false,
    message: '',
    severity: 'success',
  });

  useEffect(() => {
    loadProcesses();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const loadProcesses = async () => {
    try {
      setLoading(true);
      const data = await processService.getProcesses();
      setProcesses(data || []);
    } catch (error) {
      showSnackbar('공정 목록 조회 실패', 'error');
      setProcesses([]);
    } finally {
      setLoading(false);
    }
  };

  const showSnackbar = (message: string, severity: 'success' | 'error') => {
    setSnackbar({ open: true, message, severity });
  };

  const handleCloseSnackbar = () => {
    setSnackbar({ ...snackbar, open: false });
  };

  const handleOpenDialog = (process?: Process) => {
    if (process) {
      setSelectedProcess(process);
      setFormData({
        processName: process.processName,
        processType: process.processType || '',
        sequenceOrder: process.sequenceOrder,
        remarks: process.remarks || '',
      });
    } else {
      setSelectedProcess(null);
      setFormData({
        processCode: '',
        processName: '',
        processType: '',
        sequenceOrder: 1,
        remarks: '',
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedProcess(null);
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: name === 'sequenceOrder' && value ? Number(value) : value,
    });
  };

  const handleSubmit = async () => {
    try {
      if (selectedProcess) {
        await processService.updateProcess(selectedProcess.processId, formData as ProcessUpdateRequest);
        showSnackbar('공정 수정 성공', 'success');
      } else {
        await processService.createProcess(formData as ProcessCreateRequest);
        showSnackbar('공정 생성 성공', 'success');
      }
      handleCloseDialog();
      loadProcesses();
    } catch (error: any) {
      showSnackbar(error.response?.data?.message || '작업 실패', 'error');
    }
  };

  const handleToggleActive = async (process: Process) => {
    try {
      if (process.isActive) {
        await processService.deactivateProcess(process.processId);
        showSnackbar('공정 비활성화 성공', 'success');
      } else {
        await processService.activateProcess(process.processId);
        showSnackbar('공정 활성화 성공', 'success');
      }
      loadProcesses();
    } catch (error: any) {
      showSnackbar(error.response?.data?.message || '상태 변경 실패', 'error');
    }
  };

  const handleOpenDeleteDialog = (process: Process) => {
    setSelectedProcess(process);
    setOpenDeleteDialog(true);
  };

  const handleCloseDeleteDialog = () => {
    setOpenDeleteDialog(false);
    setSelectedProcess(null);
  };

  const handleDelete = async () => {
    if (!selectedProcess) return;

    try {
      await processService.deleteProcess(selectedProcess.processId);
      showSnackbar('공정 삭제 성공', 'success');
      handleCloseDeleteDialog();
      loadProcesses();
    } catch (error: any) {
      showSnackbar(error.response?.data?.message || '삭제 실패', 'error');
    }
  };

  const columns: GridColDef[] = [
    { field: 'sequenceOrder', headerName: '순서', width: 80 },
    { field: 'processCode', headerName: '공정 코드', width: 130 },
    { field: 'processName', headerName: '공정명', flex: 1, minWidth: 200 },
    { field: 'processType', headerName: '공정 유형', width: 150 },
    {
      field: 'isActive',
      headerName: '상태',
      width: 100,
      renderCell: (params) => (
        <Chip
          label={params.value ? '활성' : '비활성'}
          color={params.value ? 'success' : 'default'}
          size="small"
        />
      ),
    },
    {
      field: 'createdAt',
      headerName: '생성일',
      width: 180,
      valueFormatter: (params) => new Date(params.value).toLocaleString('ko-KR'),
    },
    {
      field: 'actions',
      type: 'actions',
      headerName: '작업',
      width: 150,
      getActions: (params: GridRowParams<Process>) => [
        <GridActionsCellItem
          icon={<EditIcon />}
          label="수정"
          onClick={() => handleOpenDialog(params.row)}
        />,
        <GridActionsCellItem
          icon={params.row.isActive ? <ToggleOffIcon /> : <ToggleOnIcon />}
          label={params.row.isActive ? '비활성화' : '활성화'}
          onClick={() => handleToggleActive(params.row)}
        />,
        <GridActionsCellItem
          icon={<DeleteIcon />}
          label="삭제"
          onClick={() => handleOpenDeleteDialog(params.row)}
        />,
      ],
    },
  ];

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h5" component="h1">
          공정 마스터 관리
        </Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => handleOpenDialog()}
        >
          신규 등록
        </Button>
      </Box>

      <Paper>
        <DataGrid
          rows={processes}
          columns={columns}
          getRowId={(row) => row.processId}
          loading={loading}
          autoHeight
          pageSizeOptions={[10, 25, 50, 100]}
          initialState={{
            pagination: { paginationModel: { pageSize: 25 } },
            sorting: {
              sortModel: [{ field: 'sequenceOrder', sort: 'asc' }],
            },
          }}
          disableRowSelectionOnClick
          sx={{
            '& .MuiDataGrid-cell': {
              borderBottom: '1px solid rgba(224, 224, 224, 1)',
            },
          }}
        />
      </Paper>

      {/* Create/Edit Dialog */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="md" fullWidth>
        <DialogTitle>{selectedProcess ? '공정 수정' : '신규 공정 등록'}</DialogTitle>
        <DialogContent>
          <Box display="flex" flexDirection="column" gap={2} mt={1}>
            {!selectedProcess && (
              <TextField
                name="processCode"
                label="공정 코드"
                value={(formData as ProcessCreateRequest).processCode || ''}
                onChange={handleInputChange}
                required
                fullWidth
              />
            )}
            <TextField
              name="processName"
              label="공정명"
              value={formData.processName || ''}
              onChange={handleInputChange}
              required
              fullWidth
            />
            <TextField
              name="processType"
              label="공정 유형"
              value={formData.processType || ''}
              onChange={handleInputChange}
              placeholder="예: 제조, 조립, 검사, 포장"
              fullWidth
            />
            <TextField
              name="sequenceOrder"
              label="공정 순서"
              type="number"
              value={formData.sequenceOrder || 1}
              onChange={handleInputChange}
              required
              fullWidth
              helperText="공정 진행 순서를 입력하세요 (낮은 숫자가 먼저 진행)"
            />
            <TextField
              name="remarks"
              label="비고"
              value={formData.remarks || ''}
              onChange={handleInputChange}
              multiline
              rows={3}
              fullWidth
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>취소</Button>
          <Button onClick={handleSubmit} variant="contained">
            {selectedProcess ? '수정' : '등록'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={openDeleteDialog} onClose={handleCloseDeleteDialog}>
        <DialogTitle>공정 삭제 확인</DialogTitle>
        <DialogContent>
          <Alert severity="warning" sx={{ mb: 2 }}>
            이 작업은 되돌릴 수 없습니다.
          </Alert>
          <Typography>
            공정 <strong>{selectedProcess?.processName}</strong>을(를) 삭제하시겠습니까?
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDeleteDialog}>취소</Button>
          <Button onClick={handleDelete} color="error" variant="contained">
            삭제
          </Button>
        </DialogActions>
      </Dialog>

      {/* Snackbar for notifications */}
      <Snackbar
        open={snackbar.open}
        autoHideDuration={3000}
        onClose={handleCloseSnackbar}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
      >
        <Alert onClose={handleCloseSnackbar} severity={snackbar.severity} sx={{ width: '100%' }}>
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Box>
  );
};

export default ProcessesPage;
