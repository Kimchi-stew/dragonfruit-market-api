package SpringClass.shop.service;

import SpringClass.shop.dto.Media.response.FileResponse;
import SpringClass.shop.entity.Medias.Medias;
import SpringClass.shop.entity.Users.Users;
import SpringClass.shop.enums.GenderRole;
import SpringClass.shop.enums.MediaEntityType;
import SpringClass.shop.enums.UserRole;
import SpringClass.shop.exceptions.common.ForbiddenException;
import SpringClass.shop.exceptions.media.MediaNotFoundException;
import SpringClass.shop.repository.Medias.MediasRepository;
import SpringClass.shop.security.SecurityUtils;
import com.amazonaws.services.s3.AmazonS3;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock private AmazonS3 amazonS3;
    @Mock private MediasRepository mediasRepository;
    @Mock private SecurityUtils securityUtils;

    @InjectMocks private FileService fileService;

    private static final String BUCKET = "test-bucket";
    private Users testUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(fileService, "bucket", BUCKET);
        testUser = Users.builder()
                .id(1L).email("user@test.com").password("pw")
                .nickname("user").gender(GenderRole.M).userRole(UserRole.USER).build();
    }

    private MultipartFile mockFile(String filename) throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn(filename);
        when(file.getSize()).thenReturn(1024L);
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getInputStream()).thenReturn(InputStream.nullInputStream());
        return file;
    }

    private Medias buildMedia(Long id, MediaEntityType type, Long entityId, String storedName, String url) {
        return Medias.builder()
                .id(id).user(testUser).entityType(type).entityId(entityId)
                .originalName("original.jpg").storedName(storedName)
                .url(url).mimeType("image/jpeg").fileSize(1024L).build();
    }

    private URL toUrl(String url) {
        try { return new URL(url); } catch (MalformedURLException e) { throw new RuntimeException(e); }
    }

    // ── upload ──────────────────────────────────────────────────

    @Test
    @DisplayName("PROFILE 업로드 시 entityId가 현재 유저 ID로 자동 설정된다")
    void upload_PROFILE_entityIdIsUserId() throws IOException {
        MultipartFile file = mockFile("profile.jpg");
        String url = "https://bucket.s3.amazonaws.com/profile/uuid.jpg";
        when(securityUtils.getCurrentUser()).thenReturn(testUser);
        when(amazonS3.getUrl(eq(BUCKET), anyString())).thenReturn(toUrl(url));
        Medias saved = buildMedia(1L, MediaEntityType.PROFILE, 1L, "uuid.jpg", url);
        when(mediasRepository.save(any())).thenReturn(saved);

        FileResponse result = fileService.upload(file, MediaEntityType.PROFILE);

        assertThat(result.getEntityId()).isEqualTo(testUser.getId());
        assertThat(result.getEntityType()).isEqualTo(MediaEntityType.PROFILE);
        verify(mediasRepository).save(argThat(m -> testUser.getId().equals(m.getEntityId())));
    }

    @Test
    @DisplayName("PRODUCT 업로드 시 entityId가 null이다")
    void upload_PRODUCT_entityIdIsNull() throws IOException {
        MultipartFile file = mockFile("product.jpg");
        String url = "https://bucket.s3.amazonaws.com/product/uuid.jpg";
        when(securityUtils.getCurrentUser()).thenReturn(testUser);
        when(amazonS3.getUrl(eq(BUCKET), anyString())).thenReturn(toUrl(url));
        Medias saved = buildMedia(2L, MediaEntityType.PRODUCT, null, "uuid.jpg", url);
        when(mediasRepository.save(any())).thenReturn(saved);

        FileResponse result = fileService.upload(file, MediaEntityType.PRODUCT);

        assertThat(result.getEntityId()).isNull();
        verify(mediasRepository).save(argThat(m -> m.getEntityId() == null));
    }

    @Test
    @DisplayName("REVIEW 업로드 시 S3 경로가 review/로 시작한다")
    void upload_REVIEW_s3KeyStartsWithReview() throws IOException {
        MultipartFile file = mockFile("review.jpg");
        when(securityUtils.getCurrentUser()).thenReturn(testUser);
        when(amazonS3.getUrl(anyString(), anyString())).thenReturn(toUrl("https://bucket.s3.amazonaws.com/review/uuid.jpg"));
        when(mediasRepository.save(any())).thenReturn(buildMedia(3L, MediaEntityType.REVIEW, null, "uuid.jpg", "url"));

        fileService.upload(file, MediaEntityType.REVIEW);

        verify(amazonS3).putObject(eq(BUCKET), argThat(key -> key.startsWith("review/")), any(), any());
    }

    // ── delete ──────────────────────────────────────────────────

    @Test
    @DisplayName("본인 파일 삭제 시 S3와 DB 모두 삭제된다")
    void delete_ownMedia_success() {
        String storedName = "uuid_product.jpg";
        Medias media = buildMedia(1L, MediaEntityType.PRODUCT, 10L, storedName,
                "https://bucket.s3.amazonaws.com/product/" + storedName);
        when(securityUtils.getCurrentUser()).thenReturn(testUser);
        when(mediasRepository.findByIdAndUser_Id(1L, 1L)).thenReturn(Optional.of(media));

        fileService.delete(1L);

        verify(amazonS3).deleteObject(eq(BUCKET), eq("product/" + storedName));
        verify(mediasRepository).delete(media);
    }

    @Test
    @DisplayName("존재하지 않는 파일 삭제 시 MediaNotFoundException 발생")
    void delete_notFound_throwsMediaNotFoundException() {
        when(securityUtils.getCurrentUser()).thenReturn(testUser);
        when(mediasRepository.findByIdAndUser_Id(999L, 1L)).thenReturn(Optional.empty());
        when(mediasRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> fileService.delete(999L))
                .isInstanceOf(MediaNotFoundException.class);
    }

    @Test
    @DisplayName("타인 파일 삭제 시 ForbiddenException 발생")
    void delete_otherUserMedia_throwsForbiddenException() {
        when(securityUtils.getCurrentUser()).thenReturn(testUser);
        when(mediasRepository.findByIdAndUser_Id(1L, 1L)).thenReturn(Optional.empty());
        when(mediasRepository.existsById(1L)).thenReturn(true);

        assertThatThrownBy(() -> fileService.delete(1L))
                .isInstanceOf(ForbiddenException.class);
    }

    // ── linkMedias ───────────────────────────────────────────────

    @Test
    @DisplayName("linkMedias 호출 시 모든 media의 entityId가 업데이트된다")
    void linkMedias_updatesEntityId() {
        Medias m1 = buildMedia(1L, MediaEntityType.PRODUCT, null, "uuid1.jpg", "url1");
        Medias m2 = buildMedia(2L, MediaEntityType.PRODUCT, null, "uuid2.jpg", "url2");
        when(mediasRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(m1, m2));

        fileService.linkMedias(List.of(1L, 2L), 100L);

        assertThat(m1.getEntityId()).isEqualTo(100L);
        assertThat(m2.getEntityId()).isEqualTo(100L);
        verify(mediasRepository).saveAll(List.of(m1, m2));
    }

    @Test
    @DisplayName("빈 목록으로 linkMedias 호출 시 DB 접근하지 않는다")
    void linkMedias_emptyList_doesNothing() {
        fileService.linkMedias(List.of(), 100L);
        verifyNoInteractions(mediasRepository);
    }

    @Test
    @DisplayName("null로 linkMedias 호출 시 DB 접근하지 않는다")
    void linkMedias_null_doesNothing() {
        fileService.linkMedias(null, 100L);
        verifyNoInteractions(mediasRepository);
    }

    // ── getUrlsByIds ──────────────────────────────────────────────

    @Test
    @DisplayName("getUrlsByIds는 입력 ID 순서를 유지하며 URL을 반환한다")
    void getUrlsByIds_preservesInputOrder() {
        Medias m1 = buildMedia(1L, MediaEntityType.PRODUCT, 10L, "uuid1.jpg", "https://url1.jpg");
        Medias m2 = buildMedia(2L, MediaEntityType.PRODUCT, 10L, "uuid2.jpg", "https://url2.jpg");
        when(mediasRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(m2, m1)); // DB 순서 다름

        List<String> urls = fileService.getUrlsByIds(List.of(1L, 2L));

        assertThat(urls).containsExactly("https://url1.jpg", "https://url2.jpg");
    }

    @Test
    @DisplayName("getUrlsByIds에 빈 목록 전달 시 빈 리스트 반환")
    void getUrlsByIds_emptyList_returnsEmpty() {
        assertThat(fileService.getUrlsByIds(List.of())).isEmpty();
    }

    // ── getFiles ──────────────────────────────────────────────────

    @Test
    @DisplayName("getFiles는 entityType과 entityId로 파일 목록을 반환한다")
    void getFiles_returnsFileResponseList() {
        Medias m1 = buildMedia(1L, MediaEntityType.PRODUCT, 10L, "uuid1.jpg", "url1");
        Medias m2 = buildMedia(2L, MediaEntityType.PRODUCT, 10L, "uuid2.jpg", "url2");
        when(mediasRepository.findByEntityTypeAndEntityId(MediaEntityType.PRODUCT, 10L))
                .thenReturn(List.of(m1, m2));

        List<FileResponse> result = fileService.getFiles(MediaEntityType.PRODUCT, 10L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUrl()).isEqualTo("url1");
        assertThat(result.get(1).getUrl()).isEqualTo("url2");
        assertThat(result).allMatch(r -> r.getEntityType() == MediaEntityType.PRODUCT);
    }

    @Test
    @DisplayName("해당 엔티티에 파일이 없으면 빈 목록 반환")
    void getFiles_noMedia_returnsEmptyList() {
        when(mediasRepository.findByEntityTypeAndEntityId(any(), any())).thenReturn(List.of());

        assertThat(fileService.getFiles(MediaEntityType.REVIEW, 99L)).isEmpty();
    }
}
