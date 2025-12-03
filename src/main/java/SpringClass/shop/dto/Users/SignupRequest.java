package SpringClass.shop.dto.Users;

import SpringClass.shop.enums.GenderRole;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {

    @NotBlank(message = "이메일은 필수 입력값입니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    private String password;

    @NotBlank(message = "닉네임은 필수 입력값입니다.")
    private String nickname;

    @NotBlank(message = "성별은 필수 입력값입니다.")
    private GenderRole gender;
    private String profileImage;

}
