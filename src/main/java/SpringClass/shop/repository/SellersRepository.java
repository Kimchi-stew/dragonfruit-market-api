package SpringClass.shop.repository;

import SpringClass.shop.entity.Sellers.Sellers;
import SpringClass.shop.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SellersRepository extends JpaRepository<Sellers, Long> {
    Optional<Sellers> findByUser(Users users);
    Optional<Sellers> findByIdAndDeletedAtIsNull(Long id);
    // 최신순
    List<Sellers> findAllByDeletedAtIsNullOrderByCreatedAtDesc();
    // 오래된순
    List<Sellers> findAllByDeletedAtIsNullOrderByCreatedAtAsc();
    // 팔로우순 + 최신순
    List<Sellers> findAllByDeletedAtIsNullOrderByFollowCountDescCreatedAtDesc();
    // 좋아요순 + 최신순
    List<Sellers> findAllByDeletedAtIsNullOrderByLikeCountDescCreatedAtDesc();

}
