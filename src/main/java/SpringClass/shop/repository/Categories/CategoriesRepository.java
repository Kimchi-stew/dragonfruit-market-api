package SpringClass.shop.repository.Categories;

import SpringClass.shop.entity.Categories.Categories;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoriesRepository extends JpaRepository<Categories, Long> {
    Optional<Categories> findByName(String name);
    void deleteByName(String name);
}
