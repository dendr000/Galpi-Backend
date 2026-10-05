package com.note.config;

import com.note.util.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

// /api/** 전체가 세션 토큰으로 잠겨 있다는 것(그리고 로그인에 필요한 두 경로만 열려 있다는 것)을
// 고정합니다. 이 필터가 뚫리면 정답 조회/재설정 같은 민감한 API까지 같이 뚫립니다.
class AuthFilterTest {

    private static final String JWT_SECRET = "test-only-secret-test-only-secret-test-only-secret-abcdefghij";

    private JwtService jwt;
    private AuthFilter filter;

    @BeforeEach
    void setUp() {
        jwt = new JwtService(JWT_SECRET);
        filter = new AuthFilter();
        ReflectionTestUtils.setField(filter, "jwtService", jwt);
    }

    private static final class Result {
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockFilterChain chain = new MockFilterChain();

        boolean passedThrough() {
            return chain.getRequest() != null;
        }
    }

    private Result call(String method, String path, String authorization) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setRequestURI(path);
        if (authorization != null) request.addHeader("Authorization", authorization);
        Result result = new Result();
        filter.doFilter(request, result.response, result.chain);
        return result;
    }

    @Test
    @DisplayName("/api/가 아닌 경로(화면, 정적 파일)는 토큰 없이 통과한다")
    void nonApiPathsPass() throws Exception {
        for (String path : new String[]{"/", "/index.html", "/assets/index-abc.js", "/work/33", "/img/svg/ci.svg", "/favicon.svg"}) {
            assertThat(call("GET", path, null).passedThrough()).as(path).isTrue();
        }
    }

    @Test
    @DisplayName("로그인에 필요한 두 경로만 토큰 없이 열려 있다")
    void onlyLoginEndpointsArePublic() throws Exception {
        assertThat(call("POST", "/api/auth/login", null).passedThrough()).isTrue();
        assertThat(call("POST", "/api/auth/check-sequence", null).passedThrough()).isTrue();
    }

    @Test
    @DisplayName("정답 조회/재설정과 일반 API는 토큰이 없으면 401이고 컨트롤러까지 가지 못한다")
    void protectedEndpointsRequireToken() throws Exception {
        String[][] cases = {
                {"GET", "/api/auth/current-answers"},
                {"POST", "/api/auth/reroll"},
                {"GET", "/api/works"},
                {"DELETE", "/api/works/35"},
                {"GET", "/api/hidden-categories"},
                {"GET", "/api/characters"},
        };
        for (String[] c : cases) {
            Result r = call(c[0], c[1], null);
            assertThat(r.passedThrough()).as("%s %s", c[0], c[1]).isFalse();
            assertThat(r.response.getStatus()).as("%s %s", c[0], c[1]).isEqualTo(401);
        }
    }

    @Test
    @DisplayName("401 응답은 JSON 오류 본문을 준다")
    void unauthorizedResponseIsJson() throws Exception {
        Result r = call("GET", "/api/works", null);

        assertThat(r.response.getContentType()).startsWith("application/json");
        assertThat(r.response.getContentAsString()).contains("로그인이 필요합니다");
    }

    @Test
    @DisplayName("유효한 세션 토큰이면 통과한다")
    void validSessionTokenPasses() throws Exception {
        Result r = call("GET", "/api/works", "Bearer " + jwt.issueSessionToken());

        assertThat(r.passedThrough()).isTrue();
        assertThat(r.response.getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("reveal 티켓을 세션 토큰 자리에 넣어도 통과하지 못한다")
    void revealTicketCannotBeUsedAsSession() throws Exception {
        Result r = call("GET", "/api/works", "Bearer " + jwt.issueRevealTicket("library"));

        assertThat(r.passedThrough()).isFalse();
        assertThat(r.response.getStatus()).isEqualTo(401);
    }

    @Test
    @DisplayName("잘못된 토큰, Bearer가 아닌 헤더, 다른 시크릿으로 서명한 토큰은 401")
    void invalidCredentialsAreRejected() throws Exception {
        String forged = new JwtService("another-test-secret-another-test-secret-another-test-secret-99").issueSessionToken();
        String[] headers = {"Bearer not-a-token", "Bearer ", "Basic " + jwt.issueSessionToken(), jwt.issueSessionToken(), "Bearer " + forged};

        for (String header : headers) {
            Result r = call("GET", "/api/works", header);
            assertThat(r.passedThrough()).as("헤더 '%s'", header).isFalse();
            assertThat(r.response.getStatus()).isEqualTo(401);
        }
    }

    @Test
    @DisplayName("CORS 사전 요청(OPTIONS)은 토큰 없이 통과한다")
    void preflightPasses() throws Exception {
        assertThat(call("OPTIONS", "/api/works", null).passedThrough()).isTrue();
    }

    @Test
    @DisplayName("공개 경로로 시작하는 다른 경로(접미사 붙이기)는 공개로 취급하지 않는다")
    void publicPathMatchingIsExact() throws Exception {
        assertThat(call("POST", "/api/auth/login/extra", null).passedThrough()).isFalse();
        assertThat(call("POST", "/api/auth/login-bypass", null).passedThrough()).isFalse();
        assertThat(call("POST", "/api/auth/check-sequence/x", null).passedThrough()).isFalse();
    }
}
