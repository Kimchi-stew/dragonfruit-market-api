package SpringClass.shop.repository.Products;

import SpringClass.shop.entity.Products.Products;
import SpringClass.shop.enums.GenderRole;
import SpringClass.shop.enums.PriceSortType;
import SpringClass.shop.enums.ProductCategoryType;
import SpringClass.shop.enums.SortType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductsRepositoryCustom {
    Page<Products> findProductsWithDynamicConditions(
            PriceSortType priceSortType,
            ProductCategoryType productCategoryType,
            GenderRole genderRole,
            SortType sortType,
            Pageable pageable
    );
}
