package com.kau.chungwoon.festivalbackend.controller.auth;

import com.kau.chungwoon.festivalbackend.dto.auth.request.LoginRequest;
import com.kau.chungwoon.festivalbackend.dto.auth.request.SignInRequest;
import com.kau.chungwoon.festivalbackend.dto.auth.response.IssuedTokens;
import com.kau.chungwoon.festivalbackend.dto.auth.response.TokenResponse;
import com.kau.chungwoon.festivalbackend.response.ApiResponse;
import com.kau.chungwoon.festivalbackend.security.auth.CustomUserDetails;
import com.kau.chungwoon.festivalbackend.security.jwt.RefreshTokenCookieProvider;
import com.kau.chungwoon.festivalbackend.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenCookieProvider refreshTokenCookieProvider;

    @Operation(summary = "회원가입", description = "학번 기반으로 회원가입하고 JWT 토큰을 발급합니다.")
    @SecurityRequirements
    @PostMapping("/signin")
    public ApiResponse<TokenResponse> signIn(@Valid @RequestBody SignInRequest request,
                                              HttpServletResponse response) {
        IssuedTokens tokens = authService.signIn(request);
        refreshTokenCookieProvider.addRefreshTokenCookie(response, tokens.refreshToken());
        return ApiResponse.success("회원가입이 완료되었습니다.", tokens.toTokenResponse());
    }

    @Operation(summary = "로그인", description = "학번과 비밀번호로 로그인하고 JWT 토큰을 발급합니다.")
    @SecurityRequirements
    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request,
                                             HttpServletResponse response) {
        IssuedTokens tokens = authService.login(request);
        refreshTokenCookieProvider.addRefreshTokenCookie(response, tokens.refreshToken());
        return ApiResponse.success("로그인에 성공했습니다.", tokens.toTokenResponse());
    }

    @Operation(summary = "로그아웃", description = "저장된 refresh token을 삭제합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = refreshTokenCookieProvider.extractRefreshToken(request);
        authService.logout(userDetails.id(), refreshToken);
        refreshTokenCookieProvider.clearRefreshTokenCookie(response);
        return ApiResponse.success("로그아웃이 완료되었습니다.");
    }

    @Operation(summary = "토큰 재발급", description = "refresh token으로 access/refresh token을 재발급합니다. 쿠키가 없으면 Refresh-Token 헤더를 사용합니다.")
    @SecurityRequirements
    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(
            HttpServletRequest request,
            HttpServletResponse response,
            @Parameter(description = "재발급에 사용할 refresh token (Swagger 테스트용, 실제 클라이언트는 쿠키 사용)")
            @RequestHeader(value = "Refresh-Token", required = false) String headerRefreshToken
    ) {
        String refreshToken = refreshTokenCookieProvider.extractRefreshToken(request);
        if (refreshToken == null) {
            refreshToken = headerRefreshToken;
        }
        IssuedTokens tokens = authService.refresh(refreshToken);
        refreshTokenCookieProvider.addRefreshTokenCookie(response, tokens.refreshToken());
        return ApiResponse.success("토큰 재발급이 완료되었습니다.", tokens.toTokenResponse());
    }
}
