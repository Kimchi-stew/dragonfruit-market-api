package SpringClass.shop.repository.Products;

import SpringClass.shop.entity.Products.ProductLikes;
import SpringClass.shop.entity.Products.Products;
import SpringClass.shop.entity.Users.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductLikeRepository extends JpaRepository<ProductLikes, Long> {
    Optional<ProductLikes> findByUserAndProduct(Users user, Products product);
    // 내 좋아요한 상품중 삭제되지 않은 상품들 조회
    List<ProductLikes> findByUserAndProduct_DeletedAtIsNull(Users user);

    // 내 좋아요한 상품중 삭제되지 않은 상품들 조회 + 최신순(사용자가 좋아요 누른 시간기준)
    List<ProductLikes> findByUserAndProduct_DeletedAtIsNullOrderByCreatedAtDesc(Users user);
    // 내 좋아요한 상품중 삭제되지 않은 상품들 조회 + 오래된순(사용자가 좋아요 누른 시간기준)
    List<ProductLikes> findByUserAndProduct_DeletedAtIsNullOrderByCreatedAtAsc(Users user);
    // 내 좋아요한 상품중 삭제되지 않은 상품들 조회 + 좋아요순 + 최신순(사용자가 좋아요 누른 시간기준)
    List<ProductLikes> findByUserAndProduct_DeletedAtIsNullOrderByProduct_LikeCountDescCreatedAtDesc(Users user);
}
