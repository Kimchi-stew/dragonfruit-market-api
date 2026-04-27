package SpringClass.shop.controller;

import SpringClass.shop.dto.Media.response.FileResponse;
import SpringClass.shop.enums.MediaEntityType;
import SpringClass.shop.global.ApiResponse;
import SpringClass.shop.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/files")
public class FileController {

    private final FileService fileService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "파일 업로드", description = "S3에 파일을 업로드하고 DB에 저장합니다. entityType: PRODUCT, REVIEW, PROFILE")
    public ResponseEntity<ApiResponse<FileResponse>> uploadFile(
            @Parameter(description = "업로드할 파일") @RequestPart("file") MultipartFile file,
            @RequestParam("entityType") MediaEntityType entityType) throws IOException {
        FileResponse result = fileService.upload(file, entityType);
        return ResponseEntity.ok(ApiResponse.ok(result, "파일이 업로드되었습니다."));
    }

    @GetMapping
    @Operation(summary = "미디어 목록 조회", description = "entityType과 entityId로 해당 엔티티의 파일 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<FileResponse>>> getFiles(
            @RequestParam MediaEntityType entityType,
            @RequestParam Long entityId) {
        List<FileResponse> result = fileService.getFiles(entityType, entityId);
        return ResponseEntity.ok(ApiResponse.ok(result, "요청이 성공적으로 처리되었습니다."));
    }

    @DeleteMapping("/{mediaId}")
    @Operation(summary = "파일 삭제", description = "S3에서 파일을 삭제하고 DB에서도 제거합니다. 본인이 업로드한 파일만 삭제 가능합니다.")
    public ResponseEntity<ApiResponse<Void>> deleteFile(@PathVariable Long mediaId) {
        fileService.delete(mediaId);
        return ResponseEntity.ok(ApiResponse.ok("파일이 삭제되었습니다."));
    }
}
