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
import { useTranslation } from 'react-i18next';
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
  formatTime,
  BusinessDayCheckResult,
  BusinessDayCalculationResult,
  BusinessDayAddResult,
} from '../../services/holidayService';
import { useAuthStore } from '@/stores/authStore';
import { getErrorMessage } from '@/utils/errorUtils';

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
  const { t } = useTranslation();
  const { user } = useAuthStore();
  const [currentTab, setCurrentTab] = useState(0);
  const tenantId = user?.tenantId ?? '';

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
  const [checkResult, setCheckResult] = useState<BusinessDayCheckResult | null>(null);
  const [calcStartDate, setCalcStartDate] = useState('');
  const [calcEndDate, setCalcEndDate] = useState('');
  const [calcResult, setCalcResult] = useState<BusinessDayCalculationResult | null>(null);
  const [addStartDate, setAddStartDate] = useState('');
  const [addDays, setAddDays] = useState(0);
  const [addResult, setAddResult] = useState<BusinessDayAddResult | null>(null);

  // ==================== General State ====================
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // ==================== Effects ====================

  useEffect(() => {
    loadHolidays();
    loadWorkingHours();
    // eslint-disable-next-line react-hooks/exhaustive-deps -- reload when selected year changes
  }, [selectedYear]);

  // ==================== Data Loading ====================

  const loadHolidays = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await holidayService.getHolidaysByYear(tenantId, selectedYear);
      setHolidays(data || []);
    } catch (err) {
      setError(getErrorMessage(err, t('pages.holidays.errors.loadFailed')));
      setHolidays([]);
    } finally {
      setLoading(false);
    }
  };

  const loadWorkingHours = async () => {
    try {
      const data = await workingHoursService.getAllWorkingHours(tenantId);
      setWorkingHours(data || []);
    } catch (_err) {
      setWorkingHours([]);
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
    } catch (err) {
      setError(getErrorMessage(err, t('pages.holidays.errors.saveFailed')));
    }
  };

  const handleConfirmDeleteHoliday = async () => {
    if (!selectedHoliday) return;
    try {
      await holidayService.deleteHoliday(selectedHoliday.holidayId);
      setDeleteHolidayDialogOpen(false);
      loadHolidays();
    } catch (err) {
      setError(getErrorMessage(err, t('pages.holidays.errors.deleteFailed')));
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
    } catch (err) {
      setError(getErrorMessage(err, t('pages.holidays.errors.workingHoursSaveFailed')));
    }
  };

  const handleConfirmDeleteWorkingHours = async () => {
    if (!selectedWorkingHours) return;
    try {
      await workingHoursService.deleteWorkingHours(selectedWorkingHours.workingHoursId);
      setDeleteWorkingHoursDialogOpen(false);
      loadWorkingHours();
    } catch (err) {
      setError(getErrorMessage(err, t('pages.holidays.errors.workingHoursDeleteFailed')));
    }
  };

  const handleSetDefaultWorkingHours = async (wh: WorkingHours) => {
    try {
      await workingHoursService.setAsDefault(wh.workingHoursId, tenantId);
      loadWorkingHours();
    } catch (err) {
      setError(getErrorMessage(err, t('pages.holidays.errors.setDefaultFailed')));
    }
  };

  // ==================== Business Day Calculator Handlers ====================

  const handleCheckBusinessDay = async () => {
    try {
      const result = await holidayService.checkBusinessDay(tenantId, checkDate);
      setCheckResult(result);
    } catch (err) {
      setError(getErrorMessage(err, t('pages.holidays.errors.checkBusinessDayFailed')));
    }
  };

  const handleCalculateBusinessDays = async () => {
    try {
      const result = await holidayService.calculateBusinessDays(tenantId, calcStartDate, calcEndDate);
      setCalcResult(result);
    } catch (err) {
      setError(getErrorMessage(err, t('pages.holidays.errors.calculateBusinessDaysFailed')));
    }
  };

  const handleAddBusinessDays = async () => {
    try {
      const result = await holidayService.addBusinessDays(tenantId, addStartDate, addDays);
      setAddResult(result);
    } catch (err) {
      setError(getErrorMessage(err, t('pages.holidays.errors.addBusinessDaysFailed')));
    }
  };

  // ==================== Column Definitions ====================

  const holidayColumns: GridColDef[] = [
    {
      field: 'holidayDate',
      headerName: t('pages.holidays.fields.date'),
      width: 150,
      valueFormatter: (params) => {
        const date = new Date(params.value);
        return date.toLocaleDateString('ko-KR', { year: 'numeric', month: 'long', day: 'numeric' });
      }
    },
    { field: 'holidayName', headerName: t('pages.holidays.fields.name'), width: 200 },
    {
      field: 'holidayType',
      headerName: t('pages.holidays.fields.type'),
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
      headerName: t('pages.holidays.fields.recurring'),
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
      headerName: t('pages.holidays.fields.workingDay'),
      width: 100,
      renderCell: (params: GridRenderCellParams) => (
        <Chip
          label={params.value ? t('pages.holidays.status.working') : t('pages.holidays.status.dayOff')}
          color={params.value ? 'success' : 'default'}
          size="small"
          variant="outlined"
        />
      )
    },
    { field: 'description', headerName: t('common.labels.description'), width: 250 },
    {
      field: 'actions',
      headerName: t('common.labels.actions'),
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
    { field: 'scheduleName', headerName: t('pages.holidays.workingHours.scheduleName'), width: 200 },
    {
      field: 'isDefault',
      headerName: t('pages.holidays.workingHours.default'),
      width: 80,
      renderCell: (params: GridRenderCellParams) => (
        params.value ? <Chip label={t('pages.holidays.workingHours.default')} color="primary" size="small" /> : null
      )
    },
    {
      field: 'mondayStart',
      headerName: t('pages.holidays.weekdays.mon'),
      width: 100,
      valueFormatter: (params) => {
        if (!params.value) return '-';
        return `${formatTime(params.value)}-${formatTime(params.api.getRow(params.id).mondayEnd)}`;
      }
    },
    {
      field: 'tuesdayStart',
      headerName: t('pages.holidays.weekdays.tue'),
      width: 100,
      valueFormatter: (params) => {
        if (!params.value) return '-';
        return `${formatTime(params.value)}-${formatTime(params.api.getRow(params.id).tuesdayEnd)}`;
      }
    },
    {
      field: 'wednesdayStart',
      headerName: t('pages.holidays.weekdays.wed'),
      width: 100,
      valueFormatter: (params) => {
        if (!params.value) return '-';
        return `${formatTime(params.value)}-${formatTime(params.api.getRow(params.id).wednesdayEnd)}`;
      }
    },
    {
      field: 'thursdayStart',
      headerName: t('pages.holidays.weekdays.thu'),
      width: 100,
      valueFormatter: (params) => {
        if (!params.value) return '-';
        return `${formatTime(params.value)}-${formatTime(params.api.getRow(params.id).thursdayEnd)}`;
      }
    },
    {
      field: 'fridayStart',
      headerName: t('pages.holidays.weekdays.fri'),
      width: 100,
      valueFormatter: (params) => {
        if (!params.value) return '-';
        return `${formatTime(params.value)}-${formatTime(params.api.getRow(params.id).fridayEnd)}`;
      }
    },
    {
      field: 'saturdayStart',
      headerName: t('pages.holidays.weekdays.sat'),
      width: 100,
      valueFormatter: (params) => {
        if (!params.value) return '-';
        return `${formatTime(params.value)}-${formatTime(params.api.getRow(params.id).saturdayEnd)}`;
      }
    },
    {
      field: 'sundayStart',
      headerName: t('pages.holidays.weekdays.sun'),
      width: 100,
      valueFormatter: (params) => {
        if (!params.value) return '-';
        return `${formatTime(params.value)}-${formatTime(params.api.getRow(params.id).sundayEnd)}`;
      }
    },
    {
      field: 'actions',
      headerName: t('common.labels.actions'),
      width: 150,
      sortable: false,
      renderCell: (params: GridRenderCellParams) => (
        <>
          {!params.row.isDefault && (
            <Button size="small" onClick={() => handleSetDefaultWorkingHours(params.row)}>
              {t('pages.holidays.actions.setDefault')}
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
        {t('pages.holidays.title')}
      </Typography>

      {error && (
        <Alert severity="error" onClose={() => setError(null)} sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      <Paper sx={{ width: '100%' }}>
        <Tabs value={currentTab} onChange={(e, v) => setCurrentTab(v)}>
          <Tab label={t('pages.holidays.tabs.holidays')} />
          <Tab label={t('pages.holidays.tabs.workingHours')} />
          <Tab label={t('pages.holidays.tabs.calculator')} />
        </Tabs>

        {/* Tab 1: Holiday Management */}
        <TabPanel value={currentTab} index={0}>
          <Box sx={{ mb: 2, display: 'flex', gap: 2, alignItems: 'center' }}>
            <FormControl sx={{ minWidth: 120 }}>
              <InputLabel>{t('pages.holidays.fields.year')}</InputLabel>
              <Select
                value={selectedYear}
                label={t('pages.holidays.fields.year')}
                onChange={(e) => setSelectedYear(Number(e.target.value))}
              >
                {[2024, 2025, 2026, 2027, 2028].map((year) => (
                  <MenuItem key={year} value={year}>
                    {year}{t('pages.holidays.fields.yearSuffix')}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
            <Button variant="contained" startIcon={<AddIcon />} onClick={handleCreateHoliday}>
              {t('pages.holidays.actions.addHoliday')}
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
              {t('pages.holidays.actions.addWorkingHours')}
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
                <InputLabel>{t('pages.holidays.calculator.calcType')}</InputLabel>
                <Select
                  value={calculatorType}
                  label={t('pages.holidays.calculator.calcType')}
                  onChange={(e) => setCalculatorType(e.target.value as 'check' | 'calculate' | 'add')}
                >
                  <MenuItem value="check">{t('pages.holidays.calculator.checkBusinessDay')}</MenuItem>
                  <MenuItem value="calculate">{t('pages.holidays.calculator.calculateBusinessDays')}</MenuItem>
                  <MenuItem value="add">{t('pages.holidays.calculator.addBusinessDays')}</MenuItem>
                </Select>
              </FormControl>
            </Grid>

            {calculatorType === 'check' && (
              <>
                <Grid item xs={12} md={6}>
                  <TextField
                    fullWidth
                    type="date"
                    label={t('pages.holidays.calculator.dateToCheck')}
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
                    {t('common.buttons.confirm')}
                  </Button>
                </Grid>
                {checkResult && (
                  <Grid item xs={12}>
                    <Card>
                      <CardContent>
                        <Typography variant="h6" gutterBottom>
                          {t('pages.holidays.calculator.result')}
                        </Typography>
                        <Typography>
                          {t('pages.holidays.calculator.date')}: {checkResult.date}
                        </Typography>
                        <Typography>
                          {t('pages.holidays.calculator.dayOfWeek')}: {checkResult.dayOfWeek}
                        </Typography>
                        <Typography>
                          {t('pages.holidays.calculator.isBusinessDay')}:{' '}
                          <Chip
                            label={checkResult.isBusinessDay ? t('pages.holidays.calculator.businessDay') : t('pages.holidays.calculator.holiday')}
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
                    label={t('common.labels.startDate')}
                    value={calcStartDate}
                    onChange={(e) => setCalcStartDate(e.target.value)}
                    InputLabelProps={{ shrink: true }}
                  />
                </Grid>
                <Grid item xs={12} md={5}>
                  <TextField
                    fullWidth
                    type="date"
                    label={t('common.labels.endDate')}
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
                    {t('pages.holidays.calculator.calculate')}
                  </Button>
                </Grid>
                {calcResult && (
                  <Grid item xs={12}>
                    <Card>
                      <CardContent>
                        <Typography variant="h6" gutterBottom>
                          {t('pages.holidays.calculator.result')}
                        </Typography>
                        <Typography>
                          {t('pages.holidays.calculator.period')}: {calcResult.startDate} ~ {calcResult.endDate}
                        </Typography>
                        <Typography>
                          {t('pages.holidays.calculator.totalDays')}: {calcResult.totalDays}{t('pages.holidays.calculator.daySuffix')}
                        </Typography>
                        <Typography variant="h5" color="primary">
                          {t('pages.holidays.calculator.businessDayCount')}: {calcResult.businessDays}{t('pages.holidays.calculator.daySuffix')}
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
                    label={t('common.labels.startDate')}
                    value={addStartDate}
                    onChange={(e) => setAddStartDate(e.target.value)}
                    InputLabelProps={{ shrink: true }}
                  />
                </Grid>
                <Grid item xs={12} md={5}>
                  <TextField
                    fullWidth
                    type="number"
                    label={t('pages.holidays.calculator.daysToAdd')}
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
                    {t('pages.holidays.calculator.calculate')}
                  </Button>
                </Grid>
                {addResult && (
                  <Grid item xs={12}>
                    <Card>
                      <CardContent>
                        <Typography variant="h6" gutterBottom>
                          {t('pages.holidays.calculator.result')}
                        </Typography>
                        <Typography>
                          {t('common.labels.startDate')}: {addResult.startDate}
                        </Typography>
                        <Typography>
                          {t('pages.holidays.calculator.addedBusinessDays')}: {addResult.businessDaysAdded}{t('pages.holidays.calculator.daySuffix')}
                        </Typography>
                        <Typography variant="h5" color="primary">
                          {t('pages.holidays.calculator.resultDate')}: {addResult.resultDate} ({addResult.dayOfWeek})
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
        <DialogTitle>{selectedHoliday ? t('pages.holidays.actions.editHoliday') : t('pages.holidays.actions.addHoliday')}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label={t('pages.holidays.fields.name')}
                value={holidayFormData.holidayName}
                onChange={(e) => setHolidayFormData({ ...holidayFormData, holidayName: e.target.value })}
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                type="date"
                label={t('pages.holidays.fields.date')}
                value={holidayFormData.holidayDate}
                onChange={(e) => setHolidayFormData({ ...holidayFormData, holidayDate: e.target.value })}
                InputLabelProps={{ shrink: true }}
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControl fullWidth>
                <InputLabel>{t('pages.holidays.fields.holidayType')}</InputLabel>
                <Select
                  value={holidayFormData.holidayType}
                  label={t('pages.holidays.fields.holidayType')}
                  onChange={(e) =>
                    setHolidayFormData({ ...holidayFormData, holidayType: e.target.value as 'NATIONAL' | 'COMPANY' | 'SPECIAL' })
                  }
                >
                  <MenuItem value="NATIONAL">{t('pages.holidays.types.national')}</MenuItem>
                  <MenuItem value="COMPANY">{t('pages.holidays.types.company')}</MenuItem>
                  <MenuItem value="SPECIAL">{t('pages.holidays.types.special')}</MenuItem>
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
                label={t('pages.holidays.fields.recurringHoliday')}
              />
            </Grid>
            {holidayFormData.isRecurring && (
              <Grid item xs={12}>
                <FormControl fullWidth>
                  <InputLabel>{t('pages.holidays.fields.recurrenceRule')}</InputLabel>
                  <Select
                    value={holidayFormData.recurrenceRule || ''}
                    label={t('pages.holidays.fields.recurrenceRule')}
                    onChange={(e) =>
                      setHolidayFormData({ ...holidayFormData, recurrenceRule: e.target.value })
                    }
                  >
                    <MenuItem value="YEARLY">{t('pages.holidays.recurrence.yearly')}</MenuItem>
                    <MenuItem value="MONTHLY">{t('pages.holidays.recurrence.monthly')}</MenuItem>
                    <MenuItem value="LUNAR">{t('pages.holidays.recurrence.lunar')}</MenuItem>
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
                label={t('pages.holidays.fields.substituteWorkDay')}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                multiline
                rows={3}
                label={t('common.labels.description')}
                value={holidayFormData.description || ''}
                onChange={(e) => setHolidayFormData({ ...holidayFormData, description: e.target.value })}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setHolidayDialogOpen(false)}>{t('common.buttons.cancel')}</Button>
          <Button variant="contained" onClick={handleSaveHoliday}>
            {t('common.buttons.save')}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Holiday Delete Confirmation */}
      <Dialog open={deleteHolidayDialogOpen} onClose={() => setDeleteHolidayDialogOpen(false)}>
        <DialogTitle>{t('pages.holidays.actions.deleteHoliday')}</DialogTitle>
        <DialogContent>
          <Typography>
            {t('pages.holidays.confirmDeleteHoliday', { name: selectedHoliday?.holidayName })}
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteHolidayDialogOpen(false)}>{t('common.buttons.cancel')}</Button>
          <Button variant="contained" color="error" onClick={handleConfirmDeleteHoliday}>
            {t('common.buttons.delete')}
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
        <DialogTitle>{selectedWorkingHours ? t('pages.holidays.workingHours.edit') : t('pages.holidays.workingHours.add')}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label={t('pages.holidays.workingHours.scheduleName')}
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
                label={t('pages.holidays.workingHours.setAsDefault')}
              />
            </Grid>

            <Grid item xs={12}>
              <Divider sx={{ my: 1 }}>{t('pages.holidays.workingHours.dailyHours')}</Divider>
            </Grid>

            {/* Monday */}
            <Grid item xs={12} md={4}>
              <Typography>{t('pages.holidays.weekdays.monday')}</Typography>
            </Grid>
            <Grid item xs={6} md={4}>
              <TextField
                fullWidth
                type="time"
                label={t('pages.holidays.workingHours.start')}
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
                label={t('pages.holidays.workingHours.end')}
                value={workingHoursFormData.mondayEnd || ''}
                onChange={(e) =>
                  setWorkingHoursFormData({ ...workingHoursFormData, mondayEnd: e.target.value })
                }
                InputLabelProps={{ shrink: true }}
              />
            </Grid>

            {/* Tuesday */}
            <Grid item xs={12} md={4}>
              <Typography>{t('pages.holidays.weekdays.tuesday')}</Typography>
            </Grid>
            <Grid item xs={6} md={4}>
              <TextField
                fullWidth
                type="time"
                label={t('pages.holidays.workingHours.start')}
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
                label={t('pages.holidays.workingHours.end')}
                value={workingHoursFormData.tuesdayEnd || ''}
                onChange={(e) =>
                  setWorkingHoursFormData({ ...workingHoursFormData, tuesdayEnd: e.target.value })
                }
                InputLabelProps={{ shrink: true }}
              />
            </Grid>

            {/* Wednesday */}
            <Grid item xs={12} md={4}>
              <Typography>{t('pages.holidays.weekdays.wednesday')}</Typography>
            </Grid>
            <Grid item xs={6} md={4}>
              <TextField
                fullWidth
                type="time"
                label={t('pages.holidays.workingHours.start')}
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
                label={t('pages.holidays.workingHours.end')}
                value={workingHoursFormData.wednesdayEnd || ''}
                onChange={(e) =>
                  setWorkingHoursFormData({ ...workingHoursFormData, wednesdayEnd: e.target.value })
                }
                InputLabelProps={{ shrink: true }}
              />
            </Grid>

            {/* Thursday */}
            <Grid item xs={12} md={4}>
              <Typography>{t('pages.holidays.weekdays.thursday')}</Typography>
            </Grid>
            <Grid item xs={6} md={4}>
              <TextField
                fullWidth
                type="time"
                label={t('pages.holidays.workingHours.start')}
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
                label={t('pages.holidays.workingHours.end')}
                value={workingHoursFormData.thursdayEnd || ''}
                onChange={(e) =>
                  setWorkingHoursFormData({ ...workingHoursFormData, thursdayEnd: e.target.value })
                }
                InputLabelProps={{ shrink: true }}
              />
            </Grid>

            {/* Friday */}
            <Grid item xs={12} md={4}>
              <Typography>{t('pages.holidays.weekdays.friday')}</Typography>
            </Grid>
            <Grid item xs={6} md={4}>
              <TextField
                fullWidth
                type="time"
                label={t('pages.holidays.workingHours.start')}
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
                label={t('pages.holidays.workingHours.end')}
                value={workingHoursFormData.fridayEnd || ''}
                onChange={(e) =>
                  setWorkingHoursFormData({ ...workingHoursFormData, fridayEnd: e.target.value })
                }
                InputLabelProps={{ shrink: true }}
              />
            </Grid>

            {/* Saturday */}
            <Grid item xs={12} md={4}>
              <Typography>{t('pages.holidays.weekdays.saturday')}</Typography>
            </Grid>
            <Grid item xs={6} md={4}>
              <TextField
                fullWidth
                type="time"
                label={t('pages.holidays.workingHours.start')}
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
                label={t('pages.holidays.workingHours.end')}
                value={workingHoursFormData.saturdayEnd || ''}
                onChange={(e) =>
                  setWorkingHoursFormData({ ...workingHoursFormData, saturdayEnd: e.target.value })
                }
                InputLabelProps={{ shrink: true }}
              />
            </Grid>

            {/* Sunday */}
            <Grid item xs={12} md={4}>
              <Typography>{t('pages.holidays.weekdays.sunday')}</Typography>
            </Grid>
            <Grid item xs={6} md={4}>
              <TextField
                fullWidth
                type="time"
                label={t('pages.holidays.workingHours.start')}
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
                label={t('pages.holidays.workingHours.end')}
                value={workingHoursFormData.sundayEnd || ''}
                onChange={(e) =>
                  setWorkingHoursFormData({ ...workingHoursFormData, sundayEnd: e.target.value })
                }
                InputLabelProps={{ shrink: true }}
              />
            </Grid>

            <Grid item xs={12}>
              <Divider sx={{ my: 1 }}>{t('pages.holidays.workingHours.breakTime')}</Divider>
            </Grid>

            {/* Break 1 */}
            <Grid item xs={12} md={4}>
              <Typography>{t('pages.holidays.workingHours.breakTime')} 1</Typography>
            </Grid>
            <Grid item xs={6} md={4}>
              <TextField
                fullWidth
                type="time"
                label={t('pages.holidays.workingHours.start')}
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
                label={t('pages.holidays.workingHours.end')}
                value={workingHoursFormData.breakEnd1 || ''}
                onChange={(e) =>
                  setWorkingHoursFormData({ ...workingHoursFormData, breakEnd1: e.target.value })
                }
                InputLabelProps={{ shrink: true }}
              />
            </Grid>

            {/* Break 2 */}
            <Grid item xs={12} md={4}>
              <Typography>{t('pages.holidays.workingHours.breakTime')} 2</Typography>
            </Grid>
            <Grid item xs={6} md={4}>
              <TextField
                fullWidth
                type="time"
                label={t('pages.holidays.workingHours.start')}
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
                label={t('pages.holidays.workingHours.end')}
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
                label={t('common.labels.description')}
                value={workingHoursFormData.description || ''}
                onChange={(e) =>
                  setWorkingHoursFormData({ ...workingHoursFormData, description: e.target.value })
                }
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setWorkingHoursDialogOpen(false)}>{t('common.buttons.cancel')}</Button>
          <Button variant="contained" onClick={handleSaveWorkingHours}>
            {t('common.buttons.save')}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Working Hours Delete Confirmation */}
      <Dialog
        open={deleteWorkingHoursDialogOpen}
        onClose={() => setDeleteWorkingHoursDialogOpen(false)}
      >
        <DialogTitle>{t('pages.holidays.workingHours.delete')}</DialogTitle>
        <DialogContent>
          <Typography>
            {t('pages.holidays.confirmDeleteWorkingHours', { name: selectedWorkingHours?.scheduleName })}
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteWorkingHoursDialogOpen(false)}>{t('common.buttons.cancel')}</Button>
          <Button variant="contained" color="error" onClick={handleConfirmDeleteWorkingHours}>
            {t('common.buttons.delete')}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
