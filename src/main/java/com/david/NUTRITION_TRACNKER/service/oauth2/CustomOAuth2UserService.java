package com.david.NUTRITION_TRACNKER.service.oauth2;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.david.NUTRITION_TRACNKER.entity.User;
import com.david.NUTRITION_TRACNKER.entity.enums.AuthProvider;
import com.david.NUTRITION_TRACNKER.entity.enums.UserRole;
import com.david.NUTRITION_TRACNKER.entity.enums.UserStatus;
import com.david.NUTRITION_TRACNKER.repository.UserRepository;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            // Register new Google User
            User newUser = User.builder()
                    .email(email)
                    .fullName(name)
                    .passwordHash(UUID.randomUUID().toString())
                    .role(UserRole.USER)
                    .status(UserStatus.ACTIVE)
                    .isEmailVerified(true)
                    .createdAt(LocalDateTime.now())
                    .isDeleted(false)
                    .authProvider(AuthProvider.GOOGLE)
                    .build();
            return userRepository.save(newUser);
        });
        
        return new com.david.NUTRITION_TRACNKER.service.CustomUserDetails(user, oauth2User.getAttributes());
    }
}
