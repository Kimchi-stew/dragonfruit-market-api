package SpringClass.shop.service;

import SpringClass.shop.dto.Auth.request.AutoLoginRequest;
import SpringClass.shop.dto.Auth.request.LoginRequest;
import SpringClass.shop.dto.Auth.request.SendEmailRequest;
import SpringClass.shop.dto.Auth.request.VerifyEmailRequest;
import SpringClass.shop.dto.Auth.response.TokenResponse;
import SpringClass.shop.entity.Users.RefreshToken;
import SpringClass.shop.entity.Users.Users;
import SpringClass.shop.entity.Users.VerificationCode;
import SpringClass.shop.exceptions.user.InvalidVerificationCodeException;
import SpringClass.shop.exceptions.user.RefreshTokenNotFoundException;
import SpringClass.shop.exceptions.user.UserNotFoundException;
import SpringClass.shop.global.TokenProvider;
import SpringClass.shop.repository.Users.RefreshTokenRepository;
import SpringClass.shop.repository.Users.UsersRepository;
import SpringClass.shop.repository.Users.VerificationCodeRepository;
import SpringClass.shop.security.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

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
    private final VerificationCodeRepository verificationCodeRepository;

    @Value("${aws.send-mail-from}")
    private String senderEmail;

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

    @Transactional
    public void sendEmail(SendEmailRequest request) {
        String email = request.getEmail();

        // 기존 인증 코드 삭제
        verificationCodeRepository.deleteByEmail(email);

        // 6자리 인증 코드 생성
        String code = String.format("%06d", new SecureRandom().nextInt(1_000_000));

        // 인증 코드 저장 (5분 유효)
        VerificationCode verificationCode = VerificationCode.builder()
                .email(email)
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .build();
        verificationCodeRepository.save(verificationCode);

        // SES로 이메일 발송
        Content subject = Content.builder()
                .data("[Dragon Fruit Market] 이메일 인증 코드")
                .build();
        Content body = Content.builder()
                .data("인증 코드: " + code + "\n\n5분 이내에 입력해주세요.")
                .build();

        software.amazon.awssdk.services.ses.model.SendEmailRequest sesRequest =
                software.amazon.awssdk.services.ses.model.SendEmailRequest.builder()
                        .source(senderEmail)
                        .destination(Destination.builder().toAddresses(email).build())
                        .message(Message.builder()
                                .subject(subject)
                                .body(Body.builder().text(body).build())
                                .build())
                        .build();

        sesClient.sendEmail(sesRequest);
    }

    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {
        List<VerificationCode> validCodes = verificationCodeRepository
                .findValidCodes(request.getEmail(), LocalDateTime.now());

        if (validCodes.isEmpty()) {
            throw new InvalidVerificationCodeException("인증 코드가 유효하지 않거나 만료되었습니다.");
        }

        VerificationCode verificationCode = validCodes.get(0);

        if (!verificationCode.getCode().equals(request.getCode())) {
            throw new InvalidVerificationCodeException("인증 코드가 일치하지 않습니다.");
        }

        verificationCode.setUsed(true);
    }
}
