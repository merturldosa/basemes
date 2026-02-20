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
  ToggleOn as ToggleOnIcon,
  ToggleOff as ToggleOffIcon,
} from '@mui/icons-material';
import siteService, { Site, SiteRequest } from '../../services/siteService';

const SitesPage: React.FC = () => {
  const [sites, setSites] = useState<Site[]>([]);
  const [loading, setLoading] = useState(false);
  const [openDialog, setOpenDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [selectedSite, setSelectedSite] = useState<Site | null>(null);
  const [isEdit, setIsEdit] = useState(false);
  const [snackbar, setSnackbar] = useState<{
    open: boolean;
    message: string;
    severity: 'success' | 'error' | 'info';
  }>({ open: false, message: '', severity: 'success' });

  const [formData, setFormData] = useState<SiteRequest>({
    siteCode: '',
    siteName: '',
    address: '',
    postalCode: '',
    country: '',
    region: '',
    phone: '',
    fax: '',
    email: '',
    managerName: '',
    managerPhone: '',
    managerEmail: '',
    siteType: 'FACTORY',
    remarks: '',
  });

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const data = await siteService.getAll();
      setSites(data || []);
    } catch (error) {
      setSites([]);
      setSnackbar({ open: true, message: 'Failed to load sites', severity: 'error' });
    } finally {
      setLoading(false);
    }
  };

  const handleOpenDialog = (site?: Site) => {
    if (site) {
      setIsEdit(true);
      setSelectedSite(site);
      setFormData({
        siteCode: site.siteCode,
        siteName: site.siteName,
        address: site.address || '',
        postalCode: site.postalCode || '',
        country: site.country || '',
        region: site.region || '',
        phone: site.phone || '',
        fax: site.fax || '',
        email: site.email || '',
        managerName: site.managerName || '',
        managerPhone: site.managerPhone || '',
        managerEmail: site.managerEmail || '',
        siteType: site.siteType || 'FACTORY',
        remarks: site.remarks || '',
      });
    } else {
      setIsEdit(false);
      setSelectedSite(null);
      setFormData({
        siteCode: '',
        siteName: '',
        address: '',
        postalCode: '',
        country: '',
        region: '',
        phone: '',
        fax: '',
        email: '',
        managerName: '',
        managerPhone: '',
        managerEmail: '',
        siteType: 'FACTORY',
        remarks: '',
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedSite(null);
  };

  const handleOpenDeleteDialog = (site: Site) => {
    setSelectedSite(site);
    setOpenDeleteDialog(true);
  };

  const handleCloseDeleteDialog = () => {
    setOpenDeleteDialog(false);
    setSelectedSite(null);
  };

  const handleSubmit = async () => {
    try {
      if (isEdit && selectedSite) {
        await siteService.update(selectedSite.siteId, formData);
        setSnackbar({ open: true, message: 'Site updated successfully', severity: 'success' });
      } else {
        await siteService.create(formData);
        setSnackbar({ open: true, message: 'Site created successfully', severity: 'success' });
      }
      handleCloseDialog();
      loadData();
    } catch (error) {
      setSnackbar({ open: true, message: 'Failed to save site', severity: 'error' });
    }
  };

  const handleDelete = async () => {
    if (!selectedSite) return;

    try {
      await siteService.delete(selectedSite.siteId);
      setSnackbar({ open: true, message: 'Site deleted successfully', severity: 'success' });
      handleCloseDeleteDialog();
      loadData();
    } catch (error) {
      setSnackbar({ open: true, message: 'Failed to delete site', severity: 'error' });
    }
  };

  const handleToggleActive = async (id: number) => {
    try {
      await siteService.toggleActive(id);
      setSnackbar({ open: true, message: 'Site status updated successfully', severity: 'success' });
      loadData();
    } catch (error) {
      setSnackbar({ open: true, message: 'Failed to update site status', severity: 'error' });
    }
  };

  const columns: GridColDef[] = [
    { field: 'siteCode', headerName: '사업장코드', width: 150 },
    { field: 'siteName', headerName: '사업장명', width: 200 },
    {
      field: 'siteType',
      headerName: '유형',
      width: 120,
      valueFormatter: (params) => {
        const types: { [key: string]: string } = {
          FACTORY: '공장',
          WAREHOUSE: '창고',
          OFFICE: '사무실',
          RD_CENTER: '연구소',
        };
        return types[params.value] || params.value;
      },
    },
    { field: 'address', headerName: '주소', width: 250 },
    { field: 'phone', headerName: '전화번호', width: 130 },
    { field: 'managerName', headerName: '담당자', width: 120 },
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
          icon={params.row.isActive ? <ToggleOffIcon /> : <ToggleOnIcon />}
          label="Toggle"
          onClick={() => handleToggleActive(params.row.siteId)}
        />,
        <GridActionsCellItem
          icon={<DeleteIcon />}
          label="Delete"
          onClick={() => handleOpenDeleteDialog(params.row)}
        />,
      ],
    },
  ];

  return (
    <Box sx={{ height: '100%', p: 3 }}>
      <Paper sx={{ p: 2, mb: 2 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="h5">사업장 관리</Typography>
          <Button variant="contained" startIcon={<AddIcon />} onClick={() => handleOpenDialog()}>
            신규 사업장
          </Button>
        </Box>
      </Paper>

      <Paper sx={{ height: 'calc(100vh - 250px)' }}>
        <DataGrid
          rows={sites}
          columns={columns}
          loading={loading}
          getRowId={(row) => row.siteId}
          pageSizeOptions={[10, 25, 50, 100]}
          initialState={{
            pagination: { paginationModel: { pageSize: 25 } },
          }}
        />
      </Paper>

      {/* Create/Edit Dialog */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="md" fullWidth>
        <DialogTitle>{isEdit ? '사업장 수정' : '신규 사업장'}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="사업장코드"
                value={formData.siteCode}
                onChange={(e) => setFormData({ ...formData, siteCode: e.target.value })}
                required
                disabled={isEdit}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="사업장명"
                value={formData.siteName}
                onChange={(e) => setFormData({ ...formData, siteName: e.target.value })}
                required
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth>
                <InputLabel>유형</InputLabel>
                <Select
                  value={formData.siteType}
                  onChange={(e) => setFormData({ ...formData, siteType: e.target.value })}
                  label="유형"
                >
                  <MenuItem value="FACTORY">공장</MenuItem>
                  <MenuItem value="WAREHOUSE">창고</MenuItem>
                  <MenuItem value="OFFICE">사무실</MenuItem>
                  <MenuItem value="RD_CENTER">연구소</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="전화번호"
                value={formData.phone}
                onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="주소"
                value={formData.address}
                onChange={(e) => setFormData({ ...formData, address: e.target.value })}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="담당자명"
                value={formData.managerName}
                onChange={(e) => setFormData({ ...formData, managerName: e.target.value })}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="담당자 전화"
                value={formData.managerPhone}
                onChange={(e) => setFormData({ ...formData, managerPhone: e.target.value })}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="비고"
                multiline
                rows={3}
                value={formData.remarks}
                onChange={(e) => setFormData({ ...formData, remarks: e.target.value })}
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
        <DialogTitle>사업장 삭제</DialogTitle>
        <DialogContent>
          <Typography>정말 이 사업장을 삭제하시겠습니까?</Typography>
          {selectedSite && (
            <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
              사업장명: {selectedSite.siteName}
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

export default SitesPage;
