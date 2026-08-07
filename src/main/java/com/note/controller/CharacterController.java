// 파일 위치: src/main/java/com/note/controller/CharacterController.java

package com.note.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import com.note.domain.CharacterEntity;
import com.note.repository.CharacterRepository;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import java.util.HashSet;
import java.util.List;

@RestController
@RequestMapping("/api/characters")
@CrossOrigin
@Slf4j
public class CharacterController {

    @Autowired
    private CharacterRepository characterRepository;

    // 1. 소속 작품 일련번호 조건에 부합하는 모든 캐릭터 카드 목록을 가져옵니다.
    @GetMapping
    public ResponseEntity<?> getCharacters(@RequestParam(required = false) Long workId) {
        if (workId != null) {
            log.info("[CharacterController] GET /api/characters - 소속 캐릭터 목록 추출. 부모 작품 ID: {}", workId);
            return new ResponseEntity<>(characterRepository.findByWorkId(workId), HttpStatus.OK);
        } else {
            log.info("[CharacterController] GET /api/characters - 400 에러 방지용 전체 캐릭터 목록 추출");
            return new ResponseEntity<>(characterRepository.findAll(), HttpStatus.OK);
        }
    }

    // ★ 전역 DB 기반 빠른 검색(자동완성)을 위한 신규 API
    @GetMapping("/suggest")
    public ResponseEntity<Set<String>> suggestProperties(@RequestParam String column, @RequestParam String keyword) {
        log.info("[CharacterController] GET /api/characters/suggest - 자동완성 검색. 컬럼: {}, 키워드: {}", column, keyword);
        Set<String> results = new HashSet<>();
        
        try {
            // 하드코딩된 물리 컬럼 검색 (종족, 성별, 나이)
            if ("종족".equals(column) || "성별".equals(column) || "나이".equals(column)) {
                List<CharacterEntity> all = characterRepository.findAll();
                for(CharacterEntity c : all) {
                    String val = "종족".equals(column) ? c.getSpecies() : ("성별".equals(column) ? c.getGender() : c.getAge());
                    if (val != null && val.contains(keyword)) results.add(val);
                }
            } 
            // 다이나믹 프로퍼티스(JSON) 내부 검색 (관계, 소속, 능력 등)
            else {
                List<CharacterEntity> candidates = characterRepository.findByDynamicPropertiesContaining(keyword);
                ObjectMapper mapper = new ObjectMapper();
                for(CharacterEntity c : candidates) {
                    if (c.getDynamicProperties() != null) {
                        try {
                            JsonNode node = mapper.readTree(c.getDynamicProperties());
                            if (node.has(column)) {
                                String val = node.get(column).asText();
                                if (val.contains(keyword)) results.add(val);
                            }
                        } catch (Exception e) {
                            // JSON 파싱 실패 시 무시하고 다음 캐릭터 진행
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("[CharacterController] 자동완성 데이터 추출 실패", e);
        }
        
        return new ResponseEntity<>(results, HttpStatus.OK);
    }

    // 2. 새 캐릭터 카드를 단건 추가 등록 처리합니다.
    @PostMapping
    public ResponseEntity<?> saveCharacter(@RequestBody @NonNull CharacterEntity character) {
        log.info("[CharacterController] POST /api/characters - 신규 인물 카드 생성 트래픽 수신");
        CharacterEntity savedChar = characterRepository.save(character);
        return new ResponseEntity<>(savedChar, HttpStatus.CREATED);
    }

    // 3. 기존 인물 설정 카드의 데이터 수정을 맵핑 갱신 처리합니다.
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCharacter(@PathVariable @NonNull Long id, @RequestBody @NonNull CharacterEntity character) {
        CharacterEntity charEntity = characterRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("수정 대상 인물 없음"));
        log.info("[CharacterController] PUT /api/characters/{} - 인물 설정 카드 갱신 트래픽 수신", id);
        
        charEntity.setName(character.getName());
        charEntity.setImageCode(character.getImageCode());
        charEntity.setDynamicProperties(character.getDynamicProperties());
        
        charEntity.setAge(character.getAge());
        charEntity.setBirthday(character.getBirthday());
        charEntity.setGender(character.getGender());
        charEntity.setSpecies(character.getSpecies());
        
        CharacterEntity updatedChar = characterRepository.save(charEntity);
        log.info("[CharacterController] 인물 카드 정보 갱신 세이브 완료.");
        return new ResponseEntity<>(updatedChar, HttpStatus.OK);
    }

    // 단일 캐릭터 데이터를 데이터베이스에서 영구 삭제합니다.
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCharacter(@PathVariable @NonNull Long id) {
        log.info("[CharacterController] 캐릭터 영구 삭제 요청 접수. 타겟 ID: {}", id);
        
        try {
            characterRepository.deleteById(id); 
            log.info("[CharacterController] 데이터베이스 캐릭터 삭제 완료. ID: {}", id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.error("[CharacterController] 캐릭터 삭제 중 오류 발생: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}