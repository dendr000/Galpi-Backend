// Copyright (c) dendr000. MIT License.
package com.note.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.MapPropertySource;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// exe 는 Spring 이 정적 파일을 직접 서빙하므로, 화면(React Router) 주소는 모두 index.html 로 넘어가야 한다.
// 새로고침이나 직접 주소 진입에서 /memo 가 없는 memo.html 로 넘어가 오류 화면이 뜨던 문제를 막는다.
// DB 없이 WebConfig 의 주소 규칙만 작은 Spring 설정으로 시험한다.
class WebConfigRoutingTest {

    @Configuration
    @EnableWebMvc
    static class MvcSupport {
    }

    private AnnotationConfigWebApplicationContext context;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.getEnvironment().getPropertySources()
                .addFirst(new MapPropertySource("test", Map.of("galpi.media.root", "build/tmp/galpi-test-media")));
        context.register(MvcSupport.class, WebConfig.class);
        context.refresh();
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    @Test
    @DisplayName("루트 주소는 index.html 로 넘어간다")
    void rootForwardsToIndex() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk()).andExpect(forwardedUrl("/index.html"));
    }

    @Test
    @DisplayName("메모 화면(/memo)은 없는 memo.html 이 아니라 index.html 로 넘어간다")
    void memoForwardsToIndex() throws Exception {
        mvc.perform(get("/memo")).andExpect(status().isOk()).andExpect(forwardedUrl("/index.html"));
    }

    @Test
    @DisplayName("작품, 분류, 편집기 같은 화면 주소도 index.html 로 넘어간다")
    void clientRoutesForwardToIndex() throws Exception {
        for (String path : new String[] {"/work/15", "/category", "/edit", "/bulk", "/viewer/3"}) {
            mvc.perform(get(path)).andExpect(forwardedUrl("/index.html"));
        }
    }
}
