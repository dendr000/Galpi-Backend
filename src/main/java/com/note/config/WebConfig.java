// 파일 위치: src/main/java/com/note/config/WebConfig.java
// 버전: v3.2.0
// 기능 요약: 외부 정적 파일(이미지, 폰트) 서버 경로 매핑 및 클라이언트 라우트 포워딩 제어 설정 파일

package com.note.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 유저 데이터 유실 방지를 위해 프로젝트 폴더 바깥의 독립 저장소를 바인딩한다.
    // 값 자체는 하드코딩하지 않고 galpi.media.root로 외부화 — exe 패키징(exe 프로파일)에서는
    // 사용자 데이터 폴더 기준 경로로 바뀐다. FileUploadController도 반드시 같은 값을 참조해야
    // 업로드-서빙 경로가 어긋나지 않는다.
    @Value("${galpi.media.root}")
    private String mediaRoot;

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        String root = mediaRoot.replace('\\', '/');

        // 1. 이미지 외부 경로 매핑
        registry.addResourceHandler("/img/**")
                .addResourceLocations("file:///" + root + "/img/");

        // 2. 폰트 외부 경로 매핑
        registry.addResourceHandler("/fonts/**")
                .addResourceLocations("file:///" + root + "/fonts/");
    }

    @Override
    public void addViewControllers(@NonNull ViewControllerRegistry registry) {
        // 시스템 구동 제어 컨텍스트: 루트 경로 진입 시 프론트엔드 빌드 산출물 메인 인덱스로 제어를 넘깁니다.
        registry.addViewController("/").setViewName("forward:/index.html");
        registry.addViewController("/memo").setViewName("forward:/memo.html");

        // React Router 클라이언트 라우트 폴백: 확장자가 없는 그 외 모든 경로(/work/15, /bulk 등)를
        // index.html로 넘겨 React Router가 처리하게 한다. 개발 서버(Vite)는 이 폴백을 기본 제공하지만,
        // 패키징된 exe는 Spring이 정적 파일을 직접 서빙하므로 이게 없으면 새로고침이나
        // window.location 이동 시 매핑되지 않은 경로가 그대로 404(Whitelabel Error)로 떨어진다.
        // /api/**나 실제 정적 파일(확장자 있는 경로)은 더 우선순위 높은 핸들러가 먼저 가로채므로
        // 이 폴백에 걸리지 않는다.
        registry.addViewController("/{path:[^\\.]*}").setViewName("forward:/index.html");
        registry.addViewController("/**/{path:[^\\.]*}").setViewName("forward:/index.html");
    }
}