package com.kau.chungwoon.festivalbackend.security.oauth;

import com.kau.chungwoon.festivalbackend.domain.auth.RefreshToken;
import com.kau.chungwoon.festivalbackend.domain.user.User;
import com.kau.chungwoon.festivalbackend.repository.auth.RefreshTokenRepository;
import com.kau.chungwoon.festivalbackend.security.jwt.JwtTokenProvider;
import com.kau.chungwoon.festivalbackend.security.jwt.RefreshTokenCookieProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenCookieProvider refreshTokenCookieProvider;

    @Value("${oauth2.redirect-uri}")
    private String redirectUri;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        User user = oAuth2User.getUser();

        String studentType = user.getStudentType() != null ? user.getStudentType().name() : null;
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUserId(), user.getRole().name(), user.getNickname(), user.getName(), user.getStudentId(), user.getDepartment(), studentType);

        refreshTokenRepository.findByUser(user)
                .ifPresentOrElse(
                        saved -> saved.updateToken(refreshToken),
                        () -> refreshTokenRepository.save(RefreshToken.builder()
                                .user(user)
                                .token(refreshToken)
                                .build())
                );

        refreshTokenCookieProvider.addRefreshTokenCookie(response, refreshToken);

        HttpSession session = request.getSession(false);
        String targetBase = (session != null)
                ? (String) session.getAttribute(CustomAuthorizationRequestRepository.REDIRECT_URI_SESSION_KEY)
                : null;
        if (targetBase == null || targetBase.isBlank()) {
            targetBase = redirectUri;
        } else {
            session.removeAttribute(CustomAuthorizationRequestRepository.REDIRECT_URI_SESSION_KEY);
        }

        String redirectPath = oAuth2User.getUser().getStudentId() == null ? "/info" : "/home";
        String targetUrl = UriComponentsBuilder.fromUriString(targetBase)
                .path(redirectPath)
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
