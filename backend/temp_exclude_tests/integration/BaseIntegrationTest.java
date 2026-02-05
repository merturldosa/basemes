package kr.co.softice.mes.integration;

import kr.co.softice.mes.config.TenantContext;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.entity.UserEntity;
import kr.co.softice.mes.domain.repository.TenantRepository;
import kr.co.softice.mes.domain.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base Integration Test Class
 * Provides common setup for integration tests
 * @author Moon Myung-seop
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected TenantRepository tenantRepository;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    protected TenantEntity testTenant;
    protected UserEntity testUser;
    protected String testTenantId = "test-tenant";
    protected String testUsername = "testuser";

    @BeforeEach
    public void setUp() {
        // Set tenant context
        TenantContext.setCurrentTenant(testTenantId);

        // Create test tenant if not exists
        testTenant = tenantRepository.findById(testTenantId)
                .orElseGet(() -> {
                    TenantEntity tenant = TenantEntity.builder()
                            .tenantId(testTenantId)
                            .tenantName("Test Tenant")
                            .active(true)
                            .build();
                    return tenantRepository.save(tenant);
                });

        // Create test user if not exists
        testUser = userRepository.findByTenantIdAndUsername(testTenantId, testUsername)
                .orElseGet(() -> {
                    UserEntity user = UserEntity.builder()
                            .tenant(testTenant)
                            .username(testUsername)
                            .password(passwordEncoder.encode("password"))
                            .email("test@example.com")
                            .fullName("Test User")
                            .active(true)
                            .build();
                    return userRepository.save(user);
                });
    }

    @AfterEach
    public void tearDown() {
        // Clear tenant context
        TenantContext.clear();
    }

    /**
     * Helper method to get authentication token for API calls
     */
    protected String getAuthToken() {
        // In a real scenario, this would call the auth endpoint and return JWT token
        // For testing purposes, we'll use a mock token or configure security to bypass
        return "Bearer test-token";
    }

    /**
     * Helper method to set tenant context
     */
    protected void setTenantContext(String tenantId) {
        TenantContext.setCurrentTenant(tenantId);
    }
}
