// 파일 위치: src/main/java/com/note/repository/MemoRelationRepository.java

package com.note.repository;

import com.note.domain.MemoRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemoRelationRepository extends JpaRepository<MemoRelation, Long> {
    
    // 특정 메모가 연관된 모든 선을 불러오기 위한 메서드
    List<MemoRelation> findBySourceIdOrTargetId(Long sourceId, Long targetId);
    
    // 메모가 삭제될 때, 해당 메모에 연결되어 있던 모든 선을 일괄 철거하기 위한 메서드
    void deleteBySourceIdOrTargetId(Long sourceId, Long targetId);
}