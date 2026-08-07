package com.note.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

// 1. 데이터베이스의 TB_TAG_MASTER 테이블과 1:1 매핑되는 자바 엔티티 객체입니다.
@Entity
@Table(name = "TB_TAG_MASTER")
@Data
@AllArgsConstructor
@Slf4j
public class Tag {

    // 2. 태그 고유 일련번호 기본키 설정
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long id;

    // 3. 태그의 대분류 유형 (예: GENRE, MBTI, CUPSIZE 등)
    @Column(name = "tag_type", nullable = false, length = 50)
    private String tagType;

    // 4. 실제 태그에 저장될 명칭 (예: 대학, 로맨스, ESFP, I컵 등)
    @Column(name = "tag_name", nullable = false, length = 100)
    private String tagName;

    // 5. JPA 구동을 위한 수동 기본 생성자 및 로깅 시스템 정의
    public Tag() {
        log.info("[Tag 엔티티] 데이터베이스 태그 마스터 객체가 메모리에 할당되었습니다.");
    }
}