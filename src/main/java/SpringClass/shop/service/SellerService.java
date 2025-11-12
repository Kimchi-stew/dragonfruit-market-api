package SpringClass.shop.service;


import SpringClass.shop.dto.SellerListDTO;
import SpringClass.shop.dto.SellerRequest;
import SpringClass.shop.dto.SellerResponse;
import SpringClass.shop.entity.Sellers;
import SpringClass.shop.entity.Users;
import SpringClass.shop.repository.SellersRepository;
import SpringClass.shop.security.AuthenticatedUserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SellerService {
    private final AuthenticatedUserUtils authenticatedUserUtils;
    private final SellersRepository sellersRepository;

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
                .createdAt(savedSeller.getCreatedAt())
                .build();
    }

    public List<SellerListDTO> getSellers() {
        List<Sellers> sellers;
        // 기본으로 최신순 정렬
        sellers = sellersRepository.findAllByOrderByCreatedAtDesc();

        return sellers.stream()
                .map(seller -> SellerListDTO.builder()
                        .id(seller.getId())
                        .storeName(seller.getStoreName())
                        .image(seller.getImage())
                        .build())
                .collect(Collectors.toList());
    }
}
