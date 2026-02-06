import React, { useState, useEffect } from 'react';
import {
  Box,
  Button,
  Paper,
  Typography,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  IconButton,
  Switch,
  FormControlLabel,
} from '@mui/material';
import { DataGrid, GridColDef } from '@mui/x-data-grid';
import { Add as AddIcon, Edit as EditIcon, Delete as DeleteIcon } from '@mui/icons-material';
import departmentService, { Department, DepartmentRequest } from '../services/departmentService';

const DepartmentsPage: React.FC = () => {
  const [departments, setDepartments] = useState<Department[]>([]);
  const [loading, setLoading] = useState(false);
  const [openDialog, setOpenDialog] = useState(false);
  const [editingDepartment, setEditingDepartment] = useState<Department | null>(null);
  const [formData, setFormData] = useState<DepartmentRequest>({
    departmentCode: '',
    departmentName: '',
    description: '',
    sortOrder: 0,
    isActive: true,
  });

  useEffect(() => {
    loadDepartments();
  }, []);

  const loadDepartments = async () => {
    try {
      setLoading(true);
      const data = await departmentService.getAll();
      setDepartments(data || []);
    } catch (error) {
      console.error('Failed to load departments:', error);
      setDepartments([]);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenDialog = (department?: Department) => {
    if (department) {
      setEditingDepartment(department);
      setFormData({
        departmentCode: department.departmentCode,
        departmentName: department.departmentName,
        description: department.description || '',
        sortOrder: department.sortOrder || 0,
        isActive: department.isActive,
      });
    } else {
      setEditingDepartment(null);
      setFormData({
        departmentCode: '',
        departmentName: '',
        description: '',
        sortOrder: 0,
        isActive: true,
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setEditingDepartment(null);
  };

  const handleSubmit = async () => {
    try {
      if (editingDepartment) {
        await departmentService.update(editingDepartment.id, formData);
      } else {
        await departmentService.create(formData);
      }
      handleCloseDialog();
      loadDepartments();
    } catch (error) {
      console.error('Failed to save department:', error);
    }
  };

  const handleDelete = async (id: number) => {
    if (window.confirm('정말 삭제하시겠습니까?')) {
      try {
        await departmentService.delete(id);
        loadDepartments();
      } catch (error) {
        console.error('Failed to delete department:', error);
      }
    }
  };

  const columns: GridColDef[] = [
    { field: 'departmentCode', headerName: '부서코드', width: 150 },
    { field: 'departmentName', headerName: '부서명', width: 200 },
    { field: 'description', headerName: '설명', width: 300 },
    { field: 'sortOrder', headerName: '정렬순서', width: 100 },
    {
      field: 'isActive',
      headerName: '사용여부',
      width: 100,
      renderCell: (params) => (params.value ? '사용' : '미사용'),
    },
    {
      field: 'actions',
      headerName: '작업',
      width: 120,
      renderCell: (params) => (
        <>
          <IconButton size="small" onClick={() => handleOpenDialog(params.row)}>
            <EditIcon />
          </IconButton>
          <IconButton size="small" onClick={() => handleDelete(params.row.id)}>
            <DeleteIcon />
          </IconButton>
        </>
      ),
    },
  ];

  return (
    <Box p={3}>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4">부서 관리</Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => handleOpenDialog()}>
          부서 추가
        </Button>
      </Box>

      <Paper>
        <DataGrid
          rows={departments}
          columns={columns}
          loading={loading}
          autoHeight
          pageSizeOptions={[10, 25, 50]}
          initialState={{
            pagination: { paginationModel: { pageSize: 10 } },
          }}
        />
      </Paper>

      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
        <DialogTitle>{editingDepartment ? '부서 수정' : '부서 추가'}</DialogTitle>
        <DialogContent>
          <Box display="flex" flexDirection="column" gap={2} mt={1}>
            <TextField
              label="부서코드"
              value={formData.departmentCode}
              onChange={(e) => setFormData({ ...formData, departmentCode: e.target.value })}
              required
              fullWidth
            />
            <TextField
              label="부서명"
              value={formData.departmentName}
              onChange={(e) => setFormData({ ...formData, departmentName: e.target.value })}
              required
              fullWidth
            />
            <TextField
              label="설명"
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              multiline
              rows={3}
              fullWidth
            />
            <TextField
              label="정렬순서"
              type="number"
              value={formData.sortOrder}
              onChange={(e) => setFormData({ ...formData, sortOrder: parseInt(e.target.value) })}
              fullWidth
            />
            <FormControlLabel
              control={
                <Switch
                  checked={formData.isActive}
                  onChange={(e) => setFormData({ ...formData, isActive: e.target.checked })}
                />
              }
              label="사용"
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>취소</Button>
          <Button onClick={handleSubmit} variant="contained">
            {editingDepartment ? '수정' : '추가'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default DepartmentsPage;
