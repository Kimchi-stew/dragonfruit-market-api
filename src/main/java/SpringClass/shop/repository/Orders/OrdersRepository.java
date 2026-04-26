package SpringClass.shop.repository.Orders;

import SpringClass.shop.entity.Orders.Orders;
import SpringClass.shop.entity.Users.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrdersRepository extends JpaRepository<Orders, Long> {
    Page<Orders> findByUserOrderByCreatedAtDesc(Users user, Pageable pageable);
    Optional<Orders> findByIdAndUser(Long id, Users user);
}
