package io.github.tony8864;

import io.github.tony8864.security.UserClaims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class JwtTokenServiceTest {
    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        String testSecret = "test-secret-key-12345678901234567890";
        jwtTokenService = new JwtTokenService(testSecret);
    }

    @Test
    void generateAndVerifyToken_ShouldReturnSameClaims() {
        // given
        UserClaims claims = new UserClaims("123", "test@example.com", Set.of("ADMIN", "USER"));

        // when
        String token = jwtTokenService.generate(claims, Duration.ofMinutes(5));
        UserClaims verified = jwtTokenService.verifyToken(token);

        // then
        assertThat(verified.userId()).isEqualTo("123");
        assertThat(verified.email()).isEqualTo("test@example.com");
        assertThat(verified.roles()).containsExactlyInAnyOrder("USER", "ADMIN");
    }

    @Test
    void expiredToken_ShouldThrowInvalidTokenException() {
        // given
        UserClaims claims = new UserClaims("123", "expired@example.com", Set.of("USER"));
        String token = jwtTokenService.generate(claims, Duration.ofSeconds(-5)); // already expired

        // when / then
        assertThatThrownBy(() -> jwtTokenService.verifyToken(token))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Invalid or expired JWT");
    }

    @Test
    void malformedToken_ShouldThrowInvalidTokenException() {
        // given
        String badToken = "this-is-not-a-valid-jwt";

        // when / then
        assertThatThrownBy(() -> jwtTokenService.verifyToken(badToken))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Invalid or expired JWT");
    }

    @Test
    void generate_ShouldIncludeAllClaims() {
        // given
        UserClaims claims = new UserClaims("abc", "claims@example.com", Set.of("ADMIN"));
        String token = jwtTokenService.generate(claims, Duration.ofMinutes(10));

        // when
        UserClaims verified = jwtTokenService.verifyToken(token);

        // then
        assertThat(verified.userId()).isEqualTo("abc");
        assertThat(verified.email()).isEqualTo("claims@example.com");
        assertThat(verified.roles()).containsExactly("ADMIN");
    }
}