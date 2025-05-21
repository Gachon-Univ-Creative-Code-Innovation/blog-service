package com.gucci.blog_service.category.domain;

import com.gucci.blog_service.category.domain.type.CategoryType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Category {
    @Id
    private Long categoryId;

    @Enumerated(EnumType.STRING) // CategoryType을 DB에 문자열로 저장
    private CategoryType categoryType;
}
