package SpringClass.shop.repository.Orders;

import SpringClass.shop.entity.Orders.OrderItems;
import SpringClass.shop.entity.Orders.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemsRepository extends JpaRepository<OrderItems, Long> {
    List<OrderItems> findByOrder(Orders order);
}
