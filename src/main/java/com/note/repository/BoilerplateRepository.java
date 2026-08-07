package com.note.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.note.domain.Boilerplate;

public interface BoilerplateRepository extends JpaRepository<Boilerplate, Long> {
}