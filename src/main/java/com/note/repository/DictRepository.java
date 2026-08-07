package com.note.repository;

import com.note.domain.Dict;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DictRepository extends JpaRepository<Dict, Long> {
    List<Dict> findByWorkId(String workId);
    Dict findByWorkIdAndWord(String workId, String word);
    void deleteByWorkIdAndWord(String workId, String word);
}