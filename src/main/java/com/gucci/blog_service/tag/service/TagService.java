package com.gucci.blog_service.tag.service;

import com.gucci.blog_service.post.domain.Post;
import com.gucci.blog_service.post.domain.dto.PostRequestDTO;
import com.gucci.blog_service.tag.domain.Tag;
import com.gucci.blog_service.tag.repository.TagRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    /**태그 리스트 저장*/
    @Transactional
    public List<Tag> createTags(Post post, List<String> tagNameList, Long userId) {
        //태그 생성
        List<Tag> tagToSave = Optional.ofNullable(tagNameList)
                .orElse(Collections.emptyList())
                .stream().map(name -> Tag.builder()
                        .tagName(name)
                        .userId(userId)
                        .post(post)
                        .build()
                ).toList();

        //태그 저장
        return tagRepository.saveAll(tagToSave);
    }

    /**태그 이름 조회*/
    public List<String> getTagNamesByPost(Post post) {
        return tagRepository.findAllByPost(post).stream().map(Tag::getTagName).collect(Collectors.toList());
    }

    /**태그 리스트 업데이트*/
    public void updateByTagNameList(Post post, List<String> newTagNameList) {
        if (newTagNameList == null) {
            newTagNameList = Collections.emptyList();
        }
        //존재하는 태그
        List<Tag> existingTags = tagRepository.findAllByPost(post);
        List<String> existingTagNames = existingTags.stream().map(Tag::getTagName).toList();

        //삭제할 태그
        List<String> finalNewTagNameList = newTagNameList; // final 변수로 선언
        List<String> tagsToDelete = existingTagNames.stream().filter(tagName -> !finalNewTagNameList.contains(tagName)).toList();

        //추가할 태그
        List<String> tagsToAdd = finalNewTagNameList.stream().filter(tagName -> !existingTagNames.contains(tagName)).toList();

        // post에 연결된 tag중 tagsToDelete에 포함되는 태그 삭제
        if (!tagsToDelete.isEmpty()) {
            tagRepository.deleteByPostAndTagNameIn(post, tagsToDelete);
        }

        //태그 저장
        if (!tagsToAdd.isEmpty()) {
            List<Tag> tags = tagsToAdd.stream()
                    .map(tagName -> Tag.builder().tagName(tagName).post(post).build())
                    .toList();
            tagRepository.saveAll(tags);
        }
    }

    /**게시글로 태그 삭제*/
    @Transactional
    public void deleteAllByPost(Post post) {
        tagRepository.deleteAllByPost(post);
    }
}
