package SpringClass.shop.repository.Products;

import SpringClass.shop.entity.Products.Products;
import SpringClass.shop.entity.Products.QProductCategories;
import SpringClass.shop.entity.Products.QProductLikes;
import SpringClass.shop.entity.Products.QProducts;
import SpringClass.shop.entity.Users.QUsers;
import SpringClass.shop.enums.GenderRole;
import SpringClass.shop.enums.PriceSortType;
import SpringClass.shop.enums.ProductCategoryType;
import SpringClass.shop.enums.SortType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
@RequiredArgsConstructor
public class ProductsRepositoryImpl implements ProductsRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QProducts products = QProducts.products;
    private final QProductCategories productCategories = QProductCategories.productCategories;
    private final QProductLikes productLikes = QProductLikes.productLikes;
    private final QUsers user = QUsers.users;


    @Override
    public Page<Products> findProductsWithDynamicConditions(
            PriceSortType priceSortType,
            ProductCategoryType productCategoryType,
            GenderRole genderRole,
            SortType sortType,
            Pageable pageable
    ) {
        // 옵션
        BooleanBuilder builder = getWhereClause(productCategoryType);

        // 정렬
        List<OrderSpecifier<?>> orderSpecifiers = getOrderSpecifiers(priceSortType, sortType, genderRole);


        List<Products> content = queryFactory
                .selectFrom(products)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(orderSpecifiers.toArray(new OrderSpecifier[0]))
                .fetch();


        Long totalCount = queryFactory
                .select(products.count())
                .from(products)
                .where(builder)
                .fetchOne();


        return PageableExecutionUtils.getPage(content, pageable, () -> Objects.requireNonNullElse(totalCount, 0L));
    }

    private BooleanBuilder getWhereClause(ProductCategoryType categoryType) {
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(products.deletedAt.isNull());


        if (categoryType != null) {
            builder.and(
                    JPAExpressions
                            .selectOne()
                            .from(productCategories)
                            .where(
                                    productCategories.product.eq(products),
                                    productCategories.category.name.eq(categoryType.name())
                            )
                            .exists()
            );
        }

        return builder;
    }

    // 정렬
    private List<OrderSpecifier<?>> getOrderSpecifiers(PriceSortType priceSortType, SortType sortType, GenderRole genderRole) {

        Stream<OrderSpecifier<?>> sortStream = Stream.empty();

        // 성별 정렬(성별 좋아요순)
        if (genderRole != null) {

            JPQLQuery<Long> subquery = JPAExpressions
                    .select(productLikes.count())
                    .from(productLikes)
                    .join(productLikes.user, user)
                    .where(
                            productLikes.product.eq(products),
                            user.gender.eq(genderRole)
                    );

            NumberExpression<Long> genderLikeCount =
                    Expressions.numberTemplate(Long.class, "({0})", subquery);

            sortStream = Stream.concat(sortStream, Stream.of(genderLikeCount.desc()));
        }

        // 가격
        if (sortType == SortType.POPULAR) {
            sortStream = Stream.concat(sortStream, Stream.of(products.likeCount.desc()));
        } else if (sortType == SortType.OLDEST) {
            sortStream = Stream.concat(sortStream, Stream.of(products.createdAt.asc()));
        }


        if (priceSortType == PriceSortType.ASC) {
            sortStream = Stream.concat(sortStream, Stream.of(products.price.asc()));
        } else if (priceSortType == PriceSortType.DESC) {
            sortStream = Stream.concat(sortStream, Stream.of(products.price.desc()));
        }

        // 기본 최신순
        sortStream = Stream.concat(sortStream, Stream.of(products.createdAt.desc()));

        return sortStream.collect(Collectors.toList());
    }
}
