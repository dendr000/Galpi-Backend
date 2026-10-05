package com.note.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

// 시크릿 게이트 토큰(reveal 티켓 / session 토큰)의 발급·검증 규칙을 고정합니다.
// 실제 비밀값은 쓰지 않고, 테스트 전용 가짜 시크릿으로 JwtService를 직접 만듭니다.
class JwtServiceTest {

    private static final String SECRET = "test-only-secret-test-only-secret-test-only-secret-abcdefghij";
    private static final String OTHER_SECRET = "another-test-secret-another-test-secret-another-test-secret-99";

    private final JwtService jwt = new JwtService(SECRET);

    private static SecretKey key(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private static Claims parse(String token, String secret) {
        return Jwts.parser().verifyWith(key(secret)).build().parseSignedClaims(token).getPayload();
    }

    @Test
    @DisplayName("세션 토큰은 세션으로만 유효하고 reveal 티켓으로는 쓸 수 없다")
    void sessionTokenIsOnlyValidAsSession() {
        String session = jwt.issueSessionToken();

        assertThat(jwt.isValidSessionToken(session)).isTrue();
        assertThat(jwt.isValidRevealTicket(session, "library")).isFalse();
    }

    @Test
    @DisplayName("reveal 티켓은 발급받은 테마에서만 유효하다")
    void revealTicketIsBoundToItsTheme() {
        String ticket = jwt.issueRevealTicket("library");

        assertThat(jwt.isValidRevealTicket(ticket, "library")).isTrue();
        assertThat(jwt.isValidRevealTicket(ticket, "terminal")).isFalse();
        assertThat(jwt.isValidRevealTicket(ticket, "starchart")).isFalse();
        assertThat(jwt.isValidRevealTicket(ticket, "trace")).isFalse();
    }

    @Test
    @DisplayName("reveal 티켓으로는 세션 API(Authorization)를 통과할 수 없다")
    void revealTicketIsNotASession() {
        String ticket = jwt.issueRevealTicket("library");

        assertThat(jwt.isValidSessionToken(ticket)).isFalse();
    }

    @Test
    @DisplayName("null, 빈 문자열, 쓰레기 문자열은 예외 없이 무효 처리한다")
    void garbageTokensAreRejectedWithoutThrowing() {
        assertThat(jwt.isValidSessionToken(null)).isFalse();
        assertThat(jwt.isValidSessionToken("")).isFalse();
        assertThat(jwt.isValidSessionToken("not-a-jwt")).isFalse();
        assertThat(jwt.isValidSessionToken("a.b.c")).isFalse();
        assertThat(jwt.isValidRevealTicket(null, "library")).isFalse();
        assertThat(jwt.isValidRevealTicket("not-a-jwt", "library")).isFalse();
    }

    @Test
    @DisplayName("다른 시크릿으로 서명한 토큰은 거부한다")
    void tokenSignedWithAnotherSecretIsRejected() {
        String forged = new JwtService(OTHER_SECRET).issueSessionToken();

        assertThat(jwt.isValidSessionToken(forged)).isFalse();
    }

    @Test
    @DisplayName("페이로드를 바꾼 토큰(서명 불일치)은 거부한다")
    void tamperedPayloadIsRejected() {
        String reveal = jwt.issueRevealTicket("library");
        String[] parts = reveal.split("\\.");
        // reveal 티켓의 페이로드를 session 토큰의 페이로드로 바꿔치기해서 권한 상승을 시도한다.
        String sessionPayload = jwt.issueSessionToken().split("\\.")[1];
        String forged = parts[0] + "." + sessionPayload + "." + parts[2];

        assertThat(jwt.isValidSessionToken(forged)).isFalse();
    }

    @Test
    @DisplayName("만료된 토큰은 거부한다")
    void expiredTokenIsRejected() {
        Date past = new Date(System.currentTimeMillis() - 60_000L);
        String expired = Jwts.builder()
                .claim("type", "session")
                .issuedAt(new Date(past.getTime() - 1_000L))
                .expiration(past)
                .signWith(key(SECRET))
                .compact();

        assertThat(jwt.isValidSessionToken(expired)).isFalse();
    }

    @Test
    @DisplayName("서명 없는(alg=none) 토큰은 거부한다")
    void unsignedTokenIsRejected() {
        String unsigned = Jwts.builder()
                .claim("type", "session")
                .expiration(new Date(System.currentTimeMillis() + 60_000L))
                .compact();

        assertThat(jwt.isValidSessionToken(unsigned)).isFalse();
    }

    @Test
    @DisplayName("type 클레임이 없는 토큰은 세션으로 인정하지 않는다")
    void tokenWithoutTypeIsNotASession() {
        String noType = Jwts.builder()
                .expiration(new Date(System.currentTimeMillis() + 60_000L))
                .signWith(key(SECRET))
                .compact();

        assertThat(jwt.isValidSessionToken(noType)).isFalse();
        assertThat(jwt.isValidRevealTicket(noType, "library")).isFalse();
    }

    @Test
    @DisplayName("같은 시크릿이면 서버를 다시 켠 뒤(새 인스턴스)에도 기존 세션이 유효하다")
    void sessionSurvivesServerRestartWithSameSecret() {
        String session = jwt.issueSessionToken();

        assertThat(new JwtService(SECRET).isValidSessionToken(session)).isTrue();
    }

    @Test
    @DisplayName("만료 시간: reveal 티켓은 2분, 세션은 90일")
    void expiryWindows() {
        Claims reveal = parse(jwt.issueRevealTicket("library"), SECRET);
        Claims session = parse(jwt.issueSessionToken(), SECRET);

        assertThat(reveal.getExpiration().getTime() - reveal.getIssuedAt().getTime()).isEqualTo(2 * 60 * 1000L);
        assertThat(session.getExpiration().getTime() - session.getIssuedAt().getTime()).isEqualTo(90L * 24 * 60 * 60 * 1000L);
    }

    @Test
    @DisplayName("reveal 티켓에는 테마가 담긴다")
    void revealTicketCarriesTheme() {
        Claims claims = parse(jwt.issueRevealTicket("starchart"), SECRET);

        assertThat(claims.get("type")).isEqualTo("reveal");
        assertThat(claims.get("theme")).isEqualTo("starchart");
    }
}
