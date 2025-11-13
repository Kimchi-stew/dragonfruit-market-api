package SpringClass.shop.repository;


import SpringClass.shop.entity.Sellers.SellerFollow;
import SpringClass.shop.entity.Sellers.Sellers;
import SpringClass.shop.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SellerFollowRepository extends JpaRepository<SellerFollow, Long> {
    boolean existsByUserAndSellers(Users user, Sellers sellers);
    Optional<SellerFollow> findByUserAndSellers(Users user, Sellers sellers);
}
