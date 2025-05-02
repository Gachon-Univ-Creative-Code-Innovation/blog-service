package com.gucci.blog_service.tag.service;

import com.gucci.blog_service.post.domain.Post;
import com.gucci.blog_service.tag.domain.Tag;
import com.gucci.blog_service.tag.repository.TagRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class TagServiceTest {
    @InjectMocks
    private TagService tagService;

    @Mock
    private TagRepository tagRepository;

    private final Post post = Post.builder().postId(1L).build();


    @Test
    @DisplayName("태그 리스트 업데이트 테스트 : 일반적인 상태")
    void updateByTagNameListTest() throws Exception {
        // given
        List<Tag> existingTags = List.of(
                Tag.builder().tagName("tag1").post(post).build(),
                Tag.builder().tagName("tag2").post(post).build()
        );
        List<String> newTagNameList = List.of("tag1", "tag3");

        Mockito.when(tagRepository.findAllByPost(post)).thenReturn(existingTags);

        //when
        tagService.updateByTagNameList(post, newTagNameList);

        //then
        verify(tagRepository).deleteByPostAndTagNameIn(eq(post), eq(List.of("tag2"))); //parameter가 주어진 조건과 같은지 확인

        //saveAll() 메서드에 어떤 태그 리스트가 실제로 저장되었는지 가져오기 위한 도구
        ArgumentCaptor<List<Tag>> captor = ArgumentCaptor.forClass(List.class);
        verify(tagRepository).saveAll(captor.capture());

        List<Tag> savedTags = captor.getValue();
        assertThat(savedTags.size()).isEqualTo(1);
        assertThat(savedTags.get(0).getTagName()).isEqualTo("tag3");
    }

    @Test
    @DisplayName("태그 리스트 업데이트 테스트 : newTag - 기존 태그와 동일")
    void updateByTagNameList_sameTagTest() throws Exception {
        //given
        List<Tag> existingTags = List.of(
                Tag.builder().tagName("tag1").post(post).build(),
                Tag.builder().tagName("tag2").post(post).build()
        );
        List<String> newTagNameList = List.of("tag1", "tag2");

        Mockito.when(tagRepository.findAllByPost(post)).thenReturn(existingTags);

        //when
        tagService.updateByTagNameList(post, newTagNameList);

        //then
        verify(tagRepository, never()).deleteByPostAndTagNameIn(any(), any());
        verify(tagRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("태그 리스트 업데이트 테스트 : newTag - 빈리스트")
    void updateByTagName_emptyListTest() throws Exception {
        //given
        List<Tag> existingTags = List.of(
                Tag.builder().tagName("tag1").post(post).build(),
                Tag.builder().tagName("tag2").post(post).build()
        );
        List<String> newTagNames = List.of(); // 빈 리스트

        Mockito.when(tagRepository.findAllByPost(post)).thenReturn(existingTags);

        //when
        tagService.updateByTagNameList(post, newTagNames);

        //then
        verify(tagRepository).deleteByPostAndTagNameIn(eq(post), eq(List.of("tag1", "tag2")));
        verify(tagRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("태그 리스트 업데이트 테스트 : existingTag - x, newTag - 추가")
    void updateByTagName_emptyExistTagTest() throws Exception {
        //given
        List<String> newTagNames = List.of("tag1", "tag2");
        Mockito.when(tagRepository.findAllByPost(post)).thenReturn(List.of());

        //when
        tagService.updateByTagNameList(post, newTagNames);

        //then
        ArgumentCaptor<List<Tag>> captor = ArgumentCaptor.forClass(List.class);
        verify(tagRepository).saveAll(captor.capture());

        List<Tag> savedTags = captor.getValue();
        assertThat(savedTags.size()).isEqualTo(2); //2개 저장했는지
    }

    @Test
    @DisplayName("태그 리스트 업데이트 테스트 : newTag - null")
    void newTagNameList_nullTest() throws Exception {
        //given
        List<Tag> existingTags = List.of(
                Tag.builder().tagName("tag1").post(post).build(),
                Tag.builder().tagName("tag2").post(post).build()
        );

        Mockito.when(tagRepository.findAllByPost(post)).thenReturn(existingTags);

        //when
        tagService.updateByTagNameList(post, null);

        //then
        verify(tagRepository).deleteByPostAndTagNameIn(eq(post), eq(List.of("tag1", "tag2")));
        verify(tagRepository, never()).saveAll(any());
    }



}
