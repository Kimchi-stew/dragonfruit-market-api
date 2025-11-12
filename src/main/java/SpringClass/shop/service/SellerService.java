package SpringClass.shop.service;


import SpringClass.shop.dto.SellerListDTO;
import SpringClass.shop.dto.CreateSellerDTO;
import SpringClass.shop.dto.SellerRequest;
import SpringClass.shop.dto.SellerResponse;
import SpringClass.shop.entity.Sellers;
import SpringClass.shop.entity.Users;
import SpringClass.shop.exceptions.ForbiddenException;
import SpringClass.shop.exceptions.SellerNotFoundException;
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

    public SellerResponse createSeller(CreateSellerDTO request) {
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

    public SellerResponse patchSeller(SellerRequest request) {
        // user 정보 가져오기 (baarer token에서 추출)
        Users user = authenticatedUserUtils.getCurrentUser();

        Sellers sellers = sellersRepository.findById(request.getId())
                .orElseThrow(() -> new SellerNotFoundException("상점을 찾을 수 없습니다."));

        // 소유자 확인
        if (!sellers.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("수정할 수 있는 권한이 없습니다.");
        }

        sellers.setStoreName(request.getStoreName());
        sellers.setDescription(request.getDescription());
        sellers.setImage(request.getImage());

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
}
