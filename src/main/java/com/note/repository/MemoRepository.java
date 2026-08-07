package com.note.repository;

import com.note.domain.Memo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// ==========================================================================
// [MemoRepository.java] 메모장 레포지토리
// 기능 요약: DB 메모 테이블 CRUD 인터페이스
// ==========================================================================
@Repository
public interface MemoRepository extends JpaRepository<Memo, Long> {
}