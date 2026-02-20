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
  MenuItem,
  Grid,
} from '@mui/material';
import { DataGrid, GridColDef } from '@mui/x-data-grid';
import { Add as AddIcon, Edit as EditIcon, Delete as DeleteIcon } from '@mui/icons-material';
import employeeService, { Employee, EmployeeRequest } from '../../services/employeeService';

const EmployeesPage: React.FC = () => {
  const [employees, setEmployees] = useState<Employee[]>([]);
  const [loading, setLoading] = useState(false);
  const [openDialog, setOpenDialog] = useState(false);
  const [editingEmployee, setEditingEmployee] = useState<Employee | null>(null);
  const [formData, setFormData] = useState<EmployeeRequest>({
    employeeNo: '',
    employeeName: '',
    employmentStatus: 'ACTIVE',
    isActive: true,
  });

  useEffect(() => {
    loadEmployees();
  }, []);

  const loadEmployees = async () => {
    try {
      setLoading(true);
      const data = await employeeService.getAll();
      setEmployees(data || []);
    } catch (error) {
      setEmployees([]);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenDialog = (employee?: Employee) => {
    if (employee) {
      setEditingEmployee(employee);
      setFormData({
        employeeNo: employee.employeeNo,
        employeeName: employee.employeeName,
        departmentId: employee.departmentId,
        position: employee.position,
        jobGrade: employee.jobGrade,
        hireDate: employee.hireDate,
        phoneNumber: employee.phoneNumber,
        email: employee.email,
        employmentStatus: employee.employmentStatus,
        isActive: employee.isActive,
      });
    } else {
      setEditingEmployee(null);
      setFormData({
        employeeNo: '',
        employeeName: '',
        employmentStatus: 'ACTIVE',
        isActive: true,
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setEditingEmployee(null);
  };

  const handleSubmit = async () => {
    try {
      if (editingEmployee) {
        await employeeService.update(editingEmployee.id, formData);
      } else {
        await employeeService.create(formData);
      }
      handleCloseDialog();
      loadEmployees();
    } catch {
      // Failed to save employee
    }
  };

  const handleDelete = async (id: number) => {
    if (window.confirm('정말 삭제하시겠습니까?')) {
      try {
        await employeeService.delete(id);
        loadEmployees();
      } catch {
        // Failed to delete employee
      }
    }
  };

  const columns: GridColDef[] = [
    { field: 'employeeNo', headerName: '사원코드', width: 120 },
    { field: 'employeeName', headerName: '사원명', width: 120 },
    { field: 'departmentName', headerName: '부서', width: 150 },
    { field: 'position', headerName: '직위', width: 120 },
    { field: 'phoneNumber', headerName: '전화번호', width: 130 },
    { field: 'email', headerName: '이메일', width: 180 },
    {
      field: 'employmentStatus',
      headerName: '재직상태',
      width: 100,
      renderCell: (params) => {
        const statusMap: Record<string, string> = {
          ACTIVE: '재직',
          RESIGNED: '퇴사',
          LEAVE: '휴직',
        };
        return statusMap[params.value] || params.value;
      },
    },
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
        <Typography variant="h4">사원 관리</Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => handleOpenDialog()}>
          사원 추가
        </Button>
      </Box>

      <Paper>
        <DataGrid
          rows={employees}
          columns={columns}
          loading={loading}
          autoHeight
          pageSizeOptions={[10, 25, 50]}
          initialState={{
            pagination: { paginationModel: { pageSize: 10 } },
          }}
        />
      </Paper>

      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="md" fullWidth>
        <DialogTitle>{editingEmployee ? '사원 수정' : '사원 추가'}</DialogTitle>
        <DialogContent>
          <Box mt={1}>
            <Grid container spacing={2}>
              <Grid item xs={6}>
                <TextField
                  label="사원코드"
                  value={formData.employeeNo}
                  onChange={(e) => setFormData({ ...formData, employeeNo: e.target.value })}
                  required
                  fullWidth
                />
              </Grid>
              <Grid item xs={6}>
                <TextField
                  label="사원명"
                  value={formData.employeeName}
                  onChange={(e) => setFormData({ ...formData, employeeName: e.target.value })}
                  required
                  fullWidth
                />
              </Grid>
              <Grid item xs={6}>
                <TextField
                  label="부서 ID"
                  type="number"
                  value={formData.departmentId || ''}
                  onChange={(e) => setFormData({ ...formData, departmentId: e.target.value ? parseInt(e.target.value) : undefined })}
                  fullWidth
                />
              </Grid>
              <Grid item xs={6}>
                <TextField
                  label="직위"
                  value={formData.position || ''}
                  onChange={(e) => setFormData({ ...formData, position: e.target.value })}
                  fullWidth
                />
              </Grid>
              <Grid item xs={6}>
                <TextField
                  label="직급"
                  value={formData.jobGrade || ''}
                  onChange={(e) => setFormData({ ...formData, jobGrade: e.target.value })}
                  fullWidth
                />
              </Grid>
              <Grid item xs={6}>
                <TextField
                  label="입사일"
                  type="date"
                  value={formData.hireDate || ''}
                  onChange={(e) => setFormData({ ...formData, hireDate: e.target.value })}
                  InputLabelProps={{ shrink: true }}
                  fullWidth
                />
              </Grid>
              <Grid item xs={6}>
                <TextField
                  label="전화번호"
                  value={formData.phoneNumber || ''}
                  onChange={(e) => setFormData({ ...formData, phoneNumber: e.target.value })}
                  fullWidth
                />
              </Grid>
              <Grid item xs={6}>
                <TextField
                  label="이메일"
                  type="email"
                  value={formData.email || ''}
                  onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                  fullWidth
                />
              </Grid>
              <Grid item xs={6}>
                <TextField
                  select
                  label="재직상태"
                  value={formData.employmentStatus}
                  onChange={(e) => setFormData({ ...formData, employmentStatus: e.target.value })}
                  required
                  fullWidth
                >
                  <MenuItem value="ACTIVE">재직</MenuItem>
                  <MenuItem value="RESIGNED">퇴사</MenuItem>
                  <MenuItem value="LEAVE">휴직</MenuItem>
                </TextField>
              </Grid>
              <Grid item xs={6}>
                <FormControlLabel
                  control={
                    <Switch
                      checked={formData.isActive}
                      onChange={(e) => setFormData({ ...formData, isActive: e.target.checked })}
                    />
                  }
                  label="사용"
                />
              </Grid>
            </Grid>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>취소</Button>
          <Button onClick={handleSubmit} variant="contained">
            {editingEmployee ? '수정' : '추가'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default EmployeesPage;
