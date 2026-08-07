// 파일 위치: src/main/java/com/note/domain/Memo.java

package com.note.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "galpi_memo")
@Getter
@Setter
@ToString
public class Memo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "content", columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "folder", length = 100)
    private String folder = "기타";

    @Column(name = "sort_order")
    private Integer sortOrder = -1;

    @Column(name = "canvas_x")
    private Integer canvasX = 2500;

    @Column(name = "canvas_y")
    private Integer canvasY = 2500;

    @Column(name = "theme_color", length = 50)
    private String themeColor = "var(--surface-color)";

    @Column(name = "updated_at")
    private Long updatedAt;
    
    @Column(name = "is_trash", columnDefinition = "boolean default false")
    private Boolean isTrash = false;

    @Column(name = "is_locked", columnDefinition = "boolean default false")
    private Boolean isLocked = false;

    @Column(name = "tags", length = 500)
    private String tags; // 추출된 해시태그를 콤마(,)로 구분하여 저장
}