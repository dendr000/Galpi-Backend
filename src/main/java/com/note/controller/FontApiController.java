// src/main/java/com/note/controller/FontApiController.java
package com.note.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

@RestController
@RequestMapping("/api/fonts")
@CrossOrigin
@Slf4j
public class FontApiController {

    private final String FONT_DIR = "C:/dev/Galpi/Galpi-Media/fonts";
    private final String DICT_FILE = FONT_DIR + "/font-dict.json";

    @GetMapping
    public ResponseEntity<List<Map<String, String>>> scanFonts() {
        log.info("[FontApiController] 로컬 폰트 디렉토리 자동 스캔 및 JSON 사전 매핑 개시");
        List<Map<String, String>> fontList = new ArrayList<>();
        File folder = new File(FONT_DIR);

        if (folder.exists() && folder.isDirectory()) {
            
            // 1. 외부 JSON 사전 동적 로드
            Map<String, String> dictionary = new HashMap<>();
            File dictFile = new File(DICT_FILE);
            if (dictFile.exists() && dictFile.isFile()) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    dictionary = mapper.readValue(dictFile, new TypeReference<Map<String, String>>() {});
                } catch (Exception e) {
                    log.error("[FontApiController] font-dict.json 파싱 실패", e);
                }
            }

            // 2. 폰트 파일 스캔 (.ttf, .woff 등)
            File[] files = folder.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".ttf") || 
                name.toLowerCase().endsWith(".woff") || 
                name.toLowerCase().endsWith(".woff2") || 
                name.toLowerCase().endsWith(".otf")
            );

            if (files != null) {
                for (File file : files) {
                    String filename = file.getName();
                    String fontFamily = filename.substring(0, filename.lastIndexOf('.'));

                    // 1차: 카멜케이스 자동 띄어쓰기 및 첫 글자 대문자화
                    String displayName = fontFamily.replaceAll("([a-z])([A-Z]+)", "$1 $2");
                    displayName = displayName.substring(0, 1).toUpperCase() + displayName.substring(1);

                    // 2차: JSON 사전 치환 적용 (실시간 반영)
                    for (Map.Entry<String, String> entry : dictionary.entrySet()) {
                        displayName = displayName.replace(entry.getKey(), entry.getValue());
                    }

                    // 3차: 굵기(Weight) 직관적 파악을 위한 1~9 넘버링 및 영문 표기 치환
                    displayName = displayName
                            // 폰트 파일 특유의 오타 및 찌꺼기 번호(_0) 제거
                            .replace("-Extra Boldold_0", " 8-ExtraBold")
                            .replace("-Extra Lightight_0", " 2-ExtraLight")
                            .replace("-Semi Boldold_0", " 6-SemiBold")
                            .replace("-Black_0", " 9-Black")
                            .replace("-Medium_0", " 5-Medium")
                            .replace("-Thin_0", " 1-Thin")
                            .replace("-Regular_0", " 4-Regular")
                            .replace("-Regular", " 4-Regular")
                            .replace("-VF", " VF")
                            // 하이픈(-) 공백 치환
                            .replace("-", " ")
                            // 일반적인 영문 굵기 명칭을 넘버링으로 치환
                            .replace(" Extra Bold", " 8-ExtraBold")
                            .replace(" Extra Light", " 2-ExtraLight")
                            .replace(" Semi Bold", " 6-SemiBold")
                            .replace(" Bold", " 7-Bold")
                            .replace(" Medium", " 5-Medium")
                            .replace(" Light", " 3-Light")
                            .replace(" Thin", " 1-Thin")
                            .replace(" Black", " 9-Black")
                            .replace(" Regular", " 4-Regular")
                            // 단일 알파벳 약자 치환
                            .replace(" B", " 7-Bold")
                            .replace(" M", " 5-Medium")
                            .replace(" L", " 3-Light")
                            .replace(" R", " 4-Regular");
                            
                    // 중복된 공백 깔끔하게 1개로 압축
                    displayName = displayName.replaceAll("\\s+", " ").trim();

                    Map<String, String> fontMap = new HashMap<>();
                    fontMap.put("filename", filename);
                    fontMap.put("fontFamily", fontFamily);
                    fontMap.put("displayName", displayName);

                    fontList.add(fontMap);
                }
            }
        } else {
            log.warn("[FontApiController] 폰트 폴더를 찾을 수 없습니다: " + FONT_DIR);
        }
        
        return new ResponseEntity<>(fontList, HttpStatus.OK);
    }

    // ★ 프론트엔드 Footer 모달 연동을 위한 JSON 사전 읽기 API
    @GetMapping("/dict")
    public ResponseEntity<Map<String, String>> getFontDictionary() {
        File dictFile = new File(DICT_FILE);
        try {
            if (dictFile.exists() && dictFile.isFile()) {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, String> dict = mapper.readValue(dictFile, new TypeReference<Map<String, String>>() {});
                return new ResponseEntity<>(dict, HttpStatus.OK);
            }
        } catch (Exception e) {
            log.error("[FontApiController] 사전 읽기 실패", e);
        }
        return new ResponseEntity<>(new HashMap<>(), HttpStatus.OK);
    }

    // ★ 프론트엔드 Footer 모달 연동을 위한 JSON 사전 쓰기(덮어쓰기) API
    @PostMapping("/dict")
    public ResponseEntity<?> updateFontDictionary(@RequestBody Map<String, String> newDict) {
        File folder = new File(FONT_DIR);
        if (!folder.exists()) folder.mkdirs();

        File dictFile = new File(DICT_FILE);
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(dictFile, newDict);
            log.info("[FontApiController] 폰트 JSON 사전 외부 업데이트 완료");
            return new ResponseEntity<>("SUCCESS", HttpStatus.OK);
        } catch (Exception e) {
            log.error("[FontApiController] 사전 저장 실패", e);
            return new ResponseEntity<>("FAIL", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}