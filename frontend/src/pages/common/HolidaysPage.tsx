/**
 * Holidays Page
 * 휴일 관리 페이지
 */

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
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  FormControlLabel,
  Checkbox,
  Grid,
  Chip,
  IconButton,
  Tabs,
  Tab,
  Alert,
  Card,
  CardContent,
  Divider
} from '@mui/material';
import { DataGrid, GridColDef, GridRenderCellParams } from '@mui/x-data-grid';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import CalendarMonthIcon from '@mui/icons-material/CalendarMonth';
import CalculateIcon from '@mui/icons-material/Calculate';
import {
  holidayService,
  workingHoursService,
  Holiday,
  HolidayCreateRequest,
  WorkingHours,
  WorkingHoursCreateRequest,
  getHolidayTypeLabel,
  getHolidayTypeColor,
  getRecurrenceRuleLabel,
  formatTime
} from '../../services/holidayService';

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

function TabPanel(props: TabPanelProps) {
  const { children, value, index, ...other } = props;
  return (
    <div role="tabpanel" hidden={value !== index} {...other}>
      {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
    </div>
  );
}

export default function HolidaysPage() {
  const [currentTab, setCurrentTab] = useState(0);
  const [tenantId] = useState('TENANT001'); // TODO: Get from auth context

  // ==================== Holiday State ====================
  const [holidays, setHolidays] = useState<Holiday[]>([]);
  const [selectedYear, setSelectedYear] = useState(new Date().getFullYear());
  const [holidayDialogOpen, setHolidayDialogOpen] = useState(false);
  const [deleteHolidayDialogOpen, setDeleteHolidayDialogOpen] = useState(false);
  const [selectedHoliday, setSelectedHoliday] = useState<Holiday | null>(null);
  const [holidayFormData, setHolidayFormData] = useState<HolidayCreateRequest>({
    tenantId: tenantId,
    holidayName: '',
    holidayDate: '',
    holidayType: 'NATIONAL',
    isRecurring: false,
    isWorkingDay: false
  });

  // ==================== Working Hours State ====================
  const [workingHours, setWorkingHours] = useState<WorkingHours[]>([]);
  const [workingHoursDialogOpen, setWorkingHoursDialogOpen] = useState(false);
  const [deleteWorkingHoursDialogOpen, setDeleteWorkingHoursDialogOpen] = useState(false);
  const [selectedWorkingHours, setSelectedWorkingHours] = useState<WorkingHours | null>(null);
  const [workingHoursFormData, setWorkingHoursFormData] = useState<WorkingHoursCreateRequest>({
    tenantId: tenantId,
    scheduleName: '',
    isDefault: false
  });

  // ==================== Business Day Calculator State ====================
  const [calculatorType, setCalculatorType] = useState<'check' | 'calculate' | 'add'>('check');
  const [checkDate, setCheckDate] = useState('');
  const [checkResult, setCheckResult] = useState<any>(null);
  const [calcStartDate, setCalcStartDate] = useState('');
  const [calcEndDate, setCalcEndDate] = useState('');
  const [calcResult, setCalcResult] = useState<any>(null);
  const [addStartDate, setAddStartDate] = useState('');
  const [addDays, setAddDays] = useState(0);
  const [addResult, setAddResult] = useState<any>(null);

  // ==================== General State ====================
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // ==================== Effects ====================

  useEffect(() => {
    loadHolidays();
    loadWorkingHours();
  }, [selectedYear]);

  // ==================== Data Loading ====================

  const loadHolidays = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await holidayService.getHolidaysByYear(tenantId, selectedYear);
      setHolidays(data);
    } catch (err: any) {
      setError(err.message || '휴일 목록 로드 실패');
    } finally {
      setLoading(false);
    }
  };

  const loadWorkingHours = async () => {
    try {
      const data = await workingHoursService.getAllWorkingHours(tenantId);
      setWorkingHours(data);
    } catch (err: any) {
      console.error('Failed to load working hours:', err);
    }
  };

  // ==================== Holiday Handlers ====================

  const handleCreateHoliday = () => {
    setSelectedHoliday(null);
    setHolidayFormData({
      tenantId: tenantId,
      holidayName: '',
      holidayDate: '',
      holidayType: 'NATIONAL',
      isRecurring: false,
      isWorkingDay: false
    });
    setHolidayDialogOpen(true);
  };

  const handleEditHoliday = (holiday: Holiday) => {
    setSelectedHoliday(holiday);
    setHolidayFormData({
      tenantId: holiday.tenantId,
      holidayName: holiday.holidayName,
      holidayDate: holiday.holidayDate,
      holidayType: holiday.holidayType,
      isRecurring: holiday.isRecurring,
      recurrenceRule: holiday.recurrenceRule,
      isWorkingDay: holiday.isWorkingDay,
      description: holiday.description,
      remarks: holiday.remarks
    });
    setHolidayDialogOpen(true);
  };

  const handleDeleteHoliday = (holiday: Holiday) => {
    setSelectedHoliday(holiday);
    setDeleteHolidayDialogOpen(true);
  };

  const handleSaveHoliday = async () => {
    try {
      if (selectedHoliday) {
        await holidayService.updateHoliday(selectedHoliday.holidayId, holidayFormData);
      } else {
        await holidayService.createHoliday(holidayFormData);
      }
      setHolidayDialogOpen(false);
      loadHolidays();
    } catch (err: any) {
      setError(err.message || '휴일 저장 실패');
    }
  };

  const handleConfirmDeleteHoliday = async () => {
    if (!selectedHoliday) return;
    try {
      await holidayService.deleteHoliday(selectedHoliday.holidayId);
      setDeleteHolidayDialogOpen(false);
      loadHolidays();
    } catch (err: any) {
      setError(err.message || '휴일 삭제 실패');
    }
  };

  // ==================== Working Hours Handlers ====================

  const handleCreateWorkingHours = () => {
    setSelectedWorkingHours(null);
    setWorkingHoursFormData({
      tenantId: tenantId,
      scheduleName: '',
      isDefault: false,
      mondayStart: '09:00',
      mondayEnd: '18:00',
      tuesdayStart: '09:00',
      tuesdayEnd: '18:00',
      wednesdayStart: '09:00',
      wednesdayEnd: '18:00',
      thursdayStart: '09:00',
      thursdayEnd: '18:00',
      fridayStart: '09:00',
      fridayEnd: '18:00',
      breakStart1: '12:00',
      breakEnd1: '13:00'
    });
    setWorkingHoursDialogOpen(true);
  };

  const handleEditWorkingHours = (wh: WorkingHours) => {
    setSelectedWorkingHours(wh);
    setWorkingHoursFormData({
      tenantId: wh.tenantId,
      scheduleName: wh.scheduleName,
      description: wh.description,
      mondayStart: wh.mondayStart,
      mondayEnd: wh.mondayEnd,
      tuesdayStart: wh.tuesdayStart,
      tuesdayEnd: wh.tuesdayEnd,
      wednesdayStart: wh.wednesdayStart,
      wednesdayEnd: wh.wednesdayEnd,
      thursdayStart: wh.thursdayStart,
      thursdayEnd: wh.thursdayEnd,
      fridayStart: wh.fridayStart,
      fridayEnd: wh.fridayEnd,
      saturdayStart: wh.saturdayStart,
      saturdayEnd: wh.saturdayEnd,
      sundayStart: wh.sundayStart,
      sundayEnd: wh.sundayEnd,
      breakStart1: wh.breakStart1,
      breakEnd1: wh.breakEnd1,
      breakStart2: wh.breakStart2,
      breakEnd2: wh.breakEnd2,
      effectiveFrom: wh.effectiveFrom,
      effectiveTo: wh.effectiveTo,
      isDefault: wh.isDefault
    });
    setWorkingHoursDialogOpen(true);
  };

  const handleDeleteWorkingHours = (wh: WorkingHours) => {
    setSelectedWorkingHours(wh);
    setDeleteWorkingHoursDialogOpen(true);
  };

  const handleSaveWorkingHours = async () => {
    try {
      if (selectedWorkingHours) {
        await workingHoursService.updateWorkingHours(
          selectedWorkingHours.workingHoursId,
          workingHoursFormData
        );
      } else {
        await workingHoursService.createWorkingHours(workingHoursFormData);
      }
      setWorkingHoursDialogOpen(false);
      loadWorkingHours();
    } catch (err: any) {
      setError(err.message || '근무 시간 저장 실패');
    }
  };

  const handleConfirmDeleteWorkingHours = async () => {
    if (!selectedWorkingHours) return;
    try {
      await workingHoursService.deleteWorkingHours(selectedWorkingHours.workingHoursId);
      setDeleteWorkingHoursDialogOpen(false);
      loadWorkingHours();
    } catch (err: any) {
      setError(err.message || '근무 시간 삭제 실패');
    }
  };

  const handleSetDefaultWorkingHours = async (wh: WorkingHours) => {
    try {
      await workingHoursService.setAsDefault(wh.workingHoursId, tenantId);
      loadWorkingHours();
    } catch (err: any) {
      setError(err.message || '기본 설정 실패');
    }
  };

  // ==================== Business Day Calculator Handlers ====================

  const handleCheckBusinessDay = async () => {
    try {
      const result = await holidayService.checkBusinessDay(tenantId, checkDate);
      setCheckResult(result);
    } catch (err: any) {
      setError(err.message || '영업일 확인 실패');
    }
  };

  const handleCalculateBusinessDays = async () => {
    try {
      const result = await holidayService.calculateBusinessDays(tenantId, calcStartDate, calcEndDate);
      setCalcResult(result);
    } catch (err: any) {
      setError(err.message || '영업일 계산 실패');
    }
  };

  const handleAddBusinessDays = async () => {
    try {
      const result = await holidayService.addBusinessDays(tenantId, addStartDate, addDays);
      setAddResult(result);
    } catch (err: any) {
      setError(err.message || '영업일 더하기 실패');
    }
  };

  // ==================== Column Definitions ====================

  const holidayColumns: GridColDef[] = [
    {
      field: 'holidayDate',
      headerName: '날짜',
      width: 150,
      valueFormatter: (params) => {
        const date = new Date(params.value);
        return date.toLocaleDateString('ko-KR', { year: 'numeric', month: 'long', day: 'numeric' });
      }
    },
    { field: 'holidayName', headerName: '휴일명', width: 200 },
    {
      field: 'holidayType',
      headerName: '타입',
      width: 120,
      renderCell: (params: GridRenderCellParams) => (
        <Chip
          label={getHolidayTypeLabel(params.value)}
          color={getHolidayTypeColor(params.value)}
          size="small"
        />
      )
    },
    {
      field: 'isRecurring',
      headerName: '반복',
      width: 100,
      renderCell: (params: GridRenderCellParams) => (
        params.value ? (
          <Chip label={getRecurrenceRuleLabel(params.row.recurrenceRule)} size="small" variant="outlined" />
        ) : (
          <Typography variant="body2" color="text.secondary">-</Typography>
        )
      )
    },
    {
      field: 'isWorkingDay',
      headerName: '근무일',
      width: 100,
      renderCell: (params: GridRenderCellParams) => (
        <Chip
          label={params.value ? '근무' : '휴무'}
          color={params.value ? 'success' : 'default'}
          size="small"
          variant="outlined"
        />
      )
    },
    { field: 'description', headerName: '설명', width: 250 },
    {
      field: 'actions',
      headerName: '작업',
      width: 120,
      sortable: false,
      renderCell: (params: GridRenderCellParams) => (
        <>
          <IconButton size="small" onClick={() => handleEditHoliday(params.row)}>
            <EditIcon fontSize="small" />
          </IconButton>
          <IconButton size="small" onClick={() => handleDeleteHoliday(params.row)}>
            <DeleteIcon fontSize="small" />
          </IconButton>
        </>
      )
    }
  ];

  const workingHoursColumns: GridColDef[] = [
    { field: 'scheduleName', headerName: '스케줄명', width: 200 },
    {
      field: 'isDefault',
      headerName: '기본',
      width: 80,
      renderCell: (params: GridRenderCellParams) => (
        params.value ? <Chip label="기본" color="primary" size="small" /> : null
      )
    },
    {
      field: 'mondayStart',
      headerName: '월',
      width: 100,
      valueFormatter: (params) => {
        if (!params.value) return '-';
        return `${formatTime(params.value)}-${formatTime(params.api.getRow(params.id).mondayEnd)}`;
      }
    },
    {
      field: 'tuesdayStart',
      headerName: '화',
      width: 100,
      valueFormatter: (params) => {
        if (!params.value) return '-';
        return `${formatTime(params.value)}-${formatTime(params.api.getRow(params.id).tuesdayEnd)}`;
      }
    },
    {
      field: 'wednesdayStart',
      headerName: '수',
      width: 100,
      valueFormatter: (params) => {
        if (!params.value) return '-';
        return `${formatTime(params.value)}-${formatTime(params.api.getRow(params.id).wednesdayEnd)}`;
      }
    },
    {
      field: 'thursdayStart',
      headerName: '목',
      width: 100,
      valueFormatter: (params) => {
        if (!params.value) return '-';
        return `${formatTime(params.value)}-${formatTime(params.api.getRow(params.id).thursdayEnd)}`;
      }
    },
    {
      field: 'fridayStart',
      headerName: '금',
      width: 100,
      valueFormatter: (params) => {
        if (!params.value) return '-';
        return `${formatTime(params.value)}-${formatTime(params.api.getRow(params.id).fridayEnd)}`;
      }
    },
    {
      field: 'saturdayStart',
      headerName: '토',
      width: 100,
      valueFormatter: (params) => {
        if (!params.value) return '-';
        return `${formatTime(params.value)}-${formatTime(params.api.getRow(params.id).saturdayEnd)}`;
      }
    },
    {
      field: 'sundayStart',
      headerName: '일',
      width: 100,
      valueFormatter: (params) => {
        if (!params.value) return '-';
        return `${formatTime(params.value)}-${formatTime(params.api.getRow(params.id).sundayEnd)}`;
      }
    },
    {
      field: 'actions',
      headerName: '작업',
      width: 150,
      sortable: false,
      renderCell: (params: GridRenderCellParams) => (
        <>
          {!params.row.isDefault && (
            <Button size="small" onClick={() => handleSetDefaultWorkingHours(params.row)}>
              기본설정
            </Button>
          )}
          <IconButton size="small" onClick={() => handleEditWorkingHours(params.row)}>
            <EditIcon fontSize="small" />
          </IconButton>
          <IconButton size="small" onClick={() => handleDeleteWorkingHours(params.row)}>
            <DeleteIcon fontSize="small" />
          </IconButton>
        </>
      )
    }
  ];

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        <CalendarMonthIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
        휴일 관리
      </Typography>

      {error && (
        <Alert severity="error" onClose={() => setError(null)} sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      <Paper sx={{ width: '100%' }}>
        <Tabs value={currentTab} onChange={(e, v) => setCurrentTab(v)}>
          <Tab label="휴일 관리" />
          <Tab label="근무 시간 설정" />
          <Tab label="영업일 계산기" />
        </Tabs>

        {/* Tab 1: Holiday Management */}
        <TabPanel value={currentTab} index={0}>
          <Box sx={{ mb: 2, display: 'flex', gap: 2, alignItems: 'center' }}>
            <FormControl sx={{ minWidth: 120 }}>
              <InputLabel>연도</InputLabel>
              <Select
                value={selectedYear}
                label="연도"
                onChange={(e) => setSelectedYear(Number(e.target.value))}
              >
                {[2024, 2025, 2026, 2027, 2028].map((year) => (
                  <MenuItem key={year} value={year}>
                    {year}년
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
            <Button variant="contained" startIcon={<AddIcon />} onClick={handleCreateHoliday}>
              휴일 추가
            </Button>
          </Box>

          <DataGrid
            rows={holidays}
            columns={holidayColumns}
            getRowId={(row) => row.holidayId}
            loading={loading}
            autoHeight
            initialState={{
              pagination: { paginationModel: { pageSize: 20 } }
            }}
            pageSizeOptions={[10, 20, 50]}
          />
        </TabPanel>

        {/* Tab 2: Working Hours Management */}
        <TabPanel value={currentTab} index={1}>
          <Box sx={{ mb: 2 }}>
            <Button variant="contained" startIcon={<AddIcon />} onClick={handleCreateWorkingHours}>
              근무 시간 추가
            </Button>
          </Box>

          <DataGrid
            rows={workingHours}
            columns={workingHoursColumns}
            getRowId={(row) => row.workingHoursId}
            autoHeight
            initialState={{
              pagination: { paginationModel: { pageSize: 10 } }
            }}
            pageSizeOptions={[10, 20]}
          />
        </TabPanel>

        {/* Tab 3: Business Day Calculator */}
        <TabPanel value={currentTab} index={2}>
          <Grid container spacing={3}>
            <Grid item xs={12}>
              <FormControl fullWidth>
                <InputLabel>계산 유형</InputLabel>
                <Select
                  value={calculatorType}
                  label="계산 유형"
                  onChange={(e) => setCalculatorType(e.target.value as any)}
                >
                  <MenuItem value="check">영업일 확인</MenuItem>
                  <MenuItem value="calculate">영업일 수 계산</MenuItem>
                  <MenuItem value="add">영업일 더하기</MenuItem>
                </Select>
              </FormControl>
            </Grid>

            {calculatorType === 'check' && (
              <>
                <Grid item xs={12} md={6}>
                  <TextField
                    fullWidth
                    type="date"
                    label="확인할 날짜"
                    value={checkDate}
                    onChange={(e) => setCheckDate(e.target.value)}
                    InputLabelProps={{ shrink: true }}
                  />
                </Grid>
                <Grid item xs={12} md={6}>
                  <Button
                    fullWidth
                    variant="contained"
                    startIcon={<CalculateIcon />}
                    onClick={handleCheckBusinessDay}
                    sx={{ height: '56px' }}
                  >
                    확인
                  </Button>
                </Grid>
                {checkResult && (
                  <Grid item xs={12}>
                    <Card>
                      <CardContent>
                        <Typography variant="h6" gutterBottom>
                          결과
                        </Typography>
                        <Typography>
                          날짜: {checkResult.date}
                        </Typography>
                        <Typography>
                          요일: {checkResult.dayOfWeek}
                        </Typography>
                        <Typography>
                          영업일 여부:{' '}
                          <Chip
                            label={checkResult.isBusinessDay ? '영업일' : '휴일'}
                            color={checkResult.isBusinessDay ? 'success' : 'default'}
                          />
                        </Typography>
                      </CardContent>
                    </Card>
                  </Grid>
                )}
              </>
            )}

            {calculatorType === 'calculate' && (
              <>
                <Grid item xs={12} md={5}>
                  <TextField
                    fullWidth
                    type="date"
                    label="시작일"
                    value={calcStartDate}
                    onChange={(e) => setCalcStartDate(e.target.value)}
                    InputLabelProps={{ shrink: true }}
                  />
                </Grid>
                <Grid item xs={12} md={5}>
                  <TextField
                    fullWidth
                    type="date"
                    label="종료일"
                    value={calcEndDate}
                    onChange={(e) => setCalcEndDate(e.target.value)}
                    InputLabelProps={{ shrink: true }}
                  />
                </Grid>
                <Grid item xs={12} md={2}>
                  <Button
                    fullWidth
                    variant="contained"
                    startIcon={<CalculateIcon />}
                    onClick={handleCalculateBusinessDays}
                    sx={{ height: '56px' }}
                  >
                    계산
                  </Button>
                </Grid>
                {calcResult && (
                  <Grid item xs={12}>
                    <Card>
                      <CardContent>
                        <Typography variant="h6" gutterBottom>
                          결과
                        </Typography>
                        <Typography>
                          기간: {calcResult.startDate} ~ {calcResult.endDate}
                        </Typography>
                        <Typography>
                          전체 일수: {calcResult.totalDays}일
                        </Typography>
                        <Typography variant="h5" color="primary">
                          영업일 수: {calcResult.businessDays}일
                        </Typography>
                      </CardContent>
                    </Card>
                  </Grid>
                )}
              </>
            )}

            {calculatorType === 'add' && (
              <>
                <Grid item xs={12} md={5}>
                  <TextField
                    fullWidth
                    type="date"
                    label="시작일"
                    value={addStartDate}
                    onChange={(e) => setAddStartDate(e.target.value)}
                    InputLabelProps={{ shrink: true }}
                  />
                </Grid>
                <Grid item xs={12} md={5}>
                  <TextField
                    fullWidth
                    type="number"
                    label="더할 영업일 수"
                    value={addDays}
                    onChange={(e) => setAddDays(Number(e.target.value))}
                  />
                </Grid>
                <Grid item xs={12} md={2}>
                  <Button
                    fullWidth
                    variant="contained"
                    startIcon={<CalculateIcon />}
                    onClick={handleAddBusinessDays}
                    sx={{ height: '56px' }}
                  >
                    계산
                  </Button>
                </Grid>
                {addResult && (
                  <Grid item xs={12}>
                    <Card>
                      <CardContent>
                        <Typography variant="h6" gutterBottom>
                          결과
                        </Typography>
                        <Typography>
                          시작일: {addResult.startDate}
                        </Typography>
                        <Typography>
                          더한 영업일: {addResult.businessDaysAdded}일
                        </Typography>
                        <Typography variant="h5" color="primary">
                          결과 날짜: {addResult.resultDate} ({addResult.dayOfWeek})
                        </Typography>
                      </CardContent>
                    </Card>
                  </Grid>
                )}
              </>
            )}
          </Grid>
        </TabPanel>
      </Paper>

      {/* Holiday Create/Edit Dialog */}
      <Dialog open={holidayDialogOpen} onClose={() => setHolidayDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{selectedHoliday ? '휴일 수정' : '휴일 추가'}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="휴일명"
                value={holidayFormData.holidayName}
                onChange={(e) => setHolidayFormData({ ...holidayFormData, holidayName: e.target.value })}
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                type="date"
                label="날짜"
                value={holidayFormData.holidayDate}
                onChange={(e) => setHolidayFormData({ ...holidayFormData, holidayDate: e.target.value })}
                InputLabelProps={{ shrink: true }}
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControl fullWidth>
                <InputLabel>휴일 타입</InputLabel>
                <Select
                  value={holidayFormData.holidayType}
                  label="휴일 타입"
                  onChange={(e) =>
                    setHolidayFormData({ ...holidayFormData, holidayType: e.target.value as any })
                  }
                >
                  <MenuItem value="NATIONAL">국경일</MenuItem>
                  <MenuItem value="COMPANY">회사 휴일</MenuItem>
                  <MenuItem value="SPECIAL">특별 휴일</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12}>
              <FormControlLabel
                control={
                  <Checkbox
                    checked={holidayFormData.isRecurring}
                    onChange={(e) =>
                      setHolidayFormData({ ...holidayFormData, isRecurring: e.target.checked })
                    }
                  />
                }
                label="반복 휴일"
              />
            </Grid>
            {holidayFormData.isRecurring && (
              <Grid item xs={12}>
                <FormControl fullWidth>
                  <InputLabel>반복 규칙</InputLabel>
                  <Select
                    value={holidayFormData.recurrenceRule || ''}
                    label="반복 규칙"
                    onChange={(e) =>
                      setHolidayFormData({ ...holidayFormData, recurrenceRule: e.target.value })
                    }
                  >
                    <MenuItem value="YEARLY">매년</MenuItem>
                    <MenuItem value="MONTHLY">매월</MenuItem>
                    <MenuItem value="LUNAR">음력</MenuItem>
                  </Select>
                </FormControl>
              </Grid>
            )}
            <Grid item xs={12}>
              <FormControlLabel
                control={
                  <Checkbox
                    checked={holidayFormData.isWorkingDay}
                    onChange={(e) =>
                      setHolidayFormData({ ...holidayFormData, isWorkingDay: e.target.checked })
                    }
                  />
                }
                label="근무일 (대체 근무일)"
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                multiline
                rows={3}
                label="설명"
                value={holidayFormData.description || ''}
                onChange={(e) => setHolidayFormData({ ...holidayFormData, description: e.target.value })}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setHolidayDialogOpen(false)}>취소</Button>
          <Button variant="contained" onClick={handleSaveHoliday}>
            저장
          </Button>
        </DialogActions>
      </Dialog>

      {/* Holiday Delete Confirmation */}
      <Dialog open={deleteHolidayDialogOpen} onClose={() => setDeleteHolidayDialogOpen(false)}>
        <DialogTitle>휴일 삭제</DialogTitle>
        <DialogContent>
          <Typography>
            '{selectedHoliday?.holidayName}' 휴일을 삭제하시겠습니까?
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteHolidayDialogOpen(false)}>취소</Button>
          <Button variant="contained" color="error" onClick={handleConfirmDeleteHoliday}>
            삭제
          </Button>
        </DialogActions>
      </Dialog>

      {/* Working Hours Create/Edit Dialog */}
      <Dialog
        open={workingHoursDialogOpen}
        onClose={() => setWorkingHoursDialogOpen(false)}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle>{selectedWorkingHours ? '근무 시간 수정' : '근무 시간 추가'}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="스케줄명"
                value={workingHoursFormData.scheduleName}
                onChange={(e) =>
                  setWorkingHoursFormData({ ...workingHoursFormData, scheduleName: e.target.value })
                }
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Checkbox
                    checked={workingHoursFormData.isDefault}
                    onChange={(e) =>
                      setWorkingHoursFormData({ ...workingHoursFormData, isDefault: e.target.checked })
                    }
                  />
                }
                label="기본 스케줄로 설정"
              />
            </Grid>

            <Grid item xs={12}>
              <Divider sx={{ my: 1 }}>요일별 근무시간</Divider>
            </Grid>

            {/* Monday */}
            <Grid item xs={12} md={4}>
              <Typography>월요일</Typography>
            </Grid>
            <Grid item xs={6} md={4}>
              <TextField
                fullWidth
                type="time"
                label="시작"
                value={workingHoursFormData.mondayStart || ''}
                onChange={(e) =>
                  setWorkingHoursFormData({ ...workingHoursFormData, mondayStart: e.target.value })
                }
                InputLabelProps={{ shrink: true }}
              />
            </Grid>
            <Grid item xs={6} md={4}>
              <TextField
                fullWidth
                type="time"
                label="종료"
                value={workingHoursFormData.mondayEnd || ''}
                onChange={(e) =>
                  setWorkingHoursFormData({ ...workingHoursFormData, mondayEnd: e.target.value })
                }
                InputLabelProps={{ shrink: true }}
              />
            </Grid>

            {/* Tuesday */}
            <Grid item xs={12} md={4}>
              <Typography>화요일</Typography>
            </Grid>
            <Grid item xs={6} md={4}>
              <TextField
                fullWidth
                type="time"
                label="시작"
                value={workingHoursFormData.tuesdayStart || ''}
                onChange={(e) =>
                  setWorkingHoursFormData({ ...workingHoursFormData, tuesdayStart: e.target.value })
                }
                InputLabelProps={{ shrink: true }}
              />
            </Grid>
            <Grid item xs={6} md={4}>
              <TextField
                fullWidth
                type="time"
                label="종료"
                value={workingHoursFormData.tuesdayEnd || ''}
                onChange={(e) =>
                  setWorkingHoursFormData({ ...workingHoursFormData, tuesdayEnd: e.target.value })
                }
                InputLabelProps={{ shrink: true }}
              />
            </Grid>

            {/* Wednesday */}
            <Grid item xs={12} md={4}>
              <Typography>수요일</Typography>
            </Grid>
            <Grid item xs={6} md={4}>
              <TextField
                fullWidth
                type="time"
                label="시작"
                value={workingHoursFormData.wednesdayStart || ''}
                onChange={(e) =>
                  setWorkingHoursFormData({ ...workingHoursFormData, wednesdayStart: e.target.value })
                }
                InputLabelProps={{ shrink: true }}
              />
            </Grid>
            <Grid item xs={6} md={4}>
              <TextField
                fullWidth
                type="time"
                label="종료"
                value={workingHoursFormData.wednesdayEnd || ''}
                onChange={(e) =>
                  setWorkingHoursFormData({ ...workingHoursFormData, wednesdayEnd: e.target.value })
                }
                InputLabelProps={{ shrink: true }}
              />
            </Grid>

            {/* Thursday */}
            <Grid item xs={12} md={4}>
              <Typography>목요일</Typography>
            </Grid>
            <Grid item xs={6} md={4}>
              <TextField
                fullWidth
                type="time"
                label="시작"
                value={workingHoursFormData.thursdayStart || ''}
                onChange={(e) =>
                  setWorkingHoursFormData({ ...workingHoursFormData, thursdayStart: e.target.value })
                }
                InputLabelProps={{ shrink: true }}
              />
            </Grid>
            <Grid item xs={6} md={4}>
              <TextField
                fullWidth
                type="time"
                label="종료"
                value={workingHoursFormData.thursdayEnd || ''}
                onChange={(e) =>
                  setWorkingHoursFormData({ ...workingHoursFormData, thursdayEnd: e.target.value })
                }
                InputLabelProps={{ shrink: true }}
              />
            </Grid>

            {/* Friday */}
            <Grid item xs={12} md={4}>
              <Typography>금요일</Typography>
            </Grid>
            <Grid item xs={6} md={4}>
              <TextField
                fullWidth
                type="time"
                label="시작"
                value={workingHoursFormData.fridayStart || ''}
                onChange={(e) =>
                  setWorkingHoursFormData({ ...workingHoursFormData, fridayStart: e.target.value })
                }
                InputLabelProps={{ shrink: true }}
              />
            </Grid>
            <Grid item xs={6} md={4}>
              <TextField
                fullWidth
                type="time"
                label="종료"
                value={workingHoursFormData.fridayEnd || ''}
                onChange={(e) =>
                  setWorkingHoursFormData({ ...workingHoursFormData, fridayEnd: e.target.value })
                }
                InputLabelProps={{ shrink: true }}
              />
            </Grid>

            {/* Saturday */}
            <Grid item xs={12} md={4}>
              <Typography>토요일</Typography>
            </Grid>
            <Grid item xs={6} md={4}>
              <TextField
                fullWidth
                type="time"
                label="시작"
                value={workingHoursFormData.saturdayStart || ''}
                onChange={(e) =>
                  setWorkingHoursFormData({ ...workingHoursFormData, saturdayStart: e.target.value })
                }
                InputLabelProps={{ shrink: true }}
              />
            </Grid>
            <Grid item xs={6} md={4}>
              <TextField
                fullWidth
                type="time"
                label="종료"
                value={workingHoursFormData.saturdayEnd || ''}
                onChange={(e) =>
                  setWorkingHoursFormData({ ...workingHoursFormData, saturdayEnd: e.target.value })
                }
                InputLabelProps={{ shrink: true }}
              />
            </Grid>

            {/* Sunday */}
            <Grid item xs={12} md={4}>
              <Typography>일요일</Typography>
            </Grid>
            <Grid item xs={6} md={4}>
              <TextField
                fullWidth
                type="time"
                label="시작"
                value={workingHoursFormData.sundayStart || ''}
                onChange={(e) =>
                  setWorkingHoursFormData({ ...workingHoursFormData, sundayStart: e.target.value })
                }
                InputLabelProps={{ shrink: true }}
              />
            </Grid>
            <Grid item xs={6} md={4}>
              <TextField
                fullWidth
                type="time"
                label="종료"
                value={workingHoursFormData.sundayEnd || ''}
                onChange={(e) =>
                  setWorkingHoursFormData({ ...workingHoursFormData, sundayEnd: e.target.value })
                }
                InputLabelProps={{ shrink: true }}
              />
            </Grid>

            <Grid item xs={12}>
              <Divider sx={{ my: 1 }}>휴식시간</Divider>
            </Grid>

            {/* Break 1 */}
            <Grid item xs={12} md={4}>
              <Typography>휴식시간 1</Typography>
            </Grid>
            <Grid item xs={6} md={4}>
              <TextField
                fullWidth
                type="time"
                label="시작"
                value={workingHoursFormData.breakStart1 || ''}
                onChange={(e) =>
                  setWorkingHoursFormData({ ...workingHoursFormData, breakStart1: e.target.value })
                }
                InputLabelProps={{ shrink: true }}
              />
            </Grid>
            <Grid item xs={6} md={4}>
              <TextField
                fullWidth
                type="time"
                label="종료"
                value={workingHoursFormData.breakEnd1 || ''}
                onChange={(e) =>
                  setWorkingHoursFormData({ ...workingHoursFormData, breakEnd1: e.target.value })
                }
                InputLabelProps={{ shrink: true }}
              />
            </Grid>

            {/* Break 2 */}
            <Grid item xs={12} md={4}>
              <Typography>휴식시간 2</Typography>
            </Grid>
            <Grid item xs={6} md={4}>
              <TextField
                fullWidth
                type="time"
                label="시작"
                value={workingHoursFormData.breakStart2 || ''}
                onChange={(e) =>
                  setWorkingHoursFormData({ ...workingHoursFormData, breakStart2: e.target.value })
                }
                InputLabelProps={{ shrink: true }}
              />
            </Grid>
            <Grid item xs={6} md={4}>
              <TextField
                fullWidth
                type="time"
                label="종료"
                value={workingHoursFormData.breakEnd2 || ''}
                onChange={(e) =>
                  setWorkingHoursFormData({ ...workingHoursFormData, breakEnd2: e.target.value })
                }
                InputLabelProps={{ shrink: true }}
              />
            </Grid>

            <Grid item xs={12}>
              <TextField
                fullWidth
                multiline
                rows={2}
                label="설명"
                value={workingHoursFormData.description || ''}
                onChange={(e) =>
                  setWorkingHoursFormData({ ...workingHoursFormData, description: e.target.value })
                }
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setWorkingHoursDialogOpen(false)}>취소</Button>
          <Button variant="contained" onClick={handleSaveWorkingHours}>
            저장
          </Button>
        </DialogActions>
      </Dialog>

      {/* Working Hours Delete Confirmation */}
      <Dialog
        open={deleteWorkingHoursDialogOpen}
        onClose={() => setDeleteWorkingHoursDialogOpen(false)}
      >
        <DialogTitle>근무 시간 삭제</DialogTitle>
        <DialogContent>
          <Typography>
            '{selectedWorkingHours?.scheduleName}' 근무 시간을 삭제하시겠습니까?
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteWorkingHoursDialogOpen(false)}>취소</Button>
          <Button variant="contained" color="error" onClick={handleConfirmDeleteWorkingHours}>
            삭제
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
