package com.kau.chungwoon.festivalbackend.dto.auth.response;

public record IssuedTokens(
        String accessToken,
        String refreshToken,
        String role
) {
    public TokenResponse toTokenResponse() {
        return new TokenResponse(accessToken, "Bearer", role);
    }
}
