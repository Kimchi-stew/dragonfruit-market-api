package SpringClass.shop.service;

import SpringClass.shop.dto.Inquiry.request.InquiryAnswerRequest;
import SpringClass.shop.dto.Inquiry.request.InquiryCreateRequest;
import SpringClass.shop.dto.Inquiry.response.InquiryAnswerResponse;
import SpringClass.shop.dto.Inquiry.response.InquiryCreateResponse;
import SpringClass.shop.entity.Inquiry.InquiryAnswers;
import SpringClass.shop.entity.Inquiry.Inquiries;
import SpringClass.shop.entity.Products.Products;
import SpringClass.shop.entity.Sellers.Sellers;
import SpringClass.shop.entity.Users.Users;
import SpringClass.shop.enums.GenderRole;
import SpringClass.shop.enums.InquiryStatus;
import SpringClass.shop.enums.UserRole;
import SpringClass.shop.exceptions.common.ForbiddenException;
import SpringClass.shop.exceptions.product.ProductNotFoundException;
import SpringClass.shop.repository.Inquiry.InquiryAnswersRepository;
import SpringClass.shop.repository.Inquiry.InquiriesRepository;
import SpringClass.shop.repository.Products.ProductsRepository;
import SpringClass.shop.repository.Sellers.SellersRepository;
import SpringClass.shop.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InquiryServiceTest {

    @Mock private SecurityUtils securityUtils;
    @Mock private InquiriesRepository inquiriesRepository;
    @Mock private InquiryAnswersRepository inquiryAnswersRepository;
    @Mock private ProductsRepository productsRepository;
    @Mock private SellersRepository sellersRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks private InquiryService inquiryService;

    private Users user;
    private Users sellerUser;
    private Sellers seller;
    private Products product;
    private Inquiries inquiry;

    @BeforeEach
    void setUp() {
        user = Users.builder()
                .id(1L).email("u@t.com").nickname("유저")
                .gender(GenderRole.M).userRole(UserRole.USER).build();

        sellerUser = Users.builder()
                .id(2L).email("s@t.com").nickname("판매자")
                .gender(GenderRole.M).userRole(UserRole.SELLER).build();

        seller = Sellers.builder().id(1L).user(sellerUser).storeName("샵").build();

        product = Products.builder()
                .id(1L).name("상품").price(new BigDecimal("10000")).stock(5).seller(seller).build();

        inquiry = Inquiries.builder()
                .id(1L).user(user).product(product)
                .title("문의 제목").content("문의 내용")
                .status(InquiryStatus.PENDING).build();
    }

    @Test
    @DisplayName("상품에 대한 문의 생성 성공")
    void createInquiry_withProduct_success() {
        InquiryCreateRequest request = InquiryCreateRequest.builder()
                .productId(1L).sellerId(null).title("문의 제목").content("문의 내용").build();

        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(productsRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(product));
        when(inquiriesRepository.save(any())).thenReturn(inquiry);

        InquiryCreateResponse response = inquiryService.createInquiry(request);

        assertThat(response.getTitle()).isEqualTo("문의 제목");
        assertThat(response.getStatus()).isEqualTo(InquiryStatus.PENDING);
    }

    @Test
    @DisplayName("상품도 판매자도 없이 문의 생성 시 IllegalArgumentException 발생")
    void createInquiry_noTargetSpecified_throwsException() {
        InquiryCreateRequest request = InquiryCreateRequest.builder()
                .productId(null).sellerId(null).title("제목").content("내용").build();

        when(securityUtils.getCurrentUser()).thenReturn(user);

        assertThatThrownBy(() -> inquiryService.createInquiry(request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("존재하지 않는 상품에 문의 시 ProductNotFoundException 발생")
    void createInquiry_productNotFound_throwsException() {
        InquiryCreateRequest request = InquiryCreateRequest.builder()
                .productId(999L).sellerId(null).title("제목").content("내용").build();

        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(productsRepository.findByIdAndDeletedAtIsNull(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inquiryService.createInquiry(request))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    @DisplayName("판매자가 문의 답변 성공")
    void answerInquiry_asSeller_success() {
        InquiryAnswerRequest request = new InquiryAnswerRequest("답변 내용");
        InquiryAnswers answer = InquiryAnswers.builder()
                .id(1L).inquiry(inquiry).responder(sellerUser).content("답변 내용").build();

        when(securityUtils.getCurrentUser()).thenReturn(sellerUser);
        when(sellersRepository.findByUser(sellerUser)).thenReturn(Optional.of(seller));
        when(inquiriesRepository.findById(1L)).thenReturn(Optional.of(inquiry));
        when(inquiryAnswersRepository.save(any())).thenReturn(answer);
        doNothing().when(notificationService).send(any(), any(), any(), any());

        InquiryAnswerResponse response = inquiryService.answerInquiry(1L, request);

        assertThat(response.getContent()).isEqualTo("답변 내용");
        assertThat(inquiry.getStatus()).isEqualTo(InquiryStatus.ANSWERED);
    }

    @Test
    @DisplayName("판매자도 관리자도 아닌 유저가 답변 시 ForbiddenException 발생")
    void answerInquiry_notSellerOrAdmin_throwsException() {
        InquiryAnswerRequest request = new InquiryAnswerRequest("답변");

        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(sellersRepository.findByUser(user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inquiryService.answerInquiry(1L, request))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("이미 답변된 문의에 재답변 시 ForbiddenException 발생")
    void answerInquiry_alreadyAnswered_throwsException() {
        Inquiries answeredInquiry = Inquiries.builder()
                .id(1L).user(user).product(product)
                .title("제목").content("내용")
                .status(InquiryStatus.ANSWERED).build();

        InquiryAnswerRequest request = new InquiryAnswerRequest("또 답변");

        when(securityUtils.getCurrentUser()).thenReturn(sellerUser);
        when(sellersRepository.findByUser(sellerUser)).thenReturn(Optional.of(seller));
        when(inquiriesRepository.findById(1L)).thenReturn(Optional.of(answeredInquiry));

        assertThatThrownBy(() -> inquiryService.answerInquiry(1L, request))
                .isInstanceOf(ForbiddenException.class);
    }
}
