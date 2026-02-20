/**
 * Line Chart Component
 * Reusable line chart for trend visualization
 * @author Moon Myung-seop
 */

import { useMemo } from 'react';
import { Box, useTheme } from '@mui/material';

export interface LineChartDataPoint {
  label: string;
  value: number;
}

export interface LineChartSeries {
  name: string;
  data: LineChartDataPoint[];
  color?: string;
}

interface LineChartProps {
  series: LineChartSeries[];
  height?: number;
  showGrid?: boolean;
  showLegend?: boolean;
  showXAxis?: boolean;
  showYAxis?: boolean;
  curve?: boolean; // Smooth curves vs straight lines
}

const LineChart: React.FC<LineChartProps> = ({
  series,
  height = 300,
  showGrid = true,
  showLegend = true,
  showXAxis = true,
  showYAxis = true,
  curve = true,
}) => {
  const theme = useTheme();
  const padding = { top: 20, right: 20, bottom: showXAxis ? 40 : 20, left: showYAxis ? 50 : 20 };
  const chartWidth = 800;
  const chartHeight = height;
  const innerWidth = chartWidth - padding.left - padding.right;
  const innerHeight = chartHeight - padding.top - padding.bottom;

  // Calculate scales
  const { xScale, yScale, yTicks, minValue } = useMemo(() => {
    if (series.length === 0 || series[0].data.length === 0) {
      return { xScale: [] as number[], yScale: 0, yTicks: [] as number[], maxValue: 0, minValue: 0 };
    }

    const allValues = series.flatMap((s) => s.data.map((d) => d.value));
    const max = Math.max(...allValues);
    const min = Math.min(...allValues, 0);
    const range = max - min;
    const padding_value = range * 0.1;

    const maxVal = max + padding_value;
    const minVal = Math.max(min - padding_value, 0);

    const xScale = series[0].data.map((_, i) => (i * innerWidth) / (series[0].data.length - 1));
    const yScale = innerHeight / (maxVal - minVal);

    // Generate Y-axis ticks
    const tickCount = 5;
    const ticks = Array.from({ length: tickCount }, (_, i) => {
      return minVal + ((maxVal - minVal) * i) / (tickCount - 1);
    });

    return { xScale, yScale, yTicks: ticks, maxValue: maxVal, minValue: minVal };
  }, [series, innerWidth, innerHeight]);

  // Generate path for line series
  const generatePath = (data: LineChartDataPoint[]): string => {
    if (data.length === 0) return '';

    const points = data.map((d, i) => {
      const x = xScale[i];
      const y = innerHeight - (d.value - minValue) * yScale;
      return { x, y };
    });

    if (!curve) {
      // Straight lines
      return points.map((p, i) => `${i === 0 ? 'M' : 'L'} ${p.x} ${p.y}`).join(' ');
    } else {
      // Smooth curves using cubic bezier
      if (points.length === 1) {
        return `M ${points[0].x} ${points[0].y}`;
      }

      let path = `M ${points[0].x} ${points[0].y}`;

      for (let i = 0; i < points.length - 1; i++) {
        const p0 = points[i];
        const p1 = points[i + 1];
        const cp1x = p0.x + (p1.x - p0.x) / 3;
        const cp1y = p0.y;
        const cp2x = p0.x + (2 * (p1.x - p0.x)) / 3;
        const cp2y = p1.y;

        path += ` C ${cp1x} ${cp1y}, ${cp2x} ${cp2y}, ${p1.x} ${p1.y}`;
      }

      return path;
    }
  };

  const defaultColors = [
    theme.palette.primary.main,
    theme.palette.secondary.main,
    theme.palette.success.main,
    theme.palette.warning.main,
    theme.palette.error.main,
    theme.palette.info.main,
  ];

  return (
    <Box sx={{ width: '100%', overflow: 'auto' }}>
      <svg width={chartWidth} height={chartHeight + (showLegend ? 40 : 0)} style={{ fontFamily: theme.typography.fontFamily }}>
        <defs>
          {series.map((s, idx) => {
            const color = s.color || defaultColors[idx % defaultColors.length];
            return (
              <linearGradient key={`gradient-${idx}`} id={`gradient-${idx}`} x1="0%" y1="0%" x2="0%" y2="100%">
                <stop offset="0%" stopColor={color} stopOpacity={0.3} />
                <stop offset="100%" stopColor={color} stopOpacity={0.05} />
              </linearGradient>
            );
          })}
        </defs>

        <g transform={`translate(${padding.left}, ${padding.top})`}>
          {/* Grid lines */}
          {showGrid &&
            yTicks.map((tick, i) => {
              const y = innerHeight - (tick - minValue) * yScale;
              return (
                <line
                  key={`grid-${i}`}
                  x1={0}
                  y1={y}
                  x2={innerWidth}
                  y2={y}
                  stroke={theme.palette.divider}
                  strokeDasharray="3,3"
                  strokeWidth={1}
                />
              );
            })}

          {/* Y-axis */}
          {showYAxis && (
            <>
              <line x1={0} y1={0} x2={0} y2={innerHeight} stroke={theme.palette.text.secondary} strokeWidth={2} />
              {yTicks.map((tick, i) => {
                const y = innerHeight - (tick - minValue) * yScale;
                return (
                  <g key={`y-tick-${i}`}>
                    <line x1={-5} y1={y} x2={0} y2={y} stroke={theme.palette.text.secondary} strokeWidth={2} />
                    <text
                      x={-10}
                      y={y}
                      textAnchor="end"
                      alignmentBaseline="middle"
                      fill={theme.palette.text.secondary}
                      fontSize={12}
                    >
                      {tick.toFixed(0)}
                    </text>
                  </g>
                );
              })}
            </>
          )}

          {/* X-axis */}
          {showXAxis && (
            <>
              <line
                x1={0}
                y1={innerHeight}
                x2={innerWidth}
                y2={innerHeight}
                stroke={theme.palette.text.secondary}
                strokeWidth={2}
              />
              {series[0]?.data.map((d, i) => {
                const x = xScale[i];
                return (
                  <g key={`x-tick-${i}`}>
                    <line
                      x1={x}
                      y1={innerHeight}
                      x2={x}
                      y2={innerHeight + 5}
                      stroke={theme.palette.text.secondary}
                      strokeWidth={2}
                    />
                    <text
                      x={x}
                      y={innerHeight + 20}
                      textAnchor="middle"
                      fill={theme.palette.text.secondary}
                      fontSize={12}
                    >
                      {d.label}
                    </text>
                  </g>
                );
              })}
            </>
          )}

          {/* Line series */}
          {series.map((s, idx) => {
            const color = s.color || defaultColors[idx % defaultColors.length];
            const path = generatePath(s.data);

            // Generate area path for fill
            const areaPath = path + ` L ${xScale[xScale.length - 1]} ${innerHeight} L ${xScale[0]} ${innerHeight} Z`;

            return (
              <g key={`series-${idx}`}>
                {/* Area fill */}
                <path d={areaPath} fill={`url(#gradient-${idx})`} />

                {/* Line */}
                <path d={path} stroke={color} strokeWidth={2} fill="none" />

                {/* Data points */}
                {s.data.map((d, i) => {
                  const x = xScale[i];
                  const y = innerHeight - (d.value - minValue) * yScale;
                  return (
                    <g key={`point-${idx}-${i}`}>
                      <circle cx={x} cy={y} r={4} fill={color} stroke="white" strokeWidth={2} />
                      <title>
                        {s.name}: {d.value.toFixed(2)}
                      </title>
                    </g>
                  );
                })}
              </g>
            );
          })}
        </g>

        {/* Legend */}
        {showLegend && (
          <g transform={`translate(${chartWidth / 2}, ${chartHeight + 10})`}>
            {series.map((s, idx) => {
              const color = s.color || defaultColors[idx % defaultColors.length];
              const xOffset = (idx - series.length / 2) * 120;
              return (
                <g key={`legend-${idx}`} transform={`translate(${xOffset}, 0)`}>
                  <line x1={0} y1={10} x2={20} y2={10} stroke={color} strokeWidth={2} />
                  <text x={25} y={10} alignmentBaseline="middle" fill={theme.palette.text.primary} fontSize={12}>
                    {s.name}
                  </text>
                </g>
              );
            })}
          </g>
        )}
      </svg>
    </Box>
  );
};

export default LineChart;
