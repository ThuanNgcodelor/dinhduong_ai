package com.david.NUTRITION_TRACNKER.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.david.NUTRITION_TRACNKER.service.oauth2.CustomOAuth2UserService;
import com.david.NUTRITION_TRACNKER.security.JwtAuthenticationFilter;
import com.david.NUTRITION_TRACNKER.security.CustomAuthenticationSuccessHandler;
import com.david.NUTRITION_TRACNKER.security.OAuth2AuthenticationSuccessHandler;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    @Autowired
    private OAuth2AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers(
                        "/",
                        "/login",
                        "/register/**",
                        "/forgot-password/**",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/icons/**",
                        "/default/**",
                        "/uploads/**",
                        "/recipes/suggestions",
                        "/recipes/detail/**",
                        "/recipes/search",
                        "/ai-tools/calorie-estimator",
                        "/collections/view/**"
                ).permitAll()
                .requestMatchers("/ai-tools/**").authenticated()
                .requestMatchers("/recipes/create", "/recipes/save").authenticated()
                // --- SỬA ĐỔI TẠI ĐÂY ---

                // 1. Rule cụ thể phải đứng trước. 
                // Cho phép cả ADMIN và NUTRITIONIST vào trang quản lý nguyên liệu
                // Dùng /** để bao gồm cả các action con như /add, /delete
                .requestMatchers("/admin/ingredients/**", "/admin/categories/**", "/admin/dashboard").hasAnyRole("ADMIN", "NUTRITIONIST")
                // 2. Các trang Nutritionist khác (nếu có, ví dụ duyệt bài)
                .requestMatchers("/nutritionist/**", "/admin/ingredient/**").hasAnyRole("ADMIN", "NUTRITIONIST")
                // 3. Rule tổng quát cho Admin đứng cuối cùng trong nhóm admin
                // Tất cả các trang /admin/... còn lại chỉ dành cho ADMIN
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // -----------------------

                .anyRequest().authenticated()
        )
                .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/authenticateTheUser")
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler(customAuthenticationSuccessHandler)
                .permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                .successHandler(oauth2AuthenticationSuccessHandler)
                )
                .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .deleteCookies("accessToken", "refreshToken", "JSESSIONID")
                .permitAll()
                )
                .csrf(csrf -> csrf.disable());

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
