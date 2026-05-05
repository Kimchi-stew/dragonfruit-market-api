package SpringClass.shop.service;

import SpringClass.shop.dto.Cart.request.CartRequest;
import SpringClass.shop.dto.Cart.response.CartItemResponse;
import SpringClass.shop.dto.Cart.response.CartResponse;
import SpringClass.shop.dto.Products.response.ProductSummaryDTO;
import SpringClass.shop.dto.Sellers.response.SellerSummaryDTO;
import SpringClass.shop.entity.Cart.CartItems;
import SpringClass.shop.entity.Products.Products;
import SpringClass.shop.entity.Users.Users;
import SpringClass.shop.exceptions.cart.CartNotFoundException;
import SpringClass.shop.exceptions.product.ProductNotFoundException;
import SpringClass.shop.repository.Cart.CartItemsRepository;
import SpringClass.shop.repository.Products.ProductsRepository;
import SpringClass.shop.security.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {
    private final SecurityUtils SecurityUtils;
    private final ProductsRepository productsRepository;
    private final CartItemsRepository cartItemsRepository;

    public ProductSummaryDTO plusProduct(CartRequest request) {
        Users user = SecurityUtils.getCurrentUser();
        Products products = productsRepository.findByIdAndDeletedAtIsNull(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("해당 상품을 찾을 수 없습니다."));

        CartItems cartItem = cartItemsRepository.findByUserAndProduct(user, products)
                .orElse(null);

        if (cartItem != null) {
            // 이미 장바구니에 존재하는 상품이면 수량 증가
            cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
            cartItem.setUpdatedAt(LocalDateTime.now());
            cartItemsRepository.save(cartItem);
        } else {
            // 없으면 새로 생성
            cartItem = CartItems.builder()
                    .user(user)
                    .product(products)
                    .quantity(request.getQuantity())
                    .createdAt(LocalDateTime.now())
                    .build();
            cartItemsRepository.save(cartItem);
        }
        return ProductSummaryDTO.builder()
                .id(products.getId())
                .seller(SellerSummaryDTO.from(products.getSeller()))
                .name(products.getName())
                .price(products.getPrice())
                .build();
    }

    public CartResponse getCart() {
        Users user = SecurityUtils.getCurrentUser();
        List<CartItems> cartItems = cartItemsRepository.findByUser(user);
        if(cartItems.isEmpty()) {
            throw new CartNotFoundException("장바구니에 상품이 없습니다.");
        }
        List<CartItemResponse> itemResponses = cartItems.stream()
                .map(cartItem -> CartItemResponse.builder()
                        .id(cartItem.getId())
                        .productId(cartItem.getProduct().getId())
                        .productName(cartItem.getProduct().getName())
                        .quantity(cartItem.getQuantity())
                        .pricePerItem(cartItem.getProduct().getPrice())
                        .totalPrice(cartItem.getProduct().getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())))
                        .build())
                .collect(Collectors.toList());

        int totalQuantity = itemResponses.stream().mapToInt(CartItemResponse::getQuantity).sum();
        BigDecimal totalPrice = itemResponses.stream()
                .map(CartItemResponse::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .items(itemResponses)
                .totalQuantity(totalQuantity)
                .totalPrice(totalPrice)
                .build();
    }

    @Transactional
    public void deleteProduct(CartRequest request) {
        Users user = SecurityUtils.getCurrentUser();

        Products products = productsRepository.findByIdAndDeletedAtIsNull(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("해당 상품을 찾을 수 없습니다."));
        // 장바구니에 들어있는 지 체크
        CartItems cartItems = cartItemsRepository.findByUserAndProduct(user, products)
                .orElseThrow(() -> new CartNotFoundException("장바구니에 해당 상품이 존재하지 않습니다."));
        // 수량만 삭제
        if (cartItems.getQuantity()-request.getQuantity()>=1) {
            cartItems.setQuantity(cartItems.getQuantity()-request.getQuantity());
            cartItemsRepository.save(cartItems);
        } else {
            // 장바구니에서 삭제
            cartItemsRepository.delete(cartItems);
        }
    }
}
