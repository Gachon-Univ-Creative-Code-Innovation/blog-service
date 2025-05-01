package com.gucci.blog_service.comment.service;

import com.gucci.blog_service.comment.domain.Comment;
import com.gucci.blog_service.comment.repository.CommentRepository;
import com.gucci.blog_service.post.domain.Post;
import com.gucci.blog_service.post.service.PostService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentRefService {
    private final CommentRepository commentRepository;

    @Transactional
    public void deleteAllByPost(Post post) {
        List<Comment> commentList = commentRepository.findAllByPost(post);
        commentRepository.deleteAll(commentList);
    }
}
