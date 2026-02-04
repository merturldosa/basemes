package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.domain.entity.ProductEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.repository.ProductRepository;
import kr.co.softice.mes.domain.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Product Service Test
 * 제품 마스터 서비스 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("제품 서비스 테스트")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private ProductService productService;

    private TenantEntity testTenant;
    private ProductEntity testProduct;
    private Long productId;
    private String tenantId;

    @BeforeEach
    void setUp() {
        tenantId = "TEST001";
        productId = 1L;

        testTenant = new TenantEntity();
        testTenant.setTenantId(tenantId);
        testTenant.setTenantName("Test Tenant");

        testProduct = new ProductEntity();
        testProduct.setProductId(productId);
        testProduct.setTenant(testTenant);
        testProduct.setProductCode("PROD001");
        testProduct.setProductName("Test Product");
        testProduct.setProductType("FINISHED_GOODS");
        testProduct.setIsActive(true);
    }

    // === 조회 테스트 ===

    @Test
    @DisplayName("테넌트별 제품 조회 - 성공")
    void testFindByTenant_Success() {
        List<ProductEntity> products = Arrays.asList(testProduct);
        when(productRepository.findByTenantIdWithTenant(tenantId))
                .thenReturn(products);

        List<ProductEntity> result = productService.findByTenant(tenantId);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductCode()).isEqualTo("PROD001");
        verify(productRepository).findByTenantIdWithTenant(tenantId);
    }

    @Test
    @DisplayName("활성 제품 조회 - 성공")
    void testFindActiveByTenant_Success() {
        testProduct.setIsActive(true);
        List<ProductEntity> products = Arrays.asList(testProduct);
        when(productRepository.findByTenantIdAndIsActiveWithTenant(tenantId, true))
                .thenReturn(products);

        List<ProductEntity> result = productService.findActiveByTenant(tenantId);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIsActive()).isTrue();
        verify(productRepository).findByTenantIdAndIsActiveWithTenant(tenantId, true);
    }

    @Test
    @DisplayName("제품 ID로 조회 - 성공")
    void testFindById_Success() {
        when(productRepository.findById(productId))
                .thenReturn(Optional.of(testProduct));

        Optional<ProductEntity> result = productService.findById(productId);

        assertThat(result).isPresent();
        assertThat(result.get().getProductCode()).isEqualTo("PROD001");
        verify(productRepository).findById(productId);
    }

    @Test
    @DisplayName("제품 코드로 조회 - 성공")
    void testFindByProductCode_Success() {
        when(productRepository.findByTenant_TenantIdAndProductCode(tenantId, "PROD001"))
                .thenReturn(Optional.of(testProduct));

        Optional<ProductEntity> result = productService.findByProductCode(tenantId, "PROD001");

        assertThat(result).isPresent();
        assertThat(result.get().getProductCode()).isEqualTo("PROD001");
        verify(productRepository).findByTenant_TenantIdAndProductCode(tenantId, "PROD001");
    }

    // === 생성 테스트 ===

    @Test
    @DisplayName("제품 생성 - 성공")
    void testCreateProduct_Success() {
        ProductEntity newProduct = new ProductEntity();
        newProduct.setTenant(testTenant);
        newProduct.setProductCode("PROD999");
        newProduct.setProductName("New Product");

        when(productRepository.existsByTenantAndProductCode(testTenant, "PROD999"))
                .thenReturn(false);
        when(productRepository.save(any(ProductEntity.class)))
                .thenReturn(newProduct);

        ProductEntity result = productService.createProduct(newProduct);

        assertThat(result).isNotNull();
        verify(productRepository).save(newProduct);
    }

    @Test
    @DisplayName("제품 생성 - 실패 (중복 코드)")
    void testCreateProduct_Fail_DuplicateCode() {
        ProductEntity newProduct = new ProductEntity();
        newProduct.setTenant(testTenant);
        newProduct.setProductCode("PROD001");

        when(productRepository.existsByTenantAndProductCode(testTenant, "PROD001"))
                .thenReturn(true);

        assertThatThrownBy(() -> productService.createProduct(newProduct))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product code already exists");
    }

    // === 수정 테스트 ===

    @Test
    @DisplayName("제품 수정 - 성공")
    void testUpdateProduct_Success() {
        testProduct.setProductName("Updated Product");

        when(productRepository.existsById(productId))
                .thenReturn(true);
        when(productRepository.save(any(ProductEntity.class)))
                .thenReturn(testProduct);

        ProductEntity result = productService.updateProduct(testProduct);

        assertThat(result).isNotNull();
        verify(productRepository).save(testProduct);
    }

    @Test
    @DisplayName("제품 수정 - 실패 (존재하지 않음)")
    void testUpdateProduct_Fail_NotFound() {
        when(productRepository.existsById(productId))
                .thenReturn(false);

        assertThatThrownBy(() -> productService.updateProduct(testProduct))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product not found");
    }

    // === 삭제 테스트 ===

    @Test
    @DisplayName("제품 삭제 - 성공")
    void testDeleteProduct_Success() {
        productService.deleteProduct(productId);

        verify(productRepository).deleteById(productId);
    }

    // === 활성화/비활성화 테스트 ===

    @Test
    @DisplayName("제품 활성화 - 성공")
    void testActivateProduct_Success() {
        testProduct.setIsActive(false);

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(ProductEntity.class)))
                .thenAnswer(invocation -> {
                    ProductEntity saved = invocation.getArgument(0);
                    assertThat(saved.getIsActive()).isTrue();
                    return saved;
                });

        ProductEntity result = productService.activateProduct(productId);

        assertThat(result).isNotNull();
        verify(productRepository).save(testProduct);
    }

    @Test
    @DisplayName("제품 비활성화 - 성공")
    void testDeactivateProduct_Success() {
        testProduct.setIsActive(true);

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(ProductEntity.class)))
                .thenAnswer(invocation -> {
                    ProductEntity saved = invocation.getArgument(0);
                    assertThat(saved.getIsActive()).isFalse();
                    return saved;
                });

        ProductEntity result = productService.deactivateProduct(productId);

        assertThat(result).isNotNull();
        verify(productRepository).save(testProduct);
    }

    @Test
    @DisplayName("제품 활성화 - 실패 (존재하지 않음)")
    void testActivateProduct_Fail_NotFound() {
        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.activateProduct(productId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    @DisplayName("제품 비활성화 - 실패 (존재하지 않음)")
    void testDeactivateProduct_Fail_NotFound() {
        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.deactivateProduct(productId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product not found");
    }
}
