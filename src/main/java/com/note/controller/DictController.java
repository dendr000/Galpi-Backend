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

    // 한 번의 검색 요청으로 내려보낼 최대 결과 건수 (전체 로딩으로 인한 OOM 방지).
    // 300건이면 OOM 위험 없이 여유 있고, 정렬 우선순위(DictRepository.searchByKeyword) 덕분에
    // 캡에 걸려도 정확히 일치하는 항목은 항상 살아남는다.
    private static final int SEARCH_RESULT_LIMIT = 300;

    @Autowired
    private DictRepository dictRepository;

    // 사전 전체 목록 — Alt+H 실시간 치환용 로컬 IndexedDB 캐시를 한 번 채울 때(dictLocalDb.js
    // seedDictIfNeeded) 이 엔드포인트로 전체를 딱 한 번만 받아간다. 이후엔 로컬 캐시를 재사용
    // 하므로 재요청되지 않는다.
    @GetMapping
    public List<Dict> getDictList() {
        return dictRepository.findAll();
    }

    // ★ 사전 모달 검색 전용 엔드포인트: 작품 구분 없이 전체 사전에서 키워드가 입력됐을 때만
    // 최대 SEARCH_RESULT_LIMIT건만 조회한다. 키워드가 비어 있으면 빈 결과를 반환(전체 조회 차단).
    @GetMapping("/search")
    public List<Dict> searchDict(@RequestParam String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }
        Pageable limit = PageRequest.of(0, SEARCH_RESULT_LIMIT);
        return dictRepository.searchByKeyword(keyword.trim(), limit);
    }

    @PostMapping
    @Transactional
    public Dict saveDict(@RequestBody Dict dict) {
        // ★ 단어+한자 쌍이 완전히 똑같은 찌꺼기가 들어올 때만 차단
        Dict existing = dictRepository.findByWordAndTranslation(dict.getWord(), dict.getTranslation());
        if (existing != null) {
            return existing;
        }
        return dictRepository.save(dict);
    }

    @PostMapping("/bulk")
    @Transactional
    public void saveBulkDicts(@RequestBody List<Dict> dicts) {
        for (Dict d : dicts) {
            Dict existing = dictRepository.findByWordAndTranslation(d.getWord(), d.getTranslation());
            if (existing == null) {
                dictRepository.save(d);
            }
        }
    }

    @DeleteMapping
    @Transactional
    public void deleteDict(@RequestParam String word, @RequestParam(required = false) String translation) {
        if (translation != null && !translation.isEmpty()) {
            // 특정 한자 쌍만 파괴
            dictRepository.deleteByWordAndTranslation(word, translation);
        } else {
            // 해당 단어 전체 파괴
            dictRepository.deleteByWord(word);
        }
    }
}
