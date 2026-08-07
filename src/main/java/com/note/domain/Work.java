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

// 1. 데이터베이스의 TB_WORK 테이블과 연결되는 엔티티입니다.
@Entity
@Table(name = "TB_WORK")
@Data
@AllArgsConstructor
@Slf4j
public class Work {
    
    // 2. 고유 식별자 (PK) 및 자동 증가 설정
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "work_id")
    private Long id;
    
    // 3. 필수 입력값인 작품명
    @Column(nullable = false)
    private String title;
    
    @Column(name = "check_date")
    private String checkDate;
    
    private String creator;
    private String genre;
    private String rating;
    private String url;
    private String status;
    
    // 4. 내용이 길 수 있는 소개글은 TEXT 타입으로 지정
    @Column(columnDefinition = "TEXT")
    private String description;
    
    public Work() {
        log.info("[Work Entity] 작품 엔티티 객체가 메모리에 로드되었습니다.");
    }
}