package com.gucci.blog_service.post.service;

import com.gucci.blog_service.comment.service.CommentRefService;
import com.gucci.blog_service.config.JwtTokenHelper;
import com.gucci.blog_service.post.domain.Post;
import com.gucci.blog_service.post.domain.PostDocument;
import com.gucci.blog_service.post.domain.dto.PostRequestDTO;
import com.gucci.blog_service.post.domain.dto.PostResponseDTO;
import com.gucci.blog_service.post.repository.PostDocRepository;
import com.gucci.blog_service.post.repository.PostRepository;
import com.gucci.blog_service.tag.service.TagService;
import com.gucci.common.exception.CustomException;
import com.gucci.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {
    @InjectMocks
    private PostService postService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostDocRepository postDocRepository;

    @Mock
    private CommentRefService commentRefService;

    @Mock
    private TagService tagService;

    @Mock
    private JwtTokenHelper jwtTokenHelper;

    private final Long userId = 1L;
    private final String token = "Bearer test-token";

    /**
     * 게시글
     */
    @Test
    @DisplayName("게시글 생성 : draftPostId != null (임시저장 후 발행)")
    void createPostAfterDraft() throws Exception {
        PostRequestDTO.createPost dto = PostRequestDTO.createPost.builder()
                .postId(1L)
                .title("제목")
                .content("내용")
                .build();
        Post draft = Post.builder()
                .postId(dto.getPostId())
                .documentId("draftPostId")
                .title(dto.getTitle())
                .isDraft(true)
                .build();
        PostDocument draftPostDocument = PostDocument.builder()
                .id("draftPostId")
                .content(dto.getContent())
                .build();
        Post post = Post.builder()
                .postId(draft.getPostId())
                .title(draft.getTitle())
                .isDraft(false)
                .build();

        Mockito.when(jwtTokenHelper.getUserIdFromToken(token)).thenReturn(userId);
        Mockito.when(postRepository.findById(dto.getPostId())).thenReturn(Optional.of(draft));
        Mockito.when(postDocRepository.findById(draft.getDocumentId())).thenReturn(Optional.of(draftPostDocument));
        Mockito.when(postRepository.save(any(Post.class))).thenReturn(post);

        Post result = postService.createPost(token, dto);

        assertThat(result.getPostId()).isEqualTo(dto.getPostId());
        assertThat(result.getTitle()).isEqualTo(dto.getTitle());
        assertThat(result.isDraft()).isFalse(); //발행 체크
    }

    @Test
    @DisplayName("게시글 생성 : draftPostId == null (임시저장 하지 않고 발행)")
    void createPostTest() throws Exception {
        PostRequestDTO.createPost dto = PostRequestDTO.createPost.builder()
                .postId(null)
                .title("제목")
                .content("내용")
                .build();

        PostDocument savedPostDocument = PostDocument.builder()
                .content(dto.getContent())
                .build();

        Post savedPost = Post.builder()
                .view(0L)
                .documentId(savedPostDocument.getId())
                .userId(userId)
                .title(dto.getTitle())
                .isDraft(false)
                .build();

        Mockito.when(jwtTokenHelper.getUserIdFromToken(token)).thenReturn(userId);
        Mockito.when(postDocRepository.save(any(PostDocument.class))).thenReturn(savedPostDocument);
        Mockito.when(postRepository.save(any(Post.class))).thenReturn(savedPost);

        Post result = postService.createPost(token, dto);

        assertThat(result.getPostId()).isEqualTo(dto.getPostId());
        assertThat(result.getTitle()).isEqualTo(dto.getTitle());
        assertThat(result.isDraft()).isFalse(); //발행 체크
        assertThat(result.getDocumentId()).isEqualTo(savedPostDocument.getId());

        verify(postDocRepository, times(1)).save(any(PostDocument.class));
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    @DisplayName("게시글 상세 조회")
    void getPostDetailTest() {
        Long postId = 1L;
        PostDocument postDoc = PostDocument.builder()
                .id("아이디")
                .content("내용")
                .build();
        Post post = Post.builder()
                .view(0L)
                .documentId(postDoc.getId())
                .userId(userId)
                .title("제목")
                .isDraft(false)
                .build();

        Mockito.when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        Mockito.when(postDocRepository.findById(postDoc.getId())).thenReturn(Optional.of(postDoc));

        PostResponseDTO.GetPostDetail result = postService.getPostDetail(postId);

        assertThat(result.getPostId()).isEqualTo(post.getPostId());
        assertThat(result.getAuthorId()).isEqualTo(post.getUserId());
        assertThat(result.getTitle()).isEqualTo(post.getTitle());
        assertThat(result.getContent()).isEqualTo(postDoc.getContent());
        assertThat(result.getView()).isEqualTo(post.getView());
    }

    @Test
    @DisplayName("게시글 수정 : 임시저장 글로 수정")
    void updatePostUseDraftTest() {
        //given
        long draftId = 10L;
        String draftDocId = "draftDoc";
        long postId = 1L;
        String postDocId = "postDoc";

        PostRequestDTO.updatePost request = PostRequestDTO.updatePost.builder()
                .content("수정 내용")
                .title("수정 제목")
                .build();

        Post post = Post.builder()
                .postId(postId)
                .userId(userId)
                .documentId(postDocId)
                .title("원본 제목")
                .isDraft(false)
                .build();
        PostDocument postDoc = PostDocument.builder()
                .id(postDocId)
                .content("원본 내용")
                .build();

        Post draft = Post.builder()
                .postId(draftId)
                .documentId(postDocId)
                .title("제목")
                .isDraft(false)
                .build();
        PostDocument draftDoc = PostDocument.builder()
                .id(draftDocId)
                .content("내용")
                .build();

        //목객체
        Mockito.when(jwtTokenHelper.getUserIdFromToken(token)).thenReturn(userId);
        //원본 찾고
        Mockito.when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        Mockito.when(postDocRepository.findById(post.getDocumentId())).thenReturn(Optional.of(postDoc));
        //임시 저장 찾고
        Mockito.when(postRepository.findByParentPostId(postId)).thenReturn(Optional.of(draft));
        Mockito.when(postDocRepository.findById(draft.getDocumentId())).thenReturn(Optional.of(draftDoc));

        //when
        Post result = postService.updatePost(token, postId, request);

        //then
        assertThat(result.getPostId()).isEqualTo(postId);
        assertThat(result.getTitle()).isEqualTo(request.getTitle());
        assertThat(result.getDocumentId()).isEqualTo(postDocId);

        //테스트 대상(Service 메서드)이 정말로 특정 동작을 특정 횟수만큼 수행했는지 확인
        verify(postRepository, times(1)).delete(draft);
        verify(postDocRepository, times(1)).delete(draftDoc);
        verify(postDocRepository, times(1)).save(any(PostDocument.class));
    }

    @Test
    @DisplayName("게시글 수정 : 임시저장 없이 수정")
    void updatePostTest(){
        //given
        long postId = 1L;
        String postDocId = "postDoc";

        PostRequestDTO.updatePost request = PostRequestDTO.updatePost.builder()
                .content("수정 내용")
                .title("수정 제목")
                .build();

        Post post = Post.builder()
                .postId(postId)
                .userId(userId)
                .documentId(postDocId)
                .title("원본 제목")
                .isDraft(false)
                .build();
        PostDocument postDoc = PostDocument.builder()
                .id(postDocId)
                .content("원본 내용")
                .build();

        Mockito.when(jwtTokenHelper.getUserIdFromToken(token)).thenReturn(userId);
        Mockito.when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        Mockito.when(postDocRepository.findById(postDocId)).thenReturn(Optional.of(postDoc));

        //when
        Post result = postService.updatePost(token, postId, request);

        //then
        assertThat(result.getPostId()).isEqualTo(postId);
        assertThat(result.getTitle()).isEqualTo(request.getTitle());

        verify(postDocRepository, times(1)).save(any(PostDocument.class));
    }

    @Test
    @DisplayName("임시저장 수정 : 작성자가 아닐 때")
    void updatePostNoPermissionTest() {
        //given
        long postId = 1L;
        String postDocId = "postDoc";
        long postUserId = 10L;

        PostRequestDTO.updatePost request = PostRequestDTO.updatePost.builder()
                .build();

        Post post = Post.builder()
                .postId(postUserId)
                .userId(10L)
                .documentId(postDocId)
                .title("원본 제목")
                .isDraft(false)
                .build();
        PostDocument postDoc = PostDocument.builder()
                .id(postDocId)
                .content("원본 내용")
                .build();

        Mockito.when(jwtTokenHelper.getUserIdFromToken(token)).thenReturn(userId);
        Mockito.when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        Mockito.when(postDocRepository.findById(postDocId)).thenReturn(Optional.of(postDoc));

        assertThatThrownBy(() -> postService.updatePost(token, postId, request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.NO_PERMISSION.getMessage());
    }

    @Test
    @DisplayName("게시글 삭제")
    void deletePostTest(){
        // given
        Long postId = 1L;
        String postDocId = "postDoc";

        Post post = Post.builder()
                .postId(postId)
                .userId(userId)
                .documentId(postDocId)
                .build();


        PostDocument document = PostDocument.builder()
                .id(postDocId)
                .content("내용")
                .build();

        // mock 설정
        Mockito.when(jwtTokenHelper.getUserIdFromToken(token)).thenReturn(userId);
        Mockito.when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        Mockito.when(postDocRepository.findById(postDocId)).thenReturn(Optional.of(document));
        Mockito.when(postRepository.findByParentPostId(postId)).thenReturn(Optional.empty());

        // when
        postService.deletePost(token, postId);

        // then
        verify(commentRefService).deleteAllByPost(post);
        verify(postRepository).delete(post);
        verify(postDocRepository).delete(document);
    }

    @Test
    @DisplayName("게시글 삭제 : 임시저장 있을 때")
    void deletePostDraftTest(){
        // given
        Long postId = 1L;
        String postDocId = "postDoc";
        Long draftPostId = 2L;
        String draftDocId = "draftDocId";

        Post post = Post.builder()
                .postId(postId)
                .userId(userId)
                .documentId(postDocId)
                .build();
        PostDocument document = PostDocument.builder()
                .id(postDocId)
                .content("내용")
                .build();

        // 임시저장 글
        Post draft = Post.builder()
                .postId(draftPostId)
                .documentId(draftDocId)
                .isDraft(true)
                .build();
        PostDocument draftDoc = PostDocument.builder()
                .id(draftDocId)
                .build();

        // mock 설정
        Mockito.when(jwtTokenHelper.getUserIdFromToken(token)).thenReturn(userId);
        Mockito.when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        Mockito.when(postDocRepository.findById(postDocId)).thenReturn(Optional.of(document));
        Mockito.when(postRepository.findByParentPostId(postId)).thenReturn(Optional.of(draft));
        Mockito.when(postDocRepository.findById(draftDocId)).thenReturn(Optional.of(draftDoc));

        // when
        postService.deletePost(token, postId);

        // then
        verify(commentRefService).deleteAllByPost(post);
        verify(postRepository).delete(draft);
        verify(postDocRepository).delete(draftDoc);
        verify(postRepository).delete(post);
        verify(postDocRepository).delete(document);
    }

    @Test
    @DisplayName("게시글 삭제 : 권한 없음")
    public void deletePostNoPermissionTest(){
        // given
        Long postId = 1L;
        String postDocId = "postDoc";

        Post post = Post.builder()
                .postId(postId)
                .userId(10L)
                .documentId(postDocId)
                .build();
        PostDocument document = PostDocument.builder()
                .id(postDocId)
                .content("내용")
                .build();


        // mock 설정
        Mockito.when(jwtTokenHelper.getUserIdFromToken(token)).thenReturn(userId);
        Mockito.when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        Mockito.when(postDocRepository.findById(postDocId)).thenReturn(Optional.of(document));

        // when then
        assertThatThrownBy(() -> postService.deletePost(token, postId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.NO_PERMISSION.getMessage());
    }



    /**
     * 임시저장글
     */
    @Test
    @DisplayName("임시저장 생성 : 게시글 발행 전")
    public void createDraftBeforePublishTest(){
        PostRequestDTO.createDraft request = PostRequestDTO.createDraft.builder()
                .draftPostId(null)
                .parentPostId(null)
                .content("내용")
                .title("제목")
                .build();

        Post draft = Post.builder()
                .userId(userId)
                .isDraft(true)
                .title(request.getTitle())
                .build();
        PostDocument draftDoc = PostDocument.builder()
                .content(request.getContent())
                .build();
        Mockito.when(jwtTokenHelper.getUserIdFromToken(token)).thenReturn(userId);
        Mockito.when(postDocRepository.save(any(PostDocument.class))).thenReturn(draftDoc);
        Mockito.when(postRepository.save(any(Post.class))).thenReturn(draft);

        Post result = postService.createDraft(token, request);

        assertThat(result.isDraft()).isTrue();
        assertThat(result.getTitle()).isEqualTo(request.getTitle());

        verify(postRepository).save(any(Post.class));
        verify(postDocRepository).save(any(PostDocument.class));
    }

    @Test
    @DisplayName("임시저장 생성 : 게시글 발행 후")
    public void createDraftAfterPublishTest(){
        PostRequestDTO.createDraft request = PostRequestDTO.createDraft.builder()
                .draftPostId(null)
                .parentPostId(10L)
                .content("내용")
                .title("제목")
                .build();

        Post draft = Post.builder()
                .userId(userId)
                .parentPostId(request.getParentPostId())
                .isDraft(true)
                .title(request.getTitle())
                .build();
        PostDocument draftDoc = PostDocument.builder()
                .content(request.getContent())
                .build();

        Mockito.when(jwtTokenHelper.getUserIdFromToken(token)).thenReturn(userId);
        Mockito.when(postDocRepository.save(any(PostDocument.class))).thenReturn(draftDoc);
        Mockito.when(postRepository.save(any(Post.class))).thenReturn(draft);

        Post result = postService.createDraft(token, request);

        assertThat(result.isDraft()).isTrue();
        assertThat(result.getTitle()).isEqualTo(request.getTitle());
        assertThat(result.getParentPostId()).isEqualTo(request.getParentPostId());

        verify(postRepository).save(any(Post.class));
        verify(postDocRepository).save(any(PostDocument.class));
    }

    @Test
    @DisplayName("임시저장 생성 : 임시저장 글을 또 임시저장")
    public void createDraftTest() {
        String postDocId = "postDoc";
        PostRequestDTO.createDraft request = PostRequestDTO.createDraft.builder()
                .draftPostId(1L)
                .parentPostId(10L)
                .content("내용")
                .title("제목")
                .build();

        Post draft = Post.builder()
                .postId(request.getDraftPostId())
                .userId(userId)
                .parentPostId(request.getParentPostId())
                .documentId(postDocId)
                .isDraft(true)
                .title(request.getTitle())
                .build();
        PostDocument draftDoc = PostDocument.builder()
                .id(postDocId)
                .content(request.getContent())
                .build();

        Mockito.when(jwtTokenHelper.getUserIdFromToken(token)).thenReturn(userId);
        Mockito.when(postRepository.findById(request.getDraftPostId())).thenReturn(Optional.of(draft));
        Mockito.when(postDocRepository.findById(draft.getDocumentId())).thenReturn(Optional.of(draftDoc));

        Post result = postService.createDraft(token, request);

        assertThat(result.isDraft()).isTrue();
        assertThat(result.getTitle()).isEqualTo(request.getTitle());
        assertThat(result.getParentPostId()).isEqualTo(request.getParentPostId());
        assertThat(result.getPostId()).isEqualTo(request.getDraftPostId());
    }

    @Test
    @DisplayName("임시저장 상세조회")
    public void getDraftTest(){
        Long postId = 1L;
        String postDocId = "postDoc";

        Post draft = Post.builder()
                .postId(postId)
                .userId(userId)
                .documentId(postDocId)
                .isDraft(true)
                .title("제목")
                .build();
        PostDocument draftDoc = PostDocument.builder()
                .id(postDocId)
                .content("내용")
                .build();

        Mockito.when(jwtTokenHelper.getUserIdFromToken(token)).thenReturn(userId);
        Mockito.when(postRepository.findById(postId)).thenReturn(Optional.of(draft));
        Mockito.when(postDocRepository.findById(draft.getDocumentId())).thenReturn(Optional.of(draftDoc));

        PostResponseDTO.GetDraftDetail result = postService.getDraftDetail(token, postId);

        assertThat(result.getDraftPostId()).isEqualTo(postId);
        assertThat(result.getAuthorId()).isEqualTo(userId);
        assertThat(result.getTitle()).isEqualTo(draft.getTitle());
        assertThat(result.getContent()).isEqualTo(draftDoc.getContent());
        assertThat(result.getParentPostId()).isEqualTo(draft.getParentPostId());
    }

    @Test
    @DisplayName("임시저장 상세조회 : 임시저장 글이 아님")
    public void getDraftInvalidArgumentTest() {
        Long postId = 1L;
        String postDocId = "postDoc";

        Post draft = Post.builder()
                .postId(postId)
                .userId(userId)
                .documentId(postDocId)
                .isDraft(false)
                .title("제목")
                .build();
        PostDocument draftDoc = PostDocument.builder()
                .id(postDocId)
                .content("내용")
                .build();

        Mockito.when(jwtTokenHelper.getUserIdFromToken(token)).thenReturn(userId);
        Mockito.when(postRepository.findById(postId)).thenReturn(Optional.of(draft));
        Mockito.when(postDocRepository.findById(draft.getDocumentId())).thenReturn(Optional.of(draftDoc));

        assertThatThrownBy(() -> postService.getDraftDetail(token, postId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.INVALID_ARGUMENT.getMessage());
    }

    @Test
    @DisplayName("임시저장 : 임시저장 작성자가 아님")
    public void getDraftNoPermissionTest() {
        Long postId = 1L;
        String postDocId = "postDoc";

        Post draft = Post.builder()
                .postId(postId)
                .userId(10L)
                .documentId(postDocId)
                .isDraft(true)
                .title("제목")
                .build();
        PostDocument draftDoc = PostDocument.builder()
                .id(postDocId)
                .content("내용")
                .build();

        Mockito.when(jwtTokenHelper.getUserIdFromToken(token)).thenReturn(userId);
        Mockito.when(postRepository.findById(postId)).thenReturn(Optional.of(draft));
        Mockito.when(postDocRepository.findById(draft.getDocumentId())).thenReturn(Optional.of(draftDoc));

        assertThatThrownBy(() -> postService.getDraftDetail(token, postId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.NO_PERMISSION.getMessage());
    }

    @Test
    @DisplayName("임시저장 리스트 조회")
    public void getDraftListTest() {
        Post draft = Post.builder()
                .postId(1L)
                .userId(10L)
                .documentId("postDocId")
                .isDraft(true)
                .title("제목")
                .build();
        Post draft2 = Post.builder()
                .postId(2L)
                .userId(10L)
                .documentId("postDocId2")
                .isDraft(true)
                .title("제목")
                .build();
        Post nonDraftPost = Post.builder()
                .postId(103L)
                .userId(userId)
                .title("Published Post")
                .documentId("doc3")
                .isDraft(false)
                .build();

        PostDocument draftDoc = PostDocument.builder()
                .id("postDocId")
                .content("내용")
                .build();

        PostDocument draftDoc2 = PostDocument.builder()
                .id("postDocId2")
                .content("내용")
                .build();

        List<Post> allPosts = List.of(draft, draft2, nonDraftPost);
        List<PostDocument> allPostDocuments = List.of(draftDoc, draftDoc2);

        Mockito.when(jwtTokenHelper.getUserIdFromToken(token)).thenReturn(userId);
        Mockito.when(postRepository.findAllByUserId(anyLong())).thenReturn(allPosts);
        Mockito.when(postDocRepository.findAllById(anyList())).thenReturn(allPostDocuments);

        PostResponseDTO.GetDraftList result = postService.getDraftList(token);

        assertThat(result.getDraftList().get(0).getDraftPostId()).isEqualTo(draft.getPostId());
        assertThat(result.getDraftList().get(1).getDraftPostId()).isEqualTo(draft2.getPostId());
        assertThat(result.getDraftList().size()).isEqualTo(2);
    }


    @Test
    @DisplayName("임시저장 삭제")
    public void deleteDraftTest() {
        Long postId = 1L;
        String postDocId = "postDoc";
        Post draft = Post.builder()
                .postId(postId)
                .userId(userId)
                .documentId(postDocId)
                .build();
        PostDocument draftDoc = PostDocument.builder()
                .id(postDocId)
                .build();

        Mockito.when(jwtTokenHelper.getUserIdFromToken(token)).thenReturn(userId);
        Mockito.when(postRepository.findById(postId)).thenReturn(Optional.of(draft));
        Mockito.when(postDocRepository.findById(draft.getDocumentId())).thenReturn(Optional.of(draftDoc));

        //when
        postService.deleteDraft(token, postId);

        //then
        verify(postRepository).delete(draft);
        verify(postDocRepository).delete(draftDoc);
    }

    @Test
    @DisplayName("임시저장 삭제 : 임시저장 작성자가 아님")
    public void deleteDraftNoPermissionTest() {
        Long postId = 1L;
        String postDocId = "postDoc";
        Post draft = Post.builder()
                .postId(postId)
                .userId(10L)
                .documentId(postDocId)
                .build();
        PostDocument draftDoc = PostDocument.builder()
                .id(postDocId)
                .build();

        Mockito.when(jwtTokenHelper.getUserIdFromToken(token)).thenReturn(userId);
        Mockito.when(postRepository.findById(postId)).thenReturn(Optional.of(draft));
        Mockito.when(postDocRepository.findById(draft.getDocumentId())).thenReturn(Optional.of(draftDoc));

        assertThatThrownBy(() -> postService.deleteDraft(token, postId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.NO_PERMISSION.getMessage());
    }
}
