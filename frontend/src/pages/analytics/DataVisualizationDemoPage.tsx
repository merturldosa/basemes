/**
 * Data Visualization Demo Page
 * Showcase of all analytics components with examples
 * @author Moon Myung-seop
 */

import { Box, Card, CardContent, Typography, Grid, Divider } from '@mui/material';
import {
  LineChart,
  BarChart,
  PieChart,
  GaugeChart,
  TrendIndicator,
  HeatMap,
  LineChartSeries,
  BarChartDataPoint,
  PieChartDataPoint,
  HeatMapCell,
} from '@/components/analytics';

const DataVisualizationDemoPage: React.FC = () => {
  // Sample data for Line Chart
  const lineChartData: LineChartSeries[] = [
    {
      name: 'Actual Production',
      data: [
        { label: 'Mon', value: 2450 },
        { label: 'Tue', value: 2380 },
        { label: 'Wed', value: 2520 },
        { label: 'Thu', value: 2400 },
        { label: 'Fri', value: 2700 },
        { label: 'Sat', value: 2600 },
        { label: 'Sun', value: 2300 },
      ],
      color: '#1976d2',
    },
    {
      name: 'Target Production',
      data: [
        { label: 'Mon', value: 2500 },
        { label: 'Tue', value: 2500 },
        { label: 'Wed', value: 2500 },
        { label: 'Thu', value: 2500 },
        { label: 'Fri', value: 2500 },
        { label: 'Sat', value: 2500 },
        { label: 'Sun', value: 2500 },
      ],
      color: '#ff9800',
    },
  ];

  // Sample data for Bar Chart
  const barChartData: BarChartDataPoint[] = [
    { label: 'Product A', value: 3500, color: '#2196f3' },
    { label: 'Product B', value: 2800, color: '#4caf50' },
    { label: 'Product C', value: 2400, color: '#ff9800' },
    { label: 'Product D', value: 1950, color: '#f44336' },
    { label: 'Product E', value: 1800, color: '#9c27b0' },
  ];

  // Sample data for Pie Chart
  const pieChartData: PieChartDataPoint[] = [
    { label: 'Size Defect', value: 45, color: '#f44336' },
    { label: 'Appearance Defect', value: 38, color: '#ff9800' },
    { label: 'Function Defect', value: 25, color: '#2196f3' },
    { label: 'Packaging Defect', value: 19, color: '#4caf50' },
  ];

  // Sample data for Heat Map
  const heatMapData: HeatMapCell[] = [];
  const hours = ['08:00', '10:00', '12:00', '14:00', '16:00', '18:00'];
  const days = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri'];

  days.forEach((day, row) => {
    hours.forEach((hour, col) => {
      heatMapData.push({
        row,
        col,
        value: Math.floor(Math.random() * 100) + 50,
        label: `${day} ${hour}`,
      });
    });
  });

  // Sample trend data
  const trendData = [2100, 2300, 2250, 2400, 2350, 2500, 2700];

  return (
    <Box sx={{ p: 3 }}>
      {/* Header */}
      <Box sx={{ mb: 3 }}>
        <Typography variant="h4" fontWeight="bold" gutterBottom>
          Data Visualization Components Demo
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Showcase of reusable analytics components with sample data
        </Typography>
      </Box>

      {/* Line Chart Section */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Typography variant="h6" gutterBottom fontWeight="bold">
            Line Chart - Production Trend
          </Typography>
          <Typography variant="body2" color="text.secondary" gutterBottom>
            Multi-series line chart with smooth curves and area fill
          </Typography>
          <Divider sx={{ my: 2 }} />
          <LineChart series={lineChartData} height={300} curve={true} />
        </CardContent>
      </Card>

      {/* Bar Charts Section */}
      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom fontWeight="bold">
                Vertical Bar Chart
              </Typography>
              <Typography variant="body2" color="text.secondary" gutterBottom>
                Top products by production quantity
              </Typography>
              <Divider sx={{ my: 2 }} />
              <BarChart data={barChartData} height={300} barWidth={50} />
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom fontWeight="bold">
                Horizontal Bar Chart
              </Typography>
              <Typography variant="body2" color="text.secondary" gutterBottom>
                Same data in horizontal orientation
              </Typography>
              <Divider sx={{ my: 2 }} />
              <BarChart data={barChartData} height={300} horizontal={true} />
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Pie Charts Section */}
      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom fontWeight="bold">
                Pie Chart - Defect Distribution
              </Typography>
              <Typography variant="body2" color="text.secondary" gutterBottom>
                Distribution of defect types
              </Typography>
              <Divider sx={{ my: 2 }} />
              <Box sx={{ display: 'flex', justifyContent: 'center' }}>
                <PieChart data={pieChartData} size={300} donut={false} />
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom fontWeight="bold">
                Donut Chart - Same Data
              </Typography>
              <Typography variant="body2" color="text.secondary" gutterBottom>
                Donut variation with center total
              </Typography>
              <Divider sx={{ my: 2 }} />
              <Box sx={{ display: 'flex', justifyContent: 'center' }}>
                <PieChart data={pieChartData} size={300} donut={true} donutWidth={60} />
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Gauge Charts Section */}
      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography variant="subtitle1" gutterBottom fontWeight="bold" align="center">
                OEE (Overall Equipment Effectiveness)
              </Typography>
              <Divider sx={{ my: 2 }} />
              <Box sx={{ display: 'flex', justifyContent: 'center' }}>
                <GaugeChart value={84.2} min={0} max={100} label="OEE" unit="%" />
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography variant="subtitle1" gutterBottom fontWeight="bold" align="center">
                Quality Rate
              </Typography>
              <Divider sx={{ my: 2 }} />
              <Box sx={{ display: 'flex', justifyContent: 'center' }}>
                <GaugeChart value={98.5} min={0} max={100} label="Quality" unit="%" />
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography variant="subtitle1" gutterBottom fontWeight="bold" align="center">
                Utilization Rate
              </Typography>
              <Divider sx={{ my: 2 }} />
              <Box sx={{ display: 'flex', justifyContent: 'center' }}>
                <GaugeChart value={75.3} min={0} max={100} label="Utilization" unit="%" />
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography variant="subtitle1" gutterBottom fontWeight="bold" align="center">
                Performance
              </Typography>
              <Divider sx={{ my: 2 }} />
              <Box sx={{ display: 'flex', justifyContent: 'center' }}>
                <GaugeChart value={92.7} min={0} max={100} label="Performance" unit="%" />
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Trend Indicators Section */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Typography variant="h6" gutterBottom fontWeight="bold">
            Trend Indicators with Sparklines
          </Typography>
          <Typography variant="body2" color="text.secondary" gutterBottom>
            Compact trend visualization with change indicators
          </Typography>
          <Divider sx={{ my: 2 }} />
          <Grid container spacing={2}>
            <Grid item xs={12} sm={6} md={3}>
              <Card variant="outlined">
                <TrendIndicator
                  value={2700}
                  previousValue={2500}
                  data={trendData}
                  label="Daily Production"
                  unit="EA"
                  size="medium"
                  showSparkline={true}
                />
              </Card>
            </Grid>

            <Grid item xs={12} sm={6} md={3}>
              <Card variant="outlined">
                <TrendIndicator
                  value={98.5}
                  previousValue={99.0}
                  data={[97.5, 98.2, 98.8, 99.0, 98.7, 98.3, 98.5]}
                  label="Quality Rate"
                  unit="%"
                  size="medium"
                  format="percentage"
                  showSparkline={true}
                />
              </Card>
            </Grid>

            <Grid item xs={12} sm={6} md={3}>
              <Card variant="outlined">
                <TrendIndicator
                  value={15280}
                  previousValue={14500}
                  data={[13000, 13500, 14000, 14200, 14500, 14800, 15280]}
                  label="Revenue"
                  unit="$"
                  size="medium"
                  format="currency"
                  showSparkline={true}
                />
              </Card>
            </Grid>

            <Grid item xs={12} sm={6} md={3}>
              <Card variant="outlined">
                <TrendIndicator
                  value={12.5}
                  previousValue={11.8}
                  data={[10.5, 11.0, 11.2, 11.5, 11.8, 12.0, 12.5]}
                  label="Inventory Turnover"
                  size="medium"
                  showSparkline={true}
                />
              </Card>
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      {/* Heat Map Section */}
      <Card>
        <CardContent>
          <Typography variant="h6" gutterBottom fontWeight="bold">
            Heat Map - Hourly Production Pattern
          </Typography>
          <Typography variant="body2" color="text.secondary" gutterBottom>
            Production quantity by day and time (darker = higher production)
          </Typography>
          <Divider sx={{ my: 2 }} />
          <Box sx={{ display: 'flex', justifyContent: 'center', mt: 2 }}>
            <HeatMap
              data={heatMapData}
              rowLabels={days}
              colLabels={hours}
              cellSize={60}
              showValues={true}
              colorScheme="blue"
            />
          </Box>
        </CardContent>
      </Card>

      {/* Usage Examples */}
      <Card sx={{ mt: 3, bgcolor: 'grey.50' }}>
        <CardContent>
          <Typography variant="h6" gutterBottom fontWeight="bold">
            Component Usage
          </Typography>
          <Typography variant="body2" color="text.secondary" paragraph>
            All components are available in <code>@/components/analytics</code>
          </Typography>
          <Divider sx={{ my: 2 }} />
          <Box sx={{ '& pre': { bgcolor: 'grey.900', color: 'white', p: 2, borderRadius: 1, overflow: 'auto' } }}>
            <pre>
{`import {
  LineChart,
  BarChart,
  PieChart,
  GaugeChart,
  TrendIndicator,
  HeatMap
} from '@/components/analytics';

// Line Chart
<LineChart
  series={[{ name: 'Series 1', data: [...] }]}
  height={300}
/>

// Bar Chart
<BarChart
  data={[{ label: 'A', value: 100 }]}
  horizontal={false}
/>

// Pie/Donut Chart
<PieChart
  data={[{ label: 'A', value: 100 }]}
  donut={true}
/>

// Gauge Chart
<GaugeChart
  value={84.2}
  min={0}
  max={100}
/>

// Trend Indicator
<TrendIndicator
  value={2700}
  previousValue={2500}
  data={[...]}
/>

// Heat Map
<HeatMap
  data={[{ row: 0, col: 0, value: 100 }]}
  rowLabels={['Mon', 'Tue']}
  colLabels={['8AM', '10AM']}
/>`}
            </pre>
          </Box>
        </CardContent>
      </Card>
    </Box>
  );
};

export default DataVisualizationDemoPage;
