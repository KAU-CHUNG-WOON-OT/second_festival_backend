package com.kau.chungwoon.festivalbackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Health", description = "서버 상태 확인 API")
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @Operation(summary = "헬스체크", description = "서버 동작 여부를 확인합니다.")
    @GetMapping
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }
}
