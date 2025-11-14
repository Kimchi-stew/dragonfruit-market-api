package SpringClass.shop.repository;

import SpringClass.shop.entity.Reviews.Reviews;
import SpringClass.shop.entity.Sellers.Sellers;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Reviews, Long> {
    List<Reviews> findByProductIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long productId);
    Optional<Reviews> findByIdAndDeletedAtIsNull(Long ReviewId);
}
