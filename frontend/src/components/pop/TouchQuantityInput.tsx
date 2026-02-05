import React, { useState, useEffect } from 'react';
import { Box, IconButton, TextField, Paper } from '@mui/material';
import { Add as AddIcon, Remove as RemoveIcon } from '@mui/icons-material';

/**
 * Touch Quantity Input Component
 * Touch-optimized quantity input with large +/- buttons
 * @author Moon Myung-seop
 */

interface TouchQuantityInputProps {
  value: number;
  onChange: (value: number) => void;
  min?: number;
  max?: number;
  step?: number;
  label?: string;
  disabled?: boolean;
  size?: 'small' | 'medium' | 'large';
}

const TouchQuantityInput: React.FC<TouchQuantityInputProps> = ({
  value,
  onChange,
  min = 0,
  max = 999999,
  step = 1,
  label,
  disabled = false,
  size = 'large',
}) => {
  const [localValue, setLocalValue] = useState(value.toString());

  useEffect(() => {
    setLocalValue(value.toString());
  }, [value]);

  const handleIncrement = () => {
    if (disabled) return;
    const newValue = Math.min(value + step, max);
    onChange(newValue);

    // Haptic feedback if supported
    if (navigator.vibrate) {
      navigator.vibrate(10);
    }
  };

  const handleDecrement = () => {
    if (disabled) return;
    const newValue = Math.max(value - step, min);
    onChange(newValue);

    // Haptic feedback if supported
    if (navigator.vibrate) {
      navigator.vibrate(10);
    }
  };

  const handleInputChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const inputValue = event.target.value;

    // Allow empty string for user to clear and type
    if (inputValue === '') {
      setLocalValue('');
      return;
    }

    // Only allow numbers
    if (!/^\d+$/.test(inputValue)) {
      return;
    }

    setLocalValue(inputValue);
  };

  const handleInputBlur = () => {
    // Parse and validate on blur
    const numValue = parseInt(localValue) || min;
    const validValue = Math.max(min, Math.min(numValue, max));
    onChange(validValue);
    setLocalValue(validValue.toString());
  };

  const handleKeyPress = (event: React.KeyboardEvent<HTMLInputElement>) => {
    if (event.key === 'Enter') {
      handleInputBlur();
    }
  };

  // Button sizes based on component size
  const buttonSize = size === 'large' ? 80 : size === 'medium' ? 60 : 44;
  const fontSize = size === 'large' ? 32 : size === 'medium' ? 24 : 18;
  const inputFontSize = size === 'large' ? '2rem' : size === 'medium' ? '1.5rem' : '1rem';

  return (
    <Paper
      elevation={2}
      sx={{
        p: 2,
        display: 'inline-flex',
        flexDirection: 'column',
        alignItems: 'center',
        gap: 2,
      }}
    >
      {label && (
        <Box sx={{ fontSize: '1rem', fontWeight: 600, color: 'text.secondary' }}>
          {label}
        </Box>
      )}

      <Box
        sx={{
          display: 'flex',
          alignItems: 'center',
          gap: 2,
        }}
      >
        {/* Decrement Button */}
        <IconButton
          onClick={handleDecrement}
          disabled={disabled || value <= min}
          sx={{
            width: buttonSize,
            height: buttonSize,
            bgcolor: 'error.main',
            color: 'white',
            fontSize,
            '&:hover': {
              bgcolor: 'error.dark',
            },
            '&.Mui-disabled': {
              bgcolor: 'action.disabledBackground',
              color: 'action.disabled',
            },
            '&:active': {
              transform: 'scale(0.95)',
            },
            transition: 'all 0.1s ease-in-out',
          }}
        >
          <RemoveIcon fontSize="inherit" />
        </IconButton>

        {/* Quantity Input */}
        <TextField
          value={localValue}
          onChange={handleInputChange}
          onBlur={handleInputBlur}
          onKeyPress={handleKeyPress}
          disabled={disabled}
          variant="outlined"
          type="text"
          inputMode="numeric"
          sx={{
            width: size === 'large' ? 160 : size === 'medium' ? 120 : 80,
            '& input': {
              textAlign: 'center',
              fontSize: inputFontSize,
              fontWeight: 700,
              padding: size === 'large' ? '16px' : size === 'medium' ? '12px' : '8px',
            },
          }}
        />

        {/* Increment Button */}
        <IconButton
          onClick={handleIncrement}
          disabled={disabled || value >= max}
          sx={{
            width: buttonSize,
            height: buttonSize,
            bgcolor: 'success.main',
            color: 'white',
            fontSize,
            '&:hover': {
              bgcolor: 'success.dark',
            },
            '&.Mui-disabled': {
              bgcolor: 'action.disabledBackground',
              color: 'action.disabled',
            },
            '&:active': {
              transform: 'scale(0.95)',
            },
            transition: 'all 0.1s ease-in-out',
          }}
        >
          <AddIcon fontSize="inherit" />
        </IconButton>
      </Box>

      {/* Quick increment buttons for large quantities */}
      {size === 'large' && step === 1 && (
        <Box sx={{ display: 'flex', gap: 1 }}>
          {[10, 50, 100].map((quickStep) => (
            <IconButton
              key={quickStep}
              onClick={() => {
                if (disabled) return;
                const newValue = Math.min(value + quickStep, max);
                onChange(newValue);
                if (navigator.vibrate) {
                  navigator.vibrate(10);
                }
              }}
              disabled={disabled || value >= max}
              size="small"
              sx={{
                minWidth: 60,
                bgcolor: 'primary.main',
                color: 'white',
                fontSize: '0.875rem',
                '&:hover': {
                  bgcolor: 'primary.dark',
                },
                '&.Mui-disabled': {
                  bgcolor: 'action.disabledBackground',
                },
              }}
            >
              +{quickStep}
            </IconButton>
          ))}
        </Box>
      )}

      {/* Display min/max hints */}
      <Box
        sx={{
          display: 'flex',
          justifyContent: 'space-between',
          width: '100%',
          fontSize: '0.75rem',
          color: 'text.secondary',
        }}
      >
        <span>Min: {min}</span>
        <span>Max: {max}</span>
      </Box>
    </Paper>
  );
};

export default TouchQuantityInput;
