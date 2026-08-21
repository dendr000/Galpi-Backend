// 파일 위치: src/main/java/com/note/domain/HiddenCategory.java
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

// 메인 화면 "분류" 목록에서 사용자가 숨김 처리한 카테고리 이름을 저장하는 엔티티입니다.
@Entity
@Table(name = "TB_HIDDEN_CATEGORY", uniqueConstraints = @UniqueConstraint(columnNames = "category_name"))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HiddenCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;
}
