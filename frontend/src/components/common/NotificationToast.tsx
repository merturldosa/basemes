/**
 * Notification Toast Component
 * Displays real-time notifications as toast messages
 * @author Moon Myung-seop
 */

import { useEffect, useState } from 'react';
import {
  Snackbar,
  Alert,
  IconButton,
  Badge,
  Menu,
  MenuItem,
  ListItemText,
  Typography,
  Divider,
  Box,
  Button,
} from '@mui/material';
import {
  Notifications as NotificationsIcon,
  Close as CloseIcon,
  CheckCircle as CheckIcon,
} from '@mui/icons-material';
import { notificationService, Notification } from '@/services/notificationService';
import { useAuthStore } from '@/stores/authStore';
import axios from 'axios';

const NotificationToast: React.FC = () => {
  const [open, setOpen] = useState(false);
  const [currentNotification, setCurrentNotification] = useState<Notification | null>(null);
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);

  const user = useAuthStore((state) => state.user);
  const tenantId = useAuthStore((state) => state.tenantId);

  useEffect(() => {
    if (!user || !tenantId) return;

    // Connect to WebSocket
    notificationService.connect(user.userId, tenantId);

    // Request browser notification permission
    notificationService.requestPermission();

    // Register notification handler
    const unsubscribe = notificationService.onNotification((notification) => {
      // Add to list
      setNotifications((prev) => [notification, ...prev]);
      setUnreadCount((prev) => prev + 1);

      // Show toast
      setCurrentNotification(notification);
      setOpen(true);
    });

    // Load existing notifications
    loadNotifications();

    // Cleanup
    return () => {
      unsubscribe();
      notificationService.disconnect();
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user, tenantId]);

  const loadNotifications = async () => {
    if (!user) return;

    try {
      const response = await axios.get('/api/notifications/unread', {
        params: { userId: user.userId },
      });

      if (response.data.success) {
        setNotifications(response.data.data);
        setUnreadCount(response.data.data.length);
      }
    } catch {
      // Failed to load notifications
    }
  };

  const handleClose = () => {
    setOpen(false);
  };

  const handleMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
  };

  const handleMarkAsRead = async (notificationId: number) => {
    try {
      await axios.post(`/api/notifications/${notificationId}/read`);

      // Update local state
      setNotifications((prev) =>
        prev.map((n) => (n.notificationId === notificationId ? { ...n, isRead: true } : n))
      );
      setUnreadCount((prev) => Math.max(0, prev - 1));
    } catch {
      // Failed to mark notification as read
    }
  };

  const handleMarkAllAsRead = async () => {
    if (!user) return;

    try {
      await axios.post('/api/notifications/read-all', null, {
        params: { userId: user.userId },
      });

      // Update local state
      setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));
      setUnreadCount(0);
    } catch {
      // Failed to mark all as read
    }
  };

  const getNotificationIcon = (type: string) => {
    switch (type) {
      case 'SUCCESS':
        return <CheckIcon />;
      case 'ERROR':
        return <CloseIcon />;
      default:
        return <NotificationsIcon />;
    }
  };

  const getSeverity = (type: string): 'success' | 'info' | 'warning' | 'error' => {
    switch (type) {
      case 'SUCCESS':
        return 'success';
      case 'ERROR':
        return 'error';
      case 'WARNING':
        return 'warning';
      default:
        return 'info';
    }
  };

  return (
    <>
      {/* Notification Bell Icon */}
      <IconButton color="inherit" onClick={handleMenuOpen}>
        <Badge badgeContent={unreadCount} color="error">
          <NotificationsIcon />
        </Badge>
      </IconButton>

      {/* Notification Menu */}
      <Menu
        anchorEl={anchorEl}
        open={Boolean(anchorEl)}
        onClose={handleMenuClose}
        PaperProps={{
          sx: { width: 400, maxHeight: 500 },
        }}
      >
        <Box sx={{ p: 2, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Typography variant="h6">알림</Typography>
          {unreadCount > 0 && (
            <Button size="small" onClick={handleMarkAllAsRead}>
              모두 읽음
            </Button>
          )}
        </Box>
        <Divider />

        {notifications.length === 0 ? (
          <Box sx={{ p: 3, textAlign: 'center' }}>
            <Typography variant="body2" color="text.secondary">
              알림이 없습니다
            </Typography>
          </Box>
        ) : (
          notifications.slice(0, 10).map((notification) => (
            <MenuItem
              key={notification.notificationId}
              onClick={() => {
                handleMarkAsRead(notification.notificationId);
                handleMenuClose();
              }}
              sx={{
                bgcolor: notification.isRead ? 'transparent' : 'action.hover',
                borderLeft: 3,
                borderColor: `${getSeverity(notification.notificationType)}.main`,
              }}
            >
              <ListItemText
                primary={notification.title}
                secondary={
                  <>
                    <Typography variant="body2" color="text.secondary">
                      {notification.message}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      {new Date(notification.createdAt).toLocaleString('ko-KR')}
                    </Typography>
                  </>
                }
                primaryTypographyProps={{
                  fontWeight: notification.isRead ? 'normal' : 'bold',
                }}
              />
            </MenuItem>
          ))
        )}

        {notifications.length > 10 && (
          <>
            <Divider />
            <MenuItem onClick={handleMenuClose}>
              <Typography variant="body2" color="primary" textAlign="center" width="100%">
                모두 보기
              </Typography>
            </MenuItem>
          </>
        )}
      </Menu>

      {/* Toast Notification */}
      <Snackbar
        open={open}
        autoHideDuration={6000}
        onClose={handleClose}
        anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
      >
        <Alert
          onClose={handleClose}
          severity={currentNotification ? getSeverity(currentNotification.notificationType) : 'info'}
          variant="filled"
          sx={{ width: '100%' }}
          icon={currentNotification ? getNotificationIcon(currentNotification.notificationType) : undefined}
        >
          <Typography variant="subtitle2" fontWeight="bold">
            {currentNotification?.title}
          </Typography>
          <Typography variant="body2">{currentNotification?.message}</Typography>
        </Alert>
      </Snackbar>
    </>
  );
};

export default NotificationToast;
