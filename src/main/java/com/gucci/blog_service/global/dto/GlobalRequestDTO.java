package com.gucci.blog_service.global.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class GlobalRequestDTO {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateUserNickname {
        @NotBlank(message = "유저 아이디는 필수입니다.")
        private Long userId;
        @NotBlank(message = "유저 닉네임은 필수입니다.")
        private String newNickname;
    }
}
