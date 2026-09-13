package com.note.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// ★ 사전은 작품별로 나뉘지 않는 완전 전역이다 — 예전엔 work_id로 작품마다 분리해뒀는데(Alt+H
// 실시간 치환용으로 사전 전체(19만 건+)를 브라우저에 통째로 불러오다 크래시 나던 걸 우회하려는
// 목적이었음), 그 결과 같은 단어를 검색해도 등록했던 작품이 아니면 안 보이는 혼란을 낳았다.
// Alt+H는 이제 IndexedDB 로컬 캐시(src/utils/dictLocalDb.js)로 전체를 한 번만 적재해두고
// 인덱스 조회로 처리하므로, 서버 쪽 사전은 작품 구분 없이 (word, translation) 조합만으로
// 유니크하면 된다 — 같은 한글이라도 한자가 다르면 별개 항목으로 공존한다.
@Entity
@Table(name = "TB_DICT", uniqueConstraints = @UniqueConstraint(
        name = "uq_dict_word_translation",
        columnNames = {"word", "translation"}
))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dict {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String word; // 원문

    @Column(nullable = false)
    private String translation; // 한자/영문 번역본
}
