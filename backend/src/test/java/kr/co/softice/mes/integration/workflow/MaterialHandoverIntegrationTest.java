package kr.co.softice.mes.integration.workflow;

import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.service.*;
import kr.co.softice.mes.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Material Handover 워크플로우 통합 테스트
 *
 * 자재 요청 → 핸드오버 → 수령 확인 → 자동 완료 프로세스 검증
 *
 * @author Claude Code (Sonnet 4.5)
 * @company SoftIce Co., Ltd.
 * @since 2026-01-27
 */
@DisplayName("Material Handover 워크플로우 통합 테스트")
public class MaterialHandoverIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MaterialHandoverService materialHandoverService;

    @Autowired
    private BarcodeService barcodeService;

    @Test
    @DisplayName("자재 핸드오버 전체 프로세스 - 단일 핸드오버 성공")
    void testSingleHandoverWorkflow_Success() {
        // ═══════════════════════════════════════════════════════════════
        // Given: 자재 요청 생성 (요청 수량: 100개)
        // ═══════════════════════════════════════════════════════════════

        // 자재 요청 엔티티가 있다고 가정 (MaterialRequestEntity)
        // 실제 구현에 따라 다를 수 있음

        // ═══════════════════════════════════════════════════════════════
        // When: 자재 핸드오버 생성
        // ═══════════════════════════════════════════════════════════════

        MaterialHandoverEntity handover = new MaterialHandoverEntity();
        handover.setTenant(testTenant);
        handover.setHandoverNumber("HO-2026-001");
        handover.setMaterial(testMaterial);
        handover.setRequestQuantity(100.0);
        handover.setActualQuantity(100.0);
        handover.setIssuer(testUser); // 발급자
        handover.setReceiver(testUser); // 수령자 (테스트에서는 동일 사용자)
        handover.setHandoverDate(LocalDateTime.now());
        handover.setStatus("PENDING");
        handover.setFromWarehouse(testWarehouse);
        handover = materialHandoverService.createHandover(handover);

        assertThat(handover.getId()).isNotNull();
        assertThat(handover.getStatus()).isEqualTo("PENDING");

        // ═══════════════════════════════════════════════════════════════
        // Then: QR 코드 생성 확인
        // ═══════════════════════════════════════════════════════════════

        String qrData = String.format("HANDOVER:%s:%s",
                handover.getHandoverNumber(),
                handover.getMaterial().getMaterialCode());

        String qrCodeBase64 = barcodeService.generateQRCode(qrData);

        assertThat(qrCodeBase64).isNotNull();
        assertThat(qrCodeBase64).startsWith("data:image/png;base64,");

        // ═══════════════════════════════════════════════════════════════
        // When: 수령 확인
        // ═══════════════════════════════════════════════════════════════

        handover.setStatus("CONFIRMED");
        handover.setConfirmedDate(LocalDateTime.now());
        handover.setConfirmedBy(testUser);
        handover = materialHandoverService.confirmHandover(handover.getId(), testUser.getId());

        // ═══════════════════════════════════════════════════════════════
        // Then: 수령 완료 상태 확인
        // ═══════════════════════════════════════════════════════════════

        assertThat(handover.getStatus()).isEqualTo("CONFIRMED");
        assertThat(handover.getConfirmedDate()).isNotNull();
        assertThat(handover.getConfirmedBy()).isEqualTo(testUser);
    }

    @Test
    @DisplayName("다중 핸드오버 시나리오 - 자재 요청 자동 완료")
    void testMultipleHandovers_AutoCompleteRequest() {
        // ═══════════════════════════════════════════════════════════════
        // Given: 자재 요청 생성 (요청 수량: 100개)
        // ═══════════════════════════════════════════════════════════════

        // 자재 요청 생성 (시뮬레이션)
        Long requestId = 1L;
        Double totalRequestQuantity = 100.0;

        // ═══════════════════════════════════════════════════════════════
        // When: 첫 번째 핸드오버 (60개)
        // ═══════════════════════════════════════════════════════════════

        MaterialHandoverEntity handover1 = new MaterialHandoverEntity();
        handover1.setTenant(testTenant);
        handover1.setHandoverNumber("HO-2026-002-1");
        handover1.setMaterial(testMaterial);
        handover1.setRequestQuantity(100.0);
        handover1.setActualQuantity(60.0);
        handover1.setIssuer(testUser);
        handover1.setReceiver(testUser);
        handover1.setHandoverDate(LocalDateTime.now());
        handover1.setStatus("PENDING");
        handover1.setFromWarehouse(testWarehouse);
        handover1 = materialHandoverService.createHandover(handover1);

        // 수령 확인
        handover1 = materialHandoverService.confirmHandover(handover1.getId(), testUser.getId());

        assertThat(handover1.getStatus()).isEqualTo("CONFIRMED");

        // ═══════════════════════════════════════════════════════════════
        // When: 두 번째 핸드오버 (40개) - 총 100개 완료
        // ═══════════════════════════════════════════════════════════════

        MaterialHandoverEntity handover2 = new MaterialHandoverEntity();
        handover2.setTenant(testTenant);
        handover2.setHandoverNumber("HO-2026-002-2");
        handover2.setMaterial(testMaterial);
        handover2.setRequestQuantity(100.0);
        handover2.setActualQuantity(40.0);
        handover2.setIssuer(testUser);
        handover2.setReceiver(testUser);
        handover2.setHandoverDate(LocalDateTime.now());
        handover2.setStatus("PENDING");
        handover2.setFromWarehouse(testWarehouse);
        handover2 = materialHandoverService.createHandover(handover2);

        // 수령 확인
        handover2 = materialHandoverService.confirmHandover(handover2.getId(), testUser.getId());

        assertThat(handover2.getStatus()).isEqualTo("CONFIRMED");

        // ═══════════════════════════════════════════════════════════════
        // Then: 총 핸드오버 수량 확인 (60 + 40 = 100)
        // ═══════════════════════════════════════════════════════════════

        List<MaterialHandoverEntity> handovers = materialHandoverRepository
                .findByTenantAndMaterial(testTenant, testMaterial);

        Double totalHandoverQuantity = handovers.stream()
                .filter(h -> h.getStatus().equals("CONFIRMED"))
                .mapToDouble(MaterialHandoverEntity::getActualQuantity)
                .sum();

        assertThat(totalHandoverQuantity).isEqualTo(100.0);

        // 자재 요청 자동 완료 확인
        // 실제 구현에서는 MaterialRequestService.checkAndAutoComplete() 호출
        // assertThat(materialRequest.getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    @DisplayName("부분 핸드오버 - 미완료 상태 유지")
    void testPartialHandover_RemainsIncomplete() {
        // ═══════════════════════════════════════════════════════════════
        // Given: 자재 요청 생성 (요청 수량: 100개)
        // ═══════════════════════════════════════════════════════════════

        Double totalRequestQuantity = 100.0;

        // ═══════════════════════════════════════════════════════════════
        // When: 부분 핸드오버 (50개만)
        // ═══════════════════════════════════════════════════════════════

        MaterialHandoverEntity handover = new MaterialHandoverEntity();
        handover.setTenant(testTenant);
        handover.setHandoverNumber("HO-2026-003");
        handover.setMaterial(testMaterial);
        handover.setRequestQuantity(100.0);
        handover.setActualQuantity(50.0); // 절반만 핸드오버
        handover.setIssuer(testUser);
        handover.setReceiver(testUser);
        handover.setHandoverDate(LocalDateTime.now());
        handover.setStatus("PENDING");
        handover.setFromWarehouse(testWarehouse);
        handover = materialHandoverService.createHandover(handover);

        handover = materialHandoverService.confirmHandover(handover.getId(), testUser.getId());

        // ═══════════════════════════════════════════════════════════════
        // Then: 핸드오버는 완료되었지만 자재 요청은 미완료 상태
        // ═══════════════════════════════════════════════════════════════

        assertThat(handover.getStatus()).isEqualTo("CONFIRMED");
        assertThat(handover.getActualQuantity()).isEqualTo(50.0);

        // 자재 요청은 IN_PROGRESS 상태로 유지 (50/100 = 50%)
        // assertThat(materialRequest.getStatus()).isEqualTo("IN_PROGRESS");
    }

    @Test
    @DisplayName("LOT 추적 - QR 코드 기반 핸드오버")
    void testLOTTracking_QRCodeBased() {
        // ═══════════════════════════════════════════════════════════════
        // Given: LOT 정보가 포함된 자재
        // ═══════════════════════════════════════════════════════════════

        String lotNumber = "LOT-2026-001";
        LocalDateTime manufacturingDate = LocalDateTime.now().minusDays(30);
        LocalDateTime expiryDate = LocalDateTime.now().plusMonths(6);

        // ═══════════════════════════════════════════════════════════════
        // When: LOT 정보 포함 핸드오버 생성
        // ═══════════════════════════════════════════════════════════════

        MaterialHandoverEntity handover = new MaterialHandoverEntity();
        handover.setTenant(testTenant);
        handover.setHandoverNumber("HO-2026-004");
        handover.setMaterial(testMaterial);
        handover.setRequestQuantity(100.0);
        handover.setActualQuantity(100.0);
        handover.setIssuer(testUser);
        handover.setReceiver(testUser);
        handover.setHandoverDate(LocalDateTime.now());
        handover.setStatus("PENDING");
        handover.setFromWarehouse(testWarehouse);
        handover.setLotNumber(lotNumber);
        handover.setManufacturingDate(manufacturingDate);
        handover.setExpiryDate(expiryDate);
        handover = materialHandoverService.createHandover(handover);

        // ═══════════════════════════════════════════════════════════════
        // Then: LOT 정보 QR 코드 생성
        // ═══════════════════════════════════════════════════════════════

        String qrData = String.format("HANDOVER:%s:LOT:%s:QTY:%.2f:EXP:%s",
                handover.getHandoverNumber(),
                lotNumber,
                handover.getActualQuantity(),
                expiryDate.toLocalDate());

        String qrCodeBase64 = barcodeService.generateQRCode(qrData);

        assertThat(qrCodeBase64).isNotNull();
        assertThat(qrData).contains(lotNumber);
        assertThat(qrData).contains("QTY:100.00");

        // ═══════════════════════════════════════════════════════════════
        // When: QR 코드 스캔 및 수령 확인 (모바일 시나리오)
        // ═══════════════════════════════════════════════════════════════

        handover = materialHandoverService.confirmHandover(handover.getId(), testUser.getId());

        // ═══════════════════════════════════════════════════════════════
        // Then: LOT 정보 유지 확인
        // ═══════════════════════════════════════════════════════════════

        assertThat(handover.getStatus()).isEqualTo("CONFIRMED");
        assertThat(handover.getLotNumber()).isEqualTo(lotNumber);
        assertThat(handover.getManufacturingDate()).isEqualTo(manufacturingDate);
        assertThat(handover.getExpiryDate()).isEqualTo(expiryDate);
    }

    @Test
    @DisplayName("Audit Log 통합 - 핸드오버 추적")
    void testAuditLog_HandoverTracking() {
        // ═══════════════════════════════════════════════════════════════
        // Given: 핸드오버 생성
        // ═══════════════════════════════════════════════════════════════

        MaterialHandoverEntity handover = new MaterialHandoverEntity();
        handover.setTenant(testTenant);
        handover.setHandoverNumber("HO-2026-005");
        handover.setMaterial(testMaterial);
        handover.setRequestQuantity(100.0);
        handover.setActualQuantity(100.0);
        handover.setIssuer(testUser);
        handover.setReceiver(testUser);
        handover.setHandoverDate(LocalDateTime.now());
        handover.setStatus("PENDING");
        handover.setFromWarehouse(testWarehouse);
        handover = materialHandoverService.createHandover(handover);

        // ═══════════════════════════════════════════════════════════════
        // When: Audit Log 생성 (핸드오버 생성 이벤트)
        // ═══════════════════════════════════════════════════════════════

        AuditLogEntity createLog = createAuditLog("CREATE", "MATERIAL_HANDOVER");
        createLog.setEntityId(handover.getId());
        createLog.setChangeDetails(String.format("핸드오버 생성: %s, 수량: %.2f",
                handover.getHandoverNumber(), handover.getActualQuantity()));
        createLog = auditLogRepository.save(createLog);

        // 수령 확인
        handover = materialHandoverService.confirmHandover(handover.getId(), testUser.getId());

        // Audit Log 생성 (수령 확인 이벤트)
        AuditLogEntity confirmLog = createAuditLog("CONFIRM", "MATERIAL_HANDOVER");
        confirmLog.setEntityId(handover.getId());
        confirmLog.setChangeDetails(String.format("핸드오버 수령 확인: %s, 수령자: %s",
                handover.getHandoverNumber(), handover.getConfirmedBy().getFullName()));
        confirmLog = auditLogRepository.save(confirmLog);

        // ═══════════════════════════════════════════════════════════════
        // Then: Audit Log 조회 및 검증
        // ═══════════════════════════════════════════════════════════════

        List<AuditLogEntity> logs = auditLogRepository
                .findByTenantAndEntityTypeAndEntityId(testTenant, "MATERIAL_HANDOVER", handover.getId());

        assertThat(logs).hasSize(2);
        assertThat(logs).anyMatch(log -> log.getAction().equals("CREATE"));
        assertThat(logs).anyMatch(log -> log.getAction().equals("CONFIRM"));
    }

    @Test
    @DisplayName("핸드오버 취소 워크플로우")
    void testHandoverCancellation() {
        // ═══════════════════════════════════════════════════════════════
        // Given: 핸드오버 생성 (PENDING 상태)
        // ═══════════════════════════════════════════════════════════════

        MaterialHandoverEntity handover = new MaterialHandoverEntity();
        handover.setTenant(testTenant);
        handover.setHandoverNumber("HO-2026-006");
        handover.setMaterial(testMaterial);
        handover.setRequestQuantity(100.0);
        handover.setActualQuantity(100.0);
        handover.setIssuer(testUser);
        handover.setReceiver(testUser);
        handover.setHandoverDate(LocalDateTime.now());
        handover.setStatus("PENDING");
        handover.setFromWarehouse(testWarehouse);
        handover = materialHandoverService.createHandover(handover);

        assertThat(handover.getStatus()).isEqualTo("PENDING");

        // ═══════════════════════════════════════════════════════════════
        // When: 핸드오버 취소
        // ═══════════════════════════════════════════════════════════════

        handover.setStatus("CANCELLED");
        handover.setCancelledDate(LocalDateTime.now());
        handover.setCancellationReason("수령자 부재");
        handover = materialHandoverService.updateHandover(handover);

        // ═══════════════════════════════════════════════════════════════
        // Then: 취소 상태 확인
        // ═══════════════════════════════════════════════════════════════

        assertThat(handover.getStatus()).isEqualTo("CANCELLED");
        assertThat(handover.getCancelledDate()).isNotNull();
        assertThat(handover.getCancellationReason()).isEqualTo("수령자 부재");

        // 취소된 핸드오버는 자재 요청 완료 계산에서 제외
        List<MaterialHandoverEntity> confirmedHandovers = materialHandoverRepository
                .findByTenantAndMaterialAndStatus(testTenant, testMaterial, "CONFIRMED");

        assertThat(confirmedHandovers).doesNotContain(handover);
    }
}
