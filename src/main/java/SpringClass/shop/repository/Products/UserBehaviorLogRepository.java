package SpringClass.shop.repository.Products;

import SpringClass.shop.entity.Products.UserBehaviorLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBehaviorLogRepository extends JpaRepository<UserBehaviorLog, Long> {
}
