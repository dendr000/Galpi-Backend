package com.note.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.note.domain.Note;

// 1. JpaRepository를 상속받아, 기본적인 CRUD(저장, 조회, 수정, 삭제) 메서드를 자동으로 물려받습니다.
// 2. <Note, Long>은 이 저장소가 Note 엔티티를 다루며, 그 기본 키(PK)의 타입이 Long이라는 뜻입니다.
public interface NoteRepository extends JpaRepository<Note, Long> {
    // 내부 코드를 단 한 줄도 적지 않아도, 스프링 부트가 알아서 SQL을 생성하여 구현체를 만들어 줍니다.
}