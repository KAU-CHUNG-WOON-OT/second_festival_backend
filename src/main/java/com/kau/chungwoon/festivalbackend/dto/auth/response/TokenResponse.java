package com.kau.chungwoon.festivalbackend.dto.auth.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record TokenResponse(
        @Schema(description = "Access Token", example = "eyJhbGciOiJIUzI1NiJ9.access-token-example")
        String accessToken,

        @Schema(description = "인증 타입", example = "Bearer")
        String grantType,

        @Schema(description = "사용자 권한", example = "ROLE_USER")
        String role
) {
}
