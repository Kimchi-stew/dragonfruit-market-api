package SpringClass.shop.service;

import SpringClass.shop.dto.Admin.CategoryRequest;
import SpringClass.shop.dto.Admin.CategoryResponse;
import SpringClass.shop.entity.Categories;
import SpringClass.shop.entity.Users;
import SpringClass.shop.enums.UserRole;
import SpringClass.shop.exceptions.*;
import SpringClass.shop.repository.CategoriesRepository;
import SpringClass.shop.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final SecurityUtils SecurityUtils;
    private final CategoriesRepository categoriesRepository;

    public CategoryResponse createCategory(CategoryRequest request) {
        Users user = SecurityUtils.getCurrentUser();
        if (user.getUserRole() != UserRole.ADMIN) {
            throw new AdminPermissionRequiredException("관리자만 카테고리를 추가할 수 있습니다.");
        }
        Optional<Categories> existingCategory = categoriesRepository.findByName(request.getName());
        if (existingCategory.isPresent()) {
            throw new CategoryNameAlreadyExistException("이미 존재하는 이름 입니다.");
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

    public CategoryResponse patchCategory(Long id, CategoryRequest request) {
        Users user = SecurityUtils.getCurrentUser();
        if (user.getUserRole() != UserRole.ADMIN) {
            throw new AdminPermissionRequiredException("관리자만 카테고리를 수정할 수 있습니다.");
        }
        Optional<Categories> existingCategory = categoriesRepository.findByName(request.getName());
        if (existingCategory.isPresent()) {
            throw new CategoryNameAlreadyExistException("이미 존재하는 이름 입니다.");
        }
        Categories categories = categoriesRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("존재하지 않는 카테고리 입니다."));
        categories.setName(request.getName());
        categoriesRepository.save(categories);

        return CategoryResponse.builder()
                .id(categories.getId())
                .name(categories.getName())
                .createdAt(categories.getCreatedAt())
                .build();
    }

    public void deleteCategory(Long id) {
        Users user = SecurityUtils.getCurrentUser();
        if (user.getUserRole() != UserRole.ADMIN) {
            throw new AdminPermissionRequiredException("관리자만 카테고리를 삭제할 수 있습니다.");
        }
        categoriesRepository.deleteById(id);
    }


}
