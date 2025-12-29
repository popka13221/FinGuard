package com.myname.finguard.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.myname.finguard.auth.model.User;
import com.myname.finguard.common.model.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

class JwtAuthenticationFilterTest {

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void skipsWhenNoTokenProvided() throws Exception {
        JwtTokenProvider tokenProvider = mock(JwtTokenProvider.class);
        CustomUserDetailsService userDetailsService = mock(CustomUserDetailsService.class);
        TokenBlacklistService tokenBlacklistService = mock(TokenBlacklistService.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(tokenProvider, userDetailsService, tokenBlacklistService, true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(tokenProvider, never()).isValid(org.mockito.ArgumentMatchers.anyString());
        verify(chain).doFilter(request, response);
    }

    @Test
    void skipsWhenTokenInvalid() throws Exception {
        JwtTokenProvider tokenProvider = mock(JwtTokenProvider.class);
        CustomUserDetailsService userDetailsService = mock(CustomUserDetailsService.class);
        TokenBlacklistService tokenBlacklistService = mock(TokenBlacklistService.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(tokenProvider, userDetailsService, tokenBlacklistService, true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer bad");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        when(tokenProvider.isValid("bad")).thenReturn(false);

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(userDetailsService, never()).loadUserByUsername(org.mockito.ArgumentMatchers.anyString());
        verify(chain).doFilter(request, response);
    }

    @Test
    void skipsWhenTokenTypeIsNotAccess() throws Exception {
        JwtTokenProvider tokenProvider = mock(JwtTokenProvider.class);
        CustomUserDetailsService userDetailsService = mock(CustomUserDetailsService.class);
        TokenBlacklistService tokenBlacklistService = mock(TokenBlacklistService.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(tokenProvider, userDetailsService, tokenBlacklistService, true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer refreshToken");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        when(tokenProvider.isValid("refreshToken")).thenReturn(true);
        when(tokenProvider.getType("refreshToken")).thenReturn("refresh");

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(userDetailsService, never()).loadUserByUsername(org.mockito.ArgumentMatchers.anyString());
        verify(chain).doFilter(request, response);
    }

    @Test
    void skipsWhenTokenRevoked() throws Exception {
        JwtTokenProvider tokenProvider = mock(JwtTokenProvider.class);
        CustomUserDetailsService userDetailsService = mock(CustomUserDetailsService.class);
        TokenBlacklistService tokenBlacklistService = mock(TokenBlacklistService.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(tokenProvider, userDetailsService, tokenBlacklistService, true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        when(tokenProvider.isValid("token")).thenReturn(true);
        when(tokenProvider.getType("token")).thenReturn("access");
        when(tokenProvider.getJti("token")).thenReturn("jti-1");
        when(tokenBlacklistService.isRevoked("jti-1")).thenReturn(true);

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(userDetailsService, never()).loadUserByUsername(org.mockito.ArgumentMatchers.anyString());
        verify(chain).doFilter(request, response);
    }

    @Test
    void authenticatesWhenAccessTokenValidAndNotRevoked() throws Exception {
        JwtTokenProvider tokenProvider = mock(JwtTokenProvider.class);
        CustomUserDetailsService userDetailsService = mock(CustomUserDetailsService.class);
        TokenBlacklistService tokenBlacklistService = mock(TokenBlacklistService.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(tokenProvider, userDetailsService, tokenBlacklistService, true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        when(tokenProvider.isValid("token")).thenReturn(true);
        when(tokenProvider.getType("token")).thenReturn("access");
        when(tokenProvider.getJti("token")).thenReturn("jti-1");
        when(tokenBlacklistService.isRevoked("jti-1")).thenReturn(false);
        when(tokenProvider.getEmail("token")).thenReturn("user@example.com");
        when(tokenProvider.getTokenVersion("token")).thenReturn(7);

        UserPrincipal principal = principal("user@example.com", true, 7);
        when(userDetailsService.loadUserByUsername("user@example.com")).thenReturn(principal);

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(principal);
        verify(chain).doFilter(request, response);
    }

    @Test
    void skipsAuthenticationWhenTokenVersionMismatch() throws Exception {
        JwtTokenProvider tokenProvider = mock(JwtTokenProvider.class);
        CustomUserDetailsService userDetailsService = mock(CustomUserDetailsService.class);
        TokenBlacklistService tokenBlacklistService = mock(TokenBlacklistService.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(tokenProvider, userDetailsService, tokenBlacklistService, true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        when(tokenProvider.isValid("token")).thenReturn(true);
        when(tokenProvider.getType("token")).thenReturn("access");
        when(tokenProvider.getJti("token")).thenReturn("jti-1");
        when(tokenBlacklistService.isRevoked("jti-1")).thenReturn(false);
        when(tokenProvider.getEmail("token")).thenReturn("user@example.com");
        when(tokenProvider.getTokenVersion("token")).thenReturn(2);

        UserPrincipal principal = principal("user@example.com", true, 1);
        when(userDetailsService.loadUserByUsername("user@example.com")).thenReturn(principal);

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(request, response);
    }

    @Test
    void skipsAuthenticationWhenRequireEmailVerifiedAndNotVerified() throws Exception {
        JwtTokenProvider tokenProvider = mock(JwtTokenProvider.class);
        CustomUserDetailsService userDetailsService = mock(CustomUserDetailsService.class);
        TokenBlacklistService tokenBlacklistService = mock(TokenBlacklistService.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(tokenProvider, userDetailsService, tokenBlacklistService, true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        when(tokenProvider.isValid("token")).thenReturn(true);
        when(tokenProvider.getType("token")).thenReturn("access");
        when(tokenProvider.getJti("token")).thenReturn("jti-1");
        when(tokenBlacklistService.isRevoked("jti-1")).thenReturn(false);
        when(tokenProvider.getEmail("token")).thenReturn("user@example.com");
        when(tokenProvider.getTokenVersion("token")).thenReturn(0);

        UserPrincipal principal = principal("user@example.com", false, 0);
        when(userDetailsService.loadUserByUsername("user@example.com")).thenReturn(principal);

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(request, response);
    }

    @Test
    void readsAccessTokenFromCookieWhenNoAuthorizationHeader() throws Exception {
        JwtTokenProvider tokenProvider = mock(JwtTokenProvider.class);
        CustomUserDetailsService userDetailsService = mock(CustomUserDetailsService.class);
        TokenBlacklistService tokenBlacklistService = mock(TokenBlacklistService.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(tokenProvider, userDetailsService, tokenBlacklistService, true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("FG_AUTH", "token"));
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        when(tokenProvider.isValid("token")).thenReturn(true);
        when(tokenProvider.getType("token")).thenReturn("access");
        when(tokenProvider.getJti("token")).thenReturn("jti-1");
        when(tokenBlacklistService.isRevoked("jti-1")).thenReturn(false);
        when(tokenProvider.getEmail("token")).thenReturn("user@example.com");
        when(tokenProvider.getTokenVersion("token")).thenReturn(0);

        UserPrincipal principal = principal("user@example.com", true, 0);
        when(userDetailsService.loadUserByUsername("user@example.com")).thenReturn(principal);

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        verify(chain).doFilter(request, response);
    }

    private UserPrincipal principal(String email, boolean emailVerified, int tokenVersion) {
        User user = new User();
        user.setId(1L);
        user.setEmail(email);
        user.setPasswordHash("hash");
        user.setRole(Role.USER);
        user.setEmailVerified(emailVerified);
        user.setTokenVersion(tokenVersion);
        return new UserPrincipal(user);
    }
}

