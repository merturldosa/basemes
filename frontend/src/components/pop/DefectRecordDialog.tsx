import React, { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  FormControl,
  FormLabel,
  RadioGroup,
  FormControlLabel,
  Radio,
  Box,
  Alert,
  MenuItem,
  Select,
  InputLabel,
  Typography,
  IconButton,
} from '@mui/material';
import {
  Close as CloseIcon,
  CameraAlt as CameraIcon,
  Warning as WarningIcon,
} from '@mui/icons-material';
import TouchQuantityInput from './TouchQuantityInput';

/**
 * Defect Record Dialog
 * Dialog for recording defects with quantity, type, and reason
 * @author Moon Myung-seop
 */

interface DefectRecordDialogProps {
  open: boolean;
  onClose: () => void;
  onSubmit: (defect: DefectRecord) => void;
  maxQuantity?: number;
}

export interface DefectRecord {
  defectQuantity: number;
  defectType: string;
  defectReason?: string;
  defectLocation?: string;
  severity: 'CRITICAL' | 'MAJOR' | 'MINOR';
  notes?: string;
  photo?: File;
}

const DefectRecordDialog: React.FC<DefectRecordDialogProps> = ({
  open,
  onClose,
  onSubmit,
  maxQuantity = 1000,
}) => {
  const [defectQuantity, setDefectQuantity] = useState(1);
  const [defectType, setDefectType] = useState('');
  const [defectReason, setDefectReason] = useState('');
  const [defectLocation, setDefectLocation] = useState('');
  const [severity, setSeverity] = useState<'CRITICAL' | 'MAJOR' | 'MINOR'>('MINOR');
  const [notes, setNotes] = useState('');
  const [photo, setPhoto] = useState<File | null>(null);
  const [errors, setErrors] = useState<Record<string, string>>({});

  const defectTypes = [
    { value: '외관불량', label: '외관 불량' },
    { value: '치수불량', label: '치수 불량' },
    { value: '기능불량', label: '기능 불량' },
    { value: '재질불량', label: '재질 불량' },
    { value: '조립불량', label: '조립 불량' },
    { value: '포장불량', label: '포장 불량' },
    { value: '기타', label: '기타' },
  ];

  const handleReset = () => {
    setDefectQuantity(1);
    setDefectType('');
    setDefectReason('');
    setDefectLocation('');
    setSeverity('MINOR');
    setNotes('');
    setPhoto(null);
    setErrors({});
  };

  const handleClose = () => {
    handleReset();
    onClose();
  };

  const handlePhotoCapture = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (file) {
      setPhoto(file);
    }
  };

  const validate = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (defectQuantity <= 0) {
      newErrors.defectQuantity = '불량 수량은 1 이상이어야 합니다';
    }

    if (!defectType) {
      newErrors.defectType = '불량 유형을 선택하세요';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = () => {
    if (!validate()) {
      return;
    }

    const defect: DefectRecord = {
      defectQuantity,
      defectType,
      defectReason: defectReason || undefined,
      defectLocation: defectLocation || undefined,
      severity,
      notes: notes || undefined,
      photo: photo || undefined,
    };

    onSubmit(defect);
    handleClose();
  };

  return (
    <Dialog
      open={open}
      onClose={handleClose}
      maxWidth="sm"
      fullWidth
      PaperProps={{
        sx: {
          borderRadius: 2,
        },
      }}
    >
      <DialogTitle
        sx={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          bgcolor: 'error.main',
          color: 'white',
          py: 2,
        }}
      >
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <WarningIcon />
          <Typography variant="h6" fontWeight="bold">
            불량 기록
          </Typography>
        </Box>
        <IconButton onClick={handleClose} sx={{ color: 'white' }}>
          <CloseIcon />
        </IconButton>
      </DialogTitle>

      <DialogContent sx={{ pt: 3 }}>
        {/* Defect Quantity */}
        <Box sx={{ mb: 3, display: 'flex', justifyContent: 'center' }}>
          <TouchQuantityInput
            value={defectQuantity}
            onChange={setDefectQuantity}
            min={1}
            max={maxQuantity}
            label="불량 수량"
            size="medium"
          />
        </Box>

        {errors.defectQuantity && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {errors.defectQuantity}
          </Alert>
        )}

        {/* Defect Type */}
        <FormControl fullWidth sx={{ mb: 2 }} error={!!errors.defectType}>
          <InputLabel>불량 유형 *</InputLabel>
          <Select
            value={defectType}
            onChange={(e) => setDefectType(e.target.value)}
            label="불량 유형 *"
          >
            {defectTypes.map((type) => (
              <MenuItem key={type.value} value={type.value}>
                {type.label}
              </MenuItem>
            ))}
          </Select>
          {errors.defectType && (
            <Typography variant="caption" color="error" sx={{ mt: 0.5 }}>
              {errors.defectType}
            </Typography>
          )}
        </FormControl>

        {/* Severity */}
        <FormControl component="fieldset" sx={{ mb: 2 }}>
          <FormLabel component="legend">심각도</FormLabel>
          <RadioGroup
            row
            value={severity}
            onChange={(e) => setSeverity(e.target.value as any)}
          >
            <FormControlLabel
              value="MINOR"
              control={<Radio />}
              label="경미"
            />
            <FormControlLabel
              value="MAJOR"
              control={<Radio />}
              label="주요"
            />
            <FormControlLabel
              value="CRITICAL"
              control={<Radio />}
              label="심각"
            />
          </RadioGroup>
        </FormControl>

        {/* Defect Reason */}
        <TextField
          fullWidth
          label="불량 사유"
          value={defectReason}
          onChange={(e) => setDefectReason(e.target.value)}
          multiline
          rows={2}
          sx={{ mb: 2 }}
          placeholder="불량 발생 원인을 입력하세요"
        />

        {/* Defect Location */}
        <TextField
          fullWidth
          label="불량 위치"
          value={defectLocation}
          onChange={(e) => setDefectLocation(e.target.value)}
          sx={{ mb: 2 }}
          placeholder="예: 상단 모서리, 중앙부"
        />

        {/* Notes */}
        <TextField
          fullWidth
          label="비고"
          value={notes}
          onChange={(e) => setNotes(e.target.value)}
          multiline
          rows={2}
          sx={{ mb: 2 }}
          placeholder="추가 메모 사항"
        />

        {/* Photo Capture */}
        <Box sx={{ mb: 2 }}>
          <Button
            variant="outlined"
            startIcon={<CameraIcon />}
            component="label"
            fullWidth
          >
            {photo ? `사진 첨부됨: ${photo.name}` : '불량 사진 촬영 (선택)'}
            <input
              type="file"
              hidden
              accept="image/*"
              capture="environment"
              onChange={handlePhotoCapture}
            />
          </Button>
        </Box>
      </DialogContent>

      <DialogActions sx={{ px: 3, pb: 3, gap: 1 }}>
        <Button
          onClick={handleClose}
          variant="outlined"
          size="large"
          fullWidth
        >
          취소
        </Button>
        <Button
          onClick={handleSubmit}
          variant="contained"
          color="error"
          size="large"
          fullWidth
        >
          불량 기록
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default DefectRecordDialog;
