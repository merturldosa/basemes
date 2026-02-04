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
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
} from '@mui/material';
import { DataGrid, GridColDef, GridActionsCellItem } from '@mui/x-data-grid';
import {
  Add as AddIcon,
  Visibility as ViewIcon,
  Check as CheckIcon,
  LocalShipping as ShippingIcon,
} from '@mui/icons-material';
import deliveryService, { Delivery } from '../../services/deliveryService';

const DeliveriesPage: React.FC = () => {
  const [deliveries, setDeliveries] = useState<Delivery[]>([]);
  const [loading, setLoading] = useState(false);
  const [openViewDialog, setOpenViewDialog] = useState(false);
  const [selectedDelivery, setSelectedDelivery] = useState<Delivery | null>(null);
  const [snackbar, setSnackbar] = useState<{
    open: boolean;
    message: string;
    severity: 'success' | 'error' | 'info';
  }>({ open: false, message: '', severity: 'success' });

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const data = await deliveryService.getAll();
      setDeliveries(data);
    } catch (error) {
      setSnackbar({ open: true, message: 'Failed to load deliveries', severity: 'error' });
    } finally {
      setLoading(false);
    }
  };

  const handleOpenViewDialog = (delivery: Delivery) => {
    setSelectedDelivery(delivery);
    setOpenViewDialog(true);
  };

  const handleCloseViewDialog = () => {
    setOpenViewDialog(false);
    setSelectedDelivery(null);
  };

  const handleComplete = async (id: number) => {
    try {
      await deliveryService.complete(id);
      setSnackbar({ open: true, message: 'Delivery completed successfully', severity: 'success' });
      loadData();
    } catch (error) {
      setSnackbar({ open: true, message: 'Failed to complete delivery', severity: 'error' });
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'PENDING':
        return 'warning';
      case 'COMPLETED':
        return 'success';
      default:
        return 'default';
    }
  };

  const getStatusLabel = (status: string) => {
    const statuses: { [key: string]: string } = {
      PENDING: '대기중',
      COMPLETED: '완료',
    };
    return statuses[status] || status;
  };

  const getQualityStatusColor = (status?: string) => {
    if (!status) return 'default';
    switch (status) {
      case 'PENDING':
        return 'default';
      case 'INSPECTING':
        return 'info';
      case 'PASSED':
        return 'success';
      case 'FAILED':
        return 'error';
      default:
        return 'default';
    }
  };

  const getQualityStatusLabel = (status?: string) => {
    if (!status) return '-';
    const statuses: { [key: string]: string } = {
      PENDING: '대기',
      INSPECTING: '검사중',
      PASSED: '합격',
      FAILED: '불합격',
    };
    return statuses[status] || status;
  };

  const columns: GridColDef[] = [
    { field: 'deliveryNo', headerName: '출하번호', width: 150 },
    {
      field: 'deliveryDate',
      headerName: '출하일자',
      width: 180,
      valueFormatter: (params) => new Date(params).toLocaleString('ko-KR'),
    },
    { field: 'salesOrderNo', headerName: '판매주문번호', width: 150 },
    { field: 'customerName', headerName: '고객명', width: 200 },
    { field: 'warehouseName', headerName: '창고', width: 120 },
    {
      field: 'status',
      headerName: '상태',
      width: 100,
      renderCell: (params) => (
        <Chip label={getStatusLabel(params.value)} color={getStatusColor(params.value)} size="small" />
      ),
    },
    {
      field: 'qualityCheckStatus',
      headerName: '품질검사',
      width: 100,
      renderCell: (params) => (
        <Chip label={getQualityStatusLabel(params.value)} color={getQualityStatusColor(params.value)} size="small" />
      ),
    },
    { field: 'shipperName', headerName: '출하담당자', width: 120 },
    { field: 'shippingMethod', headerName: '배송방법', width: 100 },
    { field: 'trackingNo', headerName: '운송장번호', width: 150 },
    {
      field: 'actions',
      type: 'actions',
      headerName: '작업',
      width: 120,
      getActions: (params) => {
        const actions = [
          <GridActionsCellItem
            icon={<ViewIcon />}
            label="View"
            onClick={() => handleOpenViewDialog(params.row)}
          />,
        ];

        if (params.row.status === 'PENDING') {
          actions.push(
            <GridActionsCellItem
              icon={<CheckIcon />}
              label="Complete"
              onClick={() => handleComplete(params.row.deliveryId)}
            />
          );
        }

        return actions;
      },
    },
  ];

  return (
    <Box sx={{ height: '100%', p: 3 }}>
      <Paper sx={{ p: 2, mb: 2 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="h5">출하 관리</Typography>
        </Box>
      </Paper>

      <Paper sx={{ height: 'calc(100vh - 250px)' }}>
        <DataGrid
          rows={deliveries}
          columns={columns}
          loading={loading}
          getRowId={(row) => row.deliveryId}
          pageSizeOptions={[10, 25, 50, 100]}
          initialState={{
            pagination: { paginationModel: { pageSize: 25 } },
          }}
        />
      </Paper>

      {/* View Dialog */}
      <Dialog open={openViewDialog} onClose={handleCloseViewDialog} maxWidth="md" fullWidth>
        <DialogTitle>출하 상세</DialogTitle>
        <DialogContent>
          {selectedDelivery && (
            <Grid container spacing={2} sx={{ mt: 1 }}>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="text.secondary">출하번호</Typography>
                <Typography variant="body1">{selectedDelivery.deliveryNo}</Typography>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="text.secondary">상태</Typography>
                <Chip label={getStatusLabel(selectedDelivery.status)} color={getStatusColor(selectedDelivery.status)} size="small" />
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="text.secondary">판매주문번호</Typography>
                <Typography variant="body1">{selectedDelivery.salesOrderNo}</Typography>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="text.secondary">고객</Typography>
                <Typography variant="body1">{selectedDelivery.customerName}</Typography>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="text.secondary">창고</Typography>
                <Typography variant="body1">{selectedDelivery.warehouseName}</Typography>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="text.secondary">품질검사</Typography>
                <Chip label={getQualityStatusLabel(selectedDelivery.qualityCheckStatus)} color={getQualityStatusColor(selectedDelivery.qualityCheckStatus)} size="small" />
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="text.secondary">배송방법</Typography>
                <Typography variant="body1">{selectedDelivery.shippingMethod || '-'}</Typography>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="text.secondary">운송장번호</Typography>
                <Typography variant="body1">{selectedDelivery.trackingNo || '-'}</Typography>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="text.secondary">운송업체</Typography>
                <Typography variant="body1">{selectedDelivery.carrier || '-'}</Typography>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="text.secondary">출하담당자</Typography>
                <Typography variant="body1">{selectedDelivery.shipperName}</Typography>
              </Grid>
              <Grid item xs={12}>
                <Typography variant="h6" sx={{ mt: 2, mb: 1 }}>출하 상세</Typography>
                <TableContainer>
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>번호</TableCell>
                        <TableCell>품목</TableCell>
                        <TableCell>출하수량</TableCell>
                        <TableCell>단위</TableCell>
                        <TableCell>LOT</TableCell>
                        <TableCell>위치</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {selectedDelivery.items.map((item) => (
                        <TableRow key={item.deliveryItemId}>
                          <TableCell>{item.lineNo}</TableCell>
                          <TableCell>{item.productName || item.materialName}</TableCell>
                          <TableCell>{item.deliveredQuantity}</TableCell>
                          <TableCell>{item.unit}</TableCell>
                          <TableCell>{item.lotNo || '-'}</TableCell>
                          <TableCell>{item.location || '-'}</TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              </Grid>
            </Grid>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseViewDialog}>닫기</Button>
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

export default DeliveriesPage;
