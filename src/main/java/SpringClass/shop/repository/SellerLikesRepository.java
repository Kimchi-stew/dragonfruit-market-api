package SpringClass.shop.repository;
import SpringClass.shop.entity.Products.ProductLikes;
import SpringClass.shop.entity.Sellers.SellerLikes;
import SpringClass.shop.entity.Sellers.Sellers;
import SpringClass.shop.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SellerLikesRepository extends JpaRepository<SellerLikes, Long> {
    Optional<SellerLikes> findByUserAndSellers(Users user, Sellers sellers);
    // 내 좋아요한 상점중 삭제되지 않은 상점들 조회
    List<SellerLikes> findByUserAndSellers_DeletedAtIsNull(Users user);
}
