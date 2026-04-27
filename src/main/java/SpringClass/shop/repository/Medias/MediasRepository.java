package SpringClass.shop.repository.Medias;

import SpringClass.shop.entity.Medias.Medias;
import SpringClass.shop.enums.MediaEntityType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MediasRepository extends JpaRepository<Medias, Long> {
    List<Medias> findByEntityTypeAndEntityId(MediaEntityType entityType, Long entityId);
    Optional<Medias> findByIdAndUser_Id(Long id, Long userId);
    Optional<Medias> findTopByEntityTypeAndEntityIdOrderByCreatedAtDesc(MediaEntityType entityType, Long entityId);
}
