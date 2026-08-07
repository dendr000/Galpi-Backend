package com.note.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MemoViewController {
    @GetMapping("/memo")
    public String memoPage() {
        return "memo"; 
    }
}