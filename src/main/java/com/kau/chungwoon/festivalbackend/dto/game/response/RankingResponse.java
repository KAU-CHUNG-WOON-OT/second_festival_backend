package com.kau.chungwoon.festivalbackend.dto.game.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record RankingResponse(
        @Schema(description = "순위", example = "1")
        long rank,

        @Schema(description = "표시 이름 (마스킹)", example = "홍*동")
        String displayName,

        @Schema(description = "학번 (마스킹)", example = "2023****56")
        String studentId,

        @Schema(description = "학과", example = "항공우주및기계공학부")
        String department,

        @Schema(description = "정답 시각", example = "2026-10-24T13:05:12")
        LocalDateTime solvedAt
) {
}
