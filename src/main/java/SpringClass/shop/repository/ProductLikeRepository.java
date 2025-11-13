package SpringClass.shop.repository;

import SpringClass.shop.entity.Products.ProductLikes;
import SpringClass.shop.entity.Products.Products;
import SpringClass.shop.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductLikeRepository extends JpaRepository<ProductLikes, Long> {
    Optional<ProductLikes> findByUserAndProduct(Users user, Products product);
    // 내 좋아요한 상품중 삭제되지 않은 상품들 조회
    List<ProductLikes> findByUserAndProduct_DeletedAtIsNull(Users user);
}
