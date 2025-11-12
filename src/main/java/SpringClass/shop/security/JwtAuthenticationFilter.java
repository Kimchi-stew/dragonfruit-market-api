package SpringClass.shop.security;

import SpringClass.shop.global.TokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;          // validateToken, getUsername 제공
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {


        // 1) Authorization 헤더에서 Bearer 토큰 추출
        String token = resolveToken(request);

        // 2) 토큰 존재 + 유효성 검증
        if (StringUtils.hasText(token) && tokenProvider.validateToken(token)) {

            // email로 바꿔줌
            String email = tokenProvider.getEmail(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // 4) SecurityContext에 Authentication 주입 (이미 있으면 덮어쓰지 않음)
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // 5) 다음 필터로 진행
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (!StringUtils.hasText(header)) return null;
        // "Bearer xxx" 형식만 허용
        if (header.startsWith("Bearer ")) {
            return header.substring(7).trim();
        }
        return null;
    }
}
