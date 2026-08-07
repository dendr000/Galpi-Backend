package com.note.repository;

import com.note.domain.WikiPage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WikiPageRepository extends JpaRepository<WikiPage, Long> {
    
    // 특정 작품의 '최상위 문서' 목록만 가져오기 (부모가 없는 문서들)
    List<WikiPage> findByWorkIdAndParentIdIsNullOrderByOrderNumAsc(Long workId);
    
    // 특정 부모 문서에 속한 '하위 문서' 목록 가져오기
    List<WikiPage> findByParentIdOrderByOrderNumAsc(Long parentId);
    
    // 특정 작품에 속한 모든 문서 가져오기
    List<WikiPage> findByWorkId(Long workId);
}