package com.note.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull; // ★ 추가됨: Null 안전성을 컴파일러에게 보장하기 위한 임포트
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.note.domain.Note;
import com.note.service.NoteService;
import lombok.extern.slf4j.Slf4j;

// 1. 이 클래스가 화면이 아닌 순수 데이터(JSON)를 응답하는 REST 전용 컨트롤러임을 명시합니다.
@RestController
// 2. 이 컨트롤러 내부의 모든 엔드포인트 주소 앞에 기본적으로 '/api/notes'가 붙도록 경로를 지정합니다.
@RequestMapping("/api/notes")
// 3. 웹 브라우저 화면(포트 다름 등)과 스프링 부트 서버 간의 자원 공유를 허용하여 통신 오류를 방지합니다.
@CrossOrigin
// 4. log.info()를 사용하여 콘솔창에 세세한 로그를 남길 수 있도록 롬복 로거를 주입합니다.
@Slf4j
public class NoteController {
    
    // 5. 비즈니스 로직 처리를 담당하는 NoteService 빈을 자동으로 주입받습니다.
    @Autowired
    private NoteService noteService;
    
    // 6. 데이터베이스에 있는 모든 노트의 목록을 조회하는 GET API입니다. (URL: GET /api/notes)
    @GetMapping
    public ResponseEntity<?> findAll() {
        // 7. 컨트롤러 진입 로그를 명확하게 남깁니다.
        log.info("[NoteController] GET /api/notes - findAll() 전체 조회 요청 트래픽이 수신되었습니다.");
        
        // 8. 서비스의 getNoteAll()을 호출하고, 결과 리스트를 HTTP 200 OK 상태 코드와 함께 반환합니다.
        return new ResponseEntity<>(noteService.getNoteAll(), HttpStatus.OK);
    }
    
    // 9. 새로운 노트 데이터를 하나 등록하는 POST API입니다. (URL: POST /api/notes)
    @PostMapping
    public ResponseEntity<?> saveNote(@RequestBody @NonNull Note note) {
        // 10. @NonNull을 붙여서 이 메서드로 들어오는 note 데이터 객체가 절대 Null이 아님을 보장합니다.
        log.info("[NoteController] POST /api/notes - saveNote() 신규 등록 요청이 수신되었습니다.");
        // 11. 전달받은 JSON 파싱 데이터 값을 콘솔에 검증용으로 출력합니다.
        log.info("[NoteController] 요청 본문 데이터 내용물: {}", note);
        
        // 12. 안전하게 보장된 note 객체를 서비스에 넘겨 저장하고, HTTP 201 CREATED 상태 코드로 응답합니다.
        return new ResponseEntity<>(noteService.saveNote(note), HttpStatus.CREATED);
    }
    
    // 13. URL 주소에 포함된 ID 값을 기준으로 노트 1건을 상세 조회하는 GET API입니다. (URL: GET /api/notes/{id})
    @GetMapping("/{id}")
    public ResponseEntity<?> getNoteOne(@PathVariable(name = "id") @NonNull Long id) {
        // 14. @NonNull을 ID 앞에 붙여서 조회하려는 식별자 식별 번호가 Null이 아님을 명시합니다.
        log.info("[NoteController] GET /api/notes/{} - getNoteOne() 단건 상세 조회 요청이 수신되었습니다.", id);
        // 15. 수신된 변수 값을 재차 로그로 확인합니다.
        log.info("[NoteController] 파라미터 파싱 ID 값: {}", id);
        
        // 16. 식별 번호를 서비스에 넘겨 단건 조회를 수행한 뒤 HTTP 200 OK로 반환합니다.
        return new ResponseEntity<>(noteService.getNoteOne(id), HttpStatus.OK);
    }
    
    // 17. 기존에 저장되어 있는 특정 ID의 노트 정보를 수정하는 PUT API입니다. (URL: PUT /api/notes/{id})
    @PutMapping("/{id}")
    public ResponseEntity<?> updateNote(@PathVariable(name = "id") @NonNull Long id, @RequestBody @NonNull Note note) {
        // 18. ID와 Note 객체 모두에 @NonNull을 선언하여 서비스 계층의 조건 타입과 완벽하게 일치시킵니다.
        log.info("[NoteController] PUT /api/notes/{} - updateNote() 데이터 갱신 요청이 수신되었습니다.", id);
        // 19. 어떤 ID의 데이터가 어떤 값으로 변경 요청되었는지 상세 히스토리 로그를 남깁니다.
        log.info("[NoteController] 요청 타겟 ID: {}, 변경 타겟 데이터: {}", id, note);
        
        // 20. 변경 로직을 수행하고 최종 반영된 엔티티 객체를 HTTP 200 OK와 함께 클라이언트로 보냅니다.
        return new ResponseEntity<>(noteService.modifyNote(id, note), HttpStatus.OK);
    }
    
    // 21. 특정 ID의 데이터를 완전히 삭제 처리하는 DELETE API입니다. (URL: DELETE /api/notes/{id})
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delNote(@PathVariable(name = "id") @NonNull Long id) {
        // 22. @NonNull을 적용하여 삭제 대상 키 값이 안전함을 보장합니다.
        log.info("[NoteController] DELETE /api/notes/{} - delNote() 영구 삭제 요청이 수신되었습니다.", id);
        // 23. 파싱된 타겟 ID를 재확인 로그로 남깁니다.
        log.info("[NoteController] 삭제를 시도하는 ID 값: {}", id);
        
        // 24. 삭제 비즈니스 로직을 호출하고 성공 메시지 문자열을 HTTP 200 OK 상태 코드와 함께 전송합니다.
        return new ResponseEntity<>(noteService.delNote(id), HttpStatus.OK);
    }
}