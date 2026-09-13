// 파일 위치: src/main/java/com/note/repository/AuthGateRepository.java
package com.note.repository;

import com.note.domain.AuthGate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthGateRepository extends JpaRepository<AuthGate, String> {
}
