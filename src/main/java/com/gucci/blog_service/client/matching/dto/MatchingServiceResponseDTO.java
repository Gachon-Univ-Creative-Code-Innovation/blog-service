package com.gucci.blog_service.client.matching.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class MatchingServiceResponseDTO {

    @Getter
    @Setter
    public static class RepresentTags {
        int status;
        String message;
        @JsonProperty("data")
        List<String> tags;
        String error;
    }
}
