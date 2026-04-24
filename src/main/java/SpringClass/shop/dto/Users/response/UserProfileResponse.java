package SpringClass.shop.dto.Users.response;

import SpringClass.shop.enums.GenderRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String email;
    private String nickname;
    private GenderRole gender;
    private String profileImage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
