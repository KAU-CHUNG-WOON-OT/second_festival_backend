package com.kau.chungwoon.festivalbackend.dto.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginRequest(
        @Schema(description = "로그인에 사용할 학번", example = "2023123456")
        @NotBlank(message = "학번은 필수입니다.")
        @Pattern(regexp = "\\d{10}", message = "학번은 숫자 10자리여야 합니다.")
        String studentId
) {
}
