package com.gucci.blog_service.tag.service;

import com.gucci.blog_service.post.domain.Post;
import com.gucci.blog_service.post.domain.dto.PostRequestDTO;
import com.gucci.blog_service.tag.domain.Tag;
import com.gucci.blog_service.tag.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    //태그 리스트 저장
    public void createTags(Post post, List<String> tagNameList) {
        //태그 생성
        List<Tag> tagToSave = tagNameList.stream().map(name -> Tag.builder().tagName(name).post(post).build()).toList();

        //태그 저장
        tagRepository.saveAll(tagToSave);
    }

    public List<String> getTagNamesByPost(Post post) {
        return tagRepository.findAllByPost(post).stream().map(Tag::getTagName).collect(Collectors.toList());
    }
    //태그 리스트 업데이트
    public void updateByTagNameList(Post post, List<String> newTagNameList) {
        //존재하는 태그
        List<String> existingTags = tagRepository.findAllByPost(post).stream().map(Tag::getTagName).toList();

        //삭제할 태그
        List<String> tagsToDelete = existingTags.stream().filter(tagName -> !newTagNameList.contains(tagName)).toList();

        //추가할 태그
        List<String> tagsToAdd = newTagNameList.stream().filter(tagName -> !existingTags.contains(tagName)).toList();

        // post에 연결된 tag중 tagsToDelete에 포함되는 태그 삭제
        tagRepository.deleteByPostAndTagNameIn(post, tagsToDelete);

        //태그 저장
        List<Tag> tags = tagsToAdd.stream().map(tagName -> Tag.builder().tagName(tagName).post(post).build()).toList();
        tagRepository.saveAll(tags);

    }
}
