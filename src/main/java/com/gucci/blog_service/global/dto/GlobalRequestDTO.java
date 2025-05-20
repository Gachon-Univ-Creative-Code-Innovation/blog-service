package com.gucci.blog_service.global.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
        @NotNull(message = "유저 아이디는 필수입니다.")
        private Long userId;
        @NotBlank(message = "유저 닉네임은 필수입니다.")
        private String newNickname;
    }
}
