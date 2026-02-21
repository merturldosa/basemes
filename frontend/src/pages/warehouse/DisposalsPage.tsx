import React, { useState, useEffect } from 'react';
import {
    Box,
    Paper,
    Typography,
    Button,
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
    TextField,
    Grid,
    Chip,
    IconButton,
    MenuItem,
    Select,
    FormControl,
    InputLabel,
    Stack,
    Card,
    CardContent,
    Tooltip,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow
} from '@mui/material';
import {
    Add as AddIcon,
    Delete as DeleteIcon,
    CheckCircle as ApproveIcon,
    Cancel as RejectIcon,
    PlayArrow as ProcessIcon,
    Done as CompleteIcon,
    Close as CancelIcon,
    Visibility as ViewIcon
} from '@mui/icons-material';
import { DataGrid, GridColDef, GridRenderCellParams } from '@mui/x-data-grid';
import { useTranslation } from 'react-i18next';
import axios from 'axios';
import { useAuthStore } from '@/stores/authStore';

interface DisposalItem {
    disposalItemId?: number;
    productId: number;
    productCode?: string;
    productName?: string;
    productType?: string;
    unit?: string;
    lotId?: number;
    lotNo?: string;
    disposalQuantity: number;
    processedQuantity?: number;
    disposalTransactionId?: number;
    defectType?: string;
    defectDescription?: string;
    expiryDate?: string;
    remarks?: string;
    createdAt?: string;
    updatedAt?: string;
}

interface Disposal {
    disposalId: number;
    tenantId: string;
    tenantName: string;
    disposalNo: string;
    disposalDate: string;
    disposalType: string;
    disposalStatus: string;
    workOrderId?: number;
    workOrderNo?: string;
    requesterUserId: number;
    requesterUserName: string;
    requesterName?: string;
    warehouseId: number;
    warehouseCode: string;
    warehouseName: string;
    approverUserId?: number;
    approverUserName?: string;
    approverName?: string;
    approvedDate?: string;
    processorUserId?: number;
    processorUserName?: string;
    processorName?: string;
    processedDate?: string;
    completedDate?: string;
    disposalMethod?: string;
    disposalLocation?: string;
    totalDisposalQuantity: number;
    items: DisposalItem[];
    remarks?: string;
    rejectionReason?: string;
    cancellationReason?: string;
    isActive: boolean;
    createdAt: string;
    createdBy: string;
    updatedAt?: string;
    updatedBy?: string;
}

interface Statistics {
    total: number;
    pending: number;
    approved: number;
    processed: number;
    completed: number;
}

