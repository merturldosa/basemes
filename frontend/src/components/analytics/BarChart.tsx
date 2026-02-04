/**
 * Bar Chart Component
 * Reusable bar chart for comparison visualization
 * @author Moon Myung-seop
 */

import { useMemo } from 'react';
import { Box, useTheme } from '@mui/material';

export interface BarChartDataPoint {
  label: string;
  value: number;
  color?: string;
}

interface BarChartProps {
  data: BarChartDataPoint[];
  height?: number;
  showGrid?: boolean;
  showValues?: boolean;
  showXAxis?: boolean;
  showYAxis?: boolean;
  horizontal?: boolean;
  barWidth?: number;
}

const BarChart: React.FC<BarChartProps> = ({
  data,
  height = 300,
  showGrid = true,
  showValues = true,
  showXAxis = true,
  showYAxis = true,
  horizontal = false,
  barWidth = 40,
}) => {
  const theme = useTheme();
  const padding = horizontal
    ? { top: 20, right: showValues ? 60 : 20, bottom: 20, left: 100 }
    : { top: 20, right: 20, bottom: showXAxis ? 80 : 20, left: showYAxis ? 50 : 20 };

  const chartWidth = horizontal ? 800 : Math.max(600, data.length * (barWidth + 20));
  const chartHeight = horizontal ? Math.max(400, data.length * 50) : height;
  const innerWidth = chartWidth - padding.left - padding.right;
  const innerHeight = chartHeight - padding.top - padding.bottom;

  const { maxValue, minValue, yTicks } = useMemo(() => {
    if (data.length === 0) {
      return { maxValue: 0, minValue: 0, yTicks: [] as number[] };
    }

    const allValues = data.map((d) => d.value);
    const max = Math.max(...allValues);
    const min = Math.min(...allValues, 0);
    const range = max - min;
    const padding_value = range * 0.1;

    const maxVal = max + padding_value;
    const minVal = Math.max(min - padding_value, 0);

    // Generate Y-axis ticks
    const tickCount = 5;
    const ticks = Array.from({ length: tickCount }, (_, i) => {
      return minVal + ((maxVal - minVal) * i) / (tickCount - 1);
    });

    return { maxValue: maxVal, minValue: minVal, yTicks: ticks };
  }, [data]);

  const scale = (maxValue - minValue) > 0 ? (horizontal ? innerWidth : innerHeight) / (maxValue - minValue) : 0;

  const defaultColor = theme.palette.primary.main;

  return (
    <Box sx={{ width: '100%', overflow: 'auto' }}>
      <svg width={chartWidth} height={chartHeight} style={{ fontFamily: theme.typography.fontFamily }}>
        <g transform={`translate(${padding.left}, ${padding.top})`}>
          {/* Grid lines */}
          {showGrid &&
            yTicks.map((tick, i) => {
              if (horizontal) {
                const x = (tick - minValue) * scale;
                return (
                  <line
                    key={`grid-${i}`}
                    x1={x}
                    y1={0}
                    x2={x}
                    y2={innerHeight}
                    stroke={theme.palette.divider}
                    strokeDasharray="3,3"
                    strokeWidth={1}
                  />
                );
              } else {
                const y = innerHeight - (tick - minValue) * scale;
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
              }
            })}

          {/* Axes */}
          {!horizontal && showYAxis && (
            <>
              <line x1={0} y1={0} x2={0} y2={innerHeight} stroke={theme.palette.text.secondary} strokeWidth={2} />
              {yTicks.map((tick, i) => {
                const y = innerHeight - (tick - minValue) * scale;
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

          {!horizontal && showXAxis && (
            <line
              x1={0}
              y1={innerHeight}
              x2={innerWidth}
              y2={innerHeight}
              stroke={theme.palette.text.secondary}
              strokeWidth={2}
            />
          )}

          {horizontal && showYAxis && (
            <>
              <line x1={0} y1={0} x2={0} y2={innerHeight} stroke={theme.palette.text.secondary} strokeWidth={2} />
              <line
                x1={0}
                y1={innerHeight}
                x2={innerWidth}
                y2={innerHeight}
                stroke={theme.palette.text.secondary}
                strokeWidth={2}
              />
              {yTicks.map((tick, i) => {
                const x = (tick - minValue) * scale;
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
                      {tick.toFixed(0)}
                    </text>
                  </g>
                );
              })}
            </>
          )}

          {/* Bars */}
          {data.map((d, i) => {
            const color = d.color || defaultColor;

            if (horizontal) {
              const barHeight = (innerHeight / data.length) * 0.7;
              const y = (i * innerHeight) / data.length + (innerHeight / data.length - barHeight) / 2;
              const barLength = Math.max((d.value - minValue) * scale, 0);

              return (
                <g key={`bar-${i}`}>
                  {/* Bar */}
                  <rect x={0} y={y} width={barLength} height={barHeight} fill={color} rx={4}>
                    <title>
                      {d.label}: {d.value.toFixed(2)}
                    </title>
                  </rect>

                  {/* Label */}
                  <text
                    x={-10}
                    y={y + barHeight / 2}
                    textAnchor="end"
                    alignmentBaseline="middle"
                    fill={theme.palette.text.primary}
                    fontSize={14}
                    fontWeight="500"
                  >
                    {d.label}
                  </text>

                  {/* Value */}
                  {showValues && (
                    <text
                      x={barLength + 10}
                      y={y + barHeight / 2}
                      textAnchor="start"
                      alignmentBaseline="middle"
                      fill={theme.palette.text.primary}
                      fontSize={14}
                      fontWeight="bold"
                    >
                      {d.value.toFixed(0)}
                    </text>
                  )}
                </g>
              );
            } else {
              const x = (i * innerWidth) / data.length + (innerWidth / data.length - barWidth) / 2;
              const barHeight = Math.max((d.value - minValue) * scale, 0);
              const y = innerHeight - barHeight;

              return (
                <g key={`bar-${i}`}>
                  {/* Bar */}
                  <rect x={x} y={y} width={barWidth} height={barHeight} fill={color} rx={4}>
                    <title>
                      {d.label}: {d.value.toFixed(2)}
                    </title>
                  </rect>

                  {/* Label */}
                  {showXAxis && (
                    <text
                      x={x + barWidth / 2}
                      y={innerHeight + 15}
                      textAnchor="middle"
                      fill={theme.palette.text.primary}
                      fontSize={12}
                      transform={`rotate(-45, ${x + barWidth / 2}, ${innerHeight + 15})`}
                    >
                      {d.label}
                    </text>
                  )}

                  {/* Value */}
                  {showValues && (
                    <text
                      x={x + barWidth / 2}
                      y={y - 5}
                      textAnchor="middle"
                      fill={theme.palette.text.primary}
                      fontSize={12}
                      fontWeight="bold"
                    >
                      {d.value.toFixed(0)}
                    </text>
                  )}
                </g>
              );
            }
          })}
        </g>
      </svg>
    </Box>
  );
};

export default BarChart;
