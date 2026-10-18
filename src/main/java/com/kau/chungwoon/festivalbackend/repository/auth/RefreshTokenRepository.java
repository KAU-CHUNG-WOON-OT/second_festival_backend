package com.kau.chungwoon.festivalbackend.repository.auth;

import com.kau.chungwoon.festivalbackend.domain.auth.RefreshToken;
import com.kau.chungwoon.festivalbackend.domain.user.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByUser(User user);

    Optional<RefreshToken> findByToken(String token);

    boolean existsByUser(User user);

    void deleteByUser(User user);

    void deleteByToken(String token);
}
