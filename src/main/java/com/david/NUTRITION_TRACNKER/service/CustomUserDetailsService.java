package com.david.NUTRITION_TRACNKER.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.david.NUTRITION_TRACNKER.entity.User;
import com.david.NUTRITION_TRACNKER.entity.enums.AuthProvider;
import com.david.NUTRITION_TRACNKER.entity.enums.UserStatus;
import com.david.NUTRITION_TRACNKER.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UsernameNotFoundException("Tài khoản chưa được kích hoạt");
        }

        if (user.getAuthProvider() == AuthProvider.GOOGLE) {
            throw new UsernameNotFoundException("Bạn đã đăng ký bằng google, vui lòng login bằng google giúp tôi");
        }

        return new CustomUserDetails(user); 
    }
}