package SpringClass.shop.repository.Coupon;

import SpringClass.shop.entity.Coupon.Coupons;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponsRepository extends JpaRepository<Coupons, Long> {
    Optional<Coupons> findByCode(String code);
    boolean existsByCode(String code);
}
