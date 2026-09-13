// 파일 위치: src/main/java/com/note/repository/DictRepository.java
package com.note.repository;

import com.note.domain.Dict;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface DictRepository extends JpaRepository<Dict, Long> {

    // ★ 서버사이드 검색: 작품 구분 없이 전체 사전에서 원문/치환어에 키워드가 포함된 항목만 반환.
    // Pageable로 결과 건수를 제한해 전체 테이블 로딩(OOM)을 원천 차단하는데, 예전엔 그냥
    // 가나다순으로만 정렬해서 잘랐다 — "지체"처럼 짧고 정확히 일치하는 단어가 있어도, "~之體"류
    // 복합어(강철지체, 검령지체, ...)가 알파벳상 앞쪽에 잔뜩 몰려 있으면 그 뒤로 밀려서 상한선
    // 밖으로 잘려나가, 분명히 사전에 있는 단어인데 검색해도 안 나오는 버그가 있었다. 정확히
    // 일치하는 항목(원문 우선, 그다음 번역)과 접두 일치 항목을 항상 맨 앞으로 끌어올리고, 그다음
    // 짧은 단어부터 보여주도록 정렬 우선순위를 바꿔서 — 캡에 걸리더라도 가장 관련성 높은 항목은
    // 항상 살아남게 한다.
    @Query("SELECT d FROM Dict d " +
           "WHERE (d.word LIKE CONCAT('%', :keyword, '%') OR d.translation LIKE CONCAT('%', :keyword, '%')) " +
           "ORDER BY " +
           "CASE WHEN d.word = :keyword THEN 0 " +
           "WHEN d.translation = :keyword THEN 1 " +
           "WHEN d.word LIKE CONCAT(:keyword, '%') THEN 2 " +
           "ELSE 3 END, " +
           "LENGTH(d.word) ASC, d.word ASC")
    List<Dict> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // 단어 + 한자 쌍으로 정확히 핀포인트 검색 (같은 한글이라도 한자가 다르면 별개 항목)
    Dict findByWordAndTranslation(String word, String translation);

    // 단어 전체 삭제
    void deleteByWord(String word);

    // 특정 단어+한자 쌍만 핀포인트 삭제
    void deleteByWordAndTranslation(String word, String translation);
}
