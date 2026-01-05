package com.myname.finguard.accounts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.myname.finguard.accounts.controller.AccountController;
import com.myname.finguard.accounts.dto.CreateAccountRequest;
import com.myname.finguard.accounts.dto.UserBalanceResponse;
import com.myname.finguard.accounts.service.AccountService;
import com.myname.finguard.auth.model.User;
import com.myname.finguard.auth.repository.UserRepository;
import com.myname.finguard.common.exception.ApiException;
import com.myname.finguard.common.model.Role;
import com.myname.finguard.security.UserPrincipal;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

class AccountControllerTest {

    @Mock
    private AccountService accountService;
    @Mock
    private UserRepository userRepository;

    private AccountController controller;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        controller = new AccountController(accountService, userRepository);
    }

    @Test
    void usesUserPrincipalId() {
        User user = user(10L, "user@example.com");
        UserPrincipal principal = new UserPrincipal(user);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        UserBalanceResponse expected = new UserBalanceResponse(Collections.emptyList(), Collections.emptyList());
        when(accountService.getUserBalance(10L)).thenReturn(expected);

        var response = controller.balance(auth);

        assertThat(response.getBody()).isSameAs(expected);
        verify(accountService).getUserBalance(10L);
    }

    @Test
    void resolvesUserIdFromUserDetailsEmail() {
        UserDetails principal = new org.springframework.security.core.userdetails.User(
                "details@example.com", "pwd", Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        User user = user(99L, "details@example.com");
        when(userRepository.findByEmail("details@example.com")).thenReturn(Optional.of(user));

        UserBalanceResponse expected = new UserBalanceResponse(Collections.emptyList(), Collections.emptyList());
        when(accountService.getUserBalance(99L)).thenReturn(expected);

        controller.balance(auth);

        verify(accountService).getUserBalance(99L);
    }

    @Test
    void throwsUnauthorizedForAnonymous() {
        Authentication auth = new UsernamePasswordAuthenticationToken("anonymousUser", null, Collections.emptyList());
        assertThatThrownBy(() -> controller.balance(auth))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("User is not authenticated");
    }

    @Test
    void createUsesUserPrincipalIdAndReturnsCreated() {
        User user = user(10L, "user@example.com");
        UserPrincipal principal = new UserPrincipal(user);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        CreateAccountRequest request = new CreateAccountRequest("Visa", "USD", new BigDecimal("10.00"));
        UserBalanceResponse.AccountBalance created = new UserBalanceResponse.AccountBalance(
                123L, "Visa", "USD", new BigDecimal("10.00"), false
        );
        when(accountService.createAccount(10L, request)).thenReturn(created);

        var response = controller.create(request, auth);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).isSameAs(created);
        verify(accountService).createAccount(10L, request);
    }

    @Test
    void createResolvesUserIdFromUserDetailsEmail() {
        UserDetails principal = new org.springframework.security.core.userdetails.User(
                "details@example.com", "pwd", Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        User user = user(99L, "details@example.com");
        when(userRepository.findByEmail("details@example.com")).thenReturn(Optional.of(user));

        CreateAccountRequest request = new CreateAccountRequest("Cash", "USD", BigDecimal.ZERO);
        UserBalanceResponse.AccountBalance created = new UserBalanceResponse.AccountBalance(1L, "Cash", "USD", BigDecimal.ZERO, false);
        when(accountService.createAccount(99L, request)).thenReturn(created);

        controller.create(request, auth);

        verify(accountService).createAccount(99L, request);
    }

    @Test
    void createThrowsUnauthorizedForAnonymous() {
        Authentication auth = new UsernamePasswordAuthenticationToken("anonymousUser", null, Collections.emptyList());
        assertThatThrownBy(() -> controller.create(new CreateAccountRequest("Test", "USD", BigDecimal.ZERO), auth))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("User is not authenticated");
    }

    private User user(Long id, String email) {
        User u = new User();
        u.setId(id);
        u.setEmail(email);
        u.setPasswordHash("hash");
        u.setRole(Role.USER);
        return u;
    }
}
