package com.note.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 1. 이 클래스가 데이터베이스의 테이블과 1:1로 매핑되는 JPA 엔티티임을 선언합니다.
@Entity
// 2. 롬복을 사용하여 Getter, Setter, toString 등을 자동으로 생성합니다.
@Data
// 3. 모든 필드 값을 파라미터로 받는 생성자를 자동으로 만듭니다.
@AllArgsConstructor
// 4. 파라미터가 없는 기본 생성자를 자동으로 만듭니다. (JPA 필수 요건)
@NoArgsConstructor
// 5. 이 클래스 내부에서 log.info()를 사용할 수 있도록 로거를 주입합니다.
@Slf4j
public class Note {
    
    // 6. 이 필드가 데이터베이스 테이블의 기본 키(Primary Key)임을 지정합니다.
    @Id
    // 7. 기본 키의 값을 MySQL의 AUTO_INCREMENT처럼 데이터베이스가 알아서 1씩 증가시키도록 설정합니다.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // 8. 노트(또는 작품)의 제목을 저장하는 문자열 변수입니다.
    private String title;
    
    // 9. 기존 Book의 author(저자) 대신, 노트의 본문 내용을 담을 수 있도록 content(내용)로 이름을 변경했습니다.
    private String content;
    
}