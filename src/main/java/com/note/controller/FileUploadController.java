package com.note.controller;

import org.springframework.beans.factory.annotation.Value;
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

    // WebConfig가 /img/**로 서빙하는 경로와 반드시 같은 값을 가리켜야 한다 — 별도로 하드코딩하지 않음
    @Value("${galpi.media.root}")
    private String mediaRoot;

    @PostMapping
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") @NonNull MultipartFile file,
            @RequestParam("fileName") @NonNull String fileName,
            @RequestParam(value = "folder", defaultValue = "") String folder) {
        try {
            String basePath = mediaRoot.replace('\\', '/') + "/img/";

            // 프론트에서 folder=cover, folder=character 등을 보내면 해당 하위 폴더에 저장합니다.
            if (!folder.isEmpty()) {
                basePath += folder + "/";
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