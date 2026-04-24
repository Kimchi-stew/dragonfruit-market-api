package SpringClass.shop.repository.Sellers;


import SpringClass.shop.entity.Sellers.SellerFollow;
import SpringClass.shop.entity.Sellers.Sellers;
import SpringClass.shop.entity.Users.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SellerFollowRepository extends JpaRepository<SellerFollow, Long> {
    boolean existsByUserAndSellers(Users user, Sellers sellers);
    Optional<SellerFollow> findByUserAndSellers(Users user, Sellers sellers);
    // 내 좋아요한 상점중 삭제되지 않은 상점들 조회
    List<SellerFollow> findByUserAndSellers_DeletedAtIsNull(Users user);
}
