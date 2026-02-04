/**
 * Trend Indicator Component
 * Compact trend visualization with sparkline
 * @author Moon Myung-seop
 */

import { useMemo } from 'react';
import { Box, Typography, useTheme } from '@mui/material';
import { TrendingUp as TrendingUpIcon, TrendingDown as TrendingDownIcon } from '@mui/icons-material';

interface TrendIndicatorProps {
  value: number;
  previousValue?: number;
  data?: number[];
  label?: string;
  unit?: string;
  showSparkline?: boolean;
  size?: 'small' | 'medium' | 'large';
  format?: 'number' | 'percentage' | 'currency';
}

const TrendIndicator: React.FC<TrendIndicatorProps> = ({
  value,
  previousValue,
  data = [],
  label,
  unit,
  showSparkline = true,
  size = 'medium',
  format = 'number',
}) => {
  const theme = useTheme();

  const { trend, changePercentage, changeValue } = useMemo(() => {
    if (previousValue === undefined) {
      return { trend: 'neutral', changePercentage: 0, changeValue: 0 };
    }

    const change = value - previousValue;
    const percentage = previousValue !== 0 ? (change / previousValue) * 100 : 0;

    return {
      trend: change > 0 ? 'up' : change < 0 ? 'down' : 'neutral',
      changePercentage: percentage,
      changeValue: change,
    };
  }, [value, previousValue]);

  const trendColor = trend === 'up' ? theme.palette.success.main : trend === 'down' ? theme.palette.error.main : theme.palette.text.secondary;

  const sparklinePath = useMemo(() => {
    if (!showSparkline || data.length === 0) return '';

    const width = 80;
    const height = 30;
    const max = Math.max(...data);
    const min = Math.min(...data);
    const range = max - min;
    const scale = range > 0 ? height / range : 0;

    const points = data.map((d, i) => {
      const x = (i * width) / (data.length - 1);
      const y = height - (d - min) * scale;
      return `${x},${y}`;
    });

    return `M ${points.join(' L ')}`;
  }, [data, showSparkline]);

  const formatValue = (val: number): string => {
    switch (format) {
      case 'percentage':
        return `${val.toFixed(1)}%`;
      case 'currency':
        return `$${val.toLocaleString()}`;
      default:
        return val.toLocaleString();
    }
  };

  const fontSize = size === 'small' ? 18 : size === 'medium' ? 24 : 32;
  const iconSize = size === 'small' ? 'small' : size === 'medium' ? 'medium' : 'large';

  return (
    <Box
      sx={{
        display: 'flex',
        flexDirection: 'column',
        gap: 1,
        p: size === 'small' ? 1 : size === 'medium' ? 2 : 3,
      }}
    >
      {label && (
        <Typography
          variant={size === 'small' ? 'caption' : 'body2'}
          color="text.secondary"
          fontWeight="medium"
        >
          {label}
        </Typography>
      )}

      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
        {/* Value */}
        <Box sx={{ display: 'flex', alignItems: 'baseline', gap: 0.5 }}>
          <Typography
            sx={{ fontSize, fontWeight: 'bold', color: theme.palette.text.primary }}
          >
            {formatValue(value)}
          </Typography>
          {unit && (
            <Typography variant="body2" color="text.secondary">
              {unit}
            </Typography>
          )}
        </Box>

        {/* Trend indicator */}
        {previousValue !== undefined && (
          <Box
            sx={{
              display: 'flex',
              alignItems: 'center',
              gap: 0.5,
              color: trendColor,
              bgcolor: `${trendColor}15`,
              px: 1,
              py: 0.5,
              borderRadius: 1,
            }}
          >
            {trend === 'up' ? (
              <TrendingUpIcon fontSize={iconSize} />
            ) : trend === 'down' ? (
              <TrendingDownIcon fontSize={iconSize} />
            ) : null}
            <Typography variant="caption" fontWeight="bold">
              {changePercentage > 0 ? '+' : ''}
              {changePercentage.toFixed(1)}%
            </Typography>
          </Box>
        )}
      </Box>

      {/* Sparkline */}
      {showSparkline && data.length > 0 && (
        <Box sx={{ mt: 1 }}>
          <svg width={80} height={30} style={{ display: 'block' }}>
            <path
              d={sparklinePath}
              fill="none"
              stroke={trendColor}
              strokeWidth={2}
              strokeLinecap="round"
              strokeLinejoin="round"
            />
            {/* Current point */}
            {data.length > 0 && (
              <circle
                cx={80}
                cy={30 - ((value - Math.min(...data)) / (Math.max(...data) - Math.min(...data))) * 30}
                r={3}
                fill={trendColor}
              />
            )}
          </svg>
        </Box>
      )}

      {/* Change value */}
      {previousValue !== undefined && (
        <Typography variant="caption" color="text.secondary">
          {changeValue > 0 ? '+' : ''}
          {formatValue(changeValue)} vs previous
        </Typography>
      )}
    </Box>
  );
};

export default TrendIndicator;
