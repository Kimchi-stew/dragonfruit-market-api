package SpringClass.shop.service;

import SpringClass.shop.dto.LoginRequest;
import SpringClass.shop.dto.SignupRequest;
import SpringClass.shop.dto.TokenResponse;
import SpringClass.shop.entity.RefreshToken;
import SpringClass.shop.entity.Users;
import SpringClass.shop.enums.UserRole;
import SpringClass.shop.global.TokenProvider;
import SpringClass.shop.repository.RefreshTokenRepository;
import SpringClass.shop.repository.UsersRepository;
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

    public void signup(SignupRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 계정입니다.");
        }



        Users user = Users.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .gender(request.getGender())
                .userRole(UserRole.USER)
                .profileImage(request.getProfileImage())
                .build();
        userRepository.save(user);
    }

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
}
