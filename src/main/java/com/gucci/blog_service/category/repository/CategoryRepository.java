package com.gucci.blog_service.category.repository;

import com.gucci.blog_service.category.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
