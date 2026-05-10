package SpringClass.shop.security;

import SpringClass.shop.entity.Users.Users;
import SpringClass.shop.exceptions.user.NotAuthenticatedException;
import SpringClass.shop.exceptions.user.UserNotFoundException;
import SpringClass.shop.repository.Users.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UsersRepository usersRepository;

    public Users getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new NotAuthenticatedException("로그인이 필요합니다.");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof org.springframework.security.core.userdetails.User springUser) {
            return usersRepository.findByEmail(springUser.getUsername())
                    .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
        } else {
            throw new NotAuthenticatedException("로그인이 필요합니다.");
        }
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public Optional<Users> getCurrentUserOptional() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) return Optional.empty();
            Object principal = authentication.getPrincipal();
            if (principal instanceof org.springframework.security.core.userdetails.User springUser) {
                return usersRepository.findByEmail(springUser.getUsername());
            }
            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}

