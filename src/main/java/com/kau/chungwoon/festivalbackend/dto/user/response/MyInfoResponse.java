package com.kau.chungwoon.festivalbackend.dto.user.response;

import com.kau.chungwoon.festivalbackend.domain.user.StudentType;
import io.swagger.v3.oas.annotations.media.Schema;

public record MyInfoResponse(
        @Schema(description = "사용자 PK", example = "1")
        Long userId,

        @Schema(description = "학번", example = "20231234")
        String studentId,

        @Schema(description = "이름", example = "홍길동")
        String name,

        @Schema(description = "카카오 닉네임", example = "카카오닉네임")
        String nickname,

        @Schema(description = "학과", example = "항공우주및기계공학부")
        String department,

        @Schema(description = "학적 상태", example = "UNDERGRADUATE")
        StudentType studentType,

        @Schema(description = "권한", example = "ROLE_USER")
        String role
) {
}
