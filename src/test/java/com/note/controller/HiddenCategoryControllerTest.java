package com.note.controller;

import com.note.domain.HiddenCategory;
import com.note.repository.HiddenCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// 숨김 분류(비밀 금고)의 원본은 DB다 — 메인 화면 필터와 금고 화면이 모두 이 API를 읽는다.
@ExtendWith(MockitoExtension.class)
class HiddenCategoryControllerTest {

    @Mock
    private HiddenCategoryRepository repo;

    private HiddenCategoryController controller;

    @BeforeEach
    void setUp() {
        controller = new HiddenCategoryController();
        ReflectionTestUtils.setField(controller, "hiddenCategoryRepository", repo);
    }

    @Test
    @DisplayName("저장된 숨김 분류 이름을 문자열 목록으로 돌려준다")
    void listsCategoryNames() {
        when(repo.findAll()).thenReturn(List.of(
                new HiddenCategory(2L, "MTL"),
                new HiddenCategory(4L, "NTL"),
                new HiddenCategory(6L, "여리코주의")));

        assertThat(controller.getHiddenCategories()).containsExactly("MTL", "NTL", "여리코주의");
    }

    @Test
    @DisplayName("아무것도 숨기지 않았으면 빈 목록")
    void emptyWhenNothingHidden() {
        when(repo.findAll()).thenReturn(List.of());

        assertThat(controller.getHiddenCategories()).isEmpty();
    }

    @Test
    @DisplayName("새 분류를 숨기면 저장한다")
    void hidesNewCategory() {
        when(repo.existsByCategoryName("NTL")).thenReturn(false);

        controller.hideCategory(new HiddenCategory(null, "NTL"));

        ArgumentCaptor<HiddenCategory> saved = ArgumentCaptor.forClass(HiddenCategory.class);
        verify(repo).save(saved.capture());
        assertThat(saved.getValue().getCategoryName()).isEqualTo("NTL");
        assertThat(saved.getValue().getId()).isNull();
    }

    @Test
    @DisplayName("이미 숨긴 분류를 또 숨겨도 중복 저장하지 않는다")
    void doesNotDuplicate() {
        when(repo.existsByCategoryName("NTL")).thenReturn(true);

        controller.hideCategory(new HiddenCategory(null, "NTL"));

        verify(repo, never()).save(any(HiddenCategory.class));
    }

    @Test
    @DisplayName("숨김 해제는 이름으로 삭제한다")
    void unhidesByName() {
        controller.unhideCategory("NTL");

        verify(repo).deleteByCategoryName("NTL");
    }
}
