package com.note.controller;

import com.note.domain.WikiPage;
import com.note.repository.WikiPageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wikipages")
@CrossOrigin
@Slf4j
public class WikiPageController {

    @Autowired
    private WikiPageRepository wikiPageRepository;

    // 1. 특정 작품의 최상위 문서 목록 조회
    @GetMapping("/roots/{workId}")
    public ResponseEntity<List<WikiPage>> getRootPages(@PathVariable Long workId) {
        return new ResponseEntity<>(wikiPageRepository.findByWorkIdAndParentIdIsNullOrderByOrderNumAsc(workId), HttpStatus.OK);
    }

    // ★ [확장 추가 v1.0.0] 특정 작품에 종속된 모든 하위 위키 문서 일괄 추출 API (폴더 트리 및 그래프용)
    @GetMapping("/work/{workId}")
    public ResponseEntity<List<WikiPage>> getAllPagesByWork(@PathVariable Long workId) {
        log.info("[WikiPage API] 작품 ID {}에 해당하는 모든 하위 문서 통짜 덤프 로드 시작", workId);
        return new ResponseEntity<>(wikiPageRepository.findByWorkId(workId), HttpStatus.OK);
    }

    // 2. 특정 부모의 하위 문서 목록 조회
    @GetMapping("/children/{parentId}")
    public ResponseEntity<List<WikiPage>> getChildPages(@PathVariable Long parentId) {
        return new ResponseEntity<>(wikiPageRepository.findByParentIdOrderByOrderNumAsc(parentId), HttpStatus.OK);
    }

    // 3. 단일 문서 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<?> getPageOne(@PathVariable Long id) {
        return wikiPageRepository.findById(id)
                .map(page -> new ResponseEntity<>(page, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // 4. 새 문서 생성
    @PostMapping
    public ResponseEntity<?> savePage(@RequestBody @NonNull WikiPage wikiPage) {
        if (wikiPage.getOrderNum() == null) wikiPage.setOrderNum(0);
        WikiPage saved = wikiPageRepository.save(wikiPage);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    // 5. 문서 내용 및 제목 수정
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePage(@PathVariable @NonNull Long id, @RequestBody @NonNull WikiPage wikiPage) {
        return wikiPageRepository.findById(id).map(existing -> {
            existing.setTitle(wikiPage.getTitle());
            existing.setContent(wikiPage.getContent());
            existing.setOrderNum(wikiPage.getOrderNum() != null ? wikiPage.getOrderNum() : existing.getOrderNum());
            // 부모 이동(폴더 이동) 기능 지원
            existing.setParentId(wikiPage.getParentId()); 
            return new ResponseEntity<>(wikiPageRepository.save(existing), HttpStatus.OK);
        }).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // 6. 문서 영구 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePage(@PathVariable @NonNull Long id) {
        // 주의: 하위 문서가 있는 상태에서 삭제할 경우에 대한 처리는 추후 고도화 필요
        wikiPageRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}