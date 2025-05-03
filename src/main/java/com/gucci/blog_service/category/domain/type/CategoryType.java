package com.gucci.blog_service.category.domain.type;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum CategoryType{
    DEVELOPMENT("1", "개발"),
    CLOUD_INFRA("2", "클라우드 & 인프라"),
    AI("3", "AI"),
    DATABASE("4", "데이터베이스"),
    CS("5", "CS 지식"),
    PROJECT("6", "프로젝트"),
    TROUBLESHOOT("7", "문제해결"),
    GROWTH("8", "성장 기록"),
    IT_NEWS("9", "IT 뉴스"),
    ETC("10", "기타");

    private final String code;
    private final String type;


    @JsonValue
    public String createJson(){
        return this.name();
    }
}
