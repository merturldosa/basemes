/**
 * Heat Map Component
 * Reusable heat map for pattern visualization
 * @author Moon Myung-seop
 */

import { useMemo } from 'react';
import { Box, useTheme, Typography } from '@mui/material';

export interface HeatMapCell {
  row: number;
  col: number;
  value: number;
  label?: string;
}

interface HeatMapProps {
  data: HeatMapCell[];
  rowLabels: string[];
  colLabels: string[];
  cellSize?: number;
  showValues?: boolean;
  colorScheme?: 'blue' | 'green' | 'red' | 'yellow' | 'purple';
  minValue?: number;
  maxValue?: number;
}

const HeatMap: React.FC<HeatMapProps> = ({
  data,
  rowLabels,
  colLabels,
  cellSize = 50,
  showValues = true,
  colorScheme = 'blue',
  minValue,
  maxValue,
}) => {
  const theme = useTheme();

  const colorSchemes = {
    blue: ['#e3f2fd', '#90caf9', '#42a5f5', '#1e88e5', '#1565c0'],
    green: ['#e8f5e9', '#81c784', '#4caf50', '#388e3c', '#2e7d32'],
    red: ['#ffebee', '#e57373', '#f44336', '#d32f2f', '#c62828'],
    yellow: ['#fff9c4', '#fff176', '#ffeb3b', '#fbc02d', '#f57f17'],
    purple: ['#f3e5f5', '#ba68c8', '#9c27b0', '#7b1fa2', '#6a1b9a'],
  };

  const colors = colorSchemes[colorScheme];

  const { normalizedData, min, max } = useMemo(() => {
    const values = data.map((cell) => cell.value);
    const minVal = minValue !== undefined ? minValue : Math.min(...values);
    const maxVal = maxValue !== undefined ? maxValue : Math.max(...values);

    const normalized = data.map((cell) => ({
      ...cell,
      normalizedValue: maxVal > minVal ? (cell.value - minVal) / (maxVal - minVal) : 0,
    }));

    return { normalizedData: normalized, min: minVal, max: maxVal };
  }, [data, minValue, maxValue]);

  const getColor = (normalizedValue: number): string => {
    const index = Math.min(Math.floor(normalizedValue * colors.length), colors.length - 1);
    return colors[index];
  };

  const labelPadding = 100;
  const width = colLabels.length * cellSize + labelPadding;
  const height = rowLabels.length * cellSize + labelPadding;

  return (
    <Box sx={{ width: '100%', overflow: 'auto' }}>
      <svg width={width} height={height} style={{ fontFamily: theme.typography.fontFamily }}>
        {/* Column labels */}
        {colLabels.map((label, i) => (
          <text
            key={`col-label-${i}`}
            x={labelPadding + i * cellSize + cellSize / 2}
            y={labelPadding - 10}
            textAnchor="middle"
            fill={theme.palette.text.primary}
            fontSize={12}
            transform={`rotate(-45, ${labelPadding + i * cellSize + cellSize / 2}, ${labelPadding - 10})`}
          >
            {label}
          </text>
        ))}

        {/* Row labels */}
        {rowLabels.map((label, i) => (
          <text
            key={`row-label-${i}`}
            x={labelPadding - 10}
            y={labelPadding + i * cellSize + cellSize / 2}
            textAnchor="end"
            alignmentBaseline="middle"
            fill={theme.palette.text.primary}
            fontSize={12}
          >
            {label}
          </text>
        ))}

        {/* Heat map cells */}
        {normalizedData.map((cell, i) => {
          const x = labelPadding + cell.col * cellSize;
          const y = labelPadding + cell.row * cellSize;
          const color = getColor(cell.normalizedValue);

          return (
            <g key={`cell-${i}`}>
              <rect
                x={x}
                y={y}
                width={cellSize}
                height={cellSize}
                fill={color}
                stroke={theme.palette.background.paper}
                strokeWidth={2}
              >
                <title>
                  {cell.label || `${rowLabels[cell.row]} - ${colLabels[cell.col]}`}: {cell.value.toFixed(2)}
                </title>
              </rect>

              {showValues && (
                <text
                  x={x + cellSize / 2}
                  y={y + cellSize / 2}
                  textAnchor="middle"
                  alignmentBaseline="middle"
                  fill={cell.normalizedValue > 0.5 ? 'white' : theme.palette.text.primary}
                  fontSize={11}
                  fontWeight="bold"
                >
                  {cell.value.toFixed(0)}
                </text>
              )}
            </g>
          );
        })}

        {/* Legend */}
        <g transform={`translate(${labelPadding}, ${height - 60})`}>
          <text
            x={0}
            y={0}
            fill={theme.palette.text.secondary}
            fontSize={12}
          >
            {min.toFixed(0)}
          </text>

          {colors.map((color, i) => (
            <rect
              key={`legend-${i}`}
              x={50 + i * 30}
              y={-10}
              width={30}
              height={15}
              fill={color}
            />
          ))}

          <text
            x={50 + colors.length * 30 + 10}
            y={0}
            fill={theme.palette.text.secondary}
            fontSize={12}
          >
            {max.toFixed(0)}
          </text>
        </g>
      </svg>
    </Box>
  );
};

export default HeatMap;
