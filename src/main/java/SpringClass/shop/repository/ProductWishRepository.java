package SpringClass.shop.repository;

import SpringClass.shop.entity.Products.ProductLikes;
import SpringClass.shop.entity.Products.ProductWish;
import SpringClass.shop.entity.Products.Products;
import SpringClass.shop.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductWishRepository extends JpaRepository<ProductWish, Long> {

    boolean existsByUserAndProduct(Users user, Products product);
    Optional<ProductWish> findByUserAndProduct(Users user, Products product);
    // 내가 찜한 상품들 중 삭제되지 않은 것들 조회
    List<ProductWish> findByUserAndProduct_DeletedAtIsNull(Users user);

}
