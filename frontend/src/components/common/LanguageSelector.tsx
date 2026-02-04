/**
 * Language Selector Component
 * Allows users to switch between supported languages
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
  Tooltip,
} from '@mui/material';
import { Language, Check } from '@mui/icons-material';
import { supportedLanguages, SupportedLanguage } from '@/i18n/config';

export default function LanguageSelector() {
  const { i18n, t } = useTranslation();
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);

  const handleClick = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const handleLanguageChange = (languageCode: SupportedLanguage) => {
    i18n.changeLanguage(languageCode);
    handleClose();
  };

  const currentLanguage = supportedLanguages.find(
    (lang) => lang.code === i18n.language
  ) || supportedLanguages[0];

  return (
    <>
      <Tooltip title={t('settings.language')}>
        <IconButton
          onClick={handleClick}
          size="small"
          sx={{ ml: 1 }}
          color="inherit"
        >
          <Language />
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
      >
        {supportedLanguages.map((language) => (
          <MenuItem
            key={language.code}
            onClick={() => handleLanguageChange(language.code)}
            selected={currentLanguage.code === language.code}
          >
            {currentLanguage.code === language.code && (
              <ListItemIcon>
                <Check fontSize="small" />
              </ListItemIcon>
            )}
            <ListItemText
              inset={currentLanguage.code !== language.code}
              primary={language.nativeName}
              secondary={language.name !== language.nativeName ? language.name : undefined}
            />
          </MenuItem>
        ))}
      </Menu>
    </>
  );
}
