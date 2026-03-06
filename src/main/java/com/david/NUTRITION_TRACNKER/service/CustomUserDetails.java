package com.david.NUTRITION_TRACNKER.service;


import com.david.NUTRITION_TRACNKER.entity.User;
import com.david.NUTRITION_TRACNKER.entity.enums.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class CustomUserDetails implements UserDetails, OAuth2User {

    private final User user;
    private Map<String, Object> attributes;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    public CustomUserDetails(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    // --- Phương thức quan trọng để Controller lấy được UserId ---
    public Integer getUserId() {
        return user.getUserId();
    }
    
    public User getUser() {
        return user;
    }
    
    public UserRole getUserRole() {
        return user.getRole();
    }

    // --- Các phương thức bắt buộc của Spring Security ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Chuyển Role từ DB (ADMIN, USER) thành quyền hạn Spring Security
        // Lưu ý: Spring Security thường yêu cầu prefix "ROLE_"
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }
    
    public String getFullName() {
        return user.getFullName(); 
    }
    
    public String getDisplayName() {
        return (user.getFullName() != null && !user.getFullName().isEmpty()) 
               ? user.getFullName() 
               : user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.getStatus() != null && "ACTIVE".equals(user.getStatus().name());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
//        return Boolean.TRUE.equals(user.getIsEmailVerified()); 
        return true;
    }

    // --- OAuth2User methods ---
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return user.getFullName();
    }
}