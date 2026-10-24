package com.kau.chungwoon.festivalbackend.domain.game;

import com.kau.chungwoon.festivalbackend.domain.common.BaseEntity;
import com.kau.chungwoon.festivalbackend.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 꼬맨틀 정답 기록. 유저당 1건이며, PK(auto increment) 순서가 곧 정답 순서(랭킹)입니다.
 */
@Getter
@Entity
@Table(name = "game_records")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GameRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_record_id")
    private Long gameRecordId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    public GameRecord(User user) {
        this.user = user;
    }
}
