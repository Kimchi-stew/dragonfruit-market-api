package SpringClass.shop.dto.Users.response;

import SpringClass.shop.entity.Users.Users;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class UserSummaryDTO {
    private Long id;
    private String email;
    private String nickname;

    public static UserSummaryDTO from(Users users) {
        return UserSummaryDTO.builder()
                .id(users.getId())
                .email(users.getEmail())
                .nickname(users.getNickname())
                .build();
    }
}
