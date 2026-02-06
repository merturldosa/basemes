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
  Snackbar,
  Alert,
  Chip,
  MenuItem,
  Select,
  FormControl,
  InputLabel,
  Grid,
} from '@mui/material';
import { DataGrid, GridColDef, GridActionsCellItem } from '@mui/x-data-grid';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
} from '@mui/icons-material';
import departmentService, { Department, DepartmentRequest } from '../../services/departmentService';

const DepartmentsPage: React.FC = () => {
  const [departments, setDepartments] = useState<Department[]>([]);
  const [loading, setLoading] = useState(false);
  const [openDialog, setOpenDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [selectedDepartment, setSelectedDepartment] = useState<Department | null>(null);
  const [isEdit, setIsEdit] = useState(false);
  const [snackbar, setSnackbar] = useState<{
    open: boolean;
    message: string;
    severity: 'success' | 'error' | 'info';
  }>({ open: false, message: '', severity: 'success' });

  const [formData, setFormData] = useState<DepartmentRequest>({
    departmentCode: '',
    departmentName: '',
    parentDepartmentId: undefined,
    description: '',
    sortOrder: 0,
    isActive: true,
  });

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const deptData = await departmentService.getAll();
      setDepartments(deptData || []);
    } catch (error) {
      setSnackbar({ open: true, message: 'Failed to load departments', severity: 'error' });
      setDepartments([]);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenDialog = (department?: Department) => {
    if (department) {
      setIsEdit(true);
      setSelectedDepartment(department);
      setFormData({
        departmentCode: department.departmentCode,
        departmentName: department.departmentName,
        parentDepartmentId: department.parentDepartmentId,
        description: department.description || '',
        sortOrder: department.sortOrder || 0,
        isActive: department.isActive,
      });
    } else {
      setIsEdit(false);
      setSelectedDepartment(null);
      setFormData({
        departmentCode: '',
        departmentName: '',
        parentDepartmentId: undefined,
        description: '',
        sortOrder: 0,
        isActive: true,
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedDepartment(null);
  };

  const handleOpenDeleteDialog = (department: Department) => {
    setSelectedDepartment(department);
    setOpenDeleteDialog(true);
  };

  const handleCloseDeleteDialog = () => {
    setOpenDeleteDialog(false);
    setSelectedDepartment(null);
  };

  const handleSubmit = async () => {
    try {
      if (isEdit && selectedDepartment) {
        await departmentService.update(selectedDepartment.id, formData);
        setSnackbar({ open: true, message: 'Department updated successfully', severity: 'success' });
      } else {
        await departmentService.create(formData);
        setSnackbar({ open: true, message: 'Department created successfully', severity: 'success' });
      }
      handleCloseDialog();
      loadData();
    } catch (error) {
      setSnackbar({ open: true, message: 'Failed to save department', severity: 'error' });
    }
  };

  const handleDelete = async () => {
    if (!selectedDepartment) return;

    try {
      await departmentService.delete(selectedDepartment.id);
      setSnackbar({ open: true, message: 'Department deleted successfully', severity: 'success' });
      handleCloseDeleteDialog();
      loadData();
    } catch (error) {
      setSnackbar({ open: true, message: 'Failed to delete department', severity: 'error' });
    }
  };


  const columns: GridColDef[] = [
    { field: 'departmentCode', headerName: '부서코드', width: 150 },
    { field: 'departmentName', headerName: '부서명', width: 200 },
    { field: 'parentDepartmentName', headerName: '상위부서', width: 200 },
    { field: 'description', headerName: '설명', width: 250 },
    { field: 'sortOrder', headerName: '정렬순서', width: 100, align: 'center' },
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
      field: 'actions',
      type: 'actions',
      headerName: '작업',
      width: 150,
      getActions: (params) => [
        <GridActionsCellItem
          icon={<EditIcon />}
          label="Edit"
          onClick={() => handleOpenDialog(params.row)}
        />,
        <GridActionsCellItem
          icon={<DeleteIcon />}
          label="Delete"
          onClick={() => handleOpenDeleteDialog(params.row)}
        />,
      ],
    },
  ];

  const topLevelDepartments = (departments || []).filter(d => !d.parentDepartmentId);

  return (
    <Box sx={{ height: '100%', p: 3 }}>
      <Paper sx={{ p: 2, mb: 2 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="h5">부서 관리</Typography>
          <Button variant="contained" startIcon={<AddIcon />} onClick={() => handleOpenDialog()}>
            신규 부서
          </Button>
        </Box>
      </Paper>

      <Paper sx={{ height: 'calc(100vh - 250px)' }}>
        <DataGrid
          rows={departments}
          columns={columns}
          loading={loading}
          getRowId={(row) => row.id}
          pageSizeOptions={[10, 25, 50, 100]}
          initialState={{
            pagination: { paginationModel: { pageSize: 25 } },
          }}
        />
      </Paper>

      {/* Create/Edit Dialog */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="md" fullWidth>
        <DialogTitle>{isEdit ? '부서 수정' : '신규 부서'}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="부서코드"
                value={formData.departmentCode}
                onChange={(e) => setFormData({ ...formData, departmentCode: e.target.value })}
                required
                disabled={isEdit}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="부서명"
                value={formData.departmentName}
                onChange={(e) => setFormData({ ...formData, departmentName: e.target.value })}
                required
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth>
                <InputLabel>상위 부서</InputLabel>
                <Select
                  value={formData.parentDepartmentId || ''}
                  onChange={(e) => setFormData({ ...formData, parentDepartmentId: e.target.value as number })}
                  label="상위 부서"
                >
                  <MenuItem value="">
                    <em>최상위 부서</em>
                  </MenuItem>
                  {topLevelDepartments.map((dept) => (
                    <MenuItem key={dept.id} value={dept.id}>
                      {dept.departmentName}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="정렬 순서"
                type="number"
                value={formData.sortOrder}
                onChange={(e) => setFormData({ ...formData, sortOrder: parseInt(e.target.value) || 0 })}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="설명"
                multiline
                rows={3}
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>취소</Button>
          <Button onClick={handleSubmit} variant="contained">
            {isEdit ? '수정' : '생성'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={openDeleteDialog} onClose={handleCloseDeleteDialog}>
        <DialogTitle>부서 삭제</DialogTitle>
        <DialogContent>
          <Typography>정말 이 부서를 삭제하시겠습니까?</Typography>
          {selectedDepartment && (
            <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
              부서명: {selectedDepartment.departmentName}
            </Typography>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDeleteDialog}>취소</Button>
          <Button onClick={handleDelete} color="error" variant="contained">
            삭제
          </Button>
        </DialogActions>
      </Dialog>

      <Snackbar
        open={snackbar.open}
        autoHideDuration={6000}
        onClose={() => setSnackbar({ ...snackbar, open: false })}
      >
        <Alert severity={snackbar.severity} onClose={() => setSnackbar({ ...snackbar, open: false })}>
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Box>
  );
};

export default DepartmentsPage;
