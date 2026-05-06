package SpringClass.shop.security.oauth;

import SpringClass.shop.entity.Users.Users;
import SpringClass.shop.enums.UserRole;
import SpringClass.shop.repository.Users.UsersRepository;
import SpringClass.shop.repository.oauth.OAuth2UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOauth2UserService extends DefaultOAuth2UserService {

    private final UsersRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        log.info("getAttributes : {}",oAuth2User.getAttributes());

        String provider = userRequest.getClientRegistration().getRegistrationId();

        OAuth2UserInfo oAuth2UserInfo;

        if (provider.equals("naver")) {
            log.info("네이버 로그인");
            oAuth2UserInfo = new NaverUserDetails(oAuth2User.getAttributes());
        } else if (provider.equals("kakao")) {
            log.info("카카오 로그인");
            oAuth2UserInfo = new KakaoUserDetails(oAuth2User.getAttributes());
        } else {
            throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다: " + provider);
        }

        String providerId = oAuth2UserInfo.getProviderId();
        String email = oAuth2UserInfo.getEmail();

        // 카카오 이메일 미동의 또는 미인증 계정 처리
        if (email == null || email.isEmpty()) {
            log.warn("소셜 로그인 이메일 미제공 - provider: {}, providerId: {}", provider, providerId);
            email = provider + "_" + providerId + "@social.login";
        }

        // 유저가 없을 때 바로 오류를 안 내리기 위해 Optional 사용
        Optional<Users> optionalUser = userRepository.findByEmail(email);
        Users user;
        // db에 사용자가 없을 때 회원가입된 후 토큰 반환
        if (optionalUser.isEmpty()) {
            user = Users.builder()
                    .email(email) // 로그인 아이디
                    .password("SOCIAL_LOGIN") // 소셜 로그인용 임의 비번
                    .provider(provider)
                    .providerId(providerId)
                    .userRole(UserRole.USER)
                    .build();
            userRepository.save(user);

        } else{
            user = optionalUser.get();
            // 기존 유저라면 provider 정보 업데이트
            if (!provider.equals(user.getProvider())) {
                user.setProvider(provider);
                user.setProviderId(providerId);
            }

        }

        return new CustomOauth2UserDetails(user, oAuth2User.getAttributes());
    }
}
