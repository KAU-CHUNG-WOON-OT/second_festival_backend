package com.kau.chungwoon.festivalbackend.dto.user.request;

import com.kau.chungwoon.festivalbackend.domain.user.StudentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UpdateProfileRequest(
        @Schema(description = "이름", example = "홍길동")
        @NotBlank(message = "이름을 입력해주세요.")
        String name,

        @Schema(description = "학번", example = "2023123456")
        @NotBlank(message = "학번을 입력해주세요.")
        @Pattern(regexp = "\\d{10}", message = "학번은 숫자 10자리여야 합니다.")
        String studentId,

        @Schema(description = "학과", example = "항공우주및기계공학부")
        @NotBlank(message = "학과를 입력해주세요.")
        String department,

        @Schema(description = "이메일", example = "example@kau.kr")
        @NotBlank(message = "이메일을 입력해주세요.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email,

        @Schema(description = "학적 상태 (UNDERGRADUATE, ON_LEAVE, GRADUATE)", example = "UNDERGRADUATE")
        @NotNull(message = "학적 상태를 선택해주세요.")
        StudentType studentType
) {
}
