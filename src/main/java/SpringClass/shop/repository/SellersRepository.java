package SpringClass.shop.repository;

import SpringClass.shop.entity.Products.Products;
import SpringClass.shop.entity.Sellers;
import SpringClass.shop.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SellersRepository extends JpaRepository<Sellers, Long> {
    Optional<Sellers> findByUser(Users users);
    List<Sellers> findAllByDeletedAtIsNullOrderByCreatedAtDesc();
    Optional<Sellers> findByIdAndDeletedAtIsNull(Long id);

}
