package SpringClass.shop.entity.Users;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class RefreshToken {

    @Id
    @Column(name="token", nullable = false)
    private String refreshToken;

    @Column(nullable = false, name="expiry_date")
    @DateTimeFormat(pattern = "yyyy-MM") // 6자리
    private LocalDateTime expiryDate;

    @Column(name="email", nullable = false)
    private String email;
}
