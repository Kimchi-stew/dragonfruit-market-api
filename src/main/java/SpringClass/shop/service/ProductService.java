package SpringClass.shop.service;
import SpringClass.shop.dto.*;
import SpringClass.shop.dto.Admin.CategoryResponse;
import SpringClass.shop.dto.Products.ProductDeleteDTO;
import SpringClass.shop.dto.Products.ProductListDTO;
import SpringClass.shop.dto.Products.ProductRequest;
import SpringClass.shop.dto.Products.ProductResponse;
import SpringClass.shop.dto.Sellers.SellerSummaryDTO;
import SpringClass.shop.entity.Categories;
import SpringClass.shop.entity.Products.ProductImages;
import SpringClass.shop.entity.Products.ProductLikes;
import SpringClass.shop.entity.Products.ProductWish;
import SpringClass.shop.entity.Products.Products;
import SpringClass.shop.entity.Sellers.Sellers;
import SpringClass.shop.entity.Users;
import SpringClass.shop.exceptions.ForbiddenException;
import SpringClass.shop.exceptions.ProductNotFoundException;
import SpringClass.shop.exceptions.SellerNotFoundException;
import SpringClass.shop.repository.*;
import SpringClass.shop.security.AuthenticatedUserUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final AuthenticatedUserUtils authenticatedUserUtils;
    private final SellersRepository sellersRepository;
    private final ProductsRepository productsRepository;
    private final ProductLikeRepository productLikeRepository;
    private final ProductWishRepository productWishRepository;
    private final CategoriesRepository categoriesRepository;

    public ProductResponse createProduct(ProductRequest request) {
        // user 정보 가져오기 (bearer token에서 추출)
        Users user = authenticatedUserUtils.getCurrentUser();

        // 판매자(상점) 등록을 안 하면 오류
        Sellers seller = sellersRepository.findByUser(user)
                .orElseThrow(() -> new SellerNotFoundException("판매자를 찾을 수 없습니다."));

        Products products = Products.builder()
                .name(request.getName())
                .seller(seller)
                .price(request.getPrice())
                .description(request.getDescription())
                .likeCount(0) // 기본값
                .stock(request.getStock())
                .createdAt(LocalDateTime.now())
                .build();

        // 이미지 변환 및 저장
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            List<ProductImages> imageEntities = request.getImages().stream()
                    .map(url -> ProductImages.builder()
                            .product(products) // 관계 연결
                            .imageUrl(url)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build())
                    .collect(Collectors.toList());

            products.setImages(imageEntities);
        }

        Products savedProduct = productsRepository.save(products);

        return ProductResponse.builder()
                .id(savedProduct.getId())
                .seller(SellerSummaryDTO.builder()
                        .id(savedProduct.getSeller().getId())
                        .storeName(savedProduct.getSeller().getStoreName())
                        .image(savedProduct.getSeller().getImage())
                        .build())
                .name(savedProduct.getName())
                .price(savedProduct.getPrice())
                .description(savedProduct.getDescription())
                .likeCount(savedProduct.getLikeCount())
                .wished(false) // 신규 상품이므로
                .stock(savedProduct.getStock())
                .images(savedProduct.getImages().stream()
                        .map(ProductImages::getImageUrl)
                        .collect(Collectors.toList()))
                .createdAt(savedProduct.getCreatedAt())
                .updatedAt(savedProduct.getUpdatedAt())
                .build();
    }

    public List<ProductListDTO> getProducts(String category) {
        List<Products> products;

        if ("asc".equalsIgnoreCase(category)) { // 가격 낮은순 + 최신순
            products = productsRepository.findAllByDeletedAtIsNullOrderByPriceAscCreatedAtDesc();
        } else if ("desc".equalsIgnoreCase(category)) { // 가격 높은순 + 최신순
            products = productsRepository.findAllByDeletedAtIsNullOrderByPriceDescCreatedAtDesc();

        } else if ("like".equalsIgnoreCase(category)) { // 좋아요 많은 순 + 최신순
            products = productsRepository.findAllByDeletedAtIsNullOrderByLikeCountDescCreatedAtDesc();
        }
        else if ("old".equalsIgnoreCase(category)) { // 오래된 순
            products = productsRepository.findAllByDeletedAtIsNullOrderByCreatedAtAsc();
        }
        else { // 최신순
            products = productsRepository.findAllByDeletedAtIsNullOrderByCreatedAtDesc();
        }

        return products.stream().map(product -> {
            String mainImage = null;
            if (product.getImages() != null && !product.getImages().isEmpty()) {
                mainImage = product.getImages().get(0).getImageUrl(); // 첫 번째 이미지
            }

            return ProductListDTO.builder()
                    .id(product.getId())
                    .seller(SellerSummaryDTO.builder()
                            .id(product.getSeller().getId())
                            .storeName(product.getSeller().getStoreName())
                            .image(product.getSeller().getImage())
                            .build())
                    .name(product.getName())
                    .price(product.getPrice())
                    .likeCount(product.getLikeCount())
                    .image(mainImage)
                    .build();
        }).collect(Collectors.toList());
    }

    public ProductResponse patchProduct(Long id, ProductRequest request) {
        // user 정보 가져오기 (bearer token에서 추출)
        Users user = authenticatedUserUtils.getCurrentUser();

        Products product = productsRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ProductNotFoundException("상품을 찾을 수 없습니다."));

        // 소유자 확인
        if (!product.getSeller().getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("수정할 수 있는 권한이 없습니다.");
        }
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setDescription(request.getDescription());
        product.setStock(request.getStock());

        // 이미지 변환 및 저장
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            List<ProductImages> imageEntities = request.getImages().stream()
                    .map(url -> ProductImages.builder()
                            .product(product) // 관계 연결
                            .imageUrl(url)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build())
                    .collect(Collectors.toList());

            product.setImages(imageEntities);
        }

        Products savedProduct = productsRepository.save(product);
        // 찜 여부 판별
        boolean wished = productWishRepository.existsByUserAndProduct(user, product);
        return ProductResponse.builder()
                .id(savedProduct.getId())
                .seller(SellerSummaryDTO.builder()
                        .id(savedProduct.getSeller().getId())
                        .storeName(savedProduct.getSeller().getStoreName())
                        .image(savedProduct.getSeller().getImage())
                        .build())
                .name(savedProduct.getName())
                .price(savedProduct.getPrice())
                .description(savedProduct.getDescription())
                .stock(savedProduct.getStock())
                .wished(wished)
                .images(savedProduct.getImages().stream()
                        .map(ProductImages::getImageUrl)
                        .collect(Collectors.toList()))
                .likeCount(savedProduct.getLikeCount())
                .createdAt(savedProduct.getCreatedAt())
                .updatedAt(savedProduct.getUpdatedAt())
                .build();
    }

    public ProductResponse getProduct(Long id){
        // user 정보 가져오기 (baarer token에서 추출)
        Users user = authenticatedUserUtils.getCurrentUser();

        // 상품 조회
        Products product = productsRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ProductNotFoundException("상품을 찾을 수 없습니다."));
        boolean wished = productWishRepository.existsByUserAndProduct(user, product);
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .seller(SellerSummaryDTO.builder()
                        .id(product.getSeller().getId())
                        .storeName(product.getSeller().getStoreName())
                        .build())
                .price(product.getPrice())
                .likeCount(product.getLikeCount())
                .wished(wished)
                .build();
    }

    @Transactional
    public ProductDeleteDTO deleteProduct(Long id) {
        Users user = authenticatedUserUtils.getCurrentUser();

        Products product = productsRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ProductNotFoundException("상품을 찾을 수 없습니다."));

        // 소유자 확인
        if (!product.getSeller().getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("삭제할 수 있는 권한이 없습니다.");
        }
        product.setDeletedAt(LocalDateTime.now());
        Products savedProduct = productsRepository.save(product);

        String firstImage = savedProduct.getImages() != null && !savedProduct.getImages().isEmpty()
                ? savedProduct.getImages().get(0).getImageUrl() // 첫 번째 이미지 URL 사용
                : null;

        return ProductDeleteDTO.builder()
                .name(savedProduct.getName())
                .image(firstImage)
                .deletedAt(savedProduct.getDeletedAt())
                .build();
    }

    @Transactional
    public LikesResponseDTO likeProduct(Long id) {
        // user 정보 가져오기 (bearer token에서 추출)
        Users user = authenticatedUserUtils.getCurrentUser();

        Products product = productsRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ProductNotFoundException("상품을 찾을 수 없습니다."));
        // 좋아요 여부 확인
        Optional<ProductLikes> existing = productLikeRepository.findByUserAndProduct(user, product);
        boolean liked;
        if (existing.isPresent()) {
            productLikeRepository.delete(existing.get()); // 좋아요 취소

            product.setLikeCount(product.getLikeCount() - 1);
            productsRepository.save(product);
            liked = false;
        } else {
            ProductLikes productLikes = ProductLikes.builder()
                    .user(user)
                    .product(product)
                    .createdAt(LocalDateTime.now())
                    .build();
            productLikeRepository.save(productLikes);

            product.setLikeCount(product.getLikeCount() + 1);
            productsRepository.save(product);
            liked = true;
        }
        return new LikesResponseDTO(liked, product.getLikeCount());
    }

    public WishResponseDTO wishProduct(Long id) {
        // user 정보 가져오기 (baarer token에서 추출)
        Users user = authenticatedUserUtils.getCurrentUser();

        Products product = productsRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ProductNotFoundException("상품을 찾을 수 없습니다."));

        Optional<ProductWish> existing = productWishRepository.findByUserAndProduct(user, product);
        boolean wished;
        // 찜 여부 확인
        if (existing.isPresent()) {
            productWishRepository.delete(existing.get()); // 찜 취소
            wished = false;
        } else {
            ProductWish productWish = ProductWish.builder()
                    .user(user)
                    .product(product)
                    .createdAt(LocalDateTime.now())
                    .build();
            productWishRepository.save(productWish);
            wished = true;
        }
        return new WishResponseDTO(wished);
    }

    public List<ProductListDTO> searchProduct(String keyword) {
        // 검색어 없으면 null 처리
        if (keyword == null || keyword.isBlank()) {
            return null; // null 반환
        }
        List<Products> products = productsRepository.findByDeletedAtIsNullAndNameContainingIgnoreCaseOrderByLikeCountDescCreatedAtDesc(keyword);

        return products.stream().map(product -> {
            String mainImage = null;
            if (product.getImages() != null && !product.getImages().isEmpty()) {
                mainImage = product.getImages().get(0).getImageUrl(); // 첫 번째 이미지
            }

            return ProductListDTO.builder()
                    .id(product.getId())
                    .seller(SellerSummaryDTO.builder()
                            .id(product.getSeller().getId())
                            .storeName(product.getSeller().getStoreName())
                            .image(product.getSeller().getImage())
                            .build())
                    .name(product.getName())
                    .price(product.getPrice())
                    .likeCount(product.getLikeCount())
                    .image(mainImage)
                    .build();
        }).collect(Collectors.toList());
    }

    public List<CategoryResponse> getCategories() {
        List<Categories> categories = categoriesRepository.findAll();

        List<CategoryResponse> response = categories.stream()
                .map(category -> CategoryResponse.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .createdAt(category.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return response;
    }

}
