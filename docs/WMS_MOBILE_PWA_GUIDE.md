# WMS 모바일 및 PWA 가이드

## 📋 목차
1. [개요](#개요)
2. [PWA 기능](#pwa-기능)
3. [모바일 실사](#모바일-실사)
4. [QR 스캐너](#qr-스캐너)
5. [설치 가이드](#설치-가이드)

---

## 개요

### Progressive Web App (PWA)
SoIce MES WMS는 PWA로 구현되어 네이티브 앱과 유사한 사용자 경험을 제공합니다.

### 주요 기능
- 📱 **홈 화면 추가** - 앱처럼 설치 가능
- 🔄 **오프라인 지원** - 네트워크 없이도 기본 기능 사용
- 📸 **QR 스캔** - 웹 카메라로 QR 코드 스캔
- 🔔 **푸시 알림** - 만료 임박, 재고 부족 알림
- ⚡ **빠른 로딩** - 캐싱으로 빠른 응답 속도

---

## PWA 기능

### 1. 앱 설치

#### Android/Chrome
1. 브라우저에서 WMS 접속
2. 주소창 옆 "설치" 버튼 클릭
3. 또는 메뉴 → "앱 설치" 또는 "홈 화면에 추가"
4. 홈 화면에 아이콘 생성

#### iOS/Safari
1. Safari에서 WMS 접속
2. 공유 버튼 (↑) 클릭
3. "홈 화면에 추가" 선택
4. 이름 확인 후 "추가"

#### Desktop/Chrome, Edge
1. 주소창 우측 "설치" 아이콘 클릭
2. "설치" 버튼 클릭
3. 독립 창으로 실행

### 2. 오프라인 모드

#### 캐싱 전략
```
정적 리소스 (HTML, CSS, JS, 이미지)
→ Cache First 전략

API 요청 (재고 조회, 제품 정보)
→ Network First 전략 (5분 캐시)

POST 요청 (실사 데이터 전송)
→ Background Sync (네트워크 복구 시 자동 전송)
```

#### 오프라인에서 가능한 기능
- ✅ 캐시된 재고 데이터 조회
- ✅ QR 코드 스캔 및 실사 데이터 입력
- ✅ 로컬 저장소에 임시 저장
- ⏳ 네트워크 복구 시 자동 동기화

#### 오프라인에서 불가능한 기능
- ❌ 실시간 재고 업데이트
- ❌ 새로운 데이터 조회
- ❌ 입하/출하 생성

### 3. 백그라운드 동기화

네트워크가 복구되면 자동으로 대기 중인 작업 실행:
```
오프라인 중 실사 데이터 입력
    ↓
IndexedDB에 임시 저장
    ↓
네트워크 복구 감지
    ↓
Service Worker가 자동 동기화
    ↓
서버로 데이터 전송
    ↓
사용자에게 알림
```

### 4. 푸시 알림

#### 알림 권한 요청
```javascript
// 브라우저에서 알림 권한 요청
Notification.requestPermission().then(permission => {
  if (permission === 'granted') {
    console.log('푸시 알림 허용됨');
  }
});
```

#### 알림 유형
| 알림 | 조건 | 예시 |
|------|------|------|
| 만료 임박 | 유효기간 30일 이내 | "RAW-001 LOT-001 만료 임박 (7일 남음)" |
| 재고 부족 | 안전 재고 미만 | "RAW-002 재고 부족 (현재: 50, 안전: 100)" |
| 실사 완료 | 실사 완료 시 | "원자재 창고 실사 완료" |

---

## 모바일 실사

### 모바일 전용 페이지
**URL**: `/mobile/inventory-check`

### 기능
1. **QR 스캔** - 카메라로 LOT QR 코드 스캔
2. **실사 수량 입력** - 간편한 숫자 입력
3. **차이 자동 계산** - 시스템 vs 실사 수량
4. **진행률 표시** - 완료 항목 / 전체 항목
5. **오프라인 지원** - 네트워크 없이도 입력 가능

### 사용 흐름
```
1. "QR 스캔" 버튼 클릭
   ↓
2. 카메라로 LOT QR 코드 스캔
   ↓
3. LOT 정보 자동 조회
   - 제품명: 원자재-001
   - LOT: LOT-20260124-001
   - 시스템 재고: 1000 KG
   ↓
4. 실사 수량 입력 (예: 980 KG)
   ↓
5. 차이 자동 표시 (-20 KG)
   ↓
6. 다음 LOT 스캔 (반복)
   ↓
7. 모든 항목 완료 후 "완료" 버튼
   ↓
8. 서버로 일괄 전송
```

### UI 스크린샷 설명
```
┌────────────────────────────────┐
│ 📦 모바일 실사                   │
├────────────────────────────────┤
│ 진행률: 2 / 5                   │
│ ████████░░░░░░░░░░ 40%         │
├────────────────────────────────┤
│ ✓ RAW-001                      │
│   LOT-001 | 시스템: 1000 KG    │
│   실사: 980 KG | 차이: -20 KG  │
│                                 │
│ ✓ RAW-002                      │
│   LOT-002 | 시스템: 500 KG     │
│   실사: 500 KG | 차이: 0 KG    │
│                                 │
│ ⏳ RAW-003 (대기 중)            │
├────────────────────────────────┤
│  [QR 스캔]      [완료]         │
└────────────────────────────────┘
```

---

## QR 스캐너

### QRScanner 컴포넌트

#### Props
```typescript
interface QRScannerProps {
  onScan: (data: string) => void;    // 스캔 성공 콜백
  onClose?: () => void;               // 닫기 콜백
  continuous?: boolean;               // 연속 스캔 모드
}
```

#### 사용 예시
```tsx
import QRScanner from '@/components/QRScanner';

function MyPage() {
  const [showScanner, setShowScanner] = useState(false);

  const handleScan = (qrData: string) => {
    console.log('Scanned:', qrData);
    // LOT 정보 조회
    // 실사 수량 입력 다이얼로그 표시
  };

  return (
    <>
      <Button onClick={() => setShowScanner(true)}>
        QR 스캔
      </Button>

      {showScanner && (
        <QRScanner
          onScan={handleScan}
          onClose={() => setShowScanner(false)}
        />
      )}
    </>
  );
}
```

### 기능

#### 1. 자동 초점 및 스캔
- 카메라 자동 초점
- 실시간 QR 코드 인식
- 진동 피드백 (모바일)
- 성공 알림

#### 2. 카메라 전환
- 전면/후면 카메라 전환
- 여러 카메라 지원
- 자동 최적 해상도 선택

#### 3. 플래시
- 어두운 환경에서 플래시 켜기
- 지원되는 기기에서만 활성화

#### 4. 에러 처리
- 카메라 권한 거부 → 안내 메시지
- 카메라 없음 → 에러 표시
- 스캔 실패 → 재시도

### QR 데이터 포맷
```
LOT:{lotNo}|PRODUCT:{productCode}|PRODUCT_NAME:{productName}|QTY:{quantity}|EXPIRY:{expiryDate}|STATUS:{qualityStatus}|UNIT:{unit}
```

**파싱 예시**:
```javascript
const qrData = "LOT:LOT-001|PRODUCT:RAW-001|PRODUCT_NAME:원자재-001|QTY:1000|UNIT:KG";

// 백엔드 API로 파싱
const response = await barcodeService.scan({ qrData });
const lotInfo = response.data;

console.log(lotInfo);
// {
//   lotId: 1,
//   lotNo: "LOT-001",
//   productCode: "RAW-001",
//   productName: "원자재-001",
//   currentQuantity: 1000,
//   unit: "KG"
// }
```

---

## 설치 가이드

### 1. 프론트엔드 설정

#### 의존성 설치
```bash
cd frontend
npm install
```

**추가된 패키지**:
- `@zxing/library` - QR 코드 스캔

#### 개발 서버 실행
```bash
npm run dev
```

#### 프로덕션 빌드
```bash
npm run build
```

### 2. Service Worker 검증

#### 브라우저 개발자 도구
1. F12 → Application 탭
2. Service Workers 섹션
3. "service-worker.js" 등록 확인
4. Status: "activated and is running"

#### 캐시 확인
1. Application → Cache Storage
2. "soice-mes-v1" 캐시 확인
3. 정적 리소스 캐시 확인

### 3. PWA 테스트

#### Lighthouse 감사
```bash
# Chrome DevTools
1. F12 → Lighthouse 탭
2. "Progressive Web App" 체크
3. "Generate report" 클릭
```

**목표 점수**:
- ✅ Installable: 100점
- ✅ PWA Optimized: 90점 이상
- ✅ Performance: 80점 이상

#### 오프라인 테스트
```
1. DevTools → Network 탭
2. "Offline" 체크
3. 페이지 새로고침
4. 오프라인 페이지 또는 캐시된 페이지 표시 확인
```

---

## 모범 사례

### 1. 모바일 UX
- ✅ 터치 친화적인 버튼 크기 (최소 44x44px)
- ✅ 스와이프 제스처 지원
- ✅ 하단 네비게이션 (엄지 닿기 쉬움)
- ✅ 로딩 상태 명확히 표시
- ✅ 에러 메시지 간결하고 명확하게

### 2. 성능 최적화
- ✅ 이미지 lazy loading
- ✅ Code splitting
- ✅ Service Worker 캐싱
- ✅ 번들 크기 최소화
- ✅ 중요 CSS 인라인

### 3. 오프라인 전략
- ✅ 중요 데이터만 캐싱
- ✅ 캐시 만료 시간 설정 (API: 5분)
- ✅ 오프라인 페이지 제공
- ✅ 동기화 실패 시 재시도

### 4. 보안
- ✅ HTTPS 필수 (Service Worker 요구사항)
- ✅ 카메라 권한 명확한 안내
- ✅ 민감 데이터 암호화
- ✅ 로컬 저장소 주기적 정리

---

## 문제 해결

### 문제 1: Service Worker 등록 실패
**원인**: HTTPS가 아니거나 localhost가 아님
**해결**: HTTPS 또는 localhost에서 실행

### 문제 2: 카메라 접근 거부
**원인**: 브라우저 권한 설정
**해결**: 브라우저 설정 → 권한 → 카메라 허용

### 문제 3: QR 스캔 안 됨
**원인**:
- 조명 부족
- QR 코드 손상
- 카메라 초점 문제

**해결**:
- 플래시 켜기
- QR 코드 재출력
- 카메라 거리 조정

### 문제 4: 오프라인 동기화 실패
**원인**: IndexedDB 저장 실패
**해결**: 브라우저 저장공간 확인, 브라우저 캐시 정리

### 문제 5: 앱 설치 프롬프트 안 뜸
**원인**:
- manifest.json 오류
- HTTPS 아님
- 이미 설치됨

**해결**:
- manifest.json 검증
- HTTPS 확인
- 이미 설치된 앱 제거 후 재설치

---

## 향후 기능

### Phase 1
- [ ] 음성 입력 (실사 수량)
- [ ] 배치 스캔 (여러 QR 연속 스캔)
- [ ] 오프라인 동기화 우선순위

### Phase 2
- [ ] Bluetooth 바코드 스캐너 연동
- [ ] NFC 태그 지원
- [ ] 위치 기반 창고 자동 선택

### Phase 3
- [ ] AR (증강현실) 창고 내비게이션
- [ ] AI 기반 재고 예측
- [ ] 실시간 협업 (여러 작업자 동시 실사)

---

**작성일**: 2026-01-24
**버전**: 1.0
**작성자**: Moon Myung-seop
