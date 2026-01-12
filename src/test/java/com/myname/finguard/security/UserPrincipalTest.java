package com.myname.finguard.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.myname.finguard.auth.model.User;
import com.myname.finguard.common.model.Role;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

class UserPrincipalTest {

    @Test
    void mapsAdminRoleToAuthority() {
        User user = new User();
        user.setId(1L);
        user.setEmail("admin@example.com");
        user.setPasswordHash("hash");
        user.setRole(Role.ADMIN);

        UserPrincipal principal = new UserPrincipal(user);

        assertThat(principal.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .contains("ROLE_ADMIN");
    }

    @Test
    void defaultsToUserRoleWhenNull() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setPasswordHash("hash");
        user.setRole(null);

        UserPrincipal principal = new UserPrincipal(user);

        assertThat(principal.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .contains("ROLE_USER");
    }
}
