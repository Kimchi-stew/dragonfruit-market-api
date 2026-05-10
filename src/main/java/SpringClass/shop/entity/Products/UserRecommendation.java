package SpringClass.shop.entity.Products;

import SpringClass.shop.entity.Users.Users;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_recommendations")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRecommendation {

    @EmbeddedId
    private UserRecommendationId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productId")
    @JoinColumn(name = "product_id")
    private Products product;

    @Column(nullable = false)
    private Double score;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
