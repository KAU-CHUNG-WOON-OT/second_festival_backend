package com.kau.chungwoon.festivalbackend.controller.game;

import com.kau.chungwoon.festivalbackend.dto.game.response.MyRecordResponse;
import com.kau.chungwoon.festivalbackend.dto.game.response.RankingResponse;
import com.kau.chungwoon.festivalbackend.response.ApiResponse;
import com.kau.chungwoon.festivalbackend.security.auth.CustomUserDetails;
import com.kau.chungwoon.festivalbackend.service.game.GameRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Game", description = "꼬맨틀 기록/랭킹 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
public class GameController {

    private final GameRecordService gameRecordService;

    @Operation(summary = "정답 기록", description = "정답을 맞춘 순서를 기록하고 내 순위를 반환합니다. 이미 기록이 있으면 기존 기록을 그대로 반환합니다.")
    @PostMapping("/records")
    public ApiResponse<MyRecordResponse> record(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success("정답 기록에 성공했습니다.", gameRecordService.record(userDetails.id()));
    }

    @Operation(summary = "내 기록 조회", description = "내 정답 여부와 순위를 조회합니다.")
    @GetMapping("/records/me")
    public ApiResponse<MyRecordResponse> getMyRecord(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success("내 기록 조회에 성공했습니다.", gameRecordService.getMyRecord(userDetails.id()));
    }

    @Operation(summary = "랭킹 조회", description = "가장 먼저 정답을 맞춘 10명을 조회합니다.")
    @GetMapping("/rankings")
    public ApiResponse<List<RankingResponse>> getRankings() {
        return ApiResponse.success("랭킹 조회에 성공했습니다.", gameRecordService.getRankings());
    }
}
