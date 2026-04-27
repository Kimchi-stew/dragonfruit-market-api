package SpringClass.shop.dto.Media.response;

import SpringClass.shop.enums.MediaEntityType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FileResponse {
    private Long id;
    private String url;
    private MediaEntityType entityType;
    private Long entityId;
}
