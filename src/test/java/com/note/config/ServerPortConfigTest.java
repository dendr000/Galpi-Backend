// Copyright (c) dendr000. MIT License.
package com.note.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

// 개발 서버 포트가 8080(Oracle XE 등과 자주 충돌)이 아니라 18080으로 고정돼 있는지 확인한다.
// exe는 실행할 때 --server.port 로 포트를 직접 넘기므로 exe 프로파일에는 이 설정이 없어야 한다.
class ServerPortConfigTest {

    private static Properties load(String resource) throws IOException {
        Properties props = new Properties();
        try (InputStream in = ServerPortConfigTest.class.getClassLoader().getResourceAsStream(resource)) {
            assertThat(in).as(resource + " 를 찾을 수 있어야 한다").isNotNull();
            props.load(in);
        }
        return props;
    }

    @Test
    @DisplayName("기본 설정의 서버 포트는 18080")
    void defaultServerPortIs18080() throws IOException {
        assertThat(load("application.properties").getProperty("server.port")).isEqualTo("18080");
    }

    @Test
    @DisplayName("exe 프로파일은 포트를 직접 정하지 않는다(실행 인자로 받는다)")
    void exeProfileDoesNotFixPort() throws IOException {
        assertThat(load("application-exe.properties").getProperty("server.port")).isNull();
    }
}
