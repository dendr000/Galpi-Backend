// 파일 위치: src/main/java/com/note/controller/DictController.java
package com.note.controller;

import com.note.domain.Dict;
import com.note.repository.DictRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/dicts")
public class DictController {

    // 한 번의 검색 요청으로 내려보낼 최대 결과 건수 (전체 로딩으로 인한 OOM 방지)
    private static final int SEARCH_RESULT_LIMIT = 150;

    @Autowired
    private DictRepository dictRepository;

    @GetMapping
    public List<Dict> getDictList(@RequestParam(defaultValue = "global") String workId) {
        return dictRepository.findByWorkId(workId);
    }

    // ★ 사전 모달 검색 전용 엔드포인트: 키워드가 입력됐을 때만 workId(전역+현재 작품) 범위에서
    // 최대 SEARCH_RESULT_LIMIT건만 조회한다. 키워드가 비어 있으면 빈 결과를 반환(전체 조회 차단).
    @GetMapping("/search")
    public List<Dict> searchDict(
            @RequestParam(defaultValue = "global") String workId,
            @RequestParam String keyword
    ) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }
        Pageable limit = PageRequest.of(0, SEARCH_RESULT_LIMIT);
        return dictRepository.searchByKeyword(workId, keyword.trim(), limit);
    }

    @PostMapping
    @Transactional
    public Dict saveDict(@RequestBody Dict dict) {
        // ★ 단어+한자 쌍이 완전히 똑같은 찌꺼기가 들어올 때만 차단
        Dict existing = dictRepository.findByWorkIdAndWordAndTranslation(dict.getWorkId(), dict.getWord(), dict.getTranslation());
        if (existing != null) {
            return existing; 
        }
        return dictRepository.save(dict);
    }

    @PostMapping("/bulk")
    @Transactional
    public void saveBulkDicts(@RequestBody List<Dict> dicts) {
        for (Dict d : dicts) {
            Dict existing = dictRepository.findByWorkIdAndWordAndTranslation(d.getWorkId(), d.getWord(), d.getTranslation());
            if (existing == null) {
                dictRepository.save(d);
            }
        }
    }

    @DeleteMapping
    @Transactional
    public void deleteDict(@RequestParam String workId, @RequestParam String word, @RequestParam(required = false) String translation) {
        if (translation != null && !translation.isEmpty()) {
            // 특정 한자 쌍만 파괴
            dictRepository.deleteByWorkIdAndWordAndTranslation(workId, word, translation);
        } else {
            // 해당 단어 전체 파괴
            dictRepository.deleteByWorkIdAndWord(workId, word);
        }
    }
}