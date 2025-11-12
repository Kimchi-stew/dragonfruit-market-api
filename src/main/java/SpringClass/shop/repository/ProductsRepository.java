package SpringClass.shop.repository;

import SpringClass.shop.entity.Products.Products;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductsRepository extends JpaRepository<Products, Long> {
    // 최신순
    @Query("SELECT p FROM Products p ORDER BY p.createdAt DESC")
    List<Products> findAllByOrderByCreatedAtDesc();
    // 고가순 + (최신순)
    @Query("SELECT p FROM Products p ORDER BY p.price DESC, p.createdAt DESC")
    List<Products> findAllByOrderByPriceDescCreatedAtDesc();
    // 저렴한순 + (최신순)
    @Query("SELECT p FROM Products p ORDER BY p.price ASC, p.createdAt DESC")
    List<Products> findAllByOrderByPriceAscCreatedAtDesc();
}
