package SpringClass.shop.security.oauth;

import SpringClass.shop.dto.Auth.response.TokenResponse;
import SpringClass.shop.entity.Users.RefreshToken;
import SpringClass.shop.exceptions.user.UserNotFoundException;
import SpringClass.shop.global.ApiResponse;
import SpringClass.shop.global.TokenProvider;
import SpringClass.shop.repository.Users.RefreshTokenRepository;
import SpringClass.shop.repository.Users.UsersRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final TokenProvider tokenProvider;
    private final UsersRepository usersRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        CustomOauth2UserDetails userDetails = (CustomOauth2UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();
        boolean isNewUser = userDetails.isNewUser();

        usersRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("사용자가 존재하지 않습니다."));

        String accessToken = tokenProvider.createToken(email);
        String refreshToken = tokenProvider.createRefreshToken(email);

        refreshTokenRepository.deleteByEmail(email);
        refreshTokenRepository.save(RefreshToken.builder()
                .refreshToken(refreshToken)
                .email(email)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build());

        TokenResponse tokenResponse = TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .isNewUser(isNewUser)
                .build();

        String message = isNewUser ? "추가 정보를 입력해주세요." : "로그인이 완료되었습니다.";
        ApiResponse<TokenResponse> apiResponse = ApiResponse.ok(tokenResponse, message);

        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
        response.getWriter().flush();
    }
}
