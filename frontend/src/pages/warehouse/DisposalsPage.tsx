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
import axios from 'axios';

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
        requesterUserId: 1,
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
    }, []);

    const fetchDisposals = async () => {
        setLoading(true);
        try {
            const response = await axios.get('/api/disposals');
            const data = response?.data?.data || [];
            setDisposals(data);
            calculateStatistics(data);
        } catch (error) {
            console.error('Failed to fetch disposals:', error);
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
            console.error('Failed to create disposal:', error);
            alert('폐기 생성 실패');
        }
    };

    const handleApprove = async (disposalId: number) => {
        try {
            const approverId = 1; // TODO: Get from auth context
            await axios.post(`/api/disposals/${disposalId}/approve?approverUserId=${approverId}`);
            fetchDisposals();
        } catch (error) {
            console.error('Failed to approve disposal:', error);
            alert('폐기 승인 실패');
        }
    };

    const handleReject = async (disposalId: number) => {
        const reason = prompt('거부 사유를 입력하세요:');
        if (!reason) return;

        try {
            const approverId = 1; // TODO: Get from auth context
            await axios.post(`/api/disposals/${disposalId}/reject?approverUserId=${approverId}&reason=${encodeURIComponent(reason)}`);
            fetchDisposals();
        } catch (error) {
            console.error('Failed to reject disposal:', error);
            alert('폐기 거부 실패');
        }
    };

    const handleProcess = async (disposalId: number) => {
        if (!confirm('폐기를 처리하시겠습니까? 재고가 차감됩니다.')) return;

        try {
            const processorId = 1; // TODO: Get from auth context
            await axios.post(`/api/disposals/${disposalId}/process?processorUserId=${processorId}`);
            fetchDisposals();
        } catch (error) {
            console.error('Failed to process disposal:', error);
            alert('폐기 처리 실패');
        }
    };

    const handleComplete = async (disposalId: number) => {
        const method = prompt('폐기 방법을 입력하세요 (예: 소각, 매립, 위탁처리):');
        const location = prompt('폐기 장소를 입력하세요:');
        if (!method || !location) return;

        try {
            await axios.post(`/api/disposals/${disposalId}/complete?method=${encodeURIComponent(method)}&location=${encodeURIComponent(location)}`);
            fetchDisposals();
        } catch (error) {
            console.error('Failed to complete disposal:', error);
            alert('폐기 완료 실패');
        }
    };

    const handleCancel = async (disposalId: number) => {
        const reason = prompt('취소 사유를 입력하세요:');
        if (!reason) return;

        try {
            await axios.post(`/api/disposals/${disposalId}/cancel?reason=${encodeURIComponent(reason)}`);
            fetchDisposals();
        } catch (error) {
            console.error('Failed to cancel disposal:', error);
            alert('폐기 취소 실패');
        }
    };

    const handleViewDetail = async (disposalId: number) => {
        try {
            const response = await axios.get(`/api/disposals/${disposalId}`);
            setSelectedDisposal(response.data.data);
            setOpenDetail(true);
        } catch (error) {
            console.error('Failed to fetch disposal detail:', error);
        }
    };

    const resetForm = () => {
        setFormData({
            disposalNo: '',
            disposalDate: new Date().toISOString().split('T')[0],
            disposalType: 'DEFECTIVE',
            workOrderId: '',
            requesterUserId: 1,
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

    const updateItem = (index: number, field: keyof DisposalItem, value: any) => {
        const newItems = [...items];
        newItems[index] = { ...newItems[index], [field]: value };
        setItems(newItems);
    };

    const getStatusChip = (status: string) => {
        const statusConfig: { [key: string]: { label: string; color: any } } = {
            PENDING: { label: '대기', color: 'warning' },
            APPROVED: { label: '승인', color: 'info' },
            REJECTED: { label: '거부', color: 'error' },
            PROCESSED: { label: '처리됨', color: 'primary' },
            COMPLETED: { label: '완료', color: 'success' },
            CANCELLED: { label: '취소', color: 'default' }
        };
        const config = statusConfig[status] || { label: status, color: 'default' };
        return <Chip label={config.label} color={config.color} size="small" />;
    };

    const getTypeChip = (type: string) => {
        const typeConfig: { [key: string]: { label: string; color: any } } = {
            DEFECTIVE: { label: '불량', color: 'error' },
            EXPIRED: { label: '유효기간', color: 'warning' },
            DAMAGED: { label: '파손', color: 'info' },
            OBSOLETE: { label: '폐기대상', color: 'default' },
            OTHER: { label: '기타', color: 'default' }
        };
        const config = typeConfig[type] || { label: type, color: 'default' };
        return <Chip label={config.label} color={config.color} size="small" />;
    };

    const columns: GridColDef[] = [
        { field: 'disposalNo', headerName: '폐기번호', width: 150 },
        {
            field: 'disposalDate',
            headerName: '폐기일자',
            width: 120,
            valueFormatter: (params) => new Date(params.value).toLocaleDateString('ko-KR')
        },
        {
            field: 'disposalType',
            headerName: '폐기유형',
            width: 120,
            renderCell: (params: GridRenderCellParams) => getTypeChip(params.value)
        },
        {
            field: 'disposalStatus',
            headerName: '상태',
            width: 100,
            renderCell: (params: GridRenderCellParams) => getStatusChip(params.value)
        },
        { field: 'warehouseName', headerName: '창고', width: 130 },
        { field: 'requesterUserName', headerName: '요청자', width: 100 },
        {
            field: 'totalDisposalQuantity',
            headerName: '총수량',
            width: 100,
            type: 'number'
        },
        { field: 'approverUserName', headerName: '승인자', width: 100 },
        { field: 'processorUserName', headerName: '처리자', width: 100 },
        {
            field: 'actions',
            headerName: '작업',
            width: 250,
            sortable: false,
            renderCell: (params: GridRenderCellParams) => {
                const disposal = params.row as Disposal;
                return (
                    <Stack direction="row" spacing={0.5}>
                        <Tooltip title="상세보기">
                            <IconButton size="small" onClick={() => handleViewDetail(disposal.disposalId)}>
                                <ViewIcon fontSize="small" />
                            </IconButton>
                        </Tooltip>
                        {disposal.disposalStatus === 'PENDING' && (
                            <>
                                <Tooltip title="승인">
                                    <IconButton size="small" color="success" onClick={() => handleApprove(disposal.disposalId)}>
                                        <ApproveIcon fontSize="small" />
                                    </IconButton>
                                </Tooltip>
                                <Tooltip title="거부">
                                    <IconButton size="small" color="error" onClick={() => handleReject(disposal.disposalId)}>
                                        <RejectIcon fontSize="small" />
                                    </IconButton>
                                </Tooltip>
                                <Tooltip title="취소">
                                    <IconButton size="small" onClick={() => handleCancel(disposal.disposalId)}>
                                        <CancelIcon fontSize="small" />
                                    </IconButton>
                                </Tooltip>
                            </>
                        )}
                        {disposal.disposalStatus === 'APPROVED' && (
                            <>
                                <Tooltip title="처리">
                                    <IconButton size="small" color="primary" onClick={() => handleProcess(disposal.disposalId)}>
                                        <ProcessIcon fontSize="small" />
                                    </IconButton>
                                </Tooltip>
                                <Tooltip title="취소">
                                    <IconButton size="small" onClick={() => handleCancel(disposal.disposalId)}>
                                        <CancelIcon fontSize="small" />
                                    </IconButton>
                                </Tooltip>
                            </>
                        )}
                        {disposal.disposalStatus === 'PROCESSED' && (
                            <Tooltip title="완료">
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
                <Typography variant="h4">폐기 관리</Typography>
                <Button
                    variant="contained"
                    startIcon={<AddIcon />}
                    onClick={() => setOpenCreate(true)}
                >
                    폐기 생성
                </Button>
            </Box>

            {/* Statistics Cards */}
            <Grid container spacing={2} sx={{ mb: 3 }}>
                <Grid item xs={12} sm={6} md={2.4}>
                    <Card>
                        <CardContent>
                            <Typography color="textSecondary" gutterBottom>전체</Typography>
                            <Typography variant="h5">{statistics.total}</Typography>
                        </CardContent>
                    </Card>
                </Grid>
                <Grid item xs={12} sm={6} md={2.4}>
                    <Card>
                        <CardContent>
                            <Typography color="textSecondary" gutterBottom>대기</Typography>
                            <Typography variant="h5" color="warning.main">{statistics.pending}</Typography>
                        </CardContent>
                    </Card>
                </Grid>
                <Grid item xs={12} sm={6} md={2.4}>
                    <Card>
                        <CardContent>
                            <Typography color="textSecondary" gutterBottom>승인</Typography>
                            <Typography variant="h5" color="info.main">{statistics.approved}</Typography>
                        </CardContent>
                    </Card>
                </Grid>
                <Grid item xs={12} sm={6} md={2.4}>
                    <Card>
                        <CardContent>
                            <Typography color="textSecondary" gutterBottom>처리됨</Typography>
                            <Typography variant="h5" color="primary.main">{statistics.processed}</Typography>
                        </CardContent>
                    </Card>
                </Grid>
                <Grid item xs={12} sm={6} md={2.4}>
                    <Card>
                        <CardContent>
                            <Typography color="textSecondary" gutterBottom>완료</Typography>
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
                <DialogTitle>폐기 생성</DialogTitle>
                <DialogContent>
                    <Grid container spacing={2} sx={{ mt: 1 }}>
                        <Grid item xs={12} sm={6}>
                            <TextField
                                fullWidth
                                label="폐기번호"
                                value={formData.disposalNo}
                                onChange={(e) => setFormData({ ...formData, disposalNo: e.target.value })}
                                placeholder="자동 생성"
                            />
                        </Grid>
                        <Grid item xs={12} sm={6}>
                            <TextField
                                fullWidth
                                type="date"
                                label="폐기일자"
                                value={formData.disposalDate}
                                onChange={(e) => setFormData({ ...formData, disposalDate: e.target.value })}
                                InputLabelProps={{ shrink: true }}
                            />
                        </Grid>
                        <Grid item xs={12} sm={6}>
                            <FormControl fullWidth>
                                <InputLabel>폐기유형</InputLabel>
                                <Select
                                    value={formData.disposalType}
                                    label="폐기유형"
                                    onChange={(e) => setFormData({ ...formData, disposalType: e.target.value })}
                                >
                                    <MenuItem value="DEFECTIVE">불량</MenuItem>
                                    <MenuItem value="EXPIRED">유효기간</MenuItem>
                                    <MenuItem value="DAMAGED">파손</MenuItem>
                                    <MenuItem value="OBSOLETE">폐기대상</MenuItem>
                                    <MenuItem value="OTHER">기타</MenuItem>
                                </Select>
                            </FormControl>
                        </Grid>
                        <Grid item xs={12} sm={6}>
                            <TextField
                                fullWidth
                                type="number"
                                label="작업지시 ID (선택)"
                                value={formData.workOrderId}
                                onChange={(e) => setFormData({ ...formData, workOrderId: e.target.value })}
                            />
                        </Grid>
                        <Grid item xs={12} sm={6}>
                            <TextField
                                fullWidth
                                type="number"
                                label="요청자 ID"
                                value={formData.requesterUserId}
                                onChange={(e) => setFormData({ ...formData, requesterUserId: Number(e.target.value) })}
                            />
                        </Grid>
                        <Grid item xs={12} sm={6}>
                            <TextField
                                fullWidth
                                type="number"
                                label="창고 ID"
                                value={formData.warehouseId}
                                onChange={(e) => setFormData({ ...formData, warehouseId: Number(e.target.value) })}
                            />
                        </Grid>
                        <Grid item xs={12}>
                            <TextField
                                fullWidth
                                multiline
                                rows={2}
                                label="비고"
                                value={formData.remarks}
                                onChange={(e) => setFormData({ ...formData, remarks: e.target.value })}
                            />
                        </Grid>
                    </Grid>

                    <Box sx={{ mt: 3, mb: 2, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <Typography variant="h6">폐기 항목</Typography>
                        <Button startIcon={<AddIcon />} onClick={addItem}>항목 추가</Button>
                    </Box>

                    {items.map((item, index) => (
                        <Paper key={index} sx={{ p: 2, mb: 2 }}>
                            <Grid container spacing={2}>
                                <Grid item xs={12} sm={6}>
                                    <TextField
                                        fullWidth
                                        type="number"
                                        label="제품 ID"
                                        value={item.productId || ''}
                                        onChange={(e) => updateItem(index, 'productId', Number(e.target.value))}
                                    />
                                </Grid>
                                <Grid item xs={12} sm={6}>
                                    <TextField
                                        fullWidth
                                        type="number"
                                        label="LOT ID (선택)"
                                        value={item.lotId || ''}
                                        onChange={(e) => updateItem(index, 'lotId', e.target.value ? Number(e.target.value) : null)}
                                    />
                                </Grid>
                                <Grid item xs={12} sm={4}>
                                    <TextField
                                        fullWidth
                                        type="number"
                                        label="폐기수량"
                                        value={item.disposalQuantity || ''}
                                        onChange={(e) => updateItem(index, 'disposalQuantity', Number(e.target.value))}
                                    />
                                </Grid>
                                <Grid item xs={12} sm={4}>
                                    <TextField
                                        fullWidth
                                        label="불량유형"
                                        value={item.defectType || ''}
                                        onChange={(e) => updateItem(index, 'defectType', e.target.value)}
                                    />
                                </Grid>
                                <Grid item xs={12} sm={4}>
                                    <TextField
                                        fullWidth
                                        type="date"
                                        label="유효기간 (선택)"
                                        value={item.expiryDate || ''}
                                        onChange={(e) => updateItem(index, 'expiryDate', e.target.value)}
                                        InputLabelProps={{ shrink: true }}
                                    />
                                </Grid>
                                <Grid item xs={12}>
                                    <TextField
                                        fullWidth
                                        label="불량설명"
                                        value={item.defectDescription || ''}
                                        onChange={(e) => updateItem(index, 'defectDescription', e.target.value)}
                                    />
                                </Grid>
                                <Grid item xs={12}>
                                    <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                                        <TextField
                                            fullWidth
                                            label="비고"
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
                    <Button onClick={() => setOpenCreate(false)}>취소</Button>
                    <Button variant="contained" onClick={handleCreate}>생성</Button>
                </DialogActions>
            </Dialog>

            {/* Detail Dialog */}
            <Dialog open={openDetail} onClose={() => setOpenDetail(false)} maxWidth="md" fullWidth>
                <DialogTitle>폐기 상세</DialogTitle>
                <DialogContent>
                    {selectedDisposal && (
                        <Box sx={{ mt: 2 }}>
                            <Grid container spacing={2}>
                                <Grid item xs={12} sm={6}>
                                    <Typography variant="body2" color="textSecondary">폐기번호</Typography>
                                    <Typography variant="body1">{selectedDisposal.disposalNo}</Typography>
                                </Grid>
                                <Grid item xs={12} sm={6}>
                                    <Typography variant="body2" color="textSecondary">상태</Typography>
                                    {getStatusChip(selectedDisposal.disposalStatus)}
                                </Grid>
                                <Grid item xs={12} sm={6}>
                                    <Typography variant="body2" color="textSecondary">폐기유형</Typography>
                                    {getTypeChip(selectedDisposal.disposalType)}
                                </Grid>
                                <Grid item xs={12} sm={6}>
                                    <Typography variant="body2" color="textSecondary">폐기일자</Typography>
                                    <Typography variant="body1">
                                        {new Date(selectedDisposal.disposalDate).toLocaleString('ko-KR')}
                                    </Typography>
                                </Grid>
                                <Grid item xs={12} sm={6}>
                                    <Typography variant="body2" color="textSecondary">창고</Typography>
                                    <Typography variant="body1">
                                        {selectedDisposal.warehouseCode} - {selectedDisposal.warehouseName}
                                    </Typography>
                                </Grid>
                                <Grid item xs={12} sm={6}>
                                    <Typography variant="body2" color="textSecondary">요청자</Typography>
                                    <Typography variant="body1">{selectedDisposal.requesterUserName}</Typography>
                                </Grid>
                                <Grid item xs={12} sm={6}>
                                    <Typography variant="body2" color="textSecondary">승인자</Typography>
                                    <Typography variant="body1">{selectedDisposal.approverUserName || '-'}</Typography>
                                </Grid>
                                <Grid item xs={12} sm={6}>
                                    <Typography variant="body2" color="textSecondary">처리자</Typography>
                                    <Typography variant="body1">{selectedDisposal.processorUserName || '-'}</Typography>
                                </Grid>
                                <Grid item xs={12} sm={6}>
                                    <Typography variant="body2" color="textSecondary">폐기방법</Typography>
                                    <Typography variant="body1">{selectedDisposal.disposalMethod || '-'}</Typography>
                                </Grid>
                                <Grid item xs={12} sm={6}>
                                    <Typography variant="body2" color="textSecondary">폐기장소</Typography>
                                    <Typography variant="body1">{selectedDisposal.disposalLocation || '-'}</Typography>
                                </Grid>
                                <Grid item xs={12}>
                                    <Typography variant="body2" color="textSecondary">비고</Typography>
                                    <Typography variant="body1">{selectedDisposal.remarks || '-'}</Typography>
                                </Grid>
                                {selectedDisposal.rejectionReason && (
                                    <Grid item xs={12}>
                                        <Typography variant="body2" color="textSecondary">거부사유</Typography>
                                        <Typography variant="body1" color="error">{selectedDisposal.rejectionReason}</Typography>
                                    </Grid>
                                )}
                                {selectedDisposal.cancellationReason && (
                                    <Grid item xs={12}>
                                        <Typography variant="body2" color="textSecondary">취소사유</Typography>
                                        <Typography variant="body1" color="error">{selectedDisposal.cancellationReason}</Typography>
                                    </Grid>
                                )}
                            </Grid>

                            <Typography variant="h6" sx={{ mt: 3, mb: 2 }}>폐기 항목</Typography>
                            <TableContainer>
                                <Table size="small">
                                    <TableHead>
                                        <TableRow>
                                            <TableCell>제품코드</TableCell>
                                            <TableCell>제품명</TableCell>
                                            <TableCell>LOT</TableCell>
                                            <TableCell align="right">폐기수량</TableCell>
                                            <TableCell>불량유형</TableCell>
                                            <TableCell>불량설명</TableCell>
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
                    <Button onClick={() => setOpenDetail(false)}>닫기</Button>
                </DialogActions>
            </Dialog>
        </Box>
    );
};

export default DisposalsPage;
