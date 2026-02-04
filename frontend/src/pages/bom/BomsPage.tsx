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
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
} from '@mui/material';
import { DataGrid, GridColDef, GridRenderCellParams } from '@mui/x-data-grid';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  CheckCircle as CheckCircleIcon,
  Cancel as CancelIcon,
  ContentCopy as CopyIcon,
} from '@mui/icons-material';
import bomService, { Bom, BomCreateRequest, BomUpdateRequest, BomDetail } from '../../services/bomService';
import productService, { Product } from '../../services/productService';
import processService, { Process } from '../../services/processService';

const BomsPage: React.FC = () => {
  const [boms, setBoms] = useState<Bom[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [processes, setProcesses] = useState<Process[]>([]);
  const [loading, setLoading] = useState(false);
  const [openDialog, setOpenDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [openCopyDialog, setOpenCopyDialog] = useState(false);
  const [selectedBom, setSelectedBom] = useState<Bom | null>(null);
  const [copyVersion, setCopyVersion] = useState('');
  const [formData, setFormData] = useState<Partial<BomCreateRequest>>({
    version: '1.0',
    isActive: true,
    details: [],
  });
  const [snackbar, setSnackbar] = useState<{ open: boolean; message: string; severity: 'success' | 'error' }>({
    open: false,
    message: '',
    severity: 'success',
  });

  useEffect(() => {
    loadBoms();
    loadProducts();
    loadProcesses();
  }, []);

  const loadBoms = async () => {
    try {
      setLoading(true);
      const data = await bomService.getAll();
      setBoms(data);
    } catch (error) {
      console.error('Failed to load BOMs:', error);
      setSnackbar({ open: true, message: 'BOM 목록 조회 실패', severity: 'error' });
    } finally {
      setLoading(false);
    }
  };

  const loadProducts = async () => {
    try {
      const data = await productService.getAll();
      setProducts(data);
    } catch (error) {
      console.error('Failed to load products:', error);
    }
  };

  const loadProcesses = async () => {
    try {
      const data = await processService.getAll();
      setProcesses(data);
    } catch (error) {
      console.error('Failed to load processes:', error);
    }
  };

  const handleOpenDialog = (bom?: Bom) => {
    if (bom) {
      setSelectedBom(bom);
      setFormData({
        productId: bom.productId,
        bomCode: bom.bomCode,
        bomName: bom.bomName,
        version: bom.version,
        effectiveDate: bom.effectiveDate,
        expiryDate: bom.expiryDate,
        isActive: bom.isActive,
        remarks: bom.remarks,
        details: bom.details.map(d => ({
          sequence: d.sequence,
          materialProductId: d.materialProductId,
          processId: d.processId,
          quantity: d.quantity,
          unit: d.unit,
          usageRate: d.usageRate,
          scrapRate: d.scrapRate,
          remarks: d.remarks,
        })),
      });
    } else {
      setSelectedBom(null);
      setFormData({
        version: '1.0',
        isActive: true,
        details: [],
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedBom(null);
    setFormData({
      version: '1.0',
      isActive: true,
      details: [],
    });
  };

  const handleAddDetail = () => {
    const newDetail: BomDetail = {
      materialProductId: 0,
      quantity: 0,
      unit: 'EA',
      usageRate: 100,
      scrapRate: 0,
    };
    setFormData({
      ...formData,
      details: [...(formData.details || []), newDetail],
    });
  };

  const handleUpdateDetail = (index: number, field: keyof BomDetail, value: any) => {
    const updatedDetails = [...(formData.details || [])];
    updatedDetails[index] = { ...updatedDetails[index], [field]: value };
    setFormData({ ...formData, details: updatedDetails });
  };

  const handleRemoveDetail = (index: number) => {
    const updatedDetails = formData.details?.filter((_, i) => i !== index) || [];
    setFormData({ ...formData, details: updatedDetails });
  };

  const handleSubmit = async () => {
    try {
      if (!formData.details || formData.details.length === 0) {
        setSnackbar({ open: true, message: '최소 하나의 BOM 상세를 추가해야 합니다', severity: 'error' });
        return;
      }

      if (selectedBom) {
        const updateRequest: BomUpdateRequest = {
          bomId: selectedBom.bomId,
          bomName: formData.bomName!,
          effectiveDate: formData.effectiveDate!,
          expiryDate: formData.expiryDate,
          isActive: formData.isActive!,
          remarks: formData.remarks,
          details: formData.details,
        };
        await bomService.update(selectedBom.bomId, updateRequest);
        setSnackbar({ open: true, message: 'BOM이 수정되었습니다', severity: 'success' });
      } else {
        await bomService.create(formData as BomCreateRequest);
        setSnackbar({ open: true, message: 'BOM이 생성되었습니다', severity: 'success' });
      }
      handleCloseDialog();
      loadBoms();
    } catch (error) {
      console.error('Failed to save BOM:', error);
      setSnackbar({ open: true, message: 'BOM 저장 실패', severity: 'error' });
    }
  };

  const handleDelete = async () => {
    if (!selectedBom) return;

    try {
      await bomService.delete(selectedBom.bomId);
      setSnackbar({ open: true, message: 'BOM이 삭제되었습니다', severity: 'success' });
      setOpenDeleteDialog(false);
      setSelectedBom(null);
      loadBoms();
    } catch (error) {
      console.error('Failed to delete BOM:', error);
      setSnackbar({ open: true, message: 'BOM 삭제 실패', severity: 'error' });
    }
  };

  const handleToggleActive = async (bom: Bom) => {
    try {
      await bomService.toggleActive(bom.bomId);
      setSnackbar({
        open: true,
        message: bom.isActive ? 'BOM이 비활성화되었습니다' : 'BOM이 활성화되었습니다',
        severity: 'success',
      });
      loadBoms();
    } catch (error) {
      console.error('Failed to toggle BOM:', error);
      setSnackbar({ open: true, message: 'BOM 상태 변경 실패', severity: 'error' });
    }
  };

  const handleCopyBom = async () => {
    if (!selectedBom || !copyVersion) return;

    try {
      await bomService.copy(selectedBom.bomId, copyVersion);
      setSnackbar({ open: true, message: 'BOM이 복사되었습니다', severity: 'success' });
      setOpenCopyDialog(false);
      setSelectedBom(null);
      setCopyVersion('');
      loadBoms();
    } catch (error) {
      console.error('Failed to copy BOM:', error);
      setSnackbar({ open: true, message: 'BOM 복사 실패', severity: 'error' });
    }
  };

  const columns: GridColDef[] = [
    { field: 'bomCode', headerName: 'BOM 코드', width: 130 },
    { field: 'bomName', headerName: 'BOM 명', width: 200 },
    { field: 'version', headerName: '버전', width: 80 },
    { field: 'productCode', headerName: '제품 코드', width: 120 },
    { field: 'productName', headerName: '제품명', width: 180 },
    { field: 'effectiveDate', headerName: '유효 시작일', width: 120 },
    { field: 'expiryDate', headerName: '유효 종료일', width: 120 },
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
      width: 200,
      sortable: false,
      renderCell: (params: GridRenderCellParams) => (
        <Box>
          <IconButton size="small" onClick={() => handleOpenDialog(params.row as Bom)}>
            <EditIcon fontSize="small" />
          </IconButton>
          <IconButton
            size="small"
            onClick={() => handleToggleActive(params.row as Bom)}
            color={params.row.isActive ? 'default' : 'success'}
          >
            {params.row.isActive ? <CancelIcon fontSize="small" /> : <CheckCircleIcon fontSize="small" />}
          </IconButton>
          <IconButton
            size="small"
            onClick={() => {
              setSelectedBom(params.row as Bom);
              setOpenCopyDialog(true);
            }}
          >
            <CopyIcon fontSize="small" />
          </IconButton>
          <IconButton
            size="small"
            onClick={() => {
              setSelectedBom(params.row as Bom);
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
        <h2>BOM 관리</h2>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => handleOpenDialog()}>
          BOM 생성
        </Button>
      </Box>

      <DataGrid
        rows={boms}
        columns={columns}
        loading={loading}
        getRowId={(row) => row.bomId}
        pageSizeOptions={[10, 25, 50, 100]}
        initialState={{
          pagination: { paginationModel: { pageSize: 25 } },
        }}
        sx={{ height: 600 }}
      />

      {/* Create/Edit Dialog */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="md" fullWidth>
        <DialogTitle>{selectedBom ? 'BOM 수정' : 'BOM 생성'}</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 2 }}>
            <FormControl fullWidth required disabled={!!selectedBom}>
              <InputLabel>제품</InputLabel>
              <Select
                value={formData.productId || ''}
                onChange={(e) => setFormData({ ...formData, productId: Number(e.target.value) })}
                label="제품"
              >
                {products.map((product) => (
                  <MenuItem key={product.productId} value={product.productId}>
                    {product.productCode} - {product.productName}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
            <TextField
              label="BOM 코드"
              value={formData.bomCode || ''}
              onChange={(e) => setFormData({ ...formData, bomCode: e.target.value })}
              disabled={!!selectedBom}
              required
              fullWidth
            />
            <TextField
              label="BOM 명"
              value={formData.bomName || ''}
              onChange={(e) => setFormData({ ...formData, bomName: e.target.value })}
              required
              fullWidth
            />
            <TextField
              label="버전"
              value={formData.version || ''}
              onChange={(e) => setFormData({ ...formData, version: e.target.value })}
              disabled={!!selectedBom}
              required
              fullWidth
            />
            <TextField
              label="유효 시작일"
              type="date"
              value={formData.effectiveDate || ''}
              onChange={(e) => setFormData({ ...formData, effectiveDate: e.target.value })}
              InputLabelProps={{ shrink: true }}
              required
              fullWidth
            />
            <TextField
              label="유효 종료일"
              type="date"
              value={formData.expiryDate || ''}
              onChange={(e) => setFormData({ ...formData, expiryDate: e.target.value })}
              InputLabelProps={{ shrink: true }}
              fullWidth
            />
            <TextField
              label="비고"
              value={formData.remarks || ''}
              onChange={(e) => setFormData({ ...formData, remarks: e.target.value })}
              multiline
              rows={2}
              fullWidth
            />

            <Box>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
                <h4>BOM 상세</h4>
                <Button size="small" startIcon={<AddIcon />} onClick={handleAddDetail}>
                  상세 추가
                </Button>
              </Box>
              <TableContainer component={Paper}>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>자재</TableCell>
                      <TableCell>공정</TableCell>
                      <TableCell>수량</TableCell>
                      <TableCell>단위</TableCell>
                      <TableCell>사용율(%)</TableCell>
                      <TableCell>스크랩율(%)</TableCell>
                      <TableCell>작업</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {formData.details?.map((detail, index) => (
                      <TableRow key={index}>
                        <TableCell>
                          <Select
                            value={detail.materialProductId || ''}
                            onChange={(e) => handleUpdateDetail(index, 'materialProductId', Number(e.target.value))}
                            size="small"
                            fullWidth
                          >
                            {products.map((product) => (
                              <MenuItem key={product.productId} value={product.productId}>
                                {product.productCode}
                              </MenuItem>
                            ))}
                          </Select>
                        </TableCell>
                        <TableCell>
                          <Select
                            value={detail.processId || ''}
                            onChange={(e) => handleUpdateDetail(index, 'processId', Number(e.target.value) || undefined)}
                            size="small"
                            fullWidth
                          >
                            <MenuItem value="">없음</MenuItem>
                            {processes.map((process) => (
                              <MenuItem key={process.processId} value={process.processId}>
                                {process.processCode}
                              </MenuItem>
                            ))}
                          </Select>
                        </TableCell>
                        <TableCell>
                          <TextField
                            type="number"
                            value={detail.quantity || ''}
                            onChange={(e) => handleUpdateDetail(index, 'quantity', Number(e.target.value))}
                            size="small"
                            fullWidth
                          />
                        </TableCell>
                        <TableCell>
                          <TextField
                            value={detail.unit || ''}
                            onChange={(e) => handleUpdateDetail(index, 'unit', e.target.value)}
                            size="small"
                            fullWidth
                          />
                        </TableCell>
                        <TableCell>
                          <TextField
                            type="number"
                            value={detail.usageRate || ''}
                            onChange={(e) => handleUpdateDetail(index, 'usageRate', Number(e.target.value))}
                            size="small"
                            fullWidth
                          />
                        </TableCell>
                        <TableCell>
                          <TextField
                            type="number"
                            value={detail.scrapRate || ''}
                            onChange={(e) => handleUpdateDetail(index, 'scrapRate', Number(e.target.value))}
                            size="small"
                            fullWidth
                          />
                        </TableCell>
                        <TableCell>
                          <IconButton size="small" onClick={() => handleRemoveDetail(index)}>
                            <DeleteIcon fontSize="small" />
                          </IconButton>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            </Box>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>취소</Button>
          <Button onClick={handleSubmit} variant="contained">
            {selectedBom ? '수정' : '생성'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Dialog */}
      <Dialog open={openDeleteDialog} onClose={() => setOpenDeleteDialog(false)}>
        <DialogTitle>BOM 삭제</DialogTitle>
        <DialogContent>정말로 이 BOM을 삭제하시겠습니까?</DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenDeleteDialog(false)}>취소</Button>
          <Button onClick={handleDelete} color="error" variant="contained">
            삭제
          </Button>
        </DialogActions>
      </Dialog>

      {/* Copy Dialog */}
      <Dialog open={openCopyDialog} onClose={() => setOpenCopyDialog(false)}>
        <DialogTitle>BOM 복사</DialogTitle>
        <DialogContent>
          <TextField
            label="새 버전"
            value={copyVersion}
            onChange={(e) => setCopyVersion(e.target.value)}
            fullWidth
            sx={{ mt: 2 }}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenCopyDialog(false)}>취소</Button>
          <Button onClick={handleCopyBom} variant="contained">
            복사
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

export default BomsPage;
