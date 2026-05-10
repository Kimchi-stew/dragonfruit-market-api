package SpringClass.shop.repository.Products;

import SpringClass.shop.entity.Products.UserRecommendation;
import SpringClass.shop.entity.Products.UserRecommendationId;
import SpringClass.shop.entity.Users.Users;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRecommendationRepository extends JpaRepository<UserRecommendation, UserRecommendationId> {
    List<UserRecommendation> findByUserOrderByScoreDesc(Users user, Pageable pageable);
}
