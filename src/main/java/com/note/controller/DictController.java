// 파일 위치: src/main/java/com/note/controller/DictController.java
package com.note.controller;

import com.note.domain.Dict;
import com.note.repository.DictRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/dicts")
public class DictController {

    @Autowired
    private DictRepository dictRepository;

    @GetMapping
    public List<Dict> getDictList(@RequestParam(defaultValue = "global") String workId) {
        return dictRepository.findByWorkId(workId);
    }

    @PostMapping
    @Transactional
    public Dict saveDict(@RequestBody Dict dict) {
        // ★ 단어+한자 쌍이 완전히 똑같은 찌꺼기가 들어올 때만 차단
        Dict existing = dictRepository.findByWorkIdAndWordAndTranslation(dict.getWorkId(), dict.getWord(), dict.getTranslation());
        if (existing != null) {
            return existing; 
        }
        return dictRepository.save(dict);
    }

    @PostMapping("/bulk")
    @Transactional
    public void saveBulkDicts(@RequestBody List<Dict> dicts) {
        for (Dict d : dicts) {
            Dict existing = dictRepository.findByWorkIdAndWordAndTranslation(d.getWorkId(), d.getWord(), d.getTranslation());
            if (existing == null) {
                dictRepository.save(d);
            }
        }
    }

    @DeleteMapping
    @Transactional
    public void deleteDict(@RequestParam String workId, @RequestParam String word, @RequestParam(required = false) String translation) {
        if (translation != null && !translation.isEmpty()) {
            // 특정 한자 쌍만 파괴
            dictRepository.deleteByWorkIdAndWordAndTranslation(workId, word, translation);
        } else {
            // 해당 단어 전체 파괴
            dictRepository.deleteByWorkIdAndWord(workId, word);
        }
    }
}