/**
 * Statistical Reports Page
 * Comprehensive reporting with export functionality
 * @author Moon Myung-seop
 */

import { useState, useCallback } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Tabs,
  Tab,
  Button,
  Grid,
  TextField,
  MenuItem,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Paper,
  Chip,
  IconButton,
  Tooltip,
  Stack,
  Divider,
} from '@mui/material';
import {
  Download as DownloadIcon,
  PictureAsPdf as PdfIcon,
  TableChart as ExcelIcon,
  Description as CsvIcon,
  Refresh as RefreshIcon,
  Print as PrintIcon,
} from '@mui/icons-material';

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

function TabPanel(props: TabPanelProps) {
  const { children, value, index, ...other } = props;
  return (
    <div role="tabpanel" hidden={value !== index} {...other}>
      {value === index && <Box sx={{ pt: 3 }}>{children}</Box>}
    </div>
  );
}

const StatisticalReportsPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState(0);
  const [dateRange, setDateRange] = useState({
    startDate: new Date().toISOString().split('T')[0],
    endDate: new Date().toISOString().split('T')[0],
  });
  const [selectedProduct, setSelectedProduct] = useState('all');
  const [selectedOperator, setSelectedOperator] = useState('all');
  const [selectedWarehouse, setSelectedWarehouse] = useState('all');
  const [refreshKey, setRefreshKey] = useState(0);

  /**
   * Refresh report data.
   * Currently triggers a re-render with updated filter state.
   * When API integration is added, this function should call the data-fetching logic.
   */
  const handleRefresh = useCallback(() => {
    setRefreshKey((prev) => prev + 1);
  }, []);

  // Mock data for reports (keyed by refreshKey to enable future dynamic loading)
  const productionReportData = [
    {
      date: '2026-02-04',
      workOrderNo: 'WO-20260204-001',
      productName: '제품 A',
      targetQty: 1000,
      actualQty: 950,
      efficiency: 95,
      operator: '김작업',
      defectQty: 15,
      downtime: 45,
    },
    {
      date: '2026-02-04',
      workOrderNo: 'WO-20260204-002',
      productName: '제품 B',
      targetQty: 800,
      actualQty: 850,
      efficiency: 106.25,
      operator: '이작업',
      defectQty: 8,
      downtime: 20,
    },
    {
      date: '2026-02-03',
      workOrderNo: 'WO-20260203-005',
      productName: '제품 A',
      targetQty: 1000,
      actualQty: 980,
      efficiency: 98,
      operator: '박작업',
      defectQty: 12,
      downtime: 30,
    },
  ];

  const qualityReportData = [
    {
      date: '2026-02-04',
      inspectionNo: 'IQC-20260204-001',
      type: 'IQC',
      productName: '원자재 A',
      lotNo: 'LOT-2026020401',
      inspectedQty: 500,
      result: 'PASS',
      defects: 0,
      inspector: '김품질',
    },
    {
      date: '2026-02-04',
      inspectionNo: 'OQC-20260204-002',
      type: 'OQC',
      productName: '제품 A',
      lotNo: 'LOT-2026020402',
      inspectedQty: 950,
      result: 'PASS',
      defects: 15,
      inspector: '이품질',
    },
    {
      date: '2026-02-03',
      inspectionNo: 'IQC-20260203-008',
      type: 'IQC',
      productName: '원자재 B',
      lotNo: 'LOT-2026020305',
      inspectedQty: 300,
      result: 'FAIL',
      defects: 45,
      inspector: '박품질',
    },
  ];

  const inventoryReportData = [
    {
      productName: '원자재 A',
      category: '원자재',
      currentStock: 5000,
      safetyStock: 1000,
      incomingQty: 2000,
      outgoingQty: 1500,
      turnoverRate: 12.5,
      warehouseName: '원자재 창고',
      lastUpdated: '2026-02-04 14:30',
    },
    {
      productName: '제품 A',
      category: '완제품',
      currentStock: 3500,
      safetyStock: 500,
      incomingQty: 950,
      outgoingQty: 800,
      turnoverRate: 15.2,
      warehouseName: '완제품 창고',
      lastUpdated: '2026-02-04 15:00',
    },
    {
      productName: '반제품 B',
      category: '반제품',
      currentStock: 450,
      safetyStock: 200,
      incomingQty: 500,
      outgoingQty: 600,
      turnoverRate: 18.7,
      warehouseName: '생산 창고',
      lastUpdated: '2026-02-04 13:45',
    },
  ];

  const equipmentReportData = [
    {
      equipmentNo: 'EQ-001',
      equipmentName: '사출 성형기 #1',
      utilizationRate: 87.5,
      operatingTime: 420,
      downtimeTotal: 60,
      maintenanceCount: 2,
      defectCount: 15,
      oee: 84.2,
      status: '가동',
    },
    {
      equipmentNo: 'EQ-002',
      equipmentName: '사출 성형기 #2',
      utilizationRate: 92.3,
      operatingTime: 443,
      downtimeTotal: 37,
      maintenanceCount: 1,
      defectCount: 8,
      oee: 89.5,
      status: '가동',
    },
    {
      equipmentNo: 'EQ-003',
      equipmentName: '포장기 #1',
      utilizationRate: 65.8,
      operatingTime: 316,
      downtimeTotal: 164,
      maintenanceCount: 4,
      defectCount: 22,
      oee: 62.1,
      status: '점검',
    },
  ];

  const getReportData = (reportType: string): { headers: string[]; rows: (string | number)[][] } => {
    switch (reportType) {
      case '생산':
        return {
          headers: ['날짜', '작업지시번호', '제품명', '목표수량', '실적수량', '효율(%)', '작업자', '불량수량', '다운타임(분)'],
          rows: productionReportData.map((r) => [
            r.date, r.workOrderNo, r.productName, r.targetQty, r.actualQty,
            r.efficiency, r.operator, r.defectQty, r.downtime,
          ]),
        };
      case '품질':
        return {
          headers: ['날짜', '검사번호', '검사유형', '제품명', 'LOT번호', '검사수량', '결과', '불량수', '검사자'],
          rows: qualityReportData.map((r) => [
            r.date, r.inspectionNo, r.type, r.productName, r.lotNo,
            r.inspectedQty, r.result, r.defects, r.inspector,
          ]),
        };
      case '재고':
        return {
          headers: ['제품명', '카테고리', '현재고', '안전재고', '입고수량', '출고수량', '회전율', '창고', '최종 업데이트'],
          rows: inventoryReportData.map((r) => [
            r.productName, r.category, r.currentStock, r.safetyStock, r.incomingQty,
            r.outgoingQty, r.turnoverRate, r.warehouseName, r.lastUpdated,
          ]),
        };
      case '설비':
        return {
          headers: ['설비번호', '설비명', '가동률(%)', '가동시간(분)', '다운타임(분)', '보전횟수', '불량수', 'OEE(%)', '상태'],
          rows: equipmentReportData.map((r) => [
            r.equipmentNo, r.equipmentName, r.utilizationRate, r.operatingTime,
            r.downtimeTotal, r.maintenanceCount, r.defectCount, r.oee, r.status,
          ]),
        };
      default:
        return { headers: [], rows: [] };
    }
  };

  const buildCsvString = (headers: string[], rows: (string | number)[][]): string => {
    const escapeCsvCell = (cell: string | number): string => {
      const str = String(cell);
      if (str.includes(',') || str.includes('"') || str.includes('\n')) {
        return `"${str.replace(/"/g, '""')}"`;
      }
      return str;
    };
    const headerLine = headers.map(escapeCsvCell).join(',');
    const dataLines = rows.map((row) => row.map(escapeCsvCell).join(','));
    return [headerLine, ...dataLines].join('\n');
  };

  const downloadFile = (content: string, fileName: string, mimeType: string) => {
    const BOM = '\uFEFF';
    const blob = new Blob([BOM + content], { type: `${mimeType};charset=utf-8` });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = fileName;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  };

  const handleExport = (format: 'pdf' | 'excel' | 'csv', reportType: string) => {
    const { headers, rows } = getReportData(reportType);
    if (headers.length === 0) return;

    const timestamp = new Date().toISOString().slice(0, 10).replace(/-/g, '');
    const baseFileName = `${reportType}_리포트_${timestamp}`;
    const csvContent = buildCsvString(headers, rows);

    switch (format) {
      case 'csv':
        downloadFile(csvContent, `${baseFileName}.csv`, 'text/csv');
        break;
      case 'excel':
        downloadFile(csvContent, `${baseFileName}.xls`, 'application/vnd.ms-excel');
        break;
      case 'pdf':
        window.print();
        break;
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'PASS':
      case '가동':
        return 'success';
      case 'FAIL':
      case '점검':
        return 'error';
      default:
        return 'default';
    }
  };

  const getEfficiencyColor = (efficiency: number) => {
    if (efficiency >= 100) return 'success';
    if (efficiency >= 90) return 'info';
    if (efficiency >= 80) return 'warning';
    return 'error';
  };

  const ExportButtons = ({ reportType }: { reportType: string }) => (
    <Stack direction="row" spacing={1}>
      <Tooltip title="PDF로 내보내기">
        <IconButton size="small" onClick={() => handleExport('pdf', reportType)}>
          <PdfIcon color="error" />
        </IconButton>
      </Tooltip>
      <Tooltip title="Excel로 내보내기">
        <IconButton size="small" onClick={() => handleExport('excel', reportType)}>
          <ExcelIcon color="success" />
        </IconButton>
      </Tooltip>
      <Tooltip title="CSV로 내보내기">
        <IconButton size="small" onClick={() => handleExport('csv', reportType)}>
          <CsvIcon color="primary" />
        </IconButton>
      </Tooltip>
      <Tooltip title="인쇄">
        <IconButton size="small" onClick={() => window.print()}>
          <PrintIcon />
        </IconButton>
      </Tooltip>
    </Stack>
  );

  return (
    <Box sx={{ p: 3 }}>
      {/* Header */}
      <Box sx={{ mb: 3 }}>
        <Typography variant="h4" fontWeight="bold" gutterBottom>
          통계 리포트
        </Typography>
        <Typography variant="body2" color="text.secondary">
          생산, 품질, 재고, 설비 통계 리포트 및 데이터 내보내기
        </Typography>
      </Box>

      {/* Filter Section */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Grid container spacing={2} alignItems="center">
            <Grid item xs={12} md={3}>
              <TextField
                fullWidth
                label="시작 날짜"
                type="date"
                value={dateRange.startDate}
                onChange={(e) => setDateRange({ ...dateRange, startDate: e.target.value })}
                InputLabelProps={{ shrink: true }}
                size="small"
              />
            </Grid>
            <Grid item xs={12} md={3}>
              <TextField
                fullWidth
                label="종료 날짜"
                type="date"
                value={dateRange.endDate}
                onChange={(e) => setDateRange({ ...dateRange, endDate: e.target.value })}
                InputLabelProps={{ shrink: true }}
                size="small"
              />
            </Grid>
            <Grid item xs={12} md={2}>
              <TextField
                fullWidth
                select
                label="제품"
                value={selectedProduct}
                onChange={(e) => setSelectedProduct(e.target.value)}
                size="small"
              >
                <MenuItem value="all">전체</MenuItem>
                <MenuItem value="product-a">제품 A</MenuItem>
                <MenuItem value="product-b">제품 B</MenuItem>
              </TextField>
            </Grid>
            <Grid item xs={12} md={2}>
              <TextField
                fullWidth
                select
                label="작업자"
                value={selectedOperator}
                onChange={(e) => setSelectedOperator(e.target.value)}
                size="small"
              >
                <MenuItem value="all">전체</MenuItem>
                <MenuItem value="kim">김작업</MenuItem>
                <MenuItem value="lee">이작업</MenuItem>
              </TextField>
            </Grid>
            <Grid item xs={12} md={2}>
              <Button
                fullWidth
                variant="contained"
                startIcon={<RefreshIcon />}
                onClick={handleRefresh}
              >
                조회
              </Button>
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      {/* Tabs */}
      <Card>
        <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
          <Tabs value={activeTab} onChange={(_, newValue) => setActiveTab(newValue)}>
            <Tab label="생산 리포트" />
            <Tab label="품질 리포트" />
            <Tab label="재고 리포트" />
            <Tab label="설비 리포트" />
          </Tabs>
        </Box>

        {/* Production Report Tab */}
        <TabPanel value={activeTab} index={0}>
          <CardContent>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
              <Typography variant="h6" fontWeight="bold">
                생산 실적 리포트
              </Typography>
              <ExportButtons reportType="생산" />
            </Box>
            <Divider sx={{ mb: 2 }} />

            {/* Summary Cards */}
            <Grid container spacing={2} sx={{ mb: 3 }}>
              <Grid item xs={12} sm={6} md={3}>
                <Card variant="outlined">
                  <CardContent>
                    <Typography variant="body2" color="text.secondary">
                      총 생산량
                    </Typography>
                    <Typography variant="h5" fontWeight="bold" color="primary">
                      2,780 EA
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
              <Grid item xs={12} sm={6} md={3}>
                <Card variant="outlined">
                  <CardContent>
                    <Typography variant="body2" color="text.secondary">
                      평균 효율
                    </Typography>
                    <Typography variant="h5" fontWeight="bold" color="success.main">
                      99.75%
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
              <Grid item xs={12} sm={6} md={3}>
                <Card variant="outlined">
                  <CardContent>
                    <Typography variant="body2" color="text.secondary">
                      총 불량
                    </Typography>
                    <Typography variant="h5" fontWeight="bold" color="error.main">
                      35 EA
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
              <Grid item xs={12} sm={6} md={3}>
                <Card variant="outlined">
                  <CardContent>
                    <Typography variant="body2" color="text.secondary">
                      총 다운타임
                    </Typography>
                    <Typography variant="h5" fontWeight="bold" color="warning.main">
                      95분
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
            </Grid>

            {/* Production Table */}
            <Paper sx={{ overflow: 'hidden' }}>
              <Table>
                <TableHead>
                  <TableRow sx={{ bgcolor: 'grey.100' }}>
                    <TableCell>
                      <Typography variant="subtitle2" fontWeight="bold">
                        날짜
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="subtitle2" fontWeight="bold">
                        작업지시번호
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="subtitle2" fontWeight="bold">
                        제품명
                      </Typography>
                    </TableCell>
                    <TableCell align="right">
                      <Typography variant="subtitle2" fontWeight="bold">
                        목표수량
                      </Typography>
                    </TableCell>
                    <TableCell align="right">
                      <Typography variant="subtitle2" fontWeight="bold">
                        실적수량
                      </Typography>
                    </TableCell>
                    <TableCell align="center">
                      <Typography variant="subtitle2" fontWeight="bold">
                        효율
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="subtitle2" fontWeight="bold">
                        작업자
                      </Typography>
                    </TableCell>
                    <TableCell align="right">
                      <Typography variant="subtitle2" fontWeight="bold">
                        불량수량
                      </Typography>
                    </TableCell>
                    <TableCell align="right">
                      <Typography variant="subtitle2" fontWeight="bold">
                        다운타임(분)
                      </Typography>
                    </TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {productionReportData.map((row, index) => (
                    <TableRow key={index} hover>
                      <TableCell>{row.date}</TableCell>
                      <TableCell>{row.workOrderNo}</TableCell>
                      <TableCell>{row.productName}</TableCell>
                      <TableCell align="right">{row.targetQty.toLocaleString()}</TableCell>
                      <TableCell align="right">
                        <Typography fontWeight="medium">{row.actualQty.toLocaleString()}</Typography>
                      </TableCell>
                      <TableCell align="center">
                        <Chip label={`${row.efficiency}%`} size="small" color={getEfficiencyColor(row.efficiency)} />
                      </TableCell>
                      <TableCell>{row.operator}</TableCell>
                      <TableCell align="right">
                        <Typography color={row.defectQty > 0 ? 'error' : 'text.secondary'}>{row.defectQty}</Typography>
                      </TableCell>
                      <TableCell align="right">
                        <Typography color={row.downtime > 30 ? 'warning.main' : 'text.secondary'}>
                          {row.downtime}
                        </Typography>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </Paper>
          </CardContent>
        </TabPanel>

        {/* Quality Report Tab */}
        <TabPanel value={activeTab} index={1}>
          <CardContent>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
              <Typography variant="h6" fontWeight="bold">
                품질 검사 리포트
              </Typography>
              <ExportButtons reportType="품질" />
            </Box>
            <Divider sx={{ mb: 2 }} />

            {/* Summary Cards */}
            <Grid container spacing={2} sx={{ mb: 3 }}>
              <Grid item xs={12} sm={6} md={3}>
                <Card variant="outlined">
                  <CardContent>
                    <Typography variant="body2" color="text.secondary">
                      총 검사
                    </Typography>
                    <Typography variant="h5" fontWeight="bold" color="primary">
                      3건
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
              <Grid item xs={12} sm={6} md={3}>
                <Card variant="outlined">
                  <CardContent>
                    <Typography variant="body2" color="text.secondary">
                      합격
                    </Typography>
                    <Typography variant="h5" fontWeight="bold" color="success.main">
                      2건 (66.7%)
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
              <Grid item xs={12} sm={6} md={3}>
                <Card variant="outlined">
                  <CardContent>
                    <Typography variant="body2" color="text.secondary">
                      불합격
                    </Typography>
                    <Typography variant="h5" fontWeight="bold" color="error.main">
                      1건 (33.3%)
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
              <Grid item xs={12} sm={6} md={3}>
                <Card variant="outlined">
                  <CardContent>
                    <Typography variant="body2" color="text.secondary">
                      총 불량
                    </Typography>
                    <Typography variant="h5" fontWeight="bold" color="warning.main">
                      60 EA
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
            </Grid>

            {/* Quality Table */}
            <Paper sx={{ overflow: 'hidden' }}>
              <Table>
                <TableHead>
                  <TableRow sx={{ bgcolor: 'grey.100' }}>
                    <TableCell>
                      <Typography variant="subtitle2" fontWeight="bold">
                        날짜
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="subtitle2" fontWeight="bold">
                        검사번호
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="subtitle2" fontWeight="bold">
                        검사유형
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="subtitle2" fontWeight="bold">
                        제품명
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="subtitle2" fontWeight="bold">
                        LOT번호
                      </Typography>
                    </TableCell>
                    <TableCell align="right">
                      <Typography variant="subtitle2" fontWeight="bold">
                        검사수량
                      </Typography>
                    </TableCell>
                    <TableCell align="center">
                      <Typography variant="subtitle2" fontWeight="bold">
                        결과
                      </Typography>
                    </TableCell>
                    <TableCell align="right">
                      <Typography variant="subtitle2" fontWeight="bold">
                        불량수
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="subtitle2" fontWeight="bold">
                        검사자
                      </Typography>
                    </TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {qualityReportData.map((row, index) => (
                    <TableRow key={index} hover>
                      <TableCell>{row.date}</TableCell>
                      <TableCell>{row.inspectionNo}</TableCell>
                      <TableCell>
                        <Chip label={row.type} size="small" variant="outlined" />
                      </TableCell>
                      <TableCell>{row.productName}</TableCell>
                      <TableCell>{row.lotNo}</TableCell>
                      <TableCell align="right">{row.inspectedQty.toLocaleString()}</TableCell>
                      <TableCell align="center">
                        <Chip label={row.result} size="small" color={getStatusColor(row.result)} />
                      </TableCell>
                      <TableCell align="right">
                        <Typography color={row.defects > 0 ? 'error' : 'success.main'}>{row.defects}</Typography>
                      </TableCell>
                      <TableCell>{row.inspector}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </Paper>
          </CardContent>
        </TabPanel>

        {/* Inventory Report Tab */}
        <TabPanel value={activeTab} index={2}>
          <CardContent>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
              <Typography variant="h6" fontWeight="bold">
                재고 현황 리포트
              </Typography>
              <ExportButtons reportType="재고" />
            </Box>
            <Divider sx={{ mb: 2 }} />

            {/* Summary Cards */}
            <Grid container spacing={2} sx={{ mb: 3 }}>
              <Grid item xs={12} sm={6} md={3}>
                <Card variant="outlined">
                  <CardContent>
                    <Typography variant="body2" color="text.secondary">
                      총 재고 품목
                    </Typography>
                    <Typography variant="h5" fontWeight="bold" color="primary">
                      3품목
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
              <Grid item xs={12} sm={6} md={3}>
                <Card variant="outlined">
                  <CardContent>
                    <Typography variant="body2" color="text.secondary">
                      총 재고 수량
                    </Typography>
                    <Typography variant="h5" fontWeight="bold" color="success.main">
                      8,950 EA
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
              <Grid item xs={12} sm={6} md={3}>
                <Card variant="outlined">
                  <CardContent>
                    <Typography variant="body2" color="text.secondary">
                      평균 회전율
                    </Typography>
                    <Typography variant="h5" fontWeight="bold" color="info.main">
                      15.5
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
              <Grid item xs={12} sm={6} md={3}>
                <Card variant="outlined">
                  <CardContent>
                    <Typography variant="body2" color="text.secondary">
                      안전재고 미달
                    </Typography>
                    <Typography variant="h5" fontWeight="bold" color="error.main">
                      0품목
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
            </Grid>

            {/* Inventory Table */}
            <Paper sx={{ overflow: 'hidden' }}>
              <Table>
                <TableHead>
                  <TableRow sx={{ bgcolor: 'grey.100' }}>
                    <TableCell>
                      <Typography variant="subtitle2" fontWeight="bold">
                        제품명
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="subtitle2" fontWeight="bold">
                        카테고리
                      </Typography>
                    </TableCell>
                    <TableCell align="right">
                      <Typography variant="subtitle2" fontWeight="bold">
                        현재고
                      </Typography>
                    </TableCell>
                    <TableCell align="right">
                      <Typography variant="subtitle2" fontWeight="bold">
                        안전재고
                      </Typography>
                    </TableCell>
                    <TableCell align="right">
                      <Typography variant="subtitle2" fontWeight="bold">
                        입고수량
                      </Typography>
                    </TableCell>
                    <TableCell align="right">
                      <Typography variant="subtitle2" fontWeight="bold">
                        출고수량
                      </Typography>
                    </TableCell>
                    <TableCell align="center">
                      <Typography variant="subtitle2" fontWeight="bold">
                        회전율
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="subtitle2" fontWeight="bold">
                        창고
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="subtitle2" fontWeight="bold">
                        최종 업데이트
                      </Typography>
                    </TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {inventoryReportData.map((row, index) => (
                    <TableRow key={index} hover>
                      <TableCell>
                        <Typography fontWeight="medium">{row.productName}</Typography>
                      </TableCell>
                      <TableCell>
                        <Chip label={row.category} size="small" variant="outlined" />
                      </TableCell>
                      <TableCell align="right">
                        <Typography
                          fontWeight="bold"
                          color={row.currentStock < row.safetyStock ? 'error' : 'success.main'}
                        >
                          {row.currentStock.toLocaleString()}
                        </Typography>
                      </TableCell>
                      <TableCell align="right">{row.safetyStock.toLocaleString()}</TableCell>
                      <TableCell align="right">
                        <Typography color="info.main">{row.incomingQty.toLocaleString()}</Typography>
                      </TableCell>
                      <TableCell align="right">
                        <Typography color="warning.main">{row.outgoingQty.toLocaleString()}</Typography>
                      </TableCell>
                      <TableCell align="center">
                        <Chip label={row.turnoverRate} size="small" color="primary" />
                      </TableCell>
                      <TableCell>{row.warehouseName}</TableCell>
                      <TableCell>
                        <Typography variant="body2" color="text.secondary">
                          {row.lastUpdated}
                        </Typography>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </Paper>
          </CardContent>
        </TabPanel>

        {/* Equipment Report Tab */}
        <TabPanel value={activeTab} index={3}>
          <CardContent>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
              <Typography variant="h6" fontWeight="bold">
                설비 가동 리포트
              </Typography>
              <ExportButtons reportType="설비" />
            </Box>
            <Divider sx={{ mb: 2 }} />

            {/* Summary Cards */}
            <Grid container spacing={2} sx={{ mb: 3 }}>
              <Grid item xs={12} sm={6} md={3}>
                <Card variant="outlined">
                  <CardContent>
                    <Typography variant="body2" color="text.secondary">
                      총 설비
                    </Typography>
                    <Typography variant="h5" fontWeight="bold" color="primary">
                      3대
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
              <Grid item xs={12} sm={6} md={3}>
                <Card variant="outlined">
                  <CardContent>
                    <Typography variant="body2" color="text.secondary">
                      평균 가동률
                    </Typography>
                    <Typography variant="h5" fontWeight="bold" color="success.main">
                      81.9%
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
              <Grid item xs={12} sm={6} md={3}>
                <Card variant="outlined">
                  <CardContent>
                    <Typography variant="body2" color="text.secondary">
                      평균 OEE
                    </Typography>
                    <Typography variant="h5" fontWeight="bold" color="info.main">
                      78.6%
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
              <Grid item xs={12} sm={6} md={3}>
                <Card variant="outlined">
                  <CardContent>
                    <Typography variant="body2" color="text.secondary">
                      총 다운타임
                    </Typography>
                    <Typography variant="h5" fontWeight="bold" color="warning.main">
                      261분
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
            </Grid>

            {/* Equipment Table */}
            <Paper sx={{ overflow: 'hidden' }}>
              <Table>
                <TableHead>
                  <TableRow sx={{ bgcolor: 'grey.100' }}>
                    <TableCell>
                      <Typography variant="subtitle2" fontWeight="bold">
                        설비번호
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="subtitle2" fontWeight="bold">
                        설비명
                      </Typography>
                    </TableCell>
                    <TableCell align="center">
                      <Typography variant="subtitle2" fontWeight="bold">
                        가동률
                      </Typography>
                    </TableCell>
                    <TableCell align="right">
                      <Typography variant="subtitle2" fontWeight="bold">
                        가동시간(분)
                      </Typography>
                    </TableCell>
                    <TableCell align="right">
                      <Typography variant="subtitle2" fontWeight="bold">
                        다운타임(분)
                      </Typography>
                    </TableCell>
                    <TableCell align="center">
                      <Typography variant="subtitle2" fontWeight="bold">
                        보전횟수
                      </Typography>
                    </TableCell>
                    <TableCell align="right">
                      <Typography variant="subtitle2" fontWeight="bold">
                        불량수
                      </Typography>
                    </TableCell>
                    <TableCell align="center">
                      <Typography variant="subtitle2" fontWeight="bold">
                        OEE
                      </Typography>
                    </TableCell>
                    <TableCell align="center">
                      <Typography variant="subtitle2" fontWeight="bold">
                        상태
                      </Typography>
                    </TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {equipmentReportData.map((row, index) => (
                    <TableRow key={index} hover>
                      <TableCell>
                        <Typography fontWeight="medium">{row.equipmentNo}</Typography>
                      </TableCell>
                      <TableCell>{row.equipmentName}</TableCell>
                      <TableCell align="center">
                        <Chip
                          label={`${row.utilizationRate}%`}
                          size="small"
                          color={getEfficiencyColor(row.utilizationRate)}
                        />
                      </TableCell>
                      <TableCell align="right">
                        <Typography color="success.main">{row.operatingTime}</Typography>
                      </TableCell>
                      <TableCell align="right">
                        <Typography color="error.main">{row.downtimeTotal}</Typography>
                      </TableCell>
                      <TableCell align="center">{row.maintenanceCount}</TableCell>
                      <TableCell align="right">
                        <Typography color={row.defectCount > 10 ? 'error' : 'text.secondary'}>
                          {row.defectCount}
                        </Typography>
                      </TableCell>
                      <TableCell align="center">
                        <Chip label={`${row.oee}%`} size="small" color={getEfficiencyColor(row.oee)} />
                      </TableCell>
                      <TableCell align="center">
                        <Chip label={row.status} size="small" color={getStatusColor(row.status)} />
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </Paper>
          </CardContent>
        </TabPanel>
      </Card>
    </Box>
  );
};

export default StatisticalReportsPage;
