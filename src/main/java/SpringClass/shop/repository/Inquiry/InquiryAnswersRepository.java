package SpringClass.shop.repository.Inquiry;

import SpringClass.shop.entity.Inquiry.InquiryAnswers;
import SpringClass.shop.entity.Inquiry.Inquiries;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InquiryAnswersRepository extends JpaRepository<InquiryAnswers, Long> {
    Optional<InquiryAnswers> findByInquiry(Inquiries inquiry);
}
