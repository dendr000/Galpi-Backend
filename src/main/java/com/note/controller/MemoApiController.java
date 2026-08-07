// 파일 위치: src/main/java/com/note/controller/MemoApiController.java
// 기능 요약: 메모 영속성 제어 API 비동기 컨트롤러 (휴지통, 잠금, 다중 태그 기능 병합 버전)
// 버전: v1.1.0

package com.note.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import com.note.domain.Memo;
import com.note.repository.MemoRepository;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/memos")
@CrossOrigin
@Slf4j
public class MemoApiController {

    @Autowired
    private MemoRepository memoRepository;

    // 1. 시스템에 등록된 모든 메모 리스트를 수집하여 클라이언트 브라우저 세션에 동기화합니다.
    @GetMapping
    public ResponseEntity<?> getAllMemos() {
        log.info("[MemoApiController] 전역 데이터 무결성 검증 및 로컬 스토리지 동기화를 위한 전체 메모 추출 개시.");
        List<Memo> memos = memoRepository.findAll();
        log.info("[MemoApiController] 데이터베이스 스캔 결과 총 {}건의 메모 오브젝트가 확보되었습니다.", memos.size());
        return new ResponseEntity<>(memos, HttpStatus.OK);
    }

    // 2. 신규 메모 개체를 데이터베이스 인서트 모듈로 전달합니다.
    @PostMapping
    public ResponseEntity<?> createMemo(@RequestBody @NonNull Memo memo) {
        log.info("[MemoApiController] 신규 가상 메모 개체 영구 저장 인서트 요청 수신. 페이로드: {}", memo);
        if (memo.getUpdatedAt() == null) {
            memo.setUpdatedAt(System.currentTimeMillis());
        }
        Memo savedMemo = memoRepository.save(memo);
        log.info("[MemoApiController] 데이터베이스 영구 인서트 성공. 할당된 메모 고유 일련번호: {}", savedMemo.getId());
        return new ResponseEntity<>(savedMemo, HttpStatus.CREATED);
    }

    // 3. 에디터에서 가공 및 수정이 완료된 기존 물리 메모 데이터를 갱신합니다.
    @PutMapping("/{id}")
    public ResponseEntity<?> updateMemo(@PathVariable @NonNull Long id, @RequestBody @NonNull Memo memoDetails) {
        log.info("[MemoApiController] 메모 본문 데이터 오버라이딩 수정 요청 접수. 타겟 식별 ID: {}", id);
        Memo memoEntity = memoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("업데이트 대상 메모가 존재하지 않습니다. ID: " + id));
        
        memoEntity.setTitle(memoDetails.getTitle());
        memoEntity.setContent(memoDetails.getContent());
        memoEntity.setFolder(memoDetails.getFolder());
        memoEntity.setSortOrder(memoDetails.getSortOrder());
        memoEntity.setCanvasX(memoDetails.getCanvasX());
        memoEntity.setCanvasY(memoDetails.getCanvasY());
        memoEntity.setThemeColor(memoDetails.getThemeColor());
        memoEntity.setUpdatedAt(System.currentTimeMillis());
        
        // ★ 신규 확장 필드 데이터 바인딩
        memoEntity.setIsTrash(memoDetails.getIsTrash());
        memoEntity.setIsLocked(memoDetails.getIsLocked());
        memoEntity.setTags(memoDetails.getTags());
        
        Memo updatedMemo = memoRepository.save(memoEntity);
        log.info("[MemoApiController] 메모 개체 병합 갱신 완료. 변경 완료 타임스탬프 동기화 성공.");
        return new ResponseEntity<>(updatedMemo, HttpStatus.OK);
    }

    // 4. 단일 타겟 메모 개체를 데이터베이스 세션에서 영구 파괴합니다.
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMemo(@PathVariable @NonNull Long id) {
        log.info("[MemoApiController] 메모 오브젝트 영구 삭제 명령 접수. 타겟 ID: {}", id);
        try {
            memoRepository.deleteById(id);
            log.info("[MemoApiController] 데이터베이스 메모 인덱스 트래킹 제거 완료. 삭제 완료 대상 ID: {}", id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.error("[MemoApiController] 메모 엔티티 영구 삭제 중 예외 처리 발생: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}