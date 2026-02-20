import React, { useState, useEffect } from 'react';
import {
  Box,
  Paper,
  Typography,
  Button,
  Chip,
  Alert,
  Stack,
  CircularProgress,
} from '@mui/material';
import { DataGrid, GridColDef, GridRenderCellParams } from '@mui/x-data-grid';
import { useNavigate } from 'react-router-dom';
import {
  Add as AddIcon,
  Refresh as RefreshIcon,
  CheckCircle as PassIcon,
  Cancel as FailIcon,
  Warning as PendingIcon,
} from '@mui/icons-material';
import axios from 'axios';
import { format } from 'date-fns';

/**
 * IQC 의뢰 리스트 페이지
 * 입고 품질 검사 의뢰 목록 관리
 *
 * @author Moon Myung-seop
 */

interface QualityInspection {
  qualityInspectionId: number;
  inspectionNo: string;
  inspectionDate: string;
  inspectionType: string;
  productCode: string;
  productName: string;
  inspectedQuantity: number;
  passedQuantity: number;
  failedQuantity: number;
  inspectionResult: string; // PASS, FAIL, CONDITIONAL
  inspectorName: string;
  standardCode: string;
  standardName: string;
  measuredValue?: number;
  measurementUnit?: string;
  remarks?: string;
  createdAt: string;
  updatedAt: string;
}

