/**
 * Gauge Chart Component
 * Reusable gauge chart for KPI visualization
 * @author Moon Myung-seop
 */

import { useMemo } from 'react';
import { Box, useTheme, Typography } from '@mui/material';

interface GaugeChartProps {
  value: number;
  min?: number;
  max?: number;
  size?: number;
  label?: string;
  unit?: string;
  thresholds?: {
    value: number;
    color: string;
    label?: string;
  }[];
  showNeedle?: boolean;
}

const GaugeChart: React.FC<GaugeChartProps> = ({
  value,
  min = 0,
  max = 100,
  size = 200,
  label,
  unit = '%',
  thresholds = [
    { value: 60, color: '#f44336', label: 'Low' },
    { value: 80, color: '#ff9800', label: 'Medium' },
    { value: 100, color: '#4caf50', label: 'High' },
  ],
  showNeedle = true,
}) => {
  const theme = useTheme();
  const radius = (size / 2) * 0.8;
  const centerX = size / 2;
  const centerY = size / 2 + 20;
  const startAngle = -Math.PI + Math.PI / 6; // -150 degrees
  const endAngle = Math.PI / 6; // 30 degrees
  const totalAngle = endAngle - startAngle;

  const { segments, needleAngle, color } = useMemo(() => {
    // Normalize value
    const normalizedValue = Math.max(min, Math.min(max, value));
    const percentage = (normalizedValue - min) / (max - min);
    const angle = startAngle + totalAngle * percentage;

    // Create segments based on thresholds
    const sortedThresholds = [...thresholds].sort((a, b) => a.value - b.value);
    const segs: Array<{ startAngle: number; endAngle: number; color: string; label?: string }> = [];

    let prevThreshold = min;
    let prevAngle = startAngle;

    sortedThresholds.forEach((threshold) => {
      const thresholdPercentage = (threshold.value - min) / (max - min);
      const thresholdAngle = startAngle + totalAngle * thresholdPercentage;

      segs.push({
        startAngle: prevAngle,
        endAngle: thresholdAngle,
        color: threshold.color,
        label: threshold.label,
      });

      prevThreshold = threshold.value;
      prevAngle = thresholdAngle;
    });

    // Determine current color based on value
    let currentColor = sortedThresholds[0]?.color || theme.palette.primary.main;
    for (let i = sortedThresholds.length - 1; i >= 0; i--) {
      if (normalizedValue >= sortedThresholds[i].value) {
        currentColor = sortedThresholds[i].color;
        break;
      }
    }

    return { segments: segs, needleAngle: angle, color: currentColor };
  }, [value, min, max, startAngle, totalAngle, thresholds, theme]);

  const createArcPath = (
    cx: number,
    cy: number,
    radius: number,
    startAngle: number,
    endAngle: number,
    thickness: number
  ): string => {
    const outerStartX = cx + radius * Math.cos(startAngle);
    const outerStartY = cy + radius * Math.sin(startAngle);
    const outerEndX = cx + radius * Math.cos(endAngle);
    const outerEndY = cy + radius * Math.sin(endAngle);

    const innerRadius = radius - thickness;
    const innerStartX = cx + innerRadius * Math.cos(endAngle);
    const innerStartY = cy + innerRadius * Math.sin(endAngle);
    const innerEndX = cx + innerRadius * Math.cos(startAngle);
    const innerEndY = cy + innerRadius * Math.sin(startAngle);

    const largeArc = endAngle - startAngle > Math.PI ? 1 : 0;

    return `
      M ${outerStartX} ${outerStartY}
      A ${radius} ${radius} 0 ${largeArc} 1 ${outerEndX} ${outerEndY}
      L ${innerStartX} ${innerStartY}
      A ${innerRadius} ${innerRadius} 0 ${largeArc} 0 ${innerEndX} ${innerEndY}
      Z
    `;
  };

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 1 }}>
      <svg width={size} height={size * 0.7} style={{ fontFamily: theme.typography.fontFamily }}>
        {/* Background arc */}
        <path
          d={createArcPath(centerX, centerY, radius, startAngle, endAngle, 20)}
          fill={theme.palette.grey[200]}
        />

        {/* Colored segments */}
        {segments.map((segment, i) => (
          <path
            key={`segment-${i}`}
            d={createArcPath(centerX, centerY, radius, segment.startAngle, segment.endAngle, 20)}
            fill={segment.color}
          />
        ))}

        {/* Needle */}
        {showNeedle && (
          <g>
            {/* Needle line */}
            <line
              x1={centerX}
              y1={centerY}
              x2={centerX + (radius - 30) * Math.cos(needleAngle)}
              y2={centerY + (radius - 30) * Math.sin(needleAngle)}
              stroke={theme.palette.text.primary}
              strokeWidth={3}
              strokeLinecap="round"
            />

            {/* Needle center circle */}
            <circle cx={centerX} cy={centerY} r={8} fill={theme.palette.text.primary} />
            <circle cx={centerX} cy={centerY} r={4} fill="white" />
          </g>
        )}

        {/* Min/Max labels */}
        <text
          x={centerX + radius * Math.cos(startAngle) * 0.9}
          y={centerY + radius * Math.sin(startAngle) * 0.9 + 5}
          textAnchor="end"
          fill={theme.palette.text.secondary}
          fontSize={12}
        >
          {min}
        </text>
        <text
          x={centerX + radius * Math.cos(endAngle) * 0.9}
          y={centerY + radius * Math.sin(endAngle) * 0.9 + 5}
          textAnchor="start"
          fill={theme.palette.text.secondary}
          fontSize={12}
        >
          {max}
        </text>

        {/* Current value */}
        <text
          x={centerX}
          y={centerY + 20}
          textAnchor="middle"
          fill={color}
          fontSize={32}
          fontWeight="bold"
        >
          {value.toFixed(1)}
        </text>
        {unit && (
          <text
            x={centerX}
            y={centerY + 40}
            textAnchor="middle"
            fill={theme.palette.text.secondary}
            fontSize={14}
          >
            {unit}
          </text>
        )}
      </svg>

      {label && (
        <Typography variant="body2" color="text.secondary" fontWeight="medium">
          {label}
        </Typography>
      )}

      {/* Threshold legend */}
      <Box sx={{ display: 'flex', gap: 2, mt: 1 }}>
        {thresholds.map((threshold, i) => (
          <Box key={`threshold-${i}`} sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
            <Box
              sx={{
                width: 12,
                height: 12,
                bgcolor: threshold.color,
                borderRadius: '50%',
              }}
            />
            <Typography variant="caption" color="text.secondary">
              {threshold.label || `>${threshold.value}`}
            </Typography>
          </Box>
        ))}
      </Box>
    </Box>
  );
};

export default GaugeChart;
