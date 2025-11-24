package SpringClass.shop.service;

import SpringClass.shop.dto.Admin.CategoryRequest;
import SpringClass.shop.dto.Admin.CategoryResponse;
import SpringClass.shop.entity.Categories;
import SpringClass.shop.entity.Products.ProductCategories;
import SpringClass.shop.entity.Users;
import SpringClass.shop.enums.UserRole;
import SpringClass.shop.exceptions.AdminPermissionRequiredException;
import SpringClass.shop.repository.CategoriesRepository;
import SpringClass.shop.security.AuthenticatedUserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final AuthenticatedUserUtils authenticatedUserUtils;
    private final CategoriesRepository categoriesRepository;

    public CategoryResponse createCategory(CategoryRequest request) {
        Users user = authenticatedUserUtils.getCurrentUser();
        if (user.getUserRole() != UserRole.ADMIN) {
            throw new AdminPermissionRequiredException("관리자만 카테고리를 추가할 수 있습니다.");
        }
        Categories categories = Categories.builder()
                .name(request.getName())
                .build();
        categoriesRepository.save(categories);
        return CategoryResponse.builder()
                .id(categories.getId())
                .name(categories.getName())
                .createdAt(categories.getCreatedAt())
                .build();
    }
}
