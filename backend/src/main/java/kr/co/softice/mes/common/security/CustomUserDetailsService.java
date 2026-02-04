package kr.co.softice.mes.common.security;

import kr.co.softice.mes.domain.entity.RoleEntity;
import kr.co.softice.mes.domain.entity.UserEntity;
import kr.co.softice.mes.domain.repository.UserRepository;
import kr.co.softice.mes.domain.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Custom UserDetailsService
 * Spring Security에서 사용자 정보 로드
 *
 * @author Moon Myung-seop
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    /**
     * Username과 Tenant ID로 사용자 로드
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String tenantId = TenantContext.getCurrentTenant();

        if (tenantId == null) {
            throw new UsernameNotFoundException("Tenant context not set");
        }

        return loadUserByUsernameAndTenant(username, tenantId);
    }

    /**
     * Username과 Tenant ID로 사용자 로드
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsernameAndTenant(String username, String tenantId) throws UsernameNotFoundException {
        log.debug("Loading user by username: {} and tenant: {}", username, tenantId);

        UserEntity user = userRepository.findByTenant_TenantIdAndUsername(tenantId, username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with username: " + username + " in tenant: " + tenantId
                ));

        // 사용자의 역할 조회
        List<String> roles = userRoleRepository.findRolesByUser(user).stream()
                .map(RoleEntity::getRoleCode)
                .collect(Collectors.toList());

        return UserPrincipal.create(user, roles);
    }

    /**
     * User ID로 사용자 로드
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
        log.debug("Loading user by ID: {}", userId);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));

        // 사용자의 역할 조회
        List<String> roles = userRoleRepository.findRolesByUser(user).stream()
                .map(RoleEntity::getRoleCode)
                .collect(Collectors.toList());

        return UserPrincipal.create(user, roles);
    }
}
