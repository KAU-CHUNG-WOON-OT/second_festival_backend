package com.kau.chungwoon.festivalbackend.repository.user;

import com.kau.chungwoon.festivalbackend.domain.user.SocialType;
import com.kau.chungwoon.festivalbackend.domain.user.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByStudentId(String studentId);

    boolean existsByStudentId(String studentId);

    Optional<User> findByOauthIdAndSocialType(String oauthId, SocialType socialType);
}
