package SpringClass.shop.repository;

import SpringClass.shop.entity.Reviews.ReviewImages;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewImagesRepository extends JpaRepository<ReviewImages, Long> {
}
