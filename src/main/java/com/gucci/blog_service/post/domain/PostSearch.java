package com.gucci.blog_service.post.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gucci.blog_service.post.domain.enums.PostType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) //_class 필드 무시하기
@Setting(settingPath = "elasticsearch/post-setting.json")
@Document(indexName = "post")
public class PostSearch {
    @Id
    private String postId;

    @Field(type = FieldType.Text, analyzer = "korean")
    private String title;

    @Field(type = FieldType.Keyword)
    private String author;

    @Field(type = FieldType.Keyword)
    private Set<String> tags;

    @Field(type = FieldType.Text, analyzer = "korean")
    private String content;

    @Field(type = FieldType.Long)
    private Long viewCount;

    @Field(type = FieldType.Keyword)
    private PostType postType;

//    @Field(type = FieldType.Date, format = DateFormat.date_time)
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssX")
//    private OffsetDateTime createdAt;

    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS||epoch_millis")
    private LocalDateTime createdAt;
}
