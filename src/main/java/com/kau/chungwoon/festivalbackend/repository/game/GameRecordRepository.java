package com.kau.chungwoon.festivalbackend.repository.game;

import com.kau.chungwoon.festivalbackend.domain.game.GameRecord;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRecordRepository extends JpaRepository<GameRecord, Long> {

    Optional<GameRecord> findByUser_UserId(Long userId);

    @EntityGraph(attributePaths = "user")
    List<GameRecord> findTop10ByOrderByGameRecordIdAsc();

    long countByGameRecordIdLessThanEqual(Long gameRecordId);
}
