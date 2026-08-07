package com.note.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.note.domain.Work;

// 1. 작품(Work) 데이터를 DB에 넣고 빼는 역할을 합니다.
public interface WorkRepository extends JpaRepository<Work, Long> {
}