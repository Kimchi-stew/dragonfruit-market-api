package SpringClass.shop.dto.Users.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class UserPasswordDTO {
    private String password;
    private String newPassword;
}
