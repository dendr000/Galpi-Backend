// 파일 위치: src/main/java/com/note/domain/MemoRelation.java

package com.note.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "galpi_memo_relation")
@Getter
@Setter
@ToString
public class MemoRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "source_id", nullable = false)
    private Long sourceId; // 선이 시작되는 메모의 ID

    @Column(name = "target_id", nullable = false)
    private Long targetId; // 선이 끝나는 메모의 ID

    @Column(name = "label", length = 100)
    private String label; // 관계 설명 (정방향 ➔)

    @Column(name = "type", length = 20)
    private String type = "->"; // 선 종류 (->, <->, --)

    @Column(name = "desc_rev", length = 100)
    private String descRev; // 관계 설명 (역방향 ⬅, 양방향일 경우 사용)
}