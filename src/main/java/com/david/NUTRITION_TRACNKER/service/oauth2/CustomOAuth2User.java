package com.david.NUTRITION_TRACNKER.service.oauth2;

import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class CustomOAuth2User implements OAuth2User {

    private final OAuth2User oauth2User;
    private final String email;
    private final String fullName;

    public CustomOAuth2User(OAuth2User oauth2User) {
        this.oauth2User = oauth2User;
        this.email = oauth2User.getAttribute("email");
        this.fullName = oauth2User.getAttribute("name");
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oauth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return oauth2User.getAuthorities();
    }

    @Override
    public String getName() {
        return oauth2User.getAttribute("name");
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }
}
