package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.domain.entity.InventoryEntity;
import kr.co.softice.mes.domain.entity.InventoryTransactionEntity;
import kr.co.softice.mes.domain.entity.LotEntity;
import kr.co.softice.mes.domain.repository.InventoryRepository;
import kr.co.softice.mes.domain.repository.InventoryTransactionRepository;
import kr.co.softice.mes.domain.repository.LotRepository;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Inventory Analysis Service
 * 재고 분석 서비스
 *
 * @author Moon Myung-seop
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryAnalysisService {

    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final LotRepository lotRepository;

    /**
     * 재고 회전율 분석
     * Inventory Turnover Ratio = 출고 수량 / 평균 재고
     *
     * @param tenantId 테넌트 ID
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 제품별 재고 회전율
     */
    public List<InventoryTurnoverAnalysis> analyzeInventoryTurnover(
            String tenantId,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        log.info("Analyzing inventory turnover - Tenant: {}, Period: {} to {}",
                tenantId, startDate, endDate);

        // 기간 내 출고 트랜잭션 조회
        List<InventoryTransactionEntity> outboundTransactions = inventoryTransactionRepository
                .findByTenant_TenantIdAndTransactionDateBetween(tenantId, startDate, endDate)
                .stream()
                .filter(t -> t.getTransactionType().startsWith("OUT_"))
                .collect(Collectors.toList());

        // 제품별 출고 수량 집계
        Map<Long, BigDecimal> productOutboundMap = outboundTransactions.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getProduct().getProductId(),
                        Collectors.mapping(
                                InventoryTransactionEntity::getQuantity,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                        )
                ));

        // 현재 재고 조회
        List<InventoryEntity> currentInventories = inventoryRepository.findByTenant_TenantId(tenantId);

        // 제품별 평균 재고 계산 (현재 재고 기준)
        Map<Long, BigDecimal> productAvgInventoryMap = currentInventories.stream()
                .collect(Collectors.groupingBy(
                        i -> i.getProduct().getProductId(),
                        Collectors.mapping(
                                i -> i.getAvailableQuantity().add(i.getReservedQuantity()),
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                        )
                ));

        // 재고 회전율 계산
        List<InventoryTurnoverAnalysis> result = new ArrayList<>();

        for (Map.Entry<Long, BigDecimal> entry : productOutboundMap.entrySet()) {
            Long productId = entry.getKey();
            BigDecimal outboundQty = entry.getValue();
            BigDecimal avgInventory = productAvgInventoryMap.getOrDefault(productId, BigDecimal.ZERO);

            if (avgInventory.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal turnoverRatio = outboundQty.divide(avgInventory, 2, RoundingMode.HALF_UP);

                // 제품 정보 조회
                InventoryEntity inventory = currentInventories.stream()
                        .filter(i -> i.getProduct().getProductId().equals(productId))
                        .findFirst()
                        .orElse(null);

                if (inventory != null) {
                    result.add(InventoryTurnoverAnalysis.builder()
                            .productId(productId)
                            .productCode(inventory.getProduct().getProductCode())
                            .productName(inventory.getProduct().getProductName())
                            .totalOutboundQuantity(outboundQty)
                            .averageInventory(avgInventory)
                            .turnoverRatio(turnoverRatio)
                            .periodDays(ChronoUnit.DAYS.between(startDate, endDate))
                            .build());
                }
            }
        }

        // 회전율 높은 순으로 정렬
        result.sort(Comparator.comparing(InventoryTurnoverAnalysis::getTurnoverRatio).reversed());

        log.info("Inventory turnover analysis completed - {} products analyzed", result.size());

        return result;
    }

    /**
     * 불용 재고 분석
     * 지정된 기간 동안 출고가 없는 재고
     *
     * @param tenantId 테넌트 ID
     * @param daysThreshold 임계값 (일수)
     * @return 불용 재고 목록
     */
    public List<ObsoleteInventoryAnalysis> analyzeObsoleteInventory(
            String tenantId,
            int daysThreshold) {

        log.info("Analyzing obsolete inventory - Tenant: {}, Days threshold: {}",
                tenantId, daysThreshold);

        LocalDateTime thresholdDate = LocalDateTime.now().minusDays(daysThreshold);

        // 현재 재고 조회
        List<InventoryEntity> currentInventories = inventoryRepository
                .findByTenantIdWithAllRelations(tenantId);

        List<ObsoleteInventoryAnalysis> result = new ArrayList<>();

        for (InventoryEntity inventory : currentInventories) {
            // 재고가 있는 경우만
            BigDecimal totalQty = inventory.getAvailableQuantity().add(inventory.getReservedQuantity());
            if (totalQty.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            // 마지막 트랜잭션 날짜 확인
            LocalDateTime lastTransactionDate = inventory.getLastTransactionDate();

            if (lastTransactionDate == null || lastTransactionDate.isBefore(thresholdDate)) {
                long daysSinceLastTransaction = lastTransactionDate != null ?
                        ChronoUnit.DAYS.between(lastTransactionDate, LocalDateTime.now()) :
                        999; // 트랜잭션 없음

                result.add(ObsoleteInventoryAnalysis.builder()
                        .productId(inventory.getProduct().getProductId())
                        .productCode(inventory.getProduct().getProductCode())
                        .productName(inventory.getProduct().getProductName())
                        .warehouseId(inventory.getWarehouse().getWarehouseId())
                        .warehouseCode(inventory.getWarehouse().getWarehouseCode())
                        .warehouseName(inventory.getWarehouse().getWarehouseName())
                        .lotId(inventory.getLot() != null ? inventory.getLot().getLotId() : null)
                        .lotNo(inventory.getLot() != null ? inventory.getLot().getLotNo() : null)
                        .totalQuantity(totalQty)
                        .lastTransactionDate(lastTransactionDate)
                        .lastTransactionType(inventory.getLastTransactionType())
                        .daysSinceLastTransaction(daysSinceLastTransaction)
                        .build());
            }
        }

        // 오래된 순으로 정렬
        result.sort(Comparator.comparing(ObsoleteInventoryAnalysis::getDaysSinceLastTransaction).reversed());

        log.info("Obsolete inventory analysis completed - {} items found", result.size());

        return result;
    }

    /**
     * 재고 연령 분석
     * LOT별 재고 연령 (생성일로부터 경과 일수)
     *
     * @param tenantId 테넌트 ID
     * @return LOT별 재고 연령
     */
    public List<InventoryAgingAnalysis> analyzeInventoryAging(String tenantId) {

        log.info("Analyzing inventory aging - Tenant: {}", tenantId);

        // LOT이 있는 재고만 조회
        List<InventoryEntity> inventories = inventoryRepository
                .findByTenantIdWithAllRelations(tenantId)
                .stream()
                .filter(i -> i.getLot() != null)
                .collect(Collectors.toList());

        List<InventoryAgingAnalysis> result = new ArrayList<>();

        for (InventoryEntity inventory : inventories) {
            BigDecimal totalQty = inventory.getAvailableQuantity().add(inventory.getReservedQuantity());
            if (totalQty.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            LotEntity lot = inventory.getLot();
            long ageInDays = ChronoUnit.DAYS.between(
                    lot.getCreatedAt().toLocalDate(),
                    LocalDate.now()
            );

            // 연령 구간 분류
            String ageCategory = categorizeAge(ageInDays);

            // 유효기간 임박 여부
            boolean nearExpiry = false;
            Long daysToExpiry = null;
            if (lot.getExpiryDate() != null) {
                daysToExpiry = ChronoUnit.DAYS.between(LocalDate.now(), lot.getExpiryDate());
                nearExpiry = daysToExpiry != null && daysToExpiry <= 30;
            }

            result.add(InventoryAgingAnalysis.builder()
                    .productId(inventory.getProduct().getProductId())
                    .productCode(inventory.getProduct().getProductCode())
                    .productName(inventory.getProduct().getProductName())
                    .lotId(lot.getLotId())
                    .lotNo(lot.getLotNo())
                    .lotCreatedDate(lot.getCreatedAt().toLocalDate())
                    .expiryDate(lot.getExpiryDate())
                    .totalQuantity(totalQty)
                    .ageInDays(ageInDays)
                    .ageCategory(ageCategory)
                    .daysToExpiry(daysToExpiry)
                    .nearExpiry(nearExpiry)
                    .build());
        }

        // 연령 높은 순으로 정렬
        result.sort(Comparator.comparing(InventoryAgingAnalysis::getAgeInDays).reversed());

        log.info("Inventory aging analysis completed - {} lots analyzed", result.size());

        return result;
    }

    /**
     * ABC 분석
     * 재고 가치 기준 중요도 분류
     * A등급: 상위 20% (가치의 80%)
     * B등급: 중위 30% (가치의 15%)
     * C등급: 하위 50% (가치의 5%)
     *
     * @param tenantId 테넌트 ID
     * @return 제품별 ABC 등급
     */
    public List<AbcAnalysis> analyzeABC(String tenantId) {

        log.info("Analyzing ABC classification - Tenant: {}", tenantId);

        // 현재 재고 조회
        List<InventoryEntity> inventories = inventoryRepository
                .findByTenantIdWithAllRelations(tenantId);

        // 제품별 재고 가치 계산 (수량만 사용, 단가 정보 없으면 수량 기준)
        Map<Long, ProductInventoryValue> productValueMap = new HashMap<>();

        for (InventoryEntity inventory : inventories) {
            Long productId = inventory.getProduct().getProductId();
            BigDecimal totalQty = inventory.getAvailableQuantity().add(inventory.getReservedQuantity());

            productValueMap.compute(productId, (k, v) -> {
                if (v == null) {
                    return ProductInventoryValue.builder()
                            .productId(productId)
                            .productCode(inventory.getProduct().getProductCode())
                            .productName(inventory.getProduct().getProductName())
                            .totalQuantity(totalQty)
                            .totalValue(totalQty) // 단가 정보 없으면 수량으로 대체
                            .build();
                } else {
                    v.totalQuantity = v.totalQuantity.add(totalQty);
                    v.totalValue = v.totalValue.add(totalQty);
                    return v;
                }
            });
        }

        // 가치 높은 순으로 정렬
        List<ProductInventoryValue> sortedProducts = productValueMap.values().stream()
                .sorted(Comparator.comparing(ProductInventoryValue::getTotalValue).reversed())
                .collect(Collectors.toList());

        // 전체 가치 계산
        BigDecimal totalValue = sortedProducts.stream()
                .map(ProductInventoryValue::getTotalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // ABC 등급 분류
        List<AbcAnalysis> result = new ArrayList<>();
        BigDecimal cumulativeValue = BigDecimal.ZERO;
        BigDecimal cumulativePercentage = BigDecimal.ZERO;

        for (int i = 0; i < sortedProducts.size(); i++) {
            ProductInventoryValue product = sortedProducts.get(i);
            cumulativeValue = cumulativeValue.add(product.getTotalValue());

            BigDecimal valuePercentage = totalValue.compareTo(BigDecimal.ZERO) > 0 ?
                    product.getTotalValue().divide(totalValue, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)) :
                    BigDecimal.ZERO;

            cumulativePercentage = cumulativePercentage.add(valuePercentage);

            // ABC 등급 결정
            String abcClass;
            if (cumulativePercentage.compareTo(BigDecimal.valueOf(80)) <= 0) {
                abcClass = "A";
            } else if (cumulativePercentage.compareTo(BigDecimal.valueOf(95)) <= 0) {
                abcClass = "B";
            } else {
                abcClass = "C";
            }

            result.add(AbcAnalysis.builder()
                    .productId(product.getProductId())
                    .productCode(product.getProductCode())
                    .productName(product.getProductName())
                    .totalQuantity(product.getTotalQuantity())
                    .totalValue(product.getTotalValue())
                    .valuePercentage(valuePercentage)
                    .cumulativePercentage(cumulativePercentage)
                    .abcClass(abcClass)
                    .rank(i + 1)
                    .build());
        }

        log.info("ABC analysis completed - Total products: {}", result.size());

        return result;
    }

    /**
     * 재고 이동 추이 분석
     * 일별 입출고 추이
     *
     * @param tenantId 테넌트 ID
     * @param days 분석 기간 (일수)
     * @return 일별 재고 이동 통계
     */
    public List<InventoryTrendAnalysis> analyzeInventoryTrend(String tenantId, int days) {

        log.info("Analyzing inventory trend - Tenant: {}, Days: {}", tenantId, days);

        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        LocalDateTime endDate = LocalDateTime.now();

        // 기간 내 모든 트랜잭션 조회
        List<InventoryTransactionEntity> transactions = inventoryTransactionRepository
                .findByTenant_TenantIdAndTransactionDateBetween(tenantId, startDate, endDate);

        // 일별 집계
        Map<LocalDate, DailyInventoryStats> dailyStatsMap = new TreeMap<>();

        for (InventoryTransactionEntity transaction : transactions) {
            LocalDate date = transaction.getTransactionDate().toLocalDate();

            dailyStatsMap.compute(date, (k, v) -> {
                if (v == null) {
                    v = new DailyInventoryStats();
                }

                if (transaction.getTransactionType().startsWith("IN_")) {
                    v.inboundQuantity = v.inboundQuantity.add(transaction.getQuantity());
                    v.inboundCount++;
                } else if (transaction.getTransactionType().startsWith("OUT_")) {
                    v.outboundQuantity = v.outboundQuantity.add(transaction.getQuantity());
                    v.outboundCount++;
                }

                return v;
            });
        }

        // 결과 변환
        List<InventoryTrendAnalysis> result = dailyStatsMap.entrySet().stream()
                .map(entry -> {
                    DailyInventoryStats stats = entry.getValue();
                    BigDecimal netChange = stats.inboundQuantity.subtract(stats.outboundQuantity);

                    return InventoryTrendAnalysis.builder()
                            .date(entry.getKey())
                            .inboundQuantity(stats.inboundQuantity)
                            .inboundCount(stats.inboundCount)
                            .outboundQuantity(stats.outboundQuantity)
                            .outboundCount(stats.outboundCount)
                            .netChange(netChange)
                            .build();
                })
                .collect(Collectors.toList());

        log.info("Inventory trend analysis completed - {} days analyzed", result.size());

        return result;
    }

    /**
     * 재고 연령 구간 분류
     */
    private String categorizeAge(long ageInDays) {
        if (ageInDays <= 30) {
            return "0-30일";
        } else if (ageInDays <= 60) {
            return "31-60일";
        } else if (ageInDays <= 90) {
            return "61-90일";
        } else if (ageInDays <= 180) {
            return "91-180일";
        } else {
            return "180일 초과";
        }
    }

    /**
     * 일별 재고 통계 (내부 클래스)
     */
    private static class DailyInventoryStats {
        BigDecimal inboundQuantity = BigDecimal.ZERO;
        int inboundCount = 0;
        BigDecimal outboundQuantity = BigDecimal.ZERO;
        int outboundCount = 0;
    }

    /**
     * 제품 재고 가치 (내부 클래스)
     */
    @Getter
    @Builder
    private static class ProductInventoryValue {
        private Long productId;
        private String productCode;
        private String productName;
        private BigDecimal totalQuantity;
        private BigDecimal totalValue;
    }

    // ===== Analysis Result Classes =====

    @Getter
    @Builder
    public static class InventoryTurnoverAnalysis {
        private Long productId;
        private String productCode;
        private String productName;
        private BigDecimal totalOutboundQuantity;
        private BigDecimal averageInventory;
        private BigDecimal turnoverRatio;
        private long periodDays;
    }

    @Getter
    @Builder
    public static class ObsoleteInventoryAnalysis {
        private Long productId;
        private String productCode;
        private String productName;
        private Long warehouseId;
        private String warehouseCode;
        private String warehouseName;
        private Long lotId;
        private String lotNo;
        private BigDecimal totalQuantity;
        private LocalDateTime lastTransactionDate;
        private String lastTransactionType;
        private long daysSinceLastTransaction;
    }

    @Getter
    @Builder
    public static class InventoryAgingAnalysis {
        private Long productId;
        private String productCode;
        private String productName;
        private Long lotId;
        private String lotNo;
        private LocalDate lotCreatedDate;
        private LocalDate expiryDate;
        private BigDecimal totalQuantity;
        private long ageInDays;
        private String ageCategory;
        private Long daysToExpiry;
        private boolean nearExpiry;
    }

    @Getter
    @Builder
    public static class AbcAnalysis {
        private Long productId;
        private String productCode;
        private String productName;
        private BigDecimal totalQuantity;
        private BigDecimal totalValue;
        private BigDecimal valuePercentage;
        private BigDecimal cumulativePercentage;
        private String abcClass;
        private int rank;
    }

    @Getter
    @Builder
    public static class InventoryTrendAnalysis {
        private LocalDate date;
        private BigDecimal inboundQuantity;
        private int inboundCount;
        private BigDecimal outboundQuantity;
        private int outboundCount;
        private BigDecimal netChange;
    }
}
