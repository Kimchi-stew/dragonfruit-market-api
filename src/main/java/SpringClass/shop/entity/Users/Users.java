package SpringClass.shop.entity.Users;

import SpringClass.shop.enums.GenderRole;
import SpringClass.shop.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

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

    @Column(nullable = true)
    private String nickname;

    @Column(nullable = true)
    @Enumerated(EnumType.STRING)
    private GenderRole gender;

    @Column(name="user_role")
    @Enumerated(EnumType.STRING)
    private UserRole userRole;


    // 소셜 로그인
    @Column(name="provider")
    private String provider;

    @Column(name="provider_id")
    private String providerId;

    @Column(nullable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @LastModifiedDate
    private LocalDateTime updatedAt;

   
}
