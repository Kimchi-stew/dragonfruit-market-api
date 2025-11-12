package SpringClass.shop.dto;

import SpringClass.shop.enums.GenderRole;
import SpringClass.shop.enums.UserRole;
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
