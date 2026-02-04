/**
 * Mobile Inventory List Page
 * Mobile-optimized inventory view with infinite scroll
 * @author Moon Myung-seop
 */

import { useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Chip,
  Stack,
  IconButton,
  TextField,
  InputAdornment,
  Paper,
  Divider,
} from '@mui/material';
import {
  Search as SearchIcon,
  QrCodeScanner as ScanIcon,
  Inventory as InventoryIcon,
  TrendingUp as TrendingUpIcon,
  TrendingDown as TrendingDownIcon,
} from '@mui/icons-material';
import InfiniteScrollList from '@/components/common/InfiniteScrollList';
import inventoryService from '@/services/inventoryService';

interface InventoryItem {
  inventoryId: string;
  productCode: string;
  productName: string;
  warehouseName: string;
  locationCode: string;
  quantity: number;
  unit: string;
  status: string;
  lastUpdatedAt: string;
}

export default function MobileInventoryListPage() {
  const [searchText, setSearchText] = useState('');
  const [refreshKey, setRefreshKey] = useState(0);

  /**
   * Load inventory data
   */
  const loadInventoryData = async (page: number) => {
    const response = await inventoryService.getInventoryStatus({
      page,
      size: 20,
      search: searchText || undefined,
    });
    return response.content;
  };

  /**
   * Handle search
   */
  const handleSearch = () => {
    setRefreshKey((prev) => prev + 1);
  };

  /**
   * Render inventory card
   */
  const renderInventoryCard = (item: InventoryItem) => {
    const isLowStock = item.quantity < 100;
    const isOutOfStock = item.quantity === 0;

    return (
      <Card
        elevation={2}
        sx={{
          '&:hover': {
            boxShadow: 4,
            transform: 'translateY(-2px)',
            transition: 'all 0.2s',
          },
        }}
      >
        <CardContent>
          {/* Header with Product Info */}
          <Stack direction="row" spacing={2} alignItems="flex-start" mb={2}>
            <Box
              sx={{
                bgcolor: 'primary.light',
                color: 'primary.contrastText',
                p: 1.5,
                borderRadius: 2,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
              }}
            >
              <InventoryIcon />
            </Box>

            <Box sx={{ flexGrow: 1 }}>
              <Typography variant="h6" fontWeight="bold" gutterBottom>
                {item.productName}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                {item.productCode}
              </Typography>
            </Box>

            {/* Status Chip */}
            <Chip
              label={item.status}
              color={
                item.status === 'AVAILABLE'
                  ? 'success'
                  : item.status === 'RESERVED'
                  ? 'warning'
                  : 'default'
              }
              size="small"
            />
          </Stack>

          <Divider sx={{ my: 2 }} />

          {/* Inventory Details */}
          <Stack spacing={1.5}>
            {/* Warehouse & Location */}
            <Stack direction="row" justifyContent="space-between">
              <Typography variant="body2" color="text.secondary">
                창고
              </Typography>
              <Typography variant="body2" fontWeight="medium">
                {item.warehouseName}
              </Typography>
            </Stack>

            <Stack direction="row" justifyContent="space-between">
              <Typography variant="body2" color="text.secondary">
                위치
              </Typography>
              <Typography variant="body2" fontWeight="medium">
                {item.locationCode}
              </Typography>
            </Stack>

            {/* Quantity with Visual Indicator */}
            <Stack direction="row" justifyContent="space-between" alignItems="center">
              <Typography variant="body2" color="text.secondary">
                재고 수량
              </Typography>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                {isOutOfStock ? (
                  <Chip
                    label="재고 없음"
                    color="error"
                    size="small"
                    icon={<TrendingDownIcon />}
                  />
                ) : isLowStock ? (
                  <>
                    <TrendingDownIcon fontSize="small" color="warning" />
                    <Typography variant="body1" fontWeight="bold" color="warning.main">
                      {item.quantity.toLocaleString()} {item.unit}
                    </Typography>
                  </>
                ) : (
                  <>
                    <TrendingUpIcon fontSize="small" color="success" />
                    <Typography variant="body1" fontWeight="bold" color="success.main">
                      {item.quantity.toLocaleString()} {item.unit}
                    </Typography>
                  </>
                )}
              </Box>
            </Stack>

            {/* Last Updated */}
            <Stack direction="row" justifyContent="space-between">
              <Typography variant="caption" color="text.secondary">
                최종 업데이트
              </Typography>
              <Typography variant="caption" color="text.secondary">
                {new Date(item.lastUpdatedAt).toLocaleString('ko-KR')}
              </Typography>
            </Stack>
          </Stack>
        </CardContent>
      </Card>
    );
  };

  return (
    <Box
      sx={{
        minHeight: '100vh',
        bgcolor: 'background.default',
        pb: 3,
      }}
    >
      {/* Header */}
      <Paper
        elevation={0}
        sx={{
          p: 2,
          mb: 2,
          position: 'sticky',
          top: 0,
          zIndex: 100,
          borderRadius: 0,
        }}
      >
        <Typography variant="h5" fontWeight="bold" gutterBottom>
          재고 현황
        </Typography>

        {/* Search Bar */}
        <Stack direction="row" spacing={1} mt={2}>
          <TextField
            fullWidth
            placeholder="제품명 또는 제품코드 검색"
            value={searchText}
            onChange={(e) => setSearchText(e.target.value)}
            onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
            size="small"
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon />
                </InputAdornment>
              ),
            }}
          />
          <IconButton
            color="primary"
            onClick={handleSearch}
            sx={{
              bgcolor: 'primary.main',
              color: 'white',
              '&:hover': { bgcolor: 'primary.dark' },
            }}
          >
            <SearchIcon />
          </IconButton>
          <IconButton
            color="primary"
            sx={{
              bgcolor: 'secondary.main',
              color: 'white',
              '&:hover': { bgcolor: 'secondary.dark' },
            }}
          >
            <ScanIcon />
          </IconButton>
        </Stack>
      </Paper>

      {/* Infinite Scroll List */}
      <Box sx={{ px: 2 }}>
        <InfiniteScrollList<InventoryItem>
          key={refreshKey}
          loadMore={loadInventoryData}
          renderItem={renderInventoryCard}
          getItemKey={(item) => item.inventoryId}
          pageSize={20}
          emptyMessage="재고 데이터가 없습니다"
          spacing={2}
        />
      </Box>
    </Box>
  );
}
