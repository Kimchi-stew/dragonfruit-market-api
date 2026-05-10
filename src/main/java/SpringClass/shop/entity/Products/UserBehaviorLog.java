package SpringClass.shop.entity.Products;

import SpringClass.shop.entity.Users.Users;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_behavior_logs")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBehaviorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Products product;

    @Column(name = "behavior_type", nullable = false, length = 20)
    private String behaviorType;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
