// 파일 위치: src/main/java/com/note/repository/DictRepository.java
package com.note.repository;

import com.note.domain.Dict;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DictRepository extends JpaRepository<Dict, Long> {
    List<Dict> findByWorkId(String workId);
    
    // 단어만 검색 (다중 한자 허용을 위해 단건->List 반환으로 수정)
    List<Dict> findByWorkIdAndWord(String workId, String word);
    
    // 단어 + 한자 쌍으로 정확히 핀포인트 검색
    Dict findByWorkIdAndWordAndTranslation(String workId, String word, String translation);
    
    // 단어 전체 삭제
    void deleteByWorkIdAndWord(String workId, String word);
    
    // 특정 단어+한자 쌍만 핀포인트 삭제
    void deleteByWorkIdAndWordAndTranslation(String workId, String word, String translation);
}