// 파일 위치: src/main/java/com/note/controller/HiddenCategoryController.java
package com.note.controller;

import com.note.domain.HiddenCategory;
import com.note.repository.HiddenCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping("/api/hidden-categories")
public class HiddenCategoryController {

    @Autowired
    private HiddenCategoryRepository hiddenCategoryRepository;

    @GetMapping
    public List<String> getHiddenCategories() {
        return hiddenCategoryRepository.findAll().stream()
                .map(HiddenCategory::getCategoryName)
                .collect(Collectors.toList());
    }

    @PostMapping
    @Transactional
    public void hideCategory(@RequestBody HiddenCategory request) {
        if (!hiddenCategoryRepository.existsByCategoryName(request.getCategoryName())) {
            hiddenCategoryRepository.save(new HiddenCategory(null, request.getCategoryName()));
        }
    }

    @DeleteMapping
    @Transactional
    public void unhideCategory(@RequestParam String categoryName) {
        hiddenCategoryRepository.deleteByCategoryName(categoryName);
    }
}
