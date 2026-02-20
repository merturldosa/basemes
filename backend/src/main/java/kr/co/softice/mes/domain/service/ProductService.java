package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.domain.entity.ProductEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.repository.ProductRepository;
import kr.co.softice.mes.domain.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Product Service
 * 제품 마스터 서비스
 *
 * @author Moon Myung-seop
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final TenantRepository tenantRepository;

    /**
     * Find all products by tenant ID
     */
    public List<ProductEntity> findByTenant(String tenantId) {
        return productRepository.findByTenantIdWithTenant(tenantId);
    }

    /**
     * Find active products by tenant ID
     */
    public List<ProductEntity> findActiveByTenant(String tenantId) {
        return productRepository.findByTenantIdAndIsActiveWithTenant(tenantId, true);
    }

    /**
     * Find product by ID
     */
    public Optional<ProductEntity> findById(Long productId) {
        return productRepository.findById(productId);
    }

    /**
     * Find product by product code
     */
    public Optional<ProductEntity> findByProductCode(String tenantId, String productCode) {
        return productRepository.findByTenant_TenantIdAndProductCode(tenantId, productCode);
    }

    /**
     * Create new product
     */
    @Transactional
    public ProductEntity createProduct(ProductEntity product) {
        log.info("Creating product: {} for tenant: {}",
            product.getProductCode(), product.getTenant().getTenantId());

        // Check duplicate
        if (productRepository.existsByTenantAndProductCode(product.getTenant(), product.getProductCode())) {
            throw new BusinessException(ErrorCode.PRODUCT_ALREADY_EXISTS);
        }

        return productRepository.save(product);
    }

    /**
     * Update product
     */
    @Transactional
    public ProductEntity updateProduct(ProductEntity product) {
        log.info("Updating product: {}", product.getProductId());

        if (!productRepository.existsById(product.getProductId())) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        return productRepository.save(product);
    }

    /**
     * Delete product
     */
    @Transactional
    public void deleteProduct(Long productId) {
        log.info("Deleting product: {}", productId);
        productRepository.deleteById(productId);
    }

    /**
     * Activate product
     */
    @Transactional
    public ProductEntity activateProduct(Long productId) {
        ProductEntity product = productRepository.findById(productId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        product.setIsActive(true);
        return productRepository.save(product);
    }

    /**
     * Deactivate product
     */
    @Transactional
    public ProductEntity deactivateProduct(Long productId) {
        ProductEntity product = productRepository.findById(productId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        product.setIsActive(false);
        return productRepository.save(product);
    }
}
