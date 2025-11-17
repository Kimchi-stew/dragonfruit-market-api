package SpringClass.shop.repository;
import SpringClass.shop.entity.Products.Products;
import SpringClass.shop.entity.Sellers.Sellers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductsRepository extends JpaRepository<Products, Long> {
    // 해당 상품 조회
    Optional<Products> findByIdAndDeletedAtIsNull(Long id);
    // 최신순 조회
    List<Products> findAllByDeletedAtIsNullOrderByCreatedAtDesc();
    // 오래된순 조회
    List<Products> findAllByDeletedAtIsNullOrderByCreatedAtAsc();

    // 좋아요 많은순 + 최신순
    List<Products> findAllByDeletedAtIsNullOrderByLikeCountDescCreatedAtDesc();

    // 가격 낮은순 + 최신순
    List<Products> findAllByDeletedAtIsNullOrderByPriceAscCreatedAtDesc();
    // 가격 높은순 + 최신순
    List<Products> findAllByDeletedAtIsNullOrderByPriceDescCreatedAtDesc();

    // 한 상점의 모든 상품
    List<Products> findAllBySeller(Sellers seller);

    // 한 상점의 모든 상품 + 최신순
    List<Products> findAllBySellerAndDeletedAtIsNullOrderByCreatedAtDesc(Sellers seller);
    // 상품 이름으로 검색 + 좋아요순 + 최신순
    List<Products> findByDeletedAtIsNullAndNameContainingIgnoreCaseOrderByLikeCountDescCreatedAtDesc(String keyword);

//    @Query("""
//    SELECT p FROM Products p
//    WHERE p.deletedAt IS NULL
//      AND (
//            LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
//            OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
//            OR LOWER(p.shop.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
//          )
//    ORDER BY
//        CASE WHEN LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) THEN 0 ELSE 1 END,
//        p.likeCount DESC,
//        p.createdAt DESC
//""")
//    List<Products> searchProducts(@Param("keyword") String keyword);

}
