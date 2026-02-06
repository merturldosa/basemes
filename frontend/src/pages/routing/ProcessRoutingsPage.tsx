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
  Checkbox,
  FormControlLabel,
} from '@mui/material';
import { DataGrid, GridColDef, GridRenderCellParams } from '@mui/x-data-grid';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  CheckCircle as CheckCircleIcon,
  Cancel as CancelIcon,
  ContentCopy as CopyIcon,
  ArrowUpward as ArrowUpwardIcon,
  ArrowDownward as ArrowDownwardIcon,
} from '@mui/icons-material';
import processRoutingService, {
  ProcessRouting,
  RoutingCreateRequest,
  RoutingUpdateRequest,
  RoutingStep,
} from '../../services/processRoutingService';
import productService, { Product } from '../../services/productService';
import processService, { Process } from '../../services/processService';
import equipmentService, { Equipment } from '../../services/equipmentService';

const ProcessRoutingsPage: React.FC = () => {
  const [routings, setRoutings] = useState<ProcessRouting[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [processes, setProcesses] = useState<Process[]>([]);
  const [equipments, setEquipments] = useState<Equipment[]>([]);
  const [loading, setLoading] = useState(false);
  const [openDialog, setOpenDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [openCopyDialog, setOpenCopyDialog] = useState(false);
  const [selectedRouting, setSelectedRouting] = useState<ProcessRouting | null>(null);
  const [copyVersion, setCopyVersion] = useState('');
  const [formData, setFormData] = useState<Partial<RoutingCreateRequest>>({
    version: '1.0',
    isActive: true,
    steps: [],
  });
  const [snackbar, setSnackbar] = useState<{ open: boolean; message: string; severity: 'success' | 'error' }>({
    open: false,
    message: '',
    severity: 'success',
  });

  useEffect(() => {
    loadRoutings();
    loadProducts();
    loadProcesses();
    loadEquipments();
  }, []);

  const loadRoutings = async () => {
    try {
      setLoading(true);
      const data = await processRoutingService.getAll();
      setRoutings(data || []);
    } catch (error) {
      console.error('Failed to load routings:', error);
      setSnackbar({ open: true, message: '라우팅 목록 조회 실패', severity: 'error' });
      setRoutings([]);
    } finally {
      setLoading(false);
    }
  };

  const loadProducts = async () => {
    try {
      const data = await productService.getProducts();
      setProducts(data || []);
    } catch (error) {
      console.error('Failed to load products:', error);
      setProducts([]);
    }
  };

  const loadProcesses = async () => {
    try {
      const data = await processService.getProcesses();
      setProcesses(data || []);
    } catch (error) {
      console.error('Failed to load processes:', error);
      setProcesses([]);
    }
  };

  const loadEquipments = async () => {
    try {
      const data = await equipmentService.getAll();
      setEquipments(data || []);
    } catch (error) {
      console.error('Failed to load equipments:', error);
      setEquipments([]);
    }
  };

  const handleOpenDialog = (routing?: ProcessRouting) => {
    if (routing) {
      setSelectedRouting(routing);
      setFormData({
        productId: routing.productId,
        routingCode: routing.routingCode,
        routingName: routing.routingName,
        version: routing.version,
        effectiveDate: routing.effectiveDate,
        expiryDate: routing.expiryDate,
        isActive: routing.isActive,
        remarks: routing.remarks,
        steps: (routing.steps || []).map((s) => ({
          sequenceOrder: s.sequenceOrder,
          processId: s.processId,
          standardTime: s.standardTime,
          setupTime: s.setupTime,
          waitTime: s.waitTime,
          requiredWorkers: s.requiredWorkers,
          equipmentId: s.equipmentId,
          isParallel: s.isParallel,
          parallelGroup: s.parallelGroup,
          isOptional: s.isOptional,
          alternateProcessId: s.alternateProcessId,
          qualityCheckRequired: s.qualityCheckRequired,
          qualityStandard: s.qualityStandard,
          remarks: s.remarks,
        })),
      });
    } else {
      setSelectedRouting(null);
      setFormData({
        version: '1.0',
        isActive: true,
        steps: [],
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedRouting(null);
    setFormData({
      version: '1.0',
      isActive: true,
      steps: [],
    });
  };

  const handleAddStep = () => {
    const newStep: RoutingStep = {
      processId: 0,
      standardTime: 0,
      setupTime: 0,
      waitTime: 0,
      requiredWorkers: 1,
      isParallel: false,
      isOptional: false,
      qualityCheckRequired: false,
    };
    setFormData({
      ...formData,
      steps: [...(formData.steps || []), newStep],
    });
  };

  const handleUpdateStep = (index: number, field: keyof RoutingStep, value: any) => {
    const updatedSteps = [...(formData.steps || [])];
    updatedSteps[index] = { ...updatedSteps[index], [field]: value };
    setFormData({ ...formData, steps: updatedSteps });
  };

  const handleRemoveStep = (index: number) => {
    const updatedSteps = formData.steps?.filter((_, i) => i !== index) || [];
    setFormData({ ...formData, steps: updatedSteps });
  };

  const handleMoveStepUp = (index: number) => {
    if (index === 0) return;
    const updatedSteps = [...(formData.steps || [])];
    [updatedSteps[index - 1], updatedSteps[index]] = [updatedSteps[index], updatedSteps[index - 1]];
    setFormData({ ...formData, steps: updatedSteps });
  };

  const handleMoveStepDown = (index: number) => {
    if (!formData.steps || index === formData.steps.length - 1) return;
    const updatedSteps = [...formData.steps];
    [updatedSteps[index], updatedSteps[index + 1]] = [updatedSteps[index + 1], updatedSteps[index]];
    setFormData({ ...formData, steps: updatedSteps });
  };

  const handleSubmit = async () => {
    try {
      if (!formData.steps || formData.steps.length === 0) {
        setSnackbar({ open: true, message: '최소 하나의 공정을 추가해야 합니다', severity: 'error' });
        return;
      }

      if (selectedRouting) {
        const updateRequest: RoutingUpdateRequest = {
          routingId: selectedRouting.routingId,
          routingName: formData.routingName!,
          effectiveDate: formData.effectiveDate!,
          expiryDate: formData.expiryDate,
          isActive: formData.isActive!,
          remarks: formData.remarks,
          steps: formData.steps,
        };
        await processRoutingService.update(selectedRouting.routingId, updateRequest);
        setSnackbar({ open: true, message: '라우팅이 수정되었습니다', severity: 'success' });
      } else {
        await processRoutingService.create(formData as RoutingCreateRequest);
        setSnackbar({ open: true, message: '라우팅이 생성되었습니다', severity: 'success' });
      }
      handleCloseDialog();
      loadRoutings();
    } catch (error) {
      console.error('Failed to save routing:', error);
      setSnackbar({ open: true, message: '라우팅 저장 실패', severity: 'error' });
    }
  };

  const handleDelete = async () => {
    if (!selectedRouting) return;

    try {
      await processRoutingService.delete(selectedRouting.routingId);
      setSnackbar({ open: true, message: '라우팅이 삭제되었습니다', severity: 'success' });
      setOpenDeleteDialog(false);
      setSelectedRouting(null);
      loadRoutings();
    } catch (error) {
      console.error('Failed to delete routing:', error);
      setSnackbar({ open: true, message: '라우팅 삭제 실패', severity: 'error' });
    }
  };

  const handleToggleActive = async (routing: ProcessRouting) => {
    try {
      await processRoutingService.toggleActive(routing.routingId);
      setSnackbar({
        open: true,
        message: routing.isActive ? '라우팅이 비활성화되었습니다' : '라우팅이 활성화되었습니다',
        severity: 'success',
      });
      loadRoutings();
    } catch (error) {
      console.error('Failed to toggle routing:', error);
      setSnackbar({ open: true, message: '라우팅 상태 변경 실패', severity: 'error' });
    }
  };

  const handleCopyRouting = async () => {
    if (!selectedRouting || !copyVersion) return;

    try {
      await processRoutingService.copy(selectedRouting.routingId, copyVersion);
      setSnackbar({ open: true, message: '라우팅이 복사되었습니다', severity: 'success' });
      setOpenCopyDialog(false);
      setSelectedRouting(null);
      setCopyVersion('');
      loadRoutings();
    } catch (error) {
      console.error('Failed to copy routing:', error);
      setSnackbar({ open: true, message: '라우팅 복사 실패', severity: 'error' });
    }
  };

  const columns: GridColDef[] = [
    { field: 'routingCode', headerName: '라우팅 코드', width: 130 },
    { field: 'routingName', headerName: '라우팅 명', width: 200 },
    { field: 'version', headerName: '버전', width: 80 },
    { field: 'productCode', headerName: '제품 코드', width: 120 },
    { field: 'productName', headerName: '제품명', width: 180 },
    { field: 'effectiveDate', headerName: '유효 시작일', width: 120 },
    { field: 'expiryDate', headerName: '유효 종료일', width: 120 },
    {
      field: 'totalStandardTime',
      headerName: '총 시간(분)',
      width: 100,
      renderCell: (params: GridRenderCellParams) => params.value || 0,
    },
    {
      field: 'stepsCount',
      headerName: '공정 수',
      width: 80,
      renderCell: (params: GridRenderCellParams) => {
        const routing = params.row as ProcessRouting;
        return routing.steps?.length || 0;
      },
    },
    {
      field: 'isActive',
      headerName: '상태',
      width: 100,
      renderCell: (params: GridRenderCellParams) => (
        <Chip label={params.value ? '활성' : '비활성'} color={params.value ? 'success' : 'default'} size="small" />
      ),
    },
    {
      field: 'actions',
      headerName: '작업',
      width: 200,
      sortable: false,
      renderCell: (params: GridRenderCellParams) => (
        <Box>
          <IconButton size="small" onClick={() => handleOpenDialog(params.row as ProcessRouting)}>
            <EditIcon fontSize="small" />
          </IconButton>
          <IconButton
            size="small"
            onClick={() => handleToggleActive(params.row as ProcessRouting)}
            color={params.row.isActive ? 'default' : 'success'}
          >
            {params.row.isActive ? <CancelIcon fontSize="small" /> : <CheckCircleIcon fontSize="small" />}
          </IconButton>
          <IconButton
            size="small"
            onClick={() => {
              setSelectedRouting(params.row as ProcessRouting);
              setOpenCopyDialog(true);
            }}
          >
            <CopyIcon fontSize="small" />
          </IconButton>
          <IconButton
            size="small"
            onClick={() => {
              setSelectedRouting(params.row as ProcessRouting);
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
        <h2>공정 라우팅 관리</h2>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => handleOpenDialog()}>
          라우팅 생성
        </Button>
      </Box>

      <DataGrid
        rows={routings}
        columns={columns}
        loading={loading}
        getRowId={(row) => row.routingId}
        pageSizeOptions={[10, 25, 50, 100]}
        initialState={{
          pagination: { paginationModel: { pageSize: 25 } },
        }}
        sx={{ height: 600 }}
      />

      {/* Create/Edit Dialog */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="lg" fullWidth>
        <DialogTitle>{selectedRouting ? '라우팅 수정' : '라우팅 생성'}</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 2 }}>
            <FormControl fullWidth required disabled={!!selectedRouting}>
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
              label="라우팅 코드"
              value={formData.routingCode || ''}
              onChange={(e) => setFormData({ ...formData, routingCode: e.target.value })}
              disabled={!!selectedRouting}
              required
              fullWidth
            />
            <TextField
              label="라우팅 명"
              value={formData.routingName || ''}
              onChange={(e) => setFormData({ ...formData, routingName: e.target.value })}
              required
              fullWidth
            />
            <TextField
              label="버전"
              value={formData.version || ''}
              onChange={(e) => setFormData({ ...formData, version: e.target.value })}
              disabled={!!selectedRouting}
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
                <h4>공정 순서</h4>
                <Button size="small" startIcon={<AddIcon />} onClick={handleAddStep}>
                  공정 추가
                </Button>
              </Box>
              <TableContainer component={Paper} sx={{ maxHeight: 400 }}>
                <Table size="small" stickyHeader>
                  <TableHead>
                    <TableRow>
                      <TableCell width={40}>순서</TableCell>
                      <TableCell width={150}>공정</TableCell>
                      <TableCell width={80}>표준(분)</TableCell>
                      <TableCell width={80}>준비(분)</TableCell>
                      <TableCell width={80}>대기(분)</TableCell>
                      <TableCell width={70}>인원</TableCell>
                      <TableCell width={120}>설비</TableCell>
                      <TableCell width={80}>병렬</TableCell>
                      <TableCell width={80}>품질검사</TableCell>
                      <TableCell width={100}>작업</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {formData.steps?.map((step, index) => (
                      <TableRow key={index}>
                        <TableCell>{index + 1}</TableCell>
                        <TableCell>
                          <Select
                            value={step.processId || ''}
                            onChange={(e) => handleUpdateStep(index, 'processId', Number(e.target.value))}
                            size="small"
                            fullWidth
                          >
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
                            value={step.standardTime || ''}
                            onChange={(e) => handleUpdateStep(index, 'standardTime', Number(e.target.value))}
                            size="small"
                            fullWidth
                          />
                        </TableCell>
                        <TableCell>
                          <TextField
                            type="number"
                            value={step.setupTime || ''}
                            onChange={(e) => handleUpdateStep(index, 'setupTime', Number(e.target.value))}
                            size="small"
                            fullWidth
                          />
                        </TableCell>
                        <TableCell>
                          <TextField
                            type="number"
                            value={step.waitTime || ''}
                            onChange={(e) => handleUpdateStep(index, 'waitTime', Number(e.target.value))}
                            size="small"
                            fullWidth
                          />
                        </TableCell>
                        <TableCell>
                          <TextField
                            type="number"
                            value={step.requiredWorkers || ''}
                            onChange={(e) => handleUpdateStep(index, 'requiredWorkers', Number(e.target.value))}
                            size="small"
                            fullWidth
                          />
                        </TableCell>
                        <TableCell>
                          <Select
                            value={step.equipmentId || ''}
                            onChange={(e) =>
                              handleUpdateStep(index, 'equipmentId', Number(e.target.value) || undefined)
                            }
                            size="small"
                            fullWidth
                          >
                            <MenuItem value="">없음</MenuItem>
                            {equipments.map((equipment) => (
                              <MenuItem key={equipment.equipmentId} value={equipment.equipmentId}>
                                {equipment.equipmentCode}
                              </MenuItem>
                            ))}
                          </Select>
                        </TableCell>
                        <TableCell>
                          <Checkbox
                            checked={step.isParallel || false}
                            onChange={(e) => handleUpdateStep(index, 'isParallel', e.target.checked)}
                            size="small"
                          />
                        </TableCell>
                        <TableCell>
                          <Checkbox
                            checked={step.qualityCheckRequired || false}
                            onChange={(e) => handleUpdateStep(index, 'qualityCheckRequired', e.target.checked)}
                            size="small"
                          />
                        </TableCell>
                        <TableCell>
                          <IconButton size="small" onClick={() => handleMoveStepUp(index)} disabled={index === 0}>
                            <ArrowUpwardIcon fontSize="small" />
                          </IconButton>
                          <IconButton
                            size="small"
                            onClick={() => handleMoveStepDown(index)}
                            disabled={!formData.steps || index === formData.steps.length - 1}
                          >
                            <ArrowDownwardIcon fontSize="small" />
                          </IconButton>
                          <IconButton size="small" onClick={() => handleRemoveStep(index)}>
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
            {selectedRouting ? '수정' : '생성'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Dialog */}
      <Dialog open={openDeleteDialog} onClose={() => setOpenDeleteDialog(false)}>
        <DialogTitle>라우팅 삭제</DialogTitle>
        <DialogContent>정말로 이 라우팅을 삭제하시겠습니까?</DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenDeleteDialog(false)}>취소</Button>
          <Button onClick={handleDelete} color="error" variant="contained">
            삭제
          </Button>
        </DialogActions>
      </Dialog>

      {/* Copy Dialog */}
      <Dialog open={openCopyDialog} onClose={() => setOpenCopyDialog(false)}>
        <DialogTitle>라우팅 복사</DialogTitle>
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
          <Button onClick={handleCopyRouting} variant="contained">
            복사
          </Button>
        </DialogActions>
      </Dialog>

      <Snackbar open={snackbar.open} autoHideDuration={3000} onClose={() => setSnackbar({ ...snackbar, open: false })}>
        <Alert severity={snackbar.severity}>{snackbar.message}</Alert>
      </Snackbar>
    </Box>
  );
};

export default ProcessRoutingsPage;
