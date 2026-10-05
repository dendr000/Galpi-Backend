package com.note.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.note.domain.AuthGate;
import com.note.repository.AuthGateRepository;
import com.note.util.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// 시크릿 게이트 로그인(시퀀스 검증 → 통과 문구)의 서버 측 규칙을 고정합니다. DB는 Mockito로
// 대체하므로 실제 MySQL/H2에 아무것도 쓰지 않습니다.
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthControllerTest {

    private static final String JWT_SECRET = "test-only-secret-test-only-secret-test-only-secret-abcdefghij";
    private static final String PASSPHRASE = "test-passphrase";

    // 프론트 시안의 ITEM_COUNT와 반드시 같아야 한다(LibraryScene 3x8, StarChartScene 6x10,
    // TerminalScene 로그 8줄, TraceScene 7). 프론트 쪽 계약 테스트가 같은 값을 검증한다.
    private static final Map<String, Integer> THEME_COUNTS = Map.of("library", 24, "starchart", 60, "terminal", 8, "trace", 7);

    @Mock
    private AuthGateRepository repo;

    private JwtService jwt;
    private AuthController controller;

    @BeforeEach
    void setUp() {
        jwt = new JwtService(JWT_SECRET);
        controller = new AuthController();
        ReflectionTestUtils.setField(controller, "authGateRepository", repo);
        ReflectionTestUtils.setField(controller, "jwtService", jwt);
        ReflectionTestUtils.setField(controller, "objectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(controller, "passphrase", PASSPHRASE);
    }

    private void givenAnswer(String theme, String answerJson) {
        when(repo.findById(theme)).thenReturn(Optional.of(new AuthGate(theme, answerJson, 1L)));
    }

    private static AuthController.SequenceRequest sequence(String theme, List<Integer> seq) {
        AuthController.SequenceRequest r = new AuthController.SequenceRequest();
        r.theme = theme;
        r.sequence = seq;
        return r;
    }

    private static AuthController.LoginRequest login(String ticket, String passphrase) {
        AuthController.LoginRequest r = new AuthController.LoginRequest();
        r.ticket = ticket;
        r.passphrase = passphrase;
        return r;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> body(ResponseEntity<?> response) {
        return (Map<String, Object>) response.getBody();
    }

    @Test
    @DisplayName("정답 시퀀스를 순서대로 보내면 ok=true와 해당 테마의 reveal 티켓을 준다")
    void correctSequenceIssuesTicket() {
        givenAnswer("library", "[3,7]");

        Map<String, Object> result = body(controller.checkSequence(sequence("library", List.of(3, 7))));

        assertThat(result.get("ok")).isEqualTo(true);
        String ticket = (String) result.get("ticket");
        assertThat(jwt.isValidRevealTicket(ticket, "library")).isTrue();
        assertThat(jwt.isValidRevealTicket(ticket, "terminal")).isFalse();
    }

    @Test
    @DisplayName("순서가 바뀌거나 개수가 다르면 실패하고 티켓을 주지 않는다")
    void wrongSequencesAreRejected() {
        givenAnswer("library", "[3,7]");

        for (List<Integer> wrong : List.of(List.of(7, 3), List.of(3), List.of(3, 7, 1), List.of(3, 8), List.<Integer>of())) {
            Map<String, Object> result = body(controller.checkSequence(sequence("library", wrong)));
            assertThat(result.get("ok")).as("시퀀스 %s", wrong).isEqualTo(false);
            assertThat(result).doesNotContainKey("ticket");
        }
    }

    @Test
    @DisplayName("sequence가 null이거나 알 수 없는 테마면 실패한다")
    void nullSequenceOrUnknownThemeFails() {
        givenAnswer("library", "[3,7]");
        when(repo.findById("bogus")).thenReturn(Optional.empty());

        assertThat(body(controller.checkSequence(sequence("library", null))).get("ok")).isEqualTo(false);
        assertThat(body(controller.checkSequence(sequence("bogus", List.of(3, 7)))).get("ok")).isEqualTo(false);
    }

    @Test
    @DisplayName("저장된 정답 JSON이 깨져 있어도 예외 없이 실패로 처리한다")
    void corruptedStoredAnswerFailsGracefully() {
        givenAnswer("library", "깨진-json");

        Map<String, Object> result = body(controller.checkSequence(sequence("library", List.of(3, 7))));

        assertThat(result.get("ok")).isEqualTo(false);
    }

    @Test
    @DisplayName("한 테마의 정답을 다른 테마 이름으로 보내도 통과하지 못한다")
    void answerOfOneThemeDoesNotOpenAnother() {
        givenAnswer("library", "[3,7]");
        givenAnswer("terminal", "[0,5]");

        assertThat(body(controller.checkSequence(sequence("terminal", List.of(3, 7)))).get("ok")).isEqualTo(false);
    }

    @Test
    @DisplayName("유효한 티켓 + 맞는 통과 문구면 세션 토큰을 준다")
    void loginSucceedsWithTicketAndPassphrase() {
        String ticket = jwt.issueRevealTicket("library");

        ResponseEntity<?> response = controller.login(login(ticket, PASSPHRASE));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(jwt.isValidSessionToken((String) body(response).get("token"))).isTrue();
    }

    @Test
    @DisplayName("통과 문구가 틀리면 401")
    void wrongPassphraseIsUnauthorized() {
        String ticket = jwt.issueRevealTicket("library");

        for (String wrong : new String[]{"", "wrong", PASSPHRASE.toUpperCase(), PASSPHRASE + " ", " " + PASSPHRASE, null}) {
            ResponseEntity<?> response = controller.login(login(ticket, wrong));
            assertThat(response.getStatusCode()).as("통과 문구 '%s'", wrong).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(body(response).get("error")).isEqualTo("통과 문구가 일치하지 않습니다.");
        }
    }

    @Test
    @DisplayName("시퀀스를 건너뛰고(티켓 없이/가짜 티켓) 통과 문구만으로는 로그인할 수 없다")
    void passphraseAloneCannotLogin() {
        ResponseEntity<?> noTicket = controller.login(login(null, PASSPHRASE));
        ResponseEntity<?> fakeTicket = controller.login(login("fake.ticket.value", PASSPHRASE));

        assertThat(noTicket.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(body(noTicket).get("error")).isEqualTo("시퀀스를 먼저 맞춰야 합니다.");
        assertThat(fakeTicket.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("세션 토큰이나 알 수 없는 테마의 티켓은 reveal 티켓으로 인정하지 않는다")
    void onlyRevealTicketsOfKnownThemesAreAccepted() {
        ResponseEntity<?> withSession = controller.login(login(jwt.issueSessionToken(), PASSPHRASE));
        ResponseEntity<?> withUnknownTheme = controller.login(login(jwt.issueRevealTicket("bogus"), PASSPHRASE));

        assertThat(withSession.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(withUnknownTheme.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("서버가 켜질 때 정답이 이미 있는 테마는 절대 덮어쓰지 않는다")
    void seedDefaultsNeverOverwritesExistingAnswers() {
        THEME_COUNTS.keySet().forEach(theme -> givenAnswer(theme, "[1,2]"));

        controller.seedDefaults();

        verify(repo, never()).save(any(AuthGate.class));
    }

    @Test
    @DisplayName("정답이 없는 테마만 무작위로 채우고, 값은 서로 다른 두 인덱스(범위 안)다")
    void seedDefaultsFillsOnlyMissingThemes() {
        THEME_COUNTS.keySet().forEach(theme -> when(repo.findById(theme)).thenReturn(Optional.empty()));
        givenAnswer("library", "[1,2]");

        controller.seedDefaults();

        ArgumentCaptor<AuthGate> saved = ArgumentCaptor.forClass(AuthGate.class);
        verify(repo, times(3)).save(saved.capture());
        assertThat(saved.getAllValues()).extracting(AuthGate::getThemeKey).containsExactlyInAnyOrder("starchart", "terminal", "trace");
        saved.getAllValues().forEach(gate -> assertValidAnswer(gate.getAnswerJson(), THEME_COUNTS.get(gate.getThemeKey())));
    }

    @Test
    @DisplayName("재설정은 알 수 없는 테마면 400")
    void rerollRejectsUnknownTheme() {
        AuthController.RerollRequest req = new AuthController.RerollRequest();
        req.theme = "bogus";

        assertThat(controller.reroll(req).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(repo, never()).save(any(AuthGate.class));
    }

    @Test
    @DisplayName("재설정은 항상 서로 다른 두 인덱스를 범위 안에서 뽑아 저장한다")
    void rerollProducesDistinctInRangeIndices() {
        for (Map.Entry<String, Integer> e : THEME_COUNTS.entrySet()) {
            when(repo.findById(e.getKey())).thenReturn(Optional.empty());
            AuthController.RerollRequest req = new AuthController.RerollRequest();
            req.theme = e.getKey();
            for (int i = 0; i < 300; i++) {
                ResponseEntity<?> response = controller.reroll(req);
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertValidAnswer((String) body(response).get("answer"), e.getValue());
            }
        }
    }

    @Test
    @DisplayName("현재 정답 조회는 네 테마를 모두 돌려주고 없는 것은 null로 표시한다")
    void currentAnswersListsAllThemes() {
        givenAnswer("library", "[3,7]");
        when(repo.findById("starchart")).thenReturn(Optional.empty());
        when(repo.findById("terminal")).thenReturn(Optional.empty());
        when(repo.findById("trace")).thenReturn(Optional.empty());

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rows = (List<Map<String, Object>>) controller.currentAnswers().getBody();

        assertThat(rows).hasSize(4);
        assertThat(rows).extracting(r -> r.get("theme")).containsExactlyInAnyOrder("library", "starchart", "terminal", "trace");
        Map<String, Object> library = rows.stream().filter(r -> "library".equals(r.get("theme"))).findFirst().orElseThrow();
        assertThat(library.get("answer")).isEqualTo("[3,7]");
        assertThat(library.get("itemCount")).isEqualTo(24);
        Map<String, Object> trace = rows.stream().filter(r -> "trace".equals(r.get("theme"))).findFirst().orElseThrow();
        assertThat(trace.get("answer")).isNull();
        assertThat(trace.get("itemCount")).isEqualTo(7);
    }

    private static void assertValidAnswer(String answerJson, int itemCount) {
        assertThat(answerJson).matches("\\[\\d+,\\d+]");
        String[] parts = answerJson.substring(1, answerJson.length() - 1).split(",");
        int a = Integer.parseInt(parts[0]);
        int b = Integer.parseInt(parts[1]);
        assertThat(a).isNotEqualTo(b);
        assertThat(a).isBetween(0, itemCount - 1);
        assertThat(b).isBetween(0, itemCount - 1);
    }
}
