package com.gucci.blog_service.global.controller;


import com.gucci.blog_service.global.dto.GlobalRequestDTO;
import com.gucci.blog_service.global.service.GlobalOrchestrationService;
import com.gucci.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/blog-service/global")
public class GlobalController {
    private final GlobalOrchestrationService globalOrchestrationService;

//    /** 닉네임 변경 반응 */
//    @Operation(summary = "[Server전용] post 닉네임 변경 반응", description = "user-service와 user nickname sync를 맞추기 위한 닉네임 변경 반응 API")
//    @PatchMapping("/nickname")
//    public ApiResponse<String> updateNickname(
//        @RequestBody @Valid GlobalRequestDTO.UpdateUserNickname request
//    ) {
//        globalOrchestrationService.updateUserNickname(request);
//        return ApiResponse.success("닉네임 반영 완료");
//    }
}
