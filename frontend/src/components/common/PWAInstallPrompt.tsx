/**
 * PWA Install Prompt Component
 * Prompts users to install the app as a PWA
 * @author Moon Myung-seop
 */

import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import {
  Box,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  IconButton,
  Typography,
  Alert,
} from '@mui/material';
import {
  Close,
  GetApp,
  PhoneIphone,
  Computer,
} from '@mui/icons-material';

interface BeforeInstallPromptEvent extends Event {
  prompt: () => Promise<void>;
  userChoice: Promise<{ outcome: 'accepted' | 'dismissed' }>;
}

export default function PWAInstallPrompt() {
  useTranslation();
  const [deferredPrompt, setDeferredPrompt] = useState<BeforeInstallPromptEvent | null>(null);
  const [showPrompt, setShowPrompt] = useState(false);
  const [isInstalled, setIsInstalled] = useState(false);
  const [isIOS, setIsIOS] = useState(false);
  const [showIOSInstructions, setShowIOSInstructions] = useState(false);

  useEffect(() => {
    // Check if already installed
    const isStandalone = window.matchMedia('(display-mode: standalone)').matches;
    const isInWebAppiOS = (window.navigator as any).standalone === true;

    if (isStandalone || isInWebAppiOS) {
      setIsInstalled(true);
      return;
    }

    // Check if user dismissed prompt before
    const dismissedTimestamp = localStorage.getItem('pwa-install-dismissed');
    if (dismissedTimestamp) {
      const daysSinceDismissed = (Date.now() - parseInt(dismissedTimestamp)) / (1000 * 60 * 60 * 24);
      if (daysSinceDismissed < 30) {
        // Don't show again for 30 days
        return;
      }
    }

    // Detect iOS
    const iOS = /iPad|iPhone|iPod/.test(navigator.userAgent) && !(window as any).MSStream;
    setIsIOS(iOS);

    // Handle beforeinstallprompt event (Android, Desktop)
    const handleBeforeInstallPrompt = (e: Event) => {
      e.preventDefault();
      setDeferredPrompt(e as BeforeInstallPromptEvent);
      // Show prompt after 10 seconds
      setTimeout(() => {
        setShowPrompt(true);
      }, 10000);
    };

    window.addEventListener('beforeinstallprompt', handleBeforeInstallPrompt);

    // For iOS, show instructions after 10 seconds
    if (iOS) {
      setTimeout(() => {
        setShowPrompt(true);
      }, 10000);
    }

    // Listen for app installed
    window.addEventListener('appinstalled', () => {
      setIsInstalled(true);
      setShowPrompt(false);
    });

    return () => {
      window.removeEventListener('beforeinstallprompt', handleBeforeInstallPrompt);
    };
  }, []);

  const handleInstallClick = async () => {
    if (isIOS) {
      setShowIOSInstructions(true);
      return;
    }

    if (!deferredPrompt) {
      return;
    }

    // Show install prompt
    deferredPrompt.prompt();

    // Wait for user response
    const { outcome } = await deferredPrompt.userChoice;

    if (outcome === 'accepted') {
      setIsInstalled(true);
    }

    // Clear the deferredPrompt
    setDeferredPrompt(null);
    setShowPrompt(false);
  };

  const handleDismiss = () => {
    setShowPrompt(false);
    setShowIOSInstructions(false);
    // Store dismissal timestamp
    localStorage.setItem('pwa-install-dismissed', Date.now().toString());
  };

  if (isInstalled || !showPrompt) {
    return null;
  }

  return (
    <>
      {/* Install Prompt Dialog */}
      <Dialog
        open={showPrompt && !showIOSInstructions}
        onClose={handleDismiss}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <GetApp color="primary" />
            <span>앱 설치</span>
          </Box>
          <IconButton onClick={handleDismiss} size="small">
            <Close />
          </IconButton>
        </DialogTitle>

        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            <Typography variant="body1">
              SDS MES를 앱으로 설치하시겠습니까?
            </Typography>

            <Alert severity="info" icon={<PhoneIphone />}>
              <Typography variant="body2" sx={{ fontWeight: 600, mb: 1 }}>
                앱 설치 시 장점:
              </Typography>
              <ul style={{ margin: 0, paddingLeft: 20 }}>
                <li>홈 화면에서 빠른 접근</li>
                <li>오프라인에서도 사용 가능</li>
                <li>더 빠른 로딩 속도</li>
                <li>전체 화면 경험</li>
                <li>푸시 알림 수신</li>
              </ul>
            </Alert>

            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, color: 'text.secondary' }}>
              {isIOS ? <PhoneIphone fontSize="small" /> : <Computer fontSize="small" />}
              <Typography variant="caption">
                {isIOS ? '약 2MB, 빠른 설치' : '약 5MB, 즉시 설치'}
              </Typography>
            </Box>
          </Box>
        </DialogContent>

        <DialogActions sx={{ p: 2, pt: 0 }}>
          <Button onClick={handleDismiss} color="inherit">
            나중에
          </Button>
          <Button
            onClick={handleInstallClick}
            variant="contained"
            startIcon={<GetApp />}
            size="large"
          >
            설치
          </Button>
        </DialogActions>
      </Dialog>

      {/* iOS Installation Instructions */}
      <Dialog
        open={showIOSInstructions}
        onClose={handleDismiss}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <PhoneIphone color="primary" />
            <span>iOS 설치 방법</span>
          </Box>
          <IconButton onClick={handleDismiss} size="small">
            <Close />
          </IconButton>
        </DialogTitle>

        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            <Alert severity="info">
              iOS/iPadOS에서는 Safari 브라우저를 사용해야 합니다.
            </Alert>

            <Typography variant="body2" sx={{ fontWeight: 600 }}>
              설치 단계:
            </Typography>

            <ol style={{ margin: 0, paddingLeft: 20, lineHeight: 2 }}>
              <li>
                <strong>공유 버튼</strong>을 탭하세요<br />
                <Typography variant="caption" color="text.secondary">
                  (화면 하단 중앙의 공유 아이콘)
                </Typography>
              </li>
              <li>
                <strong>"홈 화면에 추가"</strong>를 선택하세요
              </li>
              <li>
                <strong>"추가"</strong>를 탭하세요
              </li>
              <li>
                홈 화면에서 <strong>SDS MES</strong> 아이콘을 찾으세요
              </li>
            </ol>

            <Box
              sx={{
                p: 2,
                bgcolor: 'action.hover',
                borderRadius: 1,
                display: 'flex',
                alignItems: 'center',
                gap: 1,
              }}
            >
              <Typography variant="caption">
                💡 <strong>팁:</strong> Safari가 아닌 경우, Safari로 이 페이지를 여세요.
              </Typography>
            </Box>
          </Box>
        </DialogContent>

        <DialogActions sx={{ p: 2, pt: 0 }}>
          <Button onClick={handleDismiss} variant="contained" fullWidth>
            확인
          </Button>
        </DialogActions>
      </Dialog>
    </>
  );
}
