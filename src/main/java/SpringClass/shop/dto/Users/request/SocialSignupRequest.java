package SpringClass.shop.dto.Users.request;

import SpringClass.shop.enums.GenderRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SocialSignupRequest {

    @NotBlank(message = "닉네임은 필수 입력값입니다.")
    private String nickname;

    @NotNull(message = "성별은 필수 입력값입니다.")
    private GenderRole gender;
}
