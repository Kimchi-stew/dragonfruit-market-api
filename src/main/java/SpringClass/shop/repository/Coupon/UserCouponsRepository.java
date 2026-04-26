package SpringClass.shop.repository.Coupon;

import SpringClass.shop.entity.Coupon.Coupons;
import SpringClass.shop.entity.Coupon.UserCoupons;
import SpringClass.shop.entity.Users.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserCouponsRepository extends JpaRepository<UserCoupons, Long> {
    boolean existsByUserAndCoupon(Users user, Coupons coupon);
    List<UserCoupons> findByUser(Users user);
    List<UserCoupons> findByUserAndIsUsed(Users user, boolean isUsed);
    Optional<UserCoupons> findByIdAndUser(Long id, Users user);
}
