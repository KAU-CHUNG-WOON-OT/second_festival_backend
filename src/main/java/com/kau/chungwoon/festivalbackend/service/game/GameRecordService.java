package com.kau.chungwoon.festivalbackend.service.game;

import com.kau.chungwoon.festivalbackend.domain.game.GameRecord;
import com.kau.chungwoon.festivalbackend.domain.user.User;
import com.kau.chungwoon.festivalbackend.dto.game.response.MyRecordResponse;
import com.kau.chungwoon.festivalbackend.dto.game.response.RankingResponse;
import com.kau.chungwoon.festivalbackend.exception.BusinessException;
import com.kau.chungwoon.festivalbackend.exception.ErrorCode;
import com.kau.chungwoon.festivalbackend.repository.game.GameRecordRepository;
import com.kau.chungwoon.festivalbackend.repository.user.UserRepository;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameRecordService {

    private static final int RANKING_SIZE = 10;

    private final GameRecordRepository gameRecordRepository;
    private final UserRepository userRepository;

    // 기록은 순서대로 쌓이기만 하므로 10명이 채워지면 TOP 10은 더 이상 바뀌지 않습니다.
    // 그 시점부터는 DB를 조회하지 않고 메모리에 고정된 결과를 반환합니다. (DB에서 기록을 수동 삭제했다면 재시작 필요)
    private volatile List<RankingResponse> fixedRankings;

    @Transactional
    public MyRecordResponse record(Long userId) {
        GameRecord gameRecord = gameRecordRepository.findByUser_UserId(userId)
                .orElseGet(() -> save(userId));

        return toMyRecord(gameRecord);
    }

    public MyRecordResponse getMyRecord(Long userId) {
        return gameRecordRepository.findByUser_UserId(userId)
                .map(this::toMyRecord)
                .orElseGet(MyRecordResponse::notSolved);
    }

    public List<RankingResponse> getRankings() {
        List<RankingResponse> cached = fixedRankings;
        if (cached != null) {
            return cached;
        }

        List<GameRecord> records = gameRecordRepository.findTop10ByOrderByGameRecordIdAsc();
        List<RankingResponse> rankings = IntStream.range(0, records.size())
                .mapToObj(index -> toRanking(index + 1, records.get(index)))
                .toList();

        if (rankings.size() == RANKING_SIZE) {
            fixedRankings = rankings;
        }
        return rankings;
    }

    private GameRecord save(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        try {
            return gameRecordRepository.saveAndFlush(new GameRecord(user));
        } catch (DataIntegrityViolationException exception) {
            // 같은 유저의 동시 요청(더블 클릭 등)으로 unique 제약에 걸린 경우
            throw new BusinessException(ErrorCode.GAME_ALREADY_RECORDED);
        }
    }

    private MyRecordResponse toMyRecord(GameRecord gameRecord) {
        long rank = gameRecordRepository.countByGameRecordIdLessThanEqual(gameRecord.getGameRecordId());
        return new MyRecordResponse(true, rank, gameRecord.getCreatedAt());
    }

    private RankingResponse toRanking(long rank, GameRecord gameRecord) {
        User user = gameRecord.getUser();
        String displayName = user.getName() != null ? maskName(user.getName()) : user.getNickname();
        return new RankingResponse(rank, displayName, maskStudentId(user.getStudentId()), user.getDepartment(), gameRecord.getCreatedAt());
    }

    private String maskName(String name) {
        if (name.length() <= 1) {
            return name;
        }
        if (name.length() == 2) {
            return name.charAt(0) + "*";
        }
        return name.charAt(0) + "*".repeat(name.length() - 2) + name.charAt(name.length() - 1);
    }

    // 2023123456 -> 2023****56 (카카오 로그인 후 추가 정보 미등록 유저는 null)
    private String maskStudentId(String studentId) {
        if (studentId == null || studentId.length() <= 6) {
            return studentId;
        }
        return studentId.substring(0, 4) + "*".repeat(studentId.length() - 6) + studentId.substring(studentId.length() - 2);
    }
}
