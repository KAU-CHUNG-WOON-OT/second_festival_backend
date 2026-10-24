package com.kau.chungwoon.festivalbackend.dto.game.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record MyRecordResponse(
        @Schema(description = "정답 여부", example = "true")
        boolean solved,

        @Schema(description = "내 순위 (미정답이면 null)", example = "57")
        Long rank,

        @Schema(description = "정답 시각 (미정답이면 null)", example = "2026-10-24T13:05:12")
        LocalDateTime solvedAt
) {

    public static MyRecordResponse notSolved() {
        return new MyRecordResponse(false, null, null);
    }
}
