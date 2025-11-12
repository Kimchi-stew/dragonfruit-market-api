package SpringClass.shop.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.cglib.core.Local;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Payments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @OneToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Orders order;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(nullable = false, name="payment_method")
    private String paymentMethod;

    @Column(nullable = false, name="payment_status")
    private String paymentStatus;

    @Column(nullable = false, name="amount", precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name="transaction_id", unique = true)
    private String transactionId;

    @Column(name="paid_at")
    private LocalDateTime paidAt;

    @Column(name="canceled_at")
    private LocalDateTime canceledAt;

    @Column(nullable = false, name="created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(nullable = false, name="updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;


}
