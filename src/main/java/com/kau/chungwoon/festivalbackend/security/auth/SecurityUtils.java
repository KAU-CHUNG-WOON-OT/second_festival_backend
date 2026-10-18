package com.kau.chungwoon.festivalbackend.security.auth;

import com.kau.chungwoon.festivalbackend.exception.BusinessException;
import com.kau.chungwoon.festivalbackend.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
        return userDetails.id();
    }
}
