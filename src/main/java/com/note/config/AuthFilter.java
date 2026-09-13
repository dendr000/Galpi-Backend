// 파일 위치: src/main/java/com/note/config/AuthFilter.java
package com.note.config;

import com.note.util.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

// 스프링 시큐리티 없이 최소한으로 — /api/** 요청은 전부 세션 토큰(Authorization: Bearer ...)이
// 있어야 통과한다. 로그인 자체에 필요한 두 엔드포인트와 이미지/폰트 같은 정적 리소스(애초에
// /api/가 아님)만 예외. 조회까지 전부 잠그는 건 "서버 켜면 로그인 화면부터"라는 요구사항에
// 맞춘 의도적 선택이다 — 이 앱은 원래 완전히 열려 있던 API라 여기 아니면 막을 곳이 없다.
@Component
public class AuthFilter extends HttpFilter {

    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/api/auth/login",
            "/api/auth/check-sequence"
    );

    @Autowired
    private JwtService jwtService;

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        String path = req.getRequestURI();

        if (!path.startsWith("/api/") || PUBLIC_PATHS.contains(path) || "OPTIONS".equalsIgnoreCase(req.getMethod())) {
            chain.doFilter(req, res);
            return;
        }

        String header = req.getHeader("Authorization");
        String token = (header != null && header.startsWith("Bearer ")) ? header.substring(7) : null;

        if (token == null || !jwtService.isValidSessionToken(token)) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"error\":\"로그인이 필요합니다.\"}");
            return;
        }

        chain.doFilter(req, res);
    }
}
