package com.gucci.blog_service.category.domain.type;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum CategoryType{
    DEVELOPMENT(1L, "개발"),
    CLOUD_INFRA(2L, "클라우드 & 인프라"),
    AI(3L, "AI"),
    DATABASE(4L, "데이터베이스"),
    CS(5L, "CS 지식"),
    PROJECT(6L, "프로젝트"),
    TROUBLESHOOT(7L, "문제해결"),
    GROWTH(8L, "성장 기록"),
    IT_NEWS(9L, "IT 뉴스"),
    ETC(10L, "기타"),
    STUDY(11L, "스터디"),
    CONTEXT(12L, "공모전")
    ;

    private final Long code;
    private final String type;


    @JsonValue
    public String createJson(){
        return this.name();
    }
}
