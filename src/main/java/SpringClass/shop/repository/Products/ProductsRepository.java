package SpringClass.shop.repository.Products;
import SpringClass.shop.entity.Products.Products;
import SpringClass.shop.entity.Sellers.Sellers;
import SpringClass.shop.enums.GenderRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductsRepository extends JpaRepository<Products, Long>, ProductsRepositoryCustom{
    // 해당 상품 조회
    Optional<Products> findByIdAndDeletedAtIsNull(Long id);

    // 최신순 조회
    Page<Products> findAllByDeletedAtIsNullOrderByCreatedAtDesc(Pageable pageable);
    // 오래된순 조회
    Page<Products> findAllByDeletedAtIsNullOrderByCreatedAtAsc(Pageable pageable);

    // 좋아요 많은순 + 최신순
    Page<Products> findAllByDeletedAtIsNullOrderByLikeCountDescCreatedAtDesc(Pageable pageable);

    // 가격 낮은순 + 최신순
    Page<Products> findAllByDeletedAtIsNullOrderByPriceAscCreatedAtDesc(Pageable pageable);
    // 가격 높은순 + 최신순
    Page<Products> findAllByDeletedAtIsNullOrderByPriceDescCreatedAtDesc(Pageable pageable);

    // 가격 높은순 + 좋아요순 + 최신순
    Page<Products> findAllByDeletedAtIsNullOrderByPriceDescLikeCountDescCreatedAtDesc(Pageable pageable);

    // 가격 높은순 + 오래된순
    Page<Products> findAllByDeletedAtIsNullOrderByPriceDescCreatedAtAsc(Pageable pageable);

    // 가격 낮은순 + 좋아요순 + 최신순
    Page<Products> findAllByDeletedAtIsNullOrderByPriceAscLikeCountDescCreatedAtDesc(Pageable pageable);

    // 가격 낮은순 + 오래된순
    Page<Products> findAllByDeletedAtIsNullOrderByPriceAscCreatedAtAsc(Pageable pageable);


    // 한 상점의 모든 상품
    List<Products> findAllBySeller(Sellers seller);

    // 한 상점의 모든 상품 + 최신순
    List<Products> findAllBySellerAndDeletedAtIsNullOrderByCreatedAtDesc(Sellers seller);
    // 상품 이름으로 검색 + 좋아요순 + 최신순
    List<Products> findByDeletedAtIsNullAndNameContainingIgnoreCaseOrderByLikeCountDescCreatedAtDesc(String keyword);

}
