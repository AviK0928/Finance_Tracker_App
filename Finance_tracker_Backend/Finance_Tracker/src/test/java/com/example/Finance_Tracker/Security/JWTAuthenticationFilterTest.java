package com.example.Finance_Tracker.Security;

import com.example.Finance_Tracker.User.mapper.CustomUserDetails;
import com.example.Finance_Tracker.User.repository.BlacklistedTokenRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JWTAuthenticationFilterTest {

    private static final String EMAIL = "user@example.com";

    @Mock private UserDetailsService userDetailsService;
    @Mock private BlacklistedTokenRepository blacklistedTokenRepository;

    private JWTService jwtService;
    private JWTAuthenticationFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain chain;

    @BeforeEach
    void setUp() {
        // Real JWTService with a test key, so token parsing is exercised for real
        jwtService = new JWTService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret",
                Base64.getEncoder().encodeToString("test-secret-key-of-at-least-32-bytes!!".getBytes()));
        jwtService.init();

        filter = new JWTAuthenticationFilter(jwtService, userDetailsService, blacklistedTokenRepository);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        chain = new MockFilterChain();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Authentication currentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    @Test
    void demoToken_isNotAccepted() throws Exception {
        request.addHeader("Authorization", "Bearer demo-token");

        filter.doFilter(request, response, chain);

        assertThat(currentAuthentication()).isNull();
        assertThat(chain.getRequest()).as("request continues unauthenticated").isNotNull();
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void noAuthorizationHeader_continuesUnauthenticated() throws Exception {
        filter.doFilter(request, response, chain);

        assertThat(currentAuthentication()).isNull();
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void validToken_authenticatesUser() throws Exception {
        String token = jwtService.generateToken(EMAIL);
        CustomUserDetails user = new CustomUserDetails(1L, EMAIL, "hash",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(userDetailsService.loadUserByUsername(EMAIL)).thenReturn(user);
        request.addHeader("Authorization", "Bearer " + token);

        filter.doFilter(request, response, chain);

        assertThat(currentAuthentication()).isNotNull();
        assertThat(currentAuthentication().getPrincipal()).isSameAs(user);
    }

    @Test
    void blacklistedToken_isRejectedWith401() throws Exception {
        String token = jwtService.generateToken(EMAIL);
        when(blacklistedTokenRepository.existsByToken(token)).thenReturn(true);
        request.addHeader("Authorization", "Bearer " + token);

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(chain.getRequest()).as("chain must not continue").isNull();
        assertThat(currentAuthentication()).isNull();
    }
}
