package SpringClass.shop.repository.Payments;

import SpringClass.shop.entity.Orders.Orders;
import SpringClass.shop.entity.Payments.Payments;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentsRepository extends JpaRepository<Payments, Long> {
    Optional<Payments> findByOrder(Orders order);
}
