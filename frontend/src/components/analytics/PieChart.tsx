/**
 * Pie Chart Component
 * Reusable pie/donut chart for distribution visualization
 * @author Moon Myung-seop
 */

import { useMemo } from 'react';
import { Box, useTheme } from '@mui/material';

export interface PieChartDataPoint {
  label: string;
  value: number;
  color?: string;
}

interface PieChartProps {
  data: PieChartDataPoint[];
  size?: number;
  donut?: boolean;
  donutWidth?: number;
  showLegend?: boolean;
  showLabels?: boolean;
  showPercentages?: boolean;
}

const PieChart: React.FC<PieChartProps> = ({
  data,
  size = 300,
  donut = false,
  donutWidth = 60,
  showLegend = true,
  showLabels = true,
  showPercentages = true,
}) => {
  const theme = useTheme();
  const radius = size / 2 - 20;
  const centerX = size / 2;
  const centerY = size / 2;

  const defaultColors = [
    theme.palette.primary.main,
    theme.palette.secondary.main,
    theme.palette.success.main,
    theme.palette.warning.main,
    theme.palette.error.main,
    theme.palette.info.main,
    '#9c27b0',
    '#ff9800',
  ];

  const { slices, total } = useMemo(() => {
    const totalValue = data.reduce((sum, d) => sum + d.value, 0);
    let currentAngle = -Math.PI / 2; // Start at top

    const sliceData = data.map((d, i) => {
      const percentage = (d.value / totalValue) * 100;
      const angle = (d.value / totalValue) * 2 * Math.PI;
      const startAngle = currentAngle;
      const endAngle = currentAngle + angle;
      currentAngle = endAngle;

      // Calculate path for slice
      const startX = centerX + radius * Math.cos(startAngle);
      const startY = centerY + radius * Math.sin(startAngle);
      const endX = centerX + radius * Math.cos(endAngle);
      const endY = centerY + radius * Math.sin(endAngle);

      const largeArc = angle > Math.PI ? 1 : 0;

      let path: string;
      if (donut) {
        const innerRadius = radius - donutWidth;
        const innerStartX = centerX + innerRadius * Math.cos(startAngle);
        const innerStartY = centerY + innerRadius * Math.sin(startAngle);
        const innerEndX = centerX + innerRadius * Math.cos(endAngle);
        const innerEndY = centerY + innerRadius * Math.sin(endAngle);

        path = `
          M ${startX} ${startY}
          A ${radius} ${radius} 0 ${largeArc} 1 ${endX} ${endY}
          L ${innerEndX} ${innerEndY}
          A ${innerRadius} ${innerRadius} 0 ${largeArc} 0 ${innerStartX} ${innerStartY}
          Z
        `;
      } else {
        path = `
          M ${centerX} ${centerY}
          L ${startX} ${startY}
          A ${radius} ${radius} 0 ${largeArc} 1 ${endX} ${endY}
          Z
        `;
      }

      // Calculate label position
      const labelAngle = (startAngle + endAngle) / 2;
      const labelRadius = donut ? radius - donutWidth / 2 : (radius * 2) / 3;
      const labelX = centerX + labelRadius * Math.cos(labelAngle);
      const labelY = centerY + labelRadius * Math.sin(labelAngle);

      return {
        ...d,
        path,
        percentage,
        labelX,
        labelY,
        color: d.color || defaultColors[i % defaultColors.length],
      };
    });

    return { slices: sliceData, total: totalValue };
  }, [data, radius, centerX, centerY, donut, donutWidth, defaultColors]);

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2 }}>
      <svg width={size} height={size} style={{ fontFamily: theme.typography.fontFamily }}>
        {/* Slices */}
        {slices.map((slice, i) => (
          <g key={`slice-${i}`}>
            <path d={slice.path} fill={slice.color} stroke="white" strokeWidth={2}>
              <title>
                {slice.label}: {slice.value.toFixed(2)} ({slice.percentage.toFixed(1)}%)
              </title>
            </path>

            {/* Labels */}
            {showLabels && slice.percentage > 5 && (
              <text
                x={slice.labelX}
                y={slice.labelY}
                textAnchor="middle"
                alignmentBaseline="middle"
                fill="white"
                fontSize={12}
                fontWeight="bold"
                style={{ pointerEvents: 'none' }}
              >
                {showPercentages ? `${slice.percentage.toFixed(1)}%` : slice.value.toFixed(0)}
              </text>
            )}
          </g>
        ))}

        {/* Center text for donut chart */}
        {donut && (
          <g>
            <text
              x={centerX}
              y={centerY - 5}
              textAnchor="middle"
              alignmentBaseline="middle"
              fill={theme.palette.text.primary}
              fontSize={24}
              fontWeight="bold"
            >
              {total.toFixed(0)}
            </text>
            <text
              x={centerX}
              y={centerY + 15}
              textAnchor="middle"
              alignmentBaseline="middle"
              fill={theme.palette.text.secondary}
              fontSize={12}
            >
              Total
            </text>
          </g>
        )}
      </svg>

      {/* Legend */}
      {showLegend && (
        <Box
          sx={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))',
            gap: 1,
            width: '100%',
            maxWidth: size,
          }}
        >
          {slices.map((slice, i) => (
            <Box key={`legend-${i}`} sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <Box
                sx={{
                  width: 16,
                  height: 16,
                  bgcolor: slice.color,
                  borderRadius: 1,
                  flexShrink: 0,
                }}
              />
              <Box sx={{ overflow: 'hidden' }}>
                <Box
                  sx={{
                    fontSize: 12,
                    fontWeight: 500,
                    color: theme.palette.text.primary,
                    whiteSpace: 'nowrap',
                    overflow: 'hidden',
                    textOverflow: 'ellipsis',
                  }}
                >
                  {slice.label}
                </Box>
                <Box sx={{ fontSize: 11, color: theme.palette.text.secondary }}>
                  {slice.value.toFixed(0)} ({slice.percentage.toFixed(1)}%)
                </Box>
              </Box>
            </Box>
          ))}
        </Box>
      )}
    </Box>
  );
};

export default PieChart;
