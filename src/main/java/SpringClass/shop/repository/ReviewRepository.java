package SpringClass.shop.repository;

import SpringClass.shop.entity.Reviews.Reviews;
import SpringClass.shop.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Reviews, Long> {
    // 최신순
    List<Reviews> findByProductIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long productId);

    // 오래된순
    List<Reviews> findByProductIdAndDeletedAtIsNullOrderByCreatedAtAsc(Long productId);

    // 좋아요순 + 최신순
    List<Reviews> findByProductIdAndDeletedAtIsNullOrderByLikeCountDescCreatedAtDesc(Long productId);

    // 평점 높은순 + 좋아요순 + 최신순
    List<Reviews> findByProductIdAndDeletedAtIsNullOrderByRatingDescLikeCountDescCreatedAtDesc(Long productId);

    // 평점 높은순 + 오래된순
    List<Reviews> findByProductIdAndDeletedAtIsNullOrderByRatingDescCreatedAtAsc(Long productId);

    // 평점 높은순 + 최신순
    List<Reviews> findByProductIdAndDeletedAtIsNullOrderByRatingDescCreatedAtDesc(Long productId);

    // 평점 낮은순 + 좋아요순 + 최신순
    List<Reviews> findByProductIdAndDeletedAtIsNullOrderByRatingAscLikeCountDescCreatedAtDesc(Long productId);

    // 평점 낮은순 + 오래된순
    List<Reviews> findByProductIdAndDeletedAtIsNullOrderByRatingAscCreatedAtAsc(Long productId);

    // 평점 낮은순 + 최신순
    List<Reviews> findByProductIdAndDeletedAtIsNullOrderByRatingAscCreatedAtDesc(Long productId);

    Optional<Reviews> findByIdAndDeletedAtIsNull(Long ReviewId);

    List<Reviews> findByUserAndDeletedAtIsNullOrderByCreatedAtDesc(Users user);

    // 평균 평점
    @Query("SELECT AVG(r.rating) FROM Reviews r WHERE r.product.id = :productId")
    Double findAverageRating(@Param("productId") Long productId);
}
