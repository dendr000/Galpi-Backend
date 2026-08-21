// 파일 위치: src/main/java/com/note/repository/HiddenCategoryRepository.java
package com.note.repository;

import com.note.domain.HiddenCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HiddenCategoryRepository extends JpaRepository<HiddenCategory, Long> {

    boolean existsByCategoryName(String categoryName);

    void deleteByCategoryName(String categoryName);
}
