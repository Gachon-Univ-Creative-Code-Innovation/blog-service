package com.gucci.blog_service.global.service;

import com.gucci.blog_service.comment.service.CommentService;
import com.gucci.blog_service.global.dto.GlobalRequestDTO;
import com.gucci.blog_service.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GlobalOrchestrationService {
    private final PostService postService;
    private final CommentService commentService;

    public void updateUserNickname(GlobalRequestDTO.UpdateUserNickname request) {
        postService.updateUserNickname(request.getUserId(), request.getNewNickname());
        commentService.updateUserNickname(request.getUserId(), request.getNewNickname());
    }
}
