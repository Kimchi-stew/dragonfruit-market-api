package SpringClass.shop.service;

import SpringClass.shop.dto.Media.response.FileResponse;
import SpringClass.shop.entity.Medias.Medias;
import SpringClass.shop.entity.Users.Users;
import SpringClass.shop.enums.MediaEntityType;
import SpringClass.shop.exceptions.ForbiddenException;
import SpringClass.shop.exceptions.MediaNotFoundException;
import SpringClass.shop.repository.Medias.MediasRepository;
import SpringClass.shop.security.SecurityUtils;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private final AmazonS3 amazonS3;
    private final MediasRepository mediasRepository;
    private final SecurityUtils securityUtils;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Transactional
    public FileResponse upload(MultipartFile file, MediaEntityType entityType) throws IOException {
        Users user = securityUtils.getCurrentUser();

        Long entityId = (entityType == MediaEntityType.PROFILE) ? user.getId() : null;

        String originalName = file.getOriginalFilename();
        String storedName = UUID.randomUUID() + "_" + originalName;
        String s3Key = entityType.name().toLowerCase() + "/" + storedName;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        amazonS3.putObject(bucket, s3Key, file.getInputStream(), metadata);
        String url = amazonS3.getUrl(bucket, s3Key).toString();

        Medias media = Medias.builder()
                .user(user)
                .entityType(entityType)
                .entityId(entityId)
                .originalName(originalName)
                .storedName(storedName)
                .url(url)
                .mimeType(file.getContentType())
                .fileSize(file.getSize())
                .build();

        Medias saved = mediasRepository.save(media);
        return new FileResponse(saved.getId(), saved.getUrl(), saved.getEntityType(), saved.getEntityId());
    }

    @Transactional
    public void linkMedias(List<Long> mediaIds, Long entityId) {
        if (mediaIds == null || mediaIds.isEmpty()) return;
        List<Medias> medias = mediasRepository.findAllById(mediaIds);
        medias.forEach(m -> m.setEntityId(entityId));
        mediasRepository.saveAll(medias);
    }

    public List<String> getUrlsByIds(List<Long> mediaIds) {
        if (mediaIds == null || mediaIds.isEmpty()) return List.of();
        Map<Long, String> urlMap = mediasRepository.findAllById(mediaIds)
                .stream()
                .collect(java.util.stream.Collectors.toMap(Medias::getId, Medias::getUrl));
        return mediaIds.stream()
                .map(id -> urlMap.getOrDefault(id, null))
                .filter(url -> url != null)
                .toList();
    }

    public List<FileResponse> getFiles(MediaEntityType entityType, Long entityId) {
        return mediasRepository.findByEntityTypeAndEntityId(entityType, entityId)
                .stream()
                .map(m -> new FileResponse(m.getId(), m.getUrl(), m.getEntityType(), m.getEntityId()))
                .toList();
    }

    @Transactional
    public void delete(Long mediaId) {
        Users user = securityUtils.getCurrentUser();

        Medias media = mediasRepository.findByIdAndUser_Id(mediaId, user.getId())
                .orElseThrow(() -> {
                    if (!mediasRepository.existsById(mediaId)) {
                        return new MediaNotFoundException("파일을 찾을 수 없습니다.");
                    }
                    return new ForbiddenException("본인이 업로드한 파일만 삭제할 수 있습니다.");
                });

        String s3Key = media.getEntityType().name().toLowerCase() + "/" + media.getStoredName();
        amazonS3.deleteObject(bucket, s3Key);
        mediasRepository.delete(media);
    }
}
