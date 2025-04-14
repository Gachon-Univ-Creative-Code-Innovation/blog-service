package com.gucci.blog_service.post.repository;

import com.gucci.blog_service.post.domain.PostDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PostDocRepository extends MongoRepository<PostDocument, String> {
}
