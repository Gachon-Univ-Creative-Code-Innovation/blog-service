package com.gucci.blog_service.category.helper;

import com.gucci.blog_service.category.domain.Category;
import com.gucci.blog_service.category.domain.type.CategoryType;
import com.gucci.blog_service.category.repository.CategoryRepository;
import jakarta.persistence.Column;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.Comment;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class CategoryInitializer implements CommandLineRunner {
    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) throws Exception {
        if(categoryRepository.count() == 0) {
            Arrays.stream(CategoryType.values()).forEach(type -> {
                categoryRepository.save(Category.builder().categoryId(type.getCode()).categoryType(type).build());
            });
        }
    }
}
