package SpringClass.shop.repository;

import SpringClass.shop.entity.Reviews.Reviews;
import SpringClass.shop.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Reviews, Long> {
    // 최신순
    List<Reviews> findByProductIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long productId);

    // 오래된순
    List<Reviews> findByProductIdAndDeletedAtIsNullOrderByCreatedAtAsc(Long productId);

    // 좋아요순 + 최신순
    List<Reviews> findByProductIdAndDeletedAtIsNullOrderByLikeCountDescCreatedAtDesc(Long productId);

    Optional<Reviews> findByIdAndDeletedAtIsNull(Long ReviewId);

    List<Reviews> findByUserAndDeletedAtIsNullOrderByCreatedAtDesc(Users user);
}
