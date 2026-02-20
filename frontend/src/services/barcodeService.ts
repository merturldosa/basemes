import api from './api';

export interface QRScanRequest {
  qrData: string;
  scanLocation?: string;
  scanPurpose?: string;
}

export interface LotInfo {
  lotId: number;
  lotNo: string;
  itemId?: number;
  productCode: string;
  productName: string;
  currentQuantity: number;
  expiryDate?: string;
  qualityStatus: string;
  unit: string;
  isActive: boolean;
}

export interface QRCodeResponse {
  lotId?: number;
  lotNo?: string;
  qrCodeImage: string;
  qrData?: string;
}

/**
 * 바코드/QR 코드 서비스
 */
export const barcodeService = {
  /**
   * LOT QR 코드 생성 (LOT ID)
   */
  generateQRCode: async (lotId: number): Promise<{ data: QRCodeResponse }> => {
    return api.get(`/barcodes/lot/${lotId}/qrcode`);
  },

  /**
   * LOT QR 코드 생성 (LOT 번호)
   */
  generateQRCodeByLotNo: async (lotNo: string): Promise<{ data: QRCodeResponse }> => {
    return api.get(`/barcodes/lot/number/${lotNo}/qrcode`);
  },

  /**
   * QR 코드 스캔
   */
  scan: async (request: QRScanRequest): Promise<{ data: LotInfo }> => {
    return api.post('/barcodes/scan', request);
  },

  /**
   * 텍스트 QR 코드 생성
   */
  generateTextQRCode: async (data: string): Promise<{ data: string }> => {
    return api.post('/barcodes/qrcode/generate', { data });
  }
};
