// 파일 위치: src/main/java/com/note/repository/CharacterRepository.java

package com.note.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.note.domain.CharacterEntity;

// 1. 캐릭터 데이터를 DB에 넣고 빼는 역할을 합니다.
public interface CharacterRepository extends JpaRepository<CharacterEntity, Long> {
    // 2. 특정 작품(workId)에 속한 캐릭터들만 싹 모아서 가져오는 커스텀 쿼리 메서드입니다.
    List<CharacterEntity> findByWorkId(Long workId);
    
    // ★ 전역 자동완성 검색용: JSON 문자열 내부에 특정 키워드가 포함된 엔티티 필터링
    List<CharacterEntity> findByDynamicPropertiesContaining(String keyword);
}