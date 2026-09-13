// 파일 위치: src/main/java/com/note/controller/AuthController.java
package com.note.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.note.domain.AuthGate;
import com.note.repository.AuthGateRepository;
import com.note.util.JwtService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// 시크릿 게이트(로그인 페이지)의 클릭 시퀀스 검증과 최종 로그인을 담당합니다.
// 정답 인덱스는 절대 응답으로 내려가지 않습니다 — /current-answers도 이미 로그인된 사람만
// 볼 수 있는, "내가 정한 정답이 뭐였는지 까먹었을 때 확인하는 용도"의 엔드포인트입니다.
@Slf4j
@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    // 프론트 시안 4종과 1:1로 맞춘 테마별 항목 개수 (그림 배치가 바뀌면 여기도 같이 바꿔야 함)
    private static final Map<String, Integer> THEME_ITEM_COUNT = Map.of(
            "library", 24,
            "starchart", 60,
            "terminal", 8,
            "trace", 7
    );
    private static final Map<String, String> THEME_LABEL = Map.of(
            "library", "서고의 등불",
            "starchart", "성좌표",
            "terminal", "터미널 침입",
            "trace", "흔적"
    );

    @Autowired
    private AuthGateRepository authGateRepository;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private ObjectMapper objectMapper;

    @Value("${galpi.auth.passphrase}")
    private String passphrase;

    private final SecureRandom random = new SecureRandom();

    // 서버가 처음 켜질 때, 테마별 정답이 아직 없으면 무작위로 하나씩 만들어둔다
    @PostConstruct
    public void seedDefaults() {
        THEME_ITEM_COUNT.forEach((theme, count) -> {
            if (authGateRepository.findById(theme).isEmpty()) {
                authGateRepository.save(new AuthGate(theme, randomAnswerJson(count), System.currentTimeMillis()));
                log.info("[AuthController] 게이트 정답 초기 생성: {}", theme);
            }
        });
    }

    public static class SequenceRequest {
        public String theme;
        public List<Integer> sequence;
    }

    @PostMapping("/check-sequence")
    public ResponseEntity<?> checkSequence(@RequestBody SequenceRequest req) {
        AuthGate gate = authGateRepository.findById(req.theme).orElse(null);
        if (gate == null || req.sequence == null) {
            return ResponseEntity.ok(Map.of("ok", false));
        }
        try {
            List<Integer> answer = objectMapper.readValue(gate.getAnswerJson(), List.class);
            if (answer.equals(req.sequence)) {
                String ticket = jwtService.issueRevealTicket(req.theme);
                return ResponseEntity.ok(Map.of("ok", true, "ticket", ticket));
            }
        } catch (Exception e) {
            log.error("[AuthController] 정답 JSON 파싱 실패: {}", e.getMessage());
        }
        return ResponseEntity.ok(Map.of("ok", false));
    }

    public static class LoginRequest {
        public String ticket;
        public String passphrase;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        String theme = extractThemeIfValidTicket(req.ticket);
        if (theme == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "시퀀스를 먼저 맞춰야 합니다."));
        }
        if (req.passphrase == null || !constantTimeEquals(req.passphrase, passphrase)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "통과 문구가 일치하지 않습니다."));
        }
        String sessionToken = jwtService.issueSessionToken();
        return ResponseEntity.ok(Map.of("token", sessionToken));
    }

    // ticket이 어떤 테마에 대해서든 유효한 reveal 티켓이면 그 테마 이름을, 아니면 null을 반환
    private String extractThemeIfValidTicket(String ticket) {
        if (ticket == null) return null;
        for (String theme : THEME_ITEM_COUNT.keySet()) {
            if (jwtService.isValidRevealTicket(ticket, theme)) return theme;
        }
        return null;
    }

    // ★ 이 아래 두 엔드포인트는 AuthFilter가 이미 세션 토큰을 검사한 뒤에만 통과시킨다
    // (SecurityConfig.PUBLIC_PATHS에 없음 — 로그인된 사람만 자기 정답을 확인/재설정 가능)
    @GetMapping("/current-answers")
    public ResponseEntity<?> currentAnswers() {
        List<Map<String, Object>> result = THEME_ITEM_COUNT.keySet().stream().map(theme -> {
            AuthGate gate = authGateRepository.findById(theme).orElse(null);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("theme", theme);
            row.put("label", THEME_LABEL.get(theme));
            row.put("answer", gate != null ? gate.getAnswerJson() : null);
            row.put("itemCount", THEME_ITEM_COUNT.get(theme));
            return row;
        }).toList();
        return ResponseEntity.ok(result);
    }

    public static class RerollRequest {
        public String theme;
    }

    @PostMapping("/reroll")
    public ResponseEntity<?> reroll(@RequestBody RerollRequest req) {
        Integer count = THEME_ITEM_COUNT.get(req.theme);
        if (count == null) return ResponseEntity.badRequest().body(Map.of("error", "알 수 없는 테마입니다."));
        AuthGate gate = authGateRepository.findById(req.theme).orElse(new AuthGate(req.theme, null, null));
        gate.setAnswerJson(randomAnswerJson(count));
        gate.setUpdatedAt(System.currentTimeMillis());
        authGateRepository.save(gate);
        log.info("[AuthController] 게이트 정답 재설정: {}", req.theme);
        return ResponseEntity.ok(Map.of("theme", req.theme, "answer", gate.getAnswerJson()));
    }

    // 0..count-1 범위에서 서로 다른 두 인덱스를 순서 있게 뽑는다
    private String randomAnswerJson(int count) {
        int a = random.nextInt(count);
        int b;
        do { b = random.nextInt(count); } while (b == a);
        return "[" + a + "," + b + "]";
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }
}
