package SpringClass.shop.service;
import SpringClass.shop.dto.Admin.response.CategoryResponse;
import SpringClass.shop.dto.Products.request.ProductRequest;
import SpringClass.shop.dto.Products.response.ProductDeleteDTO;
import SpringClass.shop.dto.Products.response.ProductListDTO;
import SpringClass.shop.dto.Products.response.ProductResponse;
import SpringClass.shop.dto.Products.response.WishResponseDTO;
import SpringClass.shop.dto.Sellers.response.SellerSummaryDTO;
import SpringClass.shop.dto.common.response.LikesResponseDTO;
import SpringClass.shop.entity.Categories.Categories;
import SpringClass.shop.entity.Products.*;
import SpringClass.shop.entity.Sellers.Sellers;
import SpringClass.shop.entity.Users.Users;
import SpringClass.shop.enums.GenderRole;
import SpringClass.shop.enums.PriceSortType;
import SpringClass.shop.enums.ProductCategoryType;
import SpringClass.shop.enums.SortType;
import SpringClass.shop.exceptions.CategoryNotFoundException;
import SpringClass.shop.exceptions.ForbiddenException;
import SpringClass.shop.exceptions.ProductNotFoundException;
import SpringClass.shop.exceptions.SellerNotFoundException;
import SpringClass.shop.repository.Categories.CategoriesRepository;
import SpringClass.shop.repository.Products.ProductCategoriesRepository;
import SpringClass.shop.repository.Products.ProductLikeRepository;
import SpringClass.shop.repository.Products.ProductsRepository;
import SpringClass.shop.repository.Products.ProductWishRepository;
import SpringClass.shop.repository.Reviews.ReviewRepository;
import SpringClass.shop.repository.Sellers.SellersRepository;
import SpringClass.shop.security.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ProductService {
    private final SecurityUtils SecurityUtils;
    private final SellersRepository sellersRepository;
    private final ProductsRepository productsRepository;
    private final ProductLikeRepository productLikeRepository;
    private final ProductWishRepository productWishRepository;
    private final CategoriesRepository categoriesRepository;
    private final ProductCategoriesRepository productCategoriesRepository;
    private final ReviewRepository reviewRepository;

    public ProductResponse createProduct(ProductRequest request) {
        // user 정보 가져오기 (bearer token에서 추출)
        Users user = SecurityUtils.getCurrentUser();

        // 판매자(상점) 등록을 안 하면 오류
        Sellers seller = sellersRepository.findByUser(user)
                .orElseThrow(() -> new SellerNotFoundException("판매자를 찾을 수 없습니다."));

        // 존재하지 않는 카테고리면 오류
        Categories categories = categoriesRepository.findByName(request.getCategory())
                .orElseThrow(() -> new CategoryNotFoundException("존재하지 않는 카테고리 입니다."));

        Products products = Products.builder()
                .name(request.getName())
                .seller(seller)
                .price(request.getPrice())
                .description(request.getDescription())
                .likeCount(0) // 기본값
                .stock(request.getStock())
                .createdAt(LocalDateTime.now())
                .build();

        Products savedProduct = productsRepository.save(products);

        // 카테고리 저장
        ProductCategories productCategories = ProductCategories.builder()
                .product(savedProduct)
                .category(categories)
                .build();
        productCategoriesRepository.save(productCategories);

        // 이미지 변환 및 저장
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            List<ProductImages> imageEntities = request.getImages().stream()
                    .map(url -> ProductImages.builder()
                            .product(savedProduct) // 관계 연결
                            .imageUrl(url)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build())
                    .collect(Collectors.toList());

            savedProduct.setImages(imageEntities);
        }

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
                .category(request.getCategory())
                .stock(savedProduct.getStock())
                .images(savedProduct.getImages().stream()
                        .map(ProductImages::getImageUrl)
                        .collect(Collectors.toList()))
                .createdAt(savedProduct.getCreatedAt())
                .updatedAt(savedProduct.getUpdatedAt())
                .rating(null) // 리뷰가 없으므로 평균 평점에 null
                .build();
    }


    public Page<ProductListDTO> getProducts(
            PriceSortType priceSortType,
            ProductCategoryType productCategoryType,
            GenderRole genderRole,
            SortType sortType,
            Pageable pageable) {

        Page<Products> products = productsRepository.findProductsWithDynamicConditions(
                priceSortType,
                productCategoryType,
                genderRole,
                sortType,
                pageable
        );

        return products.map(ProductListDTO::from);
    }

    public ProductResponse patchProduct(Long id, ProductRequest request) {
        // user 정보 가져오기 (bearer token에서 추출)
        Users user = SecurityUtils.getCurrentUser();



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
        // 카테고리 가져오기
        ProductCategories productCategories = productCategoriesRepository.findByProduct(product)
                .orElseThrow(() -> new CategoryNotFoundException("카테고리가 존재하지 않습니다."));
        Categories categories = categoriesRepository.findById(productCategories.getProduct().getId())
                .orElseThrow(() -> new CategoryNotFoundException("카테고리가 존재하지 않습니다."));
        categories.setName(request.getName());
        categoriesRepository.save(categories);

        // 평균 평점 계산
        Double avgRating = reviewRepository.findAverageRating(savedProduct.getId());
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
                .category(productCategories.getProduct().getName())
                .images(savedProduct.getImages().stream()
                        .map(ProductImages::getImageUrl)
                        .collect(Collectors.toList()))
                .likeCount(savedProduct.getLikeCount())
                .createdAt(savedProduct.getCreatedAt())
                .updatedAt(savedProduct.getUpdatedAt())
                .rating(avgRating)
                .build();
    }

    public ProductResponse getProduct(Long id){
        // user 정보 가져오기 (baarer token에서 추출)
        Users user = SecurityUtils.getCurrentUser();

        // 상품 조회
        Products product = productsRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ProductNotFoundException("상품을 찾을 수 없습니다."));
        boolean wished = productWishRepository.existsByUserAndProduct(user, product);

        // 카테고리 가져오기
        ProductCategories productCategories = productCategoriesRepository.findByProduct(product)
                .orElseThrow(() -> new CategoryNotFoundException("카테고리가 존재하지 않습니다."));

        // 평균 평점 계산
        Double avgRating = reviewRepository.findAverageRating(product.getId());
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
                .category(productCategories.getCategory().getName())
                .rating(avgRating)
                .build();
    }

    @Transactional
    public ProductDeleteDTO deleteProduct(Long id) {
        Users user = SecurityUtils.getCurrentUser();

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
        // 카테고리 삭제
        productCategoriesRepository.deleteByProduct(savedProduct);

        return ProductDeleteDTO.builder()
                .name(savedProduct.getName())
                .image(firstImage)
                .deletedAt(savedProduct.getDeletedAt())
                .build();
    }

    @Transactional
    public LikesResponseDTO likeProduct(Long id) {
        // user 정보 가져오기 (bearer token에서 추출)
        Users user = SecurityUtils.getCurrentUser();

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
        Users user = SecurityUtils.getCurrentUser();

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
