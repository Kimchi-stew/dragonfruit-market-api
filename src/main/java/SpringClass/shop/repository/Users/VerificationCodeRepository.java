package SpringClass.shop.repository.Users;

import SpringClass.shop.entity.Users.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {

    @Query("SELECT v FROM VerificationCode v WHERE v.email = :email AND v.isUsed = false AND v.expiresAt > :now ORDER BY v.createdAt DESC")
    List<VerificationCode> findValidCodes(@Param("email") String email, @Param("now") LocalDateTime now);

    void deleteByEmail(String email);
}
