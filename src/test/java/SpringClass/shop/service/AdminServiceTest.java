package SpringClass.shop.service;

import SpringClass.shop.dto.Admin.request.CategoryRequest;
import SpringClass.shop.dto.Admin.response.CategoryResponse;
import SpringClass.shop.entity.Categories.Categories;
import SpringClass.shop.entity.Users.Users;
import SpringClass.shop.enums.GenderRole;
import SpringClass.shop.enums.UserRole;
import SpringClass.shop.exceptions.category.CategoryNameAlreadyExistException;
import SpringClass.shop.exceptions.category.CategoryNotFoundException;
import SpringClass.shop.exceptions.common.AdminPermissionRequiredException;
import SpringClass.shop.repository.Categories.CategoriesRepository;
import SpringClass.shop.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock private SecurityUtils SecurityUtils;
    @Mock private CategoriesRepository categoriesRepository;

    @InjectMocks private AdminService adminService;

    private Users adminUser;
    private Users normalUser;
    private Categories category;

    @BeforeEach
    void setUp() {
        adminUser = Users.builder()
                .id(1L).email("admin@t.com").nickname("관리자")
                .gender(GenderRole.M).userRole(UserRole.ADMIN).build();

        normalUser = Users.builder()
                .id(2L).email("user@t.com").nickname("유저")
                .gender(GenderRole.M).userRole(UserRole.USER).build();

        category = Categories.builder().id(1L).name("의류").build();
    }

    @Test
    @DisplayName("관리자가 카테고리 생성 성공")
    void createCategory_adminUser_success() {
        CategoryRequest request = new CategoryRequest("신규카테고리");
        when(SecurityUtils.getCurrentUser()).thenReturn(adminUser);
        when(categoriesRepository.findByName("신규카테고리")).thenReturn(Optional.empty());
        when(categoriesRepository.save(any())).thenReturn(Categories.builder().id(2L).name("신규카테고리").build());

        CategoryResponse response = adminService.createCategory(request);

        assertThat(response.getName()).isEqualTo("신규카테고리");
        verify(categoriesRepository).save(any(Categories.class));
    }

    @Test
    @DisplayName("일반 유저가 카테고리 생성 시 AdminPermissionRequiredException 발생")
    void createCategory_notAdmin_throwsException() {
        CategoryRequest request = new CategoryRequest("신규카테고리");
        when(SecurityUtils.getCurrentUser()).thenReturn(normalUser);

        assertThatThrownBy(() -> adminService.createCategory(request))
                .isInstanceOf(AdminPermissionRequiredException.class);
    }

    @Test
    @DisplayName("이미 존재하는 카테고리 이름으로 생성 시 CategoryNameAlreadyExistException 발생")
    void createCategory_nameExists_throwsException() {
        CategoryRequest request = new CategoryRequest("의류");
        when(SecurityUtils.getCurrentUser()).thenReturn(adminUser);
        when(categoriesRepository.findByName("의류")).thenReturn(Optional.of(category));

        assertThatThrownBy(() -> adminService.createCategory(request))
                .isInstanceOf(CategoryNameAlreadyExistException.class);
    }

    @Test
    @DisplayName("관리자가 카테고리 수정 성공")
    void patchCategory_adminUser_success() {
        CategoryRequest request = new CategoryRequest("수정됨");
        when(SecurityUtils.getCurrentUser()).thenReturn(adminUser);
        when(categoriesRepository.findByName("수정됨")).thenReturn(Optional.empty());
        when(categoriesRepository.findById(1L)).thenReturn(Optional.of(category));

        CategoryResponse response = adminService.patchCategory(1L, request);

        assertThat(response.getName()).isEqualTo("수정됨");
        verify(categoriesRepository).save(category);
    }

    @Test
    @DisplayName("일반 유저가 카테고리 수정 시 AdminPermissionRequiredException 발생")
    void patchCategory_notAdmin_throwsException() {
        CategoryRequest request = new CategoryRequest("수정됨");
        when(SecurityUtils.getCurrentUser()).thenReturn(normalUser);

        assertThatThrownBy(() -> adminService.patchCategory(1L, request))
                .isInstanceOf(AdminPermissionRequiredException.class);
    }

    @Test
    @DisplayName("수정할 카테고리 이름이 이미 존재하면 CategoryNameAlreadyExistException 발생")
    void patchCategory_nameExists_throwsException() {
        CategoryRequest request = new CategoryRequest("의류");
        when(SecurityUtils.getCurrentUser()).thenReturn(adminUser);
        when(categoriesRepository.findByName("의류")).thenReturn(Optional.of(category));

        assertThatThrownBy(() -> adminService.patchCategory(1L, request))
                .isInstanceOf(CategoryNameAlreadyExistException.class);
    }

    @Test
    @DisplayName("존재하지 않는 카테고리 수정 시 CategoryNotFoundException 발생")
    void patchCategory_notFound_throwsException() {
        CategoryRequest request = new CategoryRequest("수정됨");
        when(SecurityUtils.getCurrentUser()).thenReturn(adminUser);
        when(categoriesRepository.findByName("수정됨")).thenReturn(Optional.empty());
        when(categoriesRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.patchCategory(999L, request))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    @DisplayName("관리자가 카테고리 삭제 성공")
    void deleteCategory_adminUser_success() {
        when(SecurityUtils.getCurrentUser()).thenReturn(adminUser);

        adminService.deleteCategory(1L);

        verify(categoriesRepository).deleteById(1L);
    }

    @Test
    @DisplayName("일반 유저가 카테고리 삭제 시 AdminPermissionRequiredException 발생")
    void deleteCategory_notAdmin_throwsException() {
        when(SecurityUtils.getCurrentUser()).thenReturn(normalUser);

        assertThatThrownBy(() -> adminService.deleteCategory(1L))
                .isInstanceOf(AdminPermissionRequiredException.class);
    }
}
