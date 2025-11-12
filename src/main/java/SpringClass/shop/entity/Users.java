package SpringClass.shop.entity;

import SpringClass.shop.entity.Products.ProductLikes;
import SpringClass.shop.entity.Reviews.ReviewLikes;
import SpringClass.shop.entity.Reviews.Reviews;
import SpringClass.shop.enums.GenderRole;
import SpringClass.shop.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private GenderRole gender;

    @Column(length = 1000)
    private String profileImage;

    @Column(name="user_role")
    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    @Column(nullable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @LastModifiedDate
    private LocalDateTime updatedAt;

//    // 관계
//    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
//    private Sellers sellers;
//
//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
//    private List<Orders> orders;
//
//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
//    private List<Reviews> reviews;
//
//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
//    private List<ReviewLikes> reviewLikes;
//
//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
//    private List<ProductLikes> productLikes;
}
