package SpringClass.shop.security;

import SpringClass.shop.entity.Users;
import SpringClass.shop.exceptions.UserNotFoundException;
import SpringClass.shop.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticatedUserUtils {
    private final SecurityUtils securityUtils;
    private final UsersRepository usersRepository; // Users 조회용

    public Long getCurrentUserId() {
        return securityUtils.getCurrentUserId();
    }

    public Users getCurrentUser() {
        return securityUtils.getCurrentUser();
    }
}
