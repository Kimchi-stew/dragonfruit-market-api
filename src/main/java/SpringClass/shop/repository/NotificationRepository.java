package SpringClass.shop.repository;

import SpringClass.shop.entity.Notifications;
import SpringClass.shop.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface NotificationRepository extends JpaRepository<Notifications, Long> {
    // 특정 사용자 알림 전체 조회
    List<Notifications> findAllByUsersOrderByCreatedAtDesc(Users users);

    // 읽지 않은 알림만 조회
    List<Notifications> findAllByUsersAndIsReadFalseOrderByCreatedAtDesc(Users users);

    // 읽지 않은 알림 개수 조회
    int countByUsersAndIsReadFalse(Users user);

    // 최신순 알림 조회
    Page<Notifications> findAllByUsersOrderByCreatedAtDesc(Users user, Pageable pageable);
}
