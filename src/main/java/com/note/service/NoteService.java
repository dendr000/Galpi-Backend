package com.note.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull; // ★ 추가됨: 스프링의 Null 안전성 보장 어노테이션
import org.springframework.stereotype.Service;
import com.note.domain.Note;
import com.note.repository.NoteRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NoteService {
    
    @Autowired
    private NoteRepository noteRepository;
    
    // 파라미터 앞에 @NonNull을 붙여서 "이 note는 절대 Null이 아니다"라고 컴파일러를 안심시킵니다.
    public Note saveNote(@NonNull Note note) {
        log.info("[NoteService] saveNote() 메서드가 호출되었습니다. 저장 시도 객체: {}", note);
        Note savedNote = noteRepository.save(note);
        log.info("[NoteService] saveNote() 데이터베이스 저장 완료. 부여된 ID: {}", savedNote.getId());
        return savedNote;
    }
    
    // 파라미터 앞에 @NonNull 적용
    public Note getNoteOne(@NonNull Long id) {
        log.info("[NoteService] getNoteOne() 메서드가 호출되었습니다. 조회 대상 ID: {}", id);
        
        Note foundNote = noteRepository.findById(id).orElseThrow(() -> {
            log.error("[NoteService] getNoteOne() 예외 발생 - 해당 ID의 데이터가 존재하지 않습니다. ID: {}", id);
            return new IllegalArgumentException("요청하신 ID의 노트를 찾을 수 없습니다: " + id);
        });
        
        log.info("[NoteService] getNoteOne() 조회 성공. 찾은 데이터 제목: {}", foundNote.getTitle());
        return foundNote;
    }
    
    public List<Note> getNoteAll() {
        log.info("[NoteService] getNoteAll() 메서드가 호출되었습니다. 전체 데이터 조회를 시작합니다.");
        List<Note> noteList = noteRepository.findAll();
        log.info("[NoteService] getNoteAll() 전체 조회 완료. 총 조회된 데이터 건수: {}건", noteList.size());
        return noteList;
    }
    
    // 파라미터 앞에 @NonNull 적용
    public Note modifyNote(@NonNull Long id, @NonNull Note note) {
        log.info("[NoteService] modifyNote() 메서드가 호출되었습니다. 수정 대상 ID: {}", id);
        
        Note noteEntity = noteRepository.findById(id).orElseThrow(() -> {
            log.error("[NoteService] modifyNote() 예외 발생 - 수정할 대상이 존재하지 않습니다. ID: {}", id);
            return new IllegalArgumentException("수정하려는 ID의 노트를 찾을 수 없습니다: " + id);
        });
        
        log.info("[NoteService] modifyNote() 데이터 변경 진행 중... 기존 제목: {} -> 새 제목: {}", noteEntity.getTitle(), note.getTitle());
        noteEntity.setTitle(note.getTitle());
        noteEntity.setContent(note.getContent());
        
        noteRepository.save(noteEntity);
        log.info("[NoteService] modifyNote() 데이터 갱신 및 데이터베이스 저장 완료.");
        return noteEntity;
    }
    
    // 파라미터 앞에 @NonNull 적용
    public String delNote(@NonNull Long id) {
        log.info("[NoteService] delNote() 메서드가 호출되었습니다. 삭제 대상 ID: {}", id);
        noteRepository.deleteById(id); // (오타 수정: noteRepository로 변경해야 함!)
        noteRepository.deleteById(id); 
        log.info("[NoteService] delNote() 삭제 처리 완료.");
        return "delete success";
    }
}