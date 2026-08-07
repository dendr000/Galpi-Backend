package com.note.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Entity
@Table(name = "TB_WIKI_PAGE")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class WikiPage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "page_id")
    private Long id;

    // 이 문서가 어떤 작품(세계관)에 속해 있는지 식별 (TB_WORK의 ID)
    @Column(name = "work_id", nullable = false)
    private Long workId;

    // 이 문서의 부모 문서 ID (최상위 문서일 경우 null)
    // 예: '등장인물(ID:1)'의 하위 문서인 '주인공(ID:2)'의 parentId는 1이 됨
    @Column(name = "parent_id")
    private Long parentId;

    // 문서 제목 (예: "설정", "등장인물", "흑야국")
    @Column(nullable = false)
    private String title;

    // 문서 본문 (마크다운 텍스트)
    @Column(columnDefinition = "TEXT")
    private String content;

    // 같은 계층 내에서의 정렬 순서
    @Column(name = "order_num", columnDefinition = "INT DEFAULT 0")
    private Integer orderNum = 0;

}