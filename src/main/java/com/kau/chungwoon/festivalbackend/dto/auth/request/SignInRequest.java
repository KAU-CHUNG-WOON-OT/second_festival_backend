package com.kau.chungwoon.festivalbackend.dto.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SignInRequest(
        @Schema(description = "회원 학번", example = "2023123456")
        @NotBlank(message = "학번은 필수입니다.")
        @Pattern(regexp = "\\d{10}", message = "학번은 숫자 10자리여야 합니다.")
        String studentId,

        @Schema(description = "회원 이름", example = "홍길동")
        @NotBlank(message = "이름은 필수입니다.")
        String name,

        @Schema(description = "소속 학과", example = "항공우주및기계공학부")
        @NotBlank(message = "학과는 필수입니다.")
        String department
) {
}
