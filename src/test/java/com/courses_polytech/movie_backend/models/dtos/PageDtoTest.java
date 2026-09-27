package com.courses_polytech.movie_backend.models.dtos;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageDtoTest {

    @Test
    void from_shouldCopyContentAndPaginationMetadata() {
        Page<String> page = new PageImpl<>(List.of("a", "b"), PageRequest.of(2, 2), 42);

        PageDto<String> dto = PageDto.from(page);

        assertThat(dto.getItems()).containsExactly("a", "b");
        assertThat(dto.getPage()).isEqualTo(2);
        assertThat(dto.getSize()).isEqualTo(2);
        assertThat(dto.getTotal()).isEqualTo(42);
    }

    @Test
    void from_shouldHandleEmptyPage() {
        PageDto<String> dto = PageDto.from(Page.empty(PageRequest.of(0, 20)));

        assertThat(dto.getItems()).isEmpty();
        assertThat(dto.getTotal()).isZero();
        assertThat(dto.getSize()).isEqualTo(20);
    }
}
