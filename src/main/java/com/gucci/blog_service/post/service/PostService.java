package com.gucci.blog_service.post.service;

import com.gucci.blog_service.category.domain.Category;
import com.gucci.blog_service.category.service.CategoryService;
import com.gucci.blog_service.client.matching.client.MatchingServiceClient;
import com.gucci.blog_service.client.user.client.UserServiceClient;
import com.gucci.blog_service.comment.service.CommentRefService;
import com.gucci.blog_service.global.HtmlImageHelper;
import com.gucci.blog_service.global.JwtTokenHelper;
import com.gucci.blog_service.post.converter.PostResponseConverter;
import com.gucci.blog_service.post.domain.Post;
import com.gucci.blog_service.post.domain.PostDocument;
import com.gucci.blog_service.post.domain.dto.PostRequestDTO;
import com.gucci.blog_service.post.domain.dto.PostResponseDTO;
import com.gucci.blog_service.post.domain.enums.PostType;
import com.gucci.blog_service.post.repository.PostDocRepository;
import com.gucci.blog_service.post.repository.PostRepository;
import com.gucci.blog_service.tag.service.TagService;
import com.gucci.common.exception.CustomException;
import com.gucci.common.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final PostDocRepository postDocRepository;


    private final CommentRefService commentRefService;
    private final TagService tagService;
    private final CategoryService categoryService;
    private final PostSearchService postSearchService;
    private final PostQueryService postQueryService;

    private final UserServiceClient userServiceClient;
    private final MatchingServiceClient matchingServiceClient;

    private final JwtTokenHelper jwtTokenHelper;

    private final HtmlImageHelper htmlImageHelper;
    private final S3Service s3Service;

    private final static Integer pageSize = 10;

    /**
     * 게시글
     */

    /** 게시글 생성 */
    @Transactional
    public Post createPost(String token, PostRequestDTO.CreatePost dto) {
        Post savedPost;
        PostDocument savedPostDocument;
        Set<String> savedTags;

        Long userId = jwtTokenHelper.getUserIdFromToken(token);
        String authorNickName = jwtTokenHelper.getNicknameFromToken(token);

        //임시저장 글이었을 경우
        if (dto.getPostId() != null) {

            Post post = postQueryService.getPost(dto.getPostId());
            PostDocument postDocument = postQueryService.getPostDocument(post.getDocumentId());

            //태그 업데이트
            savedTags = tagService.updateByTagNameList(post, dto.getTagNameList());

            //img src objectKey 정제
            String processedContent = htmlImageHelper.extractObjectKeysFromPresignedUrls(dto.getContent());

            //postDoc 업데이트
            postDocument.updateContent(processedContent);
            savedPostDocument = postDocRepository.save(postDocument);

            Category category = categoryService.getCategory(dto.getCategoryCode());

            //post 업데이트
            String thumbnail = htmlImageHelper.extractFirstImageFromSavedContent(postDocument.getContent());
            post.update(dto.getTitle(), category, thumbnail, dto.getPostType());
            post.publish();
            savedPost = postRepository.save(post);
        }
        else {

            // 새로 작성한 글인 경우
            Category category = categoryService.getCategory(dto.getCategoryCode());

            //img src objectKey 정제
            String processedContent = htmlImageHelper.extractObjectKeysFromPresignedUrls(dto.getContent());
            PostDocument postDocument = PostDocument.builder()
                    .content(processedContent)
                    .build();
            savedPostDocument = postDocRepository.save(postDocument);

            String thumbnail = htmlImageHelper.extractFirstImageFromSavedContent(postDocument.getContent());

            Post post = Post.builder()
                    .view(0L)
                    .documentId(postDocument.getId())
                    .userId(userId)
                    .userNickName(authorNickName)
                    .thumbnail(thumbnail)
                    .title(dto.getTitle())
                    .isDraft(false)
                    .category(category)
                    .postType(dto.getPostType())
                    .build();
            savedPost = postRepository.save(post);

            //태그 생성, 이때 post 객체가 DB에서 조회된 영속 상태여야함
            tagService.createTags(savedPost, dto.getTagNameList(), userId);
            savedTags = tagService.getTagNamesByPost(post);
        }

        //elastic search에 인덱싱
        postSearchService.index(savedPost, savedPostDocument, savedTags);
        return savedPost;
    }

    /** 게시글 하나 상세조회  */
    @Transactional
    public PostResponseDTO.GetPostDetail getPostDetail(Long postId) {
        Post post = postQueryService.getPost(postId);
        PostDocument postDocument = postQueryService.getPostDocument(post.getDocumentId());
        Set<String> tagNameList = tagService.getTagNamesByPost(post);
        
        // 본문 HTML 내 이미지 objectKey-> url 변환
        String contentWithImageUrl = htmlImageHelper.convertImageKeysToPresignedUrls(postDocument.getContent());

        //조회시 조회수+1
        post.updateView();
        postSearchService.updateViewCount(post.getPostId(), post.getView());
        return PostResponseConverter.toGetPostDetailDto(post, contentWithImageUrl, tagNameList);
    }


    /** 팔로잉하는 사용자 글 조회 */
    public PostResponseDTO.GetPostList getFollowingPostList(String token, int page) {
        //user-service에서 following 목록 가져오기
        List<Long> followingUserIds = userServiceClient.getUserFollowingIds(token);

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());//최신순 정렬
        Page<Post> postPage = postRepository.findAllByPostTypeAndPostIdIn(PostType.POST, followingUserIds, pageable);

        //doc 조회
        List<String> docIds = postPage.stream().filter(Post::isDraft).map(Post::getDocumentId).toList();
        Map<String, PostDocument> postDocMap = postDocRepository.findAllById(docIds).stream()
                .collect(Collectors.toMap(PostDocument::getId, Function.identity())); // Function.identity() : 받은 값을 그대로 return

        //post로 dto만들기
        List<PostResponseDTO.GetPost> posts = postPage.stream()
                .filter(post -> !post.isDraft()) // 게시글만
                .map(
                post -> {
                    PostDocument postDocument = postDocMap.get(post.getDocumentId());
                    if (postDocument == null) {
                        throw new CustomException(ErrorCode.NOT_FOUND_POST);
                    }
                    Set<String> tagNameList = tagService.getTagNamesByPost(post);
                    String thumbnail = s3Service.getPresignedUrl(post.getThumbnail());

                    return PostResponseConverter.toGetPostDto(post, thumbnail, tagNameList);
                }
        ).toList();

        return PostResponseConverter.toGetPostList(postPage, posts);
    }


    /** 전체 글 조회 */
    public PostResponseDTO.GetPostList getPostAll(Integer page) {
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());//최신순 정렬
        Page<Post> postPage = postRepository.findAllByIsDraft(false, pageable);

        //doc 조회
        List<String> docIds = postPage.stream().filter(not(Post::isDraft)).map(Post::getDocumentId).toList();
        Map<String, PostDocument> postDocMap = postDocRepository.findAllById(docIds).stream()
                .collect(Collectors.toMap(PostDocument::getId, Function.identity())); // Function.identity() : 받은 값을 그대로 return

        List<PostResponseDTO.GetPost> postRes = postPage.stream()
                .filter(post -> !post.isDraft()) // 게시글만
                .map(
                        post -> {
                            PostDocument postDocument = postDocMap.get(post.getDocumentId());
                            if (postDocument == null) {
                                throw new CustomException(ErrorCode.NOT_FOUND_POST);
                            }
                            String thumbnail = s3Service.getPresignedUrl(post.getThumbnail());
                            Set<String> tagNameList = tagService.getTagNamesByPost(post);

                            return PostResponseConverter.toGetPostDto(post, thumbnail, tagNameList);
                        }
                ).toList();

        return PostResponseConverter.toGetPostList(postPage, postRes);

    }

    /** 카테고리별 글 조회 */
    public PostResponseDTO.GetPostList getPostListByCategory(Long categoryId, Integer page) {
        Category category = categoryService.getCategory(categoryId);

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());//최신순 정렬
        Page<Post> postPage = postRepository.findAllByCategoryAndIsDraft(category, false, pageable);

        //doc 조회
        List<String> docIds = postPage.stream().filter(not(Post::isDraft)).map(Post::getDocumentId).toList();
        Map<String, PostDocument> postDocMap = postDocRepository.findAllById(docIds).stream()
                .collect(Collectors.toMap(PostDocument::getId, Function.identity())); // Function.identity() : 받은 값을 그대로 return

        List<PostResponseDTO.GetPost> postRes = postPage.stream()
                .filter(post -> !post.isDraft()) // 게시글만
                .map(
                post -> {
                    PostDocument postDocument = postDocMap.get(post.getDocumentId());
                    if (postDocument == null) {
                        throw new CustomException(ErrorCode.NOT_FOUND_POST);
                    }
                    String thumbnail = s3Service.getPresignedUrl(post.getThumbnail());
                    Set<String> tagNameList = tagService.getTagNamesByPost(post);

                    return PostResponseConverter.toGetPostDto(post, thumbnail, tagNameList);
                }
        ).toList();

        return PostResponseConverter.toGetPostList(postPage, postRes);

    }

    /** 인기글 조회 */
    public PostResponseDTO.GetPostList getTrendingPostList(Integer page) {
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        Page<Post> postPage = postRepository.findAllTrending(weekAgo, PageRequest.of(page, pageSize)); //7일 이내 작성된 글 조회수 기준 정렬

        //doc 조회
        List<String> docIds = postPage.stream().filter(not(Post::isDraft)).map(Post::getDocumentId).toList();
        Map<String, PostDocument> postDocMap = postDocRepository.findAllById(docIds).stream()
                .collect(Collectors.toMap(PostDocument::getId, Function.identity())); // Function.identity() : 받은 값을 그대로 return


        List<PostResponseDTO.GetPost> postRes = postPage.stream()
                .filter(post -> !post.isDraft()) // 게시글만
                .map(
                post -> {
                    PostDocument postDocument = postDocMap.get(post.getDocumentId());
                    if (postDocument == null) {
                        throw new CustomException(ErrorCode.NOT_FOUND_POST);
                    }
                    String thumbnail = s3Service.getPresignedUrl(post.getThumbnail());
                    Set<String> tagNameList = tagService.getTagNamesByPost(post);

                    return PostResponseConverter.toGetPostDto(post, thumbnail, tagNameList);
                }
        ).toList();

        return PostResponseConverter.toGetPostList(postPage, postRes);

    }

    /** 추천글 조회 - 사용자 태그 기반으로 글 추천 */
    public PostResponseDTO.GetPostList getRecommendPostList(String token, Integer page) {
        Page<Post> postPage;
        Pageable pageable = PageRequest.of(page, pageSize);

        // 사용자 정보 조회
        Long userId = jwtTokenHelper.getUserIdFromToken(token);

        // 사용자 태그 가져오기
        List<String> tags = matchingServiceClient.getUserRepresentTags(token, userId);
        Set<String> userTags = Set.copyOf(tags);

        // 태그가 없는 경우 최신 글 추천
        if (userTags.isEmpty()) {
            postPage = postRepository.findAll(PageRequest.of(
                    pageable.getPageNumber(), pageable.getPageSize(), Sort.by("createdAt").descending()
            ));
        }
        else {
            // 사용자 태그와 일치하는 글 찾기 (태그 개수에 따라 점수 부여)
            Map<Post, Double> postScores = new HashMap<>();

            // 각 태그에 해당하는 글 조회 - 본인이 작성한 글은 제외
            for (String tag : userTags) {
                List<Post> postsWithTag = postRepository.findByTagsContaining(tag, userId
                );

                for (Post post : postsWithTag) {
                    // 태그 일치 점수 계산
                    Set<String> tagNames = tagService.getTagNamesByPost(post);
                    double tagMatchCount = calculateTagSimilarity(tagNames, userTags);

                    // 기존 점수에 추가
                    postScores.put(post, postScores.getOrDefault(post, 0.0) + tagMatchCount);
                }
            }

            // 점수 내림차순으로 정렬
            List<Post> sortedPost = postScores.entrySet().stream()
                    .sorted(Map.Entry.<Post, Double>comparingByValue().reversed())
                    .map(Map.Entry::getKey)
                    .toList();

            //page 만들기
            int total = sortedPost.size();
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), total);
            List<Post> content = start <= end ? sortedPost.subList(start, end) : List.of();
            postPage = new PageImpl<>(content, pageable, total);
        }

        // dto 만들기
        List<PostResponseDTO.GetPost> postRes = postPage.stream()
                .map(
                        post -> {
                            String thumbnail = s3Service.getPresignedUrl(post.getThumbnail());
                            Set<String> tagNameList = tagService.getTagNamesByPost(post);

                            return PostResponseConverter.toGetPostDto(post, thumbnail, tagNameList);
                        }
                ).toList();

        return PostResponseConverter.toGetPostList(postPage, postRes);
    }

    /** 게시글 수정 */
    @Transactional
    public Post updatePost(String token, Long postId, PostRequestDTO.UpdatePost dto) {
        Long userId = jwtTokenHelper.getUserIdFromToken(token);

        Post post;
        PostDocument postDocument;

        post = postQueryService.getPost(postId);
        postDocument = postQueryService.getPostDocument(post.getDocumentId());


        //권한 체크. 글 작성자만 수정 가능
        if (!post.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.NO_PERMISSION);
        }

        // 1. 임시저장 글이 있을 경우 삭제
        Post draft = postRepository.findByParentPostId(postId).orElse(null);
        if (draft != null) {
            PostDocument draftDocument = postQueryService.getPostDocument(draft.getDocumentId());

            //s3에서 사진 삭제
            List<String> objectKeys = htmlImageHelper.extractObjectKeysFromSavedContent(draftDocument.getContent());
            objectKeys.forEach(s3Service::deleteFile);

            //태그 Doc Post 삭제
            tagService.deleteAllByPost(draft);
            postRepository.delete(draft);
            postDocRepository.delete(draftDocument);
        }

        //img src objectKey 정제
        String processedContent = htmlImageHelper.extractObjectKeysFromPresignedUrls(dto.getContent());

        //Doc 업데이트
        postDocument.updateContent(processedContent);
        postDocRepository.save(postDocument); // 도큐먼트를 추적해서 변경된 필드를 저장하는 구조가 아니기 때문에, 반드시 save()를 직접 호출해야 반영

        //tag 업데이트
        tagService.updateByTagNameList(post, dto.getTagNameList());
        Set<String> tagNameList = tagService.getTagNamesByPost(post);

        Category category = categoryService.getCategory(dto.getCategoryCode());

        //Post 업데이트
        String thumbnail = htmlImageHelper.extractFirstImageFromSavedContent(postDocument.getContent());
        post.update(dto.getTitle(), category, thumbnail, PostType.POST);

        postSearchService.update(post, postDocument, tagNameList);
        return post;
    }

    /** 게시글 삭제 */
    @Transactional
    public void deletePost(String token, Long postId) {
        Long userId = jwtTokenHelper.getUserIdFromToken(token);

        Post post = postQueryService.getPost(postId);
        PostDocument postDocument = postQueryService.getPostDocument(post.getDocumentId());
        //권한 체크. 글 작성자만 삭제 가능
        if (!post.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.NO_PERMISSION);
        }

        //임시저장 글 삭제
        Post draft = postRepository.findByParentPostId(postId).orElse(null);
        if (draft != null) {
            PostDocument draftDoc = postQueryService.getPostDocument(draft.getDocumentId());

            //s3에서 사진 삭제
            List<String> objectKeys = htmlImageHelper.extractObjectKeysFromSavedContent(draftDoc.getContent());
            objectKeys.forEach(s3Service::deleteFile);

            //태그, Doc, Post 삭제

            tagService.deleteAllByPost(draft);
            postRepository.delete(draft);
            postDocRepository.delete(draftDoc);
        }

        //s3에서 사진 삭제
        List<String> objectKeys = htmlImageHelper.extractObjectKeysFromSavedContent(postDocument.getContent());
        objectKeys.forEach(s3Service::deleteFile);

        //엘라스틱서치, 댓글, 태그, Doc, Post 삭제
        postSearchService.delete(post.getPostId());
        tagService.deleteAllByPost(post);
        commentRefService.deleteAllByPost(post);
        postRepository.delete(post);
        postDocRepository.delete(postDocument);
    }


    /**
     * 임시저장글
     */

    /** 임시저장 생성 */
    @Transactional
    public Post createDraft(String token, PostRequestDTO.CreateDraft dto) {
        Long userId = jwtTokenHelper.getUserIdFromToken(token);
        String authorNickName = jwtTokenHelper.getNicknameFromToken(token);

        //글 발행 전 임시저장
        if (dto.getDraftPostId() == null && dto.getParentPostId() == null){
            //img src objectKey 정제
            String processedContent = htmlImageHelper.extractObjectKeysFromPresignedUrls(dto.getContent());

            PostDocument postDocument = PostDocument.builder()
                    .content(processedContent)
                    .build();
            postDocRepository.save(postDocument);

            Category category = categoryService.getCategory(dto.getCategoryCode());

            Post post = Post.builder()
                    .view(0L)
                    .documentId(postDocument.getId())
                    .userId(userId)
                    .userNickName(authorNickName)
                    .title(dto.getTitle())
                    .isDraft(true)
                    .category(category)
                    .build();
            Post savedPost = postRepository.save(post);

            //태그저장
            tagService.createTags(savedPost, dto.getTagNameList(), userId);

            return savedPost;
        }
        // 글 발행 후 임시저장
        else if (dto.getDraftPostId() == null){
            //img src objectKey 정제
            String processedContent = htmlImageHelper.extractObjectKeysFromPresignedUrls(dto.getContent());

            PostDocument postDocument = PostDocument.builder()
                    .content(processedContent)
                    .build();
            postDocRepository.save(postDocument);

            Category category = categoryService.getCategory(dto.getCategoryCode());

            Post post = Post.builder()
                    .view(0L)
                    .parentPostId(dto.getParentPostId())
                    .documentId(postDocument.getId())
                    .userNickName(authorNickName)
                    .userId(userId)
                    .title(dto.getTitle())
                    .isDraft(true)
                    .category(category)
                    .build();
            Post savedPost = postRepository.save(post);

            tagService.createTags(savedPost, dto.getTagNameList(), userId);
            return savedPost;
        }
        // 임시저장 글을 또 임시저장
        else {
            Post draft = postQueryService.getPost(dto.getDraftPostId());
            PostDocument draftDoc = postQueryService.getPostDocument(draft.getDocumentId()); // todo : NOT_FOUND_POST_CONTENT

            Category category = categoryService.getCategory(dto.getCategoryCode());

            tagService.updateByTagNameList(draft, dto.getTagNameList());

            //img src objectKey 정제
            String processedContent = htmlImageHelper.extractObjectKeysFromPresignedUrls(dto.getContent());

            draftDoc.updateContent(processedContent);
            postDocRepository.save(draftDoc); // 도큐먼트를 추적해서 변경된 필드를 저장하는 구조가 아니기 때문에, 반드시 save()를 직접 호출해야 반영
            draft.update(dto.getTitle(), category);

            return draft;
        }
    }

    /** 임시저장 하나 상세조회 */
    public PostResponseDTO.GetDraftDetail getDraftDetail(String token, Long postId) {
        Long userId = jwtTokenHelper.getUserIdFromToken(token);

        Post post = postQueryService.getPost(postId);
        PostDocument postDocument = postQueryService.getPostDocument(post.getDocumentId());

        if (!post.isDraft()) { // 임시저장 글이 아님
            throw new CustomException(ErrorCode.INVALID_ARGUMENT);
        }
        if (!post.getUserId().equals(userId)) { //사용자 != 임시저장 작성자
            throw new CustomException(ErrorCode.NO_PERMISSION);
        }

        Set<String> tagNameList = tagService.getTagNamesByPost(post);

        //image url과 함께 반환
        String contentWithImageUrl = htmlImageHelper.convertImageKeysToPresignedUrls(postDocument.getContent());

        return PostResponseConverter.toGetDraftDetailDto(post, contentWithImageUrl, tagNameList);
    }


    /** 임시저장 목록 조회 */
    public PostResponseDTO.GetDraftList getDraftList(String token) {
        Long userId = jwtTokenHelper.getUserIdFromToken(token);

        List<Post> postList = postRepository.findAllByUserId(userId);
        List<String> draftDocIds = postList.stream().filter(Post::isDraft).map(Post::getDocumentId).toList();
        Map<String, PostDocument> postDocMap = postDocRepository.findAllById(draftDocIds).stream()
                .collect(Collectors.toMap(PostDocument::getId, Function.identity())); // Function.identity() : 받은 값을 그대로 return


        List<PostResponseDTO.GetDraft> draftList = postList.stream()
                .filter(Post::isDraft) // draft만 통과
                .map(post -> {
                            PostDocument postDocument = postDocMap.get(post.getDocumentId());
                            if (postDocument == null) {
                                throw new CustomException(ErrorCode.NOT_FOUND_POST);
                            }
                            Set<String> tagNameList = tagService.getTagNamesByPost(post);

                            return PostResponseConverter.toGetDraftDto(post, postDocument.getContent(), tagNameList);
                        }
                ).toList();

        return PostResponseDTO.GetDraftList.builder()
                .draftList(draftList)
                .build();
    }

    /** 임시저장 삭제 */
    @Transactional
    public void deleteDraft(String token, Long postId) {
        Long userId = jwtTokenHelper.getUserIdFromToken(token);

        Post post = postQueryService.getPost(postId);
        PostDocument postDocument = postQueryService.getPostDocument(post.getDocumentId());
        //권한 체크. 글 작성자만 삭제 가능
        if (!post.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.NO_PERMISSION);
        }

        //s3에서 사진 삭제
        List<String> objectKeys = htmlImageHelper.extractObjectKeysFromSavedContent(postDocument.getContent());
        objectKeys.forEach(s3Service::deleteFile);

        tagService.deleteAllByPost(post);
        postRepository.delete(post);
        postDocRepository.delete(postDocument);
    }






    public Post getPostById(Long postId) {
        return postQueryService.getPost(postId);
    }

    @Transactional
    public void updateUserNickname(Long userId, String nickname) {
        List<Post> postList = postRepository.findAllByUserId(userId);
        postList.forEach(post -> post.update(nickname));

        List<String> postSearchIds = postList.stream().map(post -> Long.toHexString(post.getPostId())).toList();
        postSearchService.updateUserNickname(postSearchIds, nickname);
    }

    /**
     * 태그 유사도 계산 (Jaccard 유사도) : 유저 대표태그와 정확히 딱 맞을수록 점수 높음
     */
    private double calculateTagSimilarity(Set<String> postTags, Set<String> refTags) {
        if (postTags == null || refTags == null || postTags.isEmpty() || refTags.isEmpty()) {
            return 0.0;
        }

        Set<String> intersection = new HashSet<>(postTags);
        intersection.retainAll(refTags);

        Set<String> union = new HashSet<>(postTags);
        union.addAll(refTags);

        return (double) intersection.size() / union.size();
    }

}