package SpringClass.shop.repository.Notifications;

import SpringClass.shop.entity.Notifications.Notifications;
import SpringClass.shop.entity.Users.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface NotificationRepository extends JpaRepository<Notifications, Long> {

    // 읽지 않은 알림만 조회
    Page<Notifications> findAllByUsersAndIsReadFalseOrderByCreatedAtDesc(Users users, Pageable pageable);

    // 읽지 않은 알림 개수 조회
    int countByUsersAndIsReadFalse(Users user);

    // 최신순 알림 조회
    Page<Notifications> findAllByUsersOrderByCreatedAtDesc(Users user, Pageable pageable);
}
