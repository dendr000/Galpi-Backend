// 파일 위치: src/main/java/com/note/config/WebConfig.java
// 버전: v3.2.0
// 기능 요약: 외부 정적 파일(이미지, 폰트) 서버 경로 매핑 및 클라이언트 라우트 포워딩 제어 설정 파일

package com.note.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // 프로그램 실행 로그 출력 대용 주석: 정적 파일 자원 탐색 위치를 빌드 프로세스와 무관한 독립 외부 절대 경로로 변경하여 안전성을 확보합니다.
        // 유저 데이터 유실 방지를 위해 C드라이브 하위에 외부 저장소를 바인딩합니다. (폴더가 없다면 윈도우 탐색기에서 해당 위치에 생성 필요)
        
        // 1. 이미지 외부 경로 매핑
        registry.addResourceHandler("/img/**")
                .addResourceLocations("file:///C:/dev/Galpi/Galpi-media/img/");
                
        // 2. 폰트 외부 경로 매핑
        registry.addResourceHandler("/fonts/**")
                .addResourceLocations("file:///C:/dev/Galpi/Galpi-media/fonts/");
    }

    @Override
    public void addViewControllers(@NonNull ViewControllerRegistry registry) {
        // 시스템 구동 제어 컨텍스트: 루트 경로 진입 시 프론트엔드 빌드 산출물 메인 인덱스로 제어를 넘깁니다.
        registry.addViewController("/").setViewName("forward:/index.html");
        registry.addViewController("/memo").setViewName("forward:/memo.html");
    }
}