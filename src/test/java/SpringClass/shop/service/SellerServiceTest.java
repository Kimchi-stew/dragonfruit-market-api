package SpringClass.shop.service;

import SpringClass.shop.dto.Sellers.request.SellerRequest;
import SpringClass.shop.dto.Sellers.response.SellerDeleteDTO;
import SpringClass.shop.dto.Sellers.response.SellerFollowDTO;
import SpringClass.shop.dto.Sellers.response.SellerResponse;
import SpringClass.shop.dto.common.response.LikesResponseDTO;
import SpringClass.shop.entity.Products.Products;
import SpringClass.shop.entity.Sellers.SellerFollow;
import SpringClass.shop.entity.Sellers.SellerLikes;
import SpringClass.shop.entity.Sellers.Sellers;
import SpringClass.shop.entity.Users.Users;
import SpringClass.shop.enums.GenderRole;
import SpringClass.shop.enums.UserRole;
import SpringClass.shop.exceptions.common.ForbiddenException;
import SpringClass.shop.exceptions.seller.SellerNotFoundException;
import SpringClass.shop.repository.Products.ProductsRepository;
import SpringClass.shop.repository.Sellers.SellerFollowRepository;
import SpringClass.shop.repository.Sellers.SellerLikesRepository;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SellerServiceTest {

    @Mock private SecurityUtils SecurityUtils;
    @Mock private SellersRepository sellersRepository;
    @Mock private ProductsRepository productsRepository;
    @Mock private SellerLikesRepository sellerLikesRepository;
    @Mock private SellerFollowRepository sellerFollowRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks private SellerService sellerService;

    private Users user;
    private Users otherUser;
    private Sellers seller;

    @BeforeEach
    void setUp() {
        user = Users.builder()
                .id(1L).email("u@t.com").nickname("유저")
                .gender(GenderRole.M).userRole(UserRole.USER).build();

        otherUser = Users.builder()
                .id(2L).email("other@t.com").nickname("타인")
                .gender(GenderRole.M).userRole(UserRole.USER).build();

        seller = Sellers.builder()
                .id(1L).user(user).storeName("테스트샵")
                .description("설명").image("img.jpg")
                .likeCount(0).followCount(0)
                .createdAt(LocalDateTime.now()).build();
    }

    @Test
    @DisplayName("상점 생성 성공 및 유저 역할 SELLER로 변경")
    void createSeller_success() {
        SellerRequest request = new SellerRequest();
        request.setStoreName("새상점");
        request.setDescription("설명");
        request.setImage("img.jpg");

        when(SecurityUtils.getCurrentUser()).thenReturn(user);
        when(sellersRepository.save(any())).thenReturn(seller);

        SellerResponse response = sellerService.createSeller(request);

        assertThat(response.getStoreName()).isEqualTo("테스트샵");
        assertThat(user.getUserRole()).isEqualTo(UserRole.SELLER);
    }

    @Test
    @DisplayName("상점 수정 성공 (소유자)")
    void patchSeller_owner_success() {
        SellerRequest request = new SellerRequest();
        request.setStoreName("수정된상점");
        request.setDescription("새설명");
        request.setImage("new.jpg");

        when(SecurityUtils.getCurrentUser()).thenReturn(user);
        when(sellersRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(seller));
        when(sellerFollowRepository.existsByUserAndSellers(user, seller)).thenReturn(false);
        when(sellersRepository.save(any())).thenReturn(seller);

        SellerResponse response = sellerService.patchSeller(1L, request);

        assertThat(seller.getStoreName()).isEqualTo("수정된상점");
        assertThat(response.isFollowed()).isFalse();
    }

    @Test
    @DisplayName("상점 수정 시 소유자가 아니면 ForbiddenException 발생")
    void patchSeller_notOwner_throwsException() {
        SellerRequest request = new SellerRequest();
        request.setStoreName("수정시도");

        when(SecurityUtils.getCurrentUser()).thenReturn(otherUser);
        when(sellersRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(seller));

        assertThatThrownBy(() -> sellerService.patchSeller(1L, request))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("상점 삭제 성공 (소유자) - 상품 소프트 삭제 포함")
    void deleteSeller_owner_success() {
        Products product = Products.builder()
                .id(1L).name("상품").price(new BigDecimal("10000")).stock(5).seller(seller).build();

        when(SecurityUtils.getCurrentUser()).thenReturn(user);
        when(sellersRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(seller));
        when(productsRepository.findAllBySeller(seller)).thenReturn(List.of(product));

        SellerDeleteDTO result = sellerService.deleteSeller(1L);

        assertThat(seller.getDeletedAt()).isNotNull();
        assertThat(product.getDeletedAt()).isNotNull();
        assertThat(user.getUserRole()).isEqualTo(UserRole.USER);
        verify(productsRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("상점 삭제 시 소유자가 아니면 ForbiddenException 발생")
    void deleteSeller_notOwner_throwsException() {
        when(SecurityUtils.getCurrentUser()).thenReturn(otherUser);
        when(sellersRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(seller));

        assertThatThrownBy(() -> sellerService.deleteSeller(1L))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("상점 좋아요 추가")
    void likeSeller_addLike_success() {
        when(SecurityUtils.getCurrentUser()).thenReturn(user);
        when(sellersRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(seller));
        when(sellerLikesRepository.findByUserAndSellers(user, seller)).thenReturn(Optional.empty());

        LikesResponseDTO result = sellerService.likeSeller(1L);

        assertThat(result.isLiked()).isTrue();
        assertThat(seller.getLikeCount()).isEqualTo(1);
        verify(sellerLikesRepository).save(any(SellerLikes.class));
    }

    @Test
    @DisplayName("이미 좋아요한 상점 다시 좋아요 시 취소")
    void likeSeller_removeLike_success() {
        SellerLikes existing = SellerLikes.builder().user(user).sellers(seller).build();
        when(SecurityUtils.getCurrentUser()).thenReturn(user);
        when(sellersRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(seller));
        when(sellerLikesRepository.findByUserAndSellers(user, seller)).thenReturn(Optional.of(existing));

        LikesResponseDTO result = sellerService.likeSeller(1L);

        assertThat(result.isLiked()).isFalse();
        assertThat(seller.getLikeCount()).isEqualTo(-1);
        verify(sellerLikesRepository).delete(existing);
    }

    @Test
    @DisplayName("상점 팔로우 추가 및 알림 전송")
    void followSeller_follow_success() {
        when(SecurityUtils.getCurrentUser()).thenReturn(otherUser);
        when(sellersRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(seller));
        when(sellerFollowRepository.findByUserAndSellers(otherUser, seller)).thenReturn(Optional.empty());
        doNothing().when(notificationService).sendSellerFollowNotification(any(), any());

        SellerFollowDTO result = sellerService.followSeller(1L);

        assertThat(result.isFollowed()).isTrue();
        assertThat(seller.getFollowCount()).isEqualTo(1);
        verify(notificationService).sendSellerFollowNotification(seller, "타인");
    }

    @Test
    @DisplayName("이미 팔로우한 상점 다시 팔로우 시 언팔로우")
    void followSeller_unfollow_success() {
        SellerFollow existing = SellerFollow.builder().user(otherUser).sellers(seller).build();
        when(SecurityUtils.getCurrentUser()).thenReturn(otherUser);
        when(sellersRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(seller));
        when(sellerFollowRepository.findByUserAndSellers(otherUser, seller)).thenReturn(Optional.of(existing));

        SellerFollowDTO result = sellerService.followSeller(1L);

        assertThat(result.isFollowed()).isFalse();
        assertThat(seller.getFollowCount()).isEqualTo(-1);
        verify(sellerFollowRepository).delete(existing);
    }

    @Test
    @DisplayName("존재하지 않는 상점 조회 시 SellerNotFoundException 발생")
    void getSeller_notFound_throwsException() {
        when(SecurityUtils.getCurrentUser()).thenReturn(user);
        when(sellersRepository.findByIdAndDeletedAtIsNull(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sellerService.getSeller(999L))
                .isInstanceOf(SellerNotFoundException.class);
    }
}
