package com.kau.chungwoon.festivalbackend.dto.oauth;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KakaoUserInfo {

    private String oauthId;
    private String nickname;

    @SuppressWarnings("unchecked")
    public static KakaoUserInfo from(Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        return KakaoUserInfo.builder()
                .oauthId(String.valueOf(attributes.get("id")))
                .nickname((String) profile.get("nickname"))
                .build();
    }
}
