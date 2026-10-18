package com.kau.chungwoon.festivalbackend.service.auth;

import com.kau.chungwoon.festivalbackend.domain.auth.RefreshToken;
import com.kau.chungwoon.festivalbackend.dto.auth.request.LoginRequest;
import com.kau.chungwoon.festivalbackend.dto.auth.request.SignInRequest;
import com.kau.chungwoon.festivalbackend.dto.auth.response.IssuedTokens;
import com.kau.chungwoon.festivalbackend.domain.user.User;
import com.kau.chungwoon.festivalbackend.exception.BusinessException;
import com.kau.chungwoon.festivalbackend.exception.ErrorCode;
import com.kau.chungwoon.festivalbackend.repository.auth.RefreshTokenRepository;
import com.kau.chungwoon.festivalbackend.repository.user.UserRepository;
import com.kau.chungwoon.festivalbackend.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public IssuedTokens signIn(SignInRequest request) {
        if (userRepository.existsByStudentId(request.studentId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_STUDENT_ID);
        }

        User user = userRepository.save(User.builder()
                .studentId(request.studentId())
                .name(request.name())
                .department(request.department())
                .build());

        return issueTokens(user);
    }

    @Transactional
    public IssuedTokens login(LoginRequest request) {
        User user = userRepository.findByStudentId(request.studentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return issueTokens(user);
    }

    @Transactional
    public void logout(Long userId, String refreshToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenRepository.findByUser(user)
                    .filter(savedToken -> savedToken.getToken().equals(refreshToken))
                    .ifPresent(refreshTokenRepository::delete);
            return;
        }

        refreshTokenRepository.deleteByUser(user);
    }

    @Transactional
    public IssuedTokens refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        RefreshToken savedRefreshToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        User user = savedRefreshToken.getUser();

        String studentType = user.getStudentType() != null ? user.getStudentType().name() : null;
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getUserId(), user.getRole().name(), user.getNickname(), user.getName(), user.getStudentId(), user.getDepartment(), studentType);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getUserId(), user.getRole().name(), user.getNickname(), user.getName(), user.getStudentId(), user.getDepartment(), studentType);
        savedRefreshToken.updateToken(newRefreshToken);

        return new IssuedTokens(newAccessToken, newRefreshToken, user.getRole().name());
    }

    public IssuedTokens issueTokens(User user) {
        String studentType = user.getStudentType() != null ? user.getStudentType().name() : null;
        String accessToken = jwtTokenProvider.generateAccessToken(user.getUserId(), user.getRole().name(), user.getNickname(), user.getName(), user.getStudentId(), user.getDepartment(), studentType);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUserId(), user.getRole().name(), user.getNickname(), user.getName(), user.getStudentId(), user.getDepartment(), studentType);

        refreshTokenRepository.findByUser(user)
                .ifPresentOrElse(
                        savedToken -> savedToken.updateToken(refreshToken),
                        () -> refreshTokenRepository.save(RefreshToken.builder()
                                .user(user)
                                .token(refreshToken)
                                .build())
                );

        return new IssuedTokens(accessToken, refreshToken, user.getRole().name());
    }
}
