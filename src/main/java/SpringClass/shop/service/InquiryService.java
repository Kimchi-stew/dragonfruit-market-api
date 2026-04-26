package SpringClass.shop.service;

import SpringClass.shop.dto.Inquiry.request.InquiryAnswerRequest;
import SpringClass.shop.dto.Inquiry.request.InquiryCreateRequest;
import SpringClass.shop.dto.Inquiry.response.InquiryAnswerResponse;
import SpringClass.shop.dto.Inquiry.response.InquiryCreateResponse;
import SpringClass.shop.dto.Inquiry.response.InquiryListResponse;
import SpringClass.shop.entity.Inquiry.InquiryAnswers;
import SpringClass.shop.entity.Inquiry.Inquiries;
import SpringClass.shop.entity.Products.Products;
import SpringClass.shop.entity.Sellers.Sellers;
import SpringClass.shop.entity.Users.Users;
import SpringClass.shop.enums.InquiryStatus;
import SpringClass.shop.enums.NotificationType;
import SpringClass.shop.enums.UserRole;
import SpringClass.shop.exceptions.ForbiddenException;
import SpringClass.shop.exceptions.InquiryNotFoundException;
import SpringClass.shop.exceptions.ProductNotFoundException;
import SpringClass.shop.exceptions.SellerNotFoundException;
import SpringClass.shop.repository.Inquiry.InquiryAnswersRepository;
import SpringClass.shop.repository.Inquiry.InquiriesRepository;
import SpringClass.shop.repository.Products.ProductsRepository;
import SpringClass.shop.repository.Sellers.SellersRepository;
import SpringClass.shop.security.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private final SecurityUtils securityUtils;
    private final InquiriesRepository inquiriesRepository;
    private final InquiryAnswersRepository inquiryAnswersRepository;
    private final ProductsRepository productsRepository;
    private final SellersRepository sellersRepository;
    private final NotificationService notificationService;

    @Transactional
    public InquiryCreateResponse createInquiry(InquiryCreateRequest request) {
        Users user = securityUtils.getCurrentUser();

        Products product = null;
        if (request.getProductId() != null) {
            product = productsRepository.findByIdAndDeletedAtIsNull(request.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException("상품을 찾을 수 없습니다."));
        }

        Sellers seller = null;
        if (request.getSellerId() != null) {
            seller = sellersRepository.findByIdAndDeletedAtIsNull(request.getSellerId())
                    .orElseThrow(() -> new SellerNotFoundException("판매자를 찾을 수 없습니다."));
        }

        Inquiries inquiry = Inquiries.builder()
                .user(user)
                .product(product)
                .seller(seller)
                .title(request.getTitle())
                .content(request.getContent())
                .status(InquiryStatus.PENDING)
                .build();

        inquiriesRepository.save(inquiry);
        return InquiryCreateResponse.from(inquiry);
    }

    public Page<InquiryListResponse> getMyInquiries(Pageable pageable) {
        Users user = securityUtils.getCurrentUser();
        Page<Inquiries> inquiries = inquiriesRepository.findByUserOrderByCreatedAtDesc(user, pageable);

        return inquiries.map(inquiry -> {
            InquiryListResponse.AnswerSummary answerSummary = inquiryAnswersRepository.findByInquiry(inquiry)
                    .map(a -> InquiryListResponse.AnswerSummary.builder()
                            .content(a.getContent())
                            .createdAt(a.getCreatedAt())
                            .build())
                    .orElse(null);

            return InquiryListResponse.builder()
                    .inquiryId(inquiry.getId())
                    .title(inquiry.getTitle())
                    .status(inquiry.getStatus())
                    .createdAt(inquiry.getCreatedAt())
                    .answer(answerSummary)
                    .build();
        });
    }

    @Transactional
    public InquiryAnswerResponse answerInquiry(Long inquiryId, InquiryAnswerRequest request) {
        Users user = securityUtils.getCurrentUser();

        boolean isSeller = sellersRepository.findByUser(user).isPresent();
        boolean isAdmin = user.getUserRole() == UserRole.ADMIN;
        if (!isSeller && !isAdmin) {
            throw new ForbiddenException("판매자 또는 관리자만 답변할 수 있습니다.");
        }

        Inquiries inquiry = inquiriesRepository.findById(inquiryId)
                .orElseThrow(() -> new InquiryNotFoundException("문의를 찾을 수 없습니다."));

        InquiryAnswers answer = InquiryAnswers.builder()
                .inquiry(inquiry)
                .responder(user)
                .content(request.getContent())
                .build();

        inquiryAnswersRepository.save(answer);

        inquiry.setStatus(InquiryStatus.ANSWERED);
        inquiriesRepository.save(inquiry);

        notificationService.send(
                inquiry.getUser(),
                NotificationType.INQUIRY_ANSWERED,
                inquiry.getId(),
                "문의하신 내용에 답변이 등록되었습니다."
        );

        return InquiryAnswerResponse.from(answer);
    }
}
