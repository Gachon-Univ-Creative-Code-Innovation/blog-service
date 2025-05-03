package com.gucci.blog_service.category.service;

import com.gucci.blog_service.category.domain.Category;
import com.gucci.blog_service.category.domain.type.CategoryType;
import com.gucci.blog_service.category.repository.CategoryRepository;
import com.gucci.common.exception.CustomException;
import com.gucci.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public Category getCategory(Long id) {
        if (id == null) {
            return categoryRepository.findById(CategoryType.ETC.getCode())
                    .orElseGet(() -> {
                        Category etcCategory = Category.builder()
                                .categoryId(CategoryType.ETC.getCode())
                                .categoryType(CategoryType.ETC)
                                .build();
                        return categoryRepository.save(etcCategory);
                    });
        }
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
    }

}
