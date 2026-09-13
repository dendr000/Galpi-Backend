// 파일 위치: src/main/java/com/note/util/JwtService.java
package com.note.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

// 시크릿 게이트 토큰 두 종류를 발급/검증합니다.
// - "reveal" 티켓: 클릭 시퀀스가 맞았을 때 잠깐(2분) 주는 통행증. 이 티켓 없이는 로그인 API가
//   패스프레이즈를 아예 받아주지 않아서, 시퀀스를 몰라도 패스프레이즈만으로 뚫는 걸 막는다.
// - "session" 토큰: 로그인 성공 후 발급하는 진짜 세션(90일). 이후 모든 API 요청에 이 토큰을
//   Authorization 헤더로 붙여서 보낸다(프론트 axios 인터셉터가 자동으로 붙여줌).
@Slf4j
@Component
public class JwtService {

    private final SecretKey key;
    private static final long REVEAL_TICKET_TTL_MS = 2 * 60 * 1000L;
    private static final long SESSION_TTL_MS = 90L * 24 * 60 * 60 * 1000L;

    public JwtService(@Value("${galpi.auth.jwt-secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String issueRevealTicket(String themeKey) {
        Date now = new Date();
        return Jwts.builder()
                .claim("type", "reveal")
                .claim("theme", themeKey)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + REVEAL_TICKET_TTL_MS))
                .signWith(key)
                .compact();
    }

    public String issueSessionToken() {
        Date now = new Date();
        return Jwts.builder()
                .claim("type", "session")
                .issuedAt(now)
                .expiration(new Date(now.getTime() + SESSION_TTL_MS))
                .signWith(key)
                .compact();
    }

    // themeKey가 일치하는 유효한 reveal 티켓이면 true
    public boolean isValidRevealTicket(String token, String themeKey) {
        Claims claims = parseOrNull(token);
        if (claims == null) return false;
        return "reveal".equals(claims.get("type")) && themeKey.equals(claims.get("theme"));
    }

    public boolean isValidSessionToken(String token) {
        Claims claims = parseOrNull(token);
        if (claims == null) return false;
        return "session".equals(claims.get("type"));
    }

    private Claims parseOrNull(String token) {
        try {
            return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("[JwtService] 토큰 검증 실패: {}", e.getMessage());
            return null;
        }
    }
}
