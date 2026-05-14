package SpringClass.shop.service;

import SpringClass.shop.dto.Auth.request.AutoLoginRequest;
import SpringClass.shop.dto.Auth.request.LoginRequest;
import SpringClass.shop.dto.Auth.request.SendEmailDto;
import SpringClass.shop.dto.Auth.request.VerifyEmailRequest;
import SpringClass.shop.dto.Auth.response.TokenResponse;
import SpringClass.shop.entity.Users.RefreshToken;
import SpringClass.shop.entity.Users.Users;
import SpringClass.shop.exceptions.user.InvalidVerificationCodeException;
import SpringClass.shop.exceptions.user.RefreshTokenNotFoundException;
import SpringClass.shop.exceptions.user.UserNotFoundException;
import SpringClass.shop.global.TokenProvider;
import SpringClass.shop.repository.Users.RefreshTokenRepository;
import SpringClass.shop.repository.Users.UsersRepository;
import SpringClass.shop.security.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UsersRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SecurityUtils SecurityUtils;
    private final SesClient sesClient;
    private final StringRedisTemplate redisTemplate;

    @Value("${aws.send-mail-from}")
    private String senderEmail;

     // Redis 주입


    public TokenResponse login(LoginRequest request) {
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

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public TokenResponse autoLogin(AutoLoginRequest request) {
        String refreshToken = request.getRefreshToken();

        refreshTokenRepository.findById(refreshToken)
                .orElseThrow(() -> new RefreshTokenNotFoundException("리프레시 토큰이 유효하지 않습니다."));

        String email = tokenProvider.getEmail(refreshToken);
        refreshTokenRepository.deleteById(refreshToken);

        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("계정이 존재하지 않습니다."));

        String newAccessToken = tokenProvider.createToken(email);
        String newRefreshToken = tokenProvider.createRefreshToken(email);
        refreshTokenRepository.save(new RefreshToken(newRefreshToken, LocalDateTime.now().plusDays(7), user.getEmail()));
        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    @Transactional
    public void logOut() {
        Users user = SecurityUtils.getCurrentUser();
        refreshTokenRepository.deleteByEmail(user.getEmail());
    }

    public void sendEmail(SendEmailDto request) {
        String email = request.getEmail();

        String code = String.format("%06d", new SecureRandom().nextInt(1_000_000));

        // redis에 인증 코드 저장
        String redisKey = "AUTH_CODE:" + email;
        redisTemplate.opsForValue().set(redisKey, code, 5, TimeUnit.MINUTES);

        // ses로 이메일 발송
        Content subject = Content.builder()
                .data("[Dragon Fruit Market] 이메일 인증 코드")
                .build();
        Content body = Content.builder()
                .data("인증 코드: " + code + "\n\n5분 이내에 입력해주세요.")
                .build();

        SendEmailRequest sesRequest = SendEmailRequest.builder()
                .source(senderEmail)
                .destination(Destination.builder().toAddresses(email).build())
                .message(Message.builder()
                        .subject(subject)
                        .body(Body.builder().text(body).build())
                        .build())
                .build();

        sesClient.sendEmail(sesRequest);

    }

    public void verifyEmail(VerifyEmailRequest request) {
        String email = request.getEmail();
        String inputCode = request.getCode();

        String redisKey = "AUTH_CODE:" + email;
        String savedCode = redisTemplate.opsForValue().get(redisKey);

        // 코드가 없거나 만료
        if (savedCode == null) {
            throw new InvalidVerificationCodeException("인증 코드가 유효하지 않거나 만료되었습니다.");
        }

        if (!savedCode.equals(inputCode)) {
            throw new InvalidVerificationCodeException("인증 코드가 일치하지 않습니다.");
        }

        // 검증 성공 시 Redis에서 코드 제거
        redisTemplate.delete(redisKey);
    }


}
