package SpringClass.shop.repository;
import SpringClass.shop.entity.Sellers.SellerLikes;
import SpringClass.shop.entity.Sellers.Sellers;
import SpringClass.shop.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SellerLikesRepository extends JpaRepository<SellerLikes, Long> {
    Optional<SellerLikes> findByUserAndSellers(Users user, Sellers sellers);

}
