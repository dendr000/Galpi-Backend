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

// 1. 데이터베이스의 TB_CHARACTER 테이블과 연결되는 엔티티입니다.
@Entity
@Table(name = "TB_CHARACTER")
@Data
@AllArgsConstructor
@Slf4j
public class CharacterEntity {
    
    // 2. 캐릭터 고유 식별자 (PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "char_id")
    private Long id;
    
    // 3. 소속된 작품의 식별자 (FK 역할)
    @Column(name = "work_id", nullable = false)
    private Long workId;
    
    @Column(nullable = false)
    private String name;
    
    private String age;
    private String birthday;
    private String gender;
    private String species;
    
    @Column(name = "image_code")
    private String imageCode;
    
    // 4. ★ 핵심: JSON 형태의 동적 속성을 문자열(String)로 받아서 DB의 JSON 타입에 매핑합니다.
    @Column(name = "dynamic_properties", columnDefinition = "JSON")
    private String dynamicProperties;
    
    public CharacterEntity() {
        log.info("[CharacterEntity] 캐릭터 엔티티 객체가 메모리에 로드되었습니다.");
    }
}