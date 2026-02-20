/**
 * Themes Management Page
 * @author Moon Myung-seop
 */

import { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Paper,
  Grid,
  Card,
  CardContent,
  CardActions,
  Button,
  Chip,
} from '@mui/material';
import { Palette, CheckCircle } from '@mui/icons-material';
import { useThemeStore } from '@/stores/themeStore';

interface ThemeCardProps {
  code: string;
  name: string;
  industry: string;
  description: string;
  isActive: boolean;
  onSelect: () => void;
}

function ThemeCard({ name, industry, description, isActive, onSelect }: ThemeCardProps) {
  return (
    <Card
      sx={{
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
        border: isActive ? 2 : 0,
        borderColor: 'primary.main',
      }}
    >
      <CardContent sx={{ flexGrow: 1 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
          <Palette color="primary" />
          <Typography variant="h6">{name}</Typography>
          {isActive && <CheckCircle color="primary" fontSize="small" />}
        </Box>
        <Chip label={industry} size="small" sx={{ mb: 2 }} />
        <Typography variant="body2" color="text.secondary">
          {description}
        </Typography>
      </CardContent>
      <CardActions>
        <Button
          size="small"
          variant={isActive ? 'contained' : 'outlined'}
          onClick={onSelect}
          disabled={isActive}
          fullWidth
        >
          {isActive ? '현재 테마' : '테마 적용'}
        </Button>
      </CardActions>
    </Card>
  );
}

const PRESET_THEMES = [
  {
    code: 'DEFAULT',
    name: '기본 테마',
    industry: '범용',
    description: '범용 제조업을 위한 기본 테마입니다.',
  },
  {
    code: 'CHEMICAL',
    name: '화학 제조',
    industry: '화학',
    description: '화학 제조업에 최적화된 딥 블루 테마입니다.',
  },
  {
    code: 'ELECTRONICS',
    name: '전자/전기',
    industry: '전자',
    description: '전자/전기 제조업을 위한 테크 블루 테마입니다.',
  },
  {
    code: 'MEDICAL',
    name: '의료기기',
    industry: '의료',
    description: '의료기기 제조업을 위한 메디컬 틸 테마입니다.',
  },
  {
    code: 'FOOD',
    name: '식품/음료',
    industry: '식품',
    description: '식품/음료 제조업을 위한 그린 테마입니다.',
  },
];

export default function ThemesPage() {
  const { currentTheme, setTheme } = useThemeStore();
  const [selectedTheme, setSelectedTheme] = useState(currentTheme);

  useEffect(() => {
    setSelectedTheme(currentTheme);
  }, [currentTheme]);

  const handleSelectTheme = (themeCode: string) => {
    setTheme(themeCode);
    setSelectedTheme(themeCode);
    // Reload page to apply new theme
    window.location.reload();
  };

  return (
    <Box>
      <Typography variant="h4" gutterBottom fontWeight="bold">
        테마 설정
      </Typography>
      <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
        산업별 최적화된 테마를 선택하세요
      </Typography>

      <Grid container spacing={3}>
        {PRESET_THEMES.map((theme) => (
          <Grid item xs={12} sm={6} md={4} key={theme.code}>
            <ThemeCard
              {...theme}
              isActive={theme.code === selectedTheme}
              onSelect={() => handleSelectTheme(theme.code)}
            />
          </Grid>
        ))}
      </Grid>

      <Box sx={{ mt: 4 }}>
        <Paper sx={{ p: 3 }}>
          <Typography variant="h6" gutterBottom>
            테마 정보
          </Typography>
          <Typography variant="body2" color="text.secondary">
            각 산업에 최적화된 색상, 레이아웃, 활성화된 모듈이 자동으로 적용됩니다.
            테마를 변경하면 페이지가 새로고침됩니다.
          </Typography>
        </Paper>
      </Box>
    </Box>
  );
}
