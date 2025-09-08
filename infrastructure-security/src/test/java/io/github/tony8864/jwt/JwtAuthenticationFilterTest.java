package io.github.tony8864.jwt;

import io.github.tony8864.security.UserClaims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {
    private JwtTokenService tokenService;
    private JwtAuthenticationFilter filter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        tokenService = mock(JwtTokenService.class);
        filter = new JwtAuthenticationFilter(tokenService);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        chain = mock(FilterChain.class);
    }

    @Test
    void shouldAllowPublicEndpoints() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/register");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verifyNoInteractions(tokenService);
    }

    @Test
    void shouldRejectWhenNoAuthorizationHeader() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/group-chats");
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilter(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verifyNoInteractions(tokenService);
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void shouldRejectWhenHeaderNotBearer() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/group-chats");
        when(request.getHeader("Authorization")).thenReturn("Basic xyz");

        filter.doFilter(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verifyNoInteractions(tokenService);
    }

    @Test
    void shouldRejectWhenTokenInvalid() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/group-chats");
        when(request.getHeader("Authorization")).thenReturn("Bearer badtoken");
        when(tokenService.verifyToken("badtoken")).thenThrow(new RuntimeException("Invalid"));

        filter.doFilter(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void shouldAllowWhenTokenValid() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/group-chats");
        when(request.getHeader("Authorization")).thenReturn("Bearer goodtoken");

        var claims = new UserClaims("123", "test@example.com", Set.of("USER"));
        when(tokenService.verifyToken("goodtoken")).thenReturn(claims);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(request).setAttribute(eq("authenticatedUser"), any());
    }
}