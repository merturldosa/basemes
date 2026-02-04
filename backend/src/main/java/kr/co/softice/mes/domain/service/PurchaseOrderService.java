package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Purchase Order Service
 * 구매 주문 서비스
 *
 * @author Moon Myung-seop
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final SupplierRepository supplierRepository;
    private final MaterialRepository materialRepository;
    private final PurchaseRequestRepository purchaseRequestRepository;

    /**
     * 테넌트별 모든 구매 주문 조회
     */
    public List<PurchaseOrderEntity> getAllPurchaseOrders(String tenantId) {
        log.info("Fetching all purchase orders for tenant: {}", tenantId);
        return purchaseOrderRepository.findByTenantIdWithAllRelations(tenantId);
    }

    /**
     * 구매 주문 ID로 조회
     */
    public PurchaseOrderEntity getPurchaseOrderById(Long purchaseOrderId) {
        log.info("Fetching purchase order by ID: {}", purchaseOrderId);
        return purchaseOrderRepository.findByIdWithAllRelations(purchaseOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Purchase order not found: " + purchaseOrderId));
    }

    /**
     * 테넌트 및 상태별 조회
     */
    public List<PurchaseOrderEntity> getPurchaseOrdersByStatus(String tenantId, String status) {
        log.info("Fetching purchase orders by status {} for tenant: {}", status, tenantId);
        return purchaseOrderRepository.findByTenantIdAndStatus(tenantId, status);
    }

    /**
     * 테넌트 및 공급업체별 조회
     */
    public List<PurchaseOrderEntity> getPurchaseOrdersBySupplier(String tenantId, Long supplierId) {
        log.info("Fetching purchase orders by supplier {} for tenant: {}", supplierId, tenantId);
        return purchaseOrderRepository.findByTenantIdAndSupplierId(tenantId, supplierId);
    }

    /**
     * 구매 주문 생성
     */
    @Transactional
    public PurchaseOrderEntity createPurchaseOrder(String tenantId, PurchaseOrderEntity purchaseOrder) {
        log.info("Creating purchase order: {} for tenant: {}", purchaseOrder.getOrderNo(), tenantId);

        // Check if order number already exists
        if (purchaseOrderRepository.existsByTenant_TenantIdAndOrderNo(tenantId, purchaseOrder.getOrderNo())) {
            throw new IllegalArgumentException("Purchase order number already exists: " + purchaseOrder.getOrderNo());
        }

        // Get tenant
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));

        purchaseOrder.setTenant(tenant);

        // Set buyer
        if (purchaseOrder.getBuyer() != null && purchaseOrder.getBuyer().getUserId() != null) {
            UserEntity buyer = userRepository.findById(purchaseOrder.getBuyer().getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Buyer not found: " + purchaseOrder.getBuyer().getUserId()));
            purchaseOrder.setBuyer(buyer);
        }

        // Set supplier
        if (purchaseOrder.getSupplier() != null && purchaseOrder.getSupplier().getSupplierId() != null) {
            SupplierEntity supplier = supplierRepository.findById(purchaseOrder.getSupplier().getSupplierId())
                    .orElseThrow(() -> new IllegalArgumentException("Supplier not found: " + purchaseOrder.getSupplier().getSupplierId()));
            purchaseOrder.setSupplier(supplier);
        }

        // Set default values
        if (purchaseOrder.getStatus() == null) {
            purchaseOrder.setStatus("DRAFT");
        }
        if (purchaseOrder.getOrderDate() == null) {
            purchaseOrder.setOrderDate(LocalDateTime.now());
        }

        // Process items
        if (purchaseOrder.getItems() != null) {
            for (PurchaseOrderItemEntity item : purchaseOrder.getItems()) {
                // Set material
                if (item.getMaterial() != null && item.getMaterial().getMaterialId() != null) {
                    MaterialEntity material = materialRepository.findById(item.getMaterial().getMaterialId())
                            .orElseThrow(() -> new IllegalArgumentException("Material not found: " + item.getMaterial().getMaterialId()));
                    item.setMaterial(material);
                }

                // Set purchase request if provided
                if (item.getPurchaseRequest() != null && item.getPurchaseRequest().getPurchaseRequestId() != null) {
                    PurchaseRequestEntity purchaseRequest = purchaseRequestRepository.findById(item.getPurchaseRequest().getPurchaseRequestId())
                            .orElseThrow(() -> new IllegalArgumentException("Purchase request not found: " + item.getPurchaseRequest().getPurchaseRequestId()));
                    item.setPurchaseRequest(purchaseRequest);

                    // Update purchase request status to ORDERED
                    purchaseRequest.setStatus("ORDERED");
                    purchaseRequestRepository.save(purchaseRequest);
                }

                // Initialize received quantity
                if (item.getReceivedQuantity() == null) {
                    item.setReceivedQuantity(BigDecimal.ZERO);
                }

                // Calculate amount
                if (item.getUnitPrice() != null && item.getOrderedQuantity() != null) {
                    item.setAmount(item.getUnitPrice().multiply(item.getOrderedQuantity()));
                }
            }

            // Calculate total amount
            BigDecimal totalAmount = purchaseOrder.getItems().stream()
                    .map(PurchaseOrderItemEntity::getAmount)
                    .filter(amount -> amount != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            purchaseOrder.setTotalAmount(totalAmount);
        }

        PurchaseOrderEntity saved = purchaseOrderRepository.save(purchaseOrder);
        log.info("Purchase order created successfully: {}", saved.getPurchaseOrderId());

        return purchaseOrderRepository.findByIdWithAllRelations(saved.getPurchaseOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Failed to retrieve created purchase order"));
    }

    /**
     * 구매 주문 수정
     */
    @Transactional
    public PurchaseOrderEntity updatePurchaseOrder(Long purchaseOrderId, PurchaseOrderEntity updatedOrder) {
        log.info("Updating purchase order: {}", purchaseOrderId);

        PurchaseOrderEntity existingOrder = purchaseOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Purchase order not found: " + purchaseOrderId));

        // Can only update DRAFT orders
        if (!"DRAFT".equals(existingOrder.getStatus())) {
            throw new IllegalArgumentException("Only draft purchase orders can be updated");
        }

        // Update fields
        existingOrder.setExpectedDeliveryDate(updatedOrder.getExpectedDeliveryDate());
        existingOrder.setDeliveryAddress(updatedOrder.getDeliveryAddress());
        existingOrder.setPaymentTerms(updatedOrder.getPaymentTerms());
        existingOrder.setCurrency(updatedOrder.getCurrency());
        existingOrder.setRemarks(updatedOrder.getRemarks());

        // Update items if provided
        if (updatedOrder.getItems() != null) {
            existingOrder.getItems().clear();

            for (PurchaseOrderItemEntity item : updatedOrder.getItems()) {
                // Set material
                if (item.getMaterial() != null && item.getMaterial().getMaterialId() != null) {
                    MaterialEntity material = materialRepository.findById(item.getMaterial().getMaterialId())
                            .orElseThrow(() -> new IllegalArgumentException("Material not found: " + item.getMaterial().getMaterialId()));
                    item.setMaterial(material);
                }

                // Calculate amount
                if (item.getUnitPrice() != null && item.getOrderedQuantity() != null) {
                    item.setAmount(item.getUnitPrice().multiply(item.getOrderedQuantity()));
                }

                existingOrder.addItem(item);
            }

            // Recalculate total amount
            BigDecimal totalAmount = existingOrder.getItems().stream()
                    .map(PurchaseOrderItemEntity::getAmount)
                    .filter(amount -> amount != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            existingOrder.setTotalAmount(totalAmount);
        }

        PurchaseOrderEntity saved = purchaseOrderRepository.save(existingOrder);
        log.info("Purchase order updated successfully: {}", saved.getPurchaseOrderId());

        return purchaseOrderRepository.findByIdWithAllRelations(saved.getPurchaseOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Failed to retrieve updated purchase order"));
    }

    /**
     * 구매 주문 확정
     */
    @Transactional
    public PurchaseOrderEntity confirmPurchaseOrder(Long purchaseOrderId) {
        log.info("Confirming purchase order: {}", purchaseOrderId);

        PurchaseOrderEntity purchaseOrder = purchaseOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Purchase order not found: " + purchaseOrderId));

        if (!"DRAFT".equals(purchaseOrder.getStatus())) {
            throw new IllegalArgumentException("Only draft purchase orders can be confirmed");
        }

        purchaseOrder.setStatus("CONFIRMED");

        PurchaseOrderEntity saved = purchaseOrderRepository.save(purchaseOrder);
        log.info("Purchase order confirmed successfully: {}", saved.getPurchaseOrderId());

        return purchaseOrderRepository.findByIdWithAllRelations(saved.getPurchaseOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Failed to retrieve confirmed purchase order"));
    }

    /**
     * 구매 주문 취소
     */
    @Transactional
    public PurchaseOrderEntity cancelPurchaseOrder(Long purchaseOrderId) {
        log.info("Cancelling purchase order: {}", purchaseOrderId);

        PurchaseOrderEntity purchaseOrder = purchaseOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Purchase order not found: " + purchaseOrderId));

        if ("CANCELLED".equals(purchaseOrder.getStatus())) {
            throw new IllegalArgumentException("Purchase order is already cancelled");
        }

        if ("RECEIVED".equals(purchaseOrder.getStatus())) {
            throw new IllegalArgumentException("Cannot cancel fully received purchase order");
        }

        purchaseOrder.setStatus("CANCELLED");

        PurchaseOrderEntity saved = purchaseOrderRepository.save(purchaseOrder);
        log.info("Purchase order cancelled successfully: {}", saved.getPurchaseOrderId());

        return purchaseOrderRepository.findByIdWithAllRelations(saved.getPurchaseOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Failed to retrieve cancelled purchase order"));
    }

    /**
     * 구매 주문 삭제
     */
    @Transactional
    public void deletePurchaseOrder(Long purchaseOrderId) {
        log.info("Deleting purchase order: {}", purchaseOrderId);

        PurchaseOrderEntity purchaseOrder = purchaseOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Purchase order not found: " + purchaseOrderId));

        if (!"DRAFT".equals(purchaseOrder.getStatus()) && !"CANCELLED".equals(purchaseOrder.getStatus())) {
            throw new IllegalArgumentException("Only draft or cancelled purchase orders can be deleted");
        }

        purchaseOrderRepository.deleteById(purchaseOrderId);
        log.info("Purchase order deleted successfully: {}", purchaseOrderId);
    }
}
