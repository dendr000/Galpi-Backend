package com.note.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import com.note.domain.Boilerplate;
import com.note.repository.BoilerplateRepository;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/boilerplates")
@CrossOrigin
@Slf4j
public class BoilerplateController {

    @Autowired
    private BoilerplateRepository repository;

    @GetMapping
    public ResponseEntity<?> getAll() {
        log.info("[Boilerplate API] 전체 상용구 목록을 로드합니다.");
        return new ResponseEntity<>(repository.findAll(), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> save(@RequestBody @NonNull Boilerplate bp) {
        log.info("[Boilerplate API] 신규 상용구 저장: {}", bp.getTitle());
        if (bp.getCategory() == null || bp.getCategory().isEmpty()) bp.setCategory("공통");
        return new ResponseEntity<>(repository.save(bp), HttpStatus.CREATED);
    }

    // ★ 수정(Update) 처리 API 추가
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable @NonNull Long id, @RequestBody @NonNull Boilerplate bp) {
        return repository.findById(id).map(existing -> {
            existing.setCategory(bp.getCategory() != null && !bp.getCategory().isEmpty() ? bp.getCategory() : "공통");
            existing.setTitle(bp.getTitle());
            existing.setContent(bp.getContent());
            return new ResponseEntity<>(repository.save(existing), HttpStatus.OK);
        }).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable @NonNull Long id) {
        log.info("[Boilerplate API] 상용구 삭제 ID: {}", id);
        repository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}