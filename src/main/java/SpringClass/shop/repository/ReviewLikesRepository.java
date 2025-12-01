package SpringClass.shop.repository;

import SpringClass.shop.entity.Reviews.ReviewLikes;
import SpringClass.shop.entity.Reviews.Reviews;
import SpringClass.shop.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewLikesRepository extends JpaRepository<ReviewLikes, Long> {
    Optional<ReviewLikes> findByUserAndReview(Users users, Reviews reviews);
}
