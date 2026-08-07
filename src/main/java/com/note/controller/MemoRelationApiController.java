// 파일 위치: src/main/java/com/note/controller/MemoRelationApiController.java
// 기능 요약: 캔버스 뷰에서 노드 간을 잇는 화살표(관계망) 데이터의 CRUD 통신을 전담하는 REST 컨트롤러
// 버전: v1.0.0

package com.note.controller;

import com.note.domain.MemoRelation;
import com.note.repository.MemoRelationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/memo-relations")
public class MemoRelationApiController {

    private final MemoRelationRepository memoRelationRepository;

    public MemoRelationApiController(MemoRelationRepository memoRelationRepository) {
        this.memoRelationRepository = memoRelationRepository;
    }

    @GetMapping
    public ResponseEntity<List<MemoRelation>> getAllRelations() {
        return ResponseEntity.ok(memoRelationRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<MemoRelation> createRelation(@RequestBody MemoRelation relation) {
        return ResponseEntity.ok(memoRelationRepository.save(relation));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MemoRelation> updateRelation(@PathVariable Long id, @RequestBody MemoRelation updatedRelation) {
        return memoRelationRepository.findById(id)
                .map(relation -> {
                    relation.setSourceId(updatedRelation.getSourceId());
                    relation.setTargetId(updatedRelation.getTargetId());
                    relation.setLabel(updatedRelation.getLabel());
                    relation.setType(updatedRelation.getType());
                    relation.setDescRev(updatedRelation.getDescRev());
                    return ResponseEntity.ok(memoRelationRepository.save(relation));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRelation(@PathVariable Long id) {
        memoRelationRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @Transactional
    @DeleteMapping("/memo/{memoId}")
    public ResponseEntity<Void> deleteRelationsByMemoId(@PathVariable Long memoId) {
        memoRelationRepository.deleteBySourceIdOrTargetId(memoId, memoId);
        return ResponseEntity.ok().build();
    }
}