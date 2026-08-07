package com.note.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "TB_DICT")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dict {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "work_id", nullable = false)
    private String workId; // 전역 사전을 위한 "global" 또는 특정 작품 ID 저장

    @Column(nullable = false)
    private String word; // 원문

    @Column(nullable = false)
    private String translation; // 한자/영문 번역본
}