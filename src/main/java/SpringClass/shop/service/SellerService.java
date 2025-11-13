package SpringClass.shop.service;


import SpringClass.shop.dto.*;
import SpringClass.shop.entity.Products.Products;
import SpringClass.shop.entity.Sellers.SellerFollow;
import SpringClass.shop.entity.Sellers.SellerLikes;
import SpringClass.shop.entity.Sellers.Sellers;
import SpringClass.shop.entity.Users;
import SpringClass.shop.exceptions.ForbiddenException;
import SpringClass.shop.exceptions.SellerNotFoundException;
import SpringClass.shop.repository.ProductsRepository;
import SpringClass.shop.repository.SellerFollowRepository;
import SpringClass.shop.repository.SellerLikesRepository;
import SpringClass.shop.repository.SellersRepository;
import SpringClass.shop.security.AuthenticatedUserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SellerService {
    private final AuthenticatedUserUtils authenticatedUserUtils;
    private final SellersRepository sellersRepository;
    private final ProductsRepository productsRepository;
    private final SellerLikesRepository sellerLikesRepository;
    private final SellerFollowRepository sellerFollowRepository;

    public SellerResponse createSeller(SellerRequest request) {
        // user 정보 가져오기 (baarer token에서 추출)
        Users user = authenticatedUserUtils.getCurrentUser();

        Sellers sellers = Sellers.builder()
                .user(user)
                .storeName(request.getStoreName())
                .description(request.getDescription())
                .image(request.getImage())
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
                .followed(false)
                .createdAt(savedSeller.getCreatedAt())
                .build();
    }

    public List<SellerListDTO> getSellers() {
        List<Sellers> sellers;
        // 기본으로 최신순 정렬
        sellers = sellersRepository.findAllByDeletedAtIsNullOrderByCreatedAtDesc();

        return sellers.stream()
                .map(seller -> SellerListDTO.builder()
                        .id(seller.getId())
                        .storeName(seller.getStoreName())
                        .image(seller.getImage())
                        .likeCount(seller.getLikeCount())
                        .build())
                .collect(Collectors.toList());
    }

    public SellerResponse patchSeller(Long id, SellerRequest request) {
        // user 정보 가져오기 (baarer token에서 추출)
        Users user = authenticatedUserUtils.getCurrentUser();

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
                .followed(followed)
                .image(savedSeller.getImage())
                .createdAt(savedSeller.getCreatedAt())
                .build();
    }

    public SellerResponse getSeller(Long id) {
        Users user = authenticatedUserUtils.getCurrentUser();
        Sellers sellers = sellersRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new SellerNotFoundException("상점을 찾을 수 없습니다."));

        boolean followed = sellerFollowRepository.existsByUserAndSellers(user, sellers);

        return SellerResponse.builder()
                        .id(sellers.getId())
                        .userId(sellers.getUser().getId())
                        .storeName(sellers.getStoreName())
                        .image(sellers.getImage())
                        .likeCount(sellers.getLikeCount())
                        .createdAt(sellers.getCreatedAt())
                        .followed(followed)
                        .build();
    }

    public SellerDeleteDTO deleteSeller(Long id) {
        Users user = authenticatedUserUtils.getCurrentUser();

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
        Users user = authenticatedUserUtils.getCurrentUser();

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
        Users user = authenticatedUserUtils.getCurrentUser();

        Sellers sellers = sellersRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new SellerNotFoundException("상점을 찾을 수 없습니다."));
        boolean followed;
        // 팔로우 여부 확인
        Optional<SellerFollow> existing = sellerFollowRepository.findByUserAndSellers(user, sellers);
        if (existing.isPresent()) {
            sellerFollowRepository.delete(existing.get()); // 팔로우 취소
            followed = false;
        } else {
            SellerFollow sellerFollow = SellerFollow.builder()
                    .user(user)
                    .sellers(sellers)
                    .createdAt(LocalDateTime.now())
                    .build();
            sellerFollowRepository.save(sellerFollow);
            followed = true;
        }
        return new SellerFollowDTO(followed);
    }
}