const IQCRequestsPage: React.FC = () => {
  const navigate = useNavigate();
  const [inspections, setInspections] = useState<QualityInspection[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // 데이터 로드
  const loadInspections = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await axios.get('/api/quality-inspections/iqc-requests');
      setInspections(response.data.data || []);
    } catch (err: any) {
      setError(err.response?.data?.message || 'IQC 의뢰 목록을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadInspections();
  }, []);

  // 검사 결과 렌더링
  const renderInspectionResult = (params: GridRenderCellParams) => {
    const result = params.value as string;
    let color: 'success' | 'error' | 'warning' = 'warning';
    let icon = <PendingIcon fontSize="small" />;

    if (result === 'PASS') {
      color = 'success';
      icon = <PassIcon fontSize="small" />;
    } else if (result === 'FAIL') {
      color = 'error';
      icon = <FailIcon fontSize="small" />;
    }

    return (
      <Chip
        icon={icon}
        label={result}
        color={color}
        size="small"
        variant="outlined"
      />
    );
  };

  // 수량 렌더링 (합격/불합격)
  const renderQuantities = (params: GridRenderCellParams) => {
    const row = params.row as QualityInspection;
    return (
      <Stack direction="row" spacing={1}>
        <Chip
          label={`합격: ${row.passedQuantity}`}
          color="success"
          size="small"
          variant="outlined"
        />
        {row.failedQuantity > 0 && (
          <Chip
            label={`불합격: ${row.failedQuantity}`}
            color="error"
            size="small"
            variant="outlined"
          />
        )}
      </Stack>
    );
  };

  // 측정값 렌더링
  const renderMeasuredValue = (params: GridRenderCellParams) => {
    const row = params.row as QualityInspection;
    if (!row.measuredValue) return '-';
    return `${row.measuredValue} ${row.measurementUnit || ''}`;
  };

  // DataGrid 컬럼 정의
  const columns: GridColDef[] = [
    {
      field: 'inspectionNo',
      headerName: '검사번호',
      width: 150,
      sortable: true,
    },
    {
      field: 'inspectionDate',
      headerName: '검사일시',
      width: 160,
      renderCell: (params) => {
        return format(new Date(params.value as string), 'yyyy-MM-dd HH:mm');
      },
    },
    {
      field: 'productCode',
      headerName: '제품코드',
      width: 120,
    },
    {
      field: 'productName',
      headerName: '제품명',
      width: 180,
      flex: 1,
    },
    {
      field: 'standardName',
      headerName: '검사기준',
      width: 150,
    },
    {
      field: 'inspectedQuantity',
      headerName: '검사수량',
      width: 100,
      type: 'number',
      align: 'right',
      headerAlign: 'right',
    },
    {
      field: 'quantities',
      headerName: '합격/불합격',
      width: 200,
      renderCell: renderQuantities,
      sortable: false,
    },
    {
      field: 'measuredValue',
      headerName: '측정값',
      width: 120,
      renderCell: renderMeasuredValue,
    },
    {
      field: 'inspectionResult',
      headerName: '검사결과',
      width: 120,
      renderCell: renderInspectionResult,
    },
    {
      field: 'inspectorName',
      headerName: '검사자',
      width: 100,
    },
    {
      field: 'remarks',
      headerName: '비고',
      width: 150,
      flex: 1,
    },
  ];

  return (
    <Box sx={{ width: '100%', p: 3 }}>
      {/* 헤더 */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" component="h1" gutterBottom>
          📦 IQC 의뢰 리스트
        </Typography>
        <Stack direction="row" spacing={2}>
          <Button
            variant="outlined"
            startIcon={<RefreshIcon />}
            onClick={loadInspections}
            disabled={loading}
          >
            새로고침
          </Button>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => navigate('/quality/inspections/new')}
          >
            검사 등록
          </Button>
        </Stack>
      </Box>

      {/* 안내 메시지 */}
      <Alert severity="info" sx={{ mb: 2 }}>
        <Typography variant="body2">
          <strong>IQC (Incoming Quality Control)</strong>: 입고된 원자재 및 부자재에 대한 품질 검사 의뢰 목록입니다.
          <br />
          검사 결과에 따라 입고 승인 또는 반품 처리됩니다.
        </Typography>
      </Alert>

      {/* 에러 메시지 */}
      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {/* 데이터 그리드 */}
      <Paper sx={{ width: '100%', overflow: 'hidden' }}>
        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', p: 5 }}>
            <CircularProgress />
          </Box>
        ) : (
          <DataGrid
            rows={inspections}
            columns={columns}
            getRowId={(row) => row.qualityInspectionId}
            initialState={{
              pagination: {
                paginationModel: { page: 0, pageSize: 25 },
              },
              sorting: {
                sortModel: [{ field: 'inspectionDate', sort: 'desc' }],
              },
            }}
            pageSizeOptions={[10, 25, 50, 100]}
            checkboxSelection
            disableRowSelectionOnClick
            autoHeight
            sx={{
              '& .MuiDataGrid-cell': {
                borderBottom: '1px solid #f0f0f0',
              },
              '& .MuiDataGrid-columnHeaders': {
                backgroundColor: '#f5f5f5',
                fontWeight: 'bold',
              },
            }}
            onRowClick={(params) => {
              navigate(`/quality/inspections/${params.row.qualityInspectionId}`);
            }}
          />
        )}
      </Paper>

      {/* 통계 요약 */}
      <Paper sx={{ mt: 3, p: 2 }}>
        <Typography variant="h6" gutterBottom>
          📊 검사 통계
        </Typography>
        <Stack direction="row" spacing={4}>
          <Box>
            <Typography variant="body2" color="text.secondary">
              총 검사 건수
            </Typography>
            <Typography variant="h5">{inspections.length}</Typography>
          </Box>
          <Box>
            <Typography variant="body2" color="text.secondary">
              합격
            </Typography>
            <Typography variant="h5" color="success.main">
              {inspections.filter((i) => i.inspectionResult === 'PASS').length}
            </Typography>
          </Box>
          <Box>
            <Typography variant="body2" color="text.secondary">
              불합격
            </Typography>
            <Typography variant="h5" color="error.main">
              {inspections.filter((i) => i.inspectionResult === 'FAIL').length}
            </Typography>
          </Box>
          <Box>
            <Typography variant="body2" color="text.secondary">
              조건부
            </Typography>
            <Typography variant="h5" color="warning.main">
              {inspections.filter((i) => i.inspectionResult === 'CONDITIONAL').length}
            </Typography>
          </Box>
          <Box>
            <Typography variant="body2" color="text.secondary">
              합격률
            </Typography>
            <Typography variant="h5" color="primary.main">
              {inspections.length > 0
                ? ((inspections.filter((i) => i.inspectionResult === 'PASS').length /
                    inspections.length) *
                    100
                  ).toFixed(1)
                : 0}
              %
            </Typography>
          </Box>
        </Stack>
      </Paper>
    </Box>
  );
};

export default IQCRequestsPage;
