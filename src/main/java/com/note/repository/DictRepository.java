// 파일 위치: src/main/java/com/note/repository/DictRepository.java
package com.note.repository;

import com.note.domain.Dict;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface DictRepository extends JpaRepository<Dict, Long> {
    List<Dict> findByWorkId(String workId);

    // 단어만 검색 (다중 한자 허용을 위해 단건->List 반환으로 수정)
    List<Dict> findByWorkIdAndWord(String workId, String word);

    // ★ 서버사이드 검색: workId(전역+현재 작품)로 범위를 좁힌 뒤 원문/치환어에 키워드가 포함된 항목만,
    // word 가나다순 정렬로 반환. Pageable로 결과 건수를 제한해 전체 테이블 로딩(OOM)을 원천 차단.
    @Query("SELECT d FROM Dict d " +
           "WHERE (d.workId = 'global' OR d.workId = :workId) " +
           "AND (d.word LIKE CONCAT('%', :keyword, '%') OR d.translation LIKE CONCAT('%', :keyword, '%')) " +
           "ORDER BY d.word ASC")
    List<Dict> searchByKeyword(@Param("workId") String workId, @Param("keyword") String keyword, Pageable pageable);
    
    // 단어 + 한자 쌍으로 정확히 핀포인트 검색
    Dict findByWorkIdAndWordAndTranslation(String workId, String word, String translation);
    
    // 단어 전체 삭제
    void deleteByWorkIdAndWord(String workId, String word);
    
    // 특정 단어+한자 쌍만 핀포인트 삭제
    void deleteByWorkIdAndWordAndTranslation(String workId, String word, String translation);
}