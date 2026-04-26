package SpringClass.shop.repository.Inquiry;

import SpringClass.shop.entity.Inquiry.Inquiries;
import SpringClass.shop.entity.Users.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiriesRepository extends JpaRepository<Inquiries, Long> {
    Page<Inquiries> findByUserOrderByCreatedAtDesc(Users user, Pageable pageable);
}
