package com.kau.chungwoon.festivalbackend.controller.user;

import com.kau.chungwoon.festivalbackend.dto.user.request.UpdateProfileRequest;
import com.kau.chungwoon.festivalbackend.dto.user.response.MyInfoResponse;
import com.kau.chungwoon.festivalbackend.response.ApiResponse;
import com.kau.chungwoon.festivalbackend.security.auth.CustomUserDetails;
import com.kau.chungwoon.festivalbackend.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User", description = "사용자 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class  UserController {

    private final UserService userService;

    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    @GetMapping("/me")
    public ApiResponse<MyInfoResponse> getMyInfo(@AuthenticationPrincipal CustomUserDetails userDetails, HttpServletResponse response) {
        response.setHeader("Cache-Control", "private, max-age=1800");
        return ApiResponse.success("내 정보 조회에 성공했습니다.", userService.getMyInfo(userDetails.id()));
    }

    @Operation(summary = "추가 정보 등록", description = "카카오 로그인 후 학번, 학과, 이메일을 등록합니다.")
    @PostMapping("/me/profile")
    public ApiResponse<Void> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        userService.updateProfile(userDetails.id(), request);
        return ApiResponse.success("추가 정보가 등록되었습니다.", null);
    }
}
