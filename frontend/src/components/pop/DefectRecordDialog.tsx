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
import { useTranslation } from 'react-i18next';

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
  const { t } = useTranslation();
  const [defectQuantity, setDefectQuantity] = useState(1);
  const [defectType, setDefectType] = useState('');
  const [defectReason, setDefectReason] = useState('');
  const [defectLocation, setDefectLocation] = useState('');
  const [severity, setSeverity] = useState<'CRITICAL' | 'MAJOR' | 'MINOR'>('MINOR');
  const [notes, setNotes] = useState('');
  const [photo, setPhoto] = useState<File | null>(null);
  const [errors, setErrors] = useState<Record<string, string>>({});

  const defectTypes = [
    { value: 'APPEARANCE', label: t('defectRecord.defectTypes.appearance') },
    { value: 'DIMENSION', label: t('defectRecord.defectTypes.dimension') },
    { value: 'FUNCTION', label: t('defectRecord.defectTypes.function') },
    { value: 'MATERIAL', label: t('defectRecord.defectTypes.material') },
    { value: 'ASSEMBLY', label: t('defectRecord.defectTypes.assembly') },
    { value: 'PACKAGING', label: t('defectRecord.defectTypes.packaging') },
    { value: 'OTHER', label: t('defectRecord.defectTypes.other') },
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
      newErrors.defectQuantity = t('defectRecord.errors.quantityMin');
    }

    if (!defectType) {
      newErrors.defectType = t('defectRecord.errors.typeRequired');
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
            {t('defectRecord.title')}
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
            label={t('defectRecord.defectQuantity')}
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
          <InputLabel>{t('defectRecord.defectType')} *</InputLabel>
          <Select
            value={defectType}
            onChange={(e) => setDefectType(e.target.value)}
            label={`${t('defectRecord.defectType')} *`}
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
          <FormLabel component="legend">{t('defectRecord.severity')}</FormLabel>
          <RadioGroup
            row
            value={severity}
            onChange={(e) => setSeverity(e.target.value as 'CRITICAL' | 'MAJOR' | 'MINOR')}
          >
            <FormControlLabel
              value="MINOR"
              control={<Radio />}
              label={t('defectRecord.severityLevels.minor')}
            />
            <FormControlLabel
              value="MAJOR"
              control={<Radio />}
              label={t('defectRecord.severityLevels.major')}
            />
            <FormControlLabel
              value="CRITICAL"
              control={<Radio />}
              label={t('defectRecord.severityLevels.critical')}
            />
          </RadioGroup>
        </FormControl>

        {/* Defect Reason */}
        <TextField
          fullWidth
          label={t('defectRecord.defectReason')}
          value={defectReason}
          onChange={(e) => setDefectReason(e.target.value)}
          multiline
          rows={2}
          sx={{ mb: 2 }}
          placeholder={t('defectRecord.placeholders.defectReason')}
        />

        {/* Defect Location */}
        <TextField
          fullWidth
          label={t('defectRecord.defectLocation')}
          value={defectLocation}
          onChange={(e) => setDefectLocation(e.target.value)}
          sx={{ mb: 2 }}
          placeholder={t('defectRecord.placeholders.defectLocation')}
        />

        {/* Notes */}
        <TextField
          fullWidth
          label={t('common.labels.remarks')}
          value={notes}
          onChange={(e) => setNotes(e.target.value)}
          multiline
          rows={2}
          sx={{ mb: 2 }}
          placeholder={t('defectRecord.placeholders.notes')}
        />

        {/* Photo Capture */}
        <Box sx={{ mb: 2 }}>
          <Button
            variant="outlined"
            startIcon={<CameraIcon />}
            component="label"
            fullWidth
          >
            {photo ? t('defectRecord.photoAttached', { name: photo.name }) : t('defectRecord.capturePhoto')}
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
          {t('common.buttons.cancel')}
        </Button>
        <Button
          onClick={handleSubmit}
          variant="contained"
          color="error"
          size="large"
          fullWidth
        >
          {t('defectRecord.title')}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default DefectRecordDialog;
