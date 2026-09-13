// 파일 위치: src/main/java/com/note/domain/AuthGate.java
package com.note.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 시크릿 게이트(로그인 페이지)의 테마별 정답 좌표를 저장합니다. themeKey당 한 행만 존재하며,
// answerJson은 "[4,18]"처럼 해당 테마 안에서 순서대로 더블클릭해야 할 항목의 인덱스 배열입니다.
// 정답 인덱스는 여기(DB)에만 있고 프론트엔드로는 절대 내려가지 않습니다 — 클릭 시퀀스를
// 서버로 보내서 여기와 대조하는 방식(AuthController.checkSequence)이라 개발자도구로도 못 봅니다.
@Entity
@Table(name = "TB_AUTH_GATE")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthGate {

    @Id
    @Column(name = "theme_key", length = 30)
    private String themeKey;

    @Column(name = "answer_json", nullable = false, length = 100)
    private String answerJson;

    @Column(name = "updated_at")
    private Long updatedAt;
}
