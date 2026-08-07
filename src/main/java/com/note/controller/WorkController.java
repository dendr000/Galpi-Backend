package com.note.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import com.note.domain.Work;
import com.note.repository.WorkRepository;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/works")
@CrossOrigin
@Slf4j
public class WorkController {

    @Autowired
    private WorkRepository workRepository;

    // 1. 시스템에 등록된 모든 작품 목록을 가져옵니다. (제작자, 분류 자동완성 힌트용 자원 수집)
    @GetMapping
    public ResponseEntity<?> getAllWorks() {
        log.info("[WorkController] 데이터리스트 자동완성 힌트 생성을 위한 전체 작품 데이터 추출을 개시합니다.");
        List<Work> works = workRepository.findAll();
        log.info("[WorkController] 총 {}건의 데이터 추출 완료 및 전송 진행", works.size());
        return new ResponseEntity<>(works, HttpStatus.OK);
    }

    // 2. 단일 작품의 상세 메타데이터를 조회합니다.
    @GetMapping("/{id}")
    public ResponseEntity<?> getWork(@PathVariable @NonNull Long id) {
        log.info("[WorkController] 단건 조회 요청 접수 완료. 타겟 일련번호: {}", id);
        Work work = workRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 작품"));
        return new ResponseEntity<>(work, HttpStatus.OK);
    }

    // 3. 신규 작품 등록을 처리합니다.
    @PostMapping
    public ResponseEntity<?> saveWork(@RequestBody @NonNull Work work) {
        log.info("[WorkController] 신규 작품 문서 생성 인서트 요청 수신 완료. 페이로드: {}", work);
        Work savedWork = workRepository.save(work);
        log.info("[WorkController] 데이터베이스 영구 저장 성공. 할당된 ID: {}", savedWork.getId());
        return new ResponseEntity<>(savedWork, HttpStatus.CREATED);
    }

    // 4. 기존 작품 메타데이터 및 장르 문자열 갱신을 처리합니다.
    @PutMapping("/{id}")
    public ResponseEntity<?> updateWork(@PathVariable @NonNull Long id, @RequestBody @NonNull Work work) {
        log.info("[WorkController] 기존 작품 설정 수정 갱신 요청 접수. 타겟 ID: {}", id);
        Work workEntity = workRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("타겟 부재"));
        
        workEntity.setTitle(work.getTitle());
        workEntity.setGenre(work.getGenre());
        workEntity.setCreator(work.getCreator());
        workEntity.setStatus(work.getStatus());
        workEntity.setCheckDate(work.getCheckDate());
        workEntity.setDescription(work.getDescription());
        
        Work updatedWork = workRepository.save(workEntity);
        log.info("[WorkController] 데이터베이스 오버라이딩 성공. 최종 상태 데이터 반환");
        return new ResponseEntity<>(updatedWork, HttpStatus.OK);
    }

    // 5. 단일 작품 데이터를 데이터베이스에서 영구 삭제합니다.
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteWork(@PathVariable @NonNull Long id) {
        log.info("[WorkController] 작품 영구 삭제 요청 접수. 타겟 ID: {}", id);
        
        try {
            workRepository.deleteById(id);
            log.info("[WorkController] 데이터베이스 작품 삭제 완료. ID: {}", id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.error("[WorkController] 작품 삭제 중 오류 발생: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}