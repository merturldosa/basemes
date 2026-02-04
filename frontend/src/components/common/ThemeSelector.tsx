/**
 * Theme Selector Component
 * Allows users to switch between themes and modes
 * @author Moon Myung-seop
 */

import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import {
  IconButton,
  Menu,
  MenuItem,
  ListItemIcon,
  ListItemText,
  Divider,
  Box,
  Typography,
  Tooltip,
  Switch,
} from '@mui/material';
import {
  Palette,
  Check,
  DarkMode,
  LightMode,
  Science,
  Computer,
  LocalHospital,
  Restaurant,
  BusinessCenter,
} from '@mui/icons-material';
import { useThemeStore } from '@/stores/themeStore';

// Theme configurations
const themes = [
  {
    code: 'DEFAULT',
    name: { ko: '기본', en: 'Default', zh: '默认' },
    icon: <BusinessCenter />,
    description: { ko: 'SoIce 기본 테마', en: 'SoIce default theme', zh: 'SoIce默认主题' },
  },
  {
    code: 'CHEMICAL',
    name: { ko: '화학 제조업', en: 'Chemical', zh: '化工' },
    icon: <Science />,
    description: { ko: '화학 제조업 전용 테마', en: 'Chemical manufacturing theme', zh: '化工制造主题' },
  },
  {
    code: 'ELECTRONICS',
    name: { ko: '전자/전기', en: 'Electronics', zh: '电子' },
    icon: <Computer />,
    description: { ko: '전자/전기 제조업 테마', en: 'Electronics manufacturing theme', zh: '电子制造主题' },
  },
  {
    code: 'MEDICAL',
    name: { ko: '의료기기', en: 'Medical Device', zh: '医疗器械' },
    icon: <LocalHospital />,
    description: { ko: '의료기기 제조업 테마', en: 'Medical device manufacturing theme', zh: '医疗器械制造主题' },
  },
  {
    code: 'FOOD',
    name: { ko: '식품/음료', en: 'Food & Beverage', zh: '食品饮料' },
    icon: <Restaurant />,
    description: { ko: '식품/음료 제조업 테마', en: 'Food & beverage manufacturing theme', zh: '食品饮料制造主题' },
  },
];

export default function ThemeSelector() {
  const { i18n } = useTranslation();
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const { currentTheme, mode, setTheme, toggleMode } = useThemeStore();

  const handleClick = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const handleThemeChange = (themeCode: string) => {
    setTheme(themeCode);
    handleClose();
  };

  const getCurrentTheme = () => {
    return themes.find((t) => t.code === currentTheme) || themes[0];
  };

  const currentLang = i18n.language as 'ko' | 'en' | 'zh';

  return (
    <>
      <Tooltip title="테마 변경">
        <IconButton
          onClick={handleClick}
          size="small"
          sx={{ ml: 1 }}
          color="inherit"
        >
          <Palette />
        </IconButton>
      </Tooltip>

      <Menu
        anchorEl={anchorEl}
        open={Boolean(anchorEl)}
        onClose={handleClose}
        anchorOrigin={{
          vertical: 'bottom',
          horizontal: 'right',
        }}
        transformOrigin={{
          vertical: 'top',
          horizontal: 'right',
        }}
        PaperProps={{
          sx: { minWidth: 280, maxWidth: 320 },
        }}
      >
        {/* Dark Mode Toggle */}
        <Box sx={{ px: 2, py: 1.5, display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            {mode === 'dark' ? <DarkMode fontSize="small" /> : <LightMode fontSize="small" />}
            <Typography variant="body2">
              {mode === 'dark' ? '다크 모드' : '라이트 모드'}
            </Typography>
          </Box>
          <Switch
            size="small"
            checked={mode === 'dark'}
            onChange={toggleMode}
            inputProps={{ 'aria-label': 'dark mode toggle' }}
          />
        </Box>

        <Divider />

        {/* Theme Title */}
        <Box sx={{ px: 2, py: 1, bgcolor: 'action.hover' }}>
          <Typography variant="caption" color="text.secondary" fontWeight={600}>
            산업별 테마
          </Typography>
        </Box>

        {/* Theme List */}
        {themes.map((theme) => (
          <MenuItem
            key={theme.code}
            onClick={() => handleThemeChange(theme.code)}
            selected={currentTheme === theme.code}
          >
            <ListItemIcon>
              {theme.icon}
            </ListItemIcon>
            <ListItemText
              primary={
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  {theme.name[currentLang] || theme.name.ko}
                  {currentTheme === theme.code && (
                    <Check fontSize="small" color="primary" />
                  )}
                </Box>
              }
              secondary={
                <Typography variant="caption" color="text.secondary">
                  {theme.description[currentLang] || theme.description.ko}
                </Typography>
              }
            />
          </MenuItem>
        ))}

        <Divider />

        {/* Current Theme Info */}
        <Box sx={{ px: 2, py: 1.5 }}>
          <Typography variant="caption" color="text.secondary">
            현재 테마: <strong>{getCurrentTheme().name[currentLang] || getCurrentTheme().name.ko}</strong>
          </Typography>
        </Box>
      </Menu>
    </>
  );
}
