package SpringClass.shop.repository;

import SpringClass.shop.entity.Products.ProductCategories;
import SpringClass.shop.entity.Products.Products;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductCategoriesRepository extends JpaRepository<ProductCategories, Long> {
    Optional<ProductCategories> findByProduct(Products products);
    void deleteByProduct(Products products);
}
