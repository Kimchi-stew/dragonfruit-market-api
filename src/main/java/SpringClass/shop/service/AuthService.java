package SpringClass.shop.service;

import SpringClass.shop.dto.Auth.request.AutoLoginRequest;
import SpringClass.shop.dto.Auth.request.LoginRequest;
import SpringClass.shop.dto.Auth.request.SendEmailRequest;
import SpringClass.shop.dto.Auth.response.TokenResponse;
import SpringClass.shop.entity.Users.RefreshToken;
import SpringClass.shop.entity.Users.Users;
import SpringClass.shop.exceptions.RefreshTokenNotFoundException;
import SpringClass.shop.exceptions.UserNotFoundException;
import SpringClass.shop.global.TokenProvider;
import SpringClass.shop.repository.Users.RefreshTokenRepository;
import SpringClass.shop.repository.Users.UsersRepository;
import SpringClass.shop.security.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UsersRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SecurityUtils SecurityUtils;



    public TokenResponse login(LoginRequest request) {
        // 이메일, 비번 검증
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        String accessToken = tokenProvider.createToken(request.getEmail());
        String refreshToken = tokenProvider.createRefreshToken(request.getEmail());
        RefreshToken rt = RefreshToken.builder()
                .email(request.getEmail())
                .refreshToken(refreshToken)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build();

        refreshTokenRepository.save(rt);

        TokenResponse result = TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        return result;
    }

    public TokenResponse autoLogin(AutoLoginRequest request) {
        String refreshToken = request.getRefreshToken();

        refreshTokenRepository.findById(refreshToken)
                .orElseThrow(() -> new RefreshTokenNotFoundException("리프레시 토큰이 유효하지 않습니다."));

        // 토큰으로 사용자 추출
        String email = tokenProvider.getEmail(refreshToken);

        // 기존 토큰 삭제
        refreshTokenRepository.deleteById(refreshToken);

        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("계정이 존재하지 않습니다."));

        // 새로운 accessToken,  refreshToken 생성
        String newAccessToken = tokenProvider.createToken(email);
        String newRefreshToken = tokenProvider.createRefreshToken(email);
        refreshTokenRepository.save(new RefreshToken(newRefreshToken, LocalDateTime.now().plusDays(7), user.getEmail()));
        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logOut() {
        Users user = SecurityUtils.getCurrentUser();
        // 토큰 삭제
        refreshTokenRepository.deleteByEmail(user.getEmail());
    }

    // 이메일 인증 코드 발송
    public void sendEmail(SendEmailRequest request) {
        SesClient sesClient = SesClient.builder()
                .region(Region.AP_NORTHEAST_2)
                .build();

        SendEmailRequest request = SendEmailRequest.builder()
                .source("example@example.com")
                .destination(Destination.builder().toAddresses("recipient@example.com").build())
                .message(Message.builder()
                        .subject(Content.builder().data("Test Subject").build())
                        .body(Body.builder().text(Content.builder().data("Test Body").build()).build())
                        .build())
                .build();

        sesClient.sendEmail(request);
    }
}
