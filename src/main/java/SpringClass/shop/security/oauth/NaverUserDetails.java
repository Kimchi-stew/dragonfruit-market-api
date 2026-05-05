package SpringClass.shop.security.oauth;

import SpringClass.shop.repository.oauth.OAuth2UserInfo;
import lombok.AllArgsConstructor;

import java.util.Map;

@AllArgsConstructor
public class NaverUserDetails implements OAuth2UserInfo {

    private Map<String, Object> attributes;

    @Override
    public String getProvider() {
        return "naver";
    }

    @Override
    public String getProviderId() {
        return (String) ((Map) attributes.get("response")).get("id");
    }

    @Override
    public String getEmail() {
        return (String) ((Map) attributes.get("response")).get("email");
    }

}
