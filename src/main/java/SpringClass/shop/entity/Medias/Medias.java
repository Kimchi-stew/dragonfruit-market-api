package SpringClass.shop.entity.Medias;

import SpringClass.shop.entity.Users.Users;
import SpringClass.shop.enums.MediaEntityType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "medias", indexes = {
        @Index(name = "idx_medias_entity", columnList = "entity_type, entity_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Medias {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 30)
    private MediaEntityType entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    @Column(name = "stored_name", nullable = false, length = 255)
    private String storedName;

    @Column(nullable = false, length = 1000)
    private String url;

    @Column(name = "mime_type", length = 128)
    private String mimeType;

    @Column(name = "file_size")
    private Long fileSize;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
