package SpringClass.shop.dto.Users;

import SpringClass.shop.enums.GenderRole;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {
    private String email;
    private String password;
    private String nickname;
    private GenderRole gender;
    private String profileImage;

}
