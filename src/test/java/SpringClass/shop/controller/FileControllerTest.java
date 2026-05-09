package SpringClass.shop.controller;

import SpringClass.shop.dto.Media.response.FileResponse;
import SpringClass.shop.enums.MediaEntityType;
import SpringClass.shop.exceptions.common.ForbiddenException;
import SpringClass.shop.exceptions.media.MediaNotFoundException;
import SpringClass.shop.global.GlobalApiResponseHandler;
import SpringClass.shop.service.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class FileControllerTest {

    @Mock private FileService fileService;
    @InjectMocks private FileController fileController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(fileController)
                .setControllerAdvice(new GlobalApiResponseHandler())
                .build();
    }

    // ── POST /files/upload ────────────────────────────────────────

    @Test
    @DisplayName("PRODUCT 파일 업로드 성공 시 200과 FileResponse 반환")
    void uploadFile_PRODUCT_success() throws Exception {
        FileResponse response = new FileResponse(1L, "https://s3.amazonaws.com/product/uuid.jpg",
                MediaEntityType.PRODUCT, null);
        when(fileService.upload(any(), eq(MediaEntityType.PRODUCT))).thenReturn(response);

        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "content".getBytes());

        mockMvc.perform(multipart("/files/upload")
                        .file(file)
                        .param("entityType", "PRODUCT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.url").value("https://s3.amazonaws.com/product/uuid.jpg"))
                .andExpect(jsonPath("$.data.entityType").value("PRODUCT"));
    }

    @Test
    @DisplayName("PROFILE 업로드 시 entityId가 유저 ID로 채워진 응답 반환")
    void uploadFile_PROFILE_success() throws Exception {
        FileResponse response = new FileResponse(2L, "https://s3.amazonaws.com/profile/uuid.jpg",
                MediaEntityType.PROFILE, 1L);
        when(fileService.upload(any(), eq(MediaEntityType.PROFILE))).thenReturn(response);

        MockMultipartFile file = new MockMultipartFile("file", "profile.jpg", "image/jpeg", "img".getBytes());

        mockMvc.perform(multipart("/files/upload")
                        .file(file)
                        .param("entityType", "PROFILE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.entityId").value(1))
                .andExpect(jsonPath("$.data.entityType").value("PROFILE"));
    }

    @Test
    @DisplayName("잘못된 entityType 전달 시 400 반환")
    void uploadFile_invalidEntityType_returns400() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "content".getBytes());

        mockMvc.perform(multipart("/files/upload")
                        .file(file)
                        .param("entityType", "INVALID_TYPE"))
                .andExpect(status().isBadRequest());
    }

    // ── GET /files ────────────────────────────────────────────────

    @Test
    @DisplayName("미디어 목록 조회 성공 시 200과 FileResponse 목록 반환")
    void getFiles_success() throws Exception {
        List<FileResponse> responses = List.of(
                new FileResponse(1L, "https://url1.jpg", MediaEntityType.PRODUCT, 10L),
                new FileResponse(2L, "https://url2.jpg", MediaEntityType.PRODUCT, 10L)
        );
        when(fileService.getFiles(MediaEntityType.PRODUCT, 10L)).thenReturn(responses);

        mockMvc.perform(get("/files")
                        .param("entityType", "PRODUCT")
                        .param("entityId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].url").value("https://url1.jpg"))
                .andExpect(jsonPath("$.data[1].entityId").value(10));
    }

    @Test
    @DisplayName("파일 없는 엔티티 조회 시 200과 빈 배열 반환")
    void getFiles_noMedia_returnsEmptyList() throws Exception {
        when(fileService.getFiles(any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/files")
                        .param("entityType", "REVIEW")
                        .param("entityId", "99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    // ── DELETE /files/{mediaId} ───────────────────────────────────

    @Test
    @DisplayName("본인 파일 삭제 성공 시 200 반환")
    void deleteFile_success() throws Exception {
        doNothing().when(fileService).delete(1L);

        mockMvc.perform(delete("/files/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("파일이 삭제되었습니다."));
    }

    @Test
    @DisplayName("존재하지 않는 파일 삭제 시 404 반환")
    void deleteFile_notFound_returns404() throws Exception {
        doThrow(new MediaNotFoundException("파일을 찾을 수 없습니다.")).when(fileService).delete(999L);

        mockMvc.perform(delete("/files/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("타인 파일 삭제 시 403 반환")
    void deleteFile_forbidden_returns403() throws Exception {
        doThrow(new ForbiddenException("본인이 업로드한 파일만 삭제할 수 있습니다.")).when(fileService).delete(1L);

        mockMvc.perform(delete("/files/1"))
                .andExpect(status().isForbidden());
    }
}
