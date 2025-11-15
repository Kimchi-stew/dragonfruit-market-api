package SpringClass.shop.repository;

import SpringClass.shop.entity.CartItems;
import SpringClass.shop.entity.Products.Products;
import SpringClass.shop.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemsRepository extends JpaRepository<CartItems, Long> {
    Optional<CartItems> findByUserAndProduct(Users user, Products product);
    List<CartItems> findByUser(Users user);
}
