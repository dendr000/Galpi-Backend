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
        Dict existing = dictRepository.findByWorkIdAndWord(dict.getWorkId(), dict.getWord());
        if (existing != null) {
            existing.setTranslation(dict.getTranslation());
            return dictRepository.save(existing);
        }
        return dictRepository.save(dict);
    }

    @PostMapping("/bulk")
    @Transactional
    public void saveBulkDicts(@RequestBody List<Dict> dicts) {
        for (Dict d : dicts) {
            Dict existing = dictRepository.findByWorkIdAndWord(d.getWorkId(), d.getWord());
            if (existing == null) {
                dictRepository.save(d);
            }
        }
    }

    @DeleteMapping
    @Transactional
    public void deleteDict(@RequestParam String workId, @RequestParam String word) {
        dictRepository.deleteByWorkIdAndWord(workId, word);
    }
}