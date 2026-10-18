package com.kau.chungwoon.festivalbackend.service.user;

import com.kau.chungwoon.festivalbackend.domain.user.User;
import com.kau.chungwoon.festivalbackend.dto.user.request.UpdateProfileRequest;
import com.kau.chungwoon.festivalbackend.dto.user.response.MyInfoResponse;
import com.kau.chungwoon.festivalbackend.exception.BusinessException;
import com.kau.chungwoon.festivalbackend.exception.ErrorCode;
import com.kau.chungwoon.festivalbackend.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public MyInfoResponse getMyInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return new MyInfoResponse(
                user.getUserId(),
                user.getStudentId(),
                user.getName(),
                user.getNickname(),
                user.getDepartment(),
                user.getStudentType(),
                user.getRole().name()
        );
    }

    @Transactional
    public void updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.updateProfile(request.name(), request.studentId(), request.department(), request.email(), request.studentType());
    }
}
