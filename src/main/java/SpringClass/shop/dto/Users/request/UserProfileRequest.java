package SpringClass.shop.dto.Users.request;

import SpringClass.shop.enums.GenderRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class UserProfileRequest {
    private String email;
    private String nickname;
    private GenderRole gender;
    private String profileImage;
}
