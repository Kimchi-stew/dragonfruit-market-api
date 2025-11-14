package SpringClass.shop.repository;
import SpringClass.shop.entity.Products.Products;
import SpringClass.shop.entity.Sellers.Sellers;
import SpringClass.shop.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProductsRepository extends JpaRepository<Products, Long> {
    Optional<Products> findByIdAndDeletedAtIsNull(Long id);
    // 최신순 조회
    List<Products> findAllByDeletedAtIsNullOrderByCreatedAtDesc();
    // 가격 낮은순 + 최신순
    List<Products> findAllByDeletedAtIsNullOrderByPriceAscCreatedAtDesc();
    // 가격 높은순 + 최신순
    List<Products> findAllByDeletedAtIsNullOrderByPriceDescCreatedAtDesc();

    List<Products> findAllBySeller(Sellers seller);

    List<Products> findAllBySellerAndDeletedAtIsNullOrderByCreatedAtDesc(Sellers seller);
}