const DisposalsPage: React.FC = () => {
    const { t } = useTranslation();
    const { user } = useAuthStore();
    const [disposals, setDisposals] = useState<Disposal[]>([]);
    const [loading, setLoading] = useState(false);
    const [openCreate, setOpenCreate] = useState(false);
    const [openDetail, setOpenDetail] = useState(false);
    const [selectedDisposal, setSelectedDisposal] = useState<Disposal | null>(null);
    const [statistics, setStatistics] = useState<Statistics>({
        total: 0,
        pending: 0,
        approved: 0,
        processed: 0,
        completed: 0
    });

    // Form state
    const [formData, setFormData] = useState({
        disposalNo: '',
        disposalDate: new Date().toISOString().split('T')[0],
        disposalType: 'DEFECTIVE',
        workOrderId: '',
        requesterUserId: user?.userId ?? 0,
        warehouseId: 1,
        remarks: ''
    });

    const [items, setItems] = useState<DisposalItem[]>([{
        productId: 0,
        disposalQuantity: 0,
        defectType: '',
        defectDescription: '',
        expiryDate: '',
        remarks: ''
    }]);

    useEffect(() => {
        fetchDisposals();
        // eslint-disable-next-line react-hooks/exhaustive-deps -- load once on mount
    }, []);

    const fetchDisposals = async () => {
        setLoading(true);
        try {
            const response = await axios.get('/api/disposals');
            const data = response?.data?.data || [];
            setDisposals(data);
            calculateStatistics(data);
        } catch (error) {
            setDisposals([]);
        } finally {
            setLoading(false);
        }
    };

    const calculateStatistics = (data: Disposal[]) => {
        setStatistics({
            total: data.length,
            pending: data.filter(d => d.disposalStatus === 'PENDING').length,
            approved: data.filter(d => d.disposalStatus === 'APPROVED').length,
            processed: data.filter(d => d.disposalStatus === 'PROCESSED').length,
            completed: data.filter(d => d.disposalStatus === 'COMPLETED').length
        });
    };

    const handleCreate = async () => {
        try {
            const payload = {
                ...formData,
                disposalDate: new Date(formData.disposalDate).toISOString(),
                workOrderId: formData.workOrderId ? Number(formData.workOrderId) : null,
                items: items.map(item => ({
                    ...item,
                    expiryDate: item.expiryDate || null
                }))
            };

            await axios.post('/api/disposals', payload);
            setOpenCreate(false);
            resetForm();
            fetchDisposals();
        } catch (error) {
            alert(t('pages.disposals.messages.createFailed'));
        }
    };

    const handleApprove = async (disposalId: number) => {
        try {
            const approverId = user?.userId ?? 0;
            await axios.post(`/api/disposals/${disposalId}/approve?approverUserId=${approverId}`);
            fetchDisposals();
        } catch (error) {
            alert(t('pages.disposals.messages.approveFailed'));
        }
    };

    const handleReject = async (disposalId: number) => {
        const reason = prompt(t('pages.disposals.messages.enterRejectReason'));
        if (!reason) return;

        try {
            const approverId = user?.userId ?? 0;
            await axios.post(`/api/disposals/${disposalId}/reject?approverUserId=${approverId}&reason=${encodeURIComponent(reason)}`);
            fetchDisposals();
        } catch (error) {
            alert(t('pages.disposals.messages.rejectFailed'));
        }
    };

    const handleProcess = async (disposalId: number) => {
        if (!confirm(t('pages.disposals.messages.confirmProcess'))) return;

        try {
            const processorId = user?.userId ?? 0;
            await axios.post(`/api/disposals/${disposalId}/process?processorUserId=${processorId}`);
            fetchDisposals();
        } catch (error) {
            alert(t('pages.disposals.messages.processFailed'));
        }
    };

    const handleComplete = async (disposalId: number) => {
        const method = prompt(t('pages.disposals.messages.enterMethod'));
        const location = prompt(t('pages.disposals.messages.enterLocation'));
        if (!method || !location) return;

        try {
            await axios.post(`/api/disposals/${disposalId}/complete?method=${encodeURIComponent(method)}&location=${encodeURIComponent(location)}`);
            fetchDisposals();
        } catch (error) {
            alert(t('pages.disposals.messages.completeFailed'));
        }
    };

    const handleCancel = async (disposalId: number) => {
        const reason = prompt(t('pages.disposals.messages.enterCancelReason'));
        if (!reason) return;

        try {
            await axios.post(`/api/disposals/${disposalId}/cancel?reason=${encodeURIComponent(reason)}`);
            fetchDisposals();
        } catch (error) {
            alert(t('pages.disposals.messages.cancelFailed'));
        }
    };

    const handleViewDetail = async (disposalId: number) => {
        try {
            const response = await axios.get(`/api/disposals/${disposalId}`);
            setSelectedDisposal(response.data.data);
            setOpenDetail(true);
        } catch (error) {
            // Error silently handled - detail dialog will not open
        }
    };

    const resetForm = () => {
        setFormData({
            disposalNo: '',
            disposalDate: new Date().toISOString().split('T')[0],
            disposalType: 'DEFECTIVE',
            workOrderId: '',
            requesterUserId: user?.userId ?? 0,
            warehouseId: 1,
            remarks: ''
        });
        setItems([{
            productId: 0,
            disposalQuantity: 0,
            defectType: '',
            defectDescription: '',
            expiryDate: '',
            remarks: ''
        }]);
    };

    const addItem = () => {
        setItems([...items, {
            productId: 0,
            disposalQuantity: 0,
            defectType: '',
            defectDescription: '',
            expiryDate: '',
            remarks: ''
        }]);
    };

    const removeItem = (index: number) => {
        setItems(items.filter((_, i) => i !== index));
    };

    const updateItem = (index: number, field: keyof DisposalItem, value: string | number) => {
        const newItems = [...items];
        newItems[index] = { ...newItems[index], [field]: value };
        setItems(newItems);
    };

    const getStatusChip = (status: string) => {
        const statusConfig: { [key: string]: { label: string; color: 'default' | 'primary' | 'secondary' | 'error' | 'info' | 'success' | 'warning' } } = {
            PENDING: { label: t('pages.disposals.status.pending'), color: 'warning' },
            APPROVED: { label: t('pages.disposals.status.approved'), color: 'info' },
            REJECTED: { label: t('pages.disposals.status.rejected'), color: 'error' },
            PROCESSED: { label: t('pages.disposals.status.processed'), color: 'primary' },
            COMPLETED: { label: t('pages.disposals.status.completed'), color: 'success' },
            CANCELLED: { label: t('pages.disposals.status.cancelled'), color: 'default' }
        };
        const config = statusConfig[status] || { label: status, color: 'default' };
        return <Chip label={config.label} color={config.color} size="small" />;
    };

    const getTypeChip = (type: string) => {
        const typeConfig: { [key: string]: { label: string; color: 'default' | 'primary' | 'secondary' | 'error' | 'info' | 'success' | 'warning' } } = {
            DEFECTIVE: { label: t('pages.disposals.types.defective'), color: 'error' },
            EXPIRED: { label: t('pages.disposals.types.expired'), color: 'warning' },
            DAMAGED: { label: t('pages.disposals.types.damaged'), color: 'info' },
            OBSOLETE: { label: t('pages.disposals.types.obsolete'), color: 'default' },
            OTHER: { label: t('pages.disposals.types.other'), color: 'default' }
        };
        const config = typeConfig[type] || { label: type, color: 'default' };
        return <Chip label={config.label} color={config.color} size="small" />;
    };

    const columns: GridColDef[] = [
        { field: 'disposalNo', headerName: t('pages.disposals.fields.disposalNo'), width: 150 },
        {
            field: 'disposalDate',
            headerName: t('pages.disposals.fields.disposalDate'),
            width: 120,
            valueFormatter: (params) => new Date(params.value).toLocaleDateString('ko-KR')
        },
        {
            field: 'disposalType',
            headerName: t('pages.disposals.fields.disposalType'),
            width: 120,
            renderCell: (params: GridRenderCellParams) => getTypeChip(params.value)
        },
        {
            field: 'disposalStatus',
            headerName: t('common.labels.status'),
            width: 100,
            renderCell: (params: GridRenderCellParams) => getStatusChip(params.value)
        },
        { field: 'warehouseName', headerName: t('pages.disposals.fields.warehouse'), width: 130 },
        { field: 'requesterUserName', headerName: t('pages.disposals.fields.requester'), width: 100 },
        {
            field: 'totalDisposalQuantity',
            headerName: t('pages.disposals.fields.totalQuantity'),
            width: 100,
            type: 'number'
        },
        { field: 'approverUserName', headerName: t('pages.disposals.fields.approver'), width: 100 },
        { field: 'processorUserName', headerName: t('pages.disposals.fields.processor'), width: 100 },
        {
            field: 'actions',
            headerName: t('common.labels.actions'),
            width: 250,
            sortable: false,
            renderCell: (params: GridRenderCellParams) => {
                const disposal = params.row as Disposal;
                return (
                    <Stack direction="row" spacing={0.5}>
                        <Tooltip title={t('pages.disposals.actions.viewDetail')}>
                            <IconButton size="small" onClick={() => handleViewDetail(disposal.disposalId)}>
                                <ViewIcon fontSize="small" />
                            </IconButton>
                        </Tooltip>
                        {disposal.disposalStatus === 'PENDING' && (
                            <>
                                <Tooltip title={t('pages.disposals.actions.approve')}>
                                    <IconButton size="small" color="success" onClick={() => handleApprove(disposal.disposalId)}>
                                        <ApproveIcon fontSize="small" />
                                    </IconButton>
                                </Tooltip>
                                <Tooltip title={t('pages.disposals.actions.reject')}>
                                    <IconButton size="small" color="error" onClick={() => handleReject(disposal.disposalId)}>
                                        <RejectIcon fontSize="small" />
                                    </IconButton>
                                </Tooltip>
                                <Tooltip title={t('pages.disposals.actions.cancel')}>
                                    <IconButton size="small" onClick={() => handleCancel(disposal.disposalId)}>
                                        <CancelIcon fontSize="small" />
                                    </IconButton>
                                </Tooltip>
                            </>
                        )}
                        {disposal.disposalStatus === 'APPROVED' && (
                            <>
                                <Tooltip title={t('pages.disposals.actions.process')}>
                                    <IconButton size="small" color="primary" onClick={() => handleProcess(disposal.disposalId)}>
                                        <ProcessIcon fontSize="small" />
                                    </IconButton>
                                </Tooltip>
                                <Tooltip title={t('pages.disposals.actions.cancel')}>
                                    <IconButton size="small" onClick={() => handleCancel(disposal.disposalId)}>
                                        <CancelIcon fontSize="small" />
                                    </IconButton>
                                </Tooltip>
                            </>
                        )}
                        {disposal.disposalStatus === 'PROCESSED' && (
                            <Tooltip title={t('pages.disposals.actions.complete')}>
                                <IconButton size="small" color="success" onClick={() => handleComplete(disposal.disposalId)}>
                                    <CompleteIcon fontSize="small" />
                                </IconButton>
                            </Tooltip>
                        )}
                    </Stack>
                );
            }
        }
    ];

    return (
        <Box sx={{ p: 3 }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
                <Typography variant="h4">{t('pages.disposals.title')}</Typography>
                <Button
                    variant="contained"
                    startIcon={<AddIcon />}
                    onClick={() => setOpenCreate(true)}
                >
                    {t('pages.disposals.actions.create')}
                </Button>
            </Box>

            {/* Statistics Cards */}
            <Grid container spacing={2} sx={{ mb: 3 }}>
                <Grid item xs={12} sm={6} md={2.4}>
                    <Card>
                        <CardContent>
                            <Typography color="textSecondary" gutterBottom>{t('pages.disposals.stats.total')}</Typography>
                            <Typography variant="h5">{statistics.total}</Typography>
                        </CardContent>
                    </Card>
                </Grid>
                <Grid item xs={12} sm={6} md={2.4}>
                    <Card>
                        <CardContent>
                            <Typography color="textSecondary" gutterBottom>{t('pages.disposals.stats.pending')}</Typography>
                            <Typography variant="h5" color="warning.main">{statistics.pending}</Typography>
                        </CardContent>
                    </Card>
                </Grid>
                <Grid item xs={12} sm={6} md={2.4}>
                    <Card>
                        <CardContent>
                            <Typography color="textSecondary" gutterBottom>{t('pages.disposals.stats.approved')}</Typography>
                            <Typography variant="h5" color="info.main">{statistics.approved}</Typography>
                        </CardContent>
                    </Card>
                </Grid>
                <Grid item xs={12} sm={6} md={2.4}>
                    <Card>
                        <CardContent>
                            <Typography color="textSecondary" gutterBottom>{t('pages.disposals.stats.processed')}</Typography>
                            <Typography variant="h5" color="primary.main">{statistics.processed}</Typography>
                        </CardContent>
                    </Card>
                </Grid>
                <Grid item xs={12} sm={6} md={2.4}>
                    <Card>
                        <CardContent>
                            <Typography color="textSecondary" gutterBottom>{t('pages.disposals.stats.completed')}</Typography>
                            <Typography variant="h5" color="success.main">{statistics.completed}</Typography>
                        </CardContent>
                    </Card>
                </Grid>
            </Grid>

            {/* Data Grid */}
            <Paper sx={{ height: 600, width: '100%' }}>
                <DataGrid
                    rows={disposals}
                    columns={columns}
                    getRowId={(row) => row.disposalId}
                    loading={loading}
                    pageSizeOptions={[10, 25, 50]}
                    initialState={{
                        pagination: { paginationModel: { pageSize: 10 } }
                    }}
                />
            </Paper>

            {/* Create Dialog */}
            <Dialog open={openCreate} onClose={() => setOpenCreate(false)} maxWidth="md" fullWidth>
                <DialogTitle>{t('pages.disposals.actions.create')}</DialogTitle>
                <DialogContent>
                    <Grid container spacing={2} sx={{ mt: 1 }}>
                        <Grid item xs={12} sm={6}>
                            <TextField
                                fullWidth
                                label={t('pages.disposals.fields.disposalNo')}
                                value={formData.disposalNo}
                                onChange={(e) => setFormData({ ...formData, disposalNo: e.target.value })}
                                placeholder={t('pages.disposals.messages.autoGenerate')}
                            />
                        </Grid>
                        <Grid item xs={12} sm={6}>
                            <TextField
                                fullWidth
                                type="date"
                                label={t('pages.disposals.fields.disposalDate')}
                                value={formData.disposalDate}
                                onChange={(e) => setFormData({ ...formData, disposalDate: e.target.value })}
                                InputLabelProps={{ shrink: true }}
                            />
                        </Grid>
                        <Grid item xs={12} sm={6}>
                            <FormControl fullWidth>
                                <InputLabel>{t('pages.disposals.fields.disposalType')}</InputLabel>
                                <Select
                                    value={formData.disposalType}
                                    label={t('pages.disposals.fields.disposalType')}
                                    onChange={(e) => setFormData({ ...formData, disposalType: e.target.value })}
                                >
                                    <MenuItem value="DEFECTIVE">{t('pages.disposals.types.defective')}</MenuItem>
                                    <MenuItem value="EXPIRED">{t('pages.disposals.types.expired')}</MenuItem>
                                    <MenuItem value="DAMAGED">{t('pages.disposals.types.damaged')}</MenuItem>
                                    <MenuItem value="OBSOLETE">{t('pages.disposals.types.obsolete')}</MenuItem>
                                    <MenuItem value="OTHER">{t('pages.disposals.types.other')}</MenuItem>
                                </Select>
                            </FormControl>
                        </Grid>
                        <Grid item xs={12} sm={6}>
                            <TextField
                                fullWidth
                                type="number"
                                label={t('pages.disposals.fields.workOrderId')}
                                value={formData.workOrderId}
                                onChange={(e) => setFormData({ ...formData, workOrderId: e.target.value })}
                            />
                        </Grid>
                        <Grid item xs={12} sm={6}>
                            <TextField
                                fullWidth
                                type="number"
                                label={t('pages.disposals.fields.requesterId')}
                                value={formData.requesterUserId}
                                onChange={(e) => setFormData({ ...formData, requesterUserId: Number(e.target.value) })}
                            />
                        </Grid>
                        <Grid item xs={12} sm={6}>
                            <TextField
                                fullWidth
                                type="number"
                                label={t('pages.disposals.fields.warehouseId')}
                                value={formData.warehouseId}
                                onChange={(e) => setFormData({ ...formData, warehouseId: Number(e.target.value) })}
                            />
                        </Grid>
                        <Grid item xs={12}>
                            <TextField
                                fullWidth
                                multiline
                                rows={2}
                                label={t('common.labels.remarks')}
                                value={formData.remarks}
                                onChange={(e) => setFormData({ ...formData, remarks: e.target.value })}
                            />
                        </Grid>
                    </Grid>

                    <Box sx={{ mt: 3, mb: 2, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <Typography variant="h6">{t('pages.disposals.detail.items')}</Typography>
                        <Button startIcon={<AddIcon />} onClick={addItem}>{t('pages.disposals.actions.addItem')}</Button>
                    </Box>

                    {items.map((item, index) => (
                        <Paper key={index} sx={{ p: 2, mb: 2 }}>
                            <Grid container spacing={2}>
                                <Grid item xs={12} sm={6}>
                                    <TextField
                                        fullWidth
                                        type="number"
                                        label={t('pages.disposals.fields.productId')}
                                        value={item.productId || ''}
                                        onChange={(e) => updateItem(index, 'productId', Number(e.target.value))}
                                    />
                                </Grid>
                                <Grid item xs={12} sm={6}>
                                    <TextField
                                        fullWidth
                                        type="number"
                                        label={t('pages.disposals.fields.lotId')}
                                        value={item.lotId || ''}
                                        onChange={(e) => updateItem(index, 'lotId', e.target.value ? Number(e.target.value) : null)}
                                    />
                                </Grid>
                                <Grid item xs={12} sm={4}>
                                    <TextField
                                        fullWidth
                                        type="number"
                                        label={t('pages.disposals.fields.disposalQuantity')}
                                        value={item.disposalQuantity || ''}
                                        onChange={(e) => updateItem(index, 'disposalQuantity', Number(e.target.value))}
                                    />
                                </Grid>
                                <Grid item xs={12} sm={4}>
                                    <TextField
                                        fullWidth
                                        label={t('pages.disposals.fields.defectType')}
                                        value={item.defectType || ''}
                                        onChange={(e) => updateItem(index, 'defectType', e.target.value)}
                                    />
                                </Grid>
                                <Grid item xs={12} sm={4}>
                                    <TextField
                                        fullWidth
                                        type="date"
                                        label={t('pages.disposals.fields.expiryDate')}
                                        value={item.expiryDate || ''}
                                        onChange={(e) => updateItem(index, 'expiryDate', e.target.value)}
                                        InputLabelProps={{ shrink: true }}
                                    />
                                </Grid>
                                <Grid item xs={12}>
                                    <TextField
                                        fullWidth
                                        label={t('pages.disposals.fields.defectDescription')}
                                        value={item.defectDescription || ''}
                                        onChange={(e) => updateItem(index, 'defectDescription', e.target.value)}
                                    />
                                </Grid>
                                <Grid item xs={12}>
                                    <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                                        <TextField
                                            fullWidth
                                            label={t('common.labels.remarks')}
                                            value={item.remarks || ''}
                                            onChange={(e) => updateItem(index, 'remarks', e.target.value)}
                                            sx={{ mr: 1 }}
                                        />
                                        <IconButton color="error" onClick={() => removeItem(index)}>
                                            <DeleteIcon />
                                        </IconButton>
                                    </Box>
                                </Grid>
                            </Grid>
                        </Paper>
                    ))}
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setOpenCreate(false)}>{t('common.buttons.cancel')}</Button>
                    <Button variant="contained" onClick={handleCreate}>{t('common.buttons.add')}</Button>
                </DialogActions>
            </Dialog>

            {/* Detail Dialog */}
            <Dialog open={openDetail} onClose={() => setOpenDetail(false)} maxWidth="md" fullWidth>
                <DialogTitle>{t('pages.disposals.detail.title')}</DialogTitle>
                <DialogContent>
                    {selectedDisposal && (
                        <Box sx={{ mt: 2 }}>
                            <Grid container spacing={2}>
                                <Grid item xs={12} sm={6}>
                                    <Typography variant="body2" color="textSecondary">{t('pages.disposals.fields.disposalNo')}</Typography>
                                    <Typography variant="body1">{selectedDisposal.disposalNo}</Typography>
                                </Grid>
                                <Grid item xs={12} sm={6}>
                                    <Typography variant="body2" color="textSecondary">{t('common.labels.status')}</Typography>
                                    {getStatusChip(selectedDisposal.disposalStatus)}
                                </Grid>
                                <Grid item xs={12} sm={6}>
                                    <Typography variant="body2" color="textSecondary">{t('pages.disposals.fields.disposalType')}</Typography>
                                    {getTypeChip(selectedDisposal.disposalType)}
                                </Grid>
                                <Grid item xs={12} sm={6}>
                                    <Typography variant="body2" color="textSecondary">{t('pages.disposals.fields.disposalDate')}</Typography>
                                    <Typography variant="body1">
                                        {new Date(selectedDisposal.disposalDate).toLocaleString('ko-KR')}
                                    </Typography>
                                </Grid>
                                <Grid item xs={12} sm={6}>
                                    <Typography variant="body2" color="textSecondary">{t('pages.disposals.fields.warehouse')}</Typography>
                                    <Typography variant="body1">
                                        {selectedDisposal.warehouseCode} - {selectedDisposal.warehouseName}
                                    </Typography>
                                </Grid>
                                <Grid item xs={12} sm={6}>
                                    <Typography variant="body2" color="textSecondary">{t('pages.disposals.fields.requester')}</Typography>
                                    <Typography variant="body1">{selectedDisposal.requesterUserName}</Typography>
                                </Grid>
                                <Grid item xs={12} sm={6}>
                                    <Typography variant="body2" color="textSecondary">{t('pages.disposals.fields.approver')}</Typography>
                                    <Typography variant="body1">{selectedDisposal.approverUserName || '-'}</Typography>
                                </Grid>
                                <Grid item xs={12} sm={6}>
                                    <Typography variant="body2" color="textSecondary">{t('pages.disposals.fields.processor')}</Typography>
                                    <Typography variant="body1">{selectedDisposal.processorUserName || '-'}</Typography>
                                </Grid>
                                <Grid item xs={12} sm={6}>
                                    <Typography variant="body2" color="textSecondary">{t('pages.disposals.fields.disposalMethod')}</Typography>
                                    <Typography variant="body1">{selectedDisposal.disposalMethod || '-'}</Typography>
                                </Grid>
                                <Grid item xs={12} sm={6}>
                                    <Typography variant="body2" color="textSecondary">{t('pages.disposals.fields.disposalLocation')}</Typography>
                                    <Typography variant="body1">{selectedDisposal.disposalLocation || '-'}</Typography>
                                </Grid>
                                <Grid item xs={12}>
                                    <Typography variant="body2" color="textSecondary">{t('common.labels.remarks')}</Typography>
                                    <Typography variant="body1">{selectedDisposal.remarks || '-'}</Typography>
                                </Grid>
                                {selectedDisposal.rejectionReason && (
                                    <Grid item xs={12}>
                                        <Typography variant="body2" color="textSecondary">{t('pages.disposals.fields.rejectionReason')}</Typography>
                                        <Typography variant="body1" color="error">{selectedDisposal.rejectionReason}</Typography>
                                    </Grid>
                                )}
                                {selectedDisposal.cancellationReason && (
                                    <Grid item xs={12}>
                                        <Typography variant="body2" color="textSecondary">{t('pages.disposals.fields.cancellationReason')}</Typography>
                                        <Typography variant="body1" color="error">{selectedDisposal.cancellationReason}</Typography>
                                    </Grid>
                                )}
                            </Grid>

                            <Typography variant="h6" sx={{ mt: 3, mb: 2 }}>{t('pages.disposals.detail.items')}</Typography>
                            <TableContainer>
                                <Table size="small">
                                    <TableHead>
                                        <TableRow>
                                            <TableCell>{t('pages.disposals.detail.productCode')}</TableCell>
                                            <TableCell>{t('pages.disposals.detail.productName')}</TableCell>
                                            <TableCell>{t('pages.disposals.detail.lot')}</TableCell>
                                            <TableCell align="right">{t('pages.disposals.fields.disposalQuantity')}</TableCell>
                                            <TableCell>{t('pages.disposals.fields.defectType')}</TableCell>
                                            <TableCell>{t('pages.disposals.fields.defectDescription')}</TableCell>
                                        </TableRow>
                                    </TableHead>
                                    <TableBody>
                                        {selectedDisposal.items.map((item) => (
                                            <TableRow key={item.disposalItemId}>
                                                <TableCell>{item.productCode}</TableCell>
                                                <TableCell>{item.productName}</TableCell>
                                                <TableCell>{item.lotNo || '-'}</TableCell>
                                                <TableCell align="right">
                                                    {item.disposalQuantity} {item.unit}
                                                </TableCell>
                                                <TableCell>{item.defectType || '-'}</TableCell>
                                                <TableCell>{item.defectDescription || '-'}</TableCell>
                                            </TableRow>
                                        ))}
                                    </TableBody>
                                </Table>
                            </TableContainer>
                        </Box>
                    )}
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setOpenDetail(false)}>{t('common.buttons.close')}</Button>
                </DialogActions>
            </Dialog>
        </Box>
    );
};

export default DisposalsPage;
