package SpringClass.shop.service;


import SpringClass.shop.dto.Sellers.request.SellerRequest;
import SpringClass.shop.dto.Sellers.response.SellerDeleteDTO;
import SpringClass.shop.dto.Sellers.response.SellerFollowDTO;
import SpringClass.shop.dto.Sellers.response.SellerListDTO;
import SpringClass.shop.dto.Sellers.response.SellerResponse;
import SpringClass.shop.dto.common.response.LikesResponseDTO;
import SpringClass.shop.entity.Products.Products;
import SpringClass.shop.entity.Sellers.SellerFollow;
import SpringClass.shop.entity.Sellers.SellerLikes;
import SpringClass.shop.entity.Sellers.Sellers;
import SpringClass.shop.entity.Users.Users;
import SpringClass.shop.exceptions.ForbiddenException;
import SpringClass.shop.exceptions.SellerNotFoundException;
import SpringClass.shop.repository.Products.ProductsRepository;
import SpringClass.shop.repository.Sellers.SellerFollowRepository;
import SpringClass.shop.repository.Sellers.SellerLikesRepository;
import SpringClass.shop.repository.Sellers.SellersRepository;
import SpringClass.shop.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SellerService {
    private final SecurityUtils SecurityUtils;
    private final SellersRepository sellersRepository;
    private final ProductsRepository productsRepository;
    private final SellerLikesRepository sellerLikesRepository;
    private final SellerFollowRepository sellerFollowRepository;
    private final NotificationService notificationService;

    public SellerResponse createSeller(SellerRequest request) {
        // user 정보 가져오기 (baarer token에서 추출)
        Users user = SecurityUtils.getCurrentUser();

        Sellers sellers = Sellers.builder()
                .user(user)
                .storeName(request.getStoreName())
                .description(request.getDescription())
                .image(request.getImage())
                .likeCount(0)
                .followCount(0)
                .createdAt(LocalDateTime.now())
                .build();
        sellersRepository.save(sellers);

        Sellers savedSeller = sellersRepository.save(sellers);

        return SellerResponse.builder()
                .id(savedSeller.getId())
                .userId(user.getId())
                .storeName(savedSeller.getStoreName())
                .description(savedSeller.getDescription())
                .image(savedSeller.getImage())
                .likeCount(0)
                .followCount(0)
                .followed(false)
                .createdAt(savedSeller.getCreatedAt())
                .build();
    }

    public List<SellerListDTO> getSellers(String sort) {
        List<Sellers> sellers;
        if ("follow".equalsIgnoreCase(sort)) {
            sellers = sellersRepository.findAllByDeletedAtIsNullOrderByFollowCountDescCreatedAtDesc();
        } else if ("like".equalsIgnoreCase(sort)) {
            sellers = sellersRepository.findAllByDeletedAtIsNullOrderByLikeCountDescCreatedAtDesc();
        } else if ("old".equalsIgnoreCase(sort)) {
            sellers = sellersRepository.findAllByDeletedAtIsNullOrderByCreatedAtAsc();
        } else {
            // 기본으로 최신순 정렬
            sellers = sellersRepository.findAllByDeletedAtIsNullOrderByCreatedAtDesc();
        }
        return sellers.stream()
                .map(seller -> SellerListDTO.builder()
                        .id(seller.getId())
                        .storeName(seller.getStoreName())
                        .image(seller.getImage())
                        .likeCount(seller.getLikeCount())
                        .followCount(seller.getFollowCount())
                        .build())
                .collect(Collectors.toList());
    }

    public SellerResponse patchSeller(Long id, SellerRequest request) {
        // user 정보 가져오기 (baarer token에서 추출)
        Users user = SecurityUtils.getCurrentUser();

        Sellers sellers = sellersRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new SellerNotFoundException("상점을 찾을 수 없습니다."));

        // 소유자 확인
        if (!sellers.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("수정할 수 있는 권한이 없습니다.");
        }

        sellers.setStoreName(request.getStoreName());
        sellers.setDescription(request.getDescription());
        sellers.setImage(request.getImage());

        Sellers savedSeller = sellersRepository.save(sellers);
        boolean followed = sellerFollowRepository.existsByUserAndSellers(user, sellers);
        return SellerResponse.builder()
                .id(savedSeller.getId())
                .userId(user.getId())
                .storeName(savedSeller.getStoreName())
                .description(savedSeller.getDescription())
                .likeCount(savedSeller.getLikeCount())
                .followCount(savedSeller.getFollowCount())
                .followed(followed)
                .image(savedSeller.getImage())
                .createdAt(savedSeller.getCreatedAt())
                .build();
    }

    public SellerResponse getSeller(Long id) {
        Users user = SecurityUtils.getCurrentUser();
        Sellers sellers = sellersRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new SellerNotFoundException("상점을 찾을 수 없습니다."));

        boolean followed = sellerFollowRepository.existsByUserAndSellers(user, sellers);

        return SellerResponse.builder()
                        .id(sellers.getId())
                        .userId(sellers.getUser().getId())
                        .storeName(sellers.getStoreName())
                        .image(sellers.getImage())
                        .likeCount(sellers.getLikeCount())
                        .followCount(sellers.getFollowCount())
                        .createdAt(sellers.getCreatedAt())
                        .followed(followed)
                        .build();
    }

    public SellerDeleteDTO deleteSeller(Long id) {
        Users user = SecurityUtils.getCurrentUser();

        Sellers sellers = sellersRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new SellerNotFoundException("상점을 찾을 수 없습니다."));

        // 소유자 확인
        if (!sellers.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("삭제할 수 있는 권한이 없습니다.");
        }
        sellers.setDeletedAt(LocalDateTime.now());
        // 상품들도 같이 논리 삭제
        List<Products> products = productsRepository.findAllBySeller(sellers);
        products.forEach(product -> product.setDeletedAt(LocalDateTime.now()));

        productsRepository.saveAll(products);
        sellersRepository.save(sellers);

        return SellerDeleteDTO.builder()
                .storeName(sellers.getStoreName())
                .image(sellers.getImage())
                .deletedAt(sellers.getDeletedAt())
                .build();
    }

    public LikesResponseDTO likeSeller(Long id) {
        Users user = SecurityUtils.getCurrentUser();

        Sellers sellers = sellersRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new SellerNotFoundException("상점을 찾을 수 없습니다."));

        // 좋아요 여부 확인
        Optional<SellerLikes> existing = sellerLikesRepository.findByUserAndSellers(user, sellers);
        boolean liked;
        if (existing.isPresent()) {
            sellerLikesRepository.delete(existing.get()); // 좋아요 취소
            sellers.setLikeCount(sellers.getLikeCount() - 1);
            sellersRepository.save(sellers);
            liked = false;
        } else {
            SellerLikes sellerLikes = SellerLikes.builder()
                    .user(user)
                    .sellers(sellers)
                    .createdAt(LocalDateTime.now())
                    .build();
            sellerLikesRepository.save(sellerLikes);
            sellers.setLikeCount(sellers.getLikeCount() + 1);
            sellersRepository.save(sellers);
            liked = true;
        }



        return new LikesResponseDTO(liked, sellers.getLikeCount());
    }

    public SellerFollowDTO followSeller(Long id) {
        Users user = SecurityUtils.getCurrentUser();

        Sellers sellers = sellersRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new SellerNotFoundException("상점을 찾을 수 없습니다."));
        boolean followed;
        // 팔로우 여부 확인
        Optional<SellerFollow> existing = sellerFollowRepository.findByUserAndSellers(user, sellers);
        if (existing.isPresent()) {
            sellerFollowRepository.delete(existing.get()); // 팔로우 취소
            sellers.setFollowCount(sellers.getFollowCount() - 1);
            sellersRepository.save(sellers);
            followed = false;
        } else {
            SellerFollow sellerFollow = SellerFollow.builder()
                    .user(user)
                    .sellers(sellers)
                    .createdAt(LocalDateTime.now())
                    .build();
            sellerFollowRepository.save(sellerFollow);
            sellers.setFollowCount(sellers.getFollowCount() + 1);
            sellersRepository.save(sellers);
            followed = true;
            // 알림 전송
            notificationService.sendSellerFollowNotification(sellers, user.getNickname());
        }
        return new SellerFollowDTO(followed);
    }
}
