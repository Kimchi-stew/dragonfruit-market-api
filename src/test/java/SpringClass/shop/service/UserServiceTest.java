package SpringClass.shop.service;

import SpringClass.shop.dto.Products.response.ProductListDTO;
import SpringClass.shop.dto.Users.request.SignupRequest;
import SpringClass.shop.dto.Users.request.SocialSignupRequest;
import SpringClass.shop.dto.Users.request.UserPasswordDTO;
import SpringClass.shop.dto.Users.request.UserProfileRequest;
import SpringClass.shop.dto.Users.response.UserProfileResponse;
import SpringClass.shop.entity.Products.ProductLikes;
import SpringClass.shop.entity.Products.ProductWish;
import SpringClass.shop.entity.Products.Products;
import SpringClass.shop.entity.Sellers.SellerLikes;
import SpringClass.shop.entity.Sellers.Sellers;
import SpringClass.shop.entity.Users.Users;
import SpringClass.shop.enums.GenderRole;
import SpringClass.shop.enums.MediaEntityType;
import SpringClass.shop.enums.UserRole;
import SpringClass.shop.exceptions.common.ForbiddenException;
import SpringClass.shop.exceptions.seller.SellerNotFoundException;
import SpringClass.shop.exceptions.user.PasswordMismatchException;
import SpringClass.shop.exceptions.user.UserAlreadyExistException;
import SpringClass.shop.repository.Medias.MediasRepository;
import SpringClass.shop.repository.Products.ProductLikeRepository;
import SpringClass.shop.repository.Products.ProductWishRepository;
import SpringClass.shop.repository.Products.ProductsRepository;
import SpringClass.shop.repository.Reviews.ReviewRepository;
import SpringClass.shop.repository.Sellers.SellerFollowRepository;
import SpringClass.shop.repository.Sellers.SellerLikesRepository;
import SpringClass.shop.repository.Sellers.SellersRepository;
import SpringClass.shop.repository.Users.UsersRepository;
import SpringClass.shop.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private SecurityUtils SecurityUtils;
    @Mock private MediasRepository mediasRepository;
    @Mock private ProductLikeRepository productLikeRepository;
    @Mock private ProductsRepository productsRepository;
    @Mock private ProductWishRepository productWishRepository;
    @Mock private SellerLikesRepository sellerLikesRepository;
    @Mock private SellerFollowRepository sellerFollowRepository;
    @Mock private UsersRepository usersRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private ReviewRepository reviewRepository;
    @Mock private SellersRepository sellersRepository;

    @InjectMocks private UserService userService;

    private Users user;
    private Sellers seller;

    @BeforeEach
    void setUp() {
        user = Users.builder()
                .id(1L).email("u@t.com").nickname("유저")
                .password("encoded").gender(GenderRole.M).userRole(UserRole.USER).build();

        seller = Sellers.builder()
                .id(1L).user(user).storeName("내상점")
                .description("설명").image("img.jpg")
                .likeCount(5).followCount(3).build();
    }

    @Test
    @DisplayName("회원가입 성공")
    void signup_success() {
        SignupRequest request = new SignupRequest();
        request.setEmail("new@t.com");
        request.setPassword("pw");
        request.setNickname("신규");
        request.setGender(GenderRole.W);

        when(usersRepository.findByEmail("new@t.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pw")).thenReturn("encoded-new");

        userService.signup(request);

        verify(usersRepository).save(any(Users.class));
    }

    @Test
    @DisplayName("이미 존재하는 이메일로 회원가입 시 UserAlreadyExistException 발생")
    void signup_duplicateEmail_throwsException() {
        SignupRequest request = new SignupRequest();
        request.setEmail("u@t.com");
        request.setPassword("pw");

        when(usersRepository.findByEmail("u@t.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.signup(request))
                .isInstanceOf(UserAlreadyExistException.class);
    }

    @Test
    @DisplayName("소셜 유저 추가 정보 입력 성공")
    void completeSocialSignup_success() {
        Users socialUser = Users.builder()
                .id(3L).email("social@t.com").provider("kakao")
                .gender(GenderRole.M).userRole(UserRole.USER).build();

        SocialSignupRequest request = new SocialSignupRequest();
        request.setNickname("소셜유저");
        request.setGender(GenderRole.M);

        when(SecurityUtils.getCurrentUser()).thenReturn(socialUser);

        userService.completeSocialSignup(request);

        assertThat(socialUser.getNickname()).isEqualTo("소셜유저");
        verify(usersRepository).save(socialUser);
    }

    @Test
    @DisplayName("소셜 유저가 아닌 유저가 추가 정보 입력 시 ForbiddenException 발생")
    void completeSocialSignup_notSocialUser_throwsException() {
        SocialSignupRequest request = new SocialSignupRequest();
        request.setNickname("일반유저");

        when(SecurityUtils.getCurrentUser()).thenReturn(user);

        assertThatThrownBy(() -> userService.completeSocialSignup(request))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("프로필 조회 성공")
    void getProfile_success() {
        when(SecurityUtils.getCurrentUser()).thenReturn(user);
        when(mediasRepository.findTopByEntityTypeAndEntityIdOrderByCreatedAtDesc(MediaEntityType.PROFILE, 1L))
                .thenReturn(Optional.empty());

        UserProfileResponse response = userService.getProfile();

        assertThat(response.getEmail()).isEqualTo("u@t.com");
        assertThat(response.getNickname()).isEqualTo("유저");
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    void patchPassword_success() {
        UserPasswordDTO request = new UserPasswordDTO("oldpw", "newpw");

        when(SecurityUtils.getCurrentUser()).thenReturn(user);
        when(passwordEncoder.matches("oldpw", "encoded")).thenReturn(true);
        when(passwordEncoder.encode("newpw")).thenReturn("new-encoded");

        String result = userService.patchPassword(request);

        assertThat(result).isEqualTo("비밀번호가 변경되었습니다.");
        verify(usersRepository).save(user);
    }

    @Test
    @DisplayName("현재 비밀번호 불일치 시 PasswordMismatchException 발생")
    void patchPassword_mismatch_throwsException() {
        UserPasswordDTO request = new UserPasswordDTO("wrongpw", "newpw");

        when(SecurityUtils.getCurrentUser()).thenReturn(user);
        when(passwordEncoder.matches("wrongpw", "encoded")).thenReturn(false);

        assertThatThrownBy(() -> userService.patchPassword(request))
                .isInstanceOf(PasswordMismatchException.class);
    }

    @Test
    @DisplayName("좋아요한 상품 목록 조회 (기본 최신순)")
    void getLikeProducts_defaultSort_success() {
        Sellers productSeller = Sellers.builder().id(2L).storeName("판매샵").image("s.jpg").build();
        Products product = Products.builder()
                .id(1L).name("상품").price(new BigDecimal("5000"))
                .likeCount(3).seller(productSeller).build();
        ProductLikes like = ProductLikes.builder().user(user).product(product).build();

        when(SecurityUtils.getCurrentUser()).thenReturn(user);
        when(productLikeRepository.findByUserAndProduct_DeletedAtIsNullOrderByCreatedAtDesc(user))
                .thenReturn(List.of(like));

        List<ProductListDTO> result = userService.getLikeProducts("new");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("상품");
    }

    @Test
    @DisplayName("찜한 상품 목록 조회")
    void getWishProducts_success() {
        Sellers productSeller = Sellers.builder().id(2L).storeName("판매샵").image("s.jpg").build();
        Products product = Products.builder()
                .id(1L).name("찜상품").price(new BigDecimal("8000"))
                .likeCount(2).seller(productSeller).build();
        ProductWish wish = ProductWish.builder().user(user).product(product).build();

        when(SecurityUtils.getCurrentUser()).thenReturn(user);
        when(productWishRepository.findByUserAndProduct_DeletedAtIsNull(user)).thenReturn(List.of(wish));

        List<ProductListDTO> result = userService.getWishProducts();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("찜상품");
    }

    @Test
    @DisplayName("내 상점 조회 성공")
    void getMySeller_success() {
        when(SecurityUtils.getCurrentUser()).thenReturn(user);
        when(sellersRepository.findByUser(user)).thenReturn(Optional.of(seller));
        when(sellerFollowRepository.existsByUserAndSellers(user, seller)).thenReturn(false);

        var response = userService.getMySeller();

        assertThat(response.getStoreName()).isEqualTo("내상점");
        assertThat(response.isFollowed()).isFalse();
    }

    @Test
    @DisplayName("상점이 없을 때 내 상점 조회 시 SellerNotFoundException 발생")
    void getMySeller_notFound_throwsException() {
        when(SecurityUtils.getCurrentUser()).thenReturn(user);
        when(sellersRepository.findByUser(user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getMySeller())
                .isInstanceOf(SellerNotFoundException.class);
    }
}
