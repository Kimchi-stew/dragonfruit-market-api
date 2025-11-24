package SpringClass.shop.dto.Auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class AutoLoginRequest {
    private String refreshToken;
}
