package com.kau.chungwoon.festivalbackend.security.oauth;

import com.kau.chungwoon.festivalbackend.domain.user.Role;
import com.kau.chungwoon.festivalbackend.domain.user.SocialType;
import com.kau.chungwoon.festivalbackend.domain.user.User;
import com.kau.chungwoon.festivalbackend.dto.oauth.KakaoUserInfo;
import com.kau.chungwoon.festivalbackend.repository.user.UserRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        if (!"kakao".equals(registrationId)) {
            throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다: " + registrationId);
        }

        KakaoUserInfo kakaoUserInfo = KakaoUserInfo.from(attributes);
        User user = userRepository.findByOauthIdAndSocialType(kakaoUserInfo.getOauthId(), SocialType.KAKAO)
                .orElseGet(() -> userRepository.save(User.builder()
                        .oauthId(kakaoUserInfo.getOauthId())
                        .nickname(kakaoUserInfo.getNickname())
                        .role(Role.ROLE_USER)
                        .socialType(SocialType.KAKAO)
                        .build()));

        return new CustomOAuth2User(user, attributes);
    }
}
