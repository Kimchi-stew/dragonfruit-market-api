package SpringClass.shop.repository;

import SpringClass.shop.entity.RefreshToken;
import SpringClass.shop.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    // 특정 유저의 토큰 삭제
    void deleteByEmail(String email);
}
