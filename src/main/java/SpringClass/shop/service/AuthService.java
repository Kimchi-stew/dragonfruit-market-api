package SpringClass.shop.service;

import SpringClass.shop.dto.Auth.AutoLoginRequest;
import SpringClass.shop.dto.Auth.LoginRequest;
import SpringClass.shop.dto.Auth.TokenResponse;
import SpringClass.shop.entity.RefreshToken;
import SpringClass.shop.entity.Users;
import SpringClass.shop.exceptions.RefreshTokenNotFoundException;
import SpringClass.shop.exceptions.UserNotFoundException;
import SpringClass.shop.global.TokenProvider;
import SpringClass.shop.repository.RefreshTokenRepository;
import SpringClass.shop.repository.UsersRepository;
import SpringClass.shop.security.AuthenticatedUserUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UsersRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticatedUserUtils authenticatedUserUtils;



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
        Users user = authenticatedUserUtils.getCurrentUser();
        // 토큰 삭제
        refreshTokenRepository.deleteByEmail(user.getEmail());
    }
}
