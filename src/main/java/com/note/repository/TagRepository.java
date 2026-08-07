package com.note.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.note.domain.Tag;

// JpaRepository를 상속받아 태그 마스터 테이블에 데이터를 셀렉트, 인서트하는 통로 역할을 수행합니다.
public interface TagRepository extends JpaRepository<Tag, Long> {
    
    // 특정 타입(예: MBTI만, 혹은 장르만)에 부합하는 태그 목록만 데이터베이스에서 쏙 골라오는 커스텀 메서드입니다.
    List<Tag> findByTagType(String tagType);
}