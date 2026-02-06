import React, { useState, useEffect } from 'react';
import {
  Box,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Snackbar,
  Alert,
  Chip,
  IconButton,
} from '@mui/material';
import { DataGrid, GridColDef, GridRenderCellParams } from '@mui/x-data-grid';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  CheckCircle as CheckCircleIcon,
  Cancel as CancelIcon,
} from '@mui/icons-material';
import warehouseService, { Warehouse, WarehouseCreateRequest, WarehouseUpdateRequest } from '../../services/warehouseService';
import userService, { User } from '../../services/userService';

const WarehousesPage: React.FC = () => {
  const [warehouses, setWarehouses] = useState<Warehouse[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(false);
  const [openDialog, setOpenDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [selectedWarehouse, setSelectedWarehouse] = useState<Warehouse | null>(null);
  const [formData, setFormData] = useState<Partial<WarehouseCreateRequest>>({
    warehouseType: 'RAW_MATERIAL',
    isActive: true,
  });
  const [snackbar, setSnackbar] = useState<{ open: boolean; message: string; severity: 'success' | 'error' }>({
    open: false,
    message: '',
    severity: 'success',
  });

  useEffect(() => {
    loadWarehouses();
    loadUsers();
  }, []);

  const loadWarehouses = async () => {
    try {
      setLoading(true);
      const data = await warehouseService.getAll();
      setWarehouses(data || []);
    } catch (error) {
      console.error('Failed to load warehouses:', error);
      setSnackbar({ open: true, message: '창고 목록 조회 실패', severity: 'error' });
      setWarehouses([]);
    } finally {
      setLoading(false);
    }
  };

  const loadUsers = async () => {
    try {
      const response = await userService.getUsers();
      setUsers(response?.content || []);
    } catch (error) {
      console.error('Failed to load users:', error);
      setUsers([]);
    }
  };

  const handleOpenDialog = (warehouse?: Warehouse) => {
    if (warehouse) {
      setSelectedWarehouse(warehouse);
      setFormData({
        warehouseCode: warehouse.warehouseCode,
        warehouseName: warehouse.warehouseName,
        warehouseType: warehouse.warehouseType,
        location: warehouse.location,
        managerUserId: warehouse.managerUserId,
        contactNumber: warehouse.contactNumber,
        capacity: warehouse.capacity,
        unit: warehouse.unit,
        isActive: warehouse.isActive,
        remarks: warehouse.remarks,
      });
    } else {
      setSelectedWarehouse(null);
      setFormData({
        warehouseType: 'RAW_MATERIAL',
        isActive: true,
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedWarehouse(null);
    setFormData({
      warehouseType: 'RAW_MATERIAL',
      isActive: true,
    });
  };

  const handleSubmit = async () => {
    try {
      if (selectedWarehouse) {
        const updateRequest: WarehouseUpdateRequest = {
          warehouseId: selectedWarehouse.warehouseId,
          warehouseName: formData.warehouseName!,
          warehouseType: formData.warehouseType!,
          location: formData.location,
          managerUserId: formData.managerUserId!,
          contactNumber: formData.contactNumber,
          capacity: formData.capacity,
          unit: formData.unit,
          isActive: formData.isActive!,
          remarks: formData.remarks,
        };
        await warehouseService.update(selectedWarehouse.warehouseId, updateRequest);
        setSnackbar({ open: true, message: '창고가 수정되었습니다', severity: 'success' });
      } else {
        await warehouseService.create(formData as WarehouseCreateRequest);
        setSnackbar({ open: true, message: '창고가 생성되었습니다', severity: 'success' });
      }
      handleCloseDialog();
      loadWarehouses();
    } catch (error) {
      console.error('Failed to save warehouse:', error);
      setSnackbar({ open: true, message: '창고 저장 실패', severity: 'error' });
    }
  };

  const handleDelete = async () => {
    if (!selectedWarehouse) return;

    try {
      await warehouseService.delete(selectedWarehouse.warehouseId);
      setSnackbar({ open: true, message: '창고가 삭제되었습니다', severity: 'success' });
      setOpenDeleteDialog(false);
      setSelectedWarehouse(null);
      loadWarehouses();
    } catch (error) {
      console.error('Failed to delete warehouse:', error);
      setSnackbar({ open: true, message: '창고 삭제 실패', severity: 'error' });
    }
  };

  const handleToggleActive = async (warehouse: Warehouse) => {
    try {
      await warehouseService.toggleActive(warehouse.warehouseId);
      setSnackbar({
        open: true,
        message: warehouse.isActive ? '창고가 비활성화되었습니다' : '창고가 활성화되었습니다',
        severity: 'success',
      });
      loadWarehouses();
    } catch (error) {
      console.error('Failed to toggle warehouse:', error);
      setSnackbar({ open: true, message: '창고 상태 변경 실패', severity: 'error' });
    }
  };

  const getWarehouseTypeLabel = (type: string) => {
    const types: { [key: string]: string } = {
      RAW_MATERIAL: '원자재',
      WORK_IN_PROCESS: '재공',
      FINISHED_GOODS: '완제품',
      QUARANTINE: '격리',
      SCRAP: '불량',
    };
    return types[type] || type;
  };

  const columns: GridColDef[] = [
    { field: 'warehouseCode', headerName: '창고 코드', width: 150 },
    { field: 'warehouseName', headerName: '창고명', width: 200 },
    {
      field: 'warehouseType',
      headerName: '창고 유형',
      width: 120,
      renderCell: (params: GridRenderCellParams) => getWarehouseTypeLabel(params.value as string),
    },
    { field: 'location', headerName: '위치', width: 180 },
    { field: 'managerUserName', headerName: '담당자', width: 120 },
    { field: 'contactNumber', headerName: '연락처', width: 150 },
    {
      field: 'capacity',
      headerName: '용량',
      width: 120,
      renderCell: (params: GridRenderCellParams) =>
        params.row.capacity ? `${params.row.capacity} ${params.row.unit || ''}` : '-',
    },
    {
      field: 'isActive',
      headerName: '상태',
      width: 100,
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
          <IconButton size="small" onClick={() => handleOpenDialog(params.row as Warehouse)}>
            <EditIcon fontSize="small" />
          </IconButton>
          <IconButton
            size="small"
            onClick={() => handleToggleActive(params.row as Warehouse)}
            color={params.row.isActive ? 'default' : 'success'}
          >
            {params.row.isActive ? <CancelIcon fontSize="small" /> : <CheckCircleIcon fontSize="small" />}
          </IconButton>
          <IconButton
            size="small"
            onClick={() => {
              setSelectedWarehouse(params.row as Warehouse);
              setOpenDeleteDialog(true);
            }}
          >
            <DeleteIcon fontSize="small" />
          </IconButton>
        </Box>
      ),
    },
  ];

  return (
    <Box sx={{ p: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
        <h2>창고 마스터</h2>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => handleOpenDialog()}>
          창고 생성
        </Button>
      </Box>

      <DataGrid
        rows={warehouses}
        columns={columns}
        loading={loading}
        getRowId={(row) => row.warehouseId}
        pageSizeOptions={[10, 25, 50, 100]}
        initialState={{
          pagination: { paginationModel: { pageSize: 25 } },
        }}
        sx={{ height: 600 }}
      />

      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
        <DialogTitle>{selectedWarehouse ? '창고 수정' : '창고 생성'}</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 2 }}>
            <TextField
              label="창고 코드"
              value={formData.warehouseCode || ''}
              onChange={(e) => setFormData({ ...formData, warehouseCode: e.target.value })}
              disabled={!!selectedWarehouse}
              required
              fullWidth
            />
            <TextField
              label="창고명"
              value={formData.warehouseName || ''}
              onChange={(e) => setFormData({ ...formData, warehouseName: e.target.value })}
              required
              fullWidth
            />
            <FormControl fullWidth required>
              <InputLabel>창고 유형</InputLabel>
              <Select
                value={formData.warehouseType || 'RAW_MATERIAL'}
                onChange={(e) => setFormData({ ...formData, warehouseType: e.target.value })}
                label="창고 유형"
              >
                <MenuItem value="RAW_MATERIAL">원자재</MenuItem>
                <MenuItem value="WORK_IN_PROCESS">재공</MenuItem>
                <MenuItem value="FINISHED_GOODS">완제품</MenuItem>
                <MenuItem value="QUARANTINE">격리</MenuItem>
                <MenuItem value="SCRAP">불량</MenuItem>
              </Select>
            </FormControl>
            <TextField
              label="위치"
              value={formData.location || ''}
              onChange={(e) => setFormData({ ...formData, location: e.target.value })}
              fullWidth
            />
            <FormControl fullWidth required>
              <InputLabel>담당자</InputLabel>
              <Select
                value={formData.managerUserId || ''}
                onChange={(e) => setFormData({ ...formData, managerUserId: Number(e.target.value) })}
                label="담당자"
              >
                {users.map((user) => (
                  <MenuItem key={user.userId} value={user.userId}>
                    {user.username}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
            <TextField
              label="연락처"
              value={formData.contactNumber || ''}
              onChange={(e) => setFormData({ ...formData, contactNumber: e.target.value })}
              fullWidth
            />
            <Box sx={{ display: 'flex', gap: 2 }}>
              <TextField
                label="용량"
                type="number"
                value={formData.capacity || ''}
                onChange={(e) => setFormData({ ...formData, capacity: Number(e.target.value) })}
                fullWidth
              />
              <TextField
                label="단위"
                value={formData.unit || ''}
                onChange={(e) => setFormData({ ...formData, unit: e.target.value })}
                fullWidth
              />
            </Box>
            <TextField
              label="비고"
              value={formData.remarks || ''}
              onChange={(e) => setFormData({ ...formData, remarks: e.target.value })}
              multiline
              rows={3}
              fullWidth
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>취소</Button>
          <Button onClick={handleSubmit} variant="contained">
            {selectedWarehouse ? '수정' : '생성'}
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={openDeleteDialog} onClose={() => setOpenDeleteDialog(false)}>
        <DialogTitle>창고 삭제</DialogTitle>
        <DialogContent>정말로 이 창고를 삭제하시겠습니까?</DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenDeleteDialog(false)}>취소</Button>
          <Button onClick={handleDelete} color="error" variant="contained">
            삭제
          </Button>
        </DialogActions>
      </Dialog>

      <Snackbar
        open={snackbar.open}
        autoHideDuration={3000}
        onClose={() => setSnackbar({ ...snackbar, open: false })}
      >
        <Alert severity={snackbar.severity}>{snackbar.message}</Alert>
      </Snackbar>
    </Box>
  );
};

export default WarehousesPage;
