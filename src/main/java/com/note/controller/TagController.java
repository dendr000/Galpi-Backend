package com.note.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.note.domain.Tag;
import com.note.repository.TagRepository;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/tags")
@CrossOrigin
@Slf4j
public class TagController {

    @Autowired
    private TagRepository tagRepository;

    // 브라우저가 /api/tags?type=MBTI 라고 호출하면 DB에서 데이터를 꺼내 응답하는 라이브 API 포트입니다.
    @GetMapping
    public ResponseEntity<?> getTagsByType(@RequestParam @NonNull String type) {
        log.info("[TagController] 브라우저로부터 태그 마스터 데이터 동적 로딩 요청 수신. 유형 파라미터: {}", type);
        
        // 데이터베이스 쿼리를 실행하여 매핑된 결과 리스트를 바인딩합니다.
        List<Tag> tagList = tagRepository.findByTagType(type);
        
        log.info("[TagController] 데이터베이스 쿼리 완료. 반환 전송될 태그 건수: {}건", tagList.size());
        return new ResponseEntity<>(tagList, HttpStatus.OK);
    }
}