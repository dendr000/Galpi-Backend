package com.note.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin
@Slf4j
public class FileUploadController {

    @PostMapping
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") @NonNull MultipartFile file, 
            @RequestParam("fileName") @NonNull String fileName,
            @RequestParam(value = "folder", defaultValue = "") String folder) {
        try {
            // ★ 유저님의 프로젝트 폴더로 지정
            String basePath = "C:/dev/Galpi-React/src/main/resources/static/img/";
            
            // 프론트에서 folder=cover 라고 보내면 cover 폴더에 저장합니다.
            if ("cover".equals(folder)) {
                basePath += "cover/";
            }

            File dir = new File(basePath);
            if (!dir.exists()) dir.mkdirs();

            File targetFile = new File(basePath + fileName);
            file.transferTo(targetFile);

            log.info("[업로드 성공] 파일 위치: {}", targetFile.getAbsolutePath());
            return new ResponseEntity<>("SUCCESS", HttpStatus.OK);
        } catch (Exception e) {
            log.error("[업로드 실패]", e);
            return new ResponseEntity<>("FAILED", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}